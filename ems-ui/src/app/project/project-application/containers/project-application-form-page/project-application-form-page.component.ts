import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {catchError, flatMap, map, takeUntil, tap} from 'rxjs/operators';
import {ProjectStore} from '../project-application-detail/services/project-store.service';
import {ActivatedRoute} from '@angular/router';
import {
  CallService,
  InputProjectData,
  OutputCallProgrammePriority,
  OutputProject,
  OutputProjectStatus,
  ProjectService
} from '@cat/api';
import {SideNavService} from '@common/components/side-nav/side-nav.service';
import {BaseComponent} from '@common/components/base-component';
import {HeadlineType} from '@common/components/side-nav/headline-type';
import {HeadlineRoute} from '@common/components/side-nav/headline-route';
import {combineLatest, merge, Subject} from 'rxjs';
import {Log} from '../../../../common/utils/log';
import {I18nValidationError} from '@common/validation/i18n-validation-error';
import {HttpErrorResponse} from '@angular/common/http';
import {Permission} from 'src/app/security/permissions/permission';

@Component({
  selector: 'app-project-application-form-page',
  templateUrl: './project-application-form-page.component.html',
  styleUrls: ['./project-application-form-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormPageComponent extends BaseComponent implements OnInit {
  Permission = Permission;
  OutputProjectStatus = OutputProjectStatus;
  projectId = this.activatedRoute?.snapshot?.params?.projectId;

  saveError$ = new Subject<I18nValidationError | null>();
  saveSuccess$ = new Subject<boolean>();
  updateProjectData$ = new Subject<InputProjectData>();

  constructor(private projectStore: ProjectStore,
              private projectService: ProjectService,
              private activatedRoute: ActivatedRoute,
              private sideNavService: SideNavService,
              private callService: CallService) {
    super();
  }


  private fetchObjectives$ = new Subject<OutputProject>()
  private updatedProjectData$ = this.updateProjectData$
    .pipe(
      flatMap((data) => this.projectService.updateProjectData(this.projectId, data)),
      tap(() => this.saveSuccess$.next(true)),
      tap(() => this.saveError$.next(null)),
      tap(saved => Log.info('Updated project data:', this, saved)),
      catchError((error: HttpErrorResponse) => {
        this.saveError$.next(error.error);
        throw error;
      })
    );

  private callObjectives$ = this.fetchObjectives$
    .pipe(
      flatMap(project => this.callService.getCallObjectives(project.call.id)),
      tap(objectives => Log.info('Fetched objectives', this, objectives)),
      map(objectives => ({
        priorities: objectives
          .sort((a, b) => {
            const orderBool = a.code.toLocaleLowerCase() > b.code.toLocaleLowerCase();
            return orderBool ? 1 : -1;
          })
          .map(objective => objective.code + ' - ' + objective.title),
        objectivesWithPolicies: this.getObjectivesWithPolicies(objectives)
      }))
    )

  private projectDetails$ = merge(
    this.projectStore.getProject(),
    this.updatedProjectData$
  )
    .pipe(
      takeUntil(this.destroyed$),
      tap(project => this.setHeadlines(project.id + ' ' + project.acronym)),
      tap(project => this.fetchObjectives$.next(project)),
      map(project => ({
        project,
        editable: project.projectStatus.status === OutputProjectStatus.StatusEnum.DRAFT
          || project.projectStatus.status === OutputProjectStatus.StatusEnum.RETURNEDTOAPPLICANT
      })),
    );

  details$ = combineLatest([
    this.projectDetails$,
    this.callObjectives$
  ])
    .pipe(
      map(
        ([projectDetails, callObjectives]) => ({projectDetails, callObjectives})
      )
    )

  ngOnInit() {
    this.projectStore.init(this.projectId);
  }

  private setHeadlines(acronym: string): void {
    this.sideNavService.setHeadlines(this.destroyed$, [
      {
        headline: 'back.project.overview',
        route: '/project/' + this.projectId,
        type: HeadlineType.BACKROUTE
      },
      {
        headline: 'project.application.form.title',
        type: HeadlineType.TITLE
      },
      {
        headline: acronym,
        type: HeadlineType.SUBTITLE
      },
      {
        headline: 'project.application.form.section.part.a',
        scrollRoute: 'applicationFormHeading',
        type: HeadlineType.SECTION
      },
      {
        headline: 'project.application.form.section.part.a.subsection.one',
        scrollRoute: 'projectIdentificationHeading',
        type:  HeadlineType.SUBSECTION
      },
      {
        headline: 'project.application.form.section.part.a.subsection.two',
        scrollRoute: 'projectSummaryHeading',
        type:  HeadlineType.SUBSECTION
      },
      {
        headline: 'project.application.form.section.part.b',
        scrollRoute: 'projectPartnersHeading',
        type:  HeadlineType.SECTION
      },
      {
        headline: 'project.application.form.section.part.c',
        scrollRoute: 'projectDescriptionHeading',
        type: HeadlineType.SECTION
      },
      {
        headline: 'project.application.form.section.part.c.subsection.four',
        scrollRoute: 'projectWorkPlanHeading',
        type:HeadlineType.SUBSECTION
      }
    ]);
  }

  private getObjectivesWithPolicies(objectives: OutputCallProgrammePriority[]): { [key: string]: InputProjectData.SpecificObjectiveEnum[] } {
    const objectivesWithPolicies: any = {};
    objectives.forEach(objective =>
      objectivesWithPolicies[objective.code + ' - ' + objective.title] =
        objective.programmePriorityPolicies.map(priority => priority.programmeObjectivePolicy));
    return objectivesWithPolicies;
  }
}
