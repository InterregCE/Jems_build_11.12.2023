import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {OutputProjectPeriod} from '@cat/api';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FormService} from '@common/components/section/form/form.service';
import {FormArray, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {combineLatest, Observable} from 'rxjs';
import {catchError, map, startWith, take, tap} from 'rxjs/operators';
import {ProjectWorkPackagePageStore} from '../project-work-package-page-store.service';
import {ProjectWorkPackageActivitiesTabConstants} from './project-work-package-activities-tab.constants';
import {WorkPackageActivityDTO} from 'build/generated-sources/openapi/model/workPackageActivityDTO';
import {MultiLanguageInputService} from '../../../../common/services/multi-language-input.service';
import {WorkPackageActivityDeliverableDTO} from 'build/generated-sources/openapi/model/workPackageActivityDeliverableDTO';

@UntilDestroy()
@Component({
  selector: 'app-project-work-package-activities-tab',
  templateUrl: './project-work-package-activities-tab.component.html',
  styleUrls: ['./project-work-package-activities-tab.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectWorkPackageActivitiesTabComponent implements OnInit {
  constants = ProjectWorkPackageActivitiesTabConstants;

  form = this.formBuilder.group({
    activities: this.formBuilder.array([])
  });

  data$: Observable<{
    activities: WorkPackageActivityDTO[],
    periods: OutputProjectPeriod[],
    workPackageNumber: number
  }>;

  private editable = true;

  constructor(private formService: FormService,
              private formBuilder: FormBuilder,
              private workPackageStore: ProjectWorkPackagePageStore,
              public languageService: MultiLanguageInputService) {
    this.formService.init(this.form);
  }

  ngOnInit(): void {
    this.workPackageStore.isProjectEditable$
      .pipe(
        tap(projectEditable => this.editable = projectEditable),
        untilDestroyed(this)
      ).subscribe();

    combineLatest([
      this.workPackageStore.activities$, this.formService.reset$.pipe(startWith(null))
    ])
      .pipe(
        map(([activities]) => this.resetForm(activities)),
        untilDestroyed(this)
      ).subscribe();

    this.data$ = combineLatest([
      this.workPackageStore.activities$,
      this.workPackageStore.workPackage$,
      this.workPackageStore.project$,
    ]).pipe(
      map(([activities, workPackage, project]) => ({
          activities,
          periods: project.periods,
          workPackageNumber: workPackage.number,
        })
      ));
  }

  updateActivities(): void {
    this.workPackageStore.saveWorkPackageActivities(this.activities.value)
      .pipe(
        take(1),
        tap(() => this.formService.setSuccess('project.work.package.tab.activities.saved')),
        catchError(err => this.formService.setError(err))
      ).subscribe();
  }

  addNewActivity(): void {
    this.addActivity();
    this.formService.setDirty(true);
  }

  removeActivity(index: number): void {
    this.activities.removeAt(index);
    this.formService.setDirty(true);
  }

  get activities(): FormArray {
    return this.form.get(this.constants.ACTIVITIES.name) as FormArray;
  }

  addNewDeliverable(activityIndex: number): void {
    this.addDeliverable(activityIndex);
    this.formService.setDirty(true);
  }

  deliverables(activityIndex: number): FormArray {
    return this.activities.at(activityIndex).get(this.constants.DELIVERABLES.name) as FormArray;
  }

  removeDeliverable(activityIndex: number, deliverableIndex: number): void {
    this.deliverables(activityIndex).removeAt(deliverableIndex);
    this.formService.setDirty(true);
  }

  getPeriodArguments(period: OutputProjectPeriod): { [key: string]: number } {
    return {
      periodNumber: period.number,
      start: period.start,
      end: period.end
    };
  }

  addActivityVisible(): boolean {
    return this.editable && this.activities.length < 20;
  }

  addDeliverableVisible(activityIndex: number): boolean {
    return this.editable && this.deliverables(activityIndex).length < 20;
  }

  private resetForm(activities: WorkPackageActivityDTO[]): void {
    this.activities.clear();
    activities.forEach((activity, index) => {
      this.addActivity(activity);
      activity.deliverables?.forEach(deliverable => this.addDeliverable(index, deliverable));
    });
    if (!this.editable) {
      this.formService.setEditable(false);
    }
  }

  private addActivity(existing?: WorkPackageActivityDTO): void {
    this.activities.push(this.formBuilder.group(
      {
        title: this.formBuilder.control(existing?.title || [], this.constants.TITLE.validators),
        description: this.formBuilder.control(existing?.description || []),
        startPeriod: this.formBuilder.control(existing?.startPeriod || ''),
        endPeriod: this.formBuilder.control(existing?.endPeriod || ''),
        deliverables: this.formBuilder.array([]),
      },
      {
        validators: this.constants.PERIODS.validators
      })
    );
  }

  private addDeliverable(activityIndex: number, existing?: WorkPackageActivityDeliverableDTO): void {
    this.deliverables(activityIndex).push(this.formBuilder.group({
      description: this.formBuilder.control(existing?.description || [], this.constants.DELIVERABLE.validators),
      period: this.formBuilder.control(existing?.period || ''),
    }));
  }

}