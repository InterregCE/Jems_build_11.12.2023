import {FormArray, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {AppControl} from '@common/components/section/form/app-control';

export class ProgrammePriorityDetailPageConstants {

  public static CODE: AppControl = {
    name: 'code',
    errorMessages: {
      required: 'programme.priority.code.should.not.be.empty',
      maxlength: 'programme.priority.code.size.too.long',
    },
    validators: [Validators.maxLength(50), Validators.required]
  };

  public static TITLE: AppControl = {
    name: 'title',
    errorMessages: {
      required: 'programme.priority.title.should.not.be.empty',
      maxlength: 'programme.priority.title.size.too.long',
    },
    validators: [Validators.maxLength(300), Validators.required]
  };

  public static OBJECTIVE: AppControl = {
    name: 'objective',
    errorMessages: {
      required: 'programme.priority.objective.should.not.be.empty'
    },
    validators: [Validators.required]
  };

  public static SPECIFIC_OBJECTIVES: AppControl = {
    name: 'specificObjectives',
    errorMessages: {
      required: 'programme.priority.priorityPolicies.should.not.be.empty'
    }
  };

  public static POLICY_OBJECTIVE: AppControl = {
    name: 'programmeObjectivePolicy'
  };

  public static POLICY_CODE: AppControl = {
    name: 'code',
    errorMessages: {
      maxlength: 'programme.priority.specific.objective.code.size.too.long',
      required: 'programme.priority.specific.objective.code.should.not.be.empty'
    },
    validators: [Validators.maxLength(300)]
  };

  public static POLICY_SELECTED: AppControl = {
    name: 'selected'
  };

  public static selectedSpecificObjectiveCodeRequired(objective: FormGroup): ValidatorFn | null {
    const selected = objective.get(ProgrammePriorityDetailPageConstants.POLICY_SELECTED.name)?.value;
    const code = objective.get(ProgrammePriorityDetailPageConstants.POLICY_CODE.name)?.value;
    if (selected && !code) {
      return {required: true} as any;
    }
    return null;
  }

  public static mustHaveSpecificObjectiveSelected(objectives: FormArray): ValidatorFn | null {
    const oneSelected = objectives.controls.some(
      objective => !!objective.get(ProgrammePriorityDetailPageConstants.POLICY_SELECTED.name)?.value
    );
    if (oneSelected) {
      return null;
    }
    return {required: true} as any;
  }

}