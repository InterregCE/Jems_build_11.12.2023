package io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportCoFinancingBreakdown

import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerCoFinancingFundTypeDTO.MainFund
import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerCoFinancingFundTypeDTO.PartnerContribution
import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerContributionStatusDTO
import io.cloudflight.jems.server.project.service.partner.cofinancing.model.ProjectPartnerCoFinancing
import io.cloudflight.jems.server.project.service.partner.cofinancing.model.ProjectPartnerCoFinancingAndContribution
import io.cloudflight.jems.server.project.service.partner.cofinancing.model.ProjectPartnerContributionStatus
import io.cloudflight.jems.server.project.service.report.fillInOverviewFields
import io.cloudflight.jems.server.project.service.report.model.partner.contribution.ProjectPartnerReportContributionOverview
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.coFinancing.*
import java.math.BigDecimal
import java.math.RoundingMode

fun ReportExpenditureCoFinancing.toLinesModel() = ExpenditureCoFinancingBreakdown(
    funds = totalsFromAF.funds.map {
        ExpenditureCoFinancingBreakdownLine(
            fundId = it.key,
            totalEligibleBudget = it.value,
            previouslyReported = previouslyReported.funds.getOrDefault(it.key, BigDecimal.ZERO),
            previouslyReportedParked = previouslyReportedParked.funds.getOrDefault(it.key, BigDecimal.ZERO),
            currentReport = currentlyReported.funds.getOrDefault(it.key, BigDecimal.ZERO),
            currentReportReIncluded = currentlyReportedReIncluded.funds.getOrDefault(it.key, BigDecimal.ZERO),
            totalEligibleAfterControl = totalEligibleAfterControl.funds.getOrDefault(it.key, BigDecimal.ZERO),
            previouslyValidated = previouslyValidated.funds.getOrDefault(it.key, BigDecimal.ZERO),
            previouslyPaid = previouslyPaid.funds.getOrDefault(it.key, BigDecimal.ZERO),
        )
    },
    partnerContribution = ExpenditureCoFinancingBreakdownLine(
        totalEligibleBudget = totalsFromAF.partnerContribution,
        previouslyReported = previouslyReported.partnerContribution,
        previouslyReportedParked = previouslyReportedParked.partnerContribution,
        currentReport = currentlyReported.partnerContribution,
        currentReportReIncluded = currentlyReportedReIncluded.partnerContribution,
        totalEligibleAfterControl = totalEligibleAfterControl.partnerContribution,
        previouslyValidated = previouslyValidated.partnerContribution,
        previouslyPaid = previouslyPaid.partnerContribution,
    ),
    publicContribution = ExpenditureCoFinancingBreakdownLine(
        totalEligibleBudget = totalsFromAF.publicContribution,
        previouslyReported = previouslyReported.publicContribution,
        previouslyReportedParked = previouslyReportedParked.publicContribution,
        currentReport = currentlyReported.publicContribution,
        currentReportReIncluded = currentlyReportedReIncluded.publicContribution,
        totalEligibleAfterControl = totalEligibleAfterControl.publicContribution,
        previouslyValidated = previouslyValidated.publicContribution,
        previouslyPaid = previouslyPaid.publicContribution,
    ),
    automaticPublicContribution = ExpenditureCoFinancingBreakdownLine(
        totalEligibleBudget = totalsFromAF.automaticPublicContribution,
        previouslyReported = previouslyReported.automaticPublicContribution,
        previouslyReportedParked = previouslyReportedParked.automaticPublicContribution,
        currentReport = currentlyReported.automaticPublicContribution,
        currentReportReIncluded = currentlyReportedReIncluded.automaticPublicContribution,
        totalEligibleAfterControl = totalEligibleAfterControl.automaticPublicContribution,
        previouslyValidated = previouslyValidated.automaticPublicContribution,
        previouslyPaid = previouslyPaid.automaticPublicContribution,
    ),
    privateContribution = ExpenditureCoFinancingBreakdownLine(
        totalEligibleBudget = totalsFromAF.privateContribution,
        previouslyReported = previouslyReported.privateContribution,
        previouslyReportedParked = previouslyReportedParked.privateContribution,
        currentReport = currentlyReported.privateContribution,
        currentReportReIncluded = currentlyReportedReIncluded.privateContribution,
        totalEligibleAfterControl = totalEligibleAfterControl.privateContribution,
        previouslyValidated = previouslyValidated.privateContribution,
        previouslyPaid = previouslyPaid.privateContribution,
    ),
    total = ExpenditureCoFinancingBreakdownLine(
        totalEligibleBudget = totalsFromAF.sum,
        previouslyReported = previouslyReported.sum,
        previouslyReportedParked = previouslyReportedParked.sum,
        currentReport = currentlyReported.sum,
        currentReportReIncluded = currentlyReportedReIncluded.sum,
        totalEligibleAfterControl = totalEligibleAfterControl.sum,
        previouslyValidated = previouslyValidated.sum,
        previouslyPaid = previouslyPaid.sum,
    ),
)

fun ExpenditureCoFinancingBreakdown.fillInCurrent(currentData: ExpenditureCoFinancingCurrentWithReIncluded) = apply {
    funds.forEach { fund ->
        fund.currentReport = currentData.current.funds.getOrDefault(fund.fundId, BigDecimal.ZERO)
        fund.currentReportReIncluded = currentData.currentReIncluded.funds.getOrDefault(fund.fundId, BigDecimal.ZERO)
    }
    partnerContribution.currentReport = currentData.current.partnerContribution
    partnerContribution.currentReportReIncluded = currentData.currentReIncluded.partnerContribution
    publicContribution.currentReport = currentData.current.publicContribution
    publicContribution.currentReportReIncluded = currentData.currentReIncluded.publicContribution
    automaticPublicContribution.currentReport = currentData.current.automaticPublicContribution
    automaticPublicContribution.currentReportReIncluded = currentData.currentReIncluded.automaticPublicContribution
    privateContribution.currentReport = currentData.current.privateContribution
    privateContribution.currentReportReIncluded = currentData.currentReIncluded.privateContribution
    total.currentReport = currentData.current.sum
    total.currentReportReIncluded = currentData.currentReIncluded.sum
}

fun ExpenditureCoFinancingBreakdown.fillInCurrent(current: ReportExpenditureCoFinancingColumn) = apply {
    funds.forEach { fund ->
        fund.currentReport = current.funds.getOrDefault(fund.fundId, BigDecimal.ZERO)
    }
    partnerContribution.currentReport = current.partnerContribution
    publicContribution.currentReport = current.publicContribution
    automaticPublicContribution.currentReport = current.automaticPublicContribution
    privateContribution.currentReport = current.privateContribution
    total.currentReport = current.sum
}

fun ExpenditureCoFinancingBreakdown.fillInCurrentReIncluded(currentReIncluded: ReportExpenditureCoFinancingColumn) = apply {
    funds.forEach { fund ->
        fund.currentReportReIncluded = currentReIncluded.funds.getOrDefault(fund.fundId, BigDecimal.ZERO)
    }
    partnerContribution.currentReportReIncluded = currentReIncluded.partnerContribution
    publicContribution.currentReportReIncluded = currentReIncluded.publicContribution
    automaticPublicContribution.currentReportReIncluded = currentReIncluded.automaticPublicContribution
    privateContribution.currentReportReIncluded = currentReIncluded.privateContribution
    total.currentReportReIncluded = currentReIncluded.sum
}

fun ExpenditureCoFinancingBreakdown.fillInOverviewFields() = apply {
    funds.fillInOverviewFields()
    partnerContribution.fillInOverviewFields()
    publicContribution.fillInOverviewFields()
    automaticPublicContribution.fillInOverviewFields()
    privateContribution.fillInOverviewFields()
    total.fillInOverviewFields()
}

fun BigDecimal.applyPercentage(percentage: BigDecimal, roundingMode: RoundingMode = RoundingMode.DOWN): BigDecimal = this.multiply(
    percentage.divide(BigDecimal.valueOf(100))
).setScale(2, roundingMode)

fun getCurrentFrom(input: ReportExpenditureCoFinancingCalculationInput): ReportExpenditureCoFinancingColumn {
    with(input) {
        val funds = fundsPercentages.mapValues { fundPercentage -> currentTotal.applyPercentage(fundPercentage.value) }
        val fundsTotal = funds.values.sumOf { it }
        val partnerContrib = currentTotal.minus(fundsTotal)

        return ReportExpenditureCoFinancingColumn(
            // main funds + partner contribution
            funds = fundsPercentages.mapValues { fundPercentage -> currentTotal.applyPercentage(fundPercentage.value) }
                .plus(Pair(null, partnerContrib)),
            partnerContribution = partnerContrib,
            publicContribution = currentTotal.applyPercentage(publicPercentage),
            automaticPublicContribution = currentTotal.applyPercentage(automaticPublicPercentage),
            privateContribution = currentTotal.applyPercentage(privatePercentage),
            sum = currentTotal,
        )
    }
}

/**
 * Will calculate percentages, which are then applied to split current total value between contribution types
 */
fun ProjectPartnerReportContributionOverview.generateCoFinCalculationInputData(
    totalEligibleBudget: BigDecimal,
    currentValueToSplit: BigDecimal,
    funds: List<ProjectPartnerCoFinancing>,
): ReportExpenditureCoFinancingCalculationInput = generateCoFinCalculationInputDataGeneric(
    totalEligibleBudget = totalEligibleBudget,
    currentValueToSplit = currentValueToSplit,
    coFinancing = funds,
    contributionAmounts = mapOf(
        ProjectPartnerContributionStatus.Public to public.amount,
        ProjectPartnerContributionStatus.AutomaticPublic to automaticPublic.amount,
        ProjectPartnerContributionStatus.Private to private.amount,
    ),
)

fun generateCoFinCalculationInputData(
    totalEligibleBudget: BigDecimal,
    currentValueToSplit: BigDecimal,
    coFinancing: ProjectPartnerCoFinancingAndContribution,
): ReportExpenditureCoFinancingCalculationInput = generateCoFinCalculationInputDataGeneric(
    totalEligibleBudget = totalEligibleBudget,
    currentValueToSplit = currentValueToSplit,
    coFinancing = coFinancing.finances,
    contributionAmounts = mapOf(
        ProjectPartnerContributionStatus.Public to coFinancing.partnerContributions
            .filter { it.status == ProjectPartnerContributionStatusDTO.Public }.sumOf { it.amount ?: BigDecimal.ZERO },
        ProjectPartnerContributionStatus.AutomaticPublic to coFinancing.partnerContributions
            .filter { it.status == ProjectPartnerContributionStatusDTO.AutomaticPublic }.sumOf { it.amount ?: BigDecimal.ZERO },
        ProjectPartnerContributionStatus.Private to coFinancing.partnerContributions
            .filter { it.status == ProjectPartnerContributionStatusDTO.Private }.sumOf { it.amount ?: BigDecimal.ZERO },
    ),
)

private fun generateCoFinCalculationInputDataGeneric(
    totalEligibleBudget: BigDecimal,
    currentValueToSplit: BigDecimal,
    coFinancing: List<ProjectPartnerCoFinancing>,
    contributionAmounts: Map<ProjectPartnerContributionStatus, BigDecimal>,
): ReportExpenditureCoFinancingCalculationInput =
    ReportExpenditureCoFinancingCalculationInput(
        currentTotal = currentValueToSplit,
        fundsPercentages = coFinancing.getMainFunds().associateBy({ it.fund!!.id }, { it.percentage}),
        partnerContributionPercentage = coFinancing.getPartnerContributionPercentage(),
        publicPercentage = contributionAmounts.getOrDefault(ProjectPartnerContributionStatus.Public, BigDecimal.ZERO)
            .getPercentageOf(totalEligibleBudget),
        automaticPublicPercentage = contributionAmounts.getOrDefault(ProjectPartnerContributionStatus.AutomaticPublic, BigDecimal.ZERO)
            .getPercentageOf(totalEligibleBudget),
        privatePercentage = contributionAmounts.getOrDefault(ProjectPartnerContributionStatus.Private, BigDecimal.ZERO)
            .getPercentageOf(totalEligibleBudget),
    )

private fun BigDecimal.getPercentageOf(total: BigDecimal) =
    if (total.compareTo(BigDecimal.ZERO) == 0)
        BigDecimal.ZERO
    else
        this.multiply(BigDecimal.valueOf(100))
            .divide(total, 17, RoundingMode.DOWN)


private fun List<ProjectPartnerCoFinancing>.getMainFunds() = filter { it.fundType == MainFund }

private fun List<ProjectPartnerCoFinancing>.getPartnerContributionPercentage() =
    firstOrNull { it.fundType == PartnerContribution }?.percentage ?: BigDecimal.ZERO
