import {ChangeDetectionStrategy, Component} from '@angular/core';
import {BaseComponent} from '@common/components/base-component';
import {
  InputIndicatorOutputCreate,
  InputIndicatorOutputUpdate,
  ProgrammeIndicatorService,
  ProgrammePriorityService
} from '@cat/api';
import {ActivatedRoute} from '@angular/router';
import {catchError, map, take, takeUntil, tap} from 'rxjs/operators';
import {Log} from '../../../../common/utils/log';
import {of, Subject} from 'rxjs';
import {I18nValidationError} from '@common/validation/i18n-validation-error';
import {HttpErrorResponse} from '@angular/common/http';
import {TabService} from '../../../../common/services/tab.service';
import {ProgrammePageSidenavService} from '../../services/programme-page-sidenav.service';

@Component({
  selector: 'app-programme-output-indicator-submission-page',
  templateUrl: './programme-output-indicator-submission-page.component.html',
  styleUrls: ['./programme-output-indicator-submission-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeOutputIndicatorSubmissionPageComponent extends BaseComponent {

  outputIndicatorId = this.activatedRoute?.snapshot?.params?.indicatorId;
  isCreate = !this.outputIndicatorId;

  outputIndicatorSaveError$ = new Subject<I18nValidationError | null>();
  outputIndicatorSaveSuccess$ = new Subject<boolean>();

  outputIndicator$ = this.outputIndicatorId
    ? this.programmeIndicatorService.getIndicatorOutput(this.outputIndicatorId).pipe(
      tap(outputIndicatorData => Log.info('Fetched output Indicator data:', this, outputIndicatorData)))
    : of({});

  priorities$ = this.programmePriorityService.get(0, 100, 'code,asc')
    .pipe(
      map(page => page.content),
      tap(page => Log.info('Fetched the priorities:', this, page)),
    );

  constructor(private programmeIndicatorService: ProgrammeIndicatorService,
              private activatedRoute: ActivatedRoute,
              private tabService: TabService,
              private programmePriorityService: ProgrammePriorityService,
              private programmePageSidenavService: ProgrammePageSidenavService) {
    super();
    this.programmePageSidenavService.init(this.destroyed$);
  }

  createOutputIndicator(indicator: InputIndicatorOutputCreate) {
    this.programmeIndicatorService.createIndicatorOutput(indicator)
      .pipe(
        take(1),
        takeUntil(this.destroyed$),
        tap(saved => Log.info('Saved indicator:', this, saved)),
        tap(() => this.outputIndicatorSaveSuccess$.next(true)),
        tap(() => this.outputIndicatorSaveError$.next(null)),
        tap(() => this.programmePageSidenavService.goToIndicators()),
        catchError((error: HttpErrorResponse) => {
          this.outputIndicatorSaveError$.next(error.error);
          throw error;
        })
      ).subscribe();
  }

  updateOutputIndicator(indicator: InputIndicatorOutputUpdate) {
    this.programmeIndicatorService.updateIndicatorOutput(indicator)
      .pipe(
        take(1),
        takeUntil(this.destroyed$),
        tap(saved => Log.info('Saved indicator:', this, saved)),
        tap(() => this.outputIndicatorSaveSuccess$.next(true)),
        tap(() => this.outputIndicatorSaveError$.next(null)),
        catchError((error: HttpErrorResponse) => {
          this.outputIndicatorSaveError$.next(error.error);
          throw error;
        })
      ).subscribe();
  }

  cancelCreate() {
    this.programmePageSidenavService.goToIndicators();
  }

}