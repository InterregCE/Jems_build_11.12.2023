import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {
  InputTranslation,
  ProgrammeLegalStatusService,
  ProjectCallSettingsDTO,
  ProjectPartnerDetailDTO,
  ProjectPartnerDTO,
  ProjectPartnerSummaryDTO,
} from '@cat/api';
import {catchError, map, startWith, take, tap} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';
import {Forms} from '@common/utils/forms';
import {FormService} from '@common/components/section/form/form.service';
import {
  ProjectPartnerStore
} from '../../../containers/project-application-form-page/services/project-partner-store.service';
import {combineLatest, Observable, of} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {APPLICATION_FORM} from '@project/common/application-form-model';
import {Tools} from '@common/utils/tools';
import {RoutingService} from '@common/services/routing.service';
import {
  ProjectApplicationFormPartnerEditConstants
} from '@project/project-application/components/project-application-form/project-application-form-partner-edit/constants/project-application-form-partner-edit.constants';
import {ProjectPartnerRoleEnum} from '@project/model/ProjectPartnerRoleEnum';
import {ProjectPartner} from '@project/model/ProjectPartner';
import {TranslateService} from '@ngx-translate/core';
import CallTypeEnum = ProjectCallSettingsDTO.CallTypeEnum;

@Component({
  selector: 'jems-project-application-form-partner-edit',
  templateUrl: './project-application-form-partner-edit.component.html',
  styleUrls: ['./project-application-form-partner-edit.component.scss'],
  providers: [FormService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectApplicationFormPartnerEditComponent implements OnInit {
  RoleEnum = ProjectPartnerSummaryDTO.RoleEnum;
  VatRecoveryEnum = ProjectPartnerDTO.VatRecoveryEnum;
  ProjectApplicationFormPartnerEditConstants = ProjectApplicationFormPartnerEditConstants;
  tools = Tools;
  LANGUAGE = InputTranslation.LanguageEnum;
  APPLICATION_FORM = APPLICATION_FORM;

  partnerId = this.router.getParameter(this.activatedRoute, 'partnerId');
  legalStatuses$ = this.programmeLegalStatusService.getProgrammeLegalStatusList();
  filteredNace$: Observable<string[]>;
  isSpfCallType:  boolean;

  data$: Observable<{
    partner: ProjectPartnerDetailDTO;
    partners: ProjectPartner[];
    projectCallType: CallTypeEnum;
    isSpf: boolean;
  }>;

  partnerForm: FormGroup = this.formBuilder.group({
    fakeRole: [], // needed for the fake role field in view mode
    id: [],
    sortNumber: [],
    abbreviation: ['', [
      Validators.maxLength(15),
      Validators.required,
      Validators.pattern(/(?!^\s+$)^.*$/m)]
    ],
    role: ['', Validators.required],
    nameInOriginalLanguage: ['', Validators.maxLength(250)],
    nameInEnglish: [[], Validators.maxLength(250)],
    department: [],
    partnerType: [0],
    spfBeneficiaryType: [''],
    partnerSubType: [0],
    nace: [],
    otherIdentifierNumber: ['', Validators.maxLength(50)],
    otherIdentifierDescription: [],
    pic: ['', [Validators.minLength(9) , Validators.maxLength(9)]],
    legalStatusId: ['', Validators.required],
    vat: ['', Validators.maxLength(50)],
    vatRecovery: ['']
  });
  legalStatusErrors = {
    required: 'project.partner.legal.status.should.not.be.empty'
  };

  constructor(private formBuilder: FormBuilder,
              private dialog: MatDialog,
              public formService: FormService,
              private partnerStore: ProjectPartnerStore,
              private router: RoutingService,
              private activatedRoute: ActivatedRoute,
              private programmeLegalStatusService: ProgrammeLegalStatusService,
              private translate: TranslateService) {
    this.formService.init(this.partnerForm, this.partnerStore.isProjectEditable$);
    this.data$ = combineLatest([this.partnerStore.partners$, this.partnerStore.partner$, this.partnerStore.projectCallType$])
      .pipe(
        map(([partners, partner, callType]) => {
            this.isSpfCallType = callType === CallTypeEnum.SPF;
            return {
              partners,
              partner,
              projectCallType: callType,
              isSpf: this.isSpfCallType
            };
          }
        ),
        tap((data: any) => this.resetForm(data.projectCallType, data.partner))
      );
  }

  ngOnInit(): void {
    this.filteredNace$ = this.partnerForm.controls.nace.valueChanges
      .pipe(
        startWith(''),
        map(value => this.filter(value))
      );
  }

  get controls(): any {
    return this.partnerForm.controls;
  }

  onSubmit(controls: any, partners: ProjectPartner[]): void {
    this.confirmLeadPartnerChange(partners).subscribe(confirmed => {
      if (confirmed) {
        let partnerSubTypeValue = this.controls.partnerSubType.value;
        partnerSubTypeValue = partnerSubTypeValue ? partnerSubTypeValue : null;
        let partnerTypeValue = this.controls.partnerType.value;
        partnerTypeValue = partnerTypeValue ? partnerTypeValue : null;
        if (!controls.id?.value) {
          const partnerToCreate = {
            abbreviation: this.controls.abbreviation.value,
            role: this.controls.role.value,
            nameInOriginalLanguage: this.controls.nameInOriginalLanguage.value,
            nameInEnglish: this.controls.nameInEnglish.value[0].translation,
            department: this.controls.department.value,
            partnerType: partnerTypeValue,
            partnerSubType: partnerSubTypeValue,
            nace: this.controls.nace.value,
            otherIdentifierNumber: this.controls.otherIdentifierNumber.value,
            otherIdentifierDescription: this.controls.otherIdentifierDescription.value,
            pic: this.controls.pic.value,
            legalStatusId: this.controls.legalStatusId.value,
            vat: this.controls.vat.value,
            vatRecovery: this.controls.vatRecovery.value,
          } as any;

          if (!controls.partnerType.value) {
            partnerToCreate.partnerType = null;
          }

          this.partnerStore.createPartner(partnerToCreate as ProjectPartnerDTO)
            .pipe(
              take(1),
              tap(created => this.redirectToPartnerDetail(created)),
              catchError(error => this.formService.setError(error))
            ).subscribe();
        } else {
          const partnerToUpdate = {
            id: this.controls.id.value,
            abbreviation: this.controls.abbreviation.value,
            role: this.controls.role.value,
            nameInOriginalLanguage: this.controls.nameInOriginalLanguage.value,
            nameInEnglish: this.controls.nameInEnglish.value[0].translation,
            department: this.controls.department.value,
            partnerType: partnerTypeValue,
            partnerSubType: partnerSubTypeValue,
            nace: this.controls.nace.value,
            otherIdentifierNumber: this.controls.otherIdentifierNumber.value,
            otherIdentifierDescription: this.controls.otherIdentifierDescription.value,
            pic: this.controls.pic.value,
            legalStatusId: this.controls.legalStatusId.value,
            vat: this.controls.vat.value,
            vatRecovery: this.controls.vatRecovery.value,
          };

          if (!controls.partnerType.value) {
            partnerToUpdate.partnerType = null;
          }

          this.partnerStore.savePartner(partnerToUpdate as ProjectPartnerDTO)
            .pipe(
              take(1),
              tap(() => this.formService.setSuccess('project.partner.save.success')),
              catchError(error => this.formService.setError(error))
            ).subscribe();
        }
      } else {
        this.formService.setDirty(true);
      }
    });
  }

  private confirmLeadPartnerChange(partners: ProjectPartner[]): Observable<boolean>{
    const leadPartner = partners.find(it => it.role === ProjectPartnerRoleEnum.LEAD_PARTNER);
    if (
      leadPartner === undefined ||
      leadPartner === null ||
      !leadPartner.active ||
      leadPartner.id === this.controls.id.value ||
      this.controls.role.value === ProjectPartnerRoleEnum.PARTNER ||
      this.isSpfCallType
    ) {
      return of(true);
    } else {
      return Forms.confirmDialog(
        this.dialog,
        'project.partner.role.lead.already.existing.title',
        'project.partner.role.lead.already.existing',
        {
          old_name: leadPartner.abbreviation,
          new_name: this.controls.abbreviation.value
        }
      );
    }
  }

  discard(callType: CallTypeEnum, partner?: ProjectPartnerDetailDTO): void {
    if (!this.partnerId) {
      this.redirectToPartnerOverview();
    } else {
      this.resetForm(callType, partner);
    }
  }

  displayFn(nace: string): string {
    return nace ? nace.split('_').join('.') : '';
  }

  private resetForm(callType: CallTypeEnum, partner?: ProjectPartnerDetailDTO): void {
    if (!this.partnerId) {
      this.formService.setCreation(true);
    }
    this.controls.id.setValue(partner?.id);
    this.controls.abbreviation.setValue(partner?.abbreviation);
    this.controls.role.setValue(callType === CallTypeEnum.STANDARD ? partner?.role : ProjectPartnerDetailDTO.RoleEnum.LEADPARTNER);
    this.controls.nameInOriginalLanguage.setValue(partner?.nameInOriginalLanguage);
    this.controls.nameInEnglish.setValue([{
      language: this.LANGUAGE.EN,
      translation: partner?.nameInEnglish || ''
    }]);
    this.controls.department.setValue(partner?.department);
    this.controls.partnerType.setValue(partner?.partnerType || 0);
    this.controls.partnerSubType.setValue(partner?.partnerSubType || 0);
    this.controls.legalStatusId.setValue(partner?.legalStatusId);
    this.controls.nace.setValue(partner?.nace);
    this.controls.vat.setValue(partner?.vat);
    this.controls.vatRecovery.setValue(partner?.vatRecovery);
    this.controls.otherIdentifierNumber.setValue(partner?.otherIdentifierNumber);
    this.controls.otherIdentifierDescription.setValue(partner?.otherIdentifierDescription);
    this.controls.pic.setValue(partner?.pic);
    this.controls.sortNumber.setValue(partner?.sortNumber);
    this.setSpfBeneficiaryTypeValue(partner?.partnerType ? partner.partnerType: '');
  }

  selectionUnfocused(event: FocusEvent): void {
    if (this.selectOptionClicked(event)) {
      return;
    }
    const selected = this.findByNace(this.controls?.nace.value);
    if (!selected) {
      this.controls?.nace.patchValue(null);
    }
  }

  private filter(value: string): string[] {
    const filterValue = (value || '').toLowerCase();
    return this.ProjectApplicationFormPartnerEditConstants.naceEnums
      .filter(nace => nace.split('_').join('.').toLowerCase().includes(filterValue))
      .map(nace => nace);
  }

  private redirectToPartnerOverview(): void {
    this.router.navigate(['..'], {relativeTo: this.activatedRoute});
  }

  private redirectToPartnerDetail(partner: any): void {
    this.router.navigate(
      ['..', partner.id, 'identity'],
      {relativeTo: this.activatedRoute}
    );
  }

  private selectOptionClicked(event: FocusEvent): boolean {
    return !!event.relatedTarget && (event.relatedTarget as any).tagName === 'MAT-OPTION';
  }

  private findByNace(value: string): string | undefined {
    return this.ProjectApplicationFormPartnerEditConstants.naceEnums.find(nace => value === nace);
  }

  setSpfBeneficiaryTypeValue(partnerType: string) {
    const spfBeneficiaryTypeValue = this.getSpfBeneficiaryTypeTranslationKey(partnerType);
    const value = (spfBeneficiaryTypeValue !== '') ? this.translate.instant(spfBeneficiaryTypeValue) : '';
    this.controls.spfBeneficiaryType.setValue(value);
  }


  private getSpfBeneficiaryTypeTranslationKey(partnerType: string): string {
    const partnerTypeTranslationPrefix = 'project.application.form.relevance.target.group.';
    if (partnerType === ProjectPartnerDetailDTO.PartnerTypeEnum.Egtc) {
      return  partnerTypeTranslationPrefix.concat(ProjectPartnerDetailDTO.PartnerTypeEnum.Egtc);
    } else if (partnerType === ProjectPartnerDetailDTO.PartnerTypeEnum.CrossBorderLegalBody) {
      return  partnerTypeTranslationPrefix.concat(ProjectPartnerDetailDTO.PartnerTypeEnum.CrossBorderLegalBody);
    } else if (partnerType !== ProjectPartnerDetailDTO.PartnerTypeEnum.Egtc &&
      partnerType !== ProjectPartnerDetailDTO.PartnerTypeEnum.CrossBorderLegalBody && partnerType !== '') {
      return 'spf.beneficiary.type.input.option';
    }
    return '';
  }

}
