import {Pipe, PipeTransform} from '@angular/core';

@Pipe({  name: 'correctionTypeOrderPipe' })
export class CorrectionTypeOrderPipePipe implements PipeTransform {

  transform(input: any) {
    if (!input) {return [];}

    return input.sort(function(first: {key: string; value: string}, second: {key: string; value: string}) {
      const firstValues = first.value.split(' ')[0].split('.').map(d => Number(d));
      const secondValues = second.value.split(' ')[0].split('.').map(d => Number(d));
      if (firstValues[0] > secondValues[0] || (firstValues[0] == secondValues[0] && firstValues[1] > secondValues[1])) {
        return 1;
      } else {
        return -1;
      }
    });
  }
}