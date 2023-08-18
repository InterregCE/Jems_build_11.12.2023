package io.cloudflight.jems.server.project.repository.report.project.verification

import io.cloudflight.jems.server.common.entity.extractField
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportExpenditureCostEntity
import io.cloudflight.jems.server.project.entity.report.partner.procurement.ProjectPartnerReportProcurementEntity
import io.cloudflight.jems.server.project.entity.report.project.ProjectReportEntity
import io.cloudflight.jems.server.project.entity.report.verification.expenditure.ProjectReportVerificationExpenditureEntity
import io.cloudflight.jems.server.project.repository.report.partner.expenditure.getParkingMetadata
import io.cloudflight.jems.server.project.repository.report.partner.expenditure.toModel
import io.cloudflight.jems.server.project.repository.report.partner.financialOverview.investment.toModel
import io.cloudflight.jems.server.project.repository.report.partner.procurement.toModel
import io.cloudflight.jems.server.project.repository.report.partner.toModel
import io.cloudflight.jems.server.project.service.report.model.project.verification.expenditure.ProjectPartnerReportExpenditureItem
import io.cloudflight.jems.server.project.service.report.model.project.verification.expenditure.ProjectReportVerificationExpenditureLine
import io.cloudflight.jems.server.project.service.report.model.project.verification.expenditure.ProjectReportVerificationRiskBased
import java.math.BigDecimal

fun Collection<ProjectReportVerificationExpenditureEntity>.toExtendedModel(procurementsById: Map<Long, ProjectPartnerReportProcurementEntity>) =
    map {
        it.toModel(procurement = with(it.expenditure) {
            procurementId?.let { procurementsById[it] }
        })
    }

fun PartnerReportExpenditureCostEntity.toEmptyVerificationEntity() =
    ProjectReportVerificationExpenditureEntity(
        expenditureId = this.id,
        expenditure = this,
        partOfVerificationSample = false,
        deductedByJs = BigDecimal.ZERO,
        deductedByMa = BigDecimal.ZERO,
        amountAfterVerification = this.certifiedAmount,
        typologyOfErrorId = null,
        parked = false,
        verificationComment = null
    )


fun List<ProjectReportVerificationExpenditureEntity>.toModels(
    procurementsById: Map<Long, ProjectPartnerReportProcurementEntity>,
): List<ProjectReportVerificationExpenditureLine> = map {
    it.toModel(
        procurement = with(it.expenditure) {
            procurementId?.let { procurementsById[it] }
        }
    )
}

fun PartnerReportExpenditureCostEntity.toExpenditurePart(
    procurement: ProjectPartnerReportProcurementEntity?,
) = ProjectPartnerReportExpenditureItem(
    id = id,
    number = number,

    partnerId = partnerReport.partnerId,
    partnerRole = partnerReport.identification.partnerRole,
    partnerNumber = partnerReport.identification.partnerNumber,

    partnerReportId = partnerReport.id,
    partnerReportNumber = partnerReport.number,

    lumpSum = reportLumpSum?.toModel(),
    unitCost = reportUnitCost?.toModel(),
    gdpr = gdpr,
    costCategory = costCategory,
    investment = reportInvestment?.toModel(),
    contract = procurement?.toModel(),
    internalReferenceNumber = internalReferenceNumber,
    invoiceNumber = invoiceNumber,

    invoiceDate = invoiceDate,
    dateOfPayment = dateOfPayment,
    description = translatedValues.extractField { it.description },
    comment = translatedValues.extractField { it.comment },
    totalValueInvoice = totalValueInvoice,
    vat = vat,
    numberOfUnits = numberOfUnits,
    pricePerUnit = pricePerUnit,
    declaredAmount = declaredAmount,
    currencyCode = currencyCode,
    currencyConversionRate = currencyConversionRate,
    declaredAmountAfterSubmission = declaredAmountAfterSubmission,
    attachment = attachment?.toModel(),

    partOfSample = partOfSample,
    partOfSampleLocked = partOfSampleLocked,
    certifiedAmount = certifiedAmount,
    deductedAmount = deductedAmount,
    typologyOfErrorId = typologyOfErrorId,
    parked = parked,
    verificationComment = verificationComment,

    parkingMetadata = getParkingMetadata(),
)

fun ProjectReportVerificationExpenditureEntity.toModel(procurement: ProjectPartnerReportProcurementEntity?) =
    ProjectReportVerificationExpenditureLine(
        expenditure = expenditure.toExpenditurePart(procurement),
        partOfVerificationSample = partOfVerificationSample,
        deductedByJs = deductedByJs,
        deductedByMa = deductedByMa,
        amountAfterVerification = amountAfterVerification,
        typologyOfErrorId = typologyOfErrorId,
        parked = parked,
        verificationComment = verificationComment
    )

fun ProjectReportEntity.toRiskBasedModel() =
    ProjectReportVerificationRiskBased(
        projectReportId = this.id,
        riskBasedVerification = this.riskBasedVerification,
        riskBasedVerificationDescription = this.riskBasedVerificationDescription
    )

