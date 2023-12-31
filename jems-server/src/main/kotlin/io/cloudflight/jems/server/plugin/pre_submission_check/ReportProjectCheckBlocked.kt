package io.cloudflight.jems.server.plugin.pre_submission_check

import io.cloudflight.jems.plugin.contract.models.common.I18nMessageData
import io.cloudflight.jems.plugin.contract.pre_condition_check.ReportProjectCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import org.springframework.stereotype.Component


@Component
class ReportProjectCheckBlocked: ReportProjectCheckPlugin {


    companion object{
        const val KEY = "report-project-check-blocked"
    }

    override fun check(projectId: Long, reportId: Long): PreConditionCheckResult =
        PreConditionCheckResult(listOf(
            PreConditionCheckMessage(I18nMessageData("jems.report.project.check.blocked.warning"), MessageType.WARNING, emptyList())
        ) , false)

    override fun getDescription() = "It will always return false as a result of check"

    override fun getName() = "Blocked"

    override fun getVersion() = ""

    override fun getKey() = KEY
}
