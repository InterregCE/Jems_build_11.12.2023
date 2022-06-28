package io.cloudflight.jems.server.project.service.lumpsum.model


const val PREPARATION_PERIOD_NUMBER = 0
const val CLOSURE_PERIOD_NUMBER = 255

data class ProjectLumpSum (
    val programmeLumpSumId: Long,
    val period: Int? = null,
    val lumpSumContributions: List<ProjectPartnerLumpSum> = emptyList(),
    val isFastTrack: Boolean? = false
)
