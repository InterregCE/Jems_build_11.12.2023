import {Injectable} from '@angular/core';
import {combineLatest, merge, Observable, of, Subject} from 'rxjs';
import {RoutingService} from '@common/services/routing.service';
import {UntilDestroy} from '@ngneat/until-destroy';
import {
  CorrectionAvailablePartnerDTO,
  ProgrammeFundDTO,
  ProjectAuditAndControlService,
  ProjectAuditControlCorrectionDTO,
  ProjectAuditControlCorrectionExtendedDTO,
  ProjectCorrectionFinancialDescriptionDTO,
  ProjectCorrectionFinancialDescriptionService,
  ProjectCorrectionFinancialDescriptionUpdateDTO,
  ProjectCorrectionIdentificationDTO,
  ProjectCorrectionIdentificationService,
  ProjectCorrectionIdentificationUpdateDTO,
  ProjectCorrectionService,
  UserRoleDTO,
} from '@cat/api';
import {catchError, map, shareReplay, switchMap, tap} from 'rxjs/operators';
import {ProjectPaths} from '@project/common/project-util';
import {Log} from '@common/utils/log';
import {
  ReportCorrectionsAuditControlDetailPageStore
} from '@project/project-application/report/report-corrections-overview/report-corrections-audit-control-detail-page/report-corrections-audit-control-detail-page.store';
import {PermissionService} from '../../../../../../../security/permissions/permission.service';
import {
  AuditControlCorrectionStore
} from '@project/project-application/report/report-corrections-overview/report-corrections-audit-control-detail-page/audit-control-correction-overview/audit-control-correction-store.service';
import PermissionsEnum = UserRoleDTO.PermissionsEnum;

@UntilDestroy()
@Injectable({
  providedIn: 'root'
})
export class AuditControlCorrectionDetailPageStore {

  AUDIT_CONTROL_CORRECTION_PATH = 'correction/';

  projectId$: Observable<number>;
  auditControlId$: Observable<string | number | null>;
  correctionId$: Observable<string | number | null>;
  correction$: Observable<ProjectAuditControlCorrectionExtendedDTO>;
  correctionIdentification$: Observable<ProjectCorrectionIdentificationDTO>;
  canEdit$: Observable<boolean>;
  canClose$: Observable<boolean>;
  correctionPartnerData$: Observable<CorrectionAvailablePartnerDTO[]>;
  funds$ = new Subject<ProgrammeFundDTO[]>();
  pastCorrections$: Observable<ProjectAuditControlCorrectionDTO[]>;
  updatedCorrection$ = new Subject<ProjectCorrectionIdentificationDTO>();
  updatedCorrectionStatus$ = new Subject<ProjectAuditControlCorrectionDTO.StatusEnum>();
  correctionStatus$: Observable<ProjectAuditControlCorrectionDTO.StatusEnum>;
  financialDescription$: Observable<ProjectCorrectionFinancialDescriptionDTO>;
  savedFinancialDescription$ = new Subject<ProjectCorrectionFinancialDescriptionDTO>();

  constructor(
    private routingService: RoutingService,
    private reportCorrectionsAuditControlDetailPageStore: ReportCorrectionsAuditControlDetailPageStore,
    private projectAuditControlCorrectionService: ProjectCorrectionService,
    private projectCorrectionIdentificationService: ProjectCorrectionIdentificationService,
    private permissionService: PermissionService,
    private auditControlCorrectionStore: AuditControlCorrectionStore,
    private projectAuditAndControlService: ProjectAuditAndControlService,
    private projectCorrectionFinancialDescriptionService: ProjectCorrectionFinancialDescriptionService
  ) {
    this.projectId$ = this.reportCorrectionsAuditControlDetailPageStore.projectId$;
    this.auditControlId$ = this.reportCorrectionsAuditControlDetailPageStore.auditControlId$;
    this.correctionId$ = this.correctionId();
    this.correction$ = this.correction();
    this.correctionPartnerData$ = this.correctionPartnerData();
    this.correctionIdentification$ = this.correctionIdentification();
    this.pastCorrections$ = this.pastCorrections();
    this.correctionStatus$ = this.correctionStatus();
    this.canEdit$ = this.canEdit();
    this.canClose$ = this.canClose();
    this.financialDescription$ = this.financialDescription();
  }

  saveCorrection(id: number, correctionData: ProjectCorrectionIdentificationUpdateDTO): Observable<ProjectCorrectionIdentificationDTO> {
    return combineLatest([
      this.reportCorrectionsAuditControlDetailPageStore.auditControlId$,
      this.reportCorrectionsAuditControlDetailPageStore.projectId$,
    ]).pipe(
      switchMap(([auditControlId, projectId]) =>
        this.projectCorrectionIdentificationService.updateCorrectionIdentification(Number(auditControlId), id, projectId, correctionData)
      ),
      tap(correction => this.updatedCorrection$.next(correction)),
      tap(correction => Log.info('Updated correction', this, correction))
    );
  }

  private correctionId(): Observable<string | number | null> {
    return this.routingService.routeParameterChanges(this.AUDIT_CONTROL_CORRECTION_PATH, 'correctionId');
  }

  private correctionPartnerData(): Observable<CorrectionAvailablePartnerDTO[]> {
    return this.reportCorrectionsAuditControlDetailPageStore.projectId$
    .pipe(
      switchMap(projectId =>
        this.projectAuditAndControlService.getPartnerAndPartnerReportData(projectId)
      ),
      tap(correctionPartnerData => Log.info('Fetched correction partner data: ', this, correctionPartnerData))
      );
  }

  private pastCorrections(): Observable<ProjectAuditControlCorrectionDTO[]> {
    return combineLatest([
      this.reportCorrectionsAuditControlDetailPageStore.auditControlId$,
      this.reportCorrectionsAuditControlDetailPageStore.projectId$,
      this.correctionId$
    ]).pipe(
      switchMap(([auditControlId, projectId, correctionId]) =>
        this.projectCorrectionIdentificationService.getPreviousClosedCorrections(Number(auditControlId), Number(correctionId), projectId)
      ),
      tap(correctionPartnerData => Log.info('Fetched past corrections: ', this, correctionPartnerData))
    );
  }

  private correction(): Observable<ProjectAuditControlCorrectionExtendedDTO> {
    const initialCorrection = combineLatest([
      this.reportCorrectionsAuditControlDetailPageStore.auditControlId$,
      this.reportCorrectionsAuditControlDetailPageStore.projectId$,
      this.correctionId$
    ]).pipe(
      switchMap(([auditControlId, projectId, correctionId]) =>
        correctionId
          ? this.projectAuditControlCorrectionService.getProjectAuditCorrection(Number(auditControlId), Number(correctionId), projectId).pipe(
            catchError(() => {
              this.routingService.navigate([ProjectPaths.PROJECT_DETAIL_PATH, projectId]);
              return of({} as ProjectAuditControlCorrectionExtendedDTO);
            })
          )
          : of({} as ProjectAuditControlCorrectionExtendedDTO)
      ),
      tap((correction) => Log.info('Fetched correction', this, correction)),
      shareReplay(1),
    );

    return initialCorrection;
  }

  private correctionIdentification(): Observable<ProjectCorrectionIdentificationDTO> {
    const initialCorrectionIdentification = combineLatest([
      this.reportCorrectionsAuditControlDetailPageStore.auditControlId$,
      this.reportCorrectionsAuditControlDetailPageStore.projectId$,
      this.correctionId$
    ]).pipe(
      switchMap(([auditControlId, projectId, correctionId]) =>
        correctionId
          ? this.projectCorrectionIdentificationService.getCorrectionIdentification(Number(auditControlId), Number(correctionId), projectId).pipe(
            catchError(() => {
              this.routingService.navigate([ProjectPaths.PROJECT_DETAIL_PATH, projectId]);
              return of({} as ProjectCorrectionIdentificationDTO);
            })
          )
          : of({} as ProjectCorrectionIdentificationDTO)
      ),
      tap((correction) => Log.info('Fetched correction identification', this, correction)),
      shareReplay(1),
    );

    return merge(initialCorrectionIdentification, this.updatedCorrection$);
  }

  private correctionStatus(): Observable<ProjectAuditControlCorrectionDTO.StatusEnum> {
    return merge(
      this.correction$.pipe(map(correction => correction?.correction?.status)),
      this.updatedCorrectionStatus$
    );
  }

  private canEdit(): Observable<boolean> {
    return combineLatest([
      this.reportCorrectionsAuditControlDetailPageStore.canEdit$,
      this.correctionStatus$,
    ]).pipe(
      map(([canEditAuditControl, status]) =>
        canEditAuditControl && status !== ProjectAuditControlCorrectionDTO.StatusEnum.Closed)
    );
  }

  private canClose(): Observable<boolean> {
    return combineLatest([
      this.canEdit$,
      this.permissionService.hasPermission(PermissionsEnum.ProjectMonitorCloseAuditControlCorrection),
      this.correctionIdentification$
    ]).pipe(
      map(([canEdit, canClose, identification]) =>
        canEdit && canClose && !!identification.partnerId && !!identification.partnerReportId && !!identification.programmeFundId
      )
    );
  }

  closeCorrection(projectId: number, auditControlId: number, correctionId: number): Observable<ProjectAuditControlCorrectionDTO.StatusEnum> {
    return this.projectAuditControlCorrectionService.closeProjectCorrection(auditControlId, correctionId, projectId).pipe(
      map(status => status as ProjectAuditControlCorrectionDTO.StatusEnum),
      tap(status => this.updatedCorrectionStatus$.next(status)),
      tap(status => this.auditControlCorrectionStore.refreshCorrections$.next()),
      tap(status => Log.info('Changed status for correction', this, correctionId, status)),
    );
  }

  private financialDescription(): Observable<ProjectCorrectionFinancialDescriptionDTO> {
    const initialData$ = combineLatest([this.projectId$, this.auditControlId$, this.correctionId$])
      .pipe(
        switchMap(([projectId, controlId, correctionId]) => this.projectCorrectionFinancialDescriptionService.getCorrectionFinancialDescription(controlId as number, correctionId as number, projectId)),
      );
    return merge(initialData$, this.savedFinancialDescription$)
      .pipe(
        shareReplay(1)
      );
  }

  saveFinancialDescription(correctionId: number, financialDescriptionData: ProjectCorrectionFinancialDescriptionUpdateDTO): Observable<ProjectCorrectionFinancialDescriptionDTO> {
    return combineLatest([
      this.reportCorrectionsAuditControlDetailPageStore.auditControlId$,
      this.reportCorrectionsAuditControlDetailPageStore.projectId$,
    ]).pipe(
      switchMap(([auditControlId, projectId]) =>
        this.projectCorrectionFinancialDescriptionService.updateCorrectionFinancialDescription(Number(auditControlId), correctionId, projectId, financialDescriptionData)
      ),
      tap(financialDescription => this.savedFinancialDescription$.next(financialDescription)),
      tap(financialDescription => Log.info('Updated correction financial description', this, financialDescription))
    );
  }

}
