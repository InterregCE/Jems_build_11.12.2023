package io.cloudflight.jems.server.project.service.application.approve_application

import io.cloudflight.jems.api.project.dto.assessment.ProjectAssessmentQualityResult.RECOMMENDED_FOR_FUNDING
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.controllerInstitution.service.ControllerInstitutionPersistence
import io.cloudflight.jems.server.controllerInstitution.service.model.ProjectPartnerAssignmentMetadata
import io.cloudflight.jems.server.notification.handler.ProjectStatusChangeEvent
import io.cloudflight.jems.server.project.authorization.ProjectAuthorization
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.ProjectVersionPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationActionInfo
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.APPROVED
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.ELIGIBLE
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.STEP1_APPROVED
import io.cloudflight.jems.server.project.service.application.ApplicationStatus.STEP1_ELIGIBLE
import io.cloudflight.jems.server.project.service.application.projectWithId
import io.cloudflight.jems.server.project.service.application.workflow.ApplicationStateFactory
import io.cloudflight.jems.server.project.service.application.workflow.states.EligibleApplicationState
import io.cloudflight.jems.server.project.service.application.workflow.states.first_step.FirstStepEligibleApplicationState
import io.cloudflight.jems.server.project.service.model.ProjectAssessment
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.cloudflight.jems.server.project.service.model.assessment.ProjectAssessmentQuality
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.context.ApplicationEventPublisher

class ApproveApplicationInteractorTest : UnitTest() {

    companion object {
        private const val PROJECT_ID = 1L
        private fun summary(status: ApplicationStatus) = ProjectSummary(
            id = PROJECT_ID,
            customIdentifier = "01",
            callId = 1L,
            callName = "",
            acronym = "project acronym",
            status = status,
        )

        private val actionInfo = ApplicationActionInfo(
            note = "make approval",
            date = LocalDate.of(2021, 4, 13),
            entryIntoForceDate = LocalDate.of(2021, 4, 13),
        )
        private val partnerAssignmentMetadata = ProjectPartnerAssignmentMetadata(
            partnerId = 1L,
            partnerNumber = 1,
            partnerAbbreviation = "LP1",
            partnerRole = ProjectPartnerRole.LEAD_PARTNER,
            partnerActive = true,
            addressNuts3 = "Wien (AT130)",
            addressNuts3Code = "AT130",
            addressCountry = "Austria",
            addressCountryCode = "AT",
            addressCity = "Wien",
            addressPostalCode = "299281",
            projectIdentifier = "0001",
            projectAcronym = "Project Test"
        )
    }

    @RelaxedMockK
    lateinit var projectPersistence: ProjectPersistence

    @RelaxedMockK
    lateinit var projectVersionPersistence: ProjectVersionPersistence

    @RelaxedMockK
    lateinit var applicationStateFactory: ApplicationStateFactory

    @RelaxedMockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @RelaxedMockK
    lateinit var generalValidatorService: GeneralValidatorService

    @RelaxedMockK
    lateinit var projectAuthorization: ProjectAuthorization

    @InjectMockKs
    private lateinit var approveApplication: ApproveApplication

    @MockK
    lateinit var eligibleState: EligibleApplicationState

    @MockK
    lateinit var eligibleStateStep1: FirstStepEligibleApplicationState

    @MockK
    lateinit var partnerPersistence: PartnerPersistence

    @MockK
    lateinit var controllerInstitutionPersistence: ControllerInstitutionPersistence

    @BeforeEach
    fun clearMocks() {
        clearMocks(auditPublisher)
    }

    @Test
    fun `approve when in STEP2`() {
        every { partnerPersistence.getCurrentPartnerAssignmentMetadata(PROJECT_ID) } returns listOf(partnerAssignmentMetadata)
        every { projectPersistence.getProject(PROJECT_ID) } returns projectWithId(PROJECT_ID, status = ELIGIBLE).copy(
            assessmentStep2 = ProjectAssessment(assessmentQuality = ProjectAssessmentQuality(PROJECT_ID, 2, RECOMMENDED_FOR_FUNDING))
        )
        every { projectPersistence.getProjectSummary(PROJECT_ID) } returns summary(ELIGIBLE)
        every { applicationStateFactory.getInstance(any()) } returns eligibleState
        every { eligibleState.approve(actionInfo) } returns APPROVED

        assertThat(approveApplication.approve(PROJECT_ID, actionInfo)).isEqualTo(APPROVED)

        val slotAudit = slot<ProjectStatusChangeEvent>()
        verify(exactly = 1) { auditPublisher.publishEvent(capture(slotAudit)) }
        assertThat(slotAudit.captured).isEqualTo(
            ProjectStatusChangeEvent(
                context = approveApplication,
                projectSummary = summary(ELIGIBLE),
                newStatus = APPROVED
            )
        )
    }

    @Test
    fun `approve when in STEP1`() {
        every { projectPersistence.getProject(PROJECT_ID) } returns projectWithId(PROJECT_ID, status = STEP1_ELIGIBLE).copy(
            assessmentStep1 = ProjectAssessment(assessmentQuality = ProjectAssessmentQuality(PROJECT_ID, 1, RECOMMENDED_FOR_FUNDING))
        )
        every { projectPersistence.getProjectSummary(PROJECT_ID) } returns summary(STEP1_ELIGIBLE)
        every { applicationStateFactory.getInstance(any()) } returns eligibleStateStep1
        every { eligibleStateStep1.approve(actionInfo) } returns STEP1_APPROVED
        every { eligibleStateStep1.approve(actionInfo) } returns STEP1_APPROVED

        assertThat(approveApplication.approve(PROJECT_ID, actionInfo)).isEqualTo(STEP1_APPROVED)

        val slotAudit = slot<ProjectStatusChangeEvent>()
        verify(exactly = 1) { auditPublisher.publishEvent(capture(slotAudit)) }
        assertThat(slotAudit.captured).isEqualTo(
            ProjectStatusChangeEvent(
                context = approveApplication,
                projectSummary = summary(STEP1_ELIGIBLE),
                newStatus = STEP1_APPROVED
            )
        )
    }

    @ParameterizedTest(name = "assessment null when in status {0}")
    @EnumSource(value = ApplicationStatus::class, names = ["ELIGIBLE", "STEP1_ELIGIBLE"])
    fun `quality assessment null`(status: ApplicationStatus) {
        every { projectPersistence.getProjectSummary(PROJECT_ID) } returns summary(status = status)
        every { applicationStateFactory.getInstance(any()) } returns eligibleState
        every { eligibleState.approve(actionInfo) } throws QualityAssessmentMissing()

        assertThrows<QualityAssessmentMissing> { approveApplication.approve(PROJECT_ID, actionInfo) }
    }

    @Test
    fun `approve when submitted precontracted checks modification permission`() {
        every { partnerPersistence.getCurrentPartnerAssignmentMetadata(PROJECT_ID) } returns listOf(partnerAssignmentMetadata)
        every { controllerInstitutionPersistence.updatePartnerDataInAssignments(listOf(partnerAssignmentMetadata)) } returns Unit
        every { projectPersistence.getProjectSummary(PROJECT_ID) } returns summary(ApplicationStatus.MODIFICATION_PRECONTRACTING_SUBMITTED)
        every { applicationStateFactory.getInstance(any()) } returns eligibleState
        every { eligibleState.approve(actionInfo) } returns APPROVED

        assertThat(approveApplication.approve(PROJECT_ID, actionInfo)).isEqualTo(APPROVED)

        // verify CanApproveApplication annotation
    }
}
