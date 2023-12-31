import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable, of, Subject} from 'rxjs';
import {CategoryInfo, CategoryNode} from '@project/common/components/category-tree/categoryModels';
import {JemsFileDTO,} from '@cat/api';
import {ContractingFilesStoreService} from '@project/project-application/services/contracting-files-store.service';
import {AcceptedFileTypesConstants} from '@project/common/components/file-management/accepted-file-types.constants';
import {I18nMessage} from '@common/models/I18nMessage';
import {Alert} from '@common/components/forms/alert';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FileListItem} from '@common/components/file-list/file-list-item';
import {catchError, finalize, map, take} from 'rxjs/operators';
import {PageFileList} from '@common/components/file-list/page-file-list';
import {FileDescriptionChange} from '@common/components/file-list/file-list-table/file-description-change';
import FileTypeEnum = JemsFileDTO.TypeEnum;

@UntilDestroy()
@Component({
  selector: 'jems-contract-files',
  templateUrl: './contract-files.component.html',
  styleUrls: ['./contract-files.component.scss']
})
export class ContractFilesComponent implements OnInit, OnChanges {

  Alert = Alert;

  @Input()
  isEditable: boolean;

  @Input()
  isLocked: boolean;

  sectionLockedEvent$ = new BehaviorSubject<boolean>(false);

  canUpload$: Observable<boolean>;

  files$: Observable<PageFileList>;
  maximumAllowedFileSizeInMB: number;
  fileSizeOverLimitError$ = new Subject<boolean>();
  acceptedFilesTypes = AcceptedFileTypesConstants.acceptedFilesTypes;
  isUploadInProgress = false;

  selectedCategoryPath$: Observable<I18nMessage[]>;

  constructor(
    public store: ContractingFilesStoreService,
  ) {
    this.selectedCategoryPath$ = store.selectedCategoryPath$;
    this.files$ = this.getFilesToList();
    this.store.getMaximumAllowedFileSize()
      .pipe(untilDestroyed(this))
      .subscribe((maxAllowedSize) => this.maximumAllowedFileSizeInMB = maxAllowedSize);
    this.canUpload$ = store.selectedCategory$.pipe(
      map(selected => this.isEditable &&
        (selected?.type ===  FileTypeEnum.Contract || selected?.type ===  FileTypeEnum.ContractDoc)),
    );
  }

  ngOnInit(): void {
    this.store.setFileCategories(this.fileCategories());
    this.store.setSection({type: FileTypeEnum.ContractSupport} as CategoryInfo);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.isLocked) {
      this.sectionLockedEvent$.next(changes.isLocked.currentValue);
    }
  }

  private getFilesToList(): Observable<PageFileList> {
    return combineLatest([
      this.store.fileList$,
      this.sectionLockedEvent$.asObservable()
    ]).pipe(
        map(([pageFiles, isLocked]) => ({
          ...pageFiles,
          content: this.transform(pageFiles.content, isLocked),
        } as PageFileList)),
        catchError(error => {
          this.store.error$.next(error.error);
          return of({} as PageFileList);
        })
    );
  }

  downloadFile(file: FileListItem): void {
    this.store.downloadFile(file)
      .pipe(take(1))
      .subscribe();
  }

  uploadFile(target: any): void {
    if (!target) {
      return;
    }

    this.fileSizeOverLimitError$.next(false);
    this.store.error$.next(null);

    if (target?.files[0].size > this.maximumAllowedFileSizeInMB * 1024 * 1024) {
      setTimeout(() => this.fileSizeOverLimitError$.next(true), 10);
      return;
    }

    this.isUploadInProgress = true;
    this.store.uploadFile(target?.files[0])
      .pipe(
        take(1),
        finalize(() => this.isUploadInProgress = false)
      ).subscribe();
  }

  private fileCategories(): Observable<CategoryNode> {
    return of({
          info: {type: FileTypeEnum.ContractSupport},
          name: {i18nKey: 'project.application.contract.and.supporting'},
          children: [
            {
              info: {type: FileTypeEnum.Contract},
              name: {i18nKey: 'project.application.contract.and.supporting.contracts'},
            },
            {
              info: {type: FileTypeEnum.ContractDoc},
              name: {i18nKey: 'project.application.contract.and.supporting.project'},
            },
          ],
    });
  }

  private transform(content: JemsFileDTO[], isLocked: boolean): FileListItem[] {
    return content.map(file => ({
      id: file.id,
      name: file.name,
      type: file.type,
      uploaded: file.uploaded,
      author: file.author,
      sizeString: file.sizeString,
      description: file.description,
      editable: (this.isEditable && (file.type === FileTypeEnum.Contract || file.type === FileTypeEnum.ContractDoc)) && !isLocked,
      deletable: (this.isEditable && (file.type === FileTypeEnum.Contract || file.type === FileTypeEnum.ContractDoc)) && !isLocked,
      tooltipIfNotDeletable: (this.isEditable && !isLocked) ? 'file.table.action.delete.disabled.for.tab.tooltip' : '',
      iconIfNotDeletable: (this.isEditable && !isLocked) ? 'delete_forever' : ''
    }));
  }

  setDescriptionCallback = (data: FileDescriptionChange): Observable<any> => {
    return this.store.setFileDescription(data.id, data.description);
  };

  deleteCallback = (file: FileListItem): Observable<void> => {
    return this.store.deleteFile(file.id);
  };
}
