package io.cloudflight.jems.server.project.service.report.model.project.workPlan.create

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.project.service.report.model.project.workPlan.ProjectReportWorkPlanStatus
import java.math.BigDecimal

data class ProjectReportWorkPackageActivityDeliverableCreate(
    val deliverableId: Long,
    val number: Int,
    val title: Set<InputTranslation>,
    val deactivated: Boolean,

    val periodNumber: Int?,
    val previouslyReported: BigDecimal,
    val previousCurrentReport: BigDecimal,
    val currentReport: BigDecimal,

    val progress: Set<InputTranslation>,
    val previousProgress: Set<InputTranslation>,
)
