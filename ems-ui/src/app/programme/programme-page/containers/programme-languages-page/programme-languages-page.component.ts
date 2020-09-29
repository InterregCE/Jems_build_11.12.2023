import {ChangeDetectionStrategy, Component, OnDestroy} from '@angular/core';
import {BaseComponent} from '@common/components/base-component';
import {OutputProgrammeData, ProgrammeDataService} from '@cat/api';
import {catchError, flatMap, tap} from 'rxjs/operators';
import {Log} from '../../../../common/utils/log';
import {HttpErrorResponse} from '@angular/common/http';
import {merge, Subject} from 'rxjs';
import {I18nValidationError} from '@common/validation/i18n-validation-error';
import {ProgrammePageSidenavService} from '../../services/programme-page-sidenav.service';
import {Permission} from '../../../../security/permissions/permission';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-programme-languages-page',
  templateUrl: './programme-languages-page.component.html',
  styleUrls: ['./programme-languages-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgrammeLanguagesPageComponent extends BaseComponent implements OnDestroy {
  Permission = Permission;

  programmeSaveError$ = new Subject<I18nValidationError | null>();
  programmeSaveSuccess$ = new Subject<boolean>();
  saveProgrammeData$ = new Subject<OutputProgrammeData>();

  private programmeById$ = this.programmeDataService.get()
    .pipe(
      tap(programmeData => Log.info('Fetched programme data:', this, programmeData))
    );

  private savedProgramme$ = this.saveProgrammeData$
    .pipe(
      flatMap(programmeUpdate => this.programmeDataService.update(programmeUpdate)),
      tap(saved => Log.info('Updated programme:', this, saved)),
      tap((response) => this.translateService.addLangs(
        response?.systemLanguageSelections
          .filter(value => value.selected)
          .map(value => value.name))),
      tap(() => this.programmeSaveSuccess$.next(true)),
      tap(() => this.programmeSaveError$.next(null)),
      tap(() => window.location.reload()),
      catchError((error: HttpErrorResponse) => {
        this.programmeSaveError$.next(error.error);
        throw error;
      })
    );

  programme$ = merge(this.programmeById$, this.savedProgramme$);

  constructor(private programmeDataService: ProgrammeDataService,
              private programmePageSidenavService: ProgrammePageSidenavService,
              private translateService: TranslateService) {
    super();
    this.programmePageSidenavService.init(this.destroyed$);
  }

}