package io.cloudflight.jems.server.user.service

import io.cloudflight.jems.api.user.dto.OutputUser
import io.cloudflight.jems.api.user.dto.UserRegistrationDTO
import io.cloudflight.jems.server.authentication.model.LocalCurrentUser
import io.cloudflight.jems.server.user.entity.UserEntity
import io.cloudflight.jems.server.user.entity.UserRoleEntity
import io.cloudflight.jems.server.user.service.model.UserStatus
import io.cloudflight.jems.server.user.service.model.UserWithPassword
import org.springframework.security.core.authority.SimpleGrantedAuthority

// to be refactored to either only belong to repo.UserModelMapper or controller.UserMapper

fun UserWithPassword.toLocalCurrentUser() = LocalCurrentUser(
    user = getUser(),
    password = encodedPassword,
    authorities = (
        setOf("ROLE_${userRole.name}")
            union
            (userRole.permissions).map { it.name }
        )
        .map { SimpleGrantedAuthority(it) }
)

fun UserRegistrationDTO.toEntity(role: UserRoleEntity, password: String) = UserEntity(
    email = this.email,
    sendNotificationsToEmail = false,
    name = this.name,
    surname = this.surname,
    userRole = role,
    password = password,
    userStatus = UserStatus.UNCONFIRMED
)

fun UserEntity.toOutputUser() = OutputUser(
    id = this.id,
    email = this.email,
    name = this.name,
    surname = this.surname
)

