package io.cloudflight.jems.server.project.repository.report.partner.control.expenditure

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.common.file.entity.JemsFileMetadataEntity
import io.cloudflight.jems.server.common.file.service.model.JemsFileMetadata
import io.cloudflight.jems.server.project.entity.report.partner.ProjectPartnerReportEntity
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportExpenditureCostEntity
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportExpenditureCostTranslEntity
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportInvestmentEntity
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportLumpSumEntity
import io.cloudflight.jems.server.project.entity.report.partner.expenditure.PartnerReportUnitCostEntity
import io.cloudflight.jems.server.project.repository.report.partner.ProjectPartnerReportRepository
import io.cloudflight.jems.server.project.repository.report.partner.expenditure.ProjectPartnerReportExpenditureRepository
import io.cloudflight.jems.server.project.repository.report.partner.model.ExpenditureVerificationUpdate
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ExpenditureParkingMetadata
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportExpenditureCost
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ProjectPartnerReportExpenditureCurrencyRateChange
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.ReportBudgetCategory
import io.cloudflight.jems.server.project.service.report.model.partner.expenditure.control.ProjectPartnerReportExpenditureVerification
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

class ProjectPartnerReportExpenditureVerificationPersistenceProviderTest : UnitTest() {
    companion object {
        private const val PARTNER_ID = 380L
        private const val EXPENDITURE_TO_UPDATE = 40L

        private val YESTERDAY = LocalDate.now().minusDays(1)
        private val TOMORROW = LocalDate.now().plusDays(1)

        private val dummyAttachment = JemsFileMetadataEntity(
            id = 970L,
            projectId = 4L,
            partnerId = PARTNER_ID,
            path = "",
            minioBucket = "minioBucket",
            minioLocation = "",
            name = "some_file.txt",
            type = mockk(),
            size = 1475,
            user = mockk(),
            uploaded = ZonedDateTime.now(),
            description = "dummy description of attachment",
        )

        private fun dummyExpenditure(
            id: Long,
            report: ProjectPartnerReportEntity,
            lumpSum: PartnerReportLumpSumEntity? = null,
            unitCost: PartnerReportUnitCostEntity? = null,
            investment: PartnerReportInvestmentEntity? = null,
            unParkedFrom: PartnerReportExpenditureCostEntity? = null,
            gdpr: Boolean
        ) = PartnerReportExpenditureCostEntity(
            id = id,
            number = 1,
            partnerReport = report,
            reportLumpSum = lumpSum,
            reportUnitCost = unitCost,
            costCategory = ReportBudgetCategory.InfrastructureCosts,
            gdpr = gdpr,
            reportInvestment = investment,
            procurementId = 18L,
            internalReferenceNumber = "irn",
            invoiceNumber = "invoice",
            invoiceDate = YESTERDAY,
            dateOfPayment = TOMORROW,
            totalValueInvoice = BigDecimal.ONE,
            vat = BigDecimal.ZERO,
            numberOfUnits = BigDecimal.ONE,
            pricePerUnit = BigDecimal.ZERO,
            declaredAmount = BigDecimal.TEN,
            currencyCode = "HUF",
            currencyConversionRate = BigDecimal.valueOf(368),
            declaredAmountAfterSubmission = BigDecimal.valueOf(3680),
            translatedValues = mutableSetOf(),
            attachment = dummyAttachment,
            partOfSample = false,
            certifiedAmount = BigDecimal.ZERO,
            deductedAmount = BigDecimal.ZERO,
            typologyOfErrorId = 1L,
            verificationComment = "dummy comment",
            parked = false,
            unParkedFrom = unParkedFrom,
            reportOfOrigin = report,
            reportProjectOfOrigin = null,
            originalNumber = 12,
            partOfSampleLocked = false
        ).apply {
            translatedValues.add(
                PartnerReportExpenditureCostTranslEntity(
                    TranslationId(this, SystemLanguage.EN),
                    comment = "comment EN",
                    description = "desc EN",
                )
            )
        }

        private fun dummyExpectedExpenditure(id: Long, lumpSumId: Long?, unitCostId: Long?, investmentId: Long?, gdpr: Boolean) =
            ProjectPartnerReportExpenditureVerification(
                id = id,
                number = 1,
                lumpSumId = lumpSumId,
                unitCostId = unitCostId,
                costCategory = ReportBudgetCategory.InfrastructureCosts,
                gdpr = gdpr,
                investmentId = investmentId,
                contractId = 18L,
                internalReferenceNumber = "irn",
                invoiceNumber = "invoice",
                invoiceDate = YESTERDAY,
                dateOfPayment = TOMORROW,
                description = setOf(InputTranslation(SystemLanguage.EN, "desc EN")),
                comment = setOf(InputTranslation(SystemLanguage.EN, "comment EN")),
                totalValueInvoice = BigDecimal.ONE,
                vat = BigDecimal.ZERO,
                numberOfUnits = BigDecimal.ONE,
                pricePerUnit = BigDecimal.ZERO,
                declaredAmount = BigDecimal.TEN,
                currencyCode = "HUF",
                currencyConversionRate = BigDecimal.valueOf(368),
                declaredAmountAfterSubmission = BigDecimal.valueOf(3680),
                attachment = JemsFileMetadata(dummyAttachment.id, dummyAttachment.name, dummyAttachment.uploaded),
                partOfSample = false,
                certifiedAmount = BigDecimal.ZERO,
                deductedAmount = BigDecimal.ZERO,
                typologyOfErrorId = 1L,
                verificationComment = "dummy comment",
                parked = false,
                parkedOn = null,
                parkingMetadata = ExpenditureParkingMetadata(
                    reportOfOriginId = 600L,
                    reportOfOriginNumber = 601,
                    reportProjectOfOriginId = null,
                    originalExpenditureNumber = 12
                ),
                partOfSampleLocked = false
            )

        private fun dummyExpectedExpenditure(id: Long) =
            ProjectPartnerReportExpenditureCost(
                id = id,
                number = 1,
                lumpSumId = null,
                unitCostId = null,
                costCategory = ReportBudgetCategory.InfrastructureCosts,
                gdpr = false,
                investmentId = null,
                contractId = 18L,
                internalReferenceNumber = "irn",
                invoiceNumber = "invoice",
                invoiceDate = YESTERDAY,
                dateOfPayment = TOMORROW,
                description = setOf(InputTranslation(SystemLanguage.EN, "desc EN")),
                comment = setOf(InputTranslation(SystemLanguage.EN, "comment EN")),
                totalValueInvoice = BigDecimal.ONE,
                vat = BigDecimal.ZERO,
                numberOfUnits = BigDecimal.ONE,
                pricePerUnit = BigDecimal.ZERO,
                declaredAmount = BigDecimal.TEN,
                currencyCode = "HUF",
                currencyConversionRate = BigDecimal.valueOf(15L, 1),
                declaredAmountAfterSubmission = BigDecimal.valueOf(33654L, 2),
                attachment = JemsFileMetadata(dummyAttachment.id, dummyAttachment.name, dummyAttachment.uploaded),
                parkingMetadata = null,
            )

        private val dummyExpectedExpenditureUpdate = ExpenditureVerificationUpdate(
            id = EXPENDITURE_TO_UPDATE,
            partOfSample = true,
            certifiedAmount = BigDecimal.valueOf(3670),
            deductedAmount = BigDecimal.valueOf(10),
            typologyOfErrorId = 55L,
            verificationComment = "Some new text",
            parked = false
        )
    }

    @MockK
    private lateinit var reportRepository: ProjectPartnerReportRepository

    @MockK
    private lateinit var reportExpenditureRepository: ProjectPartnerReportExpenditureRepository

    @MockK
    private lateinit var reportExpenditureParkedRepository: PartnerReportParkedExpenditureRepository

    @InjectMockKs
    private lateinit var persistence: ProjectPartnerReportExpenditureVerificationPersistenceProvider

    @BeforeEach
    fun reset() {
        clearMocks(reportRepository, reportExpenditureRepository)
    }

    @Test
    fun getPartnerControlReportExpenditureVerification() {
        val lumpSumId = 808L
        val unitCostId = 809L
        val investmentId = 810L
        val report = mockk<ProjectPartnerReportEntity>()
        every { report.id } returns 600L
        every { report.number } returns 601
        val lumpSum = mockk<PartnerReportLumpSumEntity>()
        val unitCost = mockk<PartnerReportUnitCostEntity>()
        val investment = mockk<PartnerReportInvestmentEntity>()
        every { lumpSum.id } returns lumpSumId
        every { unitCost.id } returns unitCostId
        every { investment.id } returns investmentId

        every { reportExpenditureParkedRepository.findAllByParkedFromExpenditureIdIn(setOf(14L)) } returns emptyList()

        val expenditure = dummyExpenditure(id = 14L, report, lumpSum, unitCost, investment, dummyExpenditure(id = 2L, report, gdpr = false), false)
        every { reportExpenditureRepository.findTop150ByPartnerReportIdAndPartnerReportPartnerIdOrderById(
            reportId = 44L,
            partnerId = PARTNER_ID,
        ) } returns mutableListOf(expenditure)

        assertThat(persistence.getPartnerControlReportExpenditureVerification(PARTNER_ID, reportId = 44L))
            .containsExactly(
                dummyExpectedExpenditure(
                    id = 14L,
                    lumpSumId = lumpSumId,
                    unitCostId = unitCostId,
                    investmentId = investmentId,
                    gdpr = false
                )
            )
    }

    @Test
    fun getPartnerControlReportExpenditureVerificationWithGDPR() {
        val lumpSumId = 808L
        val unitCostId = 809L
        val investmentId = 810L
        val report = mockk<ProjectPartnerReportEntity>()
        every { report.id } returns 600L
        every { report.number } returns 601
        val lumpSum = mockk<PartnerReportLumpSumEntity>()
        val unitCost = mockk<PartnerReportUnitCostEntity>()
        val investment = mockk<PartnerReportInvestmentEntity>()
        every { lumpSum.id } returns lumpSumId
        every { unitCost.id } returns unitCostId
        every { investment.id } returns investmentId

        val expenditure = dummyExpenditure(id = 14L, report, lumpSum, unitCost, investment, dummyExpenditure(id = 2L, report, gdpr = true), gdpr = true)
        every { reportExpenditureRepository.findTop150ByPartnerReportIdAndPartnerReportPartnerIdOrderById(
            reportId = 44L,
            partnerId = PARTNER_ID,
        ) } returns mutableListOf(expenditure)

        assertThat(persistence.getPartnerControlReportExpenditureVerification(PARTNER_ID, reportId = 44L))
            .containsExactly(
                dummyExpectedExpenditure(
                    id = 14L,
                    lumpSumId = lumpSumId,
                    unitCostId = unitCostId,
                    investmentId = investmentId,
                    gdpr = true
                )
            )
    }

    @Test
    fun updatePartnerReportExpenditureCosts() {
        val LUMP_SUM_ID = 708L
        val UNIT_COST_ID = 709L
        val INVESTMENT_ID = 710L
        val report = mockk<ProjectPartnerReportEntity>()
        val lumpSum = mockk<PartnerReportLumpSumEntity>()
        val unitCost = mockk<PartnerReportUnitCostEntity>()
        val investment = mockk<PartnerReportInvestmentEntity>()
        every { lumpSum.id } returns LUMP_SUM_ID
        every { unitCost.id } returns UNIT_COST_ID
        every { investment.id } returns INVESTMENT_ID

        val entityToUpdate = dummyExpenditure(EXPENDITURE_TO_UPDATE, report, lumpSum, unitCost, investment, gdpr = false)

        every { reportExpenditureRepository
            .findTop150ByPartnerReportIdAndPartnerReportPartnerIdOrderById(reportId = 58L, PARTNER_ID)
        } returns mutableListOf(entityToUpdate)
        every { reportExpenditureParkedRepository.findAllByParkedFromExpenditureIdIn(setOf(EXPENDITURE_TO_UPDATE)) } returns emptyList()

        val result = persistence
            .updatePartnerControlReportExpenditureVerification(
                PARTNER_ID, reportId = 58L, listOf(
                    dummyExpectedExpenditureUpdate
                ))

        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().partOfSample).isEqualTo(true)
        assertThat(result.first().certifiedAmount).isEqualByComparingTo(BigDecimal.valueOf(3670))
        assertThat(result.first().deductedAmount).isEqualByComparingTo(BigDecimal.valueOf(10))
        assertThat(result.first().typologyOfErrorId).isEqualTo(55L)
        assertThat(result.first().verificationComment).isEqualTo("Some new text")

        // verify other values were not touched
        assertThat(result.first().lumpSumId).isEqualTo(lumpSum.id)
        assertThat(result.first().unitCostId).isEqualTo(unitCost.id)
        assertThat(result.first().costCategory).isEqualTo(ReportBudgetCategory.InfrastructureCosts)
        assertFalse(result.first().gdpr)
        assertThat(result.first().investmentId).isEqualTo(investment.id)
        assertThat(result.first().internalReferenceNumber).isEqualTo("irn")
        assertThat(result.first().invoiceNumber).isEqualTo("invoice")
        assertThat(result.first().invoiceDate).isEqualTo(YESTERDAY)
        assertThat(result.first().dateOfPayment).isEqualTo(TOMORROW)
        assertThat(result.first().comment.first().translation).isEqualTo("comment EN")
        assertThat(result.first().description.first().translation).isEqualTo("desc EN")
        assertThat(result.first().totalValueInvoice).isEqualByComparingTo(BigDecimal.ONE)
        assertThat(result.first().vat).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(result.first().numberOfUnits).isEqualByComparingTo(BigDecimal.ONE)
        assertThat(result.first().pricePerUnit).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(result.first().declaredAmount).isEqualByComparingTo(BigDecimal.TEN)
        assertThat(result.first().currencyCode).isEqualTo("HUF")
        assertThat(result.first().currencyConversionRate).isEqualByComparingTo(BigDecimal.valueOf(368))
        assertThat(result.first().declaredAmountAfterSubmission).isEqualByComparingTo(BigDecimal.valueOf(3680))
    }

    @Test
    fun updateExpenditureCurrencyRatesAndClearVerification() {
        val report = mockk<ProjectPartnerReportEntity>()
        every { report.id } returns 55L
        every { report.number } returns 16
        every { reportRepository.findByIdAndPartnerId(id = 55L, PARTNER_ID) } returns report

        val rate = BigDecimal.valueOf(15L, 1)
        val declared = BigDecimal.valueOf(33654L, 2)

        val expenditure = dummyExpenditure(780L, report, gdpr = false)
        expenditure.partOfSample = true
        expenditure.partOfSampleLocked = true
        expenditure.deductedAmount = BigDecimal.ONE
        val expenditureReIncluded = dummyExpenditure(781L, report, gdpr = false, unParkedFrom = mockk())
        expenditureReIncluded.partOfSample = true
        expenditureReIncluded.partOfSampleLocked = true
        expenditureReIncluded.deductedAmount = BigDecimal.ONE
        // verify before test that all values are not same as after test
        setOf(expenditure, expenditureReIncluded).forEach {
            assertThat(it.currencyConversionRate).isNotEqualByComparingTo(rate)
            assertThat(it.declaredAmountAfterSubmission).isNotEqualByComparingTo(declared)
            assertThat(it.partOfSample).isTrue()
            assertThat(it.partOfSampleLocked).isTrue()
            assertThat(it.certifiedAmount).isNotEqualByComparingTo(it.declaredAmountAfterSubmission)
            assertThat(it.deductedAmount).isNotEqualByComparingTo(BigDecimal.ZERO)
            assertThat(it.typologyOfErrorId).isNotNull()
        }

        every { reportExpenditureRepository.findByPartnerReportOrderByIdDesc(report) } returns
                mutableListOf(expenditure, expenditureReIncluded)

        val newRate = ProjectPartnerReportExpenditureCurrencyRateChange(
            id = 780L,
            currencyConversionRate = rate,
            declaredAmountAfterSubmission = declared,
        )

        assertThat(persistence.updateExpenditureCurrencyRatesAndClearVerification(PARTNER_ID, reportId = 55L, setOf(newRate)))
            .containsExactly(
                // updated rate and cleared verification
                dummyExpectedExpenditure(780L),
                // only cleared verification
                dummyExpectedExpenditure(781L).copy(
                    currencyConversionRate = BigDecimal.valueOf(368L),
                    declaredAmountAfterSubmission = BigDecimal.valueOf(3680L),
                    parkingMetadata = ExpenditureParkingMetadata(
                        reportOfOriginId = 55L,
                        reportOfOriginNumber = 16,
                        reportProjectOfOriginId = null,
                        originalExpenditureNumber = 12
                    ),
                ),
            )
    }

}
