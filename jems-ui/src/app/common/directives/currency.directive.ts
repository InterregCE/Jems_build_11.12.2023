import {Directive, Host, Input, OnInit} from '@angular/core';
import {CurrencyMaskConfig, CurrencyMaskDirective} from 'ngx-currency';
import {NumberService} from '../services/number.service';

/**
 * Extends the CurrencyMaskDirective with the NumberService settings.
 */
@Directive({
  // tslint:disable-next-line:directive-selector
  selector: '[currencyMask]',
})
export class CurrencyDirective implements OnInit {
  @Input()
  options?: Partial<CurrencyMaskConfig>;
  @Input()
  type: 'decimal' | 'integer';

  constructor(@Host() public currencyMaskDirective: CurrencyMaskDirective,
              private numberService: NumberService) {
  }

  ngOnInit(): void {
    this.currencyMaskDirective.options = this.type === 'decimal'
      ? this.numberService.decimalInput(this.options) : this.numberService.integerInput(this.options);
    this.currencyMaskDirective.ngDoCheck();
  }
}
