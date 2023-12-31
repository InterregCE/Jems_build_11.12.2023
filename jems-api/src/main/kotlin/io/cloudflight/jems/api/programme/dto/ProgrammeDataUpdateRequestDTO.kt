package io.cloudflight.jems.api.programme.dto

import java.time.LocalDate

data class ProgrammeDataUpdateRequestDTO(

    val cci: String?,
    val title: String?,
    val version: String?,
    val firstYear: Int?,
    val lastYear: Int?,
    val eligibleFrom: LocalDate?,
    val eligibleUntil: LocalDate?,
    val commissionDecisionNumber: String?,
    val commissionDecisionDate: LocalDate?,
    val programmeAmendingDecisionNumber: String?,
    val programmeAmendingDecisionDate: LocalDate?,
    val technicalAssistanceFlatRate: Double?,
    val projectIdProgrammeAbbreviation: String?,
    val projectIdUseCallId: Boolean

)

