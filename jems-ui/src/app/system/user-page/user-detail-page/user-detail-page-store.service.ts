import {Injectable} from '@angular/core';
import {combineLatest, merge, Observable, of, ReplaySubject, Subject} from 'rxjs';
import {
  OutputCurrentUser,
  PasswordDTO,
  UserChangeDTO,
  UserDTO,
  UserRoleSummaryDTO,
  UserService, UserSettingsChangeDTO,
  UserSettingsDTO
} from '@cat/api';
import {catchError, filter, map, shareReplay, switchMap, take, tap} from 'rxjs/operators';
import {Log} from '@common/utils/log';
import {HttpErrorResponse} from '@angular/common/http';
import {SecurityService} from '../../../security/security.service';
import {RoutingService} from '@common/services/routing.service';
import {RoleStore} from '../../services/role-store.service';
import {APIError} from '@common/models/APIError';

@Injectable()
export class UserDetailPageStore {
  public static USER_DETAIL_PATH = '/app/system/user/detail/';
  public static PROFILE_PATH = '/app/profile';
  public static CREATE_USER_PATH = '/app/system/user/detail/create';

  // TODO: remove when switching to new edit mode
  userSaveError$ = new Subject<APIError | null>();
  userSaveSuccess$ = new Subject<boolean>();
  passwordSaveError$ = new Subject<APIError | null>();
  passwordSaveSuccess$ = new Subject<boolean>();
  userSettingsSaveError$ = new Subject<APIError | null>();
  userSettingsSaveSuccess$ = new Subject<boolean>();

  user$: Observable<UserDTO>;
  currentUser$: Observable<OutputCurrentUser | null>;
  roles$: Observable<UserRoleSummaryDTO[]>;
  saveUser$ = new Subject<UserChangeDTO>();

  private userName$ = new ReplaySubject<string>(1);

  private savedUser$ = this.saveUser$
    .pipe(
      switchMap(userUpdate => this.userService.updateUser(userUpdate)),
      tap(saved => Log.info('Updated user:', this, saved)),
      tap(user => this.userName$.next(`${user.name} ${user.surname}`)),
      tap(() => this.userSaveSuccess$.next(true)),
      tap(() => this.userSaveError$.next(null)),
      catchError((error: HttpErrorResponse) => {
        this.userSaveError$.next(error.error);
        throw error;
      }),
      shareReplay(1),
    );

  constructor(private userService: UserService,
              private router: RoutingService,
              private securityService: SecurityService,
              private roleStore: RoleStore) {
    this.user$ = this.user();
    this.roles$ = this.roles();
    this.currentUser$ = this.securityService.currentUser;
  }

  createUser(user: UserChangeDTO): Observable<UserDTO> {
    return this.userService.createUser(user)
      .pipe(
        take(1),
        tap(() => this.userSaveError$.next(null)),
        tap(saved => Log.info('Created user:', this, saved)),
        catchError((error: HttpErrorResponse) => {
          this.userSaveError$.next(error.error);
          throw error;
        })
      );
  }

  changePassword(userId: number, password: PasswordDTO): Observable<void> {
    return (userId ? this.userService.resetPassword(userId, password.password) : this.userService.changeMyPassword(password))
      .pipe(
        tap(() => this.passwordSaveSuccess$.next(true)),
        tap(() => this.passwordSaveError$.next(null)),
        tap(() => Log.info('User password changed successfully.', this)),
        catchError((error: HttpErrorResponse) => {
          this.passwordSaveError$.next(error.error);
          throw error;
        })
      );
  }

  updateSettings(settings: UserSettingsChangeDTO): Observable<UserSettingsDTO> {
    return (this.userService.updateUserSetting(settings))
      .pipe(
        tap(() => this.userSettingsSaveSuccess$.next(true)),
        tap(() => this.userSettingsSaveError$.next(null)),
        tap(() => Log.info('User settings changed successfully.', this)),
        catchError((error: HttpErrorResponse) => {
          this.userSettingsSaveError$.next(error.error);
          throw error;
        })
      );
  }

  getUserName(): Observable<string> {
    return this.userName$.asObservable();
  }

  private user(): Observable<UserDTO> {
    const initialUser$ = this.router.routeParameterChanges(UserDetailPageStore.USER_DETAIL_PATH, 'userId')
      .pipe(
        filter(userId => userId !== null),
        switchMap(userId => userId ? this.userService.getById(Number(userId)) : of({} as UserDTO)),
        tap(user => this.updateUserName(user)),
        tap(user => Log.info('Fetched the user:', this, user))
      );

    const ownProfileUser$ = this.router.routeChanges(UserDetailPageStore.PROFILE_PATH)
      .pipe(
        filter(isProfilePath => isProfilePath),
        switchMap(() => this.securityService.currentUserDetails as Observable<UserDTO>),
        tap(user => this.updateUserName(user)),
      );

    const createUser$ = this.router.routeChanges(UserDetailPageStore.CREATE_USER_PATH)
      .pipe(
        filter(isCreateUser => isCreateUser),
        map(() => ({} as UserDTO)),
      );

    return merge(initialUser$, ownProfileUser$, this.savedUser$, createUser$);
  }

  private roles(): Observable<UserRoleSummaryDTO[]> {
    return combineLatest([this.user$, this.roleStore.roles$])
      .pipe(
        map(([user, roles]) => roles?.length ? roles : [user?.userRole])
      );
  }

  private updateUserName(user: UserDTO): void {
    if (user?.name) {
      this.userName$.next(`${user.name} ${user.surname}`);
    }
  }
}
