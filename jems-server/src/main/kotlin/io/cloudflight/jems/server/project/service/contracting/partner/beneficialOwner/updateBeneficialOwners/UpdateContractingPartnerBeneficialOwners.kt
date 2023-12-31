package io.cloudflight.jems.server.project.service.contracting.partner.beneficialOwner.updateBeneficialOwners

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.authorization.CanUpdateProjectContractingPartner
import io.cloudflight.jems.server.project.service.contracting.ContractingValidator
import io.cloudflight.jems.server.project.service.contracting.partner.beneficialOwner.ContractingPartnerBeneficialOwner
import io.cloudflight.jems.server.project.service.contracting.partner.beneficialOwner.ContractingPartnerBeneficialOwnersPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateContractingPartnerBeneficialOwners(
    private val beneficialOwnersPersistence: ContractingPartnerBeneficialOwnersPersistence,
    private val generalValidator: GeneralValidatorService,
    private val validator: ContractingValidator
) : UpdateContractingPartnerBeneficialOwnersInteractor {

    companion object {
        const val MAX_NUM_BENEFICIAL_OWNERS = 30
        const val MAX_NAME_LENGTH = 50
        const val MAX_VAT_LENGTH = 50
    }

    @CanUpdateProjectContractingPartner
    @Transactional
    @ExceptionWrapper(UpdateContractingPartnerBeneficialOwnersException::class)
    override fun updateBeneficialOwners(
        projectId: Long,
        partnerId: Long,
        beneficialOwners: List<ContractingPartnerBeneficialOwner>
    ): List<ContractingPartnerBeneficialOwner> {
        validator.validatePartnerLock(partnerId)
        if (beneficialOwners.size > MAX_NUM_BENEFICIAL_OWNERS)
            throw MaxAmountOfBeneficialOwnersReachedException(MAX_NUM_BENEFICIAL_OWNERS)
        beneficialOwners.validateBeneficialOwners()
        return beneficialOwnersPersistence.updateBeneficialOwners(projectId, partnerId, beneficialOwners)
    }

    private fun List<ContractingPartnerBeneficialOwner>.validateBeneficialOwners() {
        generalValidator.throwIfAnyIsInvalid(
            *mapIndexed { index, it ->
                generalValidator.maxLength(it.firstName, MAX_NAME_LENGTH, "firstName[$index]")
            }.toTypedArray(),
            *mapIndexed { index, it ->
                generalValidator.maxLength(it.lastName, MAX_NAME_LENGTH, "lastName[$index]")
            }.toTypedArray(),
            *mapIndexed { index, it ->
                generalValidator.maxLength(it.vatNumber, MAX_VAT_LENGTH, "vatNumber[$index]")
            }.toTypedArray(),
            *mapIndexed { index, it ->
                generalValidator.notBlank(it.vatNumber, "vatNumber[$index]")
            }.toTypedArray(),
        )
    }
}
