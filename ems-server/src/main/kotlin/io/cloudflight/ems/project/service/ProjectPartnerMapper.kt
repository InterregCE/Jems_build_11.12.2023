package io.cloudflight.ems.project.service

import io.cloudflight.ems.api.project.dto.InputProjectPartner
import io.cloudflight.ems.api.project.dto.OutputProjectPartner
import io.cloudflight.ems.project.entity.Project
import io.cloudflight.ems.project.entity.ProjectPartner

fun InputProjectPartner.toEntity(project: Project) = ProjectPartner(
    name = name!!,
    project = project,
    role = role!!
)

fun ProjectPartner.toOutputProjectPartner() = OutputProjectPartner(
    id = id,
    name = name,
    role = role,
    sortNumber = sortNumber
)
