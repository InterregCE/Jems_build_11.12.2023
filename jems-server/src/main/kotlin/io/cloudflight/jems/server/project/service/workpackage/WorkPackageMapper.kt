package io.cloudflight.jems.server.project.service.workpackage

import io.cloudflight.jems.api.project.dto.workpackage.InputWorkPackageCreate
import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackage
import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackageSimple
import io.cloudflight.jems.server.project.entity.Project
import io.cloudflight.jems.server.project.entity.WorkPackage

fun WorkPackage.toOutputWorkPackageSimple() = OutputWorkPackageSimple (
    id = id!!,
    number = number,
    name = name
)

fun WorkPackage.toOutputWorkPackage() = OutputWorkPackage (
    id = id!!,
    number = number,
    name = name,
    specificObjective = specificObjective,
    objectiveAndAudience = objectiveAndAudience
)

fun InputWorkPackageCreate.toEntity(project: Project) = WorkPackage (
    id = null,
    name = name,
    project = project,
    specificObjective = specificObjective,
    objectiveAndAudience = objectiveAndAudience
)