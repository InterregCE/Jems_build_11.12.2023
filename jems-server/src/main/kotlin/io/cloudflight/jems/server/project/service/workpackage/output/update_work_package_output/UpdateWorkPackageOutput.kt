package io.cloudflight.jems.server.project.service.workpackage.output.update_work_package_output

import io.cloudflight.jems.server.project.authorization.CanUpdateProjectWorkPackage
import io.cloudflight.jems.server.project.service.workpackage.WorkPackagePersistence
import io.cloudflight.jems.server.project.service.workpackage.model.WorkPackageOutput
import io.cloudflight.jems.server.project.service.workpackage.model.WorkPackageOutputUpdate
import io.cloudflight.jems.server.project.service.workpackage.output.validateWorkPackageOutputsLimit
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateWorkPackageOutput(
    private val workPackagePersistence: WorkPackagePersistence
) : UpdateWorkPackageOutputInteractor {

    @CanUpdateProjectWorkPackage
    @Transactional
    override fun updateWorkPackageOutputs(
        workPackageId: Long,
        workPackageOutputUpdate: Set<WorkPackageOutputUpdate>,
    ): Set<WorkPackageOutput> {
        validateWorkPackageOutputsLimit(workPackageOutputUpdate)
        return workPackagePersistence.updateWorkPackageOutputs(workPackageId, workPackageOutputUpdate)
    }
}