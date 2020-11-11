package io.cloudflight.jems.server.call

import io.cloudflight.jems.api.call.dto.CallStatus
import io.cloudflight.jems.api.user.dto.OutputUserRole
import io.cloudflight.jems.api.user.dto.OutputUserWithRole
import io.cloudflight.jems.server.call.entity.Call
import io.cloudflight.jems.server.user.entity.User
import io.cloudflight.jems.server.user.entity.UserRole
import java.time.ZonedDateTime

private val account = User(
    id = 1,
    email = "admin@admin.dev",
    name = "Name",
    surname = "Surname",
    userRole = UserRole(id = 1, name = "ADMIN"),
    password = "hash_pass"
)

val testUser = OutputUserWithRole(
    id = 1,
    email = "admin@admin.dev",
    name = "Name",
    surname = "Surname",
    userRole = OutputUserRole(id = 1, name = "ADMIN")
)

private val testCall = Call(
    id = 0,
    creator = account,
    name = "Test call name",
    priorityPolicies = emptySet(),
    strategies = emptySet(),
    funds = emptySet(),
    startDate = ZonedDateTime.now(),
    endDate = ZonedDateTime.now().plusDays(5L),
    status = CallStatus.DRAFT,
    description = "This is a dummy call",
    lengthOfPeriod = 1
)

fun callWithId(id: Long) = testCall.copy(id = id)
