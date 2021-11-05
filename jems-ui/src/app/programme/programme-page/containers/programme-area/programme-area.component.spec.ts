import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';

import {ProgrammeAreaComponent} from './programme-area.component';
import {HttpTestingController} from '@angular/common/http/testing';
import {ProgrammeModule} from '../../../programme.module';
import {TestModule} from '../../../../common/test-module';

describe('ProgrammeAreaComponent', () => {
  let httpTestingController: HttpTestingController;
  let component: ProgrammeAreaComponent;
  let fixture: ComponentFixture<ProgrammeAreaComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ProgrammeModule,
        TestModule
      ],
      declarations: [ProgrammeAreaComponent]
    })
      .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgrammeAreaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Nuts regions', () => {
    const nuts = [{
      code: 'RO',
      title: 'Romania',
      areas: [{
        code: 'TR',
        title: 'Transilvania',
        areas: [
          {code: 'CJ', title: 'cluj'},
          {code: 'BT', title: 'bistrita'}
        ]
      }]
    }];

    beforeEach(() => {
      component.metaData$.subscribe();
      httpTestingController.match({method: 'GET', url: '//api/nuts/metadata'}).forEach(req => req.flush({}));
      httpTestingController.expectOne({method: 'GET', url: '//api/programmedata'})
        .flush({programmeNuts: nuts});
      httpTestingController.match({method: 'GET', url: '//api/nuts'}).forEach(req => req.flush(nuts));
    });

    it('should download after metadata', fakeAsync(() => {
      tick();

      expect(component.regionTreeDataSource.data[0].title).toEqual('Romania');
      expect(component.regionTreeDataSource.data[0].children[0].children[0].title).toEqual('cluj');
    }));

    it('should select as tree', fakeAsync(() => {
      tick();

      // checking a child updates parents state to intermediate
      const clujRegion = component.regionTreeDataSource.data[0].children[0].children[0];
      clujRegion.checked = true;
      clujRegion.updateChecked();
      expect(component.regionTreeDataSource.data[0].someChecked).toBeTruthy();
      expect(component.regionTreeDataSource.data[0].children[0].someChecked).toBeTruthy();

      // checking all children updates parents state to checked
      const bistritaRegion = component.regionTreeDataSource.data[0].children[0].children[1];
      bistritaRegion.checked = true;
      bistritaRegion.updateChecked();
      expect(component.regionTreeDataSource.data[0].checked).toBeTruthy();
      expect(component.regionTreeDataSource.data[0].children[0].checked).toBeTruthy();
    }));

    it('should persist selected regions', fakeAsync(() => {
      tick();

      component.saveSelectedRegions$.next();
      httpTestingController.expectOne({method: 'PUT', url: '//api/programmedata/nuts'});
    }));

    it('selection should contain top parent and leafs', fakeAsync(() => {
      tick();

      const clujRegion = component.regionTreeDataSource.data[0].children[0].children[0];
      clujRegion.checked = true;
      clujRegion.updateChecked();
      const bistritaRegion = component.regionTreeDataSource.data[0].children[0].children[1];
      bistritaRegion.checked = false;
      bistritaRegion.updateChecked();

      let selectedRegions: any;
      component.selectedRegions$.subscribe(regions => selectedRegions = regions);
      component.selectionChanged$.next(component.regionTreeDataSource.data);
      tick();

      expect(selectedRegions.get('Romania')[0].title).toEqual('cluj');
    }));
  });
});
