import {Injectable} from '@angular/core';
import {ProjectStore} from '@project/project-application/containers/project-application-detail/services/project-store.service';
import {BehaviorSubject, combineLatest, Observable, of, Subject} from 'rxjs';
import {PageProjectReportFileDTO, ProjectReportFileMetadataDTO, ProjectSharedFolderFileService, SettingsService, UserRoleCreateDTO} from '@cat/api';
import {CategoryNode} from '@project/common/components/category-tree/categoryModels';
import {Tables} from '@common/utils/tables';
import {APIError} from '@common/models/APIError';
import {catchError, distinctUntilChanged, filter, finalize, map, startWith, switchMap, take, tap} from 'rxjs/operators';
import {v4 as uuid} from 'uuid';
import {DownloadService} from '@common/services/download.service';
import {RoutingService} from '@common/services/routing.service';
import {MatSort} from '@angular/material/sort';
import {FileListTableConstants} from '@common/components/file-list/file-list-table/file-list-table-constants';
import {PermissionService} from '../../../security/permissions/permission.service';
import PermissionsEnum = UserRoleCreateDTO.PermissionsEnum;

@Injectable()
export class SharedFolderPageStore {

  projectId$ = this.projectStore.projectId$;
  projectTitle$ = this.projectStore.projectTitle$;
  fileList$: Observable<PageProjectReportFileDTO>;
  fileCategories$: Observable<CategoryNode>;
  userCanEdit$: Observable<boolean>;

  newPageSize$ = new BehaviorSubject<number>(Tables.DEFAULT_INITIAL_PAGE_SIZE);
  newPageIndex$ = new BehaviorSubject<number>(0);
  filesChanged$ = new Subject<void>();

  newSort$ = new BehaviorSubject<Partial<MatSort>>(FileListTableConstants.DEFAULT_SORT);
  error$ = new Subject<APIError | null>();

  constructor(private readonly projectStore: ProjectStore,
              private readonly sharedFolderService: ProjectSharedFolderFileService,
              private readonly downloadService: DownloadService,
              private readonly routingService: RoutingService,
              private readonly settingsService: SettingsService,
              private readonly permissionService: PermissionService) {
    this.projectId$ = this.projectStore.projectId$;
    this.fileList$ = this.fileList();
    this.fileCategories$ = this.getCategories();
    this.userCanEdit$ = this.userCanEdit();
  }

  private getCategories(): Observable<CategoryNode> {
    return new BehaviorSubject({
      name: {i18nKey: 'project.application.form.section.shared.folder'},
      info: {type: 'SHARED_FOLDER'},
      children: []
    } as CategoryNode).asObservable();
  }

  private fileList(): Observable<PageProjectReportFileDTO> {
    return combineLatest([
      this.projectId$,
      this.newPageIndex$,
      this.newPageSize$,
      this.newSort$.pipe(
        map(sort => sort?.direction ? sort : FileListTableConstants.DEFAULT_SORT),
        map(sort => `${sort.active},${sort.direction}`),
        distinctUntilChanged(),
      ),
      this.filesChanged$.pipe(startWith(null)),
    ])
      .pipe(
        filter(([projectId]) => !!projectId),
        switchMap(([projectId, pageIndex, pageSize, sort]) =>
          this.sharedFolderService.listSharedFolderFiles(
            projectId,
            pageIndex,
            pageSize,
            sort
          )
        ),
        tap(page => {
          if (page.totalPages > 0 && page.number >= page.totalPages) {
            this.newPageIndex$.next(page.totalPages - 1);
          }
        }),
        catchError(error => {
          this.error$.next(error.error);
          return of({} as PageProjectReportFileDTO);
        })
      );
  }


  private userCanEdit(): Observable<boolean> {
    return combineLatest([
      // TODO: anything more complicated?
      this.permissionService.hasPermission(PermissionsEnum.ProjectCreatorSharedFolderEdit),
      this.permissionService.hasPermission(PermissionsEnum.ProjectMonitorSharedFolderEdit),
      this.projectStore.userIsEditOrManageCollaborator$,
    ]).pipe(
      map(([hasCreatorPermission, hasMonitorPermission, isEditCollaborator]) => (hasCreatorPermission || hasMonitorPermission) && isEditCollaborator),
    );
  }

  uploadFile(file: File): Observable<ProjectReportFileMetadataDTO> {
    const serviceId = uuid();
    this.routingService.confirmLeaveMap.set(serviceId, true);
    return combineLatest([
      this.projectId$,
    ]).pipe(
      take(1),
      switchMap(([projectId]) => this.sharedFolderService.uploadFileToSharedFolderForm(file, projectId)),
      tap(() => this.filesChanged$.next()),
      tap(() => this.error$.next(null)),
      catchError(error => {
        this.error$.next(error.error);
        return of({} as ProjectReportFileMetadataDTO);
      }),
      finalize(() => this.routingService.confirmLeaveMap.delete(serviceId))
    );
  }

  downloadFile(fileId: number): Observable<any> {
    return combineLatest([
      this.projectId$.pipe(map(id => Number(id))),
    ]).pipe(
      take(1),
      switchMap(([projectId]) => {
        this.downloadService.download(`/api/project/${projectId}/sharedFolder/byFileId/${fileId}/download`, 'project-report');
        return of(null);
      }),
    );
  }

  setDescriptionToFile(fileId: number, description: string) {
    return combineLatest([
      this.projectId$.pipe(map(Number)),
    ]).pipe(
      switchMap(([projectId]) =>
        this.sharedFolderService.setDescriptionToSharedFolderFile(fileId, projectId, description)
      ),
    );
  }

  deleteFile(fileId: number) {
    return combineLatest([
      this.projectId$.pipe(map(Number)),
    ]).pipe(
      switchMap(([projectId]) =>
        this.sharedFolderService.deleteSharedFolderFile(fileId, projectId)
      ),
    );
  }

  getMaximumAllowedFileSize(): Observable<number> {
    return this.settingsService.getMaximumAllowedFileSize();
  }
}
