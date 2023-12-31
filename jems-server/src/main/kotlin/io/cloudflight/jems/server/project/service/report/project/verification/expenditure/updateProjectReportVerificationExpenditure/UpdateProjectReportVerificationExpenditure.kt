package io.cloudflight.jems.server.project.service.report.project.verification.expenditure.updateProjectReportVerificationExpenditure

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.authorization.CanEditReportVerificationExpenditure
import io.cloudflight.jems.server.project.service.report.model.partner.control.expenditure.ParkExpenditureData
import io.cloudflight.jems.server.project.service.report.model.project.verification.expenditure.ProjectReportVerificationExpenditureLine
import io.cloudflight.jems.server.project.service.report.model.project.verification.expenditure.ProjectReportVerificationExpenditureLineUpdate
import io.cloudflight.jems.server.project.service.report.partner.control.expenditure.PartnerReportParkedExpenditurePersistence
import io.cloudflight.jems.server.project.service.report.project.base.ProjectReportPersistence
import io.cloudflight.jems.server.project.service.report.project.verification.expenditure.ProjectReportVerificationExpenditurePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class UpdateProjectReportVerificationExpenditure(
    private val reportPersistence: ProjectReportPersistence,
    private val projectReportExpenditureVerificationPersistence: ProjectReportVerificationExpenditurePersistence,
    private val partnerReportParkedExpenditurePersistence: PartnerReportParkedExpenditurePersistence,
    private val generalValidator: GeneralValidatorService
): UpdateProjectReportVerificationExpenditureInteractor {

    @CanEditReportVerificationExpenditure
    @Transactional
    @ExceptionWrapper(UpdateProjectReportVerificationExpenditureException::class)
    override fun updateExpenditureVerification(
        reportId: Long,
        expenditureVerificationUpdate: List<ProjectReportVerificationExpenditureLineUpdate>,
    ): List<ProjectReportVerificationExpenditureLine> {
        val report = reportPersistence.getReportByIdUnSecured(reportId)
        if (report.status.verificationNotStartedYet() || report.status.isFinalized())
            throw VerificationNotOpen()

        validateVerificationCommentLength(expenditureVerificationUpdate)

        val existingExpenditures = projectReportExpenditureVerificationPersistence
            .getProjectReportExpenditureVerification(reportId)

        val parkedOldIds = existingExpenditures.getParkedIds()
        val unParkedOldIds = existingExpenditures.getNotParkedIds()

        return projectReportExpenditureVerificationPersistence.updateProjectReportExpenditureVerification(
            projectReportId = reportId, expenditureVerificationUpdate
        ).also {
            updateParkedItems(
                projectReportId = reportId,
                parkedOldIds = parkedOldIds,
                unparkedOldIds = unParkedOldIds,
                newVerifications = it
            )
        }
    }

    private fun updateParkedItems(
        projectReportId: Long,
        parkedOldIds: Set<Long>,
        unparkedOldIds: Set<Long>,
        newVerifications: Collection<ProjectReportVerificationExpenditureLine>,
    ) {
        val parkedNew = newVerifications.getParkedIds()
        val unParkedNew = newVerifications.getNotParkedIds()

        val newlyParked = parkedNew.minus(parkedOldIds)
        val newlyUnParkedExpenditureIds = unParkedNew.minus(unparkedOldIds)

        partnerReportParkedExpenditurePersistence.parkExpenditures(
            newVerifications.filter { it.expenditure.id in newlyParked }.toParkData(projectReportId)
        )
        partnerReportParkedExpenditurePersistence.unParkExpenditures(newlyUnParkedExpenditureIds)
    }

    private fun Collection<ProjectReportVerificationExpenditureLine>.getParkedIds() =
        filter { it.parked }.map { it.expenditure.id }.toSet()

    private fun Collection<ProjectReportVerificationExpenditureLine>.getNotParkedIds() =
        filter { !it.parked }.map { it.expenditure.id }.toSet()

    private fun Collection<ProjectReportVerificationExpenditureLine>.toParkData(projectReportId: Long) = map {
        ParkExpenditureData(
            expenditureId = it.expenditure.id,
            // partner report, where this item was parked for the first time (if parked more times, we are interested in first)
            originalReportId = it.expenditure.parkingMetadata?.reportOfOriginId
                ?: it.expenditure.partnerReportId,
            // project report, in which item was parked (not the first time, if parked more times, we are interested in last one)
            parkedInProjectReportId = projectReportId,
            originalNumber = it.expenditure.parkingMetadata?.originalExpenditureNumber ?: it.expenditure.number,
            parkedOn = ZonedDateTime.now(),
        )
    }

    private fun validateVerificationCommentLength(expenditureVerificationList: List<ProjectReportVerificationExpenditureLineUpdate>) {
        generalValidator.throwIfAnyIsInvalid(
            *expenditureVerificationList.mapIndexed { index, it ->
                generalValidator.maxLength(it.verificationComment, 1000, "verificationComment[$index]")
            }.toTypedArray(),
        )
    }
}
