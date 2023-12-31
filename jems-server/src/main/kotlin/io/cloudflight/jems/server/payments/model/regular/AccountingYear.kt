package io.cloudflight.jems.server.payments.model.regular

import java.time.LocalDate

data class AccountingYear(
    val id: Long,
    val year: Short,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
