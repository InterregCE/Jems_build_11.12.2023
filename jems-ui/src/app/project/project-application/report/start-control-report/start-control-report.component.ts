import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ProjectPartnerReportDTO} from '@cat/api';
import {BehaviorSubject, combineLatest, Observable, of} from 'rxjs';
import {Forms} from '@common/utils/forms';
import {MatDialog} from '@angular/material/dialog';
import {
  PartnerReportDetailPageStore
} from '@project/project-application/report/partner-report-detail-page/partner-report-detail-page-store.service';
import {catchError, finalize, map, take, tap} from 'rxjs/operators';
import {RoutingService} from '@common/services/routing.service';
import {ActivatedRoute} from '@angular/router';
import {PartnerReportPageStore} from '@project/project-application/report/partner-report-page-store.service';
import {ReportUtil} from '@project/common/report-util';
import {APIError} from '@common/models/APIError';

@Component({
  selector: 'jems-start-control-report',
  templateUrl: './start-control-report.component.html',
  styleUrls: ['./start-control-report.component.scss']
})
export class StartControlReportComponent {
  ReportUtil = ReportUtil;
  ProjectPartnerReportDTO = ProjectPartnerReportDTO;

  @Input()
  reportId: number;
  @Input()
  reportStatus: ProjectPartnerReportDTO.StatusEnum;
  @Output()
  onError = new EventEmitter<APIError>();

  pendingAction$ = new BehaviorSubject(false);
  data$: Observable<{
    partnerId: string | number | null;
    isController: boolean;
    canView: boolean;
    canEdit: boolean;
  }>;

  constructor(
    private pageStore: PartnerReportPageStore,
    private partnerReportDetailStore: PartnerReportDetailPageStore,
    private dialog: MatDialog,
    private router: RoutingService,
    private activatedRoute: ActivatedRoute,
  ) {
    this.data$ = combineLatest([
      partnerReportDetailStore.partnerId$,
      pageStore.institutionUserCanViewControlReports$,
      pageStore.institutionUserCanEditControlReports$,
      pageStore.userCanViewReport$,
      pageStore.userCanEditReport$
    ]).pipe(
      map(([partnerId, institutionView, institutionEdit, reportView, reportEdit]) => ({
        partnerId,
        isController: institutionView || institutionEdit,
        canView: (institutionView || institutionEdit) || reportView || reportEdit,
        canEdit: institutionEdit
      })),
    );
  }

  redirectToControl(reportId: number, isController: boolean) {
    const tab = (isController || this.reportStatus === ProjectPartnerReportDTO.StatusEnum.Certified) ? 'identificationTab' : 'document';
    this.router.navigate([`../${reportId}/controlReport/${tab}`], {
      relativeTo: this.activatedRoute,
      queryParamsHandling: 'merge'
    });
  }

  createControlReportForPartnerReport(partnerId: string | number | null, reportId: number, isButtonDisabled: boolean): void {
    if (isButtonDisabled) {
      return;
    }

    this.pendingAction$.next(true);
    Forms.confirm(
      this.dialog,
      {
        title: 'project.application.partner.report.confirm.control.start.header',
        message: {
          i18nKey: 'project.application.partner.report.confirm.control.start.message'
        },
      }).pipe(
      take(1),
      tap((answer) => {
        if (answer) {
          this.changeStatusOfReport(partnerId, reportId);
        } else {
          this.pendingAction$.next(false);
        }
      })).subscribe();
  }

  private changeStatusOfReport(partnerId: string | number | null, reportId: number): void {
    if (!partnerId) {
      return;
    }

    this.partnerReportDetailStore.startControlOnPartnerReport(Number(partnerId), reportId)
      .pipe(
        take(1),
        tap(() => this.redirectToControl(reportId, true)),
        catchError((error) => {
          this.onError.emit(error.error);
          return of(null);
        }),
        finalize(() => this.pendingAction$.next(false))
      ).subscribe();
  }

}
