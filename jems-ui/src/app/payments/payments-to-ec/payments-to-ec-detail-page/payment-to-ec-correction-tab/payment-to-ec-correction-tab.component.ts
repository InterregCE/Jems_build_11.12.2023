import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FormService} from '@common/components/section/form/form.service';
import {BehaviorSubject, combineLatest, Observable, of} from 'rxjs';
import {
  PagePaymentToEcCorrectionLinkingDTO,
  PaymentApplicationToEcDetailDTO,
  PaymentToEcAmountSummaryDTO, PaymentToEcCorrectionLinkingDTO, PaymentToEcCorrectionLinkingUpdateDTO,
} from '@cat/api';
import {MatTableDataSource} from '@angular/material/table';
import {AbstractControl, FormArray, FormBuilder} from '@angular/forms';
import {APIError} from '@common/models/APIError';
import {PaymentsToEcDetailPageStore} from '../payment-to-ec-detail-page-store.service';
import {catchError, map, take, tap} from 'rxjs/operators';
import { Alert } from '@common/components/forms/alert';
import {PaymentToEcInclusionRow} from '../payment-to-ec-correction-select-table/payment-to-ec-inlcusion-row';
import {PaymentToEcRowSelected} from '../payment-to-ec-correction-select-table/paymnet-to-ec-row-selected';
import {PaymentToEcAmountUpdate} from '../payment-to-ec-correction-select-table/payment-to-ec-amount-update';
import {PaymentToEcCorrectionTabStoreService} from './payment-to-ec-correction-tab-store.service';

@UntilDestroy()
@Component({
  selector: 'jems-payment-to-ec-correction-tab',
  templateUrl: './payment-to-ec-correction-tab.component.html',
  styleUrls: ['./payment-to-ec-correction-tab.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [FormService]
})
export class PaymentToEcCorrectionTabComponent implements OnInit {

  displayedColumns: string[] = [];
  displayedColumnsAll = [
    'select',
    'projectId',
    'projectAcronym',
    'priorityAxis',
    'correctionNo',
    'scenario',
    'controllingBody',
    'totalEligible',
    'fundAmount',
    'partnerContribution',
    'publicContribution',
    'autoPublicContribution',
    'privateContribution',
    'comment',
    'correction'
  ];

  form = this.formBuilder.group({
    array: this.formBuilder.array([]),
  });

  data$: Observable<{
    ecId: number;
    ecCorrections: PagePaymentToEcCorrectionLinkingDTO;
    isEditable: boolean;
  }>;

  tableData: {
    ecId: number;
    isEditable: boolean;
    content: PaymentToEcInclusionRow[];
  };

  cumulativeForCurrentTab$: Observable<{
    data: PaymentToEcAmountSummaryDTO;
  }>;

  Alert = Alert;
  dataSource: MatTableDataSource<AbstractControl> = new MatTableDataSource([]);

  error$ = new BehaviorSubject<APIError | null>(null);
  editedRowIndex: number | null = null;
  successfulUpdateMessage = false;

  constructor(
    public pageStore: PaymentToEcCorrectionTabStoreService,
    private detailPageStore: PaymentsToEcDetailPageStore,
    private formBuilder: FormBuilder,
    public formService: FormService,
    private changeDetectorRef: ChangeDetectorRef
  ) {
    this.data$ = combineLatest([
      this.pageStore.correctionPage$,
      this.detailPageStore.paymentToEcId$,
      this.detailPageStore.userCanEdit$,
      this.detailPageStore.updatedPaymentApplicationStatus$,
    ]).pipe(
      tap(([ecCorrections, ecId, userCanEdit, ecStatus ]) => {
        if (ecCorrections.content) {
          this.initializeForm(ecId, userCanEdit && ecStatus === PaymentApplicationToEcDetailDTO.StatusEnum.Draft, ecCorrections.content);
        }
      }),
      map(([ecCorrections, ecId, userCanEdit, ecStatus ]) => ({
        ecCorrections,
        ecId,
        isEditable: userCanEdit && ecStatus === PaymentApplicationToEcDetailDTO.StatusEnum.Draft,
      })),
      tap(data => {
        this.displayedColumns = data.isEditable ? this.displayedColumnsAll :
          this.displayedColumnsAll.filter(col => !['select', 'correction'].includes(col));
      }),
    );

    this.cumulativeForCurrentTab$ =
      this.pageStore.cumulativeForCurrentTab().pipe(
        map((cumulativeForCurrentTab) => ({
          data: cumulativeForCurrentTab
        })),
      );
    this.formService.init(this.form);
  }

  ngOnInit(): void {
    this.pageStore.correctionRetrieveListError$.pipe(untilDestroyed(this)).subscribe(value => {
      if (value) {
        this.showErrorMessage(value);
      }
    });
  }

  selectionChanged(event$: PaymentToEcRowSelected): void {
    if (event$.selected) {
      this.selectEcCorrection(event$.ecId, event$.correctionId);
    } else {
      this.deselectEcCorrection(event$.correctionId);
    }
  }

  private selectEcCorrection(ecId: number, correctionPaymentId: number) {
    this.pageStore.selectPaymentToEc(ecId, correctionPaymentId).pipe(
      catchError((error) => this.showErrorMessage(error.error))
    ).subscribe();
  }

  private deselectEcCorrection(correctionPaymentId: number) {
    this.pageStore.deselectPaymentFromEc(correctionPaymentId).pipe(
      catchError((error) => this.showErrorMessage(error.error))
    ).subscribe();
  }

  private showErrorMessage(error: APIError): Observable<null> {
    this.error$.next(error);
    setTimeout(() => {
      if (this.error$.value?.id === error.id) {
        this.error$.next(null);
      }
    }, 10000);
    return of(null);
  }

  private showSuccessMessageAfterUpdate(): void {
    this.successfulUpdateMessage = true;
    setTimeout(() => {
      this.successfulUpdateMessage = false;
      this.changeDetectorRef.markForCheck();
    }, 4000);
  }

  submitAmountChanges(event$: PaymentToEcAmountUpdate) {
    const dataToUpdate = {
      correctedPublicContribution: event$.correctedPublicContribution,
      correctedAutoPublicContribution: event$.correctedAutoPublicContribution,
      correctedPrivateContribution: event$.correctedPrivateContribution,
      comment: event$.comment,
    } as PaymentToEcCorrectionLinkingUpdateDTO;

    this.pageStore.updateLinkedPayment(event$.correctionId, dataToUpdate).pipe(
      take(1),
      tap(_ => this.showSuccessMessageAfterUpdate()),
      catchError(err => this.showErrorMessage(err.error)),
      untilDestroyed(this)
    ).subscribe();
    this.editedRowIndex = null;
  }

  discardChanges(rowIndex: number, unchangedCorrections: PaymentToEcCorrectionLinkingDTO) {
    this.ecCorrections.at(rowIndex).patchValue({
      autoPublicContribution: unchangedCorrections.autoPublicContribution,
      publicContribution: unchangedCorrections.publicContribution,
      privateContribution: unchangedCorrections.privateContribution,
      comment: unchangedCorrections.comment,
    });
    this.editedRowIndex = null;
    this.formService.setDirty(false);
  }

  private initializeForm(ecId: number, isEditable: boolean, corrections: PaymentToEcCorrectionLinkingDTO[]) {
    this.ecCorrections.clear();
    corrections.forEach(e => this.addCorrections(e));
    this.formService.setEditable(true);
    this.formService.resetEditable();
    this.dataSource.data = [...this.ecCorrections.controls];
    this.tableData = {
      ecId,
      isEditable,
      content: corrections.map(correction => ({
        correctionId: correction.correction.id,
        paymentToEcId: correction.paymentToEcId,
        projectCustomIdentifier: correction.projectCustomIdentifier,
        projectAcronym: correction.projectAcronym,
        priorityAxis: correction.priorityAxis,
        paymentCorrectionNo: correction.correction.auditControlNumber + '.' + correction.correction.orderNr,
        scenario: correction.scenario,
        controllingBody: correction.controllingBody,
        amountApprovedPerFund: correction.fundAmount,
        partnerContribution: correction.partnerContribution,
        publicContribution: correction.publicContribution,
        correctedPublicContribution: correction.correctedPublicContribution,
        autoPublicContribution: correction.autoPublicContribution,
        correctedAutoPublicContribution: correction.correctedAutoPublicContribution,
        privateContribution: correction.privateContribution,
        correctedPrivateContribution: correction.correctedPrivateContribution,
        comment: correction.comment,
      } as PaymentToEcInclusionRow))
    }
  }

  get ecCorrections(): FormArray {
    return this.form.get('array') as FormArray;
  }

  addCorrections(correction: PaymentToEcCorrectionLinkingDTO) {
    const item = this.formBuilder.group({
      autoPublicContribution: this.formBuilder.control(correction.correctedAutoPublicContribution),
      publicContribution: this.formBuilder.control(correction.correctedPublicContribution),
      privateContribution: this.formBuilder.control(correction.correctedPrivateContribution),
      comment: this.formBuilder.control(correction.comment ? correction.comment : ''),
    });
    this.ecCorrections.push(item);
  }
}