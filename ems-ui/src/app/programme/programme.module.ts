import {NgModule} from '@angular/core';
import {ProgrammeRoutingModule} from './programme-routing.module';
import {SharedModule} from '../common/shared-module';
import {CoreModule} from '../common/core-module';
import {ProgrammePageComponent} from './programme-page/containers/programme-page/programme-page.component';
import {ProgrammeDataComponent} from './programme-page/components/programme-data/programme-data.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {ProgrammePrioritiesComponent} from './programme-page/containers/programme-priorities/programme-priorities.component';
import {ProgrammePriorityItemsComponent} from './programme-page/components/programme-priority-items/programme-priority-items.component';
import {ProgrammePrioritySubmissionComponent} from './programme-page/components/programme-priority-submission/programme-priority-submission.component';
import {ProgrammePolicyCheckboxComponent} from './programme-page/components/programme-priority-submission/programme-policy-checkbox/programme-policy-checkbox.component';
import {ProgrammePriorityComponent} from './programme-page/containers/programme-priority/programme-priority.component';
import {ProgrammeAreaComponent} from './programme-page/containers/programme-area/programme-area.component';
import {ProgrammeNutsInfoComponent} from './programme-page/components/programme-nuts-info/programme-nuts-info.component';
import {MatTreeModule} from '@angular/material/tree';
import {MatIconModule} from '@angular/material/icon';
import {ProgrammeRegionsTreeComponent} from './programme-page/components/programme-regions-tree/programme-regions-tree.component';
import {ProgrammeSelectedRegionsComponent} from './programme-page/components/programme-selected-regions/programme-selected-regions.component';
import {ProgrammeRegionsComponent} from './programme-page/components/programme-regions/programme-regions.component';
import {ProgrammeIndicatorsOverviewPageComponent} from './programme-page/containers/programme-indicators-overview-page/programme-indicators-overview-page.component';
import {ProgrammeOutputIndicatorsListComponent} from './programme-page/components/programme-output-indicators-list/programme-output-indicators-list.component';
import {ProgrammeResultIndicatorsListComponent} from './programme-page/components/programme-result-indicators-list/programme-result-indicators-list.component';
import {IndicatorsStore} from './programme-page/services/indicators-store.service';
import { ProgrammeOutputIndicatorDetailComponent } from './programme-page/components/programme-output-indicator-detail/programme-output-indicator-detail.component';
import { ProgrammeOutputIndicatorSubmissionPageComponent } from './programme-page/containers/programme-output-indicator-submission-page/programme-output-indicator-submission-page.component';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import { ProgrammeResultIndicatorSubmissionPageComponent } from './programme-page/containers/programme-result-indicator-submission-page/programme-result-indicator-submission-page.component';
import { ProgrammeResultIndicatorDetailComponent } from './programme-page/components/programme-result-indicator-detail/programme-result-indicator-detail.component';

@NgModule({
  declarations: [
    ProgrammePageComponent,
    ProgrammeDataComponent,
    ProgrammePrioritiesComponent,
    ProgrammePriorityItemsComponent,
    ProgrammePrioritySubmissionComponent,
    ProgrammePolicyCheckboxComponent,
    ProgrammePolicyCheckboxComponent,
    ProgrammePriorityComponent,
    ProgrammeAreaComponent,
    ProgrammeNutsInfoComponent,
    ProgrammeRegionsComponent,
    ProgrammeRegionsTreeComponent,
    ProgrammeSelectedRegionsComponent,
    ProgrammeNutsInfoComponent,
    ProgrammeIndicatorsOverviewPageComponent,
    ProgrammeOutputIndicatorsListComponent,
    ProgrammeResultIndicatorsListComponent,
    ProgrammeOutputIndicatorDetailComponent,
    ProgrammeOutputIndicatorSubmissionPageComponent,
    ProgrammeResultIndicatorSubmissionPageComponent,
    ProgrammeResultIndicatorDetailComponent
  ],
  providers: [
    IndicatorsStore
  ],
  imports: [
    SharedModule,
    ProgrammeRoutingModule,
    CoreModule,
    MatDatepickerModule,
    MatSelectModule,
    MatCheckboxModule,
    MatTreeModule,
    MatIconModule,
    MatCheckboxModule,
    MatAutocompleteModule
  ]
})
export class ProgrammeModule {
}
