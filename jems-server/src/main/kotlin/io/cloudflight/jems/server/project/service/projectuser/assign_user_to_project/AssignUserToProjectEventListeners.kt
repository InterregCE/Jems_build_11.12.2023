package io.cloudflight.jems.server.project.service.projectuser.assign_user_to_project

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.audit.service.AuditBuilder
import io.cloudflight.jems.server.common.event.JemsAuditEvent
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.cloudflight.jems.server.user.service.model.UserSummary
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionalEventListener

data class AssignUserToProjectEvent(val project: ProjectSummary, val users: List<UserSummary>)

@Service
data class AssignUserToProjectEventListeners(
    private val eventPublisher: ApplicationEventPublisher,
) {

    @TransactionalEventListener
    fun publishJemsAuditEvent(event: AssignUserToProjectEvent) =
        eventPublisher.publishEvent(
            JemsAuditEvent(
                auditCandidate = AuditBuilder(AuditAction.PROJECT_USER_ASSIGNMENT_PROGRAMME)
                    .project(event.project)
                    .description("Project can be accessed by: ${users(event.users)}")
                    .build()
            )
        )

    private fun users(users: List<UserSummary>): String =
        users.joinToString(", ") { "${it.email}: ${it.userRole.name}" }
}
