import {ChangeDetectionStrategy, Component} from '@angular/core';
import {InputProjectStatus, OutputProjectFile, ProjectFileStorageService, ProjectService, ProjectStatusService} from '@cat/api';
import {ActivatedRoute} from '@angular/router';
import {ProjectFileService} from '../../services/project-file.service';
import {MatDialog} from '@angular/material/dialog';
import {Permission} from '../../../../security/permissions/permission';
import {combineLatest, merge, Observable, Subject} from 'rxjs';
import {catchError, flatMap, map, startWith, take, takeUntil, tap} from 'rxjs/operators';
import {Log} from '../../../../common/utils/log';
import {BaseComponent} from '@common/components/base-component';
import {HttpErrorResponse} from '@angular/common/http';
import {MatSort} from '@angular/material/sort';
import {Tables} from '../../../../common/utils/tables';
import {Forms} from '../../../../common/utils/forms';

@Component({
  selector: 'app-project-application-detail',
  templateUrl: './project-application-detail.component.html',
  styleUrls: ['./project-application-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationDetailComponent extends BaseComponent {
  Permission = Permission

  fileNumber = 0;
  projectId = this.activatedRoute.snapshot.params.projectId;
  statusMessages: string[];
  refreshCustomColumns$ = new Subject<null>();
  updateProject$ = new Subject<InputProjectStatus>();
  projectSubmittedSuccess$ = new Subject<boolean>();
  disableActionsAfterSubmit$ = new Subject<null>();

  project$ =
    this.projectService.getProjectById(this.projectId)
      .pipe(
        take(1),
        takeUntil(this.destroyed$),
        tap(project => Log.info('Fetched project:', this, project))
      )

  private updatedProject$ = this.updateProject$
    .pipe(
      flatMap(statusUpdate => this.projectStatusService.setProjectStatus(this.projectId, statusUpdate)),
      tap(saved => Log.info('Updated status:', this, saved)),
      tap(() => this.projectSubmittedSuccess$.next(true)),
      tap(() => this.disableActionsAfterSubmit$.next()),
    );

  details$ = combineLatest([
    merge(this.project$, this.updatedProject$)
  ])
    .pipe(
      map( ([project]) => (project)),
    );

  newPageSize$ = new Subject<number>();
  newPageIndex$ = new Subject<number>();
  refreshPage$ = new Subject<void>();
  newSort$ = new Subject<Partial<MatSort>>();

  currentPage$ =
    combineLatest([
      this.newPageIndex$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      this.newPageSize$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE_SIZE)),
      this.newSort$.pipe(
        startWith(Tables.DEFAULT_INITIAL_SORT),
        map(sort => sort?.direction ? sort : Tables.DEFAULT_INITIAL_SORT),
        map(sort => `${sort.active},${sort.direction}`)
      ),
      this.refreshPage$.pipe(startWith(null))
    ])
      .pipe(
        flatMap(([pageIndex, pageSize, sort]) =>
          this.projectFileStorageService.getFilesForProject(this.projectId, pageIndex, pageSize, sort)),
        tap(page => Log.info('Fetched the projects:', this, page.content)),
        tap(() => this.refreshCustomColumns$.next()),
        tap(page => this.fileNumber = page.totalElements),
      );

  STATUS_MESSAGE_SUCCESS = (filename: string) => `Upload of '${filename}' successful.`;
  ERROR_MESSAGE_UPLOAD = (filename: string) => `Upload of '${filename}' not successful.`;
  ERROR_MESSAGE_EXISTS = (filename: string) => `File '${filename}' already exists.`;

  constructor(private projectService: ProjectService,
              private projectFileStorageService: ProjectFileStorageService,
              private projectFileService: ProjectFileService,
              private projectStatusService: ProjectStatusService,
              private dialog: MatDialog,
              private activatedRoute: ActivatedRoute) {
    super();
  }

  refreshCustomColumns(): Observable<null> {
    return this.refreshCustomColumns$.asObservable();
  }

  addNewFilesForUpload($event: File) {
    this.projectFileService.addProjectFile(this.projectId, $event).pipe(
      takeUntil(this.destroyed$),
      tap(() => this.newPageIndex$.next(Tables.DEFAULT_INITIAL_PAGE_INDEX)),
      catchError((error: HttpErrorResponse) => {
        this.addErrorFromResponse(error, $event.name);
        throw error;
      })
    ).subscribe(() => {
      this.addMessageFromResponse(this.STATUS_MESSAGE_SUCCESS($event.name));
    });
  }

  downloadFile(element: OutputProjectFile) {
    window.open(
      this.projectFileService.getDownloadLink(this.projectId, element.id),
      '_blank',
    );
  }

  deleteFile(element: OutputProjectFile) {
    Forms.confirmDialog(
      this.dialog,
      element.name,
      'Are you sure you want to delete' + ' ' + element.name + ' ?',
    ).pipe(
      take(1),
      takeUntil(this.destroyed$)
    ).subscribe(clickedYes => {
      if (clickedYes) {
        this.projectFileStorageService.deleteFile(element.id, this.projectId).pipe(
          takeUntil(this.destroyed$),
          tap(() => this.newPageIndex$.next(Tables.DEFAULT_INITIAL_PAGE_SIZE))
        ).subscribe();
      }
    });
  }

  saveDescription(data: any): void {
    this.projectFileStorageService.setDescriptionToFile(data.fileIdentifier, this.projectId, data.description).pipe(
      take(1),
      takeUntil(this.destroyed$),
      tap(() => this.refreshPage$.next()),
    ).subscribe();
  }

  private addMessageFromResponse(status: string) {
    if (!this.statusMessages) {
      this.statusMessages = [];
    }
    this.statusMessages.unshift(status);
  }

  private addErrorFromResponse(status: any, filename: string) {
    if (status.error && status.status === 422) {
      this.addMessageFromResponse(this.ERROR_MESSAGE_EXISTS(filename));
    } else {
      this.addMessageFromResponse(this.ERROR_MESSAGE_UPLOAD(filename));
    }
  }
}