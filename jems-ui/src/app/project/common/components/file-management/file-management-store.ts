import {Injectable} from '@angular/core';
import {combineLatest, Observable, of, ReplaySubject, Subject} from 'rxjs';
import {
  PageProjectFileMetadataDTO,
  ProjectCallSettingsDTO,
  ProjectFileMetadataDTO,
  ProjectFileService,
  ProjectPartnerSummaryDTO,
  ProjectStatusDTO, SettingsService,
  UserRoleDTO
} from '@cat/api';
import {ProjectStore} from '@project/project-application/containers/project-application-detail/services/project-store.service';
import {catchError, map, startWith, switchMap, take, tap, withLatestFrom} from 'rxjs/operators';
import {MatSort} from '@angular/material/sort';
import {Tables} from '@common/utils/tables';
import {CategoryInfo, CategoryNode} from '@project/common/components/category-tree/categoryModels';
import {FileCategoryTypeEnum} from '@project/common/components/file-management/file-category-type';
import {APIError} from '@common/models/APIError';
import {InvestmentSummary} from '@project/work-package/project-work-package-page/work-package-detail-page/workPackageInvestment';
import {PermissionService} from '../../../../security/permissions/permission.service';
import {ProjectUtil} from '@project/common/project-util';
import {I18nMessage} from '@common/models/I18nMessage';
import {ProjectPartnerStore} from '@project/project-application/containers/project-application-form-page/services/project-partner-store.service';
import {FormVisibilityStatusService} from '@project/common/services/form-visibility-status.service';
import {APPLICATION_FORM} from '@project/common/application-form-model';
import PermissionsEnum = UserRoleDTO.PermissionsEnum;
import {DownloadService} from '@common/services/download.service';
import CallTypeEnum = ProjectCallSettingsDTO.CallTypeEnum;

@Injectable({
  providedIn: 'root'
})
export class FileManagementStore {

  fileList$: Observable<PageProjectFileMetadataDTO>;
  fileCategories$: Observable<CategoryNode>;
  selectedCategory$ = new ReplaySubject<CategoryInfo | undefined>(1);
  selectedCategoryPath$: Observable<I18nMessage[]>;

  projectStatus$: Observable<ProjectStatusDTO>;
  userIsProjectOwnerOrEditCollaborator$: Observable<boolean>;

  canUpload$: Observable<boolean>;
  canChangeAssessmentFile$: Observable<boolean>;
  canChangeApplicationFile$: Observable<boolean>;
  canReadAssessmentFile$: Observable<boolean>;
  canReadApplicationFile$: Observable<boolean>;
  canReadFiles$: Observable<boolean>;

  deleteSuccess$ = new Subject<boolean>();
  error$ = new Subject<APIError | null>();

  newPageSize$ = new Subject<number>();
  newPageIndex$ = new Subject<number>();
  newSort$ = new Subject<Partial<MatSort>>();
  filesChanged$ = new Subject<void>();

  constructor(private projectFileService: ProjectFileService,
              private settingsService: SettingsService,
              private projectStore: ProjectStore,
              private projectPartnerStore: ProjectPartnerStore,
              private permissionService: PermissionService,
              private visibilityStatusService: FormVisibilityStatusService,
              private downloadService: DownloadService
  ) {
    this.projectStatus$ = this.projectStore.projectStatus$;
    this.userIsProjectOwnerOrEditCollaborator$ = this.projectStore.userIsProjectOwnerOrEditCollaborator$;
    this.canChangeAssessmentFile$ = this.permissionService.hasPermission(PermissionsEnum.ProjectFileAssessmentUpdate);
    this.canChangeApplicationFile$ = this.permissionService.hasPermission(PermissionsEnum.ProjectFileApplicationUpdate);
    this.canReadApplicationFile$ = this.canReadApplicationFile();
    this.canReadAssessmentFile$ = this.permissionService.hasPermission(PermissionsEnum.ProjectFileAssessmentRetrieve);
    this.canUpload$ = this.canUpload();
    this.canReadFiles$ = this.canReadFiles();
    this.selectedCategoryPath$ = this.selectedCategoryPath();
    this.fileList$ = this.fileList();
  }

  setSection(section: CategoryInfo): void {
    this.selectedCategory$.next(section);
    this.fileCategories$ = this.fileCategories(section);
  }

  uploadFile(file: File): Observable<ProjectFileMetadataDTO> {
    return this.selectedCategory$
      .pipe(
        take(1),
        withLatestFrom(this.projectStore.projectId$),
        switchMap(([category, projectId]) => this.projectFileService.uploadFileForm(file, projectId, (category as any)?.id, (category as any)?.type)),
        tap(() => this.filesChanged$.next()),
        tap(() => this.error$.next(null)),
        catchError(error => {
          this.error$.next(error.error);
          return of({} as ProjectFileMetadataDTO);
        })
      );
  }

  deleteFile(fileId: number): Observable<void> {
    return this.projectStore.projectId$
      .pipe(
        take(1),
        switchMap(projectId => this.projectFileService.deleteProjectFile(fileId, projectId)),
        tap(() => this.filesChanged$.next()),
        tap(() => this.deleteSuccess$.next(true)),
        tap(() => setTimeout(() => this.deleteSuccess$.next(false), 3000)),
        catchError(error => {
          this.error$.next(error.error);
          return of({} as ProjectFileMetadataDTO);
        })
      );
  }

  setFileDescription(fileId: number, description: string): Observable<ProjectFileMetadataDTO> {
    return this.projectStore.projectId$
      .pipe(
        take(1),
        switchMap(projectId => this.projectFileService.setProjectFileDescription(fileId, projectId, description)),
        catchError(error => {
          this.error$.next(error.error);
          return of({} as ProjectFileMetadataDTO);
        })
      );
  }

  downloadFile(fileId: number): Observable<any> {
    return this.projectStore.projectId$
      .pipe(
        take(1),
        switchMap(projectId => {
         this.downloadService.download(`/api/project/${projectId}/file/download/${fileId}`, 'translation.properties');
          return of(null);
        })
      );
  }

  private setParent(node: CategoryNode): void {
    node?.children?.forEach(child => {
      child.parent = node;
      this.setParent(child);
    });
  }

  private canUpload(): Observable<boolean> {
    return combineLatest([
      this.selectedCategory$,
      this.projectStatus$,
      this.canChangeAssessmentFile$,
      this.canChangeApplicationFile$,
      this.permissionService.hasPermission(PermissionsEnum.ProjectModificationFileAssessmentUpdate),
      this.userIsProjectOwnerOrEditCollaborator$,
    ]).pipe(
      map(([selectedCategory, projectStatus, canUploadAssessmentFile, canUploadApplicationFile, canUploadModificationFile, userIsProjectOwnerOrEditCollaborator]) => {
        if (selectedCategory?.type === FileCategoryTypeEnum.ASSESSMENT) {
          return canUploadAssessmentFile;
        }
        if (selectedCategory?.type === FileCategoryTypeEnum.MODIFICATION) {
          return canUploadModificationFile;
        }
        if (!ProjectUtil.isOpenForModifications(projectStatus)) {
          return false;
        }
        if (selectedCategory?.type === FileCategoryTypeEnum.APPLICATION || selectedCategory?.id) {
          return canUploadApplicationFile || userIsProjectOwnerOrEditCollaborator;
        }
        return false;
      })
    );
  }

  private canReadFiles(): Observable<boolean> {
    return combineLatest([
      this.permissionService.hasPermission(PermissionsEnum.ProjectFileAssessmentRetrieve),
      this.canReadApplicationFile$
    ]).pipe(
      map(([canReadAssessment, canReadApplication]) => canReadAssessment || canReadApplication)
    );
  }

  private fileList(): Observable<PageProjectFileMetadataDTO> {
    return combineLatest([
      this.selectedCategory$,
      this.projectStore.projectId$,
      this.newPageIndex$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      this.newPageSize$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_SIZE)),
      this.newSort$.pipe(
        startWith(Tables.DEFAULT_INITIAL_SORT),
        map(sort => sort?.direction ? sort : Tables.DEFAULT_INITIAL_SORT),
        map(sort => `${sort.active},${sort.direction}`)
      ),
      this.filesChanged$.pipe(startWith(null))
    ])
      .pipe(
        switchMap(([category, projectId, pageIndex, pageSize, sort]) =>
          this.projectFileService.listProjectFiles(projectId, (category as any)?.id, pageIndex, pageSize, sort, (category as any)?.type)
        ),
        catchError(error => {
          this.error$.next(error.error);
          return of({} as PageProjectFileMetadataDTO);
        })
      );
  }

  private fileCategories(section: CategoryInfo): Observable<CategoryNode> {
    return combineLatest([
      this.projectStore.projectTitle$,
      this.projectStore.projectCallType$,
      this.canReadApplicationFile$.pipe(switchMap(canReadApplicationFile => this.shouldFetchApplicationCategories(section, canReadApplicationFile) ? this.projectPartnerStore.latestPartnerSummaries$ : of([]))),
      this.canReadApplicationFile$.pipe(switchMap(canReadApplicationFile => this.shouldFetchApplicationCategories(section, canReadApplicationFile) ? this.projectStore.investmentSummariesForFiles$ : of([]))),
      this.canReadApplicationFile$,
      this.canReadAssessmentFile$
    ]).pipe(
      map(([projectTitle, callType, partners, investments, canReadApplicationFiles, canReadAssessmentFiles]) =>
        this.getCategories(section, projectTitle, partners, investments, canReadApplicationFiles, canReadAssessmentFiles, callType)
      ),
      tap(filters => this.setParent(filters)),
    );
  }

  private getCategories(section: CategoryInfo,
                        projectTitle: string,
                        partners: ProjectPartnerSummaryDTO[],
                        investments: InvestmentSummary[],
                        canReadApplicationFiles: boolean,
                        canReadAssessmentFiles: boolean,
                        callType: CallTypeEnum): CategoryNode {
    const fullTree: CategoryNode = {
      name: {i18nKey: projectTitle},
      info: {type: FileCategoryTypeEnum.ALL},
      children: []
    };
    if (canReadApplicationFiles) {
      const applicationFiles: CategoryNode = {
        name: {i18nKey: 'file.tree.type.attachments'},
        info: {type: FileCategoryTypeEnum.APPLICATION},
        children: []
      };
      applicationFiles.children?.push(
        {
          name: {i18nKey: 'file.tree.type.partner'},
          info: {type: FileCategoryTypeEnum.PARTNER},
          children: partners.map(partner => ({
            name: {
              i18nKey: ProjectPartnerStore.getPartnerTranslationKey(partner.role, callType),
              i18nArguments: {partner: `${partner.sortNumber || ''} ${partner.abbreviation}`}
            },
            info: {type: FileCategoryTypeEnum.PARTNER, id: partner.id}
          }))
        });
      if (this.visibilityStatusService.isVisible(APPLICATION_FORM.SECTION_C.PROJECT_WORK_PLAN.INVESTMENTS)) {
        applicationFiles.children?.push(
          {
            name: {i18nKey: 'file.tree.type.investment'},
            info: {type: FileCategoryTypeEnum.INVESTMENT},
            children: investments.map(investment => ({
              name: {i18nKey: investment.toString()},
              info: {type: FileCategoryTypeEnum.INVESTMENT, id: investment.id}
            }))
          }
        );
      }
      fullTree.children?.push(applicationFiles);
    }
    if (canReadAssessmentFiles) {
      fullTree.children?.push({
        name: {i18nKey: 'file.tree.type.assessment'},
        info: {type: FileCategoryTypeEnum.ASSESSMENT},
        children: []
      });
    }
    fullTree.children?.push({
      name: {i18nKey: 'file.tree.type.modification'},
      info: {type: FileCategoryTypeEnum.MODIFICATION},
      children: []
    });
    return this.findRootForSection(fullTree, section) || {};
  }

  public findRootForSection(root: CategoryNode, section: CategoryInfo): CategoryNode | null {
    if (root.info?.type === section.type && root.info?.id === section.id) {
      return root;
    }
    if (root?.children) {
      for (const child of root.children) {
        const potentialRoot = this.findRootForSection(child, section);
        if (potentialRoot != null) {
          return potentialRoot;
        }
      }
    }
    return null;
  }

  private canReadApplicationFile(): Observable<boolean> {
    return combineLatest([
      this.permissionService.hasPermission(PermissionsEnum.ProjectFileApplicationRetrieve),
      this.projectStore.userIsProjectOwner$
    ])
      .pipe(
        map(([canReadApplicationFile, userIsProjectOwner]) => canReadApplicationFile || userIsProjectOwner)
      );
  }

  private selectedCategoryPath(): Observable<I18nMessage[]> {
    return combineLatest([this.selectedCategory$, this.fileCategories$])
      .pipe(
        map(([selectedCategory, fileCategories]) =>
          ([{i18nKey: 'file.tree.type.all'}, ...this.getPath(selectedCategory as any, fileCategories)])
        )
      );
  }

  public getPath(selectedCategory: CategoryInfo, node: CategoryNode): I18nMessage[] {
    if (node.info?.type === selectedCategory?.type && (!selectedCategory?.id || selectedCategory.id === node.info?.id)) {
      return [node.name as any];
    }

    if (node.children && node.children.length > 0) {
      let potentialPath: I18nMessage[] = [];
      for (const child of node.children) {
        potentialPath = this.getPath(selectedCategory, child);
        if (potentialPath.filter(it => it.i18nKey === 'INVALID_PATH').length === 0) {
          break;
        }
      }
      return [node.name as any, ...potentialPath];
    }

    return [{i18nKey: 'INVALID_PATH'}];
  }

  private shouldFetchApplicationCategories(section: CategoryInfo, canReadApplicationFile: boolean): boolean {
    return section.type !== FileCategoryTypeEnum.ASSESSMENT && canReadApplicationFile;
  }

  getMaximumAllowedFileSize(): Observable<number> {
    return this.settingsService.getMaximumAllowedFileSize();
  }
}