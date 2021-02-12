import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
// import {DataSet, Timeline, TimelineOptions, TimelineTimeAxisScaleType} from 'vis';
import {
  Content,
  EMPTY_STRING,
  getEndDateFromPeriod,
  getGroups,
  getItems,
  getTranslations,
  periodLabelFunction,
  START_DATE
} from './project-timeplan.utils';
import {ProjectApplicationFormSidenavService} from '../../project-application/containers/project-application-form-page/services/project-application-form-sidenav.service';
import {InputTranslation, OutputProjectPeriod} from '@cat/api';
import {DataSet} from 'vis-data';
import {Timeline, TimelineOptions, TimelineTimeAxisScaleType} from 'vis-timeline';
import {ProjectTimeplanPageStore} from './project-timeplan-page-store.service';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {map, tap} from 'rxjs/operators';
import {combineLatest, Observable} from 'rxjs';

@UntilDestroy()
@Component({
  selector: 'app-project-timeplan-page',
  templateUrl: './project-timeplan-page.component.html',
  styleUrls: ['./project-timeplan-page.component.scss'],
  providers: [ProjectTimeplanPageStore],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectTimeplanPageComponent implements OnInit {
  timeline: Timeline;

  data$: Observable<{
    translations: { [language: string]: Content[]; },
    groups: DataSet<any>,
    items: DataSet<any>,
    periods: OutputProjectPeriod[]
  }>;

  options: TimelineOptions = {
    // zoomable: false,
    // moveable: false,
    showCurrentTime: false,
    showMajorLabels: false,
    orientation: 'top',
    timeAxis: {scale: 'month' as TimelineTimeAxisScaleType, step: 1},
    format: {minorLabels: periodLabelFunction},
    margin: {
      axis: 10,
      item: {vertical: 10, horizontal: 0}
    },
    min: START_DATE,
    // TODO compute end date based on last available period for this project
    // max: END_DATE,
  };

  constructor(private projectApplicationFormSidenavService: ProjectApplicationFormSidenavService,
              public pageStore: ProjectTimeplanPageStore) {
  }

  /**
   * TODO: you need to fetch:
   * - getFullWorkPackagesByProjectId(projectId)
   * - getProjectResults(projectId)
   * - get last period or all periods (those should be in store already, we will then use them for setting boundaries)
   * - get input languages available (instead of 'random lang' button we will use language toggle then)
   *
   * those first 2 you will use as ProjectTimePlan() instead of TEST_DATA in following lines:
   */
  ngOnInit(): void {
    this.data$ = combineLatest([
      this.pageStore.workPackages$,
      this.pageStore.projectResults$,
      this.pageStore.periods$,
    ])
      .pipe(
        map(([workPackages, results, periods]) => ({
          translations: getTranslations({workPackages, results}),
          groups: getGroups({workPackages, results}),
          items: getItems({workPackages, results}),
          periods
        })),
        tap(data => {
          const lastPeriod = data.periods[data.periods.length - 1];
          this.options.max = getEndDateFromPeriod(lastPeriod?.number)?.calendar();
          const doc = document.getElementById('visualization');
          if (doc) {
            this.timeline = new Timeline(doc, data.items, this.options);
            this.timeline.setGroups(data.groups);
          }
        })
      );

    combineLatest([this.data$, this.pageStore.language$])
      .pipe(
        tap(([data, language]) => this.updateLanguageSelection(data, language)),
        untilDestroyed(this)
      ).subscribe();
  }

  click(event: Event): void {
    const prop = this.timeline.getEventProperties(event);
    console.log('click gr ' + prop.group + ', id ' + prop.item);
  }

  /**
   * Call this if different language has been selected.
   */
  updateLanguageSelection(data: any, language: InputTranslation.LanguageEnum | string): void {
    const translatedIds = data.translations[language]?.map((group: any) => group.id);
    const allIds: number[] = data.groups.getIds().map((groupId: any) => Number(groupId));
    const toEmptyIds = allIds.filter(element => !translatedIds.includes(element));

    // if some translations are missing for particular language, we need to reset them to EMPTY_STRING
    data.groups.update(data.translations[language]
      .concat(toEmptyIds.map(groupId => ({id: groupId, content: EMPTY_STRING} as Content)))
    );
  }

}
