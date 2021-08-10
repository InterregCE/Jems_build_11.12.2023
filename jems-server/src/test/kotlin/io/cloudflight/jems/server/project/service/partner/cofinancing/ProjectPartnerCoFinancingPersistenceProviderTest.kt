package io.cloudflight.jems.server.project.service.partner.cofinancing

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.description.ProjectTargetGroupDTO
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRoleDTO
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerVatRecoveryDTO
import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerCoFinancingFundTypeDTO
import io.cloudflight.jems.api.project.dto.partner.cofinancing.ProjectPartnerContributionStatusDTO
import io.cloudflight.jems.server.programme.entity.fund.ProgrammeFundEntity
import io.cloudflight.jems.server.programme.entity.legalstatus.ProgrammeLegalStatusEntity
import io.cloudflight.jems.server.project.entity.partner.PartnerIdentityRow
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import io.cloudflight.jems.server.project.entity.partner.cofinancing.PartnerContributionRow
import io.cloudflight.jems.server.project.entity.partner.cofinancing.PartnerFinancingRow
import io.cloudflight.jems.server.project.entity.partner.cofinancing.ProjectPartnerCoFinancingEntity
import io.cloudflight.jems.server.project.entity.partner.cofinancing.ProjectPartnerCoFinancingFundId
import io.cloudflight.jems.server.project.entity.partner.cofinancing.ProjectPartnerContributionEntity
import io.cloudflight.jems.server.project.repository.ProjectVersionRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionUtils
import io.cloudflight.jems.server.project.repository.partner.ProjectPartnerRepository
import io.cloudflight.jems.server.project.repository.partner.cofinancing.ProjectPartnerCoFinancingPersistenceProvider
import io.cloudflight.jems.server.project.repository.partner.cofinancing.toCoFinancingModel
import io.cloudflight.jems.server.project.repository.partner.cofinancing.toContributionModel
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.partner.ProjectPartnerTestUtil
import io.cloudflight.jems.server.project.service.partner.cofinancing.model.ProjectPartnerCoFinancingAndContribution
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Optional

open class ProjectPartnerCoFinancingPersistenceProviderTest {
    protected val partnerId = 1L
    protected val projectId = 2L
    protected val timestamp: Timestamp = Timestamp.valueOf(LocalDateTime.now())
    protected val version = "1.0"

    protected class PreviousVersionOfPartner(
        override val language: SystemLanguage?,
        override val id: Long,
        override val projectId: Long,
        override val abbreviation: String,
        override val role: ProjectPartnerRoleDTO,
        override val sortNumber: Int,
        override val nameInOriginalLanguage: String?,
        override val nameInEnglish: String?,
        override val partnerType: ProjectTargetGroupDTO?,
        override val vat: String?,
        override val vatRecovery: ProjectPartnerVatRecoveryDTO?,
        override val legalStatusId: Long,
        override val department: String?
    ) : PartnerIdentityRow

    protected class PreviousVersionOfCoFinancing(
        override val type: ProjectPartnerCoFinancingFundTypeDTO,
        override val percentage: BigDecimal,
        override val language: SystemLanguage?,
        override val fundId: Long?,
        override val selected: Boolean?,
        override val fundType: String?,
        override val abbreviation: String?,
        override val description: String?,
    ) : PartnerFinancingRow

    protected class PreviousVersionOfContribution(
        override val id: Long,
        override val name: String?,
        override val status: ProjectPartnerContributionStatusDTO?,
        override val amount: BigDecimal
    ) : PartnerContributionRow

    private val fund = ProgrammeFundEntity(id = 1, selected = true)

    private val previousFinances = setOf(
        ProjectPartnerCoFinancingEntity(
            coFinancingFundId = ProjectPartnerCoFinancingFundId(1, ProjectPartnerCoFinancingFundTypeDTO.MainFund),
            percentage = BigDecimal.valueOf(15.5),
            programmeFund = null
        ),
        ProjectPartnerCoFinancingEntity(
            coFinancingFundId = ProjectPartnerCoFinancingFundId(
                1,
                ProjectPartnerCoFinancingFundTypeDTO.PartnerContribution
            ),
            percentage = BigDecimal.valueOf(25.5),
            programmeFund = null
        )
    )

    private val currentFinances = setOf(
        ProjectPartnerCoFinancingEntity(
            coFinancingFundId = ProjectPartnerCoFinancingFundId(1, ProjectPartnerCoFinancingFundTypeDTO.MainFund),
            percentage = BigDecimal.valueOf(19.5),
            programmeFund = fund
        ),
        ProjectPartnerCoFinancingEntity(
            coFinancingFundId = ProjectPartnerCoFinancingFundId(
                1,
                ProjectPartnerCoFinancingFundTypeDTO.PartnerContribution
            ),
            percentage = BigDecimal.valueOf(79.5),
            programmeFund = null
        )
    )

    private val previousContributions = listOf(
        ProjectPartnerContributionEntity(
            id = 1,
            partnerId = 1,
            name = null,
            status = ProjectPartnerContributionStatusDTO.Public,
            amount = BigDecimal.ONE
        ),
        ProjectPartnerContributionEntity(
            id = 2,
            partnerId = 1,
            name = "BMW",
            status = ProjectPartnerContributionStatusDTO.AutomaticPublic,
            amount = BigDecimal.ZERO
        )
    )

    private val currentContributions = listOf(
        ProjectPartnerContributionEntity(
            id = 1,
            partnerId = 1,
            name = null,
            status = ProjectPartnerContributionStatusDTO.Public,
            amount = BigDecimal.TEN
        ),
        ProjectPartnerContributionEntity(
            id = 2,
            partnerId = 1,
            name = "BMW",
            status = ProjectPartnerContributionStatusDTO.AutomaticPublic,
            amount = BigDecimal.ONE
        )
    )

    private val currentValue = ProjectPartnerCoFinancingAndContribution(
        finances = currentFinances.toCoFinancingModel(),
        partnerContributions = currentContributions.toContributionModel(),
        partnerAbbreviation = "partner"
    )

    private val previousValue = ProjectPartnerCoFinancingAndContribution(
        finances = previousFinances.toCoFinancingModel(),
        partnerContributions = previousContributions.toContributionModel(),
        partnerAbbreviation = "previous partner"
    )

    private val projectPartner = ProjectPartnerEntity(
        id = 1,
        project = ProjectPartnerTestUtil.project,
        abbreviation = "partner",
        role = ProjectPartnerRoleDTO.LEAD_PARTNER,
        partnerType = ProjectTargetGroupDTO.BusinessSupportOrganisation,
        legalStatus = ProgrammeLegalStatusEntity(id = 1),
        vat = "test vat",
        vatRecovery = ProjectPartnerVatRecoveryDTO.Yes,
        financing = currentFinances,
        partnerContributions = currentContributions
    )

    private val previousFinancingValues = listOf(
        PreviousVersionOfCoFinancing(
            type = ProjectPartnerCoFinancingFundTypeDTO.MainFund,
            percentage = BigDecimal.valueOf(15.5),
            language = null,
            abbreviation = null,
            description = null,
            fundId = 1,
            fundType = null,
            selected = true
        ),
        PreviousVersionOfCoFinancing(
            type = ProjectPartnerCoFinancingFundTypeDTO.PartnerContribution,
            percentage = BigDecimal.valueOf(25.5),
            language = null,
            abbreviation = null,
            description = null,
            fundId = 1,
            fundType = null,
            selected = true
        )
    )

    private val previousContributionValues = listOf(
        PreviousVersionOfContribution(
            id = 1,
            name = null,
            status = ProjectPartnerContributionStatusDTO.Public,
            amount = BigDecimal.ONE
        ),
        PreviousVersionOfContribution(
            id = 2,
            name = "BMW",
            status = ProjectPartnerContributionStatusDTO.AutomaticPublic,
            amount = BigDecimal.ZERO
        )
    )

    private val previousProjectPartner = PreviousVersionOfPartner(
        language = SystemLanguage.EN,
        id = 1,
        projectId = 1,
        abbreviation = "previous partner",
        role = ProjectPartnerRoleDTO.LEAD_PARTNER,
        sortNumber = 1,
        nameInOriginalLanguage = "",
        nameInEnglish = "",
        partnerType = ProjectTargetGroupDTO.BusinessSupportOrganisation,
        vat = "test vat",
        vatRecovery = ProjectPartnerVatRecoveryDTO.Yes,
        legalStatusId = 1,
        department = ""
    )

    @MockK
    lateinit var projectVersionRepo: ProjectVersionRepository

    @MockK
    lateinit var projectPersistence: ProjectPersistence

    @MockK
    lateinit var projectPartnerRepository: ProjectPartnerRepository

    lateinit var projectVersionUtils: ProjectVersionUtils
    protected lateinit var persistence: ProjectPartnerCoFinancingPersistence

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        projectVersionUtils = ProjectVersionUtils(projectVersionRepo)
        persistence = ProjectPartnerCoFinancingPersistenceProvider(
            projectPartnerRepository,
            projectVersionUtils
        )
        every { projectPartnerRepository.getProjectIdForPartner(partnerId) } returns projectId
        every { projectVersionRepo.findTimestampByVersion(projectId, version) } returns timestamp
    }

    @Test
    fun `should return current version of coFinancing`() {
        every { projectPartnerRepository.findById(partnerId) } returns Optional.of(projectPartner)
        assertThat(persistence.getCoFinancingAndContributions(1, null)).isEqualTo(currentValue)
    }

    @Test
    fun `should return previous version of coFinancing`() {
        every { projectPartnerRepository.getProjectIdByPartnerIdInFullHistory(partnerId) } returns projectId
        every { projectPartnerRepository.findPartnerIdentityByIdAsOfTimestamp(partnerId, timestamp) } returns listOf(
            previousProjectPartner
        )
        every {
            projectPartnerRepository.findPartnerFinancingByIdAsOfTimestamp(
                partnerId,
                timestamp
            )
        } returns previousFinancingValues
        every {
            projectPartnerRepository.findPartnerContributionByIdAsOfTimestamp(
                partnerId,
                timestamp
            )
        } returns previousContributionValues
        assertThat(persistence.getCoFinancingAndContributions(1, version)).isEqualTo(previousValue)
    }
}
