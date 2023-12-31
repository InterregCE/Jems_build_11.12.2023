package io.cloudflight.jems.server.project.service.contracting.monitoring.getProjectContractingMonitoring

import io.cloudflight.jems.server.project.repository.ProjectPersistenceProvider
import io.cloudflight.jems.server.project.service.ProjectVersionPersistence
import io.cloudflight.jems.server.project.service.contracting.ContractingValidator
import io.cloudflight.jems.server.project.service.contracting.fillEndDateWithDuration
import io.cloudflight.jems.server.project.service.contracting.fillLumpSumsList
import io.cloudflight.jems.server.project.service.contracting.model.ProjectContractingMonitoring
import io.cloudflight.jems.server.project.service.contracting.monitoring.ContractingMonitoringPersistence
import io.cloudflight.jems.server.project.service.lumpsum.ProjectLumpSumPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GetContractingMonitoringService(
    private val contractingMonitoringPersistence: ContractingMonitoringPersistence,
    private val projectPersistence: ProjectPersistenceProvider,
    private val versionPersistence: ProjectVersionPersistence,
    private val projectLumpSumPersistence: ProjectLumpSumPersistence,
) {

    @Transactional(readOnly = true)
    fun getContractingMonitoring(projectId: Long): ProjectContractingMonitoring {
        projectPersistence.getProjectSummary(projectId).let { projectSummary ->
            ContractingValidator.validateProjectStatusForModification(projectSummary)
        }

        return getProjectContractingMonitoring(projectId)
            .fillLumpSumsList ( resolveLumpSums = {
                versionPersistence.getLatestApprovedOrCurrent(projectId = projectId)
                    .let { projectLumpSumPersistence.getLumpSums(projectId = projectId, version = it) }
            } )
            .also { it.fastTrackLumpSums?.forEach { projectLumpSum ->
                projectLumpSum.installmentsAlreadyCreated = contractingMonitoringPersistence.existsSavedInstallment(
                    projectId = projectId,
                    lumpSumId = projectLumpSum.programmeLumpSumId,
                    orderNr = projectLumpSum.orderNr
                )
            } }
    }

    @Transactional(readOnly = true)
    fun getContractMonitoringDates(projectId: Long): Pair<LocalDate, LocalDate?>? =
        getProjectContractingMonitoring(projectId).let { contractMonitoring ->
            contractMonitoring.startDate?.let {
                Pair(
                    contractMonitoring.startDate,
                    contractMonitoring.endDate
                )
            }
        }

    @Transactional(readOnly = true)
    fun getProjectContractingMonitoring(projectId: Long): ProjectContractingMonitoring =
        contractingMonitoringPersistence.getContractingMonitoring(projectId)
            .fillEndDateWithDuration(resolveDuration = {
                versionPersistence.getLatestApprovedOrCurrent(projectId = projectId)
                    .let { projectPersistence.getProject(projectId = projectId, version = it).duration }
            })


}
