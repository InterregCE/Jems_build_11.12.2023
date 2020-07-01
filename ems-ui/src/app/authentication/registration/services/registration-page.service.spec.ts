import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {InputUserRegistration} from '@cat/api';
import {RegistrationPageService} from './registration-page.service';
import {HttpTestingController} from '@angular/common/http/testing';
import {AuthenticationModule} from '../../authentication.module';
import {TestModule} from '../../../common/test-module';

describe('RegistrationPageService', () => {
  let httpTestingController: HttpTestingController;

  beforeEach(() => TestBed.configureTestingModule({
    imports: [AuthenticationModule, TestModule],
  }));

  it('should be created', () => {
    const service: RegistrationPageService = TestBed.get(RegistrationPageService);
    expect(service).toBeTruthy();
  });

  it('should register an applicant', fakeAsync(() => {
    httpTestingController = TestBed.get(HttpTestingController);
    const service: RegistrationPageService = TestBed.get(RegistrationPageService);
    let success = false;
    const applicant = {
      name: 'test',
      surname: 'test',
      email: 'test@test.com',
      password: 'test'
    } as InputUserRegistration;
    service.registerApplicant(applicant);
    spyOn((service as any).userSaveSuccess$, 'next').and.callThrough();
    httpTestingController.expectOne({
      method: 'POST',
      url: `//api/registration`
    }).flush(applicant);
    httpTestingController.verify();
    tick();
    service.saveSuccess().subscribe(result => {
      success = result;
    });
    tick();
    expect((service as any).userSaveSuccess$.next).toHaveBeenCalledTimes(1);
    expect((service as any).userSaveSuccess$.next).toHaveBeenCalledWith(true);
  }));
});