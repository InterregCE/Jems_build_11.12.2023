import {Injectable} from '@angular/core';
import {CurrencyMaskConfig, CurrencyMaskInputMode} from 'ngx-currency';
import {Big, RoundingMode} from 'big.js';

@Injectable({providedIn: 'root'})
export class NumberService {

  /**
   * Converts the given number to specific locale (eg. de-DE).
   */
  static toLocale(value: number, locale: string = 'de-DE'): string | number {
    if (value === undefined) {
      return '';
    }
    if (isNaN(value)) {
      return NaN;
    }
    return new Intl.NumberFormat(
      locale,
      {minimumFractionDigits: 2, maximumFractionDigits: 2}
    ).format(value);
  }

  /**
   * Removes the thousand separator, replaces comma as decimal separator and converts the value to decimal
   */
  static toDecimal(value: string): number {
    if (!value) {
      return 0;
    }
    return parseFloat(
      value
        .split('.').join('')
        .replace(',', '.')
    );
  }

  /**
   * Sums the numbers from the given array using Big.js in order to avoid rounding errors.
   */
  static sum(arr: number[]): number {
    if (!arr || !arr.length) {
      return 0;
    }
    let sum = new Big(0);
    arr.forEach(nr => sum = sum.plus(nr));
    return Number(sum);
  }

  /**
   * Minus the numbers from the given array using Big.js in order to avoid rounding errors.
   */
  static minus(minuend: number, subtrahend: number): number {
    return Number(new Big(minuend).minus(subtrahend));
  }

  /**
   * Multiplies the numbers from the given array using Big.js in order to avoid rounding errors.
   */
  static product(arr: number[]): number {
    if (!arr || !arr.length) {
      return 0;
    }
    let prod = new Big(1);
    arr.forEach(nr => prod = prod.mul(nr));
    return Number(prod);
  }

  /**
   * Divides the numbers using Big.js in order to avoid rounding errors.
   */
  static divide(dividend: number | null, divisor: number | null): number {
    if (!dividend || !divisor) {
      return 0;
    }
    return Number(new Big(dividend).div(divisor));
  }

  /**
   * Truncates (rounds down) the given number to a fixed number of two decimals.
   */
  static truncateNumber(toFloor: number, fractionLength: number = 2): number {
    return Number(Big(toFloor).round(fractionLength, RoundingMode.RoundDown));
  }

  /**
   * Customizable later.
   */
  getLocale(): string {
    return 'de-DE';
  }

  /**
   * Customizable later.
   */

  getPrecision(): number {
    return 2;
  }

  /**
   * Customizable later.
   */

  getDecimalSeparator(): string {
    return ',';
  }

  /**
   * Customizable later.
   */
  getThousandsSeparator(): string {
    return '.';
  }

  decimalInput(custom?: Partial<CurrencyMaskConfig>): CurrencyMaskConfig {
    return Object.assign(
      {
        align: 'left',
        allowNegative: false, // maybe the better default is true? check usages and adapt..
        allowZero: true,
        decimal: this.getDecimalSeparator(),
        precision: this.getPrecision(),
        prefix: '',
        suffix: '',
        thousands: this.getThousandsSeparator(),
        nullable: true,
        inputMode: CurrencyMaskInputMode.NATURAL
      },
      custom);
  }

  integerInput(custom?: Partial<CurrencyMaskConfig>): CurrencyMaskConfig {
    return Object.assign(
      {
        align: 'left',
        allowNegative: false,
        allowZero: true,
        decimal: null as any,
        precision: 0,
        prefix: '',
        suffix: '',
        thousands: this.getThousandsSeparator(),
        nullable: true,
        inputMode: CurrencyMaskInputMode.NATURAL
      },
      custom);
  }
}