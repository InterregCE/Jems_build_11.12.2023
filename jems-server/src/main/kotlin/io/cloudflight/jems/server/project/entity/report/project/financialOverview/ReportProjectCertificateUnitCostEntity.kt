package io.cloudflight.jems.server.project.entity.report.project.financialOverview

import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeUnitCostEntity
import io.cloudflight.jems.server.project.entity.report.project.ProjectReportEntity
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity(name = "report_project_certificate_unit_cost")
class ReportProjectCertificateUnitCostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id")
    @field:NotNull
    val reportEntity: ProjectReportEntity,

    @ManyToOne(optional = false)
    @field:NotNull
    val programmeUnitCost: ProgrammeUnitCostEntity,

    @field:NotNull val total: BigDecimal,
    @field:NotNull var current: BigDecimal,
    @field:NotNull val previouslyReported: BigDecimal,
    @field:NotNull val previouslyVerified: BigDecimal,
    @field:NotNull var currentVerified: BigDecimal,

)
