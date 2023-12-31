package io.cloudflight.jems.server.project.service.checklist.model

import io.cloudflight.jems.server.programme.service.checklist.model.ProgrammeChecklistType
import java.time.LocalDate
import java.time.ZonedDateTime

data class ChecklistInstance(
    val id: Long? = null,
    val status: ChecklistInstanceStatus,
    val type: ProgrammeChecklistType,
    val name: String?,
    val creatorEmail: String?,
    val relatedToId: Long?,
    val finishedDate: LocalDate? = null,
    val createdAt: ZonedDateTime? = null,
    val programmeChecklistId: Long?,
    val consolidated: Boolean = false,
    val visible: Boolean,
    val description: String?
)
