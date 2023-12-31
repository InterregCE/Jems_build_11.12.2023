package io.cloudflight.jems.server.project.service.application.workflow

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.controllerInstitution.service.ControllerInstitutionPersistence
import io.cloudflight.jems.server.project.authorization.ProjectAuthorization
import io.cloudflight.jems.server.project.service.ProjectAssessmentPersistence
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectVersionPersistence
import io.cloudflight.jems.server.project.service.ProjectWorkflowPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.application.workflow.states.ApprovedApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.ApprovedApplicationWithConditionsState
import io.cloudflight.jems.server.project.service.application.workflow.states.ConditionsSubmittedApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.DraftApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.EligibleApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.InEligibleApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.NotApprovedApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.ReturnedToApplicantApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.ReturnedToApplicantForConditionsApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.SubmittedApplicationState
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.context.ApplicationEventPublisher

class ApplicationStateFactoryTest : UnitTest() {

    companion object {
        private const val PROJECT_ID = 1L

        private fun summary(status: ApplicationStatus) = ProjectSummary(
            id = PROJECT_ID,
            customIdentifier = "01",
            callId = 1L,
            callName = "",
            acronym = "project acronym",
            status = status
        )

    }

    @RelaxedMockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @RelaxedMockK
    lateinit var securityService: SecurityService

    @RelaxedMockK
    lateinit var projectPersistence: ProjectPersistence

    @RelaxedMockK
    lateinit var projectWorkflowPersistence: ProjectWorkflowPersistence

    @RelaxedMockK
    lateinit var projectAuthorization: ProjectAuthorization

    @RelaxedMockK
    lateinit var projectAssessmentPersistence: ProjectAssessmentPersistence

    @RelaxedMockK
    lateinit var projectVersionPersistence: ProjectVersionPersistence

    @MockK
    lateinit var controllerInstitutionPersistence: ControllerInstitutionPersistence

    @InjectMockKs
    private lateinit var applicationStateFactory: ApplicationStateFactory

    @ParameterizedTest
    @MethodSource("provideSummaryWithExpectedInstance")
    fun getInstance(summary: ProjectSummary, type: Class<Any>) {
        assertThat(applicationStateFactory.getInstance(summary))
            .isInstanceOf(type)
    }

    private fun provideSummaryWithExpectedInstance(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(summary(ApplicationStatus.DRAFT), DraftApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.SUBMITTED), SubmittedApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.CONDITIONS_SUBMITTED), ConditionsSubmittedApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.RETURNED_TO_APPLICANT), ReturnedToApplicantApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.RETURNED_TO_APPLICANT_FOR_CONDITIONS), ReturnedToApplicantForConditionsApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.ELIGIBLE), EligibleApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.INELIGIBLE), InEligibleApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.APPROVED), ApprovedApplicationState::class.java),
            Arguments.of(summary(ApplicationStatus.APPROVED_WITH_CONDITIONS), ApprovedApplicationWithConditionsState::class.java),
            Arguments.of(summary(ApplicationStatus.NOT_APPROVED), NotApprovedApplicationState::class.java),
        )
    }
}
