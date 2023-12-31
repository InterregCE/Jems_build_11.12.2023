package io.cloudflight.jems.server.user.controller

import io.cloudflight.jems.api.user.dto.UserDTO
import io.cloudflight.jems.api.user.dto.UserRegistrationDTO
import io.cloudflight.jems.api.user.dto.UserRoleDTO
import io.cloudflight.jems.api.user.dto.UserRolePermissionDTO
import io.cloudflight.jems.api.user.dto.UserSettingsDTO
import io.cloudflight.jems.api.user.dto.UserStatusDTO
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.user.service.model.User
import io.cloudflight.jems.server.user.service.model.UserRegistration
import io.cloudflight.jems.server.user.service.model.UserRole
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import io.cloudflight.jems.server.user.service.model.UserSettings
import io.cloudflight.jems.server.user.service.model.UserStatus
import io.cloudflight.jems.server.user.service.user.activate_user.ActivateUserInteractor
import io.cloudflight.jems.server.user.service.user.register_user.RegisterUserInteractor
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserRegistrationControllerTest : UnitTest() {

    companion object {
        private const val USER_ID = 9L
        private const val ROLE_ID = 20L

        private val userRole = UserRole(
            id = ROLE_ID,
            name = "maintainer",
            permissions = setOf(UserRolePermission.ProjectSubmission)
        )
        private val user = User(
            id = USER_ID,
            email = "maintainer@interact.eu",
            userSettings = UserSettings(sendNotificationsToEmail = false),
            name = "Michael",
            surname = "Schumacher",
            userRole = userRole,
            userStatus = UserStatus.ACTIVE
        )

        private val expectedUserRole = UserRoleDTO(
            id = ROLE_ID,
            name = "maintainer",
            defaultForRegisteredUser = false,
            permissions = listOf(UserRolePermissionDTO.ProjectSubmission)
        )
        private val expectedUser = UserDTO(
            id = USER_ID,
            email = "maintainer@interact.eu",
            userSettings = UserSettingsDTO(sendNotificationsToEmail = false),
            name = "Michael",
            surname = "Schumacher",
            userRole = expectedUserRole,
            userStatus = UserStatusDTO.ACTIVE
        )
    }

    @MockK
    lateinit var registerUserInteractor: RegisterUserInteractor

    @MockK
    lateinit var activateUserInteractor: ActivateUserInteractor

    @InjectMockKs
    private lateinit var controller: UserRegistrationController

    @Test
    fun registerApplicant() {
        val userRegister = UserRegistrationDTO(
            email = user.email,
            name = user.name,
            surname = user.surname,
            password = "plain_pass",
            captcha = "testCaptcha"
        )

        val slotUserRegister = slot<UserRegistration>()
        every { registerUserInteractor.registerUser(capture(slotUserRegister)) } returns user
        assertThat(controller.registerApplicant(userRegister)).isEqualTo(expectedUser)
        assertThat(slotUserRegister.captured).isEqualTo(
            UserRegistration(
                email = "maintainer@interact.eu",
                name = "Michael",
                surname = "Schumacher",
                password = "plain_pass",
                captcha = "testCaptcha"
            )
        )
    }

}
