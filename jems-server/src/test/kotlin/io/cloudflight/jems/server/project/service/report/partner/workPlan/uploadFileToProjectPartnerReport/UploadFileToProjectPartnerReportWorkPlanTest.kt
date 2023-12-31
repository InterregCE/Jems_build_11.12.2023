package io.cloudflight.jems.server.project.service.report.partner.workPlan.uploadFileToProjectPartnerReport

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.file.service.model.JemsFileCreate
import io.cloudflight.jems.server.common.file.service.model.JemsFileMetadata
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.project.service.file.model.ProjectFile
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.report.partner.file.ProjectPartnerReportFilePersistence
import io.cloudflight.jems.server.project.service.report.partner.workPlan.ProjectPartnerReportWorkPlanPersistence
import io.mockk.CapturingSlot
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val PROJECT_ID = 450L
private const val PARTNER_ID = 478L
private const val REPORT_ID = 462L
private const val WP_ID = 481L
private const val USER_ID = 435L

private const val FILE_SIZE = 100L

internal class UploadFileToProjectPartnerReportWorkPlanTest : UnitTest() {

    private fun getDummyFile(name: String) = ProjectFile(mockk(), name = name, size = FILE_SIZE)

    @MockK
    lateinit var reportFilePersistence: ProjectPartnerReportFilePersistence
    @MockK
    lateinit var reportWorkPlanPersistence: ProjectPartnerReportWorkPlanPersistence
    @MockK
    lateinit var partnerPersistence: PartnerPersistence
    @MockK
    lateinit var securityService: SecurityService

    @InjectMockKs
    lateinit var interactor: UploadFileToProjectPartnerReportWorkPlan

    @BeforeEach
    fun setup() {
        clearMocks(partnerPersistence)
        clearMocks(securityService)
        every { partnerPersistence.getProjectIdForPartnerId(PARTNER_ID) } returns PROJECT_ID
        every { securityService.getUserIdOrThrow() } returns USER_ID
    }

    @Test
    fun uploadToActivity() {
        every { reportWorkPlanPersistence.existsByActivityId(PARTNER_ID, REPORT_ID, WP_ID, activityId = 10L) } returns true

        val newFile = slot<JemsFileCreate>()
        val mockedResult = mockk<JemsFileMetadata>()
        every { reportFilePersistence.updatePartnerReportActivityAttachment(10L, capture(newFile)) } returns mockedResult

        assertThat(
            interactor.uploadToActivity(PARTNER_ID, REPORT_ID, WP_ID, 10L, getDummyFile("new_file.pdf"))
        ).isEqualTo(mockedResult)

        assertBasicFileAttributes(newFile)
        with(newFile.captured) {
            assertThat(path).isEqualTo("Project/000450/Report/Partner/000478/PartnerReport/000462/WorkPlan/WorkPackage/000481/Activity/000010/")
            assertThat(type).isEqualTo(JemsFileType.Activity)
        }
    }

    @Test
    fun `uploadToActivity - not existing`() {
        every { reportWorkPlanPersistence.existsByActivityId(PARTNER_ID, REPORT_ID, WP_ID, activityId = -1L) } returns false
        assertThrows<ActivityNotFoundException> {
            interactor.uploadToActivity(PARTNER_ID, REPORT_ID, WP_ID, -1L, mockk())
        }
    }

    @Test
    fun `uploadToActivity - file type invalid`() {
        every { reportWorkPlanPersistence.existsByActivityId(PARTNER_ID, REPORT_ID, WP_ID, 12L) } returns true

        val file = mockk<ProjectFile>()
        every { file.name } returns "invalid.exe"

        assertThrows<FileTypeNotSupported> {
            interactor.uploadToActivity(PARTNER_ID, REPORT_ID, WP_ID, 12L, file)
        }
    }

    @Test
    fun uploadToDeliverable() {
        every { reportWorkPlanPersistence.existsByDeliverableId(PARTNER_ID, REPORT_ID, WP_ID, activityId = 10L, deliverableId = 12L) } returns true

        val newFile = slot<JemsFileCreate>()
        val mockedResult = mockk<JemsFileMetadata>()
        every { reportFilePersistence.updatePartnerReportDeliverableAttachment(12L, capture(newFile)) } returns mockedResult

        assertThat(
            interactor.uploadToDeliverable(PARTNER_ID, REPORT_ID, WP_ID, 10L, 12L, getDummyFile("new_file.pdf"))
        ).isEqualTo(mockedResult)

        assertBasicFileAttributes(newFile)
        with(newFile.captured) {
            assertThat(path).isEqualTo("Project/000450/Report/Partner/000478/PartnerReport/000462/WorkPlan/WorkPackage/000481/Activity/000010/Deliverable/000012/")
            assertThat(type).isEqualTo(JemsFileType.Deliverable)
        }
    }

    @Test
    fun `uploadToDeliverable - file type invalid`() {
        every { reportWorkPlanPersistence.existsByDeliverableId(PARTNER_ID, REPORT_ID, WP_ID, 10L, 14L) } returns true

        val file = mockk<ProjectFile>()
        every { file.name } returns "invalid.exe"

        assertThrows<FileTypeNotSupported> {
            interactor.uploadToDeliverable(PARTNER_ID, REPORT_ID, WP_ID, 10L, 14L, file)
        }
    }

    @Test
    fun `uploadToDeliverable - not existing`() {
        every { reportWorkPlanPersistence.existsByDeliverableId(PARTNER_ID, REPORT_ID, WP_ID, activityId = -1L, deliverableId = -1L) } returns false
        assertThrows<DeliverableNotFoundException> {
            interactor.uploadToDeliverable(PARTNER_ID, REPORT_ID, WP_ID, -1L, -1L, mockk())
        }
    }

    @Test
    fun uploadToOutput() {
        every { reportWorkPlanPersistence.existsByOutputId(PARTNER_ID, REPORT_ID, WP_ID, outputId = 15L) } returns true

        val newFile = slot<JemsFileCreate>()
        val mockedResult = mockk<JemsFileMetadata>()
        every { reportFilePersistence.updatePartnerReportOutputAttachment(15L, capture(newFile)) } returns mockedResult

        assertThat(
            interactor.uploadToOutput(PARTNER_ID, REPORT_ID, WP_ID, 15L, getDummyFile("new_file.pdf"))
        ).isEqualTo(mockedResult)

        assertBasicFileAttributes(newFile)
        with(newFile.captured) {
            assertThat(path).isEqualTo("Project/000450/Report/Partner/000478/PartnerReport/000462/WorkPlan/WorkPackage/000481/Output/000015/")
            assertThat(type).isEqualTo(JemsFileType.Output)
        }
    }

    @Test
    fun `uploadToOutput - not existing`() {
        every { reportWorkPlanPersistence.existsByOutputId(PARTNER_ID, REPORT_ID, WP_ID, outputId = -1L) } returns false
        assertThrows<OutputNotFoundException> {
            interactor.uploadToOutput(PARTNER_ID, REPORT_ID, WP_ID, -1L, mockk())
        }
    }

    @Test
    fun `uploadToOutput - file type invalid`() {
        every { reportWorkPlanPersistence.existsByOutputId(PARTNER_ID, REPORT_ID, WP_ID, 17L) } returns true

        val file = mockk<ProjectFile>()
        every { file.name } returns "invalid.exe"

        assertThrows<FileTypeNotSupported> {
            interactor.uploadToOutput(PARTNER_ID, REPORT_ID, WP_ID, 17L, file)
        }
    }

    private fun assertBasicFileAttributes(newFile: CapturingSlot<JemsFileCreate>) {
        with(newFile.captured) {
            assertThat(projectId).isEqualTo(PROJECT_ID)
            assertThat(partnerId).isEqualTo(PARTNER_ID)
            assertThat(name).isEqualTo("new_file.pdf")
            assertThat(size).isEqualTo(FILE_SIZE)
            assertThat(userId).isEqualTo(USER_ID)
        }
    }

}
