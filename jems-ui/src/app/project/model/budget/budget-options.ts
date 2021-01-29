export class BudgetOptions {
  officeAndAdministrationOnStaffCostsFlatRate: number | null;
  officeAndAdministrationOnDirectCostsFlatRate: number | null;
  staffCostsFlatRate: number | null;
  travelAndAccommodationOnStaffCostsFlatRate: number | null;
  otherCostsOnStaffCostsFlatRate: number | null;

  constructor(officeAndAdministrationOnStaffCostsFlatRate: number | null, officeAndAdministrationOnDirectCostsFlatRate: number | null, staffCostsFlatRate: number | null, travelAndAccommodationOnStaffCostsFlatRate: number | null, otherCostsOnStaffCostsFlatRate: number | null) {
    this.officeAndAdministrationOnStaffCostsFlatRate = officeAndAdministrationOnStaffCostsFlatRate;
    this.officeAndAdministrationOnDirectCostsFlatRate = officeAndAdministrationOnDirectCostsFlatRate;
    this.staffCostsFlatRate = staffCostsFlatRate;
    this.travelAndAccommodationOnStaffCostsFlatRate = travelAndAccommodationOnStaffCostsFlatRate;
    this.otherCostsOnStaffCostsFlatRate = otherCostsOnStaffCostsFlatRate;
  }
}
