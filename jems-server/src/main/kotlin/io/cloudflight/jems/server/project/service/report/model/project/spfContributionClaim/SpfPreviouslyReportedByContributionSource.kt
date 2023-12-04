package io.cloudflight.jems.server.project.service.report.model.project.spfContributionClaim

import java.math.BigDecimal

data class SpfPreviouslyReportedByContributionSource(
    val finances: Map<Long, SpfPreviouslyReportedContributionRow>,
    val partnerContributions: Map<Long, SpfPreviouslyReportedContributionRow>,
)