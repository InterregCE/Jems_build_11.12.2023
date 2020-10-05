import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output
} from '@angular/core';
import { ViewEditForm } from '@common/components/forms/view-edit-form';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InputProjectData, OutputProject } from '@cat/api';
import { Permission } from '../../../../security/permissions/permission';
import { FormState } from '@common/components/forms/form-state';
import { SideNavService } from '@common/components/side-nav/side-nav.service';
import { Tools } from '../../../../common/utils/tools';

@Component({
    selector: 'app-project-application-form',
    templateUrl: './project-application-form.component.html',
    styleUrls: ['./project-application-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormComponent extends ViewEditForm implements OnInit {
    Permission = Permission;
    tools = Tools;

    @Input()
    project: OutputProject;
    @Input()
    editable: boolean;
    @Input()
    priorities: string[];
    @Input()
    objectivesWithPolicies: { [key: string]: InputProjectData.SpecificObjectiveEnum[] };
    @Output()
    updateData = new EventEmitter<InputProjectData>();

    currentPriority: string;
    previousObjective: InputProjectData.SpecificObjectiveEnum;
    currentObjectives: any = [];
    selectedSpecificObjective: InputProjectData.SpecificObjectiveEnum;

    applicationForm: FormGroup = this.formBuilder.group({
        projectId: [''],
        projectAcronym: ['', Validators.compose([
            Validators.maxLength(25),
            Validators.required])
        ],
        projectTitle: ['', Validators.maxLength(250)],
        projectDuration: ['', Validators.compose([
            Validators.max(999),
            Validators.min(1)
        ])],
        projectSummary: ['', Validators.maxLength(2000)],
        programmePriority: ['', Validators.required],
        specificObjective: ['', Validators.required]
    });

    projectAcronymErrors = {
        maxlength: 'project.acronym.size.too.long',
        required: 'project.acronym.should.not.be.empty'
    };
    projectTitleErrors = {
        maxlength: 'project.title.size.too.long',
    };
    projectDurationErrors = {
        max: 'project.duration.size.max',
        min: 'project.duration.size.max',
    };
    projectSummaryErrors = {
        maxlength: 'project.summary.size.too.long'
    };
    programmePriorityErrors = {
        required: 'project.priority.should.not.be.empty'
    }

    constructor(private formBuilder: FormBuilder,
        protected changeDetectorRef: ChangeDetectorRef,
        private sideNavService: SideNavService) {
        super(changeDetectorRef);
    }

    ngOnInit() {
        super.ngOnInit();
        this.changeFormState$.next(FormState.VIEW);
    }

    protected enterViewMode(): void {
        this.applicationForm.controls.programmePriority.setValue(undefined);
        this.currentPriority = '';
        this.selectedSpecificObjective = '' as InputProjectData.SpecificObjectiveEnum;
        this.sideNavService.setAlertStatus(false);
        this.initFields();
    }

    protected enterEditMode(): void {
        this.sideNavService.setAlertStatus(true);
        this.currentObjectives = [];
        this.applicationForm.controls.projectId.disable();
    }

    getForm(): FormGroup | null {
        return this.applicationForm;
    }

    onSubmit(): void {
        this.updateData.emit({
            acronym: this.applicationForm.controls.projectAcronym.value,
            title: this.applicationForm.controls.projectTitle.value,
            duration: this.applicationForm.controls.projectDuration.value,
            introProgrammeLanguage: this.applicationForm.controls.projectSummary.value,
            intro: '',
            specificObjective: this.selectedSpecificObjective
        });
    }

    private initFields() {
        this.applicationForm.controls.projectId.setValue(this.project.id);
        this.applicationForm.controls.projectAcronym.setValue(this.project.acronym);
        this.applicationForm.controls.projectTitle.setValue(this.project?.projectData?.title);
        this.applicationForm.controls.projectDuration.setValue(this.project?.projectData?.duration);
        this.applicationForm.controls.projectSummary.setValue(this.project?.projectData?.introProgrammeLanguage);
        if (this.project?.projectData?.specificObjective) {
            this.previousObjective = this.project?.projectData?.specificObjective.programmeObjectivePolicy;
            this.selectedSpecificObjective = this.project?.projectData?.specificObjective.programmeObjectivePolicy;
            const prevPriority = this.project?.projectData?.programmePriority.code
                + ' - ' + this.project?.projectData?.programmePriority.title;
            this.currentPriority = prevPriority
            this.applicationForm.controls.programmePriority.setValue(prevPriority);
            this.applicationForm.controls.specificObjective.setValue(this.selectedSpecificObjective);
        }
    }

    changeCurrentPriority(selectedPriority: string){
      this.currentPriority = selectedPriority;
      this.applicationForm.controls.specificObjective.setValue('');
    }
}