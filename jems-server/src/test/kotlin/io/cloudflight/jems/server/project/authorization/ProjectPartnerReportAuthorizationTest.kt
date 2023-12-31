package io.cloudflight.jems.server.project.authorization

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.authentication.model.CurrentUser
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
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectReportingEdit
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectReportingView
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectReportingReOpen
import io.cloudflight.jems.server.user.service.model.UserRolePermission.ProjectRetrieve
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.Optional

internal class ProjectPartnerReportAuthorizationTest : UnitTest() {

    companion object {
        private const val PARTNER_ID = 288L
        private const val PROJECT_ID = 305L
    }

    @MockK
    lateinit var securityService: SecurityService

    @MockK
    lateinit var reportPersistence: ProjectPartnerReportPersistence

    @MockK
    lateinit var projectPersistence: ProjectPersistence

    @MockK
    lateinit var partnerPersistence: PartnerPersistence

    @MockK
    lateinit var partnerCollaboratorPersistence: UserPartnerCollaboratorPersistence

    @MockK
    lateinit var controllerInstitutionPersistence: ControllerInstitutionPersistence

    @MockK
    lateinit var currentUser: CurrentUser

    @InjectMockKs
    lateinit var reportAuthorization: ProjectPartnerReportAuthorization

    @BeforeAll
    fun setup() {
        every { partnerPersistence.getProjectIdForPartnerId(PARTNER_ID, null) } returns PROJECT_ID
    }

    @BeforeEach
    fun resetMocks() {
        clearMocks(currentUser)
        clearMocks(securityService)
        every { securityService.currentUser } returns currentUser
    }

    @Test
    fun `assigned monitor user with permission can edit not specific`() {
        every { currentUser.hasPermission(ProjectReportingEdit) } returns true
        every { currentUser.user.assignedProjects } returns setOf(PROJECT_ID)
        assertThat(reportAuthorization.canEditPartnerReportNotSpecific(PARTNER_ID)).isTrue
    }

    @Test
    fun `collaborator with permission can edit not specific`() {
        every { securityService.getUserIdOrThrow() } returns 4590L

        every { currentUser.hasPermission(ProjectReportingEdit) } returns false
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, PARTNER_ID) } returns Optional.of(EDIT)
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, PARTNER_ID) } returns null

        assertThat(reportAuthorization.canEditPartnerReportNotSpecific(PARTNER_ID)).isTrue
    }

    @Test
    fun `controller even with Edit permission CAN NOT edit`() {
        every { securityService.getUserIdOrThrow() } returns 4590L

        every { currentUser.hasPermission(ProjectReportingEdit) } returns false
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, PARTNER_ID) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, PARTNER_ID) } returns UserInstitutionAccessLevel.Edit

        assertThat(reportAuthorization.canEditPartnerReportNotSpecific(PARTNER_ID)).isFalse
    }

    @Test
    fun `user can NOT edit not specific`() {
        every { securityService.getUserIdOrThrow() } returns 3205L

        every { currentUser.hasPermission(ProjectReportingEdit) } returns false
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 3205L, PARTNER_ID) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 3205L, PARTNER_ID) } returns null

        assertThat(reportAuthorization.canEditPartnerReportNotSpecific(PARTNER_ID)).isFalse
    }

    @ParameterizedTest(name = "assigned monitor user with permission can edit (isOpen {0})")
    @ValueSource(booleans = [true, false])
    fun `assigned monitor user with permission can edit`(isOpen: Boolean) {
        val report = mockk<ProjectPartnerReport>()
        every { report.status.isClosed() } returns !isOpen
        every { reportPersistence.getPartnerReportById(PARTNER_ID, 17L) } returns report

        every { currentUser.hasPermission(ProjectReportingEdit) } returns true
        every { currentUser.user.assignedProjects } returns setOf(PROJECT_ID)
        assertThat(reportAuthorization.canEditPartnerReport(PARTNER_ID, 17L)).isEqualTo(isOpen)
    }

    @Test
    fun `assigned monitor user with permission can view`() {
        every { currentUser.hasPermission(ProjectReportingView) } returns true
        every { currentUser.user.assignedProjects } returns setOf(PROJECT_ID)
        assertThat(reportAuthorization.canViewPartnerReport(PARTNER_ID)).isTrue
    }

    @Test
    fun `creator + collaborator with permission can view`() {
        every { currentUser.hasPermission(ProjectReportingView) } returns false
        every { securityService.getUserIdOrThrow() } returns 4590L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, PARTNER_ID) } returns Optional.of(VIEW)
        assertThat(reportAuthorization.canViewPartnerReport(PARTNER_ID)).isTrue
    }

    @Test
    fun `controller with permission can view`() {
        every { currentUser.hasPermission(ProjectReportingView) } returns false
        every { securityService.getUserIdOrThrow() } returns 4591L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4591L, PARTNER_ID) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4591L, PARTNER_ID) } returns UserInstitutionAccessLevel.View
        assertThat(reportAuthorization.canViewPartnerReport(PARTNER_ID)).isTrue
    }

    @Test
    fun `user can NOT view`() {
        every { currentUser.hasPermission(ProjectReportingView) } returns false
        every { securityService.getUserIdOrThrow() } returns 3205L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 3205L, PARTNER_ID) } returns Optional.empty()
        assertThat(reportAuthorization.canViewPartnerReport(PARTNER_ID)).isFalse
    }

    @Test
    fun `canReOpenPartnerReport - assigned user`() {
        every { partnerPersistence.getProjectIdForPartnerId(41L) } returns 281L
        every { currentUser.hasPermission(ProjectReportingReOpen) } returns true
        every { currentUser.user.assignedProjects } returns setOf(281)
        assertThat(reportAuthorization.canReOpenPartnerReport(41L)).isTrue()
    }

    @Test
    fun `canReOpenPartnerReport - global view on AF`() {
        every { partnerPersistence.getProjectIdForPartnerId(42L) } returns 282L
        every { currentUser.hasPermission(ProjectReportingReOpen) } returns true
        every { currentUser.user.assignedProjects } returns emptySet()
        every { currentUser.hasPermission(ProjectRetrieve) } returns true
        assertThat(reportAuthorization.canReOpenPartnerReport(42L)).isTrue()
    }

    @Test
    fun `canReOpenPartnerReport - controller user with view`() {
        every { partnerPersistence.getProjectIdForPartnerId(43L) } returns 283L
        every { currentUser.hasPermission(ProjectReportingReOpen) } returns true
        every { currentUser.user.assignedProjects } returns emptySet()
        every { currentUser.hasPermission(ProjectRetrieve) } returns false

        every { securityService.getUserIdOrThrow() } returns 540L
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(540L, partnerId = 43L) } returns UserInstitutionAccessLevel.View
        assertThat(reportAuthorization.canReOpenPartnerReport(43L)).isFalse()
    }

    @Test
    fun `canReOpenPartnerReport - controller user with edit`() {
        every { partnerPersistence.getProjectIdForPartnerId(44L) } returns 284L
        every { currentUser.hasPermission(ProjectReportingReOpen) } returns true
        every { currentUser.user.assignedProjects } returns emptySet()
        every { currentUser.hasPermission(ProjectRetrieve) } returns false

        every { securityService.getUserIdOrThrow() } returns 541L
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(541L, partnerId = 44L) } returns UserInstitutionAccessLevel.Edit
        assertThat(reportAuthorization.canReOpenPartnerReport(44L)).isTrue()
    }

    @ParameterizedTest(name = "control report - user can view - because is controller with {0}")
    @EnumSource(value = UserInstitutionAccessLevel::class)
    fun `control report - user can view - because is controller`(accessLevel: UserInstitutionAccessLevel) {
        val userId = 10L + accessLevel.ordinal
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 50L) } returns accessLevel
        every { reportPersistence.getPartnerReportById(50, 0L) } returns mockk()
        assertThat(reportAuthorization.canViewPartnerControlReport(50L, 0L)).isTrue
    }

    @ParameterizedTest(name = "control report - user can view - because can see all reports (status {0})")
    @EnumSource(value = ReportStatus::class, names = ["Certified"])
    fun `control report - user can view - because can see all reports`(reportStatus: ReportStatus) {
        val userId = 13L + reportStatus.ordinal
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 51L) } returns null

        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns reportStatus
        every { reportPersistence.getPartnerReportById(51L, reportId = 1L) } returns report
        every { partnerPersistence.getProjectIdForPartnerId(51L) } returns PROJECT_ID
        every { currentUser.hasPermission(ProjectReportingView) } returns true
        every { currentUser.user.assignedProjects } returns setOf(PROJECT_ID)

        assertThat(reportAuthorization.canViewPartnerControlReport(51L, 1L)).isTrue
    }

    @ParameterizedTest(name = "control report - user can not view (status {0})")
    @EnumSource(value = ReportStatus::class, names = ["Certified"], mode = EnumSource.Mode.EXCLUDE)
    fun `control report - user can not view`(status: ReportStatus) {
        val userId = 18L
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 52L) } returns null

        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns status
        every { reportPersistence.getPartnerReportById(52L, reportId = 1L) } returns report

        assertThat(reportAuthorization.canViewPartnerControlReport(52L, reportId = 1L)).isFalse
    }

    @Test
    fun `control report - user can edit`() {
        val userId = 26L
        every { reportPersistence.exists(28L, 5L) } returns true
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 28L) } returns
            UserInstitutionAccessLevel.Edit
        assertThat(reportAuthorization.canEditPartnerControlReport(28L, 5L)).isTrue
    }

    @Test
    fun `control report - report not exists`() {
        val userId = 26L
        every { reportPersistence.exists(28L, 5L) } returns false
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 28L) } returns
            UserInstitutionAccessLevel.Edit
        assertThat(reportAuthorization.canEditPartnerControlReport(28L, 5L)).isFalse
    }

    @Test
    fun `control report - user can not edit - only view`() {
        val userId = 28L
        every { reportPersistence.exists(150L, 5L) } returns true
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 150L) } returns
            UserInstitutionAccessLevel.View
        assertThat(reportAuthorization.canEditPartnerControlReport(150L, 5L)).isFalse
    }

    @Test
    fun `control report - user can not edit - not assigned`() {
        val userId = 30L
        every { reportPersistence.exists(152L, 5L) } returns true
        every { securityService.getUserIdOrThrow() } returns userId
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId, partnerId = 152L) } returns null
        assertThat(reportAuthorization.canEditPartnerControlReport(152L, 5L)).isFalse
    }

    @Test
    fun `canUpdatePartner - has EDIT`() {
        val partnerId = 1198L
        val userId = 458L

        every { securityService.getUserIdOrThrow() } returns userId
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = userId, partnerId = partnerId) } returns Optional.of(EDIT)

        assertThat(reportAuthorization.canUpdatePartner(partnerId)).isTrue
    }

    @Test
    fun `canUpdatePartner - has only VIEW`() {
        val partnerId = 1288L
        val userId = 456L

        every { securityService.getUserIdOrThrow() } returns userId
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = userId, partnerId = partnerId) } returns Optional.of(VIEW)

        assertThat(reportAuthorization.canUpdatePartner(partnerId)).isFalse
    }

    @Test
    fun `canUpdatePartner - has no partner-related permission`() {
        val partnerId = 1279L
        val userId = 468L

        every { securityService.getUserIdOrThrow() } returns userId
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = userId, partnerId = partnerId) } returns Optional.empty()

        assertThat(reportAuthorization.canUpdatePartner(partnerId)).isFalse
    }

    @ParameterizedTest(name = "canRetrievePartner - is Partner collaborator (level {0})")
    @EnumSource(value = PartnerCollaboratorLevel::class)
    fun `canRetrievePartner - is Partner collaborator`(level: PartnerCollaboratorLevel) {
        val partnerId = 1192L
        val userId = 546L
        val projectId = 452L
        val project = mockk<ProjectApplicantAndStatus>()
        every { project.projectId } returns projectId
        every { project.getUserIdsWithViewLevel() } returns emptySet()
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { projectPersistence.getApplicantAndStatusById(projectId) } returns project

        every { securityService.getUserIdOrThrow() } returns userId
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId, partnerId = partnerId) } returns Optional.of(level)

        assertThat(reportAuthorization.canRetrievePartner(partnerId)).isTrue
    }

    @Test
    fun `canRetrievePartner - is in institution`() {
        val partnerId = 1193L
        val userId = 547L
        val projectId = 453L
        val project = mockk<ProjectApplicantAndStatus>()
        every { project.projectId } returns projectId
        every { project.getUserIdsWithViewLevel() } returns setOf(userId)
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { projectPersistence.getApplicantAndStatusById(projectId) } returns project

        every { securityService.getUserIdOrThrow() } returns userId
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId, partnerId = partnerId) } returns Optional.empty()

        assertThat(reportAuthorization.canRetrievePartner(partnerId)).isTrue
    }

    @Test
    fun `canRetrievePartner - cannot retrieve`() {
        val partnerId = 1194L
        val userId = 548L
        val projectId = 454L
        val project = mockk<ProjectApplicantAndStatus>()
        every { project.projectId } returns projectId
        every { project.getUserIdsWithViewLevel() } returns emptySet()
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { projectPersistence.getApplicantAndStatusById(projectId) } returns project

        every { securityService.getUserIdOrThrow() } returns userId
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId, partnerId = partnerId) } returns Optional.empty()

        assertThat(reportAuthorization.canRetrievePartner(partnerId)).isFalse
    }


    @Test
    fun `canEditPartnerControlReportChecklist - is monitor`() {
        val partnerId = 1193L
        val reportId = 1193L
        val projectId = 453L
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { currentUser.hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl) } returns true
        every { currentUser.hasPermission(ProjectReportingView) } returns true
        every { currentUser.user.assignedProjects } returns setOf(projectId)

        assertThat(reportAuthorization.canEditPartnerControlReportChecklist(partnerId, reportId)).isTrue
    }

    @Test
    fun `canEditPartnerControlReportChecklist - is controller with Edit, permission and report in control`() {
        val partnerId = 1193L
        val reportId = 1193L
        val projectId = 453L
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns ReportStatus.InControl
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { currentUser.hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl) } returns true
        every { currentUser.hasPermission(UserRolePermission.ProjectRetrieve) } returns false
        every { reportPersistence.getPartnerReportById(partnerId, reportId) } returns report
        every { securityService.getUserIdOrThrow() } returns 4590L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, partnerId) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, partnerId) } returns UserInstitutionAccessLevel.Edit
        every { currentUser.user.assignedProjects } returns emptySet()

        assertThat(reportAuthorization.canEditPartnerControlReportChecklist(partnerId, reportId)).isTrue
    }

    @Test
    fun `canEditPartnerControlReportChecklist - is controller with Edit, no permission and report in control`() {
        val partnerId = 1193L
        val reportId = 1193L
        val projectId = 453L
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns ReportStatus.InControl
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { currentUser.hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl) } returns false
        every { reportPersistence.getPartnerReportById(partnerId, reportId) } returns report
        every { securityService.getUserIdOrThrow() } returns 4590L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, partnerId) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, partnerId) } returns UserInstitutionAccessLevel.Edit

        assertThat(reportAuthorization.canEditPartnerControlReportChecklist(partnerId, reportId)).isTrue
    }

    @Test
    fun `canEditPartnerControlReportChecklist - is controller with View, permission and report in control`() {
        val partnerId = 1193L
        val reportId = 1193L
        val projectId = 453L
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns ReportStatus.InControl
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { currentUser.hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl) } returns true
        every { currentUser.hasPermission(UserRolePermission.ProjectRetrieve) } returns false
        every { reportPersistence.getPartnerReportById(partnerId, reportId) } returns report
        every { securityService.getUserIdOrThrow() } returns 4590L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, partnerId) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, partnerId) } returns UserInstitutionAccessLevel.View
        every { currentUser.user.assignedProjects } returns emptySet()

        assertThat(reportAuthorization.canEditPartnerControlReportChecklist(partnerId, reportId)).isFalse
    }

    @Test
    fun `canEditPartnerControlReportChecklist - is controller with permission and report certified`() {
        val partnerId = 1193L
        val reportId = 1193L
        val projectId = 453L
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns ReportStatus.Certified
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { currentUser.hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl) } returns true
        every { currentUser.hasPermission(UserRolePermission.ProjectRetrieve) } returns false
        every { reportPersistence.getPartnerReportById(partnerId, reportId) } returns report
        every { securityService.getUserIdOrThrow() } returns 4590L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, partnerId) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, partnerId) } returns UserInstitutionAccessLevel.Edit
        every { currentUser.user.assignedProjects } returns emptySet()

        assertThat(reportAuthorization.canEditPartnerControlReportChecklist(partnerId, reportId)).isTrue
    }

    @Test
    fun `canEditPartnerControlReportChecklist - is controller without permission and report certified`() {
        val partnerId = 1193L
        val reportId = 1193L
        val projectId = 453L
        val report = mockk<ProjectPartnerReport>()
        every { report.status } returns ReportStatus.Certified
        every { partnerPersistence.getProjectIdForPartnerId(partnerId) } returns projectId
        every { currentUser.hasPermission(UserRolePermission.ProjectReportingChecklistAfterControl) } returns false
        every { reportPersistence.getPartnerReportById(partnerId, reportId) } returns report
        every { securityService.getUserIdOrThrow() } returns 4590L
        every { partnerCollaboratorPersistence.findByUserIdAndPartnerId(userId = 4590L, partnerId) } returns Optional.empty()
        every { controllerInstitutionPersistence.getControllerUserAccessLevelForPartner(userId = 4590L, partnerId) } returns UserInstitutionAccessLevel.Edit

        assertThat(reportAuthorization.canEditPartnerControlReportChecklist(partnerId, reportId)).isFalse
    }

}
