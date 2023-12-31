import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormService} from '@common/components/section/form/form.service';
import {combineLatest, Observable} from 'rxjs';
import {
  FinancingSourceBreakdownDTO, VerificationWorkOverviewDTO,
} from '@cat/api';
import {
  ProjectVerificationReportOverviewTabStoreService
} from '@project/project-application/report/project-verification-report/project-verification-report-overview-tab/project-verification-report-overview-tab-store.service';
import {map} from 'rxjs/operators';

@Component({
  selector: 'jems-project-verification-report-overview-tab',
  templateUrl: './project-verification-report-overview-tab.component.html',
  styleUrls: ['./project-verification-report-overview-tab.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [FormService],
})
export class ProjectVerificationReportOverviewTabComponent {

  data$: Observable<{
    verificationWorkOverview: VerificationWorkOverviewDTO;
    financingSourceBreakdown: FinancingSourceBreakdownDTO;
  }>;

  constructor(
    private projectVerificationReportOverviewTabStoreService: ProjectVerificationReportOverviewTabStoreService
  ) {
    this.data$ = combineLatest([
      projectVerificationReportOverviewTabStoreService.verificationWorkOverview$,
      projectVerificationReportOverviewTabStoreService.financingSourceBreakdown$,
    ]).pipe(
      map(([verificationWorkOverview, financingSourceBreakdown]: any) => ({
        verificationWorkOverview,
        financingSourceBreakdown,
      })),
    );
  }
}
