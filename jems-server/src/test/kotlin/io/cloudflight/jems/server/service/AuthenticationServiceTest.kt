package io.cloudflight.jems.server.service

import io.cloudflight.jems.api.dto.LoginRequest
import io.cloudflight.jems.api.user.dto.OutputUserRole
import io.cloudflight.jems.api.user.dto.OutputUserWithRole
import io.cloudflight.jems.server.audit.service.AuditCandidateWithUser
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.security.model.LocalCurrentUser
import io.cloudflight.jems.server.security.service.SecurityService
import io.cloudflight.jems.server.security.service.impl.AuthenticationServiceImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import java.util.Collections
import javax.servlet.http.HttpServletRequest

class AuthenticationServiceTest {

    @RelaxedMockK
    lateinit var securityService: SecurityService

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
        MockKAnnotations.init(this)
    }

    @Test
    fun `logging in is audited`() {
        every { securityService.currentUser } returns LocalCurrentUser(
            OutputUserWithRole(
                1, "admin@test.net", "test", "test",
                OutputUserRole(1, "Role")
            ), "", Collections.emptyList()
        )

        authenticationService.login(req, LoginRequest("admin@test.net", "admin"))

        val event = slot<AuditCandidateWithUser>()
        verify { auditService.logEvent(capture(event)) }
        assertThat(event.captured.description).isEqualTo("user with email admin@test.net logged in")
    }

    @Test
    fun `logging out is audited`() {
        authenticationService.logout(req)

        val event = slot<AuditCandidateWithUser>()
        verify { auditService.logEvent(capture(event)) }
        assertThat(event.captured.description).isEqualTo("user with email  logged out")
    }

    @Test
    fun `current user is returned`() {
        every { securityService.currentUser } returns LocalCurrentUser(
            OutputUserWithRole(
                1, "test@test.net", "test", "test",
                OutputUserRole(1, "Role")
            ), "", Collections.emptyList()
        )

        val currentUser = authenticationService.getCurrentUser()

        assertThat(currentUser?.name).isEqualTo("test@test.net")
        assertThat(currentUser?.role).isEqualTo("Role")
    }

    @Test
    fun `current user is null`() {
        val currentUser = authenticationService.getCurrentUser()

        assertThat(currentUser?.name).isEqualTo("")
        assertThat(currentUser?.role).isEqualTo("")
    }
}