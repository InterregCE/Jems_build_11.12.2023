package io.cloudflight.jems.server.project.service.get_project

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.project.service.ProjectPersistence
import io.cloudflight.jems.server.project.service.model.ProjectCallSettings
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class GetProjectTest : UnitTest() {

    companion object {
        val startDate: ZonedDateTime = ZonedDateTime.now().minusDays(2)
        val endDate: ZonedDateTime = ZonedDateTime.now().plusDays(2)

        val callSettings = ProjectCallSettings(
            callId = 15,
            callName = "Call 15",
            startDate = startDate,
            endDate = endDate,
            lengthOfPeriod = 6,
            flatRates = emptySet(),
            lumpSums = emptyList(),
            unitCosts = emptyList(),
        )
    }

    @MockK
    lateinit var persistence: ProjectPersistence

    @InjectMockKs
    lateinit var getProject: GetProject

    @Test
    fun getProjectCallSettings() {
        every { persistence.getProjectCallSettingsForProject(1L) } returns callSettings
        assertThat(getProject.getProjectCallSettings(1L)).isEqualTo(callSettings.copy())
    }

}