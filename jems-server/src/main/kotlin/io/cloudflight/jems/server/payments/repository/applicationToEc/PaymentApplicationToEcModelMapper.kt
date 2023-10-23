package io.cloudflight.jems.server.payments.repository.applicationToEc

import io.cloudflight.jems.server.payments.accountingYears.repository.toEntity
import io.cloudflight.jems.server.payments.accountingYears.repository.toModel
import io.cloudflight.jems.server.payments.entity.PaymentApplicationToEcEntity
import io.cloudflight.jems.server.payments.entity.PaymentToEcCumulativeAmountsEntity
import io.cloudflight.jems.server.payments.entity.PaymentToEcExtensionEntity
import io.cloudflight.jems.server.payments.model.ec.PaymentApplicationToEc
import io.cloudflight.jems.server.payments.model.ec.PaymentApplicationToEcDetail
import io.cloudflight.jems.server.payments.model.ec.PaymentApplicationToEcSummary
import io.cloudflight.jems.server.payments.model.ec.PaymentToEcAmountSummaryLine
import io.cloudflight.jems.server.payments.model.ec.PaymentToEcExtension
import io.cloudflight.jems.server.payments.model.regular.PaymentSearchRequestScoBasis
import io.cloudflight.jems.server.programme.entity.ProgrammePriorityEntity
import io.cloudflight.jems.server.programme.entity.fund.ProgrammeFundEntity
import io.cloudflight.jems.server.programme.repository.fund.toEntity
import io.cloudflight.jems.server.programme.repository.fund.toModel
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.Named
import org.mapstruct.factory.Mappers
import org.springframework.data.domain.Page


private val mapper = Mappers.getMapper(PaymentApplicationToEcModelMapper::class.java)

fun PaymentApplicationToEcEntity.toModel(): PaymentApplicationToEc = mapper.map(this)
fun PaymentApplicationToEc.toEntity(): PaymentApplicationToEcEntity = mapper.map(this)
fun Page<PaymentApplicationToEcEntity>.toModel() = map { it.toModel() }

fun PaymentToEcExtensionEntity.toModel() = PaymentToEcExtension(
    paymentId = paymentId,
    ecPaymentId = paymentApplicationToEc?.id,
    ecPaymentStatus = paymentApplicationToEc?.status,
)

fun PaymentApplicationToEcEntity.toDetailModel() = PaymentApplicationToEcDetail(
    id = id,
    status = status,
    paymentApplicationToEcSummary = PaymentApplicationToEcSummary(
        programmeFund = programmeFund.toModel(),
        accountingYear = accountingYear.toModel(),
        nationalReference = nationalReference,
        technicalAssistanceEur = technicalAssistanceEur,
        submissionToSfcDate = submissionToSfcDate,
        sfcNumber = sfcNumber,
        comment = comment
    )
)

fun PaymentApplicationToEcDetail.toEntity() = PaymentApplicationToEcEntity(
    id = id,
    status = status,
    programmeFund = paymentApplicationToEcSummary.programmeFund.toEntity(),
    accountingYear = paymentApplicationToEcSummary.accountingYear.toEntity(),
    nationalReference = paymentApplicationToEcSummary.nationalReference,
    sfcNumber = paymentApplicationToEcSummary.sfcNumber,
    comment = paymentApplicationToEcSummary.comment,
    submissionToSfcDate = paymentApplicationToEcSummary.submissionToSfcDate,
    technicalAssistanceEur = paymentApplicationToEcSummary.technicalAssistanceEur
)

fun PaymentToEcAmountSummaryLine.toEntity(
    paymentToEc: PaymentApplicationToEcEntity,
    programmePriority: ProgrammePriorityEntity,
    type: PaymentSearchRequestScoBasis
) =
    PaymentToEcCumulativeAmountsEntity(
        paymentApplicationToEc = paymentToEc,
        priorityAxis = programmePriority,
        type = type,
        totalEligibleExpenditure = totalEligibleExpenditure,
        totalUnionContribution = totalUnionContribution,
        totalPublicContribution = totalPublicContribution
    )

fun List<PaymentToEcCumulativeAmountsEntity>.toModel() = map { mapper.map(it) }

@Mapper
interface PaymentApplicationToEcModelMapper {
    @Named("toFundModel")
    fun toFundModel(entity: ProgrammeFundEntity) = entity.toModel()

    @Mapping(source = "programmeFund", target = "programmeFund", qualifiedByName = ["toFundModel"])
    fun map(entity: PaymentApplicationToEcEntity): PaymentApplicationToEc
    fun map(model: PaymentApplicationToEc): PaymentApplicationToEcEntity

    @Mappings(
        Mapping(source = "priorityAxis.code", target = "priorityAxis"),
    )
    fun map(entity: PaymentToEcCumulativeAmountsEntity): PaymentToEcAmountSummaryLine
}