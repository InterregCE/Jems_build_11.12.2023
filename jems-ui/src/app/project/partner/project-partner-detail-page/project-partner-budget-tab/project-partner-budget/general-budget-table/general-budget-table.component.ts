import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ProjectPartnerBudgetConstants} from '../project-partner-budget.constants';
import {
  AbstractControl,
  ControlContainer,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import {MatTableDataSource} from '@angular/material/table';
import {map, startWith} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {NumberService} from '../../../../../../common/services/number.service';
import {FormService} from '@common/components/section/form/form.service';
import {MultiLanguageInputService} from '../../../../../../common/services/multi-language-input.service';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {GeneralBudgetTable} from '../../../../../project-application/model/general-budget-table';

@UntilDestroy()
@Component({
  selector: 'app-general-budget-table',
  templateUrl: './general-budget-table.component.html',
  styleUrls: ['./general-budget-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GeneralBudgetTableComponent implements OnInit, OnChanges {

  constants = ProjectPartnerBudgetConstants;
  columnsToDisplay = ['description', 'awardProcedures', 'investment', 'unitType', 'numberOfUnits', 'pricePerUnit', 'total', 'action'];

  @Input()
  editable = true;
  @Input()
  tableName: string;
  @Input()
  budgetTable: GeneralBudgetTable;
  @Input()
  investmentIds: number[];

  budgetForm: FormGroup;
  dataSource: MatTableDataSource<AbstractControl>;
  numberOfItems$: Observable<number>;

  constructor(private formService: FormService, private controlContainer: ControlContainer, private formBuilder: FormBuilder, private multiLanguageInputService: MultiLanguageInputService) {
    this.budgetForm = this.controlContainer.control as FormGroup;
  }

  ngOnInit(): void {

    this.dataSource = new MatTableDataSource<AbstractControl>(this.items.controls);
    this.numberOfItems$ = this.items.valueChanges.pipe(startWith(0), map(() => this.items.length));

    this.items.valueChanges.subscribe(() => {
      this.dataSource.data = this.items.controls;
      this.items.controls.forEach(control => {
        this.setRowTotal(control as FormGroup);
      });
      this.setTableTotal();
    });

    this.formService.reset$.pipe(
      map(() => this.resetTableFormGroup(this.budgetTable)),
      untilDestroyed(this)
    ).subscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.budgetTable) {
      this.resetTableFormGroup(this.budgetTable);
    }
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.formService.setDirty(true);
  }

  addNewItem(): void {
    this.items.push(this.formBuilder.group({
      id: null,
      description: [this.multiLanguageInputService.multiLanguageFormFieldDefaultValue()],
      unitType: [this.multiLanguageInputService.multiLanguageFormFieldDefaultValue()],
      awardProcedures: [this.multiLanguageInputService.multiLanguageFormFieldDefaultValue()],
      investmentId: [null],
      numberOfUnits: [1, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
      pricePerUnit: [0, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
      rowSum: [0, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]]
    }));
    this.formService.setDirty(true);
  }

  private resetTableFormGroup(commonBudgetTable: GeneralBudgetTable): void {
    this.total.setValue(commonBudgetTable.total);
    this.items.clear();
    this.budgetTable.entries.forEach(item => {
      this.items.push(this.formBuilder.group({
        id: [item.id],
        description: [item.description],
        unitType: [item.unitType],
        awardProcedures: [item.awardProcedures],
        investmentId: [item.investmentId],
        numberOfUnits: [item.numberOfUnits, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
        pricePerUnit: [item.pricePerUnit, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
        rowSum: [item.rowSum, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]],
      }));
    });

  }

  private setTableTotal(): void {
    let total = 0;
    this.items.controls.forEach(control => {
      total = NumberService.sum([control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.value || 0, total]);
    });
    this.total.setValue(NumberService.truncateNumber(total));
  }

  private setRowTotal(control: FormGroup): void {
    const numberOfUnits = control.get(this.constants.FORM_CONTROL_NAMES.numberOfUnits)?.value || 0;
    const pricePerUnit = control.get(this.constants.FORM_CONTROL_NAMES.pricePerUnit)?.value || 0;
    control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.setValue(NumberService.truncateNumber(NumberService.product([numberOfUnits, pricePerUnit])), {emitEvent: false});
  }

  get table(): FormGroup {
    return this.budgetForm.get(this.tableName) as FormGroup;
  }

  get items(): FormArray {
    return this.table.get(this.constants.FORM_CONTROL_NAMES.items) as FormArray;
  }

  get total(): FormControl {
    return this.table.get(this.constants.FORM_CONTROL_NAMES.total) as FormControl;
  }

}