import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {JemsFileDTO, JemsFileMetadataDTO, ProjectFileMetadataDTO, ProjectPartnerReportSummaryDTO, ProjectStatusDTO} from '@cat/api';
import {FileCategoryTypeEnum} from '@project/common/components/file-management/file-category-type';
import {ProjectUtil} from '@project/common/project-util';

@Component({
  selector: 'jems-actions-cell',
  templateUrl: './actions-cell.component.html',
  styleUrls: ['./actions-cell.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionsCellComponent {
  @Input()
  file: ProjectFileMetadataDTO;
  @Input()
  reportFile: JemsFileMetadataDTO;
  @Input()
  type: FileCategoryTypeEnum;
  @Input()
  reportFileType: JemsFileDTO.TypeEnum;
  @Input()
  projectStatus: ProjectStatusDTO;
  @Input()
  reportStatus: ProjectPartnerReportSummaryDTO.StatusEnum;

  @Input()
  isAllowedToChangeApplicationFile: boolean;
  @Input()
  isAllowedToChangeAssessmentFile: boolean;
  @Input()
  isAllowedToChangeModificationFile: boolean;
  @Input()
  isThisUserOwner: boolean;
  @Input()
  isReport: boolean;

  @Output()
  edit = new EventEmitter<number>();
  @Output()
  download = new EventEmitter<number>();
  @Output()
  delete = new EventEmitter<ProjectFileMetadataDTO>();
  @Output()
  deleteReportFile = new EventEmitter<JemsFileMetadataDTO>();

  canChangeFile(): boolean {
    if (this.type === FileCategoryTypeEnum.ALL) {
      return false;
    }

    if (this.type === FileCategoryTypeEnum.MODIFICATION) {
      return this.canChangeModificationFile();
    }

    if (this.type === FileCategoryTypeEnum.ASSESSMENT) {
      return this.canChangeAssessmentFile();
    }

    if (this.type === FileCategoryTypeEnum.APPLICATION) {
      return this.canChangeApplicationFile();
    }

    return false;
  }

  canDeleteFile(): boolean {
    if (this.type === FileCategoryTypeEnum.ALL) {
      return false;
    }

    if (this.type === FileCategoryTypeEnum.MODIFICATION) {
      return this.canChangeModificationFile();
    }

    if (this.type === FileCategoryTypeEnum.ASSESSMENT) {
      return this.canChangeAssessmentFile();
    }

    if (this.type === FileCategoryTypeEnum.APPLICATION) {
      return this.canDeleteApplicationFile();
    }

    return false;
  }

  canDeleteReport(): boolean {
    return this.reportStatus === ProjectPartnerReportSummaryDTO.StatusEnum.Draft
      && this.reportFileType === JemsFileDTO.TypeEnum.PartnerReport;
  }

  private canChangeApplicationFile(): boolean {
    return this.isAllowedToChangeApplicationFile
      || (this.isThisUserOwner && ProjectUtil.isOpenForModifications(this.projectStatus));
  }

  private canChangeAssessmentFile(): boolean {
    return this.isAllowedToChangeAssessmentFile;
  }

  private canDeleteApplicationFile(): boolean {
    // the user can only delete files that are added after a last status change
    const lastStatusChange = this.projectStatus?.updated;
    const fileIsNotLocked = this.file.uploadedAt > lastStatusChange;

    const userIsAbleToDelete = this.isAllowedToChangeApplicationFile
      || (this.isThisUserOwner && ProjectUtil.isOpenForModifications(this.projectStatus));
    return fileIsNotLocked && userIsAbleToDelete;
  }

  private canChangeModificationFile(): boolean {
    return this.isAllowedToChangeModificationFile;
  }
}
