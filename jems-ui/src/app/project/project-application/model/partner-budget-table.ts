import {PartnerBudgetTableEntry} from './partner-budget-table-entry';
import {PartnerBudgetTableType} from './partner-budget-table-type';
import {NumberService} from '../../../common/services/number.service';

export class PartnerBudgetTable {
  type: PartnerBudgetTableType;
  entries: PartnerBudgetTableEntry[] = [];
  total?: number;

  constructor(type: PartnerBudgetTableType, entries: PartnerBudgetTableEntry[]) {
    this.type = type;
    this.entries = entries;
    this.computeTotal();
  }

  valid(): boolean {
    return this.entries.every(entry => entry.valid());
  }

  computeTotal(): void {
    const sum = NumberService.sum(this.entries.map(entry => entry.total || 0));
    this.total = NumberService.truncateNumber(sum);
  }
}
