import {InputTranslation} from '@cat/api';

export class StaffCostsBudgetTableEntry {

  id?: number;
  description?: InputTranslation[] = [];
  numberOfUnits?: number;
  pricePerUnit?: number;
  rowSum?: number;
  new?: boolean;
  type?: string;
  unitType?: string;
  comment?: InputTranslation[] = [];

  constructor(data: Partial<StaffCostsBudgetTableEntry>) {
    this.id = data.id;
    this.description = data.description;
    this.numberOfUnits = data.numberOfUnits;
    this.pricePerUnit = data.pricePerUnit;
    this.new = data.new;
    this.rowSum = data.rowSum;
    this.type = data.type;
    this.unitType = data.unitType;
    this.comment = data.comment;
  }

}
