package io.cloudflight.jems.server.project.service.report.project.base.getProjectReportList

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.report.model.project.ProjectReportStatus
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportExpenditureCoFinancingPersistence
import io.cloudflight.jems.server.project.service.report.project.base.ProjectReportPersistence
import io.cloudflight.jems.server.project.service.report.project.base.getProjectReport.GetProjectReportTest.Companion.expectedReportSummary
import io.cloudflight.jems.server.project.service.report.project.base.getProjectReport.GetProjectReportTest.Companion.period7
import io.cloudflight.jems.server.project.service.report.project.base.getProjectReport.GetProjectReportTest.Companion.period8
import io.cloudflight.jems.server.project.service.report.project.base.getProjectReport.GetProjectReportTest.Companion.report
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

internal class GetProjectReportListTest : UnitTest() {

    @MockK
    private lateinit var reportPersistence: ProjectReportPersistence
    @MockK
    private lateinit var certificateCoFinancingPersistence: ProjectPartnerReportExpenditureCoFinancingPersistence
    @MockK
    private lateinit var projectPersistence: ProjectPersistence

    @InjectMockKs
    lateinit var interactor: GetProjectReportList

    @BeforeEach
    fun reset() {
        clearMocks(reportPersistence, certificateCoFinancingPersistence, projectPersistence)
        every { projectPersistence.getProjectPeriods(any(), "v4") } returns listOf(period7)
        every { projectPersistence.getProjectPeriods(any(), "v5") } returns listOf(period8)
        every { certificateCoFinancingPersistence.getTotalsForProjectReports(any())} returns mapOf(
            Pair(7L, BigDecimal.ONE),
        )
    }

    @Test
    fun findAll() {
        val projectId = 91L
        every { reportPersistence.listReports(projectId, Pageable.unpaged()) } returns PageImpl(
            listOf(
                report.copy(id = 6L, periodNumber = 6, totalEligibleAfterVerification = null),
                report.copy(id = 7L, periodNumber = 7),
                report.copy(id = 8L, periodNumber = 8, status = ProjectReportStatus.InVerification,
                    linkedFormVersion = "v5", totalEligibleAfterVerification = BigDecimal.ZERO),
                report.copy(id = 9L, periodNumber = 9, status = ProjectReportStatus.Finalized,
                    amountRequested = BigDecimal.ZERO, totalEligibleAfterVerification = BigDecimal.ZERO),
                report.copy(id = 10L, periodNumber = 10, status = ProjectReportStatus.Submitted,
                    totalEligibleAfterVerification = BigDecimal.valueOf(54),
                    amountRequested = BigDecimal.valueOf(58),
                ),
            )
        )

        assertThat(interactor.findAll(projectId, Pageable.unpaged())).containsExactly(
            expectedReportSummary.copy(id = 6L, deletable = true, totalEligibleAfterVerification = null, periodDetail = null),
            expectedReportSummary.copy(id = 7L, deletable = true, amountRequested = BigDecimal.ONE, periodDetail = period7),
            expectedReportSummary.copy(id = 8L, status = ProjectReportStatus.InVerification,
                totalEligibleAfterVerification = null, linkedFormVersion = "v5", periodDetail = period8),
            expectedReportSummary.copy(id = 9L, status = ProjectReportStatus.Finalized, periodDetail = null,
                totalEligibleAfterVerification = null, amountRequested = null),
            expectedReportSummary.copy(id = 10L, status = ProjectReportStatus.Submitted, periodDetail = null,
                totalEligibleAfterVerification = BigDecimal.valueOf(54),
                amountRequested = BigDecimal.valueOf(58),
            ),
        )
    }

}
