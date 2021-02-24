import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TableConfiguration} from './model/table.configuration';
import {DatePipe} from '@angular/common';
import {ColumnConfiguration} from './model/column.configuration';
import {ColumnType} from './model/column-type.enum';
import {Observable} from 'rxjs';
import {Tools} from '../../utils/tools';
import {MatSort} from '@angular/material/sort';
import {Tables} from '../../utils/tables';
import {MoneyPipe} from '../../pipe/money.pipe';
import {LanguageService} from '../../services/language.service';
import {InputTranslation} from '@cat/api';
import {tap} from 'rxjs/operators';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit {
  ColumnType = ColumnType;

  @Input()
  configuration: TableConfiguration;
  @Input()
  rows: Observable<any[]> | any[];
  @Input()
  totalElements: number;
  @Input()
  pageIndex: number;

  @Output()
  sortRows = new EventEmitter<Partial<MatSort>>();
  @Output()
  newPageSize: EventEmitter<number> = new EventEmitter<number>();
  @Output()
  newPageIndex: EventEmitter<number> = new EventEmitter<number>();

  columnsToDisplay: string[] = [];
  currentPageSize = Tables.DEFAULT_INITIAL_PAGE_SIZE;
  currentSystemLanguage: string; // = new ReplaySubject<string>(1);

  constructor(private datePipe: DatePipe,
              private moneyPipe: MoneyPipe,
              private languageService: LanguageService) {
  }

  ngOnInit(): void {
    this.columnsToDisplay = this.configuration.columns.map(col => col.displayedColumn);

    this.languageService.systemLanguage$
      .pipe(
        tap(lang => {
          this.currentSystemLanguage = lang;
        })
      ).subscribe();
  }

  /**
   * formats element value with certain date format, translation key or its pure value.
   *
   * @param column configuration
   * @param element value
   */
  formatColumnValue(column: ColumnConfiguration, element: any): any {
    if (!column.elementProperty) {
      return element;
    }
    if (column.i18nFixedKey) {
      return column.i18nFixedKey;
    }
    if (column.columnType === ColumnType.InputTranslation) {
      const elementInSystemLang = element[column.elementProperty]
        .find((it: InputTranslation) => it.language === this.currentSystemLanguage);
      return !!elementInSystemLang ? elementInSystemLang.translation : '';
    }
    const elementValue = Tools.getChainedProperty(element, column.elementProperty, '');
    if (column.elementTranslationKey) {
      return `${column.elementTranslationKey}.${elementValue}`;
    }
    if (column.alternativeValueCondition && column.alternativeValueCondition(elementValue)) {
      return column.alternativeValue;
    }
    if (column.columnType === ColumnType.DateColumn) {
      return this.datePipe.transform(elementValue, Tables.DEFAULT_DATE_FORMAT);
    }
    if (column.columnType === ColumnType.Decimal) {
      return this.moneyPipe.transform(elementValue);
    }
    return elementValue;
  }

  getI18nArgs(column: ColumnConfiguration, element: any): any {
    if (column.i18nArgs) {
      return column.i18nArgs(element);
    }
    return null;
  }

  /**
   * formats tooltip value with certain translation key or its pure value.
   *
   * @param column configuration
   * @param element value
   */
  formatColumnTooltip(column: ColumnConfiguration, element: any): any {
    if (!column.tooltip?.tooltipContent) {
      return element;
    }
    const elementTitle = Tools.getChainedProperty(element, column.tooltip.tooltipContent, '');
    if (column.tooltip.tooltipTranslationKey) {
      return `${column.tooltip.tooltipTranslationKey}.${elementTitle}`;
    }
    return elementTitle;
  }
}
