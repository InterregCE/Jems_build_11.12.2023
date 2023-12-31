package io.cloudflight.jems.server.project.service.partner.model

data class ProjectPartnerBudgetOptions(
    val partnerId: Long,
    val officeAndAdministrationOnStaffCostsFlatRate: Int? = null,
    val officeAndAdministrationOnDirectCostsFlatRate: Int? = null,
    val travelAndAccommodationOnStaffCostsFlatRate: Int? = null,
    val staffCostsFlatRate: Int? = null,
    val otherCostsOnStaffCostsFlatRate: Int? = null
){
    fun isEmpty() =
        officeAndAdministrationOnStaffCostsFlatRate == null
            && officeAndAdministrationOnDirectCostsFlatRate == null
            && staffCostsFlatRate == null
            && travelAndAccommodationOnStaffCostsFlatRate == null
            && otherCostsOnStaffCostsFlatRate == null

    fun hasFlatRateOffice() =
        officeAndAdministrationOnDirectCostsFlatRate != null || officeAndAdministrationOnStaffCostsFlatRate != null
    fun hasFlatRateTravel() = travelAndAccommodationOnStaffCostsFlatRate != null
    fun hasFlatRateStaff() = staffCostsFlatRate != null
    fun hasFlatRateOther() = otherCostsOnStaffCostsFlatRate != null

}
