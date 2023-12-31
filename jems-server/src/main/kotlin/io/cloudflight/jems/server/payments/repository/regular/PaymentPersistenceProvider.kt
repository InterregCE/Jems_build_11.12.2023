package io.cloudflight.jems.server.payments.repository.regular

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.common.file.repository.JemsFileMetadataRepository
import io.cloudflight.jems.server.common.file.service.JemsProjectFileService
import io.cloudflight.jems.server.common.file.service.model.JemsFileType.PaymentAttachment
import io.cloudflight.jems.server.payments.entity.PaymentGroupingId
import io.cloudflight.jems.server.payments.entity.PaymentPartnerEntity
import io.cloudflight.jems.server.payments.entity.QPaymentEntity
import io.cloudflight.jems.server.payments.entity.QPaymentPartnerEntity
import io.cloudflight.jems.server.payments.entity.QPaymentPartnerInstallmentEntity
import io.cloudflight.jems.server.payments.model.regular.PartnerPayment
import io.cloudflight.jems.server.payments.model.regular.PartnerPaymentSimple
import io.cloudflight.jems.server.payments.model.regular.PaymentConfirmedInfo
import io.cloudflight.jems.server.payments.model.regular.PaymentDetail
import io.cloudflight.jems.server.payments.model.regular.PaymentPartnerInstallment
import io.cloudflight.jems.server.payments.model.regular.PaymentPartnerInstallmentUpdate
import io.cloudflight.jems.server.payments.model.regular.PaymentPerPartner
import io.cloudflight.jems.server.payments.model.regular.PaymentRegularToCreate
import io.cloudflight.jems.server.payments.model.regular.PaymentSearchRequest
import io.cloudflight.jems.server.payments.model.regular.PaymentToCreate
import io.cloudflight.jems.server.payments.model.regular.PaymentToProject
import io.cloudflight.jems.server.payments.model.regular.PaymentToProjectTmp
import io.cloudflight.jems.server.payments.model.regular.PaymentType
import io.cloudflight.jems.server.payments.model.regular.contributionMeta.ContributionMeta
import io.cloudflight.jems.server.payments.repository.toDetailModel
import io.cloudflight.jems.server.payments.repository.toEntity
import io.cloudflight.jems.server.payments.repository.toFTLSPaymentEntity
import io.cloudflight.jems.server.payments.repository.toFTLSPaymentModel
import io.cloudflight.jems.server.payments.repository.toListModel
import io.cloudflight.jems.server.payments.repository.toModelList
import io.cloudflight.jems.server.payments.repository.toRegularPaymentEntity
import io.cloudflight.jems.server.payments.repository.toRegularPaymentModel
import io.cloudflight.jems.server.payments.service.regular.PaymentPersistence
import io.cloudflight.jems.server.programme.repository.fund.ProgrammeFundRepository
import io.cloudflight.jems.server.project.entity.report.project.financialOverview.QReportProjectCertificateCoFinancingEntity
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.lumpsum.ProjectLumpSumRepository
import io.cloudflight.jems.server.project.repository.partner.ProjectPartnerRepository
import io.cloudflight.jems.server.project.repository.report.partner.ProjectPartnerReportRepository
import io.cloudflight.jems.server.project.repository.report.project.ProjectReportCoFinancingRepository
import io.cloudflight.jems.server.project.repository.report.project.base.ProjectReportRepository
import io.cloudflight.jems.server.project.service.report.model.partner.financialOverview.coFinancing.ReportExpenditureCoFinancingColumn
import io.cloudflight.jems.server.project.service.report.model.project.financialOverview.coFinancing.PaymentCumulativeAmounts
import io.cloudflight.jems.server.project.service.report.model.project.financialOverview.coFinancing.PaymentCumulativeData
import io.cloudflight.jems.server.user.entity.UserEntity
import io.cloudflight.jems.server.user.repository.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate


@Repository
class PaymentPersistenceProvider(
    private val paymentRepository: PaymentRepository,
    private val paymentPartnerRepository: PaymentPartnerRepository,
    private val paymentPartnerInstallmentRepository: PaymentPartnerInstallmentRepository,
    private val paymentContributionMetaRepository: PaymentContributionMetaRepository,
    private val projectRepository: ProjectRepository,
    private val projectPartnerRepository: ProjectPartnerRepository,
    private val projectLumpSumRepository: ProjectLumpSumRepository,
    private val userRepository: UserRepository,
    private val fundRepository: ProgrammeFundRepository,
    private val projectFileMetadataRepository: JemsFileMetadataRepository,
    private val projectReportCoFinancingRepository: ProjectReportCoFinancingRepository,
    private val projectReportRepository: ProjectReportRepository,
    private val fileRepository: JemsProjectFileService,
    private val projectPartnerReportRepository: ProjectPartnerReportRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : PaymentPersistence {

    @Transactional(readOnly = true)
    override fun existsById(id: Long) =
        paymentRepository.existsById(id)

    @Transactional(readOnly = true)
    override fun getAllPaymentToProject(pageable: Pageable, filters: PaymentSearchRequest): Page<PaymentToProject> {
        return fetchPayments(pageable, filters).map {
            when(it.payment.type) {
                PaymentType.REGULAR -> it.toRegularPaymentModel()
                PaymentType.FTLS -> it.toFTLSPaymentModel()
            }
        }
    }

    private fun fetchPayments(pageable: Pageable, filters: PaymentSearchRequest): Page<PaymentToProjectTmp> {
        val specPayment = QPaymentEntity.paymentEntity
        val specPaymentPartner = QPaymentPartnerEntity.paymentPartnerEntity
        val specPaymentPartnerInstallment = QPaymentPartnerInstallmentEntity.paymentPartnerInstallmentEntity
        val specPartnerReportCertificateCoFin = QReportProjectCertificateCoFinancingEntity.reportProjectCertificateCoFinancingEntity

        val results = jpaQueryFactory
            .select(
                specPayment,
                specPaymentPartnerInstallment.amountPaid(),
                specPaymentPartnerInstallment.amountAuthorized(),
                specPaymentPartnerInstallment.paymentDate.max(),
                specPartnerReportCertificateCoFin.sumCurrentVerified
            )
            .from(specPayment)
            .leftJoin(specPaymentPartner)
                .on(specPaymentPartner.payment.id.eq(specPayment.id))
            .leftJoin(specPaymentPartnerInstallment)
                .on(specPaymentPartnerInstallment.paymentPartner.id.eq(specPaymentPartner.id))
            .leftJoin(specPartnerReportCertificateCoFin)
                .on(specPartnerReportCertificateCoFin.reportEntity.id.eq(specPayment.projectReport.id))
            .where(filters.transformToWhereClause())
            .groupBy(specPayment)
            .having(filters.transformToHavingClause())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(pageable.sort.toQueryDslOrderBy())
            .fetchResults()

        return results.toPageResult(pageable)
    }

    @Transactional(readOnly = true)
    override fun getConfirmedInfosForPayment(paymentId: Long): PaymentConfirmedInfo {
        var amountPaid = BigDecimal.ZERO
        var amountAuthorized = BigDecimal.ZERO
        var lastPaymentDate: LocalDate? = null
        val paymentInstallments = paymentPartnerInstallmentRepository.findAllByPaymentPartnerPaymentId(paymentId)

        paymentInstallments.forEach { installment ->
            if (installment.isPaymentConfirmed == true) {
                amountPaid = amountPaid.add(installment.amountPaid)
                lastPaymentDate =  if (installment.paymentDate != null && (lastPaymentDate == null || installment.paymentDate!!.isAfter(lastPaymentDate)))
                    installment.paymentDate else lastPaymentDate
            }
            amountAuthorized = if (installment.isSavePaymentInfo == true) amountAuthorized.add(installment.amountPaid) else amountAuthorized
        }
        return PaymentConfirmedInfo(
            id = paymentId,
            amountPaidPerFund = amountPaid,
            amountAuthorizedPerFund = amountAuthorized,
            dateOfLastPayment = lastPaymentDate
        )
    }

    @Transactional
    override fun deleteAllByProjectIdAndOrderNrIn(projectId: Long, orderNr: Set<Int>) {
        paymentRepository.deleteAllByProjectIdAndOrderNr(projectId, orderNr)
    }

    @Transactional
    override fun getAmountPerPartnerByProjectIdAndLumpSumOrderNrIn(
        projectId: Long,
        orderNrsToBeAdded: MutableSet<Int>
    ): List<PaymentPerPartner> =
        paymentRepository
            .getAmountPerPartnerByProjectIdAndLumpSumOrderNrIn(projectId, orderNrsToBeAdded)
            .toListModel()

    @Transactional
    override fun saveFTLSPayments(projectId: Long, paymentsToBeSaved: Map<PaymentGroupingId, PaymentToCreate>) {
        val projectEntity = projectRepository.getById(projectId)
        val paymentEntities = paymentRepository.saveAll(paymentsToBeSaved.map { (id, model) ->
            model.toFTLSPaymentEntity(
                projectEntity = projectEntity,
                lumpSum = projectLumpSumRepository.getByIdProjectIdAndIdOrderNr(projectId, id.orderNr),
                fundEntity = fundRepository.getById(id.programmeFundId)
            )
        }).associateBy { PaymentGroupingId(it.projectLumpSum!!.id.orderNr, it.fund.id) }

        paymentEntities.forEach { (paymentId, entity) ->
            paymentPartnerRepository.saveAll(
                paymentsToBeSaved[paymentId]!!.partnerPayments.map {
                    it.toEntity(
                        paymentEntity = entity,
                        partnerEntity = projectPartnerRepository.getById(it.partnerId),
                        partnerReportEntity = null
                    )
                }
            )
        }
    }

    @Transactional
    override fun saveRegularPayments(projectReportId: Long, paymentsToBeSaved: List<PaymentRegularToCreate>) {
        val projectReportEntity = projectReportRepository.getById(projectReportId)
        val projectEntity = projectRepository.getById(projectReportEntity.projectId)
        val fundIdToFundEntity = projectReportCoFinancingRepository.findAllByIdReportIdOrderByIdFundSortNumber(projectReportId)
            .mapNotNull { it.programmeFund }.associateBy { it.id }

        val paymentEntitiesByFundId = paymentRepository.saveAll(
            paymentsToBeSaved.map {
                val fund = fundIdToFundEntity.getOrElse(it.fundId) { throw PaymentFinancingSourceNotFoundException() }
                it.toRegularPaymentEntity(projectEntity, projectReportEntity, fund)
            }
        ).associateBy { it.fund.id }


        val paymentPartnerEntities = paymentsToBeSaved.flatMap { regular ->
            regular.partnerPayments.map {
                PaymentPartnerEntity(
                    payment = paymentEntitiesByFundId.get(regular.fundId)!!,
                    projectPartner = projectPartnerRepository.getById(it.partnerId),
                    partnerCertificate = projectPartnerReportRepository.getById(it.partnerReportId!!),
                    amountApprovedPerPartner = it.amountApprovedPerPartner,
                )
            }
        }
        paymentPartnerRepository.saveAll(paymentPartnerEntities)
    }

    @Transactional(readOnly = true)
    override fun getPaymentDetails(paymentId: Long): PaymentDetail =
        paymentRepository.getById(paymentId).toDetailModel(
             partnerPayments = getAllPartnerPayments(paymentId)
         )

    @Transactional(readOnly = true)
    override fun getAllPartnerPayments(
        paymentId: Long
    ): List<PartnerPayment> =
        paymentPartnerRepository.findAllByPaymentId(paymentId)
            .map {
                it.toDetailModel(
                    partnerEntity = it.projectPartner,
                    installments = findPaymentPartnerInstallments(it.id),
                    partnerReportId = it.partnerCertificate?.id,
                    partnerReportNumber = it.partnerCertificate?.number
                )
            }

    @Transactional(readOnly = true)
    override fun getAllPartnerPaymentsForPartner(partnerId: Long) =
        paymentPartnerRepository.findAllByProjectPartnerId(partnerId).map {
            PartnerPaymentSimple(fundId = it.payment.fund.id, it.amountApprovedPerPartner ?: BigDecimal.ZERO)
        }

    @Transactional(readOnly = true)
    override fun getPaymentPartnerId(paymentId: Long, partnerId: Long): Long =
        paymentPartnerRepository.getIdByPaymentIdAndPartnerId(paymentId, partnerId)

    @Transactional(readOnly = true)
    override fun getPaymentPartnersIdsByPaymentId(paymentId: Long): List<Long> =
        paymentPartnerRepository.findAllByPaymentId(paymentId).map { it.id }


    @Transactional(readOnly = true)
    override fun findPaymentPartnerInstallments(paymentPartnerId: Long): List<PaymentPartnerInstallment> =
        this.paymentPartnerInstallmentRepository.findAllByPaymentPartnerId(paymentPartnerId).toModelList()

    @Transactional(readOnly = true)
    override fun findByPartnerId(partnerId: Long) =
        paymentPartnerInstallmentRepository.findAllByPaymentPartnerProjectPartnerId(partnerId).toModelList()

    @Transactional
    override fun updatePaymentPartnerInstallments(
        paymentPartnerId: Long,
        toDeleteInstallmentIds: Set<Long>,
        paymentPartnerInstallments: List<PaymentPartnerInstallmentUpdate>
    ): List<PaymentPartnerInstallment> {
        paymentPartnerInstallmentRepository.deleteAllByIdInBatch(toDeleteInstallmentIds)

        return paymentPartnerInstallmentRepository.saveAll(
            paymentPartnerInstallments.map {
                it.toEntity(
                    paymentPartner = paymentPartnerRepository.getById(paymentPartnerId),
                    savePaymentInfoUser = getUserOrNull(it.savePaymentInfoUserId),
                    paymentConfirmedUser = getUserOrNull(it.paymentConfirmedUserId)
                )
            }
        ).toModelList()
    }

    @Transactional
    override fun deletePaymentAttachment(fileId: Long) {
        fileRepository.delete(
            projectFileMetadataRepository.findByTypeAndId(PaymentAttachment, fileId) ?: throw ResourceNotFoundException("file")
        )
    }

    @Transactional(readOnly = true)
    override fun getPaymentsByProjectId(projectId: Long): List<PaymentToProject> {
        return paymentRepository.findAllByProjectId(projectId).toListModel(
            getConfirm = { id -> getConfirmedInfosForPayment(id) }
        )
    }

    @Transactional
    override fun storePartnerContributionsWhenReadyForPayment(contributions: Collection<ContributionMeta>) {
        paymentContributionMetaRepository.saveAll(contributions.toEntities())
    }

    @Transactional
    override fun deleteContributionsWhenReadyForPaymentReverted(projectId: Long, orderNrs: Set<Int>) {
        paymentContributionMetaRepository.deleteAll(
            paymentContributionMetaRepository.findByProjectIdAndLumpSumOrderNrIn(projectId, orderNrs)
        )
    }

    @Transactional(readOnly = true)
    override fun getFtlsCumulativeForPartner(partnerId: Long): ReportExpenditureCoFinancingColumn {
        val contribution = paymentContributionMetaRepository.getContributionCumulative(partnerId)
        val funds = paymentPartnerRepository.getPaymentOfTypeCumulativeForPartner(PaymentType.FTLS, partnerId).toMap()
            .plus(Pair(null, contribution.partnerContribution))
        return ReportExpenditureCoFinancingColumn(
            funds = funds,
            partnerContribution = contribution.partnerContribution,
            publicContribution = contribution.publicContribution,
            automaticPublicContribution = contribution.automaticPublicContribution,
            privateContribution = contribution.privateContribution,
            sum = funds.values.sumOf { it },
        )
    }

    @Transactional(readOnly = true)
    override fun getFtlsCumulativeForProject(projectId: Long): PaymentCumulativeData {
        val contribution = paymentContributionMetaRepository.getContributionCumulativePerProject(projectId)
        val funds = paymentPartnerRepository.getPaymentOfTypeCumulativeForProject(PaymentType.FTLS, projectId).toMap()

        val cumulativeAmounts = PaymentCumulativeAmounts(
            funds = funds,
            partnerContribution = contribution.partnerContribution,
            publicContribution = contribution.publicContribution,
            automaticPublicContribution = contribution.automaticPublicContribution,
            privateContribution = contribution.privateContribution,
        )

        return PaymentCumulativeData(
            amounts = cumulativeAmounts,
            confirmedAndPaid = paymentPartnerInstallmentRepository.getConfirmedCumulativeForProject(projectId).toMap(),
        )
    }

    private fun getUserOrNull(userId: Long?): UserEntity? =
        if (userId != null) {
            userRepository.getById(userId)
        } else null

    private fun QPaymentPartnerInstallmentEntity.amountPaid() =
        CaseBuilder().`when`(this.isPaymentConfirmed.isTrue).then(this.amountPaid).otherwise(BigDecimal.ZERO).sum()

    private fun QPaymentPartnerInstallmentEntity.amountAuthorized() =
        CaseBuilder().`when`(this.isSavePaymentInfo.isTrue).then(this.amountPaid).otherwise(BigDecimal.ZERO).sum()
}
