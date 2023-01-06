import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormService} from '@common/components/section/form/form.service';
import {combineLatest, Observable} from 'rxjs';
import {
  ProjectPeriodDTO, ProjectReportDTO, ProjectReportUpdateDTO
} from '@cat/api';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {
  ProjectStore
} from '@project/project-application/containers/project-application-detail/services/project-store.service';
import {
  ProjectApplicationFormSidenavService
} from '@project/project-application/containers/project-application-form-page/services/project-application-form-sidenav.service';
import {catchError, filter, map, take, tap} from 'rxjs/operators';
import { APPLICATION_FORM } from '@project/common/application-form-model';
import { ProjectReportDetailPageStore } from '../project-report-detail-page-store.service';

@Component({
  selector: 'jems-project-report-identification-tab',
  templateUrl: './project-report-identification-tab.component.html',
  styleUrls: ['./project-report-identification-tab.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectReportIdentificationTabComponent {
  APPLICATION_FORM = APPLICATION_FORM;
  ProjectReportDTO = ProjectReportDTO;

  data$: Observable<{
    projectReport: ProjectReportDTO;
    periods: ProjectPeriodDTO[];
  }>;

  form: FormGroup = this.formBuilder.group({
    startDate: [''],
    endDate: [''],
    periodNumber: [null, Validators.required],
    deadlineId: [null, Validators.required],
    type:[null, Validators.required],
    reportingDate: ['', Validators.required]
  });

  dateNameArgs = {
    startDate: 'start date',
    endDate: 'end date'
  };

  inputErrorMessages = {
    matDatetimePickerParse: 'common.date.should.be.valid',
    matDatetimePickerMin: 'common.error.field.start.before.end',
    matDatetimePickerMax: 'common.error.field.end.after.start'
  };

  selectedPeriod: ProjectPeriodDTO | undefined = undefined;
  availablePeriods: ProjectPeriodDTO[] = [];
  availableDeadlines: string[] = [];

  constructor(public pageStore: ProjectReportDetailPageStore,
              public formService: FormService,
              private formBuilder: FormBuilder,
              private projectStore: ProjectStore,
              private projectSidenavService: ProjectApplicationFormSidenavService) {
    this.data$ = combineLatest([
      pageStore.projectReport$,
      this.projectStore.projectPeriods$,
    ]).pipe(
      tap(([projectReport, availablePeriods]) =>
        this.availablePeriods = availablePeriods
      ),
      map(([projectReport, availablePeriods]) => ({
        projectReport,
        periods: availablePeriods,
      })),
      tap((data) => this.resetForm(data.projectReport)),
    );

    this.form.get('period')?.valueChanges.pipe(
      filter(period => !!period),
      tap(periodNumber => this.selectedPeriod = this.availablePeriods.find(period => period.number === periodNumber)),
    ).subscribe();

    this.formService.init(this.form, this.pageStore.reportEditable$);
  }

  resetForm(identification: ProjectReportDTO) {
    this.form.patchValue(identification);
    this.form.patchValue({
      periodNumber: identification?.periodDetail?.number,
    });
    this.formService.resetEditable();
    if (identification.deadlineId === null){
      this.form.get('deadlineId')?.patchValue(0);
    }
  }

  saveIdentification(): void {
    const data = {...this.form.value} as ProjectReportUpdateDTO;
    this.pageStore.saveIdentification(data)
      .pipe(
        take(1),
        tap(() => this.formService.setSuccess('project.application.project.report.identification.saved')),
        catchError(err => this.formService.setError(err))
      ).subscribe();
  }

}
