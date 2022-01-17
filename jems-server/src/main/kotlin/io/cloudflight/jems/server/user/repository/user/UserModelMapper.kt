package io.cloudflight.jems.server.user.repository.user

import io.cloudflight.jems.server.user.entity.UserEntity
import io.cloudflight.jems.server.user.entity.UserRoleEntity
import io.cloudflight.jems.server.user.repository.userrole.toModel
import io.cloudflight.jems.server.user.service.model.User
import io.cloudflight.jems.server.user.service.model.UserChange
import io.cloudflight.jems.server.user.service.model.UserRolePermission
import io.cloudflight.jems.server.user.service.model.UserRoleSummary
import io.cloudflight.jems.server.user.service.model.UserSummary
import io.cloudflight.jems.server.user.service.model.UserWithPassword
import org.springframework.data.domain.Page

fun UserEntity.toModel(permissions: Set<UserRolePermission>) = User(
    id = id,
    email = email,
    name = name,
    surname = surname,
    userRole = userRole.toModel(permissions, null),
    userStatus = userStatus
)

fun UserEntity.toModelWithPassword(permissions: Set<UserRolePermission>) = UserWithPassword(
    id = id,
    email = email,
    name = name,
    surname = surname,
    userRole = userRole.toModel(permissions, null),
    encodedPassword = password,
    userStatus = userStatus
)

fun Page<UserEntity>.toModel() = map {
    UserSummary(
        id = it.id,
        email = it.email,
        name = it.name,
        surname = it.surname,
        userRole = it.userRole.toModel(null),
        userStatus = it.userStatus
    )
}

fun UserChange.toEntity(passwordEncoded: String, role: UserRoleEntity) = UserEntity(
    id = id,
    email = email,
    name = name,
    surname = surname,
    userRole = role,
    password = passwordEncoded,
    userStatus = userStatus
)

fun UserEntity.toUserSummary() = UserSummary(
    id = this.id,
    email = this.email,
    name = this.name,
    surname = this.surname,
    userRole = this.userRole.toUserRoleSummary(),
    userStatus = this.userStatus
)

fun UserRoleEntity.toUserRoleSummary() = UserRoleSummary(
    id = id,
    name = name,
    isDefault = false
)
