import {Routes} from '@angular/router';
import {UserDetailComponent} from '../user/user-page/containers/user-detail/user-detail.component';

export const routes: Routes = [
  {
    path: '',
    component: UserDetailComponent,
    data: {
      breadcrumb: 'user.breadcrumb.your.profile'
    },
  }
];