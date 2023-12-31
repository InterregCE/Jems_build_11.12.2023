package io.cloudflight.jems.server.project.service.checklist.getInstances.verification

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.programme.service.checklist.model.ProgrammeChecklistType
import io.cloudflight.jems.server.project.authorization.ProjectChecklistAuthorization
import io.cloudflight.jems.server.project.service.checklist.ControlChecklistInstancePersistence
import io.cloudflight.jems.server.project.service.checklist.VerificationChecklistInstancePersistence
import io.cloudflight.jems.server.project.service.checklist.model.ChecklistInstanceSearchRequest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GetVerificationChecklistInstancesTest : UnitTest() {

    private val relatedToId = 2L
    private val projectId = 3L
    private val reportId = 4L

    @RelaxedMockK
    lateinit var persistence: VerificationChecklistInstancePersistence

    @MockK
    lateinit var securityService: SecurityService

    @RelaxedMockK
    lateinit var checklistAuthorization: ProjectChecklistAuthorization

    @InjectMockKs
    lateinit var getVerificationChecklistInstances: GetVerificationChecklistInstances

    @BeforeEach
    fun reset() {
        clearAllMocks()
    }

    @Test
    fun `get all verification checklist instances`() {
        val searchRequest = slot<ChecklistInstanceSearchRequest>()
        every { checklistAuthorization.canConsolidate(relatedToId) } returns true

        getVerificationChecklistInstances.getVerificationChecklistInstances(projectId, reportId)

        verify { persistence.findChecklistInstances(capture(searchRequest)) }
        Assertions.assertThat(searchRequest.captured.relatedToId).isEqualTo(reportId)
        Assertions.assertThat(searchRequest.captured.type).isEqualTo(ProgrammeChecklistType.VERIFICATION)
    }
}
