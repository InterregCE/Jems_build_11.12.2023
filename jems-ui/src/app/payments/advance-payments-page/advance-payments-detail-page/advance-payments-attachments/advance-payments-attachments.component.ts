import {ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {FormService} from '@common/components/section/form/form.service';
import {AcceptedFileTypesConstants} from '@project/common/components/file-management/accepted-file-types.constants';
import {combineLatest, Observable, Subject} from 'rxjs';
import {FileListItem} from '@common/components/file-list/file-list-item';
import {ActivatedRoute} from '@angular/router';
import {
  ReportFileManagementStore
} from '@project/project-application/report/partner-report-detail-page/partner-report-annexes-tab/report-file-management-store';
import {PaymentAdvanceAttachmentService, JemsFileDTO, UserRoleDTO} from '@cat/api';
import {finalize, map, take} from 'rxjs/operators';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FileListComponent} from '@common/components/file-list/file-list.component';
import {FileDescriptionChange} from '@common/components/file-list/file-list-table/file-description-change';
import {Alert} from '@common/components/forms/alert';

import {Tables} from '@common/utils/tables';
import {PageFileList} from '@common/components/file-list/page-file-list';
import {AdvancePaymentAttachmentsStore} from './advance-payments-attachments-store.service';
import PermissionsEnum = UserRoleDTO.PermissionsEnum;

@UntilDestroy()
@Component({
  selector: 'jems-advance-payments-attachments',
  templateUrl: './advance-payments-attachments.component.html',
  styleUrls: ['./advance-payments-attachments.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdvancePaymentsAttachmentsComponent implements OnChanges {
  Alert = Alert;
  Tables = Tables;
  PermissionsEnum = PermissionsEnum;

  acceptedFilesTypes = AcceptedFileTypesConstants.acceptedFilesTypes;
  maximumAllowedFileSizeInMB: number;
  fileSizeOverLimitError$ = new Subject<boolean>();
  isUploadInProgress = false;

  data$: Observable<{
    attachments: PageFileList;
    isPaymentEditable: boolean;
  }>;

  @Input()
  private paymentId: number;

  constructor(
    private activatedRoute: ActivatedRoute,
    public advancePaymentAttachmentsStore: AdvancePaymentAttachmentsStore,
    private fileManagementStore: ReportFileManagementStore,
    private paymentAdvanceAttachmentService: PaymentAdvanceAttachmentService
  ) {
    this.data$ = combineLatest([
      this.advancePaymentAttachmentsStore.attachments$,
      this.advancePaymentAttachmentsStore.paymentEditable$,
    ]).pipe(
      map(([attachments, isPaymentEditable]) => ({
        attachments: {
          ...attachments,
          content: attachments.content?.map((file: JemsFileDTO) => ({
            id: file.id,
            name: file.name,
            type: file.type,
            uploaded: file.uploaded,
            author: file.author,
            sizeString: file.sizeString,
            description: file.description,
            editable: isPaymentEditable,
            deletable: isPaymentEditable,
            tooltipIfNotDeletable: '',
            iconIfNotDeletable: ''
          })),
        },
        isPaymentEditable,
      })),
    );
    this.fileManagementStore.getMaximumAllowedFileSize().pipe(untilDestroyed(this))
      .subscribe((maxAllowedSize) => this.maximumAllowedFileSizeInMB = maxAllowedSize);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.paymentId && !changes.paymentId.previousValue) {
      this.advancePaymentAttachmentsStore.paymentId$.next(this.paymentId);
    }
  }

  uploadFile(target: any): void {
    this.isUploadInProgress = true;
    FileListComponent.doFileUploadWithValidation(
      target,
      this.fileSizeOverLimitError$,
      this.advancePaymentAttachmentsStore.error$,
      this.maximumAllowedFileSizeInMB,
      file => this.advancePaymentAttachmentsStore.uploadPaymentFile(file),
    ).add(() => this.isUploadInProgress = false);
  }

  downloadFile(file: FileListItem): void {
    this.advancePaymentAttachmentsStore.downloadFile(file.id)
      .pipe(take(1))
      .subscribe();
  }

  setDescriptionCallback = (data: FileDescriptionChange): Observable<any> => {
    return this.paymentAdvanceAttachmentService.updateAttachmentDescription(data.id, data.description);
  };

  deleteCallback = (file: FileListItem): Observable<void> => {
    return this.paymentAdvanceAttachmentService.deleteAttachment(file.id);
  };
}
