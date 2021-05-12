package io.cloudflight.jems.server.project.service.application.submit_application

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.plugin.contract.pre_condition_check.PreConditionCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.audit.model.AuditProject
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.call.repository.CallNotFound
import io.cloudflight.jems.server.plugin.JemsPluginRegistry
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectWorkflowPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.application.workflow.ApplicationStateFactory
import io.cloudflight.jems.server.project.service.application.workflow.states.DraftApplicationState
import io.cloudflight.jems.server.project.service.create_new_project_version.CreateNewProjectVersionInteractor
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher

class SubmitApplicationInteractorTest : UnitTest() {

    companion object {
        private const val PROJECT_ID = 1L
        private val summary = ProjectSummary(
            id = PROJECT_ID,
            callName = "",
            acronym = "project acronym",
            status = ApplicationStatus.DRAFT
        )

    }

    @MockK
    lateinit var projectPersistence: ProjectPersistence

    @MockK
    lateinit var projectWorkflowPersistence: ProjectWorkflowPersistence

    @MockK
    lateinit var applicationStateFactory: ApplicationStateFactory

    @MockK
    lateinit var preConditionCheckPlugin: PreConditionCheckPlugin

    @MockK
    lateinit var jemsPluginRegistry: JemsPluginRegistry

    @RelaxedMockK
    lateinit var createNewProjectVersionInteractor: CreateNewProjectVersionInteractor

    @RelaxedMockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var submitApplication: SubmitApplication

    @MockK
    lateinit var draftState: DraftApplicationState


    @Test
    fun submit() {
        every { jemsPluginRegistry.get(PreConditionCheckPlugin::class, "standard-pre-condition-check-plugin") } returns preConditionCheckPlugin
        every { preConditionCheckPlugin.check(PROJECT_ID) } returns PreConditionCheckResult(emptyList(), true)
        every { projectPersistence.getProjectSummary(PROJECT_ID) } returns summary
        every { applicationStateFactory.getInstance(any()) } returns draftState
        every { draftState.submit() } returns ApplicationStatus.SUBMITTED
        every {
            projectWorkflowPersistence.getLatestApplicationStatusNotEqualTo(
                PROJECT_ID,
                ApplicationStatus.RETURNED_TO_APPLICANT
            )
        } returns ApplicationStatus.SUBMITTED


        assertThat(submitApplication.submit(PROJECT_ID)).isEqualTo(ApplicationStatus.SUBMITTED)

        val slotAudit = slot<AuditCandidateEvent>()
        verify(exactly = 1) { auditPublisher.publishEvent(capture(slotAudit)) }
        assertThat(slotAudit.captured.auditCandidate).isEqualTo(
            AuditCandidate(
                action = AuditAction.APPLICATION_STATUS_CHANGED,
                project = AuditProject(id = PROJECT_ID.toString(), name = "project acronym"),
                description = "Project application status changed from DRAFT to SUBMITTED"
            )
        )
    }

    @Test
    fun `should throw exception when pre condition check fails`() {
        every { jemsPluginRegistry.get(PreConditionCheckPlugin::class, "standard-pre-condition-check-plugin") } returns preConditionCheckPlugin
        every { preConditionCheckPlugin.check(PROJECT_ID) } returns PreConditionCheckResult(emptyList(), false)

        assertThrows<SubmitApplicationPreConditionCheckFailedException> {submitApplication.submit(PROJECT_ID) }
    }
}
