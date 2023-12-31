package io.cloudflight.jems.server.project.service.report.partner.file.setDescriptionToFile

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.JemsProjectFileService
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.authorization.CanEditPartnerReport
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.common.file.service.model.JemsFileType.PartnerReport
import io.cloudflight.jems.server.project.service.report.partner.SensitiveDataAuthorizationService
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectPartnerReportExpenditurePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SetDescriptionToProjectPartnerReportFile(
    private val partnerPersistence: PartnerPersistence,
    private val filePersistence: JemsFilePersistence,
    private val fileService: JemsProjectFileService,
    private val generalValidator: GeneralValidatorService,
    private val reportExpenditurePersistence: ProjectPartnerReportExpenditurePersistence,
    private val sensitiveDataAuthorization: SensitiveDataAuthorizationService
) : SetDescriptionToProjectPartnerReportFileInteractor {

    @CanEditPartnerReport
    @Transactional
    @ExceptionWrapper(SetDescriptionToProjectPartnerReportFileException::class)
    override fun setDescription(partnerId: Long, reportId: Long, fileId: Long, description: String) {
        validateDescription(text = description)

        if(isGdprProtected(fileId = fileId, partnerId = partnerId) &&
            !sensitiveDataAuthorization.canEditPartnerSensitiveData(partnerId)) {
            throw SensitiveFileException()
        }

        val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)
        // to make sure fileId corresponds to correct report, we need to verify it through location path
        val reportPrefix = PartnerReport.generatePath(projectId, partnerId, reportId)

        if (!filePersistence.existsFile(partnerId = partnerId, pathPrefix = reportPrefix, fileId = fileId))
            throw FileNotFound()

        fileService.setDescription(fileId = fileId, description = description)
    }

    private fun validateDescription(text: String) {
        generalValidator.throwIfAnyIsInvalid(
            generalValidator.maxLength(text, 250, "description"),
        )
    }

    private fun isGdprProtected(fileId: Long, partnerId: Long) =
        reportExpenditurePersistence.existsByPartnerIdAndAttachmentIdAndGdprTrue(partnerId = partnerId, fileId = fileId)

}
