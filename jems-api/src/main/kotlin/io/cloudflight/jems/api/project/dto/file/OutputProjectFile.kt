package io.cloudflight.jems.api.project.dto.file

import io.cloudflight.jems.api.user.dto.OutputUser
import java.time.ZonedDateTime

data class OutputProjectFile (
    val id: Long?,
    val name: String,
    val author: OutputUser,
    val type: ProjectFileType,
    val description: String?,
    val size: Long,
    val updated: ZonedDateTime
)