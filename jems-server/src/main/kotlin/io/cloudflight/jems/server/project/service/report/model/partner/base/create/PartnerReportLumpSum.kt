package io.cloudflight.jems.server.project.service.report.model.partner.base.create

import java.math.BigDecimal

data class PartnerReportLumpSum(
    val lumpSumId: Long,
    val orderNr: Int,
    val period: Int?,
    val total: BigDecimal,
    val previouslyReported: BigDecimal,
    val previouslyReportedParked: BigDecimal,
    val previouslyPaid: BigDecimal,
    val previouslyValidated: BigDecimal,
)
