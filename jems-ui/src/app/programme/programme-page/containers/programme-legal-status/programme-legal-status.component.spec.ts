import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';

import {ProgrammeLegalStatusComponent} from './programme-legal-status.component';
import {ProgrammeModule} from '../../../programme.module';
import {TestModule} from '../../../../common/test-module';
import {HttpTestingController} from '@angular/common/http/testing';
import {ProgrammeLegalStatusDTO} from '@cat/api';

describe('ProgrammeLegalStatusComponent', () => {
  let httpTestingController: HttpTestingController;

  let component: ProgrammeLegalStatusComponent;
  let fixture: ComponentFixture<ProgrammeLegalStatusComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ProgrammeModule, TestModule],
      declarations: [ProgrammeLegalStatusComponent]
    })
      .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgrammeLegalStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch initial legal statuses', fakeAsync(() => {
    let result: ProgrammeLegalStatusDTO[] = [];
    component.legalStatuses$.subscribe(res => result = res);

    httpTestingController.match({method: 'GET', url: `//api/programmeLegalStatus`})
      .forEach(req => req.flush([{id: 1}]));

    tick();
    expect(result.length).toBe(1);
    expect(result[0].id).toBe(1);
  }));

  it('should update legal statuses', fakeAsync(() => {
    let result: ProgrammeLegalStatusDTO[] = [];
    component.legalStatuses$.subscribe(res => result = res);
    component.saveStatuses$.next({toPersist: [], toDeleteIds: []});

    httpTestingController.match({method: 'GET', url: `//api/programmeLegalStatus`});
    httpTestingController.match({method: 'PUT', url: `//api/programmeLegalStatus`})
      .forEach(req => req.flush([{id: 1}]));

    tick();
    expect(result.length).toBe(1);
    expect(result[0].id).toBe(1);
  }));
});
