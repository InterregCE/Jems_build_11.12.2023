package io.cloudflight.jems.server.project.entity

import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.user.service.model.UserStatus
import java.sql.Timestamp

interface ProjectVersionRow {
    val version: String
    val projectId: Long
    val rowEnd: Timestamp?
    val createdAt: Timestamp
    val status: ApplicationStatus

    //// role
    val roleId: Long
    var roleName: String

}
