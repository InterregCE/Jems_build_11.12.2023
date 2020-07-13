import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {TableConfiguration} from '@common/components/table/model/table.configuration';
import {ActionConfiguration} from '@common/components/table/model/action.configuration';
import {ColumnType} from '@common/components/table/model/column-type.enum';
import {DescriptionCellComponent} from '@common/components/table/cell-renderers/description-cell/description-cell.component';
import {combineLatest, Observable} from 'rxjs';
import {take, takeUntil} from 'rxjs/operators';
import {TableComponent} from '@common/components/table/table.component';
import {PermissionService} from '../../../../../security/permissions/permission.service';
import {InputProjectFileDescription, OutputProject, OutputProjectFile, OutputProjectStatus, PageOutputProjectFile} from '@cat/api';
import {Permission} from '../../../../../security/permissions/permission';
import {BaseComponent} from '@common/components/base-component';
import {Tables} from '../../../../../common/utils/tables';
import {MatSort} from '@angular/material/sort';

@Component({
  selector: 'app-project-application-files-list',
  templateUrl: './project-application-files-list.component.html',
  styleUrls: ['./project-application-files-list.component.scss']
})
export class ProjectApplicationFilesListComponent extends BaseComponent implements OnInit {
  Tables = Tables;

  @Input()
  project$: Observable<OutputProject>
  @Input()
  filePage: PageOutputProjectFile;
  @Input()
  refreshCustomColumns$: Observable<null>;
  @Input()
  pageIndex: number;
  @Input()
  disableActions$: Observable<null>;
  @Output()
  deleteFile = new EventEmitter<OutputProjectFile>();
  @Output()
  downloadFile = new EventEmitter<OutputProjectFile>();
  @Output()
  saveDescription = new EventEmitter<any>();
  @Output()
  newPageSize: EventEmitter<number> = new EventEmitter<number>();
  @Output()
  newPageIndex: EventEmitter<number> = new EventEmitter<number>();
  @Output()
  newSort: EventEmitter<Partial<MatSort>> = new EventEmitter<Partial<MatSort>>();

  @ViewChild(TableComponent) table: TableComponent;
  OutputProjectStatus = OutputProjectStatus;

  tableConfiguration: TableConfiguration = new TableConfiguration({
    routerLink: '/project/',
    isTableClickable: false,
    actionColumn: true,
    actions: [],
    columns: [
      {
        displayedColumn: 'file.table.column.name.name',
        elementProperty: 'name',
        columnType: ColumnType.String,
        sortProperty: 'name'
      },
      {
        displayedColumn: 'file.table.column.name.timestamp',
        elementProperty: 'updated',
        columnType: ColumnType.Date,
        sortProperty: 'updated'
      },
      {
        displayedColumn: 'file.table.column.name.username',
        elementProperty: 'author.email',
        columnType: ColumnType.String,
        sortProperty: 'author.email'
      },
      {
        displayedColumn: 'file.table.column.name.description',
        elementProperty: 'description',
        columnType: ColumnType.CustomComponent,
        component: DescriptionCellComponent,
        extraProps: {
          onSave: (value: string, index: number, fileId: number) => this.onSave(value, index, fileId),
          onCancel: (index: number) => this.onCancel(index),
          readOnly: true,
        },
        sortProperty: 'description'
      }
    ]
  });

  editAction = new ActionConfiguration('fas fa-edit',
    (element: any, index: number) => this.editFileDescription(element, index));
  downloadAction = new ActionConfiguration('fas fa-file-download',
    (element: OutputProjectFile) => this.onDownload(element));
  deleteAction = new ActionConfiguration('fas fa-trash',
    (element: OutputProjectFile) => this.onDelete(element));

  constructor(private permissionService: PermissionService) {
    super();
  }

  ngOnInit() {
    this.assignActionsToUser();
    this.refreshCustomColumns$.subscribe(() => {
      if (this.table) {
        setTimeout(() => this.table.createCustomComponents(), 0);
      }
    })
    this.disableActions$
      .pipe(takeUntil(this.destroyed$))
      .subscribe(() => {
      this.tableConfiguration.actions = [this.downloadAction];
    })
    this.project$
      .pipe(takeUntil(this.destroyed$))
      .subscribe((project) => {
        if (project.projectStatus.status === OutputProjectStatus.StatusEnum.SUBMITTED) {
          this.tableConfiguration.actions = [this.downloadAction];
        }
      })
  }

  onDownload(element: OutputProjectFile) {
    this.downloadFile.emit(element);
  }

  editFileDescription(element: any, rowIndex: number) {
    this.table.changeCustomColumnData(rowIndex, {
      onSave: (value: string, index: number, fileId: number) => this.onSave(value, index, fileId),
      onCancel: (index: number) => this.onCancel(index),
      readOnly: false,
    });
  }

  onDelete(element: OutputProjectFile) {
    this.deleteFile.emit(element);
  }

  onCancel(rowIndex: number): void {
    this.closeInputFieldAndMakeReadonly(rowIndex);
  }

  onSave(saveValue: string, rowIndex: number, fileId: number): void {
    const descriptionText = {description: saveValue} as InputProjectFileDescription;
    this.saveDescription.emit({fileIdentifier: fileId, description: descriptionText})
    this.closeInputFieldAndMakeReadonly(rowIndex);
  }

  closeInputFieldAndMakeReadonly(rowIndex: number): void {
    this.table.changeCustomColumnData(rowIndex, {
      onSave: (value: string, index: number, fileId: number) => this.onSave(value, index, fileId),
      onCancel: (index: number) => this.onCancel(index),
      readOnly: true,
    });
  }

  assignActionsToUser(): void {
    combineLatest([
      this.permissionService.hasPermission(Permission.APPLICANT_USER),
      this.permissionService.hasPermission(Permission.PROGRAMME_USER),
      this.permissionService.hasPermission(Permission.ADMINISTRATOR)
    ])
      .pipe(
        take(1),
        takeUntil(this.destroyed$)
      )
      .subscribe(([isApplicant, isProgramme, isAdmin]) => {
        if (isApplicant || isAdmin) {
          this.tableConfiguration.actions = [this.editAction, this.downloadAction, this.deleteAction];
          return;
        }
        if (isProgramme) {
          this.tableConfiguration.actions = [this.downloadAction];
          return;
        }
      });
  }
}