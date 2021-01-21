import {ProjectPartnerRoleEnum} from './ProjectPartnerRoleEnum';

export class ProjectPartner {
  id: number;
  abbreviation: string;
  role: ProjectPartnerRoleEnum | null;
  sortNumber: number;
  country: string;

  constructor(id: number, abbreviation: string, role: ProjectPartnerRoleEnum | null, sortNumber: number, country: string) {
    this.id = id;
    this.abbreviation = abbreviation;
    this.role = role;
    this.sortNumber = sortNumber;
    this.country = country;
  }

  toPartnerNumberString(): string {
    return this.role === ProjectPartnerRoleEnum.LEAD_PARTNER ? 'LP1' : 'PP'.concat(this.sortNumber.toString());
  }
}