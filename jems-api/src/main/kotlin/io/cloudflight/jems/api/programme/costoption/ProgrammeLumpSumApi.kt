package io.cloudflight.jems.api.programme.costoption

import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Api("Programme Cost Option")
@RequestMapping("/api/costOption/lumpSum")
interface ProgrammeLumpSumApi {

    @ApiOperation("Retrieve all programme lump sums")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping
    fun getProgrammeLumpSums(pageable: Pageable): Page<ProgrammeLumpSumDTO>


    @ApiOperation("Retrieve programme lump sum by id")
    @GetMapping("/lumpSum/{id}")
    fun getProgrammeLumpSum(@PathVariable id: Long): ProgrammeLumpSumDTO

    @ApiOperation("Create programme lump sum")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createProgrammeLumpSum(@RequestBody lumpSum: ProgrammeLumpSumDTO): ProgrammeLumpSumDTO

    @ApiOperation("Update existing programme lump sum")
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProgrammeLumpSum(@RequestBody lumpSum: ProgrammeLumpSumDTO): ProgrammeLumpSumDTO

    @ApiOperation("Delete programme lump sum")
    @DeleteMapping("/{lumpSumId}")
    fun deleteProgrammeLumpSum(@PathVariable lumpSumId: Long)

}