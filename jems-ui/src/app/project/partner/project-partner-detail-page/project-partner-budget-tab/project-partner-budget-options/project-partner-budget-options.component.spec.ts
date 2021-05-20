import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ProjectPartnerBudgetOptionsComponent} from './project-partner-budget-options.component';
import {HttpTestingController} from '@angular/common/http/testing';
import {ProjectPartnerStore} from '../../../../project-application/containers/project-application-form-page/services/project-partner-store.service';
import {TestModule} from '../../../../../common/test-module';
import {ProjectModule} from '../../../../project.module';
import {ActivatedRoute} from '@angular/router';
import {BudgetOptions} from '../../../../model/budget/budget-options';
import {ProjectPartnerDetailPageStore} from '../../project-partner-detail-page.store';
import {ProjectPartnerBudgetTabService} from '../project-partner-budget-tab.service';
import {ProjectVersionStore} from '../../../../services/project-version-store.service';
import {of} from 'rxjs';

describe('ProjectApplicationFormPartnerBudgetOptionsComponent', () => {
  let component: ProjectPartnerBudgetOptionsComponent;
  let fixture: ComponentFixture<ProjectPartnerBudgetOptionsComponent>;
  let httpTestingController: HttpTestingController;
  let partnerStore: ProjectPartnerStore;
  let projectPartnerBudgetTabService: ProjectPartnerBudgetTabService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestModule,
        ProjectModule
      ],
      declarations: [ProjectPartnerBudgetOptionsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {params: {projectId: 1}}
          }
        },
        {
          provide: ProjectVersionStore,
          useValue: {
            currentRouteVersion$: of('1.0')
          }
        },
        {
          provide: ProjectPartnerDetailPageStore,
          useClass: ProjectPartnerDetailPageStore
        },
        {
          provide: ProjectPartnerBudgetTabService,
          useClass: ProjectPartnerBudgetTabService
        }
      ]
    })
      .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    partnerStore = TestBed.inject(ProjectPartnerStore);
    projectPartnerBudgetTabService = TestBed.inject(ProjectPartnerBudgetTabService);
  }));

  beforeEach(() => {
    partnerStore.partner$.next({id: 2});
    fixture = TestBed.createComponent(ProjectPartnerBudgetOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch and save budget options', fakeAsync(() => {
    httpTestingController.expectOne({
      method: 'GET',
      url: '//api/project/partner/2/budget/options?version=1.0'
    });

    component.doUpdateBudgetOptions(new BudgetOptions(8, 12, 10, 10, null));
    tick();

    httpTestingController.expectOne({
      method: 'PUT',
      url: '//api/project/partner/2/budget/options'
    });
  }));
});
