import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ProjectStore} from '../../../project-application/containers/project-application-detail/services/project-store.service';
import {ActivatedRoute} from '@angular/router';
import {ProjectCallSettingsDTO, ProjectPartnerBudgetDTO, ProjectService} from '@cat/api';
import {map, tap} from 'rxjs/operators';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {NumberService} from '@common/services/number.service';
import {AllowedBudgetCategories} from '@project/model/allowed-budget-category';

@Component({
  selector: 'app-budget-table',
  templateUrl: './budget-table.component.html',
  styleUrls: ['./budget-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BudgetTableComponent implements OnInit, OnChanges {

  @Input()
  projectId: number;

  @Input()
  dataSource: ProjectPartnerBudgetDTO[];

  @Input()
  hideFooter = false;

  @Input()
  hideCountry = false;

  displayedColumns: string[];

  totalStaffCosts: number;
  totalOfficeAndAdministrationCosts: number;
  totalTravelCosts: number;
  totalExternalCosts: number;
  totalEquipmentCosts: number;
  totalInfrastructureCosts: number;
  totalOtherCosts: number;
  totalLumpSums: number;
  totalUnitCosts: number;
  total: number;

  budget$: Observable<ProjectPartnerBudgetDTO[]>;
  dataSourceChanged$: BehaviorSubject<ProjectPartnerBudgetDTO[]>;

  constructor(
    public projectStore: ProjectStore,
    private activatedRoute: ActivatedRoute,
    private projectService: ProjectService,
  ) {
  }

  ngOnInit(): void {
    this.dataSourceChanged$ = new BehaviorSubject<ProjectPartnerBudgetDTO[]>(this.dataSource);
    this.budget$ = combineLatest([
      this.dataSourceChanged$,
      this.projectStore.allowedBudgetCategories$,
      this.projectService.getProjectCallSettingsById(this.projectId),
    ])
      .pipe(
        tap(([, allowedBudgetCategories, callSettings]) => {
          this.displayedColumns = this.getDisplayedColumns(allowedBudgetCategories, callSettings);
        }),
        tap(() => this.calculateFooterSums(this.dataSource || [])),
        map(() => this.dataSource),
      );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.dataSource) {
      this.dataSourceChanged$.next(this.dataSource);
    }
  }

  private calculateFooterSums(budgets: ProjectPartnerBudgetDTO[]): void {
    this.totalStaffCosts = NumberService.sum(budgets.map(budget => budget.staffCosts));
    this.totalOfficeAndAdministrationCosts
      = NumberService.sum(budgets.map(budget => budget.officeAndAdministrationCosts));
    this.totalTravelCosts = NumberService.sum(budgets.map(budget => budget.travelCosts));
    this.totalExternalCosts = NumberService.sum(budgets.map(budget => budget.externalCosts));
    this.totalEquipmentCosts = NumberService.sum(budgets.map(budget => budget.equipmentCosts));
    this.totalInfrastructureCosts = NumberService.sum(budgets.map(budget => budget.infrastructureCosts));
    this.totalOtherCosts = NumberService.sum(budgets.map(budget => budget.otherCosts));
    this.totalLumpSums = NumberService.sum(budgets.map(budget => budget.lumpSumContribution));
    this.totalUnitCosts = NumberService.sum(budgets.map(budget => budget.unitCosts));
    this.total = NumberService.sum(budgets.map(budget => budget.totalSum));
  }

  private getDisplayedColumns(allowedBudgetCategories: AllowedBudgetCategories,
                              callSettings: ProjectCallSettingsDTO): string[] {
    const columns: string[] = ['partner'];
    if (!this.hideCountry) {
      columns.push('country');
    }
    if (allowedBudgetCategories.staff.realOrUnitCosts() || callSettings.flatRates?.staffCostFlatRateSetup) {
      columns.push('staffCosts');
    }
    if (callSettings.flatRates?.officeAndAdministrationOnDirectCostsFlatRateSetup || callSettings.flatRates?.officeAndAdministrationOnStaffCostsFlatRateSetup) {
      columns.push('officeAndAdministrationCosts');
    }
    if (allowedBudgetCategories.travel.realOrUnitCosts() || callSettings.flatRates?.travelAndAccommodationOnStaffCostsFlatRateSetup) {
      columns.push('travelCosts');
    }
    if (allowedBudgetCategories.external.realOrUnitCosts()) {
      columns.push('externalCosts');
    }
    if (allowedBudgetCategories.equipment.realOrUnitCosts()) {
      columns.push('equipmentCosts');
    }
    if (allowedBudgetCategories.infrastructure.realOrUnitCosts()) {
      columns.push('infrastructureCosts');
    }
    if (callSettings.flatRates?.otherCostsOnStaffCostsFlatRateSetup) {
      columns.push('otherCosts');
    }
    if (callSettings.lumpSums?.length) {
      columns.push('lumpSums');
    }
    if (callSettings.unitCosts?.find(cost => !cost.oneCostCategory)) {
      columns.push('unitCosts');
    }
    columns.push('total');
    return columns;
  }

}
