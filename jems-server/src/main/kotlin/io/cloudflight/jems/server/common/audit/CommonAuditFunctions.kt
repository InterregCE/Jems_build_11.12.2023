package io.cloudflight.jems.server.common.audit

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

fun Map<String, Pair<Any?, Any?>>.onlyNewChanges() = entries.stream()
    .map { "${it.key} set to ${it.changeTo()}" }
    .collect(Collectors.joining(",\n"))

fun Map<String, Pair<Any?, Any?>>.fromOldToNewChanges(): String {
    if (isEmpty())
        return "(no-change)"

    return entries.stream()
        .map { "${it.key} changed from ${it.changeFrom()} to ${it.changeTo()}" }
        .collect(Collectors.joining(",\n"))
}

private fun Map.Entry<String, Pair<Any?, Any?>>.changeFrom() = value.first?.toAuditString()
private fun Map.Entry<String, Pair<Any?, Any?>>.changeTo() = value.second?.toAuditString()

private fun Any.toAuditString(): String {
    return when (this) {

        is Collection<*> -> if (isEmpty()) "[]" else if(first() is Int || first() is Long) toString() else joinToString(
            separator = "\n  ",
            prefix = "[\n  ",
            transform = { it.toString() }
        ) + "\n]"

        is String -> "'${this}'"

        is Boolean -> if (this) "enabled" else "disabled"

        is ZonedDateTime -> this.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)

        else -> toString()
    }
}
