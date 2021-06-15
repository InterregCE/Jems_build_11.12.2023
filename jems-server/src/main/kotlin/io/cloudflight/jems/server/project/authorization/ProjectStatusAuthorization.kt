package io.cloudflight.jems.server.project.authorization

import io.cloudflight.jems.server.authentication.authorization.Authorization
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectService
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.APPROVED
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.APPROVED_WITH_CONDITIONS
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.ELIGIBLE
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.STEP1_APPROVED
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.STEP1_APPROVED_WITH_CONDITIONS
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.STEP1_ELIGIBLE
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.STEP1_SUBMITTED
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.SUBMITTED
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component


@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectStatusAuthorization.canSubmit(#projectId)")
annotation class CanSubmitApplication

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectStatusDecideApproved')")
annotation class CanApproveApplication

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectStatusDecideApprovedWithConditions')")
annotation class CanApproveApplicationWithConditions

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectStatusDecideNotApproved')")
annotation class CanRefuseApplication

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectStatusDecideEligible')")
annotation class CanSetApplicationAsEligible

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('ProjectStatusDecideIneligible')")
annotation class CanSetApplicationAsIneligible

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.canReadProject(#projectId) && @projectStatusAuthorization.canReturnToApplicant(#projectId)")
annotation class CanReturnApplicationToApplicant

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectAuthorization.canReadProject(#projectId) && @projectStatusAuthorization.canStartSecondStep(#projectId)")
annotation class CanStartSecondStep

@Component
class ProjectStatusAuthorization(
    override val securityService: SecurityService,
    val projectPersistence: ProjectPersistence,
    val projectService: ProjectService
) : Authorization(securityService) {

    fun canSubmit(projectId: Long): Boolean {
        val project = projectPersistence.getApplicantAndStatusById(projectId)
        val isOwner = isActiveUserIdEqualTo(userId = project.applicantId)

        if (isOwner || hasPermission(UserRolePermission.ProjectSubmission))
            return project.projectStatus.isDraftOrReturned()
        else if (hasPermission(UserRolePermission.ProjectRetrieve))
            return false
        else
            throw ResourceNotFoundException("project")
    }

    fun canReturnToApplicant(projectId: Long): Boolean {
        val project = projectPersistence.getApplicantAndStatusById(projectId)
        val oldStatus = project.projectStatus
        val oldPossibilities = setOf(SUBMITTED, ELIGIBLE, APPROVED_WITH_CONDITIONS, APPROVED)

        return oldPossibilities.contains(oldStatus) && (isProgrammeUser() || isAdmin())
    }

    fun canStartSecondStep(projectId: Long): Boolean {
        val project = projectPersistence.getApplicantAndStatusById(projectId)
        val oldPossibilities = setOf(STEP1_APPROVED_WITH_CONDITIONS, STEP1_APPROVED)

        if (!isProgrammeUser() && ! isAdmin())
            throw ResourceNotFoundException("project")

        return oldPossibilities.contains(project.projectStatus)
    }

}
