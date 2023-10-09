import {ChangeDetectionStrategy, Component} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable, of} from 'rxjs';
import {PaymentApplicationToEcDetailDTO, PaymentApplicationToEcDTO, UserRoleDTO} from '@cat/api';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {PaymentsToEcDetailPageStore} from './payment-to-ec-detail-page-store.service';
import PaymentEcStatusEnum = PaymentApplicationToEcDTO.StatusEnum;
import {catchError, finalize, map, take} from 'rxjs/operators';
import {APIError} from '@common/models/APIError';
import {Alert} from '@common/components/forms/alert';
import {PaymentsPageSidenavService} from '../../payments-page-sidenav.service';
import {RoutingService} from '@common/services/routing.service';
import {ActivatedRoute} from '@angular/router';

@UntilDestroy()
@Component({
  selector: 'jems-payments-to-ec-detail-page',
  templateUrl: './payment-to-ec-detail-page.component.html',
  styleUrls: ['./payment-to-ec-detail-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentToEcDetailPageComponent {
  Alert = Alert;
  paymentStatusEnum = PaymentEcStatusEnum;
  PermissionEnum = UserRoleDTO.PermissionsEnum;
  finalizeError$ = new BehaviorSubject<APIError | null>(null);
  finalizationPending$ = new BehaviorSubject(false);

  data$: Observable<{
    paymentDetail: PaymentApplicationToEcDetailDTO;
    userCanEdit: boolean;
    finalizationDisabled: boolean;
    userCanView: boolean;
  }>;

  constructor(public pageStore: PaymentsToEcDetailPageStore,
              private router: RoutingService,
              private activatedRoute: ActivatedRoute,
              private paymentsPageSidenav: PaymentsPageSidenavService) {
    this.data$ = combineLatest([
      this.pageStore.paymentToEcDetail$,
      this.pageStore.updatedPaymentApplicationStatus$,
      this.pageStore.userCanEdit$,
      this.pageStore.userCanView$,
    ]).pipe(
        map(([paymentDetail, paymentStatus, userCanEdit, userCanView]) => ({
              paymentDetail: this.getUpdatePayment(paymentDetail, paymentStatus),
              userCanEdit,
              finalizationDisabled: this.isFinalizationDisabled(paymentStatus, userCanEdit),
              userCanView
            })
        )
    );
  }

  getUpdatePayment(savedPayment: PaymentApplicationToEcDetailDTO, newStatus: PaymentApplicationToEcDetailDTO.StatusEnum): PaymentApplicationToEcDetailDTO {
    const updatedPayment = savedPayment;
    updatedPayment.status = newStatus;
    return updatedPayment;
  }

  finalizePaymentApplication(paymentId: number) {
    this.finalizationPending$.next(true);

    this.pageStore.finalizePaymentApplicationToEc(paymentId).pipe(
      take(1),
      catchError((err) =>
        this.showErrorMessage(err)
      ),
      finalize(() => this.finalizationPending$.next(false)),
      untilDestroyed(this)
    ).subscribe();
  }

  public showErrorMessage(error: APIError): Observable<null> {
    this.finalizeError$.next(error);
    setTimeout(() => {
      this.finalizeError$.next(null);
    }, 4000);
    return of(null);
  }

  private isFinalizationDisabled(paymentStatus: PaymentApplicationToEcDetailDTO.StatusEnum, userCanEdit: boolean): boolean {
    return paymentStatus == this.paymentStatusEnum.Finished || !userCanEdit;
  }

  activeTab(route: string): boolean {
    return this.router.url?.includes(route);
  }

  routeTo(route: string): void {
    this.router.navigate([route], {relativeTo: this.activatedRoute, queryParamsHandling: 'merge'});
  }

}
