package io.cloudflight.jems.server.project.repository.workpackage

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.CS
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.EN
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.SK
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackageSimple
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.indicator.OutputIndicatorEntity
import io.cloudflight.jems.server.programme.repository.indicator.OutputIndicatorRepository
import io.cloudflight.jems.server.project.entity.workpackage.WorkPackageDetailRow
import io.cloudflight.jems.server.project.entity.workpackage.WorkPackageEntity
import io.cloudflight.jems.server.project.entity.workpackage.WorkPackageRow
import io.cloudflight.jems.server.project.entity.workpackage.WorkPackageTransl
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityPartnerEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityPartnerId
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityPartnerRow
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityRow
import io.cloudflight.jems.server.project.entity.workpackage.activity.WorkPackageActivityTranslationEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageActivityDeliverableTranslationEntity
import io.cloudflight.jems.server.project.entity.workpackage.activity.deliverable.WorkPackageDeliverableRow
import io.cloudflight.jems.server.project.entity.workpackage.investment.WorkPackageInvestmentRow
import io.cloudflight.jems.server.project.entity.workpackage.output.OutputRowWithTranslations
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputEntity
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputId
import io.cloudflight.jems.server.project.entity.workpackage.output.WorkPackageOutputRow
import io.cloudflight.jems.server.project.repository.ProjectRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionRepository
import io.cloudflight.jems.server.project.repository.ProjectVersionUtils
import io.cloudflight.jems.server.project.repository.partneruser.UserPartnerCollaboratorRepository
import io.cloudflight.jems.server.project.repository.projectuser.UserProjectCollaboratorRepository
import io.cloudflight.jems.server.project.repository.workpackage.activity.WorkPackageActivityPartnerRepository
import io.cloudflight.jems.server.project.repository.workpackage.activity.WorkPackageActivityRepository
import io.cloudflight.jems.server.project.repository.workpackage.investment.WorkPackageInvestmentRepository
import io.cloudflight.jems.server.project.repository.workpackage.output.WorkPackageOutputRepository
import io.cloudflight.jems.server.project.service.model.Address
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivity
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivityDeliverable
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivitySummary
import io.cloudflight.jems.server.project.service.workpackage.model.ProjectWorkPackage
import io.cloudflight.jems.server.project.service.workpackage.model.ProjectWorkPackageFull
import io.cloudflight.jems.server.project.service.workpackage.model.WorkPackageInvestment
import io.cloudflight.jems.server.project.service.workpackage.output.model.WorkPackageOutput
import io.cloudflight.jems.server.utils.partner.ProjectPartnerTestUtil.Companion.project
import io.cloudflight.jems.server.utils.partner.activityEntity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Optional

class ProjectWorkPackagePersistenceProviderGetTest : UnitTest() {

    companion object {
        private const val WORK_PACKAGE_ID = 1L
        private const val WORK_PACKAGE_ID_2 = 654L
        private const val PROJECT_ID = 64L

        private const val activityId1 = 3L
        private const val activityId2 = 2L

        private const val deliverable2_1_id = 2L
        private const val deliverable2_2_id = 1L

        private val outputId1 = WorkPackageOutputId(workPackageId = WORK_PACKAGE_ID, outputNumber = 1)
        private val outputId2 = WorkPackageOutputId(workPackageId = WORK_PACKAGE_ID, outputNumber = 2)

        private fun trIdAct(activityEntity: WorkPackageActivityEntity, lang: SystemLanguage) =
            TranslationId(
                sourceEntity = activityEntity,
                language = lang
            )

        private fun trIdActDel(deliverableEntity: WorkPackageActivityDeliverableEntity, lang: SystemLanguage) =
            TranslationId(
                sourceEntity = deliverableEntity,
                language = lang
            )

        const val activityProjectPartnerId = 3L
        val activityPartnerMock: WorkPackageActivityPartnerEntity = mockk()
        var activity1 = WorkPackageActivityEntity(
            id = activityId1,
            workPackage = WorkPackageEntity(id = WORK_PACKAGE_ID, number = 10, project = project, deactivated = false),
            activityNumber = 1,
            startPeriod = 4,
            endPeriod = 6,
            partners = mutableSetOf(activityPartnerMock),
            deactivated = false,
        )
        val activity1Partner1 = WorkPackageActivityPartnerEntity(
            WorkPackageActivityPartnerId(
                activity = activity1,
                projectPartnerId = activityProjectPartnerId
            )
        )
        val activity1_model = WorkPackageActivity(
            id = activityId1,
            workPackageId = 1L,
            workPackageNumber = 10,
            activityNumber = 1,
            startPeriod = 4,
            endPeriod = 6,
            deactivated = false,
            partnerIds = setOf(activity1Partner1.id.projectPartnerId)
        )
        val activity2 = WorkPackageActivityEntity(
            id = activityId2,
            workPackage = WorkPackageEntity(id = WORK_PACKAGE_ID, number = 10, project = project, deactivated = false),
            activityNumber = 2,
            startPeriod = 1,
            endPeriod = 3,
            translatedValues = mutableSetOf(),
            deactivated = false,
            deliverables = mutableSetOf()
        ).apply {
            deliverables.addAll(mutableSetOf(
                WorkPackageActivityDeliverableEntity(
                    id = 1L,
                    deliverableNumber = 2,
                    startPeriod = 2,
                    deactivated = false,
                    workPackageActivity = this
                ),
                WorkPackageActivityDeliverableEntity(
                    id = 2L,
                    deliverableNumber = 1,
                    startPeriod = 1,
                    deactivated = false,
                    translatedValues = mutableSetOf(),
                    workPackageActivity = this
                ).apply {
                    translatedValues.addAll(
                        setOf(
                            WorkPackageActivityDeliverableTranslationEntity(
                                translationId = trIdActDel(this, SK),
                                description = "sk_deliverable_desc"
                            ),
                            WorkPackageActivityDeliverableTranslationEntity(
                                translationId = trIdActDel(this, CS),
                                description = ""
                            ),
                            WorkPackageActivityDeliverableTranslationEntity(
                                translationId = trIdActDel(this, EN),
                                description = null
                            )
                        )
                    )
                }

            ))
            translatedValues.addAll(
                setOf(
                    WorkPackageActivityTranslationEntity(
                        translationId = trIdAct(this, SK),
                        title = "sk_title",
                        description = ""
                    ),
                    WorkPackageActivityTranslationEntity(
                        translationId = trIdAct(this, CS),
                        title = null,
                        description = "cs_desc"
                    ),
                    WorkPackageActivityTranslationEntity(
                        translationId = trIdAct(this, EN),
                        title = " ",
                        description = " "
                    )
                )
            )
        }

        val activity2_model = WorkPackageActivity(
            id = activityId2,
            workPackageId = 1L,
            workPackageNumber = 10,
            activityNumber = 2,
            title = setOf(
                InputTranslation(language = SK, translation = "sk_title"),
            ),
            description = setOf(
                InputTranslation(language = CS, translation = "cs_desc"),
            ),
            startPeriod = 1,
            endPeriod = 3,
            deactivated = false,
            deliverables = listOf(
                WorkPackageActivityDeliverable(
                    id = deliverable2_1_id,
                    deliverableNumber = 1,
                    period = 1,
                    description = setOf(
                        InputTranslation(
                            language = SK,
                            translation = "sk_deliverable_desc"
                        ),
                    ),
                    deactivated = false
                ),
                WorkPackageActivityDeliverable(
                    id = deliverable2_2_id,
                    deliverableNumber = 2,
                    period = 2,
                    deactivated = false
                )
            )
        )

        private const val INDICATOR_ID = 30L
        private val indicatorOutput = OutputIndicatorEntity(
            id = INDICATOR_ID,
            identifier = "ID.30",
            code = "tst",
            programmePriorityPolicyEntity = null,
            resultIndicatorEntity = null,
            translatedValues = mutableSetOf()
        )

        val output1 = WorkPackageOutputEntity(
            outputId = outputId1,
            periodNumber = 1,
            programmeOutputIndicatorEntity = indicatorOutput,
        )
        val output1_model = WorkPackageOutput(
            workPackageId = 1L,
            outputNumber = 1,
            periodNumber = 1,
            programmeOutputIndicatorId = INDICATOR_ID,
            programmeOutputIndicatorIdentifier = "ID.30",
            deactivated = false,
        )
        val output2 = WorkPackageOutputEntity(
            outputId = outputId2,
            periodNumber = 2,
        )
        val output2_model = WorkPackageOutput(
            workPackageId = 1L,
            outputNumber = 2,
            periodNumber = 2,
            deactivated = false,
        )

        val workPackageWithActivities = WorkPackageEntity(
            id = WORK_PACKAGE_ID,
            project = project,
            number = 10,
            deactivated = false,
            activities = mutableListOf(activity2, activity1), // for testing sorting
            translatedValues = mutableSetOf()
        ).apply {
            translatedValues.addAll(
                setOf(
                    WorkPackageTransl(
                        translationId = TranslationId(this, CS),
                        name = "WP CS name"
                    )
                )
            )
        }

        val output = WorkPackageOutput(
            workPackageId = 1L,
            outputNumber = 1,
            title = setOf(

                InputTranslation(language = SK, translation = "sk_title"),
            ),
            description = setOf(
                InputTranslation(language = EN, translation = "en_desc"),
                InputTranslation(language = SK, translation = "sk_desc"),
            ),
            periodNumber = 3,
            programmeOutputIndicatorId = INDICATOR_ID,
            targetValue = BigDecimal.TEN,
            deactivated = false,
        )

    }

    @MockK
    lateinit var repository: WorkPackageRepository

    @MockK
    lateinit var repositoryActivity: WorkPackageActivityRepository

    @MockK
    lateinit var repositoryActivityPartner: WorkPackageActivityPartnerRepository

    @MockK
    lateinit var repositoryOutput: WorkPackageOutputRepository

    @MockK
    lateinit var investmentRepository: WorkPackageInvestmentRepository

    @MockK
    lateinit var outputIndicatorRepository: OutputIndicatorRepository

    @MockK
    lateinit var projectVersionRepo: ProjectVersionRepository

    @MockK
    lateinit var projectRepository: ProjectRepository

    @MockK
    lateinit var projectCollaboratorRepository: UserProjectCollaboratorRepository

    @MockK
    lateinit var partnerCollaboratorRepository: UserPartnerCollaboratorRepository

    private lateinit var projectVersionUtils: ProjectVersionUtils

    private lateinit var persistence: WorkPackagePersistenceProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        projectVersionUtils = ProjectVersionUtils(projectVersionRepo)
        persistence = WorkPackagePersistenceProvider(
            repository,
            repositoryActivity,
            repositoryActivityPartner,
            repositoryOutput,
            investmentRepository,
            outputIndicatorRepository,
            projectVersionUtils,
            projectRepository,
            projectCollaboratorRepository,
            partnerCollaboratorRepository
        )
    }

    @Test
    fun `get full-rich work packages`() {
        val emptyWP = WorkPackageEntity(
            id = WORK_PACKAGE_ID_2,
            project = project,
            number = 2,
            deactivated = false
        )
        every { repository.findAllByProjectId(eq(1)) } returns
            listOf(
                workPackageWithActivities.also {
                    it.activities.clear()
                },
                emptyWP
            )
        val wkPackages = setOf(WORK_PACKAGE_ID, WORK_PACKAGE_ID_2)
        val activityIds = setOf(activityId1, activityId2)
        every { activityPartnerMock.id } returns WorkPackageActivityPartnerId(activity1, activityProjectPartnerId)
        every {
            repositoryActivity.findAllByWorkPackageIdIn(wkPackages)
        } returns listOf(activity1, activity2)
        every {
            repositoryOutput.findAllByOutputIdWorkPackageIdIn(wkPackages)
        } returns listOf(output2, output1)
        every { repositoryActivityPartner.findAllByIdActivityIdIn(activityIds) } returns mutableListOf(
            activity1Partner1
        )

        val result = persistence.getWorkPackagesWithOutputsAndActivitiesByProjectId(1L, null)
        assertThat(result.size).isEqualTo(2)
        assertThat(result.map { it.id }).containsExactly(WORK_PACKAGE_ID, WORK_PACKAGE_ID_2)
        assertThat(result.map { it.workPackageNumber }).containsExactly(workPackageWithActivities.number, 2)
        assertThat(result[0].name).containsExactly(InputTranslation(CS, "WP CS name"))
        assertThat(result[0].activities).containsExactly(activity1_model, activity2_model)
        assertThat(result[0].outputs).containsExactly(output1_model, output2_model)
    }

    @Test
    fun `get full-rich work packages for previous version`() {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val version = "3.0"
        val id = 1L
        val wpId = 2L
        val activityNumber = 3
        val mockWPRow: WorkPackageRow = mockk()
        every { mockWPRow.id } returns wpId
        every { mockWPRow.language } returns EN
        every { mockWPRow.name } returns "name"
        every { mockWPRow.number } returns 3
        every { mockWPRow.specificObjective } returns "specificObjective"
        every { mockWPRow.objectiveAndAudience } returns "objectiveAndAudience"
        every { mockWPRow.deactivated } returns false
        val mockWPARow: WorkPackageActivityRow = mockk()
        every { mockWPARow.id } returns activityId1
        every { mockWPARow.workPackageId } returns wpId
        every { mockWPARow.activityNumber } returns activityNumber
        every { mockWPARow.language } returns EN
        every { mockWPARow.startPeriod } returns 1
        every { mockWPARow.endPeriod } returns 2
        every { mockWPARow.title } returns "title"
        every { mockWPARow.description } returns "description"
        every { mockWPARow.deactivated } returns false
        val mockWPAPRow: WorkPackageActivityPartnerRow = mockk()
        every { mockWPAPRow.activityId } returns activityId1
        every { mockWPAPRow.workPackageId } returns wpId
        every { mockWPAPRow.projectPartnerId } returns 5
        val mockWPDRow: WorkPackageDeliverableRow = mockk()
        every { mockWPDRow.id } returns deliverable2_1_id
        every { mockWPDRow.deliverableNumber } returns 4
        every { mockWPDRow.language } returns EN
        every { mockWPDRow.startPeriod } returns 1
        every { mockWPDRow.description } returns "description"
        every { mockWPDRow.title } returns "title"
        every { mockWPDRow.deactivated } returns false
        val mockWPORow: WorkPackageOutputRow = mockk()
        every { mockWPORow.workPackageId } returns wpId
        every { mockWPORow.outputNumber } returns 5
        every { mockWPORow.language } returns EN
        every { mockWPORow.programmeOutputIndicatorId } returns 1L
        every { mockWPORow.programmeOutputIndicatorIdentifier } returns "programmeOutputIndicatorIdentifier"
        every { mockWPORow.targetValue } returns BigDecimal.TEN
        every { mockWPORow.periodNumber } returns 1
        every { mockWPORow.title } returns "title"
        every { mockWPORow.description } returns "description"
        every { mockWPORow.deactivated } returns false


        every { projectVersionRepo.findTimestampByVersion(id, version) } returns timestamp
        every { repository.findAllByProjectIdAsOfTimestamp(id, timestamp) } returns listOf(mockWPRow)
        every {
            repositoryActivity.findAllByActivityIdWorkPackageIdAsOfTimestamp(
                setOf(wpId),
                timestamp
            )
        } returns listOf(mockWPARow)
        every {
            repositoryActivity.findAllDeliverablesByActivityIdAsOfTimestamp(
                activityId1,
                timestamp
            )
        } returns listOf(mockWPDRow)
        every { repositoryOutput.findAllByOutputIdWorkPackageIdAsOfTimestamp(setOf(wpId), timestamp) } returns listOf(
            mockWPORow
        )
        every { repositoryActivityPartner.findAllByWorkPackageIdsAsOfTimestamp(setOf(wpId), timestamp) } returns listOf(
            mockWPAPRow
        )

        // test
        val result = persistence.getWorkPackagesWithOutputsAndActivitiesByProjectId(id, version)

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo(
            ProjectWorkPackage(
                id = mockWPRow.id,
                deactivated = false,
                workPackageNumber = mockWPRow.number!!,
                name = setOf(InputTranslation(mockWPRow.language!!, mockWPRow.name)),
                specificObjective = setOf(InputTranslation(mockWPRow.language!!, mockWPRow.specificObjective)),
                objectiveAndAudience = setOf(InputTranslation(mockWPRow.language!!, mockWPRow.objectiveAndAudience)),
                activities = listOf(
                    WorkPackageActivity(
                        id = activityId1,
                        activityNumber = mockWPARow.activityNumber,
                        workPackageId = mockWPARow.workPackageId,
                        title = setOf(InputTranslation(mockWPARow.language!!, mockWPARow.title)),
                        description = setOf(InputTranslation(mockWPARow.language!!, mockWPARow.description)),
                        startPeriod = mockWPARow.startPeriod,
                        endPeriod = mockWPARow.endPeriod,
                        deactivated = false,
                        deliverables = listOf(
                            WorkPackageActivityDeliverable(
                                id = deliverable2_1_id,
                                deliverableNumber = mockWPDRow.deliverableNumber,
                                description = setOf(
                                    InputTranslation(EN, mockWPDRow.description)
                                ),
                                title = setOf(
                                    InputTranslation(EN, mockWPDRow.title)
                                ),
                                period = mockWPDRow.startPeriod,
                                deactivated = false
                            )
                        ),
                        partnerIds = setOf(5)
                    )
                ),
                outputs = listOf(
                    WorkPackageOutput(
                        workPackageId = mockWPORow.workPackageId,
                        outputNumber = mockWPORow.outputNumber,
                        programmeOutputIndicatorId = mockWPORow.programmeOutputIndicatorId,
                        programmeOutputIndicatorIdentifier = mockWPORow.programmeOutputIndicatorIdentifier,
                        targetValue = mockWPORow.targetValue,
                        periodNumber = mockWPORow.periodNumber,
                        title = setOf(InputTranslation(mockWPORow.language!!, mockWPORow.title)),
                        description = setOf(InputTranslation(mockWPORow.language!!, mockWPORow.description)),
                        deactivated = false,
                    )
                )
            )
        )
    }

    @Test
    fun `get work package activities - not-existing work package`() {
        every { repository.findById(eq(-1)) } returns Optional.empty()
        val ex = assertThrows<ResourceNotFoundException> { persistence.getWorkPackageActivitiesForWorkPackage(-1, 1L) }
        assertThat(ex.entity).isEqualTo("workPackage")
    }

    @Test
    fun `get work package activities for project`() {
        every { repository.findAllByProjectId(eq(1L), any()) } returns listOf(workPackageWithActivities)
        every { repositoryActivity.findAllByWorkPackageIdIn(setOf(workPackageWithActivities.id)) } returns listOf(
            activity1, activity2)

        val wpActivityList = persistence.getWorkPackageActivitiesForProject(1L)
        assertThat(wpActivityList).contains(
            WorkPackageActivitySummary(
                activityId = activityId1,
                workPackageNumber = workPackageWithActivities.number!!,
                activityNumber = activity1.activityNumber
            ),
            WorkPackageActivitySummary(
                activityId = activityId2,
                workPackageNumber = workPackageWithActivities.number!!,
                activityNumber = activity2.activityNumber
            ))
    }

    @Test
    fun `work package activities and deliverables are correctly mapped and sorted`() {
        every { activityPartnerMock.id } returns WorkPackageActivityPartnerId(activity1, activityProjectPartnerId)
        every { repository.findById(eq(WORK_PACKAGE_ID)) } returns Optional.of(workPackageWithActivities)
        val partnerList = mutableListOf(activity1Partner1)
        every { repositoryActivityPartner.findAllByIdActivityIdIn(listOf(WORK_PACKAGE_ID)) } returns partnerList

        assertThat(persistence.getWorkPackageActivitiesForWorkPackage(WORK_PACKAGE_ID, 1L)).containsExactly(
            activity1_model, activity2_model,
        )
    }

    @Test
    fun `work package historical activities are correctly mapped without translations`() {
        val timestamp: Timestamp = Timestamp.valueOf(LocalDateTime.now())
        every { projectVersionRepo.findTimestampByVersion(PROJECT_ID, "A") } returns timestamp
        every {
            repositoryActivity.findAllActivitiesByWorkPackageIdAsOfTimestamp(
                WORK_PACKAGE_ID,
                timestamp
            )
        } returns listOf(
            WorkPackageActivityRowImpl(activityId1, null, WORK_PACKAGE_ID, 10,1, 1, 2, null, null, false, null)
        )
        every {
            repositoryActivity.findAllDeliverablesByActivityIdAsOfTimestamp(
                activityId1,
                timestamp
            )
        } returns emptyList()
        every {
            repositoryActivityPartner.findAllByActivityIdAsOfTimestamp(
                activityId1,
                timestamp
            )
        } returns listOf(
            WorkPackageActivityPartnerRowImpl(
                activityId1,
                WORK_PACKAGE_ID,
                207L
            ),
            WorkPackageActivityPartnerRowImpl(
                activityId1,
                WORK_PACKAGE_ID,
                208L
            ),
        )

        assertThat(
            persistence.getWorkPackageActivitiesForWorkPackage(
                WORK_PACKAGE_ID,
                PROJECT_ID,
                "A"
            )
        ).containsExactly(
            WorkPackageActivity(
                id = activityId1,
                workPackageId = WORK_PACKAGE_ID,
                workPackageNumber = 10,
                activityNumber = 1,
                startPeriod = 1,
                endPeriod = 2,
                deliverables = emptyList(),
                partnerIds = setOf(207L, 208L),
                deactivated = false,
            )
        )
    }

    @Test
    fun `getWorkPackageOutputs are correctly mapped and sorted`() {
        every { repositoryOutput.findAllByOutputIdWorkPackageIdOrderByOutputIdOutputNumber(20L) } returns listOf(output2, output1)
        assertThat(persistence.getWorkPackageOutputsForWorkPackage(20L, 1L)).containsExactly(
            output1_model, output2_model
        )
    }

    @Test
    fun getWorkPackagesWithAllDataByProjectId() {
        every { projectRepository.findById(PROJECT_ID).get().periods } returns emptyList()
        every { repository.findAllByProjectId(PROJECT_ID, Sort.by(Sort.Direction.ASC, "id")) } returns listOf(workPackageWithActivities)
        val workPackageIds = setOf(WORK_PACKAGE_ID)
        every { repositoryActivity.findAllByWorkPackageIdIn(workPackageIds) } returns listOf(activityEntity)
        every { repositoryOutput.findAllByOutputIdWorkPackageIdIn(workPackageIds) } returns emptyList()
        every { investmentRepository.findInvestmentsByProjectId(PROJECT_ID) } returns emptyList()

        val workPackages = persistence.getWorkPackagesWithAllDataByProjectId(PROJECT_ID)
        assertThat(workPackages).containsExactly(
            ProjectWorkPackageFull(
                id = WORK_PACKAGE_ID,
                workPackageNumber = workPackageWithActivities.number!!,
                name = setOf(InputTranslation(CS, "WP CS name")),
                specificObjective = emptySet(),
                objectiveAndAudience = emptySet(),
                activities = listOf(WorkPackageActivity(
                    id = activityEntity.id,
                    workPackageId = WORK_PACKAGE_ID,
                    workPackageNumber = workPackageWithActivities.number!!,
                    activityNumber = activityEntity.activityNumber,
                    title = emptySet(),
                    description = emptySet(),
                    startPeriod = 1,
                    endPeriod = 3,
                    deactivated = false,
                    deliverables = emptyList()
                )),
                outputs = emptyList(),
                investments = emptyList(),
                deactivated = false
            )
        )
    }

    @ParameterizedTest(name = "getWorkPackagesByProjectId - when version is {0}")
    @ValueSource(strings = ["1.1", ""])
    fun getWorkPackagesByProjectId(versionString: String) {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val version: String? = versionString.ifEmpty { null }
        val mockWPRow: WorkPackageRow = mockk()
        val wpId = 2L
        if (version != null) {
            every { mockWPRow.id } returns wpId
            every { mockWPRow.language } returns EN
            every { mockWPRow.name } returns "name"
            every { mockWPRow.number } returns 3
            every { mockWPRow.deactivated } returns false
            every { mockWPRow.specificObjective } returns ""
            every { mockWPRow.objectiveAndAudience } returns ""
            every { projectVersionRepo.findTimestampByVersion(PROJECT_ID, version) } returns timestamp
            every { repository.findAllByProjectIdAsOfTimestamp(PROJECT_ID, timestamp) } returns listOf(mockWPRow)
        } else {
            every { repository.findAllByProjectId(PROJECT_ID) } returns listOf(workPackageWithActivities)
        }

        val workPackagesSimple = persistence.getWorkPackagesByProjectId(PROJECT_ID, version)
        if (version != null) {
            assertThat(workPackagesSimple).containsExactly(
                OutputWorkPackageSimple(
                    id = wpId,
                    number = 3,
                    name = setOf(InputTranslation(EN, "name")),
                    deactivated = false
                )
            )
        } else {
            assertThat(workPackagesSimple).containsExactly(
                OutputWorkPackageSimple(
                    id = workPackageWithActivities.id,
                    number = workPackageWithActivities.number,
                    name = setOf(InputTranslation(CS, "WP CS name")),
                    deactivated = false
                )
            )
        }
    }

    @Test
    fun `should return work packages with all the details for the specified version of the project when there is no problem`() {
        val timestamp = Timestamp.valueOf(LocalDateTime.of(2020, 8, 15, 6, 0))
        val version = "1.0"

        val mockWPRow: WorkPackageRow = mockk()
        every { mockWPRow.id } returns WORK_PACKAGE_ID
        every { mockWPRow.language } returns CS
        every { mockWPRow.name } returns "WP CS name"
        every { mockWPRow.number } returns workPackageWithActivities.number!!
        every { mockWPRow.specificObjective } returns ""
        every { mockWPRow.objectiveAndAudience } returns ""
        every { mockWPRow.deactivated } returns false

        val mockWPARow: WorkPackageActivityRow = mockk()
        every { mockWPARow.id } returns activityEntity.id
        every { mockWPARow.workPackageId } returns WORK_PACKAGE_ID
        every { mockWPARow.activityNumber } returns activityEntity.activityNumber
        every { mockWPARow.language } returns CS
        every { mockWPARow.startPeriod } returns 1
        every { mockWPARow.endPeriod } returns 3
        every { mockWPARow.title } returns "title"
        every { mockWPARow.description } returns "description"
        every { mockWPARow.deactivated } returns false
        every { mockWPARow.workPackageNumber } returns workPackageWithActivities.number!!
        every { mockWPARow.partnerId } returns 5
        val mockWPAPRow: WorkPackageActivityPartnerRow = mockk()
        every { mockWPAPRow.activityId } returns activityId1
        every { mockWPAPRow.workPackageId } returns WORK_PACKAGE_ID
        every { mockWPAPRow.projectPartnerId } returns 5

        val mockWPOutputRow: WorkPackageOutputRow = mockk()
        every { mockWPOutputRow.workPackageId } returns WORK_PACKAGE_ID
        every { mockWPOutputRow.outputNumber } returns 1
        every { mockWPOutputRow.language } returns CS
        every { mockWPOutputRow.targetValue } returns BigDecimal.TEN
        every { mockWPOutputRow.periodNumber } returns 1
        every { mockWPOutputRow.programmeOutputIndicatorId } returns null
        every { mockWPOutputRow.programmeOutputIndicatorIdentifier } returns null
        every { mockWPOutputRow.programmeOutputIndicatorLanguage } returns null
        every { mockWPOutputRow.title } returns "title output"
        every { mockWPOutputRow.description } returns "description output"
        every { mockWPOutputRow.deactivated } returns false

        val mockWPInvestmentRow: WorkPackageInvestmentRow = mockk()
        every { mockWPInvestmentRow.id } returns 1
        every { mockWPInvestmentRow.language } returns CS
        every { mockWPInvestmentRow.deactivated } returns false
        every { mockWPInvestmentRow.investmentNumber } returns 1
        every { mockWPInvestmentRow.title } returns "title investment"
        every { mockWPInvestmentRow.expectedDeliveryPeriod } returns null
        every { mockWPInvestmentRow.justificationExplanation } returns null
        every { mockWPInvestmentRow.justificationTransactionalRelevance } returns null
        every { mockWPInvestmentRow.justificationBenefits } returns null
        every { mockWPInvestmentRow.justificationPilot } returns null
        every { mockWPInvestmentRow.risk } returns "risk"
        every { mockWPInvestmentRow.documentation } returns "documentation"
        every { mockWPInvestmentRow.documentationExpectedImpacts } returns null
        every { mockWPInvestmentRow.ownershipSiteLocation } returns null
        every { mockWPInvestmentRow.ownershipRetain } returns null
        every { mockWPInvestmentRow.ownershipMaintenance } returns null
        every { mockWPInvestmentRow.country } returns "country"
        every { mockWPInvestmentRow.countryCode } returns "AT"
        every { mockWPInvestmentRow.nutsRegion2 } returns "reg2"
        every { mockWPInvestmentRow.nutsRegion2Code } returns "AT01"
        every { mockWPInvestmentRow.nutsRegion3 } returns "reg3"
        every { mockWPInvestmentRow.nutsRegion3Code } returns "AT011"
        every { mockWPInvestmentRow.street } returns "str"
        every { mockWPInvestmentRow.houseNumber } returns "nr"
        every { mockWPInvestmentRow.postalCode } returns "code"
        every { mockWPInvestmentRow.city } returns "city"

        every { projectRepository.findPeriodsByProjectIdAsOfTimestamp(PROJECT_ID, timestamp) } returns emptyList()
        every { projectVersionRepo.findTimestampByVersion(PROJECT_ID, version) } returns timestamp
        every { repository.findWorkPackagesBaseByProjectIdAsOfTimestamp(PROJECT_ID, timestamp) } returns listOf(mockWPRow)
        every { repositoryActivity.findAllActivitiesByWorkPackageIdAsOfTimestamp(WORK_PACKAGE_ID, timestamp) } returns listOf(mockWPARow)
        every { repositoryActivity.findAllDeliverablesByActivityIdAsOfTimestamp(activityEntity.id, timestamp) } returns listOf()
        every { repositoryOutput.findAllByOutputIdWorkPackageIdAsOfTimestamp(setOf(WORK_PACKAGE_ID), timestamp) } returns listOf(mockWPOutputRow)
        every { investmentRepository.findAllByWorkPackageIdAsOfTimestamp(WORK_PACKAGE_ID, timestamp) } returns listOf(mockWPInvestmentRow)

        val workPackages = persistence.getWorkPackagesWithAllDataByProjectId(PROJECT_ID, version)
        assertThat(workPackages).containsExactly(
            ProjectWorkPackageFull(
                id = WORK_PACKAGE_ID,
                workPackageNumber = workPackageWithActivities.number!!,
                name = setOf(InputTranslation(CS, "WP CS name")),
                specificObjective = emptySet(),
                objectiveAndAudience = emptySet(),
                activities = listOf(WorkPackageActivity(
                    id = activityEntity.id,
                    workPackageId = WORK_PACKAGE_ID,
                    workPackageNumber = workPackageWithActivities.number!!,
                    activityNumber = activityEntity.activityNumber,
                    title = setOf(InputTranslation(CS, "title")),
                    description = setOf(InputTranslation(CS, "description")),
                    startPeriod = 1,
                    endPeriod = 3,
                    deactivated = false,
                    deliverables = emptyList(),
                    partnerIds = setOf(5)
                )),
                outputs = listOf(
                    WorkPackageOutput(
                        workPackageId = WORK_PACKAGE_ID,
                        outputNumber = 1,
                        targetValue = BigDecimal.TEN,
                        periodNumber = 1,
                        deactivated = false,
                        title = setOf(InputTranslation(CS, "title output")),
                        description = setOf(InputTranslation(CS, "description output")),
                    )
                ),
                investments = listOf(
                    WorkPackageInvestment(
                        id = 1L,
                        investmentNumber = 1,
                        title = setOf(InputTranslation(CS, "title investment")),
                        risk = setOf(InputTranslation(CS, "risk")),
                        documentation = setOf(InputTranslation(CS, "documentation")),
                        deactivated = false,
                        address = Address("country", "AT","reg2", "AT01","reg3", "AT011", "str", "nr", "code", "city"),
                    )
                ),
                deactivated = false
            )
        )
    }

    @ParameterizedTest(name = "getAllOutputsForProjectIdSortedByNumbers - when version is {0}")
    @ValueSource(strings = ["7.1", ""])
    fun getAllOutputsForProjectIdSortedByNumbers(versionString: String) {
        val version: String? = versionString.ifEmpty { null }

        val outputRow11 = OutputRowImpl(
            workPackageId = 1L,
            workPackageNumber = 1,
            number = 1,
            title = "Out 1.1",
            language = EN,
            targetValue = BigDecimal.TEN,
            programmeOutputId = 514L,
            programmeResultId = 500L,
        )
        val outputRow12 = OutputRowImpl(
            workPackageId = 1L,
            workPackageNumber = 1,
            number = 2,
            title = "Out 1.2",
            language = EN,
            targetValue = BigDecimal.ZERO,
            programmeOutputId = 522L,
            programmeResultId = null,
        )
        val outputRow41 = OutputRowImpl(
            workPackageId = 4L,
            workPackageNumber = 4,
            number = 1,
            title = "Out 4.1",
            language = EN,
            targetValue = BigDecimal.ONE,
            programmeOutputId = null,
            programmeResultId = null,
        )

        val timestamp = Timestamp.valueOf(LocalDateTime.now())

        val data = listOf(outputRow11, outputRow12, outputRow41)
        if (version != null) {
            every { projectVersionRepo.findTimestampByVersion(PROJECT_ID, version) } returns timestamp
            every { repositoryOutput.findAllByProjectIdAsOfTimestampOrderedByNumbers(PROJECT_ID, timestamp) } returns data
        } else {
            every { repositoryOutput.findAllByProjectIdOrderedByNumbers(PROJECT_ID) } returns data
        }

        val resultingOutputs = persistence.getAllOutputsForProjectIdSortedByNumbers(PROJECT_ID, version)
        assertThat(resultingOutputs).hasSize(3)

        with(resultingOutputs[0]) {
            assertThat(workPackageId).isEqualTo(1L)
            assertThat(workPackageNumber).isEqualTo(1)
            assertThat(outputTitle).containsExactlyInAnyOrder(InputTranslation(EN, "Out 1.1"))
            assertThat(outputNumber).isEqualTo(1)
            assertThat(outputTargetValue).isEqualByComparingTo(BigDecimal.TEN)
            assertThat(programmeOutputId).isEqualTo(514L)
            assertThat(programmeResultId).isEqualTo(500L)
        }
        with(resultingOutputs[1]) {
            assertThat(workPackageId).isEqualTo(1L)
            assertThat(workPackageNumber).isEqualTo(1)
            assertThat(outputTitle).containsExactlyInAnyOrder(InputTranslation(EN, "Out 1.2"))
            assertThat(outputNumber).isEqualTo(2)
            assertThat(outputTargetValue).isEqualByComparingTo(BigDecimal.ZERO)
            assertThat(programmeOutputId).isEqualTo(522L)
            assertThat(programmeResultId).isNull()
        }
        with(resultingOutputs[2]) {
            assertThat(workPackageId).isEqualTo(4L)
            assertThat(workPackageNumber).isEqualTo(4)
            assertThat(outputTitle).containsExactlyInAnyOrder(InputTranslation(EN, "Out 4.1"))
            assertThat(outputNumber).isEqualTo(1)
            assertThat(outputTargetValue).isEqualByComparingTo(BigDecimal.ONE)
            assertThat(programmeOutputId).isNull()
            assertThat(programmeResultId).isNull()
        }
    }

    data class WorkPackageActivityRowImpl(
        override val id: Long,
        override val language: SystemLanguage?,
        override val workPackageId: Long,
        override val workPackageNumber: Int?,
        override val activityNumber: Int,
        override val startPeriod: Int?,
        override val endPeriod: Int?,
        override val title: String?,
        override val description: String?,
        override val deactivated: Boolean,
        override val partnerId: Long?,
    ) : WorkPackageActivityRow

    data class WorkPackageActivityPartnerRowImpl(
        override val activityId: Long,
        override val workPackageId: Long,
        override val projectPartnerId: Long
    ) : WorkPackageActivityPartnerRow

    data class OutputRowImpl(
        override val workPackageId: Long,
        override val workPackageNumber: Int,
        override val number: Int,
        override val title: String? = null,
        override val language: SystemLanguage?,
        override val targetValue: BigDecimal,
        override val programmeOutputId: Long?,
        override val programmeResultId: Long?
    ) : OutputRowWithTranslations

}
