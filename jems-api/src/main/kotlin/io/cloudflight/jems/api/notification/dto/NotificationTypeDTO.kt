package io.cloudflight.jems.api.notification.dto

enum class NotificationTypeDTO {
    // Project
    ProjectSubmittedStep1,
    ProjectSubmitted,
    ProjectApprovedStep1,
    ProjectApprovedWithConditionsStep1,
    ProjectIneligibleStep1,
    ProjectNotApprovedStep1,
    ProjectApproved,
    ProjectApprovedWithConditions,
    ProjectIneligible,
    ProjectNotApproved,
    ProjectReturnedToApplicant,
    ProjectResubmitted,
    ProjectReturnedForConditions,
    ProjectConditionsSubmitted,
    ProjectContracted,
    ProjectInModification,
    ProjectModificationSubmitted,
    ProjectModificationApproved,
    ProjectModificationRejected,
    SharedFolderFileUpload,
    SharedFolderFileDelete,

    // Partner Report
    PartnerReportSubmitted,
    PartnerReportReOpen,
    PartnerReportControlOngoing,
    PartnerReportCertified,
    PartnerReportReOpenCertified,
    ControlCommunicationFileUpload,
    ControlCommunicationFileDelete,


    // Project Report
    ProjectReportSubmitted,
    ProjectReportVerificationOngoing,
    ProjectReportVerificationDoneNotificationSent,
    ProjectReportVerificationFinalized,
    ProjectReportVerificationFileUpload,
    ProjectReportVerificationFileDelete;
}
