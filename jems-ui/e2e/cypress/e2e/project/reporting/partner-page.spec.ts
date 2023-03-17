import user from "../../../fixtures/users.json";
import call from "../../../fixtures/api/call/1.step.call.json";
import partner from "../../../fixtures/api/application/partner/partner.json";
import {loginByRequest} from "../../../support/login.commands";

context('Partner reports tests', () => {
  it.only('TB-745 Partner page', function () {
    cy.fixture('project/reporting/TB-745.json').then(testData => {
      cy.fixture('api/application/application.json').then(application => {
        prepareTestData(testData, application);
        cy.createApprovedApplication(application, user.programmeUser.email).then(applicationId => {
          const partnerIdsToDisable = [2, 3];

          enableModification(applicationId);
          disableSelectedPartners(testData, applicationId, partnerIdsToDisable);
          verifyPartnerChangesBeforeApproving(testData, applicationId, partnerIdsToDisable);
          cy.approveModification(applicationId, testData.approvalInfo, user.programmeUser.email);

          cy.loginByRequest(user.admin.email).then(() => {
            cy.visit(`app/project/detail/${applicationId}`, {failOnStatusCode: false})
              .then(() => {
                partnerIdsToDisable.forEach(id => {
                  cy.get('mat-expansion-panel-header:contains("Partner details")')
                    .next('div')
                    .find(`li:contains("${testData.partners[id].abbreviation}")`)
                    .contains('mat-icon', 'person_off')
                    .should('exist');
                });

                verifyIconsInProjectPrivileges(testData, partnerIdsToDisable, true);
              });
          });

          enableModification(applicationId);
          loginByRequest(user.applicantUser.email).then(() => {
            cy.createFullPartner(applicationId, partner);
            submitProjectApp(applicationId);
          });

          cy.loginByRequest(user.admin.email).then(() => {
            cy.visit(`app/project/detail/${applicationId}`, {failOnStatusCode: false})
            verifyPartnerAvailability(false);
          });

          cy.approveModification(applicationId, testData.approvalInfo, user.programmeUser.email);

          cy.loginByRequest(user.admin.email).then(() => {
            cy.visit(`app/project/detail/${applicationId}`, {failOnStatusCode: false})
            verifyPartnerAvailability(true);
          });
        });
      });
    });
  });
});

function prepareTestData(testData, application) {
  cy.loginByRequest(user.programmeUser.email);

  cy.createCall(call)
    .then(callId => {
      application.details.projectCallId = callId;
      cy.publishCall(callId);
    });

  cy.loginByRequest(user.applicantUser.email);

  preparePartnersList(testData, application);

  application.lumpSums = [];
  application.description.workPlan[0].activities[0].cypressReferencePartner = application.partners[0].details.abbreviation;
  application.description.workPlan[0].activities[1].cypressReferencePartner = application.partners[1].details.abbreviation;
}

function preparePartnersList(testData, application) {
  application.partners = [];
  testData.partners.forEach(partnerData => {
    const tempPartner = JSON.parse(JSON.stringify(partner));
    tempPartner.details.abbreviation = partnerData.abbreviation;
    tempPartner.details.role = partnerData.role;
    tempPartner.budget = partnerData.budget;
    tempPartner.cofinancing = partnerData.cofinancing;
    application.partners.push(tempPartner);
  });
}

function enableModification(applicationId) {
  cy.loginByRequest(user.programmeUser.email);

  cy.visit(`app/project/detail/${applicationId}/modification`, {failOnStatusCode: false})
  cy.contains('Open new modification')
    .click();
  cy.get('jems-confirm-dialog')
    .should('be.visible');
  cy.get('jems-confirm-dialog')
    .find('.mat-dialog-actions')
    .contains('Confirm')
    .click();
  cy.contains('You have successfully opened a modification')
    .should('be.visible');
}

function disableSelectedPartners(testData, applicationId, partnerIdsToDisable) {
  cy.loginByRequest(user.applicantUser.email);
  cy.visit(`app/project/detail/${applicationId}/applicationFormPartner`, {failOnStatusCode: false})
    .then(() => {
      disablePartnersByIds(testData, partnerIdsToDisable);
      submitProjectApp(applicationId);
    });
}

function submitProjectApp(applicationId) {
  cy.runPreSubmissionCheck(applicationId);
  cy.submitProjectApplication(applicationId);
}

function disablePartnersByIds(testData, partnerIdsToDisable) {
  partnerIdsToDisable.forEach(id => {
    cy.contains('mat-row', testData.partners[id].abbreviation)
      .contains('button', 'Deactivate partner')
      .click();

    cy.contains('button', 'Confirm')
      .click();

    cy.contains('div', `Partner "${testData.partners[id].abbreviation}" deactivated successfully`)
      .scrollIntoView()
      .should('be.visible');

    cy.contains('div', `Partner "${testData.partners[id].abbreviation}" deactivated successfully`)
      .should('not.exist');

    cy.contains('mat-row', testData.partners[id].abbreviation)
      .contains('button', 'Deactivate partner')
      .scrollIntoView()
      .should('be.disabled');

    cy.contains('mat-row', testData.partners[id].abbreviation)
      .contains('Inactive')
      .scrollIntoView()
      .should('be.visible');

    cy.contains('mat-row', testData.partners[id].abbreviation)
      .contains('mat-icon', 'person_off')
      .scrollIntoView()
      .should('be.visible');

    verifyIconsInProjectPartners(testData, id, true);
  })
}

function verifyPartnerChangesBeforeApproving(testData, applicationId, partnerIdsToDisable) {
  const disabledPartnerAbbreviation = testData.partners[partnerIdsToDisable[0]].abbreviation;

  cy.loginByRequest(user.admin.email);

  cy.visit(`app/project/detail/${applicationId}/applicationFormPartner`, {failOnStatusCode: false})
    .then(() => {
      cy.get('mat-sidenav')
        .should('be.visible');

      partnerIdsToDisable.forEach(id => {
        cy.contains('mat-row', testData.partners[id].abbreviation)
          .contains('Inactive')
          .scrollIntoView()
          .should('be.visible');

        cy.contains('mat-row', testData.partners[id].abbreviation)
          .contains('mat-icon', 'person_off')
          .scrollIntoView()
          .should('be.visible');

        verifyIconsInPartnerDetails(testData, id, false);
        verifyIconsInProjectPartners(testData, id, true);
      });

      verifyIconsInProjectPrivileges(testData, partnerIdsToDisable, false)
      verifyDeactivatedPartnerBannerDisplay("Partner details", disabledPartnerAbbreviation, false);
      verifyDeactivatedPartnerBannerDisplay("Project partners", disabledPartnerAbbreviation, true);
    });
}

function verifyIconsInProjectPrivileges(testData, partnerIdsToDisable, shouldIconsBeDisplayed) {
  const displayFlag = shouldIconsBeDisplayed ? 'be.visible' : 'not.exist';

  cy.contains('Project privileges')
    .click()
    .then(() => {
      partnerIdsToDisable.forEach(id => {
        cy.get(`mat-expansion-panel-header:contains("${testData.partners[id].abbreviation}")`)
          .scrollIntoView()
          .contains('mat-icon', 'person_off')
          .should(displayFlag)
      });
    });
}

function verifyIconsInPartnerDetails(testData, id, shouldIconsBeDisplayed) {
  const displayFlag = shouldIconsBeDisplayed ? 'be.visible' : 'not.exist';

  cy.get('mat-expansion-panel-header:contains("Partner details")')
    .next('div')
    .find(`li:contains("${testData.partners[id].abbreviation}")`)
    .then((foundElement) => {
      if (shouldIconsBeDisplayed) {
        cy.wrap(foundElement)
          .contains('mat-icon', 'person_off')
          .scrollIntoView()
          .should(displayFlag)
      } else {
        cy.wrap(foundElement)
          .contains('mat-icon', 'person_off')
          .should(displayFlag)
      }
    })
}

function verifyIconsInProjectPartners(testData, id, shouldIconsBeDisplayed) {
  const displayFlag = shouldIconsBeDisplayed ? 'be.visible' : 'not.exist';

  cy.get('mat-expansion-panel-header:contains("Project partners")')
    .next('div')
    .find(`li:contains("${testData.partners[id].abbreviation}")`)
    .contains('mat-icon', 'person_off')
    .then((foundElement) => {
      if (shouldIconsBeDisplayed) {
        cy.wrap(foundElement)
          .scrollIntoView()
          .should(displayFlag);
      } else {
        cy.wrap(foundElement)
          .should(displayFlag);
      }
    })
}

function verifyDeactivatedPartnerBannerDisplay(headerTitle, disabledPartnerAbbreviation, shouldBannerBeDisplayed) {
  const displayFlag = shouldBannerBeDisplayed ? 'be.visible' : 'not.exist';

  cy.get(`mat-expansion-panel-header:contains(${headerTitle})`)
    .next('div')
    .find(`span:contains("${disabledPartnerAbbreviation}")`)
    .click()
    .then(() => {
      cy.contains('div', "You are currently viewing a deactivated partner.")
        .should(displayFlag);
    });
}

function verifyPartnerInPartnerDetails(shouldPartnerBeDisplayed) {
  const displayFlag = shouldPartnerBeDisplayed ? 'be.visible' : 'not.exist';

  cy.get('mat-expansion-panel-header:contains("Partner details")')
    .next('div')
    .then((foundElement) => {
      if (shouldPartnerBeDisplayed) {
        cy.wrap(foundElement)
          .find(`li:contains("PP30")`)
          .scrollIntoView()
          .should(displayFlag)
      } else {
        cy.wrap(foundElement)
          .find(`li:contains("PP30")`)
          .should(displayFlag)
      }
    });
}

function verifyPartnerAvailabilityInProjectPrivileges(shouldPartnerBeDisplayed) {
  const displayFlag = shouldPartnerBeDisplayed ? 'be.visible' : 'not.exist';

  cy.contains('Project privileges')
    .click()
    .then((foundElement) => {
      if (shouldPartnerBeDisplayed) {
        cy.wrap(foundElement)
          .get(`mat-expansion-panel-header:contains("PP30")`)
          .scrollIntoView()
          .should(displayFlag)
      } else {
        cy.wrap(foundElement)
          .get(`mat-expansion-panel-header:contains("PP30")`)
          .should(displayFlag)
      }
    });
}

function verifyPartnerAvailability(shouldPartnerBeDisplayed) {
  verifyPartnerInPartnerDetails(shouldPartnerBeDisplayed);
  verifyPartnerAvailabilityInProjectPrivileges(shouldPartnerBeDisplayed);
}
