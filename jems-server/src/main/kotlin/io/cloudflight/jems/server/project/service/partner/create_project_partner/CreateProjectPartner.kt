package io.cloudflight.jems.server.project.service.partner.create_project_partner

import io.cloudflight.jems.api.call.dto.CallType
import io.cloudflight.jems.api.project.dto.description.ProjectTargetGroupDTO
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.authorization.CanUpdateProjectForm
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartner
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerDetail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val MAX_NUMBER_OF_PROJECT_PARTNERS = 50 // previous value = 30

@Service
class CreateProjectPartner(
    private val persistence: PartnerPersistence,
    private val projectPersistence: ProjectPersistence,
    private val generalValidator: GeneralValidatorService
) : CreateProjectPartnerInteractor {

    @CanUpdateProjectForm
    @Transactional
    @ExceptionWrapper(CreateProjectPartnerException::class)
    override fun create(projectId: Long, projectPartner: ProjectPartner): ProjectPartnerDetail =
        ifProjectPartnerIsValid(projectPartner).run {

            if (isProjectCallNonStandard(projectId) && persistence.countByProjectIdActive(projectId) >= 1)
                throw MaximumNumberOfActivePartnersReached(1)

            if (persistence.countByProjectId(projectId) >= MAX_NUMBER_OF_PROJECT_PARTNERS)
                throw MaximumNumberOfPartnersReached(MAX_NUMBER_OF_PROJECT_PARTNERS)

            if (projectPartner.role!!.isLead)
                persistence.changeRoleOfLeadPartnerToPartnerIfItExists(projectId)

            persistence.throwIfPartnerAbbreviationAlreadyExists(projectId, projectPartner.abbreviation!!)

            persistence.create(projectId, projectPartner, shouldResortPartnersByRole(projectId))
        }


    private fun ifProjectPartnerIsValid(partner: ProjectPartner) =
        generalValidator.throwIfAnyIsInvalid(
            generalValidator.notEqualTo(partner.partnerType.toString(), ProjectTargetGroupDTO.GeneralPublic.toString(), "partnerType"),
            generalValidator.nullOrZero(partner.id, "id"),
            generalValidator.notNull(partner.role, "role"),
            generalValidator.notBlank(partner.abbreviation, "abbreviation"),
            generalValidator.maxLength(partner.abbreviation, 15, "abbreviation"),
            generalValidator.maxLength(partner.nameInOriginalLanguage, 250, "nameInOriginalLanguage"),
            generalValidator.maxLength(partner.nameInEnglish, 250, "nameInEnglish"),
            generalValidator.notNull(partner.legalStatusId, "legalStatusId"),
            generalValidator.maxLength(partner.otherIdentifierNumber, 50, "otherIdentifierNumber"),
            generalValidator.maxLength(partner.otherIdentifierDescription, 100, "otherIdentifierDescription"),
            generalValidator.exactLength(partner.pic, 9, "pic"),
            generalValidator.onlyDigits(partner.pic, "pic"),
            generalValidator.maxLength(partner.vat, 50, "vat"),
        )

    private fun shouldResortPartnersByRole(projectId: Long) =
        projectPersistence.getProjectSummary(projectId).status.isModifiableStatusBeforeApproved()

    private fun isProjectCallNonStandard(projectId: Long) =
        projectPersistence.getProjectCallSettings(projectId).callType != CallType.STANDARD
}
