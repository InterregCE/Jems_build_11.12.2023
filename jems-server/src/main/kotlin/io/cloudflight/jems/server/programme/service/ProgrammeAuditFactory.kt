package io.cloudflight.jems.server.programme.service

import io.cloudflight.jems.api.programme.dto.OutputProgrammePriority
import io.cloudflight.jems.server.audit.entity.AuditAction
import io.cloudflight.jems.server.audit.service.AuditBuilder
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.nuts.service.NutsIdentifier
import io.cloudflight.jems.server.programme.entity.ProgrammeFund
import io.cloudflight.jems.server.programme.entity.ProgrammeLegalStatus
import java.util.stream.Collectors

fun programmePriorityAdded(programmePriority: OutputProgrammePriority): AuditCandidate {
    return AuditBuilder(AuditAction.PROGRAMME_PRIORITY_ADDED)
        .description("New programme priority '${programmePriority.code}' '${programmePriority.title}' was created")
        .build()
}

fun programmeBasicDataChanged(changes: Map<String, Pair<Any?, Any?>>): AuditCandidate {
    val changedString = changes.entries.stream()
        .map { "${it.key} changed from ${it.value.first} to ${it.value.second}" }
        .collect(Collectors.joining(",\n"))

    return AuditBuilder(AuditAction.PROGRAMME_BASIC_DATA_EDITED)
        .description("Programme basic data changed:\n$changedString")
        .build()
}

fun programmeNutsAreaChanged(updatedNuts: Collection<NutsIdentifier>): AuditCandidate {
    val changes = updatedNuts.stream()
        .map { it.toString() }
        .collect(Collectors.joining(",\n"))

    return AuditBuilder(AuditAction.PROGRAMME_NUTS_AREA_CHANGED)
        .description("The programme area has been set to:\n$changes")
        .build()
}

fun programmeFundsChanged(funds: Iterable<ProgrammeFund>): AuditCandidate {
    val fundsAsString = funds.asSequence()
        .filter { it.selected }
        .map { it.abbreviation }.joinToString(",\n")

    return AuditBuilder(AuditAction.PROGRAMME_FUNDS_CHANGED)
        .description("Programme funds has been set to:\n$fundsAsString")
        .build()
}

fun programmeLegalStatusesChanged(statuses: Iterable<ProgrammeLegalStatus>): AuditCandidate {
    val statusesAsString = statuses.asSequence()
        .map { it.description }.joinToString(",\n")

    return AuditBuilder(AuditAction.LEGAL_STATUS_EDITED)
        .description("Values for partner legal status set to:\n$statusesAsString")
        .build()
}