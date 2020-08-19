package io.cloudflight.ems.api.project.dto

import io.cloudflight.ems.api.programme.dto.OutputProgrammePriorityPolicySimple

data class OutputProjectData (
    val title: String?,
    val duration: Int?,
    val intro: String?,
    val introProgrammeLanguage: String?,
    val specificObjective: OutputProgrammePriorityPolicySimple?
)
