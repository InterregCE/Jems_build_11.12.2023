import {ChangeDetectionStrategy, Component, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {WorkPackageInvestmentService, WorkPackageInvestmentDTO} from '@cat/api';
import {TableConfiguration} from '@common/components/table/model/table.configuration';
import {MatSort} from '@angular/material/sort';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Subject} from 'rxjs';
import {ProjectStore} from '../../../project-application/containers/project-application-detail/services/project-store.service';
import {Permission} from '../../../../security/permissions/permission';
import {filter, map, mergeMap, startWith, take, tap} from 'rxjs/operators';
import {Tables} from '../../../../common/utils/tables';
import {Log} from '../../../../common/utils/log';
import {FormService} from '@common/components/section/form/form.service';
import {ProjectWorkPackagePageStore} from '../project-work-package-page-store.service';
import {ColumnType} from '@common/components/table/model/column-type.enum';
import {Forms} from '../../../../common/utils/forms';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-project-work-package-investments-tab',
  templateUrl: './project-work-package-investments-tab.component.html',
  styleUrls: ['./project-work-package-investments-tab.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectWorkPackageInvestmentsTabComponent implements OnInit {
  projectId = this.activatedRoute.snapshot.params.projectId;
  workPackageId = this.activatedRoute.snapshot.params.workPackageId;

  tableConfiguration: TableConfiguration;
  Permission = Permission;

  @Input()
  pageIndex: number;
  @Input()
  editable: boolean;

  @ViewChild('deletionCell', {static: true})
  deletionCell: TemplateRef<any>;

  newPageSize$ = new Subject<number>();
  newPageIndex$ = new Subject<number>();
  newSort$ = new Subject<Partial<MatSort>>();

  currentPage$ =
    combineLatest([
      this.newPageIndex$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      this.newPageSize$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_SIZE)),
      this.newSort$.pipe(
        startWith(Tables.DEFAULT_INITIAL_SORT),
        map(sort => sort?.direction ? sort : Tables.DEFAULT_INITIAL_SORT),
        map(sort => `${sort.active},${sort.direction}`)
      ),
      this.workPackageStore.workPackage$
        .pipe(
          tap(workPackage => this.workPackageId = workPackage.id),
          tap(() => this.setRouterLink())
        )
    ])
      .pipe(
        mergeMap(([pageIndex, pageSize, sort]) =>
          this.workPackageService.getWorkPackageInvestments(this.workPackageId, pageIndex, pageSize, sort)),
        tap(page => Log.info('Fetched the work package investments:', this, page.content)),
      );

  constructor(private activatedRoute: ActivatedRoute,
              public projectStore: ProjectStore,
              public workPackageStore: ProjectWorkPackagePageStore,
              private workPackageService: WorkPackageInvestmentService,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.tableConfiguration = new TableConfiguration({
      isTableClickable: true,
      routerLink: '/app/project/detail/' + this.projectId + '/applicationFormWorkPackage/detail/' + this.workPackageId + '/investment/detail',
      columns: [
        {
          displayedColumn: 'project.application.form.workpackage.investments.number',
          elementProperty: 'investmentNumber',
          alternativeValueCondition: (element: any) => {
            return element === null;
          },
          alternativeValue: 'project.application.form.partner.number.info.auto',
          sortProperty: 'investmentNumber'
        },
        {
          displayedColumn: 'project.application.form.workpackage.investments.title',
          elementProperty: 'title',
          sortProperty: 'title',
        },
        {
          displayedColumn: 'project.application.form.workpackage.investments.nuts3',
          elementProperty: 'address.nutsRegion3',
          sortProperty: 'address.nutsRegion3',
        },
        {
          displayedColumn: ' ',
          columnType: ColumnType.CustomComponent,
          customCellTemplate: this.deletionCell
        },
      ]
    });
    this.setRouterLink();
  }

  delete(workPackageInvestment: WorkPackageInvestmentDTO): void {
    let message: string;
    let name: string;
    if (workPackageInvestment.title) {
      message = 'project.application.form.workpackage.table.action.delete.dialog.message';
      name = workPackageInvestment.title;
    } else {
      message = 'project.application.form.workpackage.table.action.delete.dialog.message.no.name';
      name = ' ';
    }
    Forms.confirmDialog(
      this.dialog,
      'project.application.form.workpackage.table.action.delete.dialog.header',
      message,
      {name, boldWarningMessage: 'project.application.form.workpackage.table.action.delete.dialog.warning'})
      .pipe(
        take(1),
        filter(answer => !!answer),
        map(() => this.workPackageStore.deleteWorkPackageInvestment(this.workPackageId, this.projectId, workPackageInvestment.id)),
      ).subscribe();
  }

  private setRouterLink(): void {
    this.tableConfiguration.routerLink = '/app/project/detail/' + this.projectId + '/applicationFormWorkPackage/detail/' + this.workPackageId + '/investment/detail';
  }

}
