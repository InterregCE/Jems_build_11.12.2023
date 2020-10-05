package io.cloudflight.jems.server.project.service

import io.cloudflight.jems.api.call.dto.CallStatus
import io.cloudflight.jems.api.call.dto.OutputCallWithDates
import io.cloudflight.jems.api.project.dto.InputProject
import io.cloudflight.jems.api.project.dto.OutputProjectSimple
import io.cloudflight.jems.api.project.dto.status.ProjectApplicationStatus
import io.cloudflight.jems.api.user.dto.OutputUser
import io.cloudflight.jems.api.user.dto.OutputUserRole
import io.cloudflight.jems.api.user.dto.OutputUserWithRole
import io.cloudflight.jems.api.programme.dto.OutputProgrammePriorityPolicySimple
import io.cloudflight.jems.api.programme.dto.ProgrammeObjectivePolicy.AdvancedTechnologies
import io.cloudflight.jems.api.programme.dto.ProgrammeObjectivePolicy.HealthcareAcrossBorders
import io.cloudflight.jems.api.project.dto.InputProjectData
import io.cloudflight.jems.api.project.dto.OutputProjectData
import io.cloudflight.jems.api.strategy.ProgrammeStrategy
import io.cloudflight.jems.server.audit.entity.AuditAction
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.call.entity.Call
import io.cloudflight.jems.server.project.entity.Project
import io.cloudflight.jems.server.project.entity.ProjectStatus
import io.cloudflight.jems.server.user.entity.User
import io.cloudflight.jems.server.user.entity.UserRole
import io.cloudflight.jems.server.exception.ResourceNotFoundException
import io.cloudflight.jems.server.call.repository.CallRepository
import io.cloudflight.jems.server.programme.entity.ProgrammePriorityPolicy
import io.cloudflight.jems.server.programme.repository.ProgrammePriorityPolicyRepository
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.ProjectStatusRepository
import io.cloudflight.jems.server.user.repository.UserRepository
import io.cloudflight.jems.server.security.ADMINISTRATOR
import io.cloudflight.jems.server.security.APPLICANT_USER
import io.cloudflight.jems.server.security.PROGRAMME_USER
import io.cloudflight.jems.server.security.model.LocalCurrentUser
import io.cloudflight.jems.server.security.service.SecurityService
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.strategy.entity.Strategy
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Optional
import java.util.stream.Collectors

class ProjectServiceTest {

    private val TEST_DATE: LocalDate = LocalDate.now()
    private val TEST_DATE_TIME = ZonedDateTime.of(TEST_DATE, LocalTime.of(10, 0), ZoneId.of("Europe/Bratislava"))

    private val UNPAGED = Pageable.unpaged()

    private val user = OutputUserWithRole(
        id = 1,
        email = "admin@admin.dev",
        name = "Name",
        surname = "Surname",
        userRole = OutputUserRole(id = 1, name = "ADMIN")
    )

    private val userWithoutRole = OutputUser(
        id = user.id,
        email = user.email,
        name = user.name,
        surname = user.surname
    )

    private val account = User(
        id = 1,
        email = "admin@admin.dev",
        name = "Name",
        surname = "Surname",
        userRole = UserRole(id = 1, name = "ADMIN"),
        password = "hash_pass"
    )

    private val statusDraft = ProjectStatus(
        id = 10,
        status = ProjectApplicationStatus.DRAFT,
        user = account,
        updated = TEST_DATE_TIME
    )

    private val statusSubmitted = ProjectStatus(
        id = 11,
        status = ProjectApplicationStatus.SUBMITTED,
        user = account,
        updated = TEST_DATE_TIME
    )

    private val dummyCall = Call(
        id = 5,
        creator = account,
        name = "call",
        priorityPolicies = setOf(ProgrammePriorityPolicy(HealthcareAcrossBorders, "HAB")),
        strategies = setOf(Strategy(ProgrammeStrategy.MediterraneanSeaBasin, true)),
        funds = emptySet(),
        startDate = ZonedDateTime.now(),
        endDate = ZonedDateTime.now(),
        status = CallStatus.PUBLISHED,
        lengthOfPeriod = 1
    )

    @RelaxedMockK
    lateinit var projectRepository: ProjectRepository

    @MockK
    lateinit var projectStatusRepository: ProjectStatusRepository

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var callRepository: CallRepository

    @RelaxedMockK
    lateinit var auditService: AuditService

    @MockK
    lateinit var securityService: SecurityService

    @MockK
    lateinit var programmePriorityPolicyRepository: ProgrammePriorityPolicyRepository

    lateinit var projectService: ProjectService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { securityService.currentUser } returns LocalCurrentUser(user, "hash_pass", emptyList())
        every { userRepository.findById(eq(user.id!!)) } returns Optional.of(account)
        every { projectStatusRepository.save(any<ProjectStatus>()) } returnsArgument 0
        projectService = ProjectServiceImpl(
            projectRepository,
            projectStatusRepository,
            callRepository,
            userRepository,
            auditService,
            programmePriorityPolicyRepository,
            securityService
        )
    }

    @Test
    fun projectRetrieval_admin() {
        every { securityService.currentUser } returns
            LocalCurrentUser(user, "hash_pass", listOf(SimpleGrantedAuthority("ROLE_$ADMINISTRATOR")))

        val projectToReturn = Project(
            id = 25,
            call = dummyCall,
            acronym = "test acronym",
            applicant = account,
            projectStatus = statusSubmitted,
            firstSubmission = statusSubmitted
        )
        every { projectRepository.findAll(UNPAGED) } returns PageImpl(listOf(projectToReturn))

        // test start
        val result = projectService.findAll(UNPAGED)

        // assertions:
        assertEquals(1, result.totalElements)

        val expectedProjects = listOf(
            OutputProjectSimple(
                id = 25,
                callName = dummyCall.name,
                acronym = "test acronym",
                firstSubmissionDate = TEST_DATE_TIME,
                lastResubmissionDate = null,
                projectStatus = ProjectApplicationStatus.SUBMITTED,
                specificObjectiveCode = null,
                programmePriorityCode = null
            )
        )
        assertIterableEquals(expectedProjects, result.get().collect(Collectors.toList()))
    }

    @Test
    fun `programme user lists submitted projects`() {
        every { securityService.currentUser } returns
            LocalCurrentUser(
                user, "hash_pass",
                listOf(SimpleGrantedAuthority("ROLE_$PROGRAMME_USER"))
            )

        projectService.findAll(UNPAGED);

        verify {
            projectRepository.findAllByProjectStatusStatusNot(
                withArg {
                    assertThat(it).isEqualTo(ProjectApplicationStatus.DRAFT)
                }, UNPAGED
            )
        }
    }

    @Test
    fun projectRetrieval_applicant() {
        every { securityService.currentUser } returns
            LocalCurrentUser(user, "hash_pass", listOf(SimpleGrantedAuthority("ROLE_$APPLICANT_USER")))
        every { projectRepository.findAllByApplicantId(eq(user.id!!), UNPAGED) } returns PageImpl(emptyList())

        assertEquals(0, projectService.findAll(UNPAGED).totalElements)
    }

    @Test
    fun projectCreation_OK() {
        every { callRepository.findById(eq(dummyCall.id!!)) } returns Optional.of(dummyCall.copy(endDate = ZonedDateTime.now().plusDays(1)))
        every { projectRepository.save(any<Project>()) } returns Project(
            612,
            dummyCall,
            "test",
            account,
            statusDraft
        )

        val result = projectService.createProject(InputProject("test", dummyCall.id))

        assertEquals(OutputCallWithDates(id = dummyCall.id!!, name = dummyCall.name, startDate = dummyCall.startDate, endDate = dummyCall.endDate), result.call)
        assertEquals(result.acronym, "test")
        assertEquals(result.firstSubmission, null)
        assertEquals(result.lastResubmission, null)
        assertEquals(result.projectStatus.id, 10)
        assertEquals(result.projectStatus.status, ProjectApplicationStatus.DRAFT)
        assertEquals(result.projectStatus.updated, TEST_DATE_TIME)

        verifyAudit("612")
    }

    @Test
    fun projectCreation_withoutUser() {
        every { userRepository.findById(eq(user.id!!)) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { projectService.createProject(InputProject("test", dummyCall.id)) }
    }

    @Test
    fun projectGet_OK() {
        every { projectRepository.findById(eq(1)) } returns
            Optional.of(Project(1, dummyCall, "test", account, statusSubmitted, statusSubmitted))

        val result = projectService.getById(1);

        assertThat(result).isNotNull()
        assertThat(result.id).isEqualTo(1);
        assertThat(result.acronym).isEqualTo("test")
        assertThat(result.projectStatus.id).isEqualTo(11)
        assertThat(result.firstSubmission?.id).isEqualTo(11)
        assertThat(result.firstSubmission?.updated).isEqualTo(TEST_DATE_TIME)
    }

    @Test
    fun projectGet_notExisting() {
        every { projectRepository.findById(eq(-1)) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { projectService.getById(-1) }
    }

    private fun verifyAudit(projectIdExpected: String) {
        val event = slot<AuditCandidate>()

        verify { auditService.logEvent(capture(event)) }
        with(event) {
            assertEquals(projectIdExpected, captured.projectId)
            assertEquals(AuditAction.APPLICATION_STATUS_CHANGED, captured.action)
        }
    }

    private val projectData = InputProjectData(
        acronym = "new acronym",
        title = "new title",
        duration = 15,
        intro = null,
        introProgrammeLanguage = "english text",
        specificObjective = AdvancedTechnologies
    )

    @Test
    fun projectUpdate_notExisting() {
        every { projectRepository.findById(eq(-1)) } returns Optional.empty()
        val exception = assertThrows<ResourceNotFoundException> { projectService.update(-1, projectData) }
        assertThat(exception.entity).isEqualTo("project")
    }

    @Test
    fun projectUpdate_successful() {
        val projectToReturn = Project(
            id = 1,
            call = dummyCall,
            acronym = "test acronym",
            applicant = account,
            projectStatus = statusSubmitted,
            firstSubmission = statusSubmitted
        )
        every { projectRepository.findById(eq(1)) } returns Optional.of(projectToReturn)
        every { projectRepository.save(any<Project>()) } returnsArgument 0
        every { programmePriorityPolicyRepository.findById(eq(AdvancedTechnologies)) } returns
            Optional.of(ProgrammePriorityPolicy(programmeObjectivePolicy = AdvancedTechnologies, code = "AT"))

        val expectedData = OutputProjectData(
            title = projectData.title,
            duration = projectData.duration,
            intro = projectData.intro,
            introProgrammeLanguage = projectData.introProgrammeLanguage,
            specificObjective = OutputProgrammePriorityPolicySimple(programmeObjectivePolicy = AdvancedTechnologies, code = "AT"),
            programmePriority = null
        )

        val result = projectService.update(1, projectData)
        assertThat(result.projectData).isEqualTo(expectedData)
        assertThat(result.acronym).isEqualTo(projectData.acronym)
    }

}