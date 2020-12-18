package io.cloudflight.jems.server.project.entity.workpackage.activity

import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableEntity
import javax.persistence.CascadeType
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity(name = "project_work_package_activity")
data class WorkPackageActivityEntity(

    @EmbeddedId
    val activityId: WorkPackageActivityId,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "translationId.activityId")
    val translatedValues: Set<WorkPackageActivityTranslationEntity> = emptySet(),

    val startPeriod: Int? = null,

    val endPeriod: Int? = null,

    @OneToMany(mappedBy = "deliverableId.activityId", cascade = [CascadeType.ALL], orphanRemoval = true)
    val deliverables: List<WorkPackageActivityDeliverableEntity> = emptyList(),
)