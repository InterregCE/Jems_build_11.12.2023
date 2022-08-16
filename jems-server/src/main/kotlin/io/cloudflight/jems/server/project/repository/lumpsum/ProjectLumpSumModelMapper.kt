package io.cloudflight.jems.server.project.repository.lumpsum

import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeLumpSumEntity
import io.cloudflight.jems.server.project.entity.lumpsum.*
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectLumpSum
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectPartnerLumpSum
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun List<ProjectLumpSumEntity>.toModel() = sortedBy { it.id.orderNr }.map {
    ProjectLumpSum(
        period = it.endPeriod,
        programmeLumpSumId = it.programmeLumpSum.id,
        lumpSumContributions = it.lumpSumContributions.toModel(),
        isFastTrack = it.programmeLumpSum.isFastTrack,
        readyForPayment = it.isReadyForPayment,
        comment = it.comment,
        lastApprovedVersionBeforeReadyForPayment = it.lastApprovedVersionBeforeReadyForPayment,
        paymentEnabledDate = it.paymentEnabledDate
    )
}

//fun ProjectLumpSumEntity.toModel() = ProjectLumpSum(
//    period = endPeriod,
//    programmeLumpSumId = programmeLumpSum.id,
//    lumpSumContributions = lumpSumContributions.toModel(),
//    isFastTrack = programmeLumpSum.isFastTrack,
//    readyForPayment = isReadyForPayment,
//    comment = comment,
//    lastApprovedProjectVersion = lastApprovedProjectVersion,
//    paymentEnabledDate = paymentEnabledDate
//)

fun Iterable<ProjectPartnerLumpSumEntity>.toModel() = sortedBy { it.id.projectPartner.sortNumber }
    .map {
        ProjectPartnerLumpSum(
            partnerId = it.id.projectPartner.id,
            amount = it.amount,
        )
    }

fun ProjectLumpSum.toEntity(
    projectLumpSumId: ProjectLumpSumId,
    getProgrammeLumpSum: (Long) -> ProgrammeLumpSumEntity,
    getProjectPartner: (Long) -> ProjectPartnerEntity,
): ProjectLumpSumEntity {
    return ProjectLumpSumEntity(
        id = projectLumpSumId,
        programmeLumpSum = getProgrammeLumpSum.invoke(this.programmeLumpSumId),
        endPeriod = period,
        lumpSumContributions = lumpSumContributions.toPartnerLumpSumEntity(projectLumpSumId, getProjectPartner),
        isReadyForPayment = readyForPayment,
        comment = comment,
        lastApprovedVersionBeforeReadyForPayment = lastApprovedVersionBeforeReadyForPayment,
        paymentEnabledDate = paymentEnabledDate
    )
}

fun List<ProjectLumpSum>.toEntity(
    projectId: Long,
    getProgrammeLumpSum: (Long) -> ProgrammeLumpSumEntity,
    getProjectPartner: (Long) -> ProjectPartnerEntity,
) = mapIndexed { index, model ->
    model.toEntity(ProjectLumpSumId(projectId, index.plus(1)), getProgrammeLumpSum, getProjectPartner)
}

fun List<ProjectPartnerLumpSum>.toPartnerLumpSumEntity(
    projectLumpSumId: ProjectLumpSumId,
    getProjectPartner: (Long) -> ProjectPartnerEntity
) = mapTo(HashSet()) {
    ProjectPartnerLumpSumEntity(
        id = ProjectPartnerLumpSumId(
            projectLumpSumId = projectLumpSumId,
            projectPartner = getProjectPartner.invoke(it.partnerId),
        ),
        amount = it.amount,
    )
}

fun List<ProjectLumpSumRow>.toProjectLumpSumHistoricalData() =
    this.groupBy { it.orderNr }.map { groupedRows ->
        ProjectLumpSum(
            programmeLumpSumId = groupedRows.value.first().programmeLumpSumId,
            period = groupedRows.value.first().endPeriod,
            lumpSumContributions = groupedRows.value
                .filter { it.projectPartnerId != null }
                .map {
                    ProjectPartnerLumpSum(
                        partnerId = it.projectPartnerId!!,
                        amount = it.amount
                    )
                }.toList(),
            isFastTrack = groupedRows.value.first().fastTrack != 0,
            readyForPayment = groupedRows.value.first().readyForPayment != 0,
            comment = groupedRows.value.first().comment,
            paymentEnabledDate = if(groupedRows.value.first().paymentEnabledDate != null) { ZonedDateTime.of(groupedRows.value.first().paymentEnabledDate!!.toLocalDateTime(), ZoneOffset.UTC)} else null,
            lastApprovedVersionBeforeReadyForPayment = groupedRows.value.first().lastApprovedVersionBeforeReadyForPayment
        )
    }
