declare global {

  namespace Cypress {
    interface Chainable {
      addPartnerReport(partnerId: number);

      updatePartnerReportIdentification(partnerId: number, reportId: number, partnerReportIdentification);

      updatePartnerReportExpenditures(partnerId: number, reportId: number, partnerReportExpenditures);

      runPreSubmissionPartnerReportCheck(partnerId: number, reportId: number);

      submitPartnerReport(partnerId: number, reportId: number);

      startControlWork(partnerId: number, reportId: number);

      setExpenditureItemsAsParked(partnerId: number, reportId: number, partnerReportExpenditures);

      finalizeControl(partnerId: number, reportId: number);

      addPublicProcurement(partnerId: number, reportId: number, procurementDetails)

      addBeneficialOwnerToProcurement(partnerId: number, reportId: number, procurementId: number, beneficialOwnerDetails: any);

      addSubcontractorToProcurement(partnerId: number, reportId: number, procurementId: number, subcontractorDetails: any);

      addAttachmentToProcurement(fileName:string, filePath: string, partnerId: number, reportId: number, procurementId: number);

      getRegularLumpSums(applicationId: number)

      getFastTrackLumpSums(applicationId: number)

      getUnitCostsByPartnerAndReportIds(partnerId: number, reportId: number)

      getLumpSumsByPartnerAndReportIds(partnerId: number, reportId: number)

      startControlChecklist(partnerId: number, reportId: number, checklist);

      finishControlChecklist(partnerId, reportId, checklistId);
    }
  }
}

Cypress.Commands.add('addPartnerReport', (partnerId: number) => {
  cy.request({
    method: 'POST',
    url: `api/project/report/partner/create/${partnerId}`,
  }).then(response => {
    return response.body.id
  });
});

Cypress.Commands.add('updatePartnerReportIdentification', (partnerId: number, reportId: number, partnerReportIdentification) => {
  cy.request({
    method: 'PUT',
    url: `api/project/report/partner/identification/byPartnerId/${partnerId}/byReportId/${reportId}`,
    body: partnerReportIdentification
  });
});

Cypress.Commands.add('updatePartnerReportExpenditures', (partnerId: number, reportId: number, partnerReportExpenditures) => {
  assignUnitCostIds(partnerId, reportId, partnerReportExpenditures);
  assignLumpSumIds(partnerId, reportId, partnerReportExpenditures);

  cy.request({
    method: 'PUT',
    url: `api/project/report/partner/expenditure/byPartnerId/${partnerId}/byReportId/${reportId}`,
    body: partnerReportExpenditures
  }).then(response => response.body);
});

Cypress.Commands.add('runPreSubmissionPartnerReportCheck', (partnerId: number, reportId: number) => {
  cy.request({
    method: 'POST',
    url: `api/project/report/partner/preCheck/${partnerId}/${reportId}`,
  });
});

Cypress.Commands.add('submitPartnerReport', (partnerId: number, reportId: number) => {
  cy.request({
    method: 'POST',
    url: `api/project/report/partner/submit/${partnerId}/${reportId}`,
  });
});

Cypress.Commands.add('startControlWork', (partnerId: number, reportId: number) => {
  cy.request({
    method: 'POST',
    url: `api/project/report/partner/startControl/${partnerId}/${reportId}`,
  })
});

Cypress.Commands.add('setExpenditureItemsAsParked', (partnerId: number, reportId: number, parkedExpenditures) => {
  cy.request({
    method: 'PUT',
    url: `api/project/report/partner/control/expenditure/byPartnerId/${partnerId}/byReportId/${reportId}`,
    body: parkedExpenditures
  })
});

Cypress.Commands.add('finalizeControl', (partnerId: number, reportId: number) => {
  cy.request({
    method: 'POST',
    url: `api/project/report/partner/finalizeControl/${partnerId}/${reportId}`,
  })
});

Cypress.Commands.add('addPublicProcurement', (partnerId: number, reportId: number, procurementDetails) => {
  cy.request({
    method: 'POST',
    url: `api/project/report/partner/procurement/byPartnerId/${partnerId}/byReportId/${reportId}`,
    body: procurementDetails
  }).then(response => response.body);
});

Cypress.Commands.add('getRegularLumpSums', (applicationId: number) => {
  cy.request({
    method: 'GET',
    url: `api/project/byId/${applicationId}`
  }).then((response) => {
    return response.body.callSettings.lumpSums.filter(lumpSum => !lumpSum.fastTrack);
  })
});

Cypress.Commands.add('getFastTrackLumpSums', (applicationId: number) => {
  cy.request({
    method: 'GET',
    url: `api/project/byId/${applicationId}`
  }).then((response) => {
    return response.body.callSettings.lumpSums.filter(lumpSum => lumpSum.fastTrack);
  })
});

Cypress.Commands.add('getLumpSumsByPartnerAndReportIds', (partnerId: number, reportId: number) => {
  cy.request({
    method: 'GET',
    url: `api/project/report/partner/expenditure/byPartnerId/${partnerId}/byReportId/${reportId}/lumpSums`
  }).then(response => response.body)
});

Cypress.Commands.add('getUnitCostsByPartnerAndReportIds', (partnerId: number, reportId: number) => {
  cy.request({
    method: 'GET',
    url: `api/project/report/partner/expenditure/byPartnerId/${partnerId}/byReportId/${reportId}/unitCosts`
  }).then(response => response.body);
});

Cypress.Commands.add('addBeneficialOwnerToProcurement', (partnerId: number, reportId: number, procurementId: number, beneficialOwnerDetails: any) => {
  cy.request({
    method: 'PUT',
    url: `api/project/report/partner/procurement/beneficialOwner/byPartnerId/${partnerId}/byReportId/${reportId}/byProcurementId/${procurementId}`,
    body: [beneficialOwnerDetails]
  }).then(response => response.body);
});

Cypress.Commands.add('addAttachmentToProcurement', (fileName: string, filePath: string, partnerId: number, reportId: number, procurementId: number) => {
  const filePathWithFileName = filePath + fileName;
  const fileType = "application/text";

  cy.fixture(filePathWithFileName, "binary")
    .then((txtBin) => Cypress.Blob.binaryStringToBlob(txtBin))
    .then((blob) => {
      const formData = new FormData();
      formData.append("file", blob, fileName);
      formData.append("file_type", fileType);

      cy.request({
        method: 'POST',
        url: `api/project/report/partner/procurement/attachment/byPartnerId/${partnerId}/byReportId/${reportId}/byProcurementId/${procurementId}`,
        body: formData
      })
    })
})

Cypress.Commands.add('addSubcontractorToProcurement', (partnerId: number, reportId: number, procurementId: number, subcontractorDetails: any) => {
  cy.request({
    method: 'PUT',
    url: `api/project/report/partner/procurement/subcontractor/byPartnerId/${partnerId}/byReportId/${reportId}/byProcurementId/${procurementId}`,
    body: [subcontractorDetails]
  }).then(response => response.body);
});

Cypress.Commands.add('startControlChecklist', (partnerId, reportId, checklist) => {
  startControlChecklist(partnerId, reportId, checklist);
});

Cypress.Commands.add('finishControlChecklist', (partnerId, reportId, checklistId) => {
    finishControlChecklist(partnerId, reportId, checklistId)
});

function assignUnitCostIds(partnerId, reportId, partnerReportExpenditures) {
  partnerReportExpenditures.forEach(expenditure => {
    if (expenditure.cypressReferenceUnit === 'shouldHaveUnitCost') {
      cy.getUnitCostsByPartnerAndReportIds(partnerId, reportId)
        .then(unitCosts => unitCosts.forEach(unitCost => {
          if (unitCost.unitCostProgrammeId === expenditure.unitCostId) expenditure.unitCostId = unitCost.id;
        }));
    }
  })
}

function assignLumpSumIds(partnerId, reportId, partnerReportExpenditures) {
  partnerReportExpenditures.forEach(expenditure => {
    if (expenditure.cypressReferenceUnit === 'shouldHaveLumpSum') {
      cy.getLumpSumsByPartnerAndReportIds(partnerId, reportId)
        .then(lumpSums => lumpSums.forEach(lumpSum => {
          expenditure.lumpSumId = lumpSum.id
        }))
    }
  })
}

function startControlChecklist(partnerId, reportId, checklist) {
  return cy.request({
    method: 'POST',
    url: `api/controlChecklist/byPartnerId/${partnerId}/byReportId/${reportId}`,
    body: checklist
  }).then(response => {
    cy.wrap(response.body.id).as('checklistId');
  });
}

function finishControlChecklist(partnerId, reportId, checklistId) {
    cy.request({
      method: 'PUT',
      url: `api/controlChecklist/byPartnerId/${partnerId}/byReportId/${reportId}/status/${checklistId}/FINISHED`
    });
}

export {}
