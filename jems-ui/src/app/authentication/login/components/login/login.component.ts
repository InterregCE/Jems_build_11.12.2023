import {ChangeDetectorRef, Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AbstractForm} from '@common/components/forms/abstract-form';
import {LoginRequest} from '@cat/api';
import {TranslateService} from '@ngx-translate/core';
import {ResourceStoreService} from '@common/services/resource-store.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent extends AbstractForm {

  @Output()
  submitLogin: EventEmitter<LoginRequest> = new EventEmitter<LoginRequest>();

  hide = true;
  loginForm = this.formBuilder.group({
    email: ['', Validators.required],
    password: ['', Validators.required]
  });

  registerLink = '/no-auth/register';

  largeLogo$ = this.resourceStore.largeLogo$;

  constructor(private readonly formBuilder: FormBuilder,
              protected changeDetectorRef: ChangeDetectorRef,
              protected translationService: TranslateService,
              public resourceStore: ResourceStoreService) {
    super(changeDetectorRef, translationService);
  }

  onSubmit(): void {
    this.submitted = true;
    this.submitLogin.emit({
      email: this.loginForm.controls.email.value,
      password: this.loginForm.controls.password.value
    });
  }

  getForm(): FormGroup | null {
    return this.loginForm;
  }
}
