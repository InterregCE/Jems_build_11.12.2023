import {ChangeDetectionStrategy, Component} from '@angular/core';
import {BaseComponent} from '@common/components/base-component';
import {combineLatest, Subject} from 'rxjs';
import {MatSort} from '@angular/material/sort';
import {map, mergeMap, startWith, tap} from 'rxjs/operators';
import {Tables} from '../../../../common/utils/tables';
import {Log} from '../../../../common/utils/log';
import {ProgrammePageSidenavService} from '../../services/programme-page-sidenav.service';
import { Permission } from 'src/app/security/permissions/permission';
import {LumpSumsStore} from '../../services/lump-sums-store.service';
import {ProgrammeCostOptionService} from '@cat/api';
import {UnitCostStore} from '../../services/unit-cost-store.service';

@Component({
  selector: 'app-programme-simplified-cost-options',
  templateUrl: './programme-simplified-cost-options.component.html',
  styleUrls: ['./programme-simplified-cost-options.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeSimplifiedCostOptionsComponent extends BaseComponent {

  Permission = Permission;
  lumpSum$ = this.lumpSumsStore.lumpSum();
  unitCost$ = this.unitCostStore.unitCost();

  newLumpSumPageSize$ = new Subject<number>();
  newLumpSumPageIndex$ = new Subject<number>();
  newLumpSumSort$ = new Subject<Partial<MatSort>>();

  newUnitCostPageSize$ = new Subject<number>();
  newUnitCostPageIndex$ = new Subject<number>();
  newUnitCostSort$ = new Subject<Partial<MatSort>>();

  currentLumpSumsPage$ =
    combineLatest([
      this.newLumpSumPageIndex$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      this.newLumpSumPageSize$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_SIZE)),
      this.newLumpSumSort$.pipe(
        startWith(Tables.DEFAULT_INITIAL_SORT),
        map(sort => sort?.direction ? sort : Tables.DEFAULT_INITIAL_SORT),
        map(sort => `${sort.active},${sort.direction}`)
      )
    ])
      .pipe(
        mergeMap(([pageIndex, pageSize, sort]) =>
          this.programmeCostOptionService.getProgrammeLumpSums(pageIndex, pageSize, sort)),
        tap(page => Log.info('Fetched the Lump Sums:', this, page.content)),
      );

  currentUnitCostPage$ =
    combineLatest([
      this.newUnitCostPageIndex$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      this.newUnitCostPageSize$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_SIZE)),
      this.newUnitCostSort$.pipe(
        startWith(Tables.DEFAULT_INITIAL_SORT),
        map(sort => sort?.direction ? sort : Tables.DEFAULT_INITIAL_SORT),
        map(sort => `${sort.active},${sort.direction}`)
      )
    ])
      .pipe(
        mergeMap(([pageIndex, pageSize, sort]) =>
          this.programmeCostOptionService.getProgrammeUnitCosts(pageIndex, pageSize, sort)),
        tap(page => Log.info('Fetched the Unit Costs:', this, page.content)),
      );

  constructor(private lumpSumsStore: LumpSumsStore,
              private unitCostStore: UnitCostStore,
              private programmeCostOptionService: ProgrammeCostOptionService,
              private programmePageSidenavService: ProgrammePageSidenavService) {
    super();
    this.programmePageSidenavService.init(this.destroyed$);
  }
}