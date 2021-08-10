package io.cloudflight.jems.server.project.service.associatedorganization

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.api.project.dto.ProjectContactTypeDTO
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationAddress
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationDetail
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerSummaryDTO
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerContactDTO
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerAddressTypeDTO
import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRoleDTO
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.legalstatus.ProgrammeLegalStatusEntity
import io.cloudflight.jems.server.project.entity.AddressEntity
import io.cloudflight.jems.server.project.entity.Contact
import io.cloudflight.jems.server.project.entity.ProjectEntity
import io.cloudflight.jems.server.project.entity.TranslationOrganizationId
import io.cloudflight.jems.server.project.entity.associatedorganization.AssociatedOrganizationAddressRow
import io.cloudflight.jems.server.project.entity.associatedorganization.AssociatedOrganizationContactRow
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganization
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationAddress
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationContact
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationContactId
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationRow
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationTransl
import io.cloudflight.jems.server.project.entity.partner.PartnerSimpleRow
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerAddress
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerAddressId
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import io.cloudflight.jems.server.project.repository.ProjectAssociatedOrganizationRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionUtils
import io.cloudflight.jems.server.project.repository.partner.ProjectPartnerRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Optional

class AssociatedOrganizationPersistenceTest {

    companion object {
        private const val projectId = 2L

        private val project = ProjectEntity(
            id = 1,
            call = mockk(),
            acronym = "Test Project",
            applicant = mockk(),
            currentStatus = mockk()
        )
        private val address = AddressEntity(
            country = "country",
            nutsRegion2 = "nutsRegion2",
            nutsRegion3 = "nutsRegion3",
            street = "street",
            houseNumber = "houseNumber",
            postalCode = "postalCode",
            city = "city",
            homepage = "homepage"
        )
        private val projectPartner = ProjectPartnerEntity(
            id = 3L,
            project = project,
            abbreviation = "abbreviation",
            sortNumber = 1,
            role = ProjectPartnerRoleDTO.LEAD_PARTNER,
            legalStatus = ProgrammeLegalStatusEntity(id = 5L),
            addresses = setOf(ProjectPartnerAddress(
                addressId = ProjectPartnerAddressId(partnerId = 3L, ProjectPartnerAddressTypeDTO.Organization),
                address = address
            ))
        )

        private val projectAssociatedOrganization = ProjectAssociatedOrganization(
            id = 2L,
            project = project,
            partner = projectPartner,
            nameInOriginalLanguage = "nameInOriginalLanguage",
            nameInEnglish = "nameInEnglish",
            sortNumber = 1,
            translatedValues = mutableSetOf(ProjectAssociatedOrganizationTransl(
                translationId = TranslationOrganizationId(2L, SystemLanguage.EN),
                roleDescription = "roleDescription"
            )),
            addresses = mutableSetOf(ProjectAssociatedOrganizationAddress(
                organizationId = 2L,
                address = address
            )),
            contacts = mutableSetOf(ProjectAssociatedOrganizationContact(
                contactId = ProjectAssociatedOrganizationContactId(2L, ProjectContactTypeDTO.ContactPerson),
                contact = Contact(
                    title = "title",
                    firstName = "firstName",
                    lastName = "lastName",
                    email = "email",
                    telephone = "telephone"
                )
            ))
        )
        private val projectAssociatedOrganizationOutput = OutputProjectAssociatedOrganizationDetail(
            id = 2L,
            partner = ProjectPartnerSummaryDTO(
                id = projectPartner.id,
                abbreviation = projectPartner.abbreviation,
                role = projectPartner.role,
                sortNumber = projectPartner.sortNumber,
                country = "country"
            ),
            nameInOriginalLanguage = "nameInOriginalLanguage",
            nameInEnglish = "nameInEnglish",
            sortNumber = 1,
            address = OutputProjectAssociatedOrganizationAddress(
                country = "country",
                nutsRegion2 = "nutsRegion2",
                nutsRegion3 = "nutsRegion3",
                street = "street",
                houseNumber = "houseNumber",
                postalCode = "postalCode",
                city = "city",
                homepage = "homepage"
            ),
            contacts = listOf(
                ProjectPartnerContactDTO(
                    type = ProjectContactTypeDTO.ContactPerson,
                    title = "title",
                    firstName = "firstName",
                    lastName = "lastName",
                    email = "email",
                    telephone = "telephone"
                )
            ),
            roleDescription = setOf(InputTranslation(SystemLanguage.EN, "roleDescription"))
        )

    }

    @MockK
    lateinit var projectPartnerRepo: ProjectPartnerRepository
    @MockK
    lateinit var projectAssociatedOrganizationRepo: ProjectAssociatedOrganizationRepository
    @MockK
    lateinit var projectVersionRepo: ProjectVersionRepository

    private lateinit var projectVersionUtils: ProjectVersionUtils

    lateinit var persistence: AssociatedOrganizationPersistence

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        projectVersionUtils = ProjectVersionUtils(projectVersionRepo)
        persistence = AssociatedOrganizationPersistenceProvider(projectPartnerRepo, projectAssociatedOrganizationRepo, projectVersionUtils)
    }

    @Test
    fun `getById - current`() {
        val aoId = 1L
        every { projectAssociatedOrganizationRepo.findFirstByProjectIdAndId(projectId, aoId) } returns Optional.of(projectAssociatedOrganization)
        assertThat(persistence.getById(projectId, aoId)).isEqualTo(projectAssociatedOrganizationOutput)
    }

    @Test
    fun `getById - not found`() {
        val aoId = 1L
        every { projectAssociatedOrganizationRepo.findFirstByProjectIdAndId(projectId, aoId) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { persistence.getById(projectId, aoId) }
    }

    @Test
    fun `getById - previous version`() {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val version = "3.0"
        val aoId = 1L
        val mockPPRow: PartnerSimpleRow = mockk()
        every { mockPPRow.id } returns projectPartner.id
        every { mockPPRow.abbreviation } returns "abbreviation"
        every { mockPPRow.role } returns ProjectPartnerRoleDTO.LEAD_PARTNER
        every { mockPPRow.sortNumber } returns 1
        every { mockPPRow.country } returns "country"
        val mockAOARow: AssociatedOrganizationAddressRow = mockk()
        every { mockAOARow.id } returns projectAssociatedOrganization.id
        every { mockAOARow.country } returns "country"
        every { mockAOARow.nutsRegion2 } returns "nutsRegion2"
        every { mockAOARow.nutsRegion3 } returns "nutsRegion3"
        every { mockAOARow.street } returns "street"
        every { mockAOARow.houseNumber } returns "houseNumber"
        every { mockAOARow.postalCode } returns "postalCode"
        every { mockAOARow.city } returns "city"
        every { mockAOARow.homepage } returns "homepage"
        val mockAOCRow: AssociatedOrganizationContactRow = mockk()
        every { mockAOCRow.type } returns ProjectContactTypeDTO.ContactPerson
        every { mockAOCRow.title } returns "title"
        every { mockAOCRow.firstName } returns "firstName"
        every { mockAOCRow.lastName } returns "lastName"
        every { mockAOCRow.email } returns "email"
        every { mockAOCRow.telephone } returns "telephone"
        val mockAORow: ProjectAssociatedOrganizationRow = mockk()
        every { mockAORow.id } returns projectAssociatedOrganization.id
        every { mockAORow.nameInOriginalLanguage } returns projectAssociatedOrganization.nameInOriginalLanguage
        every { mockAORow.nameInEnglish } returns projectAssociatedOrganization.nameInEnglish
        every { mockAORow.sortNumber } returns projectAssociatedOrganization.sortNumber
        every { mockAORow.language } returns SystemLanguage.EN
        every { mockAORow.roleDescription } returns "roleDescription"

        every { projectVersionRepo.findTimestampByVersion(projectId, version) } returns timestamp
        every { projectAssociatedOrganizationRepo.getPartnerIdByProjectIdAndIdAsOfTimestamp(projectId, aoId, timestamp) } returns projectPartner.id
        every { projectPartnerRepo.findOneByIdAsOfTimestamp(projectPartner.id, timestamp) } returns mockPPRow
        every { projectAssociatedOrganizationRepo.findAssociatedOrganizationAddressesByIdAsOfTimestamp(aoId, timestamp) } returns listOf(mockAOARow)
        every { projectAssociatedOrganizationRepo.findAssociatedOrganizationContactsByIdAsOfTimestamp(aoId, timestamp) } returns listOf(mockAOCRow)
        every { projectAssociatedOrganizationRepo.findFirstByProjectIdAndId(projectId, aoId, timestamp) } returns listOf(mockAORow)

        assertThat(persistence.getById(projectId, aoId, version))
            .isEqualTo(projectAssociatedOrganizationOutput)
    }

    @Test
    fun `getById - previous version not found`() {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val version = "3.0"
        val aoId = 1L
        every { projectVersionRepo.findTimestampByVersion(projectId, version) } returns timestamp
        every { projectAssociatedOrganizationRepo.getPartnerIdByProjectIdAndIdAsOfTimestamp(projectId, aoId, timestamp) } returns null

        assertThrows<ResourceNotFoundException> { persistence.getById(projectId, aoId, version) }
    }
}
