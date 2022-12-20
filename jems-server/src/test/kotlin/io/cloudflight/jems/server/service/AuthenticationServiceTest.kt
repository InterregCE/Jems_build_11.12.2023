package io.cloudflight.jems.server.service

import io.cloudflight.jems.api.authentication.dto.LoginRequest
import io.cloudflight.jems.api.user.dto.UserRoleDTO
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditUser
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.authentication.model.LocalCurrentUser
import io.cloudflight.jems.server.authentication.service.AuthenticationServiceImpl
import io.cloudflight.jems.server.authentication.service.LoginAttemptService
import io.cloudflight.jems.server.authentication.service.LoginBlockedException
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.utils.failedLoginAttemptEntity
import io.cloudflight.jems.server.utils.loginEmail
import io.cloudflight.jems.server.user.service.model.User
import io.cloudflight.jems.server.user.service.model.UserRole
import io.cloudflight.jems.server.user.service.model.UserStatus
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import java.util.Collections
import javax.servlet.http.HttpServletRequest

class AuthenticationServiceTest : UnitTest() {

    @RelaxedMockK
    lateinit var securityService: SecurityService

    @RelaxedMockK
    lateinit var loginAttemptService: LoginAttemptService

    @RelaxedMockK
    lateinit var authenticationManager: AuthenticationManager

    @RelaxedMockK
    lateinit var auditService: AuditService

    @RelaxedMockK
    lateinit var req: HttpServletRequest

    @InjectMockKs
    lateinit var authenticationService: AuthenticationServiceImpl

    @BeforeEach
    fun setup() {
        clearMocks(auditService)
    }

    @Test
    fun `logging in is audited`() {
        every { securityService.currentUser } returns LocalCurrentUser(
            User(
                1, "admin@test.net", "test", "test",
                UserRole(1, "Role", emptySet()),
                userStatus = UserStatus.ACTIVE
            ), "", Collections.emptyList()
        )

        authenticationService.login(req, LoginRequest("admin@test.net", "admin"))

        val event = slot<AuditCandidate>()
        val user = slot<AuditUser>()
        verify { auditService.logEvent(capture(event), capture(user)) }
        assertThat(event.captured.description).isEqualTo("user with email admin@test.net logged in")
        assertThat(user.captured).isEqualTo(AuditUser(id = 1, email = "admin@test.net"))
    }

    @Test
    fun `logging out is audited`() {
        authenticationService.logout(req)

        val event = slot<AuditCandidate>()
        verify { auditService.logEvent(capture(event), any()) }
        assertThat(event.captured.description).isEqualTo("user with email  logged out")
    }

    @Test
    fun `current user is returned`() {
        every { securityService.currentUser } returns LocalCurrentUser(
            User(
                1, "test@test.net", "test", "test",
                UserRole(1, "Role", emptySet()),
                userStatus = UserStatus.ACTIVE
            ), "", Collections.emptyList()
        )

        val currentUser = authenticationService.getCurrentUser()

        assertThat(currentUser.name).isEqualTo("test@test.net")
        assertThat(currentUser.role.name).isEqualTo("Role")
    }

    @Test
    fun `current user is null`() {
        every { securityService.currentUser } returns null
        val currentUser = authenticationService.getCurrentUser()

        assertThat(currentUser.name).isEqualTo("")
        assertThat(currentUser.role).isEqualTo(
            UserRoleDTO(
                name = "",
                permissions = emptyList(),
                defaultForRegisteredUser = false
            )
        )
    }

    @Test
    fun `should throw LoginBlockedException when there have been many failed login attempts recently`() {
        every { loginAttemptService.getFailedLoginAttempt(loginEmail) } returns failedLoginAttemptEntity()

        assertThrows<LoginBlockedException> {
            authenticationService.login(req, LoginRequest(loginEmail, "password"))
        }

    }
}