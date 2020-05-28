import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {InputProject} from 'build/generated-sources/openapi/model/models';

@Component({
  selector: 'app-project-application-submission',
  templateUrl: 'project-application-submission.component.html',
  styleUrls: ['project-application-submission.component.scss']
})

export class ProjectApplicationSubmissionComponent {
  @ViewChild('projectApplicationSubmitForm', {static: false}) projectApplicationSubmitForm: NgForm;
  @Input()
  success: boolean;
  @Input()
  error: boolean;
  @Input()
  errorMessages: string[];

  @Output()
  submitProjectApplication: EventEmitter<InputProject> = new EventEmitter<InputProject>();

  project = {} as InputProject;

  onSubmit() {
    this.submitProjectApplication.emit(this.project);
  }

  resetFormFields(): void {
    this.project = {} as InputProject;
    this.projectApplicationSubmitForm.resetForm();
  }
}
