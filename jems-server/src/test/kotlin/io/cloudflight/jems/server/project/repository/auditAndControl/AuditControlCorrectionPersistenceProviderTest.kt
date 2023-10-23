package io.cloudflight.jems.server.project.repository.auditAndControl

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.entity.auditAndControl.AuditControlEntity
import io.cloudflight.jems.server.project.entity.auditAndControl.ProjectAuditControlCorrectionEntity
import io.cloudflight.jems.server.project.repository.auditAndControl.correction.AuditControlCorrectionPersistenceProvider
import io.cloudflight.jems.server.project.repository.auditAndControl.correction.AuditControlCorrectionRepository
import io.cloudflight.jems.server.project.repository.auditAndControl.correction.toEntity
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.auditAndControl.correction.model.CorrectionStatus
import io.cloudflight.jems.server.project.service.auditAndControl.correction.model.ProjectAuditControlCorrection
import io.cloudflight.jems.server.project.service.auditAndControl.correction.model.ProjectAuditControlCorrectionExtended
import io.cloudflight.jems.server.project.service.auditAndControl.model.AuditControlType
import io.cloudflight.jems.server.project.service.auditAndControl.model.AuditStatus
import io.cloudflight.jems.server.project.service.auditAndControl.model.ControllingBody
import io.cloudflight.jems.server.project.service.auditAndControl.updateProjectAudit.UpdateProjectAuditTest
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

class AuditControlCorrectionPersistenceProviderTest : UnitTest() {

    companion object {
        private const val AUDIT_CONTROL_ID = 1L
        private const val PROJECT_ID = 2L
        private const val CORRECTION_ID = 1L

        private val projectAuditControlEntity = AuditControlEntity(
            id = AUDIT_CONTROL_ID,
            number = 20,
            projectId = PROJECT_ID,
            projectCustomIdentifier = "test",
            status = AuditStatus.Ongoing,
            controllingBody = ControllingBody.OLAF,
            controlType = AuditControlType.Administrative,
            startDate = UpdateProjectAuditTest.DATE.minusDays(1),
            endDate = UpdateProjectAuditTest.DATE.plusDays(1),
            finalReportDate = UpdateProjectAuditTest.DATE.minusDays(5),
            totalControlledAmount = BigDecimal.valueOf(10000),
            totalCorrectionsAmount = BigDecimal.ZERO,
            comment = null
        )

        private val projectSummary = ProjectSummary(
            id = PROJECT_ID,
            customIdentifier = "test",
            callId = 1L,
            callName = "call",
            acronym = "test",
            status = ApplicationStatus.CONTRACTED,
        )

        private val correction = ProjectAuditControlCorrection(
            id = 1L,
            auditControlId = AUDIT_CONTROL_ID,
            orderNr = 10,
            status = CorrectionStatus.Ongoing,
            linkedToInvoice = true,
        )

        private val extendedCorrection = ProjectAuditControlCorrectionExtended(
            correction = correction,
            auditControlNumber = 20,
            projectCustomIdentifier = projectSummary.customIdentifier
        )

        private val correctionEntity = ProjectAuditControlCorrectionEntity(
            id = 1L,
            auditControlEntity = projectAuditControlEntity,
            orderNr = 10,
            status = CorrectionStatus.Ongoing,
            linkedToInvoice = true,
        )
    }

    @MockK
    lateinit var auditControlCorrectionRepository: AuditControlCorrectionRepository

    @MockK
    lateinit var auditControlRepository: AuditControlRepository

    @InjectMockKs
    lateinit var auditControlCorrectionPersistenceProvider: AuditControlCorrectionPersistenceProvider

    @Test
    fun saveCorrection() {
        val correctionEntity = correction.toEntity { projectAuditControlEntity }

        every { auditControlRepository.getById(AUDIT_CONTROL_ID) } returns projectAuditControlEntity
        every { auditControlCorrectionRepository.save(any()) } returns correctionEntity

        assertThat(auditControlCorrectionPersistenceProvider.saveCorrection(correction)).isEqualTo(correction)
    }

    @Test
    fun getAllCorrectionsByAuditControlId() {
        every {
            auditControlCorrectionRepository.findAllByAuditControlEntityId(
                AUDIT_CONTROL_ID,
                Pageable.unpaged()
            )
        } returns PageImpl(listOf(correctionEntity))

        assertThat(
            auditControlCorrectionPersistenceProvider.getAllCorrectionsByAuditControlId(
                AUDIT_CONTROL_ID,
                Pageable.unpaged()
            ).content
        ).isEqualTo(listOf(correction))
    }

    @Test
    fun getByCorrectionId() {
        every { auditControlCorrectionRepository.getById(CORRECTION_ID) } returns correctionEntity

        assertThat(auditControlCorrectionPersistenceProvider.getByCorrectionId(CORRECTION_ID)).isEqualTo(correction)
    }

    @Test
    fun getExtendedByCorrectionId() {
        every { auditControlCorrectionRepository.getById(CORRECTION_ID) } returns correctionEntity

        assertThat(auditControlCorrectionPersistenceProvider.getExtendedByCorrectionId(CORRECTION_ID)).isEqualTo(
            extendedCorrection
        )
    }

    @Test
    fun getLastUsedOrderNr() {
        every { auditControlCorrectionRepository.findFirstByAuditControlEntityIdOrderByOrderNrDesc(AUDIT_CONTROL_ID) } returns correctionEntity

        assertThat(auditControlCorrectionPersistenceProvider.getLastUsedOrderNr(AUDIT_CONTROL_ID)).isEqualTo(10)
    }

    @Test
    fun getLastCorrectionIdByAuditControlId() {
        every { auditControlCorrectionRepository.findFirstByAuditControlEntityIdOrderByOrderNrDesc(AUDIT_CONTROL_ID) } returns correctionEntity

        assertThat(auditControlCorrectionPersistenceProvider.getLastCorrectionIdByAuditControlId(AUDIT_CONTROL_ID)).isEqualTo(1)
    }

}