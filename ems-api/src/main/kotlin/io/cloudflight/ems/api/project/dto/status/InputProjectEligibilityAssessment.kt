package io.cloudflight.ems.api.project.dto.status

import io.cloudflight.ems.api.project.dto.status.ProjectEligibilityAssessmentResult
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class InputProjectEligibilityAssessment(

    @field:NotNull(message = "project.status.eligibilityassessment.should.not.be.empty")
    val result: ProjectEligibilityAssessmentResult?,

    @field:Size(max = 1000, message = "project.eligibilityassessment.note.size.too.long")
    val note: String? = null

)