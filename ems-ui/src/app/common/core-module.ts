import {NgModule} from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';
import {TopBarService} from '@common/components/top-bar/top-bar.service';
import {FormFieldErrorsComponent} from '@common/components/forms/form-field-errors/form-field-errors.component';
import {DatePipe, KeyValuePipe} from '@angular/common';
import {ConfirmDialogComponent} from '@common/components/modals/confirm-dialog/confirm-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {MatSortHeader} from '@angular/material/sort';
import {MatSidenavModule} from '@angular/material/sidenav';
import {RouterModule} from '@angular/router';

import {HelpMenuComponent} from '@common/components/top-bar/help-menu/help-menu.component';
import {AlertComponent} from '@common/components/forms/form-validation/alert.component';
import {ExpandableTextareaComponent} from '@common/components/expandable-textarea/expandable-textarea.component';
import {BreadcrumbComponent} from '@common/components/breadcrumb/breadcrumb.component';
import {TopBarComponent} from './components/top-bar/top-bar.component';
import {MenuComponent} from './components/menu/menu.component';
import {SharedModule} from './shared-module';
import {TabService} from './services/tab.service';
import {LanguageService} from './services/language.service';

const declarations = [
  TopBarComponent,
  MenuComponent,
  FormFieldErrorsComponent,
  ConfirmDialogComponent,
  HelpMenuComponent,
  AlertComponent,
  ExpandableTextareaComponent,
  BreadcrumbComponent,
];

@NgModule({
  declarations: [
    declarations
  ],
  imports: [
    RouterModule,
    SharedModule,
    MatTabsModule,
    MatDialogModule,
    MatMenuModule,
    MatIconModule,
    MatSidenavModule,
    MatSelectModule,
  ],
  providers: [
    DatePipe,
    TopBarService,
    LanguageService,
    KeyValuePipe,
    MatSortHeader,
    TabService,
  ],
  exports: [
    declarations
  ]
})
export class CoreModule {
}
