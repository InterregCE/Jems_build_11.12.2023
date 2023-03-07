package io.cloudflight.jems.server.project.service.report.model.project.workPlan

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.programme.service.indicator.model.OutputIndicatorSummary
import io.cloudflight.jems.server.project.service.model.ProjectPeriod
import io.cloudflight.jems.server.project.service.report.model.file.JemsFileMetadata
import java.math.BigDecimal

data class ProjectReportWorkPackageOutput(
    val id: Long,
    val number: Int,
    val title: Set<InputTranslation>,
    val deactivated: Boolean,

    val outputIndicator: OutputIndicatorSummary?,
    val period: ProjectPeriod?,
    val targetValue: BigDecimal,
    val currentReport: BigDecimal,
    val previouslyReported: BigDecimal,
    val progress: Set<InputTranslation>,
    val attachment: JemsFileMetadata?,
)
