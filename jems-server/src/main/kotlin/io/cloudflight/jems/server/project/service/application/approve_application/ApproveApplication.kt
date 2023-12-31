package io.cloudflight.jems.server.project.service.application.approve_application

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.controllerInstitution.service.ControllerInstitutionPersistence
import io.cloudflight.jems.server.notification.handler.ProjectStatusChangeEvent
import io.cloudflight.jems.server.project.authorization.CanApproveApplication
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectVersionPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationActionInfo
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.application.ifIsValid
import io.cloudflight.jems.server.project.service.application.workflow.ApplicationStateFactory
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApproveApplication(
    private val projectPersistence: ProjectPersistence,
    private val generalValidatorService: GeneralValidatorService,
    private val applicationStateFactory: ApplicationStateFactory,
    private val projectVersionPersistence: ProjectVersionPersistence,
    private val partnerPersistence: PartnerPersistence,
    private val controllerInstitutionPersistence: ControllerInstitutionPersistence,
    private val auditPublisher: ApplicationEventPublisher
) : ApproveApplicationInteractor {

    @CanApproveApplication
    @Transactional
    @ExceptionWrapper(ApproveApplicationException::class)
    override fun approve(projectId: Long, actionInfo: ApplicationActionInfo): ApplicationStatus =
        actionInfo.ifIsValid(generalValidatorService).let {
            projectPersistence.getProjectSummary(projectId).let { projectSummary ->
                applicationStateFactory.getInstance(projectSummary).approve(actionInfo).also {
                    if(projectSummary.isInStep2()) {
                        projectVersionPersistence.saveTimestampForApprovedApplication(projectId)
                        // initialize data inside institution-assignment table
                        controllerInstitutionPersistence.updatePartnerDataInAssignments(
                            partners = partnerPersistence.getCurrentPartnerAssignmentMetadata(projectId)
                        )
                    }
                    auditPublisher.publishEvent(ProjectStatusChangeEvent(this, projectSummary, it))
                }
            }
        }
}
