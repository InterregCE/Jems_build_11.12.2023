package io.cloudflight.jems.api.call.dto

data class PreSubmissionPluginsDTO(
    val pluginKey: String,
    val firstStepPluginKey: String?,
    val reportPartnerCheckPluginKey: String,
    val reportProjectCheckPluginKey: String,
    val controlReportPartnerCheckPluginKey: String,
    val controlReportSamplingCheckPluginKey: String,
)
