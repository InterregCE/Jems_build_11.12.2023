package io.cloudflight.jems.api.audit.dto

enum class AuditAction {
    // package CALL
    CALL_ADDED,
    CALL_CONFIGURATION_CHANGED,
    CALL_PUBLISHED,

    // package CURRENCY
    CURRENCY_IMPORT,

    // package NUTS
    NUTS_DATASET_DOWNLOAD,

    // package PROGRAMME
    PROGRAMME_PRIORITY_ADDED,
    PROGRAMME_PRIORITY_UPDATED,
    PROGRAMME_PRIORITY_DELETED,
    PROGRAMME_BASIC_DATA_EDITED,
    PROGRAMME_NUTS_AREA_CHANGED,
    PROGRAMME_FUNDS_CHANGED,
    PROGRAMME_UI_LANGUAGES_CHANGED,
    PROGRAMME_INPUT_LANGUAGES_CHANGED,
    PROGRAMME_STRATEGIES_CHANGED,
    PROGRAMME_TRANSLATION_FILE_UPLOADED,
    PROGRAMME_STATE_AID_CHANGED,
    PROGRAMME_TYPOLOGY_ERRORS,

    // PROGRAMME - Indicators
    PROGRAMME_INDICATOR_ADDED,
    PROGRAMME_INDICATOR_EDITED,
    PROGRAMME_INDICATOR_DELETED,

    // PROGRAMME - Cost Options:
    PROGRAMME_LUMP_SUM_ADDED,
    PROGRAMME_LUMP_SUM_CHANGED,
    PROGRAMME_LUMP_SUM_DELETED,
    PROGRAMME_UNIT_COST_ADDED,
    PROGRAMME_UNIT_COST_CHANGED,
    PROGRAMME_UNIT_COST_DELETED,

    // package PROJECT
    APPLICATION_STATUS_CHANGED,
    APPLICATION_SNAPSHOT_CREATED,
    @Deprecated("Kept for backwards compatibility.Use APPLICATION_SNAPSHOT_CREATED instead")
    APPLICATION_VERSION_SNAPSHOT_CREATED,
    APPLICATION_VERSION_RECORDED,
    CALL_ALREADY_ENDED,
    QUALITY_ASSESSMENT_CONCLUDED,
    ELIGIBILITY_ASSESSMENT_CONCLUDED,
    PROJECT_USER_ASSIGNMENT_PROGRAMME,
    PROJECT_USER_ASSIGNMENT_APPLICANTS,
    PROJECT_CONTRACT_MONITORING_CHANGED,
    PROJECT_CONTRACT_INFO_CHANGED,
    PROJECT_CONTRACT_PARTNER_INFO_CHANGE,
    PROJECT_BENEFICIAL_OWNER_ADDED,
    PROJECT_BENEFICIAL_OWNER_REMOVED,
    PROJECT_BENEFICIAL_OWNER_CHANGED,
    PROJECT_CONTRACTING_SECTION_LOCKED,
    PROJECT_CONTRACTING_SECTION_UNLOCKED,

    // messages related to FILEs
    PROJECT_FILE_DELETED,
    PROJECT_FILE_DOWNLOADED_SUCCESSFULLY,
    PROJECT_FILE_DOWNLOADED_FAILED,
    PROJECT_FILE_UPLOADED_SUCCESSFULLY,
    PROJECT_FILE_UPLOAD_FAILED,
    PROJECT_FILE_DESCRIPTION_CHANGED,

    // package SECURITY
    USER_LOGGED_IN,
    USER_LOGGED_OUT,
    USER_SESSION_EXPIRED,

    // package USER
    USER_ADDED,
    USER_REGISTERED,
    NEW_USER_CONFIRMED,
    USER_DATA_CHANGED,
    PASSWORD_CHANGED,
    ROLE_PRIVILEGES_CHANGED,

    @Deprecated("Kept for backwards compatibility.Use ROLE_PRIVILEGES_CHANGED instead")
    ROLE_PRIVILEGES_CREATED,

    //package LEGAL STATUS
    LEGAL_STATUS_EDITED,

    //plugin
    PLUGIN_CALLED,
    PLUGIN_EXECUTED,

    //email
    MAIL_SENT,
    MAIL_NOT_SENT,

    // report
    PARTNER_REPORT_ADDED,
    PARTNER_REPORT_SUBMITTED,
    PARTNER_REPORT_REOPENED,
    PARTNER_REPORT_CONTROL_REOPENED,
    PARTNER_REPORT_DELETED,
    PARTNER_REPORT_CONTROL_ONGOING,
    PARTNER_REPORT_CONTROL_FINALIZED,
    PARKED_EXPENDITURE_REINCLUDED,
    PARKED_EXPENDITURE_DELETED,

    PROJECT_REPORT_ADDED,
    PROJECT_REPORT_DELETED,
    PROJECT_REPORT_SUBMITTED,
    PROJECT_REPORT_VERIFICATION_ONGOING,
    PROJECT_REPORT_VERIFICATION_FINALIZED,

    // partner report control
    CONTROL_REPORT_CERTIFICATE_GENERATED,
    CONTROL_REPORT_EXPORT_GENERATED,

    // Checklist
    ASSESSMENT_CHECKLIST_STATUS_CHANGE,
    ASSESSMENT_CHECKLIST_DELETED,
    ASSESSMENT_CHECKLIST_CONSOLIDATION_CHANGE,
    ASSESSMENT_CHECKLIST_VISIBILITY_CHANGE,
    CHECKLIST_IS_CREATED,
    CHECKLIST_IS_DELETED,
    CHECKLIST_STATUS_CHANGE,
    CHECKLIST_DELETED,

    // Controllers
    CONTROLLER_INSTITUTION_CHANGED,
    INSTITUTION_PARTNER_ASSIGNMENT_CHANGED,
    INSTITUTION_PARTNER_ASSIGNMENT_DROPPED,

    // Payments
    FTLS_READY_FOR_PAYMENT_CHANGE,
    PAYMENT_INSTALLMENT_AUTHORISED,
    PAYMENT_INSTALLMENT_CONFIRMED,
    PAYMENT_INSTALLMENT_IS_DELETED,
    // Advance Payments
    ADVANCE_PAYMENT_IS_CREATED,
    ADVANCE_PAYMENT_IS_DELETED,
    ADVANCE_PAYMENT_DETAIL_AUTHORISED,
    ADVANCE_PAYMENT_DETAIL_CONFIRMED,
    ADVANCE_PAYMENT_SETTLEMENT_CREATED,
    ADVANCE_PAYMENT_SETTLEMENT_DELETED,
}
