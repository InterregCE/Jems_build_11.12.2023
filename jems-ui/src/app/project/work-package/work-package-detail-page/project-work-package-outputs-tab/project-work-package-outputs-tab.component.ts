import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FormService} from '@common/components/section/form/form.service';
import {ProjectWorkPackageOutputsTabConstants} from './project-work-package-outputs-tab.constants';
import {combineLatest, Observable} from 'rxjs';
import {FormArray, FormBuilder} from '@angular/forms';
import {ProjectWorkPackagePageStore} from '../project-work-package-page-store.service';
import {MultiLanguageInputService} from '../../../../common/services/multi-language-input.service';
import {catchError, map, startWith, tap} from 'rxjs/operators';
import {IndicatorOutputDto, OutputProjectPeriod, WorkPackageOutputDTO} from '@cat/api';
import {take} from 'rxjs/internal/operators';

@UntilDestroy()
@Component({
  selector: 'app-project-work-package-outputs-tab',
  templateUrl: './project-work-package-outputs-tab.component.html',
  styleUrls: ['./project-work-package-outputs-tab.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectWorkPackageOutputsTabComponent implements OnInit {
  constants = ProjectWorkPackageOutputsTabConstants;

  form = this.formBuilder.group({
    outputs: this.formBuilder.array([])
  });

  data$: Observable<{
    outputs: WorkPackageOutputDTO[],
    periods: OutputProjectPeriod[],
    outputIndicators: IndicatorOutputDto[],
    workPackageNumber: number
  }>;

  constructor(public formService: FormService,
              private formBuilder: FormBuilder,
              private workPackageStore: ProjectWorkPackagePageStore,
              public languageService: MultiLanguageInputService) {
    this.formService.init(this.form, this.workPackageStore.isProjectEditable$);
  }

  ngOnInit(): void {
    combineLatest([
      this.workPackageStore.outputs$, this.formService.reset$.pipe(startWith(null))
    ])
      .pipe(
        map(([outputs]) => this.resetForm(outputs)),
        untilDestroyed(this)
      ).subscribe();

    this.data$ = combineLatest([
      this.workPackageStore.outputs$,
      this.workPackageStore.workPackage$,
      this.workPackageStore.outputIndicators$,
      this.workPackageStore.project$,
    ]).pipe(
      map(([outputs, workPackage, indicators, project]) => ({
          outputs,
          periods: project.periods,
          outputIndicators: indicators,
          workPackageNumber: workPackage.number,
        })
      ));
  }

  updateOutputs(): void {
    this.workPackageStore.saveWorkPackageOutputs(this.outputs.value)
      .pipe(
        take(1),
        tap(() => this.formService.setSuccess('project.application.form.workpackage.outputs.save.success')),
        catchError(err => this.formService.setError(err))
      ).subscribe();
  }

  addNewOutput(): void {
    this.addOutput();
    this.formService.setDirty(true);
  }

  removeOutput(index: number): void {
    this.outputs.removeAt(index);
    this.outputs.controls.forEach(
      (output, i) => output.get(this.constants.OUTPUT_NUMBER.name)?.setValue(i)
    );
    this.formService.setDirty(true);
  }

  get outputs(): FormArray {
    return this.form.get(this.constants.OUTPUTS.name) as FormArray;
  }

  addOutputVisible(): boolean {
    return this.form.enabled && this.outputs.length < 20;
  }

  getMeasurementUnit(indicatorId: number, indicators: IndicatorOutputDto[]): string | undefined {
    return indicators.find(indicator => indicator.id === indicatorId)?.measurementUnit;
  }

  private resetForm(outputs: WorkPackageOutputDTO[]): void {
    this.outputs.clear();
    outputs.forEach((activity, index) => this.addOutput(activity));
    this.formService.resetEditable();
  }

  private addOutput(existing?: WorkPackageOutputDTO): void {
    this.outputs.push(this.formBuilder.group({
        outputNumber: this.formBuilder.control(existing?.outputNumber || this.outputs.length),
        programmeOutputIndicatorId: this.formBuilder.control(existing?.programmeOutputIndicatorId || null),
        title: this.formBuilder.control(existing?.title || [], this.constants.TITLE.validators),
        targetValue: this.formBuilder.control(existing?.targetValue || ''),
        periodNumber: this.formBuilder.control(existing?.periodNumber || ''),
        description: this.formBuilder.control(existing?.description || []),
      }
    ));
  }
}