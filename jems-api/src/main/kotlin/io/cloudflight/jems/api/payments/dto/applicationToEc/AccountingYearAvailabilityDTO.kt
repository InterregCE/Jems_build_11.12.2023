package io.cloudflight.jems.api.payments.dto.applicationToEc

import java.time.LocalDate

data class AccountingYearAvailabilityDTO(
    val id: Long,
    val year: Short,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val available: Boolean,
)
