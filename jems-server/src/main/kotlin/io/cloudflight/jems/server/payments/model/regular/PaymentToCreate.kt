package io.cloudflight.jems.server.payments.model.regular

import java.math.BigDecimal

data class PaymentToCreate(
    val programmeLumpSumId: Long,
    val partnerPayments: List<PaymentPartnerToCreate>,
    val amountApprovedPerFund: BigDecimal,

    val projectCustomIdentifier: String,
    val projectAcronym: String,
)
