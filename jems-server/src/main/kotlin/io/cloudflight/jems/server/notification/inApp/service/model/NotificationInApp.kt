package io.cloudflight.jems.server.notification.inApp.service.model

import java.time.ZonedDateTime

data class NotificationInApp(
    val subject: String,
    val body: String,
    val type: NotificationType,
    val time: ZonedDateTime,
    val templateVariables: MutableMap<String, Any>,
    val recipientsInApp: Set<String>,

    val recipientsEmail: Set<String>,
    val emailTemplate: String?,
)
