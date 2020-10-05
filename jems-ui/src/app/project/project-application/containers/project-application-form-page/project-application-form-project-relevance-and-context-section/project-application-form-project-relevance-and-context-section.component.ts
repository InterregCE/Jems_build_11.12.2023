import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {BaseComponent} from '@common/components/base-component';
import {merge, Observable, Subject} from 'rxjs';
import {I18nValidationError} from '@common/validation/i18n-validation-error';
import {catchError, flatMap, map, tap} from 'rxjs/operators';
import {Log} from '../../../../../common/utils/log';
import {HttpErrorResponse} from '@angular/common/http';
import {Permission} from 'src/app/security/permissions/permission';
import {InputProjectRelevance, ProjectDescriptionService, OutputCall} from '@cat/api';

@Component({
  selector: 'app-project-application-form-project-relevance-and-context-section',
  templateUrl: './project-application-form-project-relevance-and-context-section.component.html',
  styleUrls: ['./project-application-form-project-relevance-and-context-section.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormProjectRelevanceAndContextSectionComponent extends BaseComponent implements OnInit {
  Permission = Permission;

  @Input()
  projectId: number;
  @Input()
  editable: boolean;
  @Input()
  strategies:OutputCall.StrategiesEnum[];

  saveError$ = new Subject<I18nValidationError | null>();
  saveSuccess$ = new Subject<boolean>();
  updateProjectDescription$ = new Subject<InputProjectRelevance>();
  projectDescriptionDetails$: Observable<any>;
  deleteEntriesFromTables$ = new Subject<InputProjectRelevance>();

  constructor(private projectDescriptionService: ProjectDescriptionService) {
    super();
  }

  ngOnInit(): void {
    const savedDescription$ = this.projectDescriptionService.getProjectDescription(this.projectId)
      .pipe(
        map(project => project.projectRelevance)
      )

    const updatedProjectDescription$ = this.updateProjectDescription$
      .pipe(
        flatMap((data) => this.projectDescriptionService.updateProjectRelevance(this.projectId, data)),
        tap(() => this.saveSuccess$.next(true)),
        tap(() => this.saveError$.next(null)),
        tap(saved => Log.info('Updated project relevance and context:', this, saved)),
        catchError((error: HttpErrorResponse) => {
          this.saveError$.next(error.error);
          throw error;
        })
      );

    const deletedEntriesFromTables$ = this.deleteEntriesFromTables$
      .pipe(
        flatMap((data) => this.projectDescriptionService.updateProjectRelevance(this.projectId, data)),
        tap(() => this.saveError$.next(null)),
        tap(saved => Log.info('Deleted entries from project relevance tables:', this, saved)),
        catchError((error: HttpErrorResponse) => {
          this.saveError$.next(error.error);
          throw error;
        })
      );

    this.projectDescriptionDetails$ = merge(savedDescription$, updatedProjectDescription$, deletedEntriesFromTables$)
      .pipe(
        map(project => ({
          project,
          editable: this.editable
        })),
      );
  }
}