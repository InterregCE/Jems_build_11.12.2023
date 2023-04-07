package io.cloudflight.jems.server.project.authorization

import io.cloudflight.jems.server.authentication.authorization.Authorization
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.controllerInstitution.service.ControllerInstitutionPersistence
import io.cloudflight.jems.server.controllerInstitution.service.model.UserInstitutionAccessLevel
import io.cloudflight.jems.server.project.entity.partneruser.PartnerCollaboratorLevel
import io.cloudflight.jems.server.project.entity.partneruser.PartnerCollaboratorLevel.EDIT
import io.cloudflight.jems.server.project.entity.partneruser.PartnerCollaboratorLevel.VIEW
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.model.ProjectApplicantAndStatus
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.UserPartnerCollaboratorPersistence
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import java.util.Optional

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canEditPartnerReport(#partnerId, #reportId)")
annotation class CanEditPartnerReport

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canEditPartnerReportNotSpecific(#partnerId)")
annotation class CanEditPartnerReportNotSpecific

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canViewPartnerReport(#partnerId)")
annotation class CanViewPartnerReport

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canEditPartnerControlReport(#partnerId, #reportId)")
annotation class CanEditPartnerControlReport

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canEditPartnerControlReportChecklist(#partnerId, #reportId)")
annotation class CanEditPartnerControlReportChecklist

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canViewPartnerControlReport(#partnerId, #reportId)")
annotation class CanViewPartnerControlReport

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canViewPartnerControlReport(#partnerId, #reportId) || " +
    "@projectPartnerReportAuthorization.canRetrievePartner(#partnerId)")
annotation class CanViewPartnerControlReportFile

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@projectPartnerReportAuthorization.canEditPartnerControlReport(#partnerId, #reportId) || " +
    "@projectPartnerReportAuthorization.canUpdatePartner(#partnerId)")
annotation class CanEditPartnerControlReportFile

@Component
class ProjectPartnerReportAuthorization(
    override val securityService: SecurityService,
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val projectPersistence: ProjectPersistence,
    private val partnerPersistence: PartnerPersistence,
    private val partnerCollaboratorPersistence: UserPartnerCollaboratorPersistence,
    private val controllerInstitutionPersistence: ControllerInstitutionPersistence,
) : Authorization(securityService) {

    fun canEditPartnerReport(partnerId: Long, reportId: Long): Boolean {
        val report = reportPersistence.getPartnerReportById(partnerId, reportId = reportId)

        return report.status.isOpen() && hasPermissionForPartner(partnerId = partnerId, EDIT)
    }

    fun canEditPartnerReportNotSpecific(partnerId: Long): Boolean =
        hasPermissionForPartner(partnerId = partnerId, EDIT)

    fun canViewPartnerReport(partnerId: Long): Boolean =
        hasPermissionForPartner(partnerId = partnerId, VIEW)

    private fun hasPermissionForPartner(partnerId: Long, levelNeeded: PartnerCollaboratorLevel): Boolean {
        val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)

        // monitor users
        if (hasPermissionForProject(levelNeeded.correspondingRolePermission, projectId))
            return true

        // partner collaborators and controller institutions
        val levelForCollaborator = getLevelForUserCollaborator(partnerId = partnerId)
        val levelForController = getLevelForUserController(partnerId = partnerId)

        return when(levelNeeded) {
            VIEW -> levelForCollaborator.isPresent || levelForController.isPresent
            EDIT -> levelForCollaborator.isPresent && levelForCollaborator.get() == EDIT
        }
    }

    fun getLevelForUserCollaborator(partnerId: Long) =
        partnerCollaboratorPersistence.findByUserIdAndPartnerId(
            userId = securityService.getUserIdOrThrow(),
            partnerId = partnerId,
        )

    fun getLevelForUserController(partnerId: Long): Optional<UserInstitutionAccessLevel> =
        Optional.ofNullable(
            controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(
                userId = securityService.getUserIdOrThrow(),
                partnerId = partnerId,
            )
        )

    fun canEditPartnerControlReportChecklist(partnerId: Long, reportId: Long): Boolean {
        val report = reportPersistence.getPartnerReportById(partnerId, reportId)
        val reportIsFinalized = report.status.isFinalized()
        val userAccessLevelFromInstitution = controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(
            userId = securityService.getUserIdOrThrow(),
            partnerId = partnerId,
        )
        val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)
        val inControlPermissions = userAccessLevelFromInstitution == UserInstitutionAccessLevel.Edit
        val finalizedPermissions = (userAccessLevelFromInstitution != null)
                && hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl, projectId)

        return if (reportIsFinalized) {
            finalizedPermissions
        } else {
            inControlPermissions
        }
    }


    fun canEditPartnerControlReport(partnerId: Long, reportId: Long): Boolean =
        reportPersistence.exists(partnerId, reportId = reportId) &&
            controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(
                userId = securityService.getUserIdOrThrow(),
                partnerId = partnerId,
            ) == UserInstitutionAccessLevel.Edit

    fun canViewPartnerControlReport(partnerId: Long, reportId: Long): Boolean {
        val controllerPermission = controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(
            userId = securityService.getUserIdOrThrow(),
            partnerId = partnerId,
        )
        // controller institutions
        if (controllerPermission != null)
            return true

        val report = reportPersistence.getPartnerReportById(partnerId, reportId = reportId)
        return report.status.isFinalized() && hasPermissionForPartner(partnerId = partnerId, VIEW)
    }

    fun canUpdatePartner(partnerId: Long): Boolean {
        val perm = partnerCollaboratorPersistence
            .findByUserIdAndPartnerId(userId = securityService.getUserIdOrThrow(), partnerId = partnerId)

        return perm.isPresent && perm.get() == EDIT
    }

    fun canRetrievePartner(partnerId: Long): Boolean {
        val partnerUserPermission = partnerCollaboratorPersistence
            .findByUserIdAndPartnerId(userId = securityService.getUserIdOrThrow(), partnerId = partnerId)

        val projectCollaborators = getProjectFromPartnerId(partnerId).getUserIdsWithViewLevel()

        return partnerUserPermission.isPresent || isActiveUserIdEqualToOneOf(projectCollaborators)
    }

    private fun getProjectFromPartnerId(partnerId: Long): ProjectApplicantAndStatus {
        return projectPersistence.getApplicantAndStatusById(
            partnerPersistence.getProjectIdForPartnerId(partnerId),
        )
    }

}
