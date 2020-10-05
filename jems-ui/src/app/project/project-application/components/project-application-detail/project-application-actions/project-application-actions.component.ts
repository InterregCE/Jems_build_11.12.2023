import {ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output} from '@angular/core';
import {AbstractForm} from '@common/components/forms/abstract-form';
import {MatDialog} from '@angular/material/dialog';
import {FormGroup} from '@angular/forms';
import {filter, map, take, takeUntil} from 'rxjs/operators';
import {Forms} from '../../../../../common/utils/forms';
import {InputProjectStatus, InputRevertProjectStatus, OutputProjectStatus, OutputRevertProjectStatus} from '@cat/api';
import {Alert} from '@common/components/forms/alert';
import {Permission} from 'src/app/security/permissions/permission';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';

@Component({
  selector: 'app-project-application-actions',
  templateUrl: './project-application-actions.component.html',
  styleUrls: ['./project-application-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationActionsComponent extends AbstractForm {
  Alert = Alert;
  Permission = Permission;
  OutputProjectStatus = OutputProjectStatus;

  returnableStatuses = [
    OutputProjectStatus.StatusEnum.SUBMITTED,
    OutputProjectStatus.StatusEnum.ELIGIBLE,
    OutputProjectStatus.StatusEnum.APPROVEDWITHCONDITIONS,
    OutputProjectStatus.StatusEnum.APPROVED,
  ];

  @Input()
  projectStatus: OutputProjectStatus.StatusEnum;
  @Input()
  projectRevertStatus: OutputRevertProjectStatus;
  @Input()
  projectCallEndDate: Date;
  @Input()
  projectId: number;

  @Output()
  changeStatus = new EventEmitter<InputProjectStatus.StatusEnum>();
  @Output()
  revertStatus = new EventEmitter<InputRevertProjectStatus>();

  constructor(public translate: TranslateService,
              private dialog: MatDialog,
              protected changeDetectorRef: ChangeDetectorRef) {
    super(changeDetectorRef);
  }

  getForm(): FormGroup | null {
    return null;
  }

  submitProject(): void {
    Forms.confirmDialog(
      this.dialog,
      'project.detail.submit.dialog.title',
      'project.detail.submit.dialog.message',
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(answer => !!answer),
      map(() => this.changeStatus.emit(InputProjectStatus.StatusEnum.SUBMITTED))
    ).subscribe();
  }

  resubmitProject(): void {
    Forms.confirmDialog(
      this.dialog,
      'project.detail.resubmit.dialog.title',
      'project.detail.resubmit.dialog.message',
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(answer => !!answer),
      map(() => this.changeStatus.emit(InputProjectStatus.StatusEnum.SUBMITTED))
    ).subscribe();
  }

  returnToApplicant(): void {
    Forms.confirmDialog(
      this.dialog,
      'project.detail.return.dialog.title',
      'project.detail.return.dialog.message',
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(answer => !!answer),
      map(() => this.changeStatus.emit(InputProjectStatus.StatusEnum.RETURNEDTOAPPLICANT))
    ).subscribe();
  }

  revertProjectStatus(): void {
    Forms.confirmDialog(
      this.dialog,
      'project.application.revert.status.dialog.title',
      'project.application.revert.status.dialog.message',
      {
        from: this.translate.instant('common.label.projectapplicationstatus.' + this.projectRevertStatus?.from?.status),
        to: this.translate.instant('common.label.projectapplicationstatus.' + this.projectRevertStatus?.to?.status)
      }
    ).pipe(
      take(1),
      takeUntil(this.destroyed$),
      filter(answer => !!answer),
      map(() => this.revertStatus.emit({
        projectStatusFromId: this.projectRevertStatus.from.id,
        projectStatusToId: this.projectRevertStatus.to.id
      }))
    ).subscribe();
  }

  isOpen(): boolean {
    const currentDate = moment(new Date());
    return currentDate.isBefore(this.projectCallEndDate);
  };
}