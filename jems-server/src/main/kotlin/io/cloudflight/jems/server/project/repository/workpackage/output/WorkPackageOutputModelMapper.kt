package io.cloudflight.jems.server.project.repository.workpackage.output

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.common.entity.addTranslationEntities
import io.cloudflight.jems.server.common.entity.extractField
import io.cloudflight.jems.server.common.entity.extractTranslation
import io.cloudflight.jems.server.programme.entity.indicator.OutputIndicatorEntity
import io.cloudflight.jems.server.programme.entity.indicator.OutputIndicatorTranslEntity
import io.cloudflight.jems.server.programme.entity.indicator.ResultIndicatorTranslEntity
import io.cloudflight.jems.server.project.entity.ProjectPeriodEntity
import io.cloudflight.jems.server.project.entity.workpackage.output.OutputRowWithTranslations
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputTranslationId
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputEntity
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputId
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputTransl
import io.cloudflight.jems.server.project.service.model.ProjectPeriod
import io.cloudflight.jems.server.project.service.result.model.OutputRow
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutputTranslatedValue
import kotlin.collections.HashSet

fun WorkPackageOutput.toEntity(
    workPackageId: Long,
    index: Int,
    resolveProgrammeIndicatorEntity: (Long?) -> OutputIndicatorEntity?,
) : WorkPackageOutputEntity {
    val outputId = WorkPackageOutputId(workPackageId, index)
    return WorkPackageOutputEntity(
        outputId = outputId,
        translatedValues = mutableSetOf(),
        periodNumber = periodNumber,
        programmeOutputIndicatorEntity = resolveProgrammeIndicatorEntity.invoke(programmeOutputIndicatorId),
        targetValue = targetValue
    ).apply {
        translatedValues.addTranslationEntities(
            { language ->
                WorkPackageOutputTransl(
                    translationId = WorkPackageOutputTranslationId(this, language),
                    title = title.extractTranslation(language),
                    description = description.extractTranslation(language),
                )
            }, arrayOf(title, description)
        )
    }
}

fun List<WorkPackageOutput>.toIndexedEntity(
    workPackageId: Long,
    resolveProgrammeIndicatorEntity: (Long?) -> OutputIndicatorEntity?,
) = mapIndexed { index, output -> output.toEntity(workPackageId, index.plus(1), resolveProgrammeIndicatorEntity) }

fun Iterable<WorkPackageOutputEntity>.toModel(periods: Collection<ProjectPeriodEntity> = emptySet()) = map {
    WorkPackageOutput(
        workPackageId = it.outputId.workPackageId,
        outputNumber = it.outputId.outputNumber,
        programmeOutputIndicatorId = it.programmeOutputIndicatorEntity?.id,
        programmeOutputIndicatorIdentifier = it.programmeOutputIndicatorEntity?.identifier,
        programmeOutputIndicatorName = it.programmeOutputIndicatorEntity?.translatedValues?.extractIndicatorField { it.name } ?: emptySet(),
        programmeOutputIndicatorMeasurementUnit = it.programmeOutputIndicatorEntity?.translatedValues?.extractIndicatorField { it.measurementUnit } ?: emptySet(),
        targetValue = it.targetValue,
        periodNumber = it.periodNumber,
        periodStartMonth = periods.find { period -> period.id.number == it.periodNumber }?.start,
        periodEndMonth = periods.find { period -> period.id.number == it.periodNumber }?.end,
        title = it.translatedValues.extractField { it.title },
        description = it.translatedValues.extractField { it.description },
    )
}.sortedBy { it.outputNumber }

fun Set<WorkPackageOutputTransl>.toModel() = mapTo(HashSet()) {
    WorkPackageOutputTranslatedValue(
        language = it.translationId.language,
        description = it.description,
        title = it.title
    )
}

fun List<OutputRowWithTranslations>.toModel() = this
    .groupBy { Pair(it.workPackageId, it.number) }
    .map { groupedRows ->
        OutputRow(
            workPackageId = groupedRows.value.first().workPackageId,
            workPackageNumber = groupedRows.value.first().workPackageNumber,
            outputTitle = groupedRows.value.extractField { it.title },
            outputNumber = groupedRows.value.first().number,
            outputTargetValue = groupedRows.value.first().targetValue,
            programmeOutputId = groupedRows.value.first().programmeOutputId,
            programmeResultId = groupedRows.value.first().programmeResultId,
        )
    }

fun Set<OutputIndicatorTranslEntity>.extractIndicatorField(extractFunction: (OutputIndicatorTranslEntity) -> String?) =
    map { InputTranslation(it.translationId.language, extractFunction.invoke(it)) }
        .filterTo(HashSet()) { !it.translation.isNullOrBlank() }
