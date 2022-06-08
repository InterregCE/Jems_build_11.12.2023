package io.cloudflight.jems.api.project

import io.cloudflight.jems.api.project.dto.assignment.ProjectUserDTO
import io.cloudflight.jems.api.project.dto.assignment.UpdateProjectUserDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@Api("Project User")
interface ProjectUserApi {

    companion object {
        private const val ENDPOINT_API_PROJECT_USER = "/api/projectUser"
    }

    @ApiOperation("Retrieves projects with already assigned users")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping("$ENDPOINT_API_PROJECT_USER/list")
    fun listProjectsWithAssignedUsers(pageable: Pageable): Page<ProjectUserDTO>

    @ApiOperation("Assigns a list of Users for monitoring to Projects")
    @PutMapping("$ENDPOINT_API_PROJECT_USER/updateProjectUserAssignments", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectUserAssignments(@RequestBody projectUsers: Set<UpdateProjectUserDTO>)

}