package io.cloudflight.jems.server.project.service.workpackage.activity.update_activity

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.CS
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.EN
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage.SK
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.common.exception.I18nValidationException
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerRole
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerSummary
import io.cloudflight.jems.server.project.service.workpackage.WorkPackagePersistence
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivity
import io.cloudflight.jems.server.project.service.workpackage.activity.model.WorkPackageActivityDeliverable
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Sort
import java.util.stream.Collectors

@ExtendWith(MockKExtension::class)
internal class UpdateActivityTest {

    companion object {
        val activity1 = WorkPackageActivity(
            workPackageId = 1L,
            title = setOf(
                InputTranslation(language = EN, translation = null),
                InputTranslation(language = CS, translation = ""),
                InputTranslation(language = SK, translation = "sk_title"),
            ),
            description = setOf(
                InputTranslation(language = EN, translation = "en_desc"),
                InputTranslation(language = CS, translation = null),
                InputTranslation(language = SK, translation = "sk_desc"),
            ),
            startPeriod = 1,
            endPeriod = 3,
            deliverables = listOf(
                WorkPackageActivityDeliverable(
                    period = 1,
                    description = setOf(InputTranslation(language = EN, translation = "en_deliv_desc"))
                )
            ),
            partnerIds = setOf(3)
        )

        val projectPartnerIds = listOf(
            ProjectPartnerSummary(id = 3, abbreviation = "lp1", role = ProjectPartnerRole.LEAD_PARTNER, active = true,),
            ProjectPartnerSummary(id = 5, abbreviation = "p2", role = ProjectPartnerRole.PARTNER, active = true)
        )
    }

    @MockK
    lateinit var persistence: WorkPackagePersistence
    @MockK
    lateinit var partnerPersistence: PartnerPersistence

    @InjectMockKs
    lateinit var updateActivity: UpdateActivity

    @MockK
    lateinit var veryBigActivitiesList: List<WorkPackageActivity>
    @MockK
    lateinit var veryBigDeliverablesList: List<WorkPackageActivityDeliverable>

    @Test
    fun updateActivitiesForWorkPackage() {
        every { persistence.updateWorkPackageActivities(1L, any()) } returnsArgument 1
        every { partnerPersistence.findAllByProjectIdForDropdown(1L, Sort.unsorted()) } returns projectPartnerIds
        assertThat(updateActivity.updateActivitiesForWorkPackage(1L, 1L, listOf(activity1))).containsExactly(activity1)
    }

    @Test
    fun `update activities when max allowed activities amount reached`() {
        every { veryBigActivitiesList.size } returns 21
        val exception = assertThrows<I18nValidationException> { updateActivity.updateActivitiesForWorkPackage(1L, 2L, veryBigActivitiesList) }
        assertThat(exception.i18nKey).isEqualTo("workPackage.activity.max.allowed.reached")
    }

    @Test
    fun `update activities - empty activities should pass`() {
        every { persistence.updateWorkPackageActivities(3L, any()) } returns emptyList()
        every { partnerPersistence.findAllByProjectIdForDropdown(1L, Sort.unsorted()) } returns projectPartnerIds
        assertDoesNotThrow { updateActivity.updateActivitiesForWorkPackage(1L, 3L, emptyList()) }
    }

    @Test
    fun `update activities - empty deliverables should pass`() {
        every { persistence.updateWorkPackageActivities(4L, any()) } returns emptyList()
        every { partnerPersistence.findAllByProjectIdForDropdown(1L, Sort.unsorted()) } returns projectPartnerIds
        assertDoesNotThrow { updateActivity.updateActivitiesForWorkPackage(
            1L,
            4L,
            listOf(WorkPackageActivity(1L, 4L, deliverables = emptyList()))
        ) }
    }

    @Test
    fun `update activities when max allowed deliverables amount reached`() {
        every { veryBigDeliverablesList.size } returns 21
        val toBeSaved = listOf(WorkPackageActivity(1L, 5L, deliverables = veryBigDeliverablesList))
        val exception = assertThrows<I18nValidationException> { updateActivity.updateActivitiesForWorkPackage(1L, 5L, toBeSaved) }
        assertThat(exception.i18nKey).isEqualTo("workPackage.activity.deliverables.max.allowed.reached")
    }

    @Test
    fun `update activities when start period is after end period`() {
        val toBeSaved = listOf(WorkPackageActivity(1L, 6L, startPeriod = 2568, endPeriod = 2567))
        val exception = assertThrows<I18nValidationException> { updateActivity.updateActivitiesForWorkPackage(1L, 6L, toBeSaved) }
        assertThat(exception.i18nKey).isEqualTo("workPackage.activity.startPeriod.is.after.endPeriod")
    }

    @Test
    fun `update activities without partners assigned`() {
        every { persistence.updateWorkPackageActivities(1L, any()) } returnsArgument 1
        every { partnerPersistence.findAllByProjectIdForDropdown(1L, Sort.unsorted()) } returns emptyList()
        val activity = activity1.copy(partnerIds = emptySet())
        assertThat(updateActivity.updateActivitiesForWorkPackage(1L, 1L, listOf(activity)))
            .containsExactly(activity)
    }

    @Test
    fun `update activities when partner is not assigned to project`() {
        every { partnerPersistence.findAllByProjectIdForDropdown(1L, Sort.unsorted()) } returns projectPartnerIds
        val exception = assertThrows<PartnersNotFound> {
            updateActivity.updateActivitiesForWorkPackage(1L, 2L, listOf(activity1.copy(partnerIds = setOf(3, 10))))
        }
        assertThat(exception.message).isEqualTo("PartnerIds: 10")
    }

    @Test
    fun `update activities when title is too long`() {
        val title = setOf(InputTranslation(
            language = CS,
            translation = getStringOfLength(201)
        ))
        val toBeSaved = listOf(WorkPackageActivity(1L, 7L, title = title))
        val exception = assertThrows<I18nValidationException> { updateActivity.updateActivitiesForWorkPackage(1L, 7L, toBeSaved) }
        assertThat(exception.i18nKey).isEqualTo("workPackage.activity.title.size.too.long")
    }

    @Test
    fun `update activities when description is too long`() {
        val description = setOf(InputTranslation(
            language = SK,
            translation = getStringOfLength(1001)
        ))
        val toBeSaved = listOf(WorkPackageActivity(1L, 8L, description = description))
        val exception = assertThrows<I18nValidationException> { updateActivity.updateActivitiesForWorkPackage(1L, 8L, toBeSaved) }
        assertThat(exception.i18nKey).isEqualTo("workPackage.activity.description.size.too.long")
    }

    @Test
    fun `update activity deliverables when description is too long`() {
        every { partnerPersistence.findAllByProjectIdForDropdown(1L, Sort.unsorted()) } returns projectPartnerIds
        every { persistence.updateWorkPackageActivities(any(), any()) } returnsArgument 1
        val description = InputTranslation(
            language = EN,
            translation = getStringOfLength(1001)
        )
        val toBeSaved = listOf(WorkPackageActivity(1L, 9L, deliverables = listOf(WorkPackageActivityDeliverable(description = setOf(description)))))
        val exception = assertThrows<I18nValidationException> { updateActivity.updateActivitiesForWorkPackage(1L, 9L, toBeSaved) }
        assertThat(exception.i18nKey).isEqualTo("workPackage.activity.deliverable.description.size.too.long")
    }

    private fun getStringOfLength(length: Int): String =
        IntArray(length).map { "x" }.stream().collect(Collectors.joining())

}
