package io.cloudflight.jems.server.project.service.report.model.partner.expenditure

data class ExpenditureParkingMetadata(
    val reportOfOriginId: Long,
    val reportOfOriginNumber: Int,
    val reportProjectOfOriginId: Long?,
    val originalExpenditureNumber: Int
)
