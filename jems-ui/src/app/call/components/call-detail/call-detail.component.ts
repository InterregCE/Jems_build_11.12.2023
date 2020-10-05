import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {InputCallCreate, InputCallUpdate, OutputCall, OutputProgrammeStrategy, OutputProgrammeFund} from '@cat/api'
import {ViewEditForm} from '@common/components/forms/view-edit-form';
import {FormState} from '@common/components/forms/form-state';
import {Forms} from '../../../common/utils/forms';
import {filter, take, takeUntil} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';
import {CallPriorityCheckbox} from '../../containers/model/call-priority-checkbox';
import {Tools} from '../../../common/utils/tools';
import {SelectionModel} from '@angular/cdk/collections';

@Component({
  selector: 'app-call-detail',
  templateUrl: './call-detail.component.html',
  styleUrls: ['./call-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CallDetailComponent extends ViewEditForm implements OnInit {
  tools = Tools;

  @Input()
  call: OutputCall
  @Input()
  priorityCheckboxes: CallPriorityCheckbox[];
  @Input()
  strategies: OutputProgrammeStrategy[];
  @Input()
  funds: OutputProgrammeFund[];

  @Output()
  create: EventEmitter<InputCallCreate> = new EventEmitter<InputCallCreate>()
  @Output()
  update: EventEmitter<InputCallUpdate> = new EventEmitter<InputCallUpdate>()
  @Output()
  publish: EventEmitter<number> = new EventEmitter<number>()
  @Output()
  cancel: EventEmitter<void> = new EventEmitter<void>()

  startDateErrors = {
    required: 'call.startDate.unknown',
    matDatetimePickerParse: 'common.date.should.be.valid',
    matDatetimePickerMax: 'call.startDate.must.be.before.endDate',
  };
  endDateErrors = {
    required: 'call.endDate.unknown',
    matDatetimePickerParse: 'common.date.should.be.valid',
    matDatetimePickerMin: 'call.endDate.must.be.after.startDate',
  };
  nameErrors = {
    required: 'call.name.unknown',
    maxlength: 'call.name.wrong.size'
  };
  descriptionErrors = {
    maxlength: 'call.description.wrong.size'
  };
  lengthOfPeriodErrors = {
    max: 'call.lengthOfPeriod.invalid.period',
    min: 'call.lengthOfPeriod.invalid.period',
  };
  published = false;
  selection = new SelectionModel<OutputProgrammeStrategy>(true, []);
  selectionFunds = new SelectionModel<OutputProgrammeFund>(true, []);

  callForm = this.formBuilder.group({
    name: ['', Validators.compose([
      Validators.required,
      Validators.maxLength(250)
    ])],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    description: ['', Validators.maxLength(1000)],
    lengthOfPeriod: ['', Validators.compose(
      [Validators.max(99), Validators.min(1)])]
  });

  constructor(private formBuilder: FormBuilder,
              private dialog: MatDialog,
              protected changeDetectorRef: ChangeDetectorRef) {
    super(changeDetectorRef)
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.published = this.call?.status === OutputCall.StatusEnum.PUBLISHED;
    this.changeFormState$.next(this.call?.id ? FormState.VIEW : FormState.EDIT);
  }

  getForm(): FormGroup | null {
    return this.callForm;
  }

  onSubmit(): void {
    const call = {
      name: this.callForm?.controls?.name?.value,
      startDate: this.callForm?.controls?.startDate?.value,
      endDate: this.callForm?.controls?.endDate?.value,
      description: this.callForm?.controls?.description?.value,
      lengthOfPeriod: this.callForm?.controls?.lengthOfPeriod?.value,
      priorityPolicies: this.priorityCheckboxes
        .flatMap(checkbox => checkbox.getCheckedChildPolicies()),
      strategies: this.buildUpdateEntityStrategies(),
      funds: this.buildUpdateEntityFunds(),
    }
    if (!this.call.id) {
      this.create.emit(call);
      return;
    }
    this.update.emit({
      ...call,
      id: this.call.id
    });
  }

  onCancel(): void {
    if (this.call?.id) {
      this.changeFormState$.next(FormState.VIEW);
    }
    this.cancel.emit();
  }

  publishCall(): void {
    Forms.confirmDialog(
      this.dialog,
      'call.dialog.title',
      'call.dialog.message'
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(yes => !!yes)
    ).subscribe(() => {
      this.publish.emit(this.call?.id);
    });
  }

  publishingRequirementsNotAchieved(): boolean {
    return (this.priorityCheckboxes
      && !this.priorityCheckboxes.some(priority => priority.checked || priority.someChecked())
      || !this.call.lengthOfPeriod
      || this.buildUpdateEntityFunds().length === 0);
  }

  private buildUpdateEntityStrategies():  OutputProgrammeStrategy.StrategyEnum[] {
    return this.strategies
      .filter(strategy => this.selection.isSelected(strategy))
      .map(strategy => strategy.strategy);
  }

  private buildUpdateEntityFunds():  number[] {
    return this.funds
      .filter(fund => this.selectionFunds.isSelected(fund))
      .map(fund => fund.id);
  }

  protected enterViewMode(): void {
    this.callForm.controls.name.setValue(this.call.name);
    this.callForm.controls.startDate.setValue(this.call.startDate);
    this.callForm.controls.endDate.setValue(this.call.endDate);
    this.callForm.controls.description.setValue(this.call.description);
    this.callForm.controls.lengthOfPeriod.setValue(this.call.lengthOfPeriod);
    if (this.strategies) {
      this.selection.select(...this.strategies.filter(element => element.active));
      this.selection.deselect(...this.strategies.filter(element => !element.active));
      this.changeDetectorRef.markForCheck();
    }
    if (this.funds) {
      this.selectionFunds.select(...this.funds.filter(element => element.selected));
      this.selectionFunds.deselect(...this.funds.filter(element => !element.selected));
      this.changeDetectorRef.markForCheck();
    }
  }
}