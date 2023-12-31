package io.cloudflight.jems.server.project.repository.report.partner.workPlan

import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.project.entity.report.partner.ProjectPartnerReportEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageActivityDeliverableEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageActivityDeliverableTranslEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageActivityEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageActivityTranslEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageOutputEntity
import io.cloudflight.jems.server.project.entity.report.partner.workPlan.ProjectPartnerReportWorkPackageOutputTranslEntity
import io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create.CreateProjectPartnerReportWorkPackage
import io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create.CreateProjectPartnerReportWorkPackageActivity
import io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create.CreateProjectPartnerReportWorkPackageActivityDeliverable
import io.cloudflight.jems.server.project.service.report.model.partner.workPlan.create.CreateProjectPartnerReportWorkPackageOutput

fun CreateProjectPartnerReportWorkPackage.toEntity(report: ProjectPartnerReportEntity) =
    ProjectPartnerReportWorkPackageEntity(
        reportEntity = report,
        number = number,
        workPackageId = workPackageId,
        deactivated = deactivated
    )

fun CreateProjectPartnerReportWorkPackageActivity.toEntity(wp: ProjectPartnerReportWorkPackageEntity) =
    ProjectPartnerReportWorkPackageActivityEntity(
        workPackageEntity = wp,
        number = number,
        activityId = activityId,
        attachment = null,
        translatedValues = mutableSetOf(),
        deactivated = deactivated
    ).apply {
        translatedValues.addAll(
            title.filter { !it.translation.isNullOrBlank() }
                .mapTo(HashSet()) {
                    ProjectPartnerReportWorkPackageActivityTranslEntity(
                        TranslationId(this, it.language),
                        title = it.translation,
                        description = null,
                    )
                }
        )
    }

fun List<CreateProjectPartnerReportWorkPackageOutput>.toEntity(wp: ProjectPartnerReportWorkPackageEntity) =
    map {
        ProjectPartnerReportWorkPackageOutputEntity(
            workPackageEntity = wp,
            number = it.number,
            contribution = null,
            evidence = null,
            attachment = null,
            translatedValues = mutableSetOf(),
            deactivated = it.deactivated,
        ).apply {
            translatedValues.addAll(
                it.title.filter { !it.translation.isNullOrBlank() }
                    .mapTo(HashSet()) {
                        ProjectPartnerReportWorkPackageOutputTranslEntity(
                            TranslationId(this, it.language),
                            title = it.translation,
                        )
                    }
            )
        }
    }

fun List<CreateProjectPartnerReportWorkPackageActivityDeliverable>.toEntity(
    activity: ProjectPartnerReportWorkPackageActivityEntity,
) = map {
    ProjectPartnerReportWorkPackageActivityDeliverableEntity(
        activityEntity = activity,
        number = it.number,
        deliverableId = it.deliverableId,
        contribution = null,
        evidence = null,
        attachment = null,
        translatedValues = mutableSetOf(),
        deactivated = it.deactivated
    ).apply {
        translatedValues.addAll(
            it.title.filter { !it.translation.isNullOrBlank() }
                .mapTo(HashSet()) {
                    ProjectPartnerReportWorkPackageActivityDeliverableTranslEntity(
                        TranslationId(this, it.language),
                        title = it.translation,
                    )
                }
        )
    }
}
