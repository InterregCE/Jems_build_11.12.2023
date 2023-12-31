package io.cloudflight.jems.server.project.service.projectuser.assign_user_to_project

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditProject
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.common.event.JemsAuditEvent
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.cloudflight.jems.server.user.service.model.UserRoleSummary
import io.cloudflight.jems.server.user.service.model.UserStatus
import io.cloudflight.jems.server.user.service.model.UserSummary
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

internal class AssignUserToProjectEventListenersTest : UnitTest() {

    companion object {
        val project = ProjectSummary(1L, "cid", 1L, "call", "acronym", ApplicationStatus.STEP1_DRAFT)
        val userRole = UserRoleSummary(2L, "role", false)
        val user = UserSummary(3L, "email", sendNotificationsToEmail = false, "", "", userRole, UserStatus.ACTIVE)
        val otherUser = UserSummary(4L, "other@email", sendNotificationsToEmail = false, "", "", userRole, UserStatus.ACTIVE)
    }

    @RelaxedMockK
    lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    lateinit var assignUserToProjectEventListeners: AssignUserToProjectEventListeners

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `assigning a user triggers an audit log`() {
        val auditSlot = slot<JemsAuditEvent>()
        val assignUserToProjectEvent = AssignUserToProjectEvent(project, listOf(user, otherUser))

        assignUserToProjectEventListeners.publishJemsAuditEvent(assignUserToProjectEvent)

        verify { eventPublisher.publishEvent(capture(auditSlot)) }
        assertThat(auditSlot.captured.auditCandidate).isEqualTo(
            AuditCandidate(
                action = AuditAction.PROJECT_USER_ASSIGNMENT_PROGRAMME,
                project = AuditProject(project.id.toString(), project.customIdentifier, project.acronym),
                description = "Project can be accessed by: ${user.email}: role, ${otherUser.email}: role"
            )
        )
    }

    @Test
    fun `removing assignments triggers an audit log`() {
        val auditSlot = slot<JemsAuditEvent>()
        val assignUserToProjectEvent = AssignUserToProjectEvent(project, emptyList())

        assignUserToProjectEventListeners.publishJemsAuditEvent(assignUserToProjectEvent)

        verify { eventPublisher.publishEvent(capture(auditSlot)) }
        assertThat(auditSlot.captured.auditCandidate).isEqualTo(
            AuditCandidate(
                action = AuditAction.PROJECT_USER_ASSIGNMENT_PROGRAMME,
                project = AuditProject(project.id.toString(), project.customIdentifier, project.acronym),
                description = "Project can be accessed by: "
            )
        )
    }
}
