package io.cloudflight.jems.server.payments.repository.applicationToEc

import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanOperation
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import io.cloudflight.jems.api.programme.dto.priority.ProgrammeObjectivePolicy
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.common.file.entity.JemsFileMetadataEntity
import io.cloudflight.jems.server.common.file.repository.JemsFileMetadataRepository
import io.cloudflight.jems.server.common.file.service.JemsSystemFileService
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.payments.accountingYears.repository.AccountingYearRepository
import io.cloudflight.jems.server.payments.entity.AccountingYearEntity
import io.cloudflight.jems.server.payments.entity.PaymentApplicationToEcEntity
import io.cloudflight.jems.server.payments.entity.PaymentEntity
import io.cloudflight.jems.server.payments.entity.PaymentToEcCumulativeAmountsEntity
import io.cloudflight.jems.server.payments.entity.PaymentToEcExtensionEntity
import io.cloudflight.jems.server.payments.entity.QPaymentEntity
import io.cloudflight.jems.server.payments.model.ec.*
import io.cloudflight.jems.server.payments.model.regular.AccountingYear
import io.cloudflight.jems.server.payments.model.regular.PaymentEcStatus
import io.cloudflight.jems.server.payments.model.regular.PaymentSearchRequestScoBasis
import io.cloudflight.jems.server.payments.model.regular.PaymentType
import io.cloudflight.jems.server.programme.entity.ProgrammePriorityEntity
import io.cloudflight.jems.server.programme.entity.QProgrammePriorityEntity
import io.cloudflight.jems.server.programme.entity.QProgrammeSpecificObjectiveEntity
import io.cloudflight.jems.server.programme.entity.fund.ProgrammeFundEntity
import io.cloudflight.jems.server.programme.repository.fund.ProgrammeFundRepository
import io.cloudflight.jems.server.programme.repository.priority.ProgrammePriorityRepository
import io.cloudflight.jems.server.programme.service.fund.model.ProgrammeFund
import io.cloudflight.jems.server.project.service.contracting.model.ContractingMonitoringExtendedOption
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class PaymentApplicationsToEcPersistenceProviderTest : UnitTest() {

    @MockK
    private lateinit var paymentApplicationsToEcRepository: PaymentApplicationsToEcRepository

    @MockK
    private lateinit var paymentToEcExtensionRepository: PaymentToEcExtensionRepository

    @MockK
    private lateinit var programmeFundRepository: ProgrammeFundRepository

    @MockK
    private lateinit var accountingYearRepository: AccountingYearRepository

    @MockK
    private lateinit var fileRepository: JemsSystemFileService

    @MockK
    private lateinit var reportFileRepository: JemsFileMetadataRepository

    @MockK
    private lateinit var paymentToEcCumulativeAmountsRepository: PaymentToEcCumulativeAmountsRepository

    @MockK
    private lateinit var programmePriorityRepository: ProgrammePriorityRepository

    @MockK
    private lateinit var jpaQueryFactory: JPAQueryFactory

    @InjectMockKs
    private lateinit var persistenceProvider: PaymentApplicationToEcPersistenceProvider

    companion object {
        private const val paymentApplicationsToEcId = 1L
        private const val accountingYearId = 3L
        private const val expectedAccountingYearId = 4L
        private const val programmeFundId = 10L
        private const val expectedProgrammeFundId = 11L
        private val submissionDate = LocalDate.now()

        private val programmeFundEntity = ProgrammeFundEntity(programmeFundId, true)
        private val expectedProgrammeFundEntity = ProgrammeFundEntity(expectedProgrammeFundId, true)
        private val programmeFund = ProgrammeFund(programmeFundId, true)
        private val expectedProgrammeFund = ProgrammeFund(expectedProgrammeFundId, true)

        private val accountingYearEntity =
            AccountingYearEntity(accountingYearId, 2021, LocalDate.of(2021, 1, 1), LocalDate.of(2022, 6, 30))
        private val expectedAccountingYearEntity =
            AccountingYearEntity(expectedAccountingYearId, 2021, LocalDate.of(2021, 1, 1), LocalDate.of(2022, 6, 30))
        private val accountingYear =
            AccountingYear(accountingYearId, 2021, LocalDate.of(2021, 1, 1), LocalDate.of(2022, 6, 30))
        private val expectedAccountingYear =
            AccountingYear(expectedAccountingYearId, 2021, LocalDate.of(2021, 1, 1), LocalDate.of(2022, 6, 30))

        private val paymentApplicationToEcCreate = PaymentApplicationToEcCreate(
            id = null,
            programmeFundId = programmeFund.id,
            accountingYearId = accountingYearId,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        private val paymentApplicationToEcUpdate = PaymentApplicationToEcSummaryUpdate(
            id = paymentApplicationsToEcId,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        private val paymentApplicationsToEcSummary = PaymentApplicationToEcSummary(
            programmeFund = programmeFund,
            accountingYear = accountingYear,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        private val expectedPaymentApplicationsToEcSummary = PaymentApplicationToEcSummary(
            programmeFund = programmeFund,
            accountingYear = accountingYear,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )


        private val expectedPaymentApplicationsToEcCreateSummary = PaymentApplicationToEcSummary(
            programmeFund = programmeFund,
            accountingYear = accountingYear,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        private val expectedPaymentApplicationToEcCreate = PaymentApplicationToEcDetail(
            id = 0,
            status = PaymentEcStatus.Draft,
            paymentApplicationToEcSummary = expectedPaymentApplicationsToEcCreateSummary
        )


        private val expectedPaymentApplicationToEc = PaymentApplicationToEcDetail(
            id = paymentApplicationsToEcId,
            status = PaymentEcStatus.Draft,
            paymentApplicationToEcSummary = paymentApplicationsToEcSummary
        )

        private val expectedPaymentApplicationToEcFinalized = PaymentApplicationToEcDetail(
            id = paymentApplicationsToEcId,
            status = PaymentEcStatus.Finished,
            paymentApplicationToEcSummary = expectedPaymentApplicationsToEcSummary
        )

        private fun paymentApplicationsToEcEntity() = PaymentApplicationToEcEntity(
            id = paymentApplicationsToEcId,
            programmeFund = programmeFundEntity,
            accountingYear = accountingYearEntity,
            status = PaymentEcStatus.Draft,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        private val paymentApplicationToEcEntity = PaymentApplicationToEcEntity(
            id = paymentApplicationsToEcId,
            programmeFund = programmeFundEntity,
            accountingYear = accountingYearEntity,
            status = PaymentEcStatus.Draft,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        private val paymentApplicationToEc = PaymentApplicationToEc(
            id = paymentApplicationsToEcId,
            programmeFund = programmeFund,
            accountingYear = accountingYear,
            status = PaymentEcStatus.Draft
        )

        private val ftlsPayment = PaymentEntity(
            id = 99L,
            type = PaymentType.FTLS,
            project = mockk(),
            projectCustomIdentifier = "sample project",
            amountApprovedPerFund = BigDecimal.valueOf(255.88),
            fund = mockk(),
            projectAcronym = "sample",
            projectLumpSum = mockk(),
            projectReport = mockk()
        )

        private fun paymentToEcExtensionEntity(paymentApplicationToEcEntity: PaymentApplicationToEcEntity?) =
            PaymentToEcExtensionEntity(
                paymentId = 99L,
                payment = ftlsPayment,
                autoPublicContribution = BigDecimal.ZERO,
                correctedAutoPublicContribution = BigDecimal.ZERO,
                partnerContribution = BigDecimal.valueOf(45.80),
                publicContribution = BigDecimal.valueOf(25.00),
                correctedPublicContribution = BigDecimal.valueOf(55.00),
                privateContribution = BigDecimal.ZERO,
                correctedPrivateContribution = BigDecimal.ZERO,
                paymentApplicationToEc = paymentApplicationToEcEntity,
                finalScoBasis = null,
            )

        private val paymentToEcExtensionModel = PaymentToEcExtension(
            paymentId = 99L,
            ecPaymentId = paymentApplicationsToEcId,
            ecPaymentStatus = PaymentEcStatus.Draft
        )

        private val programmePriority1 = ProgrammePriorityEntity(
            id = 1L,
            code = "PO1",
            objective = ProgrammeObjectivePolicy.AdvancedTechnologies.objective
        )
        private val programmePriority2 = ProgrammePriorityEntity(
            id = 2L,
            code = "PO2",
            objective = ProgrammeObjectivePolicy.AdvancedTechnologies.objective
        )

        private val selectedPaymentsToEcEntities = listOf(
            PaymentToEcCumulativeAmountsEntity(
                id = 1L,
                paymentApplicationToEc = paymentApplicationToEcEntity,
                priorityAxis = programmePriority1,
                type = PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95,
                totalEligibleExpenditure = BigDecimal(101),
                totalUnionContribution = BigDecimal.ZERO,
                totalPublicContribution = BigDecimal(102)
            ),
            PaymentToEcCumulativeAmountsEntity(
                id = 2L,
                paymentApplicationToEc = paymentApplicationToEcEntity,
                priorityAxis = programmePriority2,
                type = PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95,
                totalEligibleExpenditure = BigDecimal(201),
                totalUnionContribution = BigDecimal.ZERO,
                totalPublicContribution = BigDecimal(202)
            ),
        )

       private val expectedPaymentsIncludedInPaymentsToEcMapped = mapOf(
            Pair(
                PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95, listOf(
                    PaymentToEcAmountSummaryLine(
                        priorityAxis = "PO1",
                        totalEligibleExpenditure = BigDecimal(101),
                        totalUnionContribution = BigDecimal.ZERO,
                        totalPublicContribution = BigDecimal(102)
                    ),
                    PaymentToEcAmountSummaryLine(
                        priorityAxis = "PO2",
                        totalEligibleExpenditure = BigDecimal(201),
                        totalUnionContribution = BigDecimal.ZERO,
                        totalPublicContribution = BigDecimal(202)
                    )
                )
            ),
            Pair(
                PaymentSearchRequestScoBasis.FallsUnderArticle94Or95, emptyList()
            )
        )
    }

    @BeforeEach
    fun resetMocks() {
        clearMocks(paymentApplicationsToEcRepository, programmeFundRepository, accountingYearRepository)
    }

    @Test
    fun createPaymentApplicationToEc() {
        every { programmeFundRepository.getById(programmeFundId) } returns programmeFundEntity
        every { accountingYearRepository.getById(accountingYearId) } returns accountingYearEntity
        every { paymentApplicationsToEcRepository.save(any()) } returnsArgument 0

        assertThat(persistenceProvider.createPaymentApplicationToEc(paymentApplicationToEcCreate)).isEqualTo(
            expectedPaymentApplicationToEcCreate
        )
        verify(exactly = 1) { paymentApplicationsToEcRepository.save(any()) }
    }

    @Test
    fun findAll() {
        every { paymentApplicationsToEcRepository.findAll(Pageable.unpaged()) } returns PageImpl(
            listOf(
                paymentApplicationToEcEntity
            )
        )

        assertThat(persistenceProvider.findAll(Pageable.unpaged())).isEqualTo(PageImpl(listOf(paymentApplicationToEc)))
    }

    @Test
    fun getPaymentApplicationToEcDetail() {
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationsToEcEntity()
        assertThat(persistenceProvider.getPaymentApplicationToEcDetail(paymentApplicationsToEcId)).isEqualTo(
            expectedPaymentApplicationToEc
        )
    }

    @Test
    fun updatePaymentApplicationToEc() {

        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns PaymentApplicationToEcEntity(
            id = paymentApplicationsToEcId,
            programmeFund = expectedProgrammeFundEntity,
            accountingYear = expectedAccountingYearEntity,
            status = PaymentEcStatus.Draft,
            nationalReference = "National Reference",
            technicalAssistanceEur = BigDecimal.valueOf(105.32),
            submissionToSfcDate = submissionDate,
            sfcNumber = "SFC number",
            comment = "Comment"
        )

        assertThat(persistenceProvider.updatePaymentApplicationToEc(paymentApplicationsToEcId, paymentApplicationToEcUpdate)).isEqualTo(
            PaymentApplicationToEcDetail(
                id = paymentApplicationsToEcId, status = PaymentEcStatus.Draft, isAvailableToReOpen = false,
                paymentApplicationToEcSummary =  PaymentApplicationToEcSummary(
                    programmeFund = expectedProgrammeFund,
                    accountingYear = expectedAccountingYear,
                    nationalReference = "National Reference",
                    technicalAssistanceEur = BigDecimal.valueOf(105.32),
                    submissionToSfcDate = submissionDate,
                    sfcNumber = "SFC number",
                    comment = "Comment"
                )
            )
        )
    }

    @Test
    fun updatePaymentApplicationToEcSummaryOtherSection() {
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationsToEcEntity()

        assertThat(persistenceProvider.updatePaymentToEcSummaryOtherSection(paymentApplicationToEcUpdate)).isEqualTo(
            expectedPaymentApplicationToEc
        )
    }

    @Test
    fun deleteById() {
        every { paymentApplicationsToEcRepository.deleteById(paymentApplicationsToEcId) } returns Unit
        persistenceProvider.deleteById(paymentApplicationsToEcId)

        verify(exactly = 1) { paymentApplicationsToEcRepository.deleteById(paymentApplicationsToEcId) }
    }

    @Test
    fun deletePaymentToEcAttachment() {
        val file = mockk<JemsFileMetadataEntity>()
        every { fileRepository.delete(file) } answers { }
        every { reportFileRepository.findByTypeAndId(JemsFileType.PaymentToEcAttachment, 16L) } returns file
        persistenceProvider.deletePaymentToEcAttachment(16L)
        verify(exactly = 1) { fileRepository.delete(file) }
    }

    @Test
    fun `deletePaymentToEcAttachment - not existing`() {
        every { reportFileRepository.findByTypeAndId(JemsFileType.PaymentToEcAttachment, -1L) } returns null
        assertThrows<ResourceNotFoundException> { persistenceProvider.deletePaymentToEcAttachment(-1L) }
        verify(exactly = 0) { fileRepository.delete(any()) }
    }

    @Test
    fun finalizePaymentToEc() {
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationsToEcEntity()
        assertThat(
            persistenceProvider.updatePaymentApplicationToEcStatus(
                paymentApplicationsToEcId,
                PaymentEcStatus.Finished
            )
        ).isEqualTo(expectedPaymentApplicationToEcFinalized)
    }

    @Test
    fun getPaymentExtension() {
        every { paymentToEcExtensionRepository.getById(99L) } returns paymentToEcExtensionEntity(paymentApplicationToEcEntity)
        assertThat(persistenceProvider.getPaymentExtension(99L)).isEqualTo(paymentToEcExtensionModel)
    }

    @Test
    fun getPaymentsLinkedToEcPayment() {
        every { paymentToEcExtensionRepository.findAllByPaymentApplicationToEcId(paymentApplicationsToEcId) } returns
            listOf(paymentToEcExtensionEntity(paymentApplicationToEcEntity))
        val query = mockk<JPAQuery<Tuple>>()
        every { jpaQueryFactory.select(any(), any(), any(), any(), any()) } returns query
        every { query.from(any()) } returns query
        every { query.leftJoin(any<EntityPath<Any>>()) } returns query
        every { query.on(any()) } returns query
        val slotWhere = slot<Predicate>()
        every { query.where(capture(slotWhere)) } returns query

        val tuple = mockk<Tuple>()
        every { tuple.get(0, Long::class.java) } returns 1L
        every { tuple.get(1, PaymentType::class.java) } returns PaymentType.REGULAR
        every { tuple.get(2, PaymentSearchRequestScoBasis::class.java) } returns PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95
        every { tuple.get(3, ContractingMonitoringExtendedOption::class.java) } returns ContractingMonitoringExtendedOption.No
        every { tuple.get(4, ContractingMonitoringExtendedOption::class.java) } returns ContractingMonitoringExtendedOption.No

        val result = mockk<List<Tuple>>()
        every { result.size } returns 1
        every { query.fetch() } returns listOf(tuple)

        assertThat(persistenceProvider.getPaymentsLinkedToEcPayment(paymentApplicationsToEcId)).isEqualTo(
            mapOf(
                1L to PaymentInEcPaymentMetadata(
                    paymentId = 1,
                    type = PaymentType.REGULAR,
                    finalScoBasis = PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95,
                    typologyProv94 = ContractingMonitoringExtendedOption.No,
                    typologyProv95 = ContractingMonitoringExtendedOption.No
                )
            )
        )
    }

    @Test
    fun selectPaymentToEcPayment() {
        val entity = paymentToEcExtensionEntity(null)
        every { paymentToEcExtensionRepository.findAllById(setOf(99L)) } returns listOf(entity)
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationToEcEntity

        persistenceProvider.selectPaymentToEcPayment(paymentIds = setOf(99L), ecPaymentId = paymentApplicationsToEcId)
        assertThat(entity.paymentApplicationToEc).isEqualTo(paymentApplicationToEcEntity)
    }

    @Test
    fun deselectPaymentFromEcPaymentAndResetFields() {
        val entity = paymentToEcExtensionEntity(paymentApplicationToEcEntity)
        every { paymentToEcExtensionRepository.findById(99L) } returns Optional.of(entity)
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationToEcEntity

        persistenceProvider.deselectPaymentFromEcPaymentAndResetFields(99L)
        assertThat(entity.paymentApplicationToEc).isEqualTo(null)
        assertThat(entity.correctedPublicContribution).isEqualTo(BigDecimal.valueOf(25.00))
        assertThat(entity.correctedAutoPublicContribution).isEqualTo(BigDecimal.ZERO)
        assertThat(entity.correctedPrivateContribution).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun updatePaymentToEcCorrectedAmounts() {
        val entity = paymentToEcExtensionEntity(paymentApplicationToEcEntity)
        every { paymentToEcExtensionRepository.getById(99L) } returns entity
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationToEcEntity
        val update = PaymentToEcLinkingUpdate(
            correctedPrivateContribution = BigDecimal.TEN,
            correctedPublicContribution = BigDecimal.valueOf(100.00),
            correctedAutoPublicContribution = BigDecimal.ZERO
        )
        persistenceProvider.updatePaymentToEcCorrectedAmounts(99L, update)
        assertThat(entity.correctedPublicContribution).isEqualTo(BigDecimal.valueOf(100.00))
        assertThat(entity.correctedAutoPublicContribution).isEqualTo(BigDecimal.ZERO)
        assertThat(entity.correctedPrivateContribution).isEqualTo(BigDecimal.TEN)
    }

    @Test
    fun calculateAndGetTotals() {

        val expectedPaymentsToEcTmp = listOf(
            PaymentToEcAmountSummaryLineTmp(
                priorityAxis = "PO1",
                fundAmount = BigDecimal.valueOf(100),
                partnerContribution = BigDecimal.valueOf(200),
                ofWhichPublic = BigDecimal.valueOf(300),
                ofWhichAutoPublic = BigDecimal.valueOf(400),
            ),
        )

        val query = mockk<JPAQuery<Tuple>>()
        every { jpaQueryFactory.select(any(), any(), any(), any(), any(), any(), any()) } returns query
        every { query.from(any()) } returns query
        val slotLeftJoin = mutableListOf<EntityPath<Any>>()
        every { query.leftJoin(capture(slotLeftJoin)) } returns query
        val slotLeftJoinOn = mutableListOf<BooleanOperation>()
        every { query.on(capture(slotLeftJoinOn)) } returns query
        every { query.groupBy(any()) } returns query
        val slotWhere = slot<BooleanOperation>()
        every { query.where(capture(slotWhere)) } returns query

        val tuple = mockk<Tuple>()
        every { tuple.get(1, String::class.java) } returns "PO1"
        every { tuple.get(2, BigDecimal::class.java) } returns BigDecimal.valueOf(100)
        every { tuple.get(3, BigDecimal::class.java) } returns BigDecimal.valueOf(200)
        every { tuple.get(4, BigDecimal::class.java) } returns BigDecimal.valueOf(300)
        every { tuple.get(5, BigDecimal::class.java) } returns BigDecimal.valueOf(400)

        val result = mockk<List<Tuple>>()
        every { result.size } returns 1
        every { query.fetch() } returns listOf(tuple)

        assertThat(
            persistenceProvider.calculateAndGetTotals(15L)
        ).containsExactlyInAnyOrderEntriesOf(mapOf(
            PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95 to expectedPaymentsToEcTmp,
            PaymentSearchRequestScoBasis.FallsUnderArticle94Or95 to emptyList(),
        ))
        assertThat(slotLeftJoin).hasSize(3)
        assertThat(slotLeftJoin[0]).isInstanceOf(QPaymentEntity::class.java)
        assertThat(slotLeftJoinOn[0].toString())
            .isEqualTo("paymentEntity.id = paymentToEcExtensionEntity.payment.id")
        assertThat(slotLeftJoin[1]).isInstanceOf(QProgrammeSpecificObjectiveEntity::class.java)
        assertThat(slotLeftJoinOn[1].toString())
            .isEqualTo("programmeSpecificObjectiveEntity.programmeObjectivePolicy = paymentEntity.project.priorityPolicy.programmeObjectivePolicy")
        assertThat(slotLeftJoin[2]).isInstanceOf(QProgrammePriorityEntity::class.java)
        assertThat(slotLeftJoinOn[2].toString())
            .isEqualTo("programmePriorityEntity.id = programmeSpecificObjectiveEntity.programmePriority.id")
        assertThat(slotWhere.captured.toString())
            .isEqualTo("paymentToEcExtensionEntity.paymentApplicationToEc.id = 15")
    }

    @Test
    fun saveTotalsWhenFinishingEcPayment() {
        val paymentsToSave = listOf(
            PaymentToEcAmountSummaryLine(
                priorityAxis = "PO1",
                totalEligibleExpenditure = BigDecimal(101),
                totalUnionContribution = BigDecimal.ZERO,
                totalPublicContribution = BigDecimal(102)
            ),
            PaymentToEcAmountSummaryLine(
                priorityAxis = "PO2",
                totalEligibleExpenditure = BigDecimal(201),
                totalUnionContribution = BigDecimal.ZERO,
                totalPublicContribution = BigDecimal(202)
            )
        )
        val entitySlot = slot<List<PaymentToEcCumulativeAmountsEntity>>()
        every { programmePriorityRepository.getAllByCodeIn(listOf("PO1", "PO2")) } returns listOf(
            programmePriority1,
            programmePriority2
        )
        every { paymentApplicationsToEcRepository.getById(paymentApplicationsToEcId) } returns paymentApplicationsToEcEntity()
        every { paymentToEcCumulativeAmountsRepository.saveAll(capture(entitySlot)) } returnsArgument 0

        persistenceProvider.saveTotalsWhenFinishingEcPayment(
            ecPaymentId = paymentApplicationsToEcId,
            totals = mapOf(Pair(PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95, paymentsToSave))
        )
        verify(exactly = 1) { paymentToEcCumulativeAmountsRepository.saveAll(entitySlot.captured) }
    }


    @Test
    fun `getTotalsForFinishedEcPayment - type ArtNot94Not95`() {

        every {
            paymentToEcCumulativeAmountsRepository.getAllByPaymentApplicationToEcIdAndType(
                paymentApplicationsToEcId,
                PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95
            )
        } returns selectedPaymentsToEcEntities
        every {
            paymentToEcCumulativeAmountsRepository.getAllByPaymentApplicationToEcIdAndType(
                paymentApplicationsToEcId,
                PaymentSearchRequestScoBasis.FallsUnderArticle94Or95
            )
        } returns emptyList()


        assertThat(
            persistenceProvider.getTotalsForFinishedEcPayment(
                ecPaymentId = paymentApplicationsToEcId
            )
        ).isEqualTo(expectedPaymentsIncludedInPaymentsToEcMapped)
    }

    @Test
    fun `getTotalsForFinishedEcPayment - type null`() {
        every {
            paymentToEcCumulativeAmountsRepository.getAllByPaymentApplicationToEcIdAndType(
                paymentApplicationsToEcId,
                PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95
            )
        } returns selectedPaymentsToEcEntities
        every {
            paymentToEcCumulativeAmountsRepository.getAllByPaymentApplicationToEcIdAndType(
                paymentApplicationsToEcId,
                PaymentSearchRequestScoBasis.FallsUnderArticle94Or95
            )
        } returns emptyList()

        assertThat(
            persistenceProvider.getTotalsForFinishedEcPayment(
                ecPaymentId = paymentApplicationsToEcId
            )
        ).isEqualTo(expectedPaymentsIncludedInPaymentsToEcMapped)
    }

    @Test
    fun updatePaymentToEcFinalScoBasis() {
        val entity = paymentToEcExtensionEntity(paymentApplicationToEcEntity)
        every { paymentToEcExtensionRepository.findAllById(setOf(99L)) } returns listOf(entity)
        persistenceProvider.updatePaymentToEcFinalScoBasis(mapOf(99L to PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95))
        assertThat(entity.finalScoBasis).isEqualTo(PaymentSearchRequestScoBasis.DoesNotFallUnderArticle94Nor95)
    }

}
