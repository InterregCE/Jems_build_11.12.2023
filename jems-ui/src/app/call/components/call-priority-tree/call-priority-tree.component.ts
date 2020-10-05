import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {BaseComponent} from '@common/components/base-component';
import {CallPriorityCheckbox} from '../../containers/model/call-priority-checkbox';

@Component({
  selector: 'app-call-priority-tree',
  templateUrl: './call-priority-tree.component.html',
  styleUrls: ['./call-priority-tree.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CallPriorityTreeComponent extends BaseComponent {
  @Input()
  priorityCheckboxes: CallPriorityCheckbox[];
  @Input()
  disabled: boolean;
}