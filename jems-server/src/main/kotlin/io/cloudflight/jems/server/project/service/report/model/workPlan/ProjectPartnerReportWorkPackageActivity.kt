package io.cloudflight.jems.server.project.service.report.model.workPlan

import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.project.service.report.model.file.ProjectReportFileMetadata

data class ProjectPartnerReportWorkPackageActivity(
    val id: Long,
    val number: Int,
    val title: Set<InputTranslation>,

    val progress: Set<InputTranslation>,
    val attachment: ProjectReportFileMetadata?,

    val deliverables: List<ProjectPartnerReportWorkPackageActivityDeliverable>,
)
