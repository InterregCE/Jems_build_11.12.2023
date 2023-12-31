package io.cloudflight.jems.server.project.service.report.project.base.startVerificationProjectReport

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.notification.handler.ProjectReportStatusChanged
import io.cloudflight.jems.server.project.service.report.model.project.ProjectReportStatus
import io.cloudflight.jems.server.project.service.report.model.project.ProjectReportSubmissionSummary
import io.cloudflight.jems.server.project.service.report.model.project.base.ProjectReportModel
import io.cloudflight.jems.server.project.service.report.project.base.ProjectReportPersistence
import io.cloudflight.jems.server.project.service.report.project.verification.expenditure.ProjectReportVerificationExpenditurePersistence
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.context.ApplicationEventPublisher
import java.time.ZonedDateTime

internal class StartVerificationProjectReportTest : UnitTest() {

    companion object {
        private const val PROJECT_ID = 256L

        private val mockedResult = ProjectReportSubmissionSummary(
            id = 37L,
            reportNumber = 4,
            status = ProjectReportStatus.InVerification,
            version = "5.6.1",
            // not important
            firstSubmission = ZonedDateTime.now(),
            createdAt = ZonedDateTime.now(),
            projectIdentifier = "FG01_654",
            projectAcronym = "acronym",
            projectId = PROJECT_ID,
        )
    }

    @MockK
    lateinit var reportPersistence: ProjectReportPersistence

    @MockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @MockK
    lateinit var projectReportExpenditureVerificationPersistence: ProjectReportVerificationExpenditurePersistence

    @InjectMockKs
    lateinit var interactor: StartVerificationProjectReport

    @BeforeEach
    fun reset() {
        clearMocks(reportPersistence)
        clearMocks(auditPublisher)
    }

    @ParameterizedTest(name = "startVerification (status {0})")
    @EnumSource(value = ProjectReportStatus::class, names = ["Submitted"])
    fun startVerification(status: ProjectReportStatus) {
        val report = report(37L, status)
        every { reportPersistence.getReportById(PROJECT_ID, 37L) } returns report

        every { reportPersistence.startVerificationOnReportById(any(), any()) } returns mockedResult
        every { projectReportExpenditureVerificationPersistence.initiateEmptyVerificationForProjectReport(any()) } returns Unit

        val auditSlot = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(auditSlot)) } returns Unit
        every { auditPublisher.publishEvent(ofType(ProjectReportStatusChanged::class)) } returns Unit

        interactor.startVerification(PROJECT_ID, 37L)

        verify(exactly = 1) { reportPersistence.startVerificationOnReportById(PROJECT_ID, 37L) }

        Assertions.assertThat(auditSlot.captured.auditCandidate.action)
            .isEqualTo(AuditAction.PROJECT_REPORT_VERIFICATION_ONGOING)
        Assertions.assertThat(auditSlot.captured.auditCandidate.project?.id).isEqualTo(PROJECT_ID.toString())
        Assertions.assertThat(auditSlot.captured.auditCandidate.project?.customIdentifier).isEqualTo("FG01_654")
        Assertions.assertThat(auditSlot.captured.auditCandidate.project?.name).isEqualTo("acronym")
        Assertions.assertThat(auditSlot.captured.auditCandidate.entityRelatedId).isEqualTo(37L)
        Assertions.assertThat(auditSlot.captured.auditCandidate.description)
            .isEqualTo("[FG01_654] Project report R.4 verification started")
    }

    @ParameterizedTest(name = "startVerification - wrong status (status {0})")
    @EnumSource(value = ProjectReportStatus::class, names = ["Submitted"], mode = EnumSource.Mode.EXCLUDE)
    fun `startVerification - wrong status`(status: ProjectReportStatus) {
        val report = report(39L, status)
        every { reportPersistence.getReportById(PROJECT_ID, 39L) } returns report

        assertThrows<ReportNotSubmitted> { interactor.startVerification(PROJECT_ID, 39L) }

        verify(exactly = 0) { reportPersistence.startVerificationOnReportById(any(), any()) }
        verify(exactly = 0) { auditPublisher.publishEvent(any()) }
    }

    private fun report(id: Long, status: ProjectReportStatus): ProjectReportModel {
        val report = mockk<ProjectReportModel>()
        every { report.id } returns id
        every { report.status } returns status
        return report
    }

}
