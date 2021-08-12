package io.cloudflight.jems.server.project.service.partner.cofinancing.update_cofinancing

import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerContributionStatusDTO
import io.cloudflight.jems.server.common.exception.I18nValidationException
import io.cloudflight.jems.server.project.service.partner.cofinancing.model.ProjectPartnerContribution
import io.cloudflight.jems.server.project.service.partner.cofinancing.model.UpdateProjectPartnerCoFinancing
import org.springframework.http.HttpStatus
import java.math.BigDecimal

private const val MAX_FUNDS = 5

fun validateFinancing(
    financing: Collection<UpdateProjectPartnerCoFinancing>,
    allowedFundIds: Set<Long>
) {
    if (!financing.all { it.percentage != null && it.percentage <= BigDecimal.valueOf(100) && it.percentage >= BigDecimal.ZERO })
        invalid("project.partner.coFinancing.percentage.invalid")

    if (financing.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.percentage) }.compareTo(BigDecimal.valueOf(100.0)) != 0)
        invalid("project.partner.coFinancing.sum.invalid")

    // there needs to be exactly 1 fundId, which is null
    if (financing.count { it.fundId != null } + 1 != financing.count())
        invalid("project.partner.coFinancing.one.and.only.partner.contribution")

    // there can only be a maximum of (MAX_FUNDS) funds for a partner
    if (financing.count() > MAX_FUNDS || financing.count() > allowedFundIds.size + 1)
        invalid("project.partner.coFinancing.maximum.partner.contributions")

    if (financing.mapTo(HashSet()) { it.fundId }.size != financing.size)
        invalid("project.partner.coFinancing.fund.not.unique")

    val fundIds = financing
        .filter { it.fundId != null }
        .mapTo(HashSet()) { it.fundId!! }

    if (!allowedFundIds.containsAll(fundIds))
        invalid("project.partner.coFinancing.fundId.not.allowed.for.call")
}

fun validateContribution(
    partnerContributions: Collection<ProjectPartnerContribution>
) {
    // there needs to be exactly 1 partner contribution = which name is null
    if (partnerContributions.count { it.isNotPartner() } + 1 != partnerContributions.count())
        invalid("project.partner.contribution.one.and.only.partner.contribution")

    if (!partnerContributions.all { (it.isNotPartner() && !it.name.isNullOrBlank()) || it.isPartner })
        invalid("project.partner.contribution.name.is.mandatory")

    if (!partnerContributions.all { it.status != null })
        invalid("project.partner.contribution.status.is.mandatory")

    val partnerStatus = partnerContributions.find { it.isPartner }!!.status!!
    if (partnerStatus == ProjectPartnerContributionStatusDTO.AutomaticPublic)
        invalid("project.partner.contribution.partner.status.invalid")

    if (!partnerContributions.all { it.amount != null && it.amount >= BigDecimal.ZERO })
        invalid("project.partner.contribution.amount.is.mandatory")
}

private fun invalid(message: String) {
    throw I18nValidationException(
        httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
        i18nKey = message
    )
}
