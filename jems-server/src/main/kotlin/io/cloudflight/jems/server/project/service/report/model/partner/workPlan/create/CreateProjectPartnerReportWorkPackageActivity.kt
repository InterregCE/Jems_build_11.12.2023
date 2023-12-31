package io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create

import io.cloudflight.jems.api.project.dto.InputTranslation

data class CreateProjectPartnerReportWorkPackageActivity(
    val activityId: Long?,
    val number: Int,
    val title: Set<InputTranslation>,
    val deactivated: Boolean,
    val startPeriodNumber: Int?,
    val endPeriodNumber: Int?,

    val deliverables: List<CreateProjectPartnerReportWorkPackageActivityDeliverable>,
)
