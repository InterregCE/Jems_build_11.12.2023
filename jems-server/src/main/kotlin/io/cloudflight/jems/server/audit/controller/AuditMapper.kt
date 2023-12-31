package io.cloudflight.jems.server.audit.controller

import io.cloudflight.jems.api.audit.dto.AuditDTO
import io.cloudflight.jems.api.audit.dto.AuditProjectDTO
import io.cloudflight.jems.api.audit.dto.AuditSearchRequestDTO
import io.cloudflight.jems.api.audit.dto.AuditUserDTO
import io.cloudflight.jems.server.audit.model.Audit
import io.cloudflight.jems.server.audit.model.AuditFilter
import io.cloudflight.jems.server.audit.model.AuditProject
import io.cloudflight.jems.server.audit.model.AuditSearchRequest
import io.cloudflight.jems.server.audit.model.AuditUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

fun Page<Audit>.toDto() = map {
    AuditDTO(
        id = it.id,
        timestamp = it.timestamp,
        action = it.action,
        project = it.project?.tDto(),
        user = it.user?.tDto(),
        description = it.description,
    )
}

fun AuditUser.tDto() = AuditUserDTO(
    id = id,
    email = email,
)

fun AuditProject.tDto() = AuditProjectDTO(
    id = id,
    customIdentifier = customIdentifier,
    name = name
)

fun AuditSearchRequestDTO?.toModel(pageable: Pageable) =
    if (this == null)
        AuditSearchRequest(
            pageable = pageable
        )
    else
        AuditSearchRequest(
            userId = AuditFilter(values = userIds.filterNotNullTo(HashSet())),
            userEmail = AuditFilter(values = userEmails.filterNotNullTo(HashSet())),
            action = AuditFilter(values = actions.map { it?.name }.filterNotNullTo(HashSet())),
            projectId = AuditFilter(values = projectIds.filterNotNullTo(HashSet())),
            timeFrom = timeFrom?.withSecond(0)?.minusNanos(1),
            timeTo = timeTo?.withSecond(0)?.plusMinutes(1)?.minusNanos(1),
            pageable = pageable,
        )
