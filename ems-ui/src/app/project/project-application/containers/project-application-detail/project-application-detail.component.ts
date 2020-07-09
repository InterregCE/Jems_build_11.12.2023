import {ChangeDetectionStrategy, Component} from '@angular/core';
import {OutputProjectFile, ProjectFileStorageService, ProjectService} from '@cat/api';
import {ActivatedRoute} from '@angular/router';
import {ProjectFileService} from '../../services/project-file.service';
import {MatDialog} from '@angular/material/dialog';
import {Permission} from '../../../../security/permissions/permission';
import {combineLatest, Observable, Subject} from 'rxjs';
import {catchError, flatMap, map, startWith, take, takeUntil, tap} from 'rxjs/operators';
import {PageEvent} from '@angular/material/paginator';
import {Log} from '../../../../common/utils/log';
import {BaseComponent} from '@common/components/base-component';
import {HttpErrorResponse} from '@angular/common/http';
import {DeleteDialogComponent} from '../../components/project-application-detail/delete-dialog/delete-dialog.component';
import {MatSort} from '@angular/material/sort';
import {Tables} from '../../../../common/utils/tables';

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

  project$ =
    this.projectService.getProjectById(Number(this.projectId))
      .pipe(
        take(1),
        takeUntil(this.destroyed$)
      )

  newPage$ = new Subject<PageEvent>();
  newSort$ = new Subject<Partial<MatSort>>();

  currentPage$ =
    combineLatest([
      this.newPage$.pipe(startWith(Tables.DEFAULT_INITIAL_PAGE)),
      this.newSort$.pipe(
        startWith(Tables.DEFAULT_INITIAL_SORT),
        map(sort => sort?.direction ? sort : Tables.DEFAULT_INITIAL_SORT),
        map(sort => `${sort.active},${sort.direction}`)
      )
    ])
      .pipe(
        flatMap(([page, sort]) =>
          this.projectFileStorageService.getFilesForProject(this.projectId, page?.pageIndex, page?.pageSize, sort)),
        tap(page => Log.info('Fetched the projects:', this, page.content)),
        tap(() => this.refreshCustomColumns$.next()),
        tap(page => this.fileNumber = page.content.length),
      );

  STATUS_MESSAGE_SUCCESS = (filename: string) => `Upload of '${filename}' successful.`;
  ERROR_MESSAGE_UPLOAD = (filename: string) => `Upload of '${filename}' not successful.`;
  ERROR_MESSAGE_EXISTS = (filename: string) => `File '${filename}' already exists.`;

  constructor(private projectService: ProjectService,
              private projectFileStorageService: ProjectFileStorageService,
              private projectFileService: ProjectFileService,
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
      tap(() => this.newPage$.next(Tables.DEFAULT_INITIAL_PAGE)),
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
    const dialogRef = this.dialog.open(DeleteDialogComponent, {
      minWidth: '30rem',
      data: {name: element.name}
    });

    dialogRef.afterClosed().subscribe((clickedYes: boolean) => {
      if (clickedYes) {
        this.projectFileStorageService.deleteFile(element.id, this.projectId).pipe(
          takeUntil(this.destroyed$),
          tap(() => this.newPage$.next(Tables.DEFAULT_INITIAL_PAGE))
        ).subscribe();
      }
    });
  }

  saveDescription(data: any): void {
    this.projectFileStorageService.setDescriptionToFile(data.fileIdentifier, this.projectId, data.description).pipe(
      takeUntil(this.destroyed$),
      tap(() => this.newPage$.next(Tables.DEFAULT_INITIAL_PAGE))
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
