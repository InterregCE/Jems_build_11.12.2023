package io.cloudflight.jems.api.project.dto.report.partner.expenditure

import io.cloudflight.jems.api.common.dto.file.JemsFileMetadataDTO
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.api.project.dto.report.partner.expenditure.verification.ExpenditureParkingMetadataDTO
import java.math.BigDecimal
import java.time.LocalDate

data class ProjectPartnerReportExpenditureCostDTO(
    val id: Long?,
    val number: Int,
    val lumpSumId: Long?,
    val unitCostId: Long?,
    val costCategory: BudgetCategoryDTO,
    val gdpr: Boolean,
    val investmentId: Long?,
    val contractId: Long?,
    val internalReferenceNumber: String?,
    val invoiceNumber: String?,
    val invoiceDate: LocalDate?,
    val dateOfPayment: LocalDate?,
    val description: Set<InputTranslation> = emptySet(),
    val comment: Set<InputTranslation> = emptySet(),
    val totalValueInvoice: BigDecimal? = null,
    val vat: BigDecimal? = null,
    val numberOfUnits: BigDecimal = BigDecimal.ONE,
    val pricePerUnit: BigDecimal = BigDecimal.ZERO,
    val declaredAmount: BigDecimal = BigDecimal.ZERO,
    val currencyCode: String,
    val currencyConversionRate: BigDecimal?,
    val declaredAmountAfterSubmission: BigDecimal?,
    val attachment: JemsFileMetadataDTO?,
    val parkingMetadata: ExpenditureParkingMetadataDTO?,
)
