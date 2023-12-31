import {Injectable} from '@angular/core';
import {merge, Observable, of, Subject} from 'rxjs';
import {
  AccountingYearDTO, AccountingYearService,
  PaymentApplicationToEcDetailDTO,
  PaymentApplicationToECService,
  PaymentApplicationToEcUpdateDTO,
  ProgrammeFundDTO,
  ProgrammeFundService,
  UserRoleCreateDTO,
} from '@cat/api';
import {PermissionService} from '../../../security/permissions/permission.service';
import {RoutingService} from '@common/services/routing.service';
import {map, switchMap, tap} from 'rxjs/operators';
import {Log} from '@common/utils/log';
import {MatSort} from '@angular/material/sort';
import {UntilDestroy} from '@ngneat/until-destroy';
import PermissionsEnum = UserRoleCreateDTO.PermissionsEnum;

@UntilDestroy()
@Injectable({
  providedIn: 'root'
})
export class PaymentsToEcDetailPageStore {
  public static PAYMENTS_TO_EC_PATH = '/app/payments/paymentApplicationsToEc/';

  paymentToEcId$: Observable<number>;
  paymentToEcDetail$: Observable<PaymentApplicationToEcDetailDTO>;
  savedPaymentToEcDetail$ = new Subject<PaymentApplicationToEcDetailDTO>();
  programmeFunds$: Observable<ProgrammeFundDTO[]>;
  accountingYears$: Observable<AccountingYearDTO[]>;
  newPageSize$ = new Subject<number>();
  newPageIndex$ = new Subject<number>();
  newSort$ = new Subject<Partial<MatSort>>();
  userCanEdit$: Observable<boolean>;

  constructor(private paymentApplicationToECService: PaymentApplicationToECService,
              private permissionService: PermissionService,
              private routingService: RoutingService,
              private programmeFundService: ProgrammeFundService,
              private accountingYearsService: AccountingYearService
  ) {
    this.paymentToEcId$ = this.routingService.routeParameterChanges(PaymentsToEcDetailPageStore.PAYMENTS_TO_EC_PATH, 'paymentToEcId').pipe(map(Number));
    this.accountingYears$ = this.accountingYears();
    this.paymentToEcDetail$ = this.paymentDetail();
    this.userCanEdit$ = this.userCanEdit();
    this.programmeFunds$ = this.allFunds();
  }


  private paymentDetail(): Observable<PaymentApplicationToEcDetailDTO> {
    const initialPaymentDetail$ = this.paymentToEcId$
      .pipe(
        switchMap((paymentId: number) => paymentId ? this.paymentApplicationToECService.getPaymentApplicationToEcDetail(paymentId) : of({}) as Observable<PaymentApplicationToEcDetailDTO>),
        tap(data => Log.info('Fetched payment to ec detail', this, data))
      );

    return merge(initialPaymentDetail$, this.savedPaymentToEcDetail$);
  }

  private userCanEdit(): Observable<boolean> {
    return  this.permissionService.hasPermission(PermissionsEnum.PaymentsToEcUpdate)
      .pipe(
        map((canUpdate) => canUpdate)
      );
  }

  private accountingYears(): Observable<AccountingYearDTO[]> {
    return this.accountingYearsService.getAccountingYears()
      .pipe(
        tap(accountingYears => Log.info('Fetched accounting years:', this, accountingYears))
      );
  }

  updatePaymentToEcSummary(paymentToEcSummaryData: PaymentApplicationToEcUpdateDTO): Observable<PaymentApplicationToEcDetailDTO> {
    return this.paymentApplicationToECService.updatePaymentApplicationToEc(paymentToEcSummaryData).pipe(
      tap(saved => Log.info('Payment to Ec summary data updated!', saved)),
      tap(data => this.savedPaymentToEcDetail$.next(data))
    );
  }

  createPaymentToEc(paymentToEcSummaryData: PaymentApplicationToEcUpdateDTO): Observable<PaymentApplicationToEcDetailDTO> {
    return this.paymentApplicationToECService.createPaymentApplicationToEc(paymentToEcSummaryData).pipe(
      tap(saved => Log.info('Payment to Ec created!', saved)),
      tap(data => this.savedPaymentToEcDetail$.next(data))
    );
  }

   private allFunds(): Observable<ProgrammeFundDTO[]> {
    return this.programmeFundService.getProgrammeFundList()
      .pipe(
        tap(programmeFunds => Log.info('Fetched programme funds:', this, programmeFunds))
      );
  }
}
