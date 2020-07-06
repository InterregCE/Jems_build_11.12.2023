import {NgModule} from '@angular/core';
import {UserPageComponent} from './user-page/containers/user-page/user-page.component';
import {UserRoutingModule} from './user-routing.module';
import {UserListComponent} from './user-page/components/user-list/user-list.component';
import {SharedModule} from '../common/shared-module';
import {UserSubmissionComponent} from './user-page/components/user-submission/user-submission.component';
import {MatSelectModule} from '@angular/material/select';
import {CoreModule} from '../common/core-module';
import {UserEditComponent} from './user-page/components/user-detail/user-edit/user-edit.component';
import {UserDetailComponent} from './user-page/containers/user-detail/user-detail.component';
import {MatCardModule} from '@angular/material/card';
import {RolePageService} from './user-role/services/role-page/role-page.service';
import {UserRoleFormFieldComponent} from './user-page/components/user-detail/user-role-form-field/user-role-form-field.component';
import {UserPasswordComponent} from './user-page/components/user-detail/user-password/user-password.component';
import { PasswordFieldComponent } from './user-page/components/user-detail/user-password/password-field/password-field.component';

@NgModule({
  declarations: [
    UserPageComponent,
    UserListComponent,
    UserSubmissionComponent,
    UserDetailComponent,
    UserEditComponent,
    UserRoleFormFieldComponent,
    UserPasswordComponent,
    PasswordFieldComponent
  ],
  imports: [
    SharedModule,
    UserRoutingModule,
    MatSelectModule,
    MatCardModule,
    CoreModule,
  ],
  providers: [
    RolePageService
  ]
})
export class UserModule {
}
