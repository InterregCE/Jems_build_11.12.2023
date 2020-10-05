export class Tools {

  /**
   * Gets a chained property or the default value if provided.
   */
  static getChainedProperty(object: any, property: string, defaultVal: any): any {
    const keys: string[] = property.split('.');
    const newObject: any = object[keys[0]];
    if (newObject && keys.length > 1) {
      return Tools.getChainedProperty(newObject, keys.slice(1).join('.'), defaultVal);
    }
    return newObject === undefined ? defaultVal : newObject;
  }

  static checkDigitsOnPaste(event: ClipboardEvent) {
    const clipboardData = event.clipboardData?.getData('text').toUpperCase() || '';
    if (clipboardData.includes('E') || clipboardData.includes('-') || clipboardData.includes('+')
      || clipboardData.includes('.') || clipboardData.includes(',')) {
      event.stopPropagation();
      event.preventDefault();
    }
  }

  static checkDigitsOnInput(event: KeyboardEvent) {
    const charCode = (event.which) ? event.which : event.keyCode;
    if (charCode === 101)
      return false;
    return (charCode >= 48 && charCode <= 57);
  }
}