import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
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
import {ProjectPartnerBudgetConstants} from '../project-partner-budget.constants';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {NumberService} from '../../../../../../common/services/number.service';
import {Tables} from '../../../../../../common/utils/tables';
import {FormService} from '@common/components/section/form/form.service';
import {MultiLanguageInputService} from '../../../../../../common/services/multi-language-input.service';

@Component({
  selector: 'app-staff-cost-table',
  templateUrl: './staff-cost-table.component.html',
  styleUrls: ['./staff-cost-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush

})
export class StaffCostTableComponent implements OnInit {

  constants = ProjectPartnerBudgetConstants;
  columnsToDisplay = ['description', 'numberOfUnits', 'pricePerUnit', 'total', 'action'];

  @Input()
  editable: boolean;

  budgetForm: FormGroup;
  dataSource: MatTableDataSource<AbstractControl>;
  numberOfItems$: Observable<number>;

  constructor(private formService: FormService, private controlContainer: ControlContainer, private formBuilder: FormBuilder, private multiLanguageInputService: MultiLanguageInputService) {
    this.budgetForm = this.controlContainer.control as FormGroup;
  }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource<AbstractControl>(this.items.controls);
    this.numberOfItems$ = this.items.valueChanges.pipe(startWith(null), map(() => this.items.length));
    this.items.valueChanges.subscribe(() => {
      this.dataSource.data = this.items.controls;
      this.items.controls.forEach(control => {
        this.setRowSum(control as FormGroup);
      });
      this.setTotal();
    });
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.formService.setDirty(true);
  }

  addNewItem(): void {
    this.items.push(this.formBuilder.group({
      id: Tables.getNextId(this.items.controls),
      description: this.formBuilder.control(this.multiLanguageInputService.multiLanguageFormFieldDefaultValue()),
      numberOfUnits: this.formBuilder.control(1, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]),
      pricePerUnit: this.formBuilder.control(0, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]),
      rowSum: this.formBuilder.control(0, [Validators.max(this.constants.MAX_VALUE), Validators.min(this.constants.MIN_VALUE)]),
      new: true
    }));
    this.formService.setDirty(true);
  }

  private setTotal(): void {
    let total = 0;
    this.items.controls.forEach(control => {
      total = NumberService.sum([control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.value || 0, total]);
    });
    this.total.setValue(NumberService.truncateNumber(total));
  }

  private setRowSum(control: FormGroup): void {
    const numberOfUnits = control.get(this.constants.FORM_CONTROL_NAMES.numberOfUnits)?.value || 0;
    const pricePerUnit = control.get(this.constants.FORM_CONTROL_NAMES.pricePerUnit)?.value || 0;
    control.get(this.constants.FORM_CONTROL_NAMES.rowSum)?.setValue(NumberService.truncateNumber(NumberService.product([numberOfUnits, pricePerUnit])), {emitEvent: false});
  }

  get staff(): FormGroup {
    return this.budgetForm.get(this.constants.FORM_CONTROL_NAMES.staff) as FormGroup;
  }

  get items(): FormArray {
    return this.staff.get(this.constants.FORM_CONTROL_NAMES.items) as FormArray;
  }

  get total(): FormControl {
    return this.staff.get(this.constants.FORM_CONTROL_NAMES.total) as FormControl;
  }

}