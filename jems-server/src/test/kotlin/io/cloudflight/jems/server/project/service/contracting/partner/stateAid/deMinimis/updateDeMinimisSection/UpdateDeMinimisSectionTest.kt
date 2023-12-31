package io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.updateDeMinimisSection

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.repository.contracting.partner.stateAid.deMinimis.toModel
import io.cloudflight.jems.server.project.service.ProjectVersionPersistence
import io.cloudflight.jems.server.project.service.budget.get_partner_budget_per_funds.GetPartnerBudgetPerFundService
import io.cloudflight.jems.server.project.service.contracting.ContractingModificationDeniedException
import io.cloudflight.jems.server.project.service.contracting.ContractingValidator
import io.cloudflight.jems.server.project.service.contracting.monitoring.getProjectContractingMonitoring.GetContractingMonitoringService
import io.cloudflight.jems.server.project.service.contracting.partner.partnerLock.ContractingPartnerLockPersistence
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.ContractingPartnerStateAidDeMinimisPersistence
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.LAST_APPROVED_VERSION
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.PARTNER_ID
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.PROJECT_ID
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.deMinimisEntity
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.deMinimisModel
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.expectedDeMinimisSection
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.getContractMonitoring
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.leadPartner
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.partnerFunds
import io.cloudflight.jems.server.project.service.contracting.partner.stateAid.deMinimis.updateStateAidDeMinimisSection.UpdateContractingPartnerStateAidDeMinimis
import io.cloudflight.jems.server.project.service.contracting.sectionLock.ProjectContractingSectionLockPersistence
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateDeMinimisSectionTest : UnitTest() {

    @MockK
    lateinit var contractingPartnerStateAidDeMinimisPersistence: ContractingPartnerStateAidDeMinimisPersistence

    @MockK
    lateinit var getContractingMonitoringService: GetContractingMonitoringService

    @MockK
    lateinit var partnerBudgetPerFundService: GetPartnerBudgetPerFundService

    @MockK
    lateinit var partnerPersistence: PartnerPersistence

    @MockK
    lateinit var validator: GeneralValidatorService

    @MockK
    lateinit var  projectContractingSectionLockPersistence: ProjectContractingSectionLockPersistence

    @MockK
    lateinit var contractingPartnerLockPersistence: ContractingPartnerLockPersistence

    @RelaxedMockK
    lateinit var versionPersistence: ProjectVersionPersistence

    @InjectMockKs
    lateinit var contractingValidator: ContractingValidator

    @InjectMockKs
    lateinit var updateContractingPartnerStateAidDeMinimis: UpdateContractingPartnerStateAidDeMinimis

    @BeforeEach
    fun setup() {
        every { partnerPersistence.getById(PARTNER_ID, LAST_APPROVED_VERSION) } returns leadPartner
        every { partnerPersistence.getProjectIdForPartnerId(PARTNER_ID) } returns PROJECT_ID
        every { versionPersistence.getLatestApprovedOrCurrent(PROJECT_ID) } returns LAST_APPROVED_VERSION
    }

    @Test
    fun updateDeMinimis() {
        every { contractingPartnerStateAidDeMinimisPersistence.saveDeMinimis(PARTNER_ID, deMinimisEntity.toModel()) } returns deMinimisEntity
        every { contractingPartnerLockPersistence.isLocked(PARTNER_ID) } returns false
        every { getContractingMonitoringService.getProjectContractingMonitoring(PROJECT_ID) } returns getContractMonitoring()
        every { partnerBudgetPerFundService.getProjectPartnerBudgetPerFund(PROJECT_ID, LAST_APPROVED_VERSION) } returns partnerFunds

        assertThat(
            updateContractingPartnerStateAidDeMinimis.updateDeMinimisSection(
                PARTNER_ID,
                deMinimisModel
            )
        ).isEqualTo(
            expectedDeMinimisSection
        )
    }

    @Test
    fun `update de minimis when partner section is locked`() {
        every { contractingPartnerStateAidDeMinimisPersistence.saveDeMinimis(PARTNER_ID, deMinimisEntity.toModel()) } returns deMinimisEntity
        every { contractingPartnerLockPersistence.isLocked(PARTNER_ID) } returns true
        every { getContractingMonitoringService.getProjectContractingMonitoring(PROJECT_ID) } returns getContractMonitoring()
        every { partnerBudgetPerFundService.getProjectPartnerBudgetPerFund(PROJECT_ID, LAST_APPROVED_VERSION) } returns partnerFunds

        assertThrows<ContractingModificationDeniedException> { updateContractingPartnerStateAidDeMinimis.updateDeMinimisSection(PARTNER_ID, deMinimisModel) }
    }
}
