import user from '../../../fixtures/users.json';
import faker from '@faker-js/faker';
import call from '../../../fixtures/api/call/1.step.call.json';
import application from '../../../fixtures/api/application/application.json';

context('Project privileges tests', () => {

  it('TB-379 Automatically assign users to projects', () => {
    cy.fixture('project/project-privileges/TB-379.json').then(testData => {
      cy.loginByRequest(user.admin.email);
      testData.privilegedUser.email = faker.internet.email();
      cy.createUser(testData.privilegedUser);
      cy.visit('app/project', {failOnStatusCode: false});

      cy.contains('Assignment').click();
      cy.get('.mat-paginator-range-label').then(paginatorRange => {
        const numberOfPages = +paginatorRange.text().match(/\d - (\d+) of \d+/)[1];

        cy.get(`mat-chip:contains('${testData.privilegedUser.email}')`).should(chipElements => {
          expect(chipElements).to.have.length(numberOfPages);
          expect(chipElements).not.to.have.class('mat-chip-selected-user');
        });
      });
    });
  });

  it('TB-363 Add/remove user privileges to/from project', () => {
    cy.fixture('project/project-privileges/TB-363.json').then(testData => {
      cy.loginByRequest(user.programmeUser.email);
      cy.createCall(call).then(callId => {
        application.details.projectCallId = callId;
        cy.publishCall(callId);
      });
      cy.loginByRequest(user.applicantUser.email);
      cy.createFullApplication(application, user.programmeUser.email).then(applicationId => {
        testData.projectCollaborator.email = faker.internet.email();
        cy.createUser(testData.projectCollaborator, user.admin.email);

        // Add user privileges to the project
        cy.visit(`app/project/detail/${applicationId}/privileges`, {failOnStatusCode: false});
        cy.get('jems-application-form-privileges-expansion-panel').then(applicationFormUsers => {
          cy.wrap(applicationFormUsers).contains('+').click();
          cy.wrap(applicationFormUsers).find('input[formcontrolname="userEmail"]').last().type(testData.projectCollaborator.email);
          cy.wrap(applicationFormUsers).contains('Save changes').click();

          cy.get('div.jems-alert-success').should('contain', 'Project collaborators were saved successfully');
        });

        cy.loginByRequest(testData.projectCollaborator.email);
        cy.visit('/', {failOnStatusCode: false});
        cy.get('jems-project-application-list').contains(application.identification.acronym).should('be.visible');

        // Remove user privileges from the project
        cy.loginByRequest(user.applicantUser.email);
        cy.visit(`app/project/detail/${applicationId}/privileges`, {failOnStatusCode: false});
        cy.get('jems-application-form-privileges-expansion-panel').then(applicationFormUsers => {
          cy.wrap(applicationFormUsers).find('mat-icon:contains("delete")').last().click();
          cy.wrap(applicationFormUsers).contains('Save changes').click();

          cy.get('div.jems-alert-success').should('contain', 'Project collaborators were saved successfully');
        });

        cy.loginByRequest(testData.projectCollaborator.email);
        cy.visit('/', {failOnStatusCode: false});
        cy.get('jems-project-application-list').contains(application.identification.acronym).should('not.exist');

        cy.visit(`app/project/detail/${applicationId}`, {failOnStatusCode: false});
        cy.get('jems-project-detail-page').should('be.empty');
      });
    });
  });

  it('TB-364 Restrict management of project specific privileges', () => {
    cy.fixture('project/project-privileges/TB-364.json').then(testData => {
      cy.loginByRequest(user.admin.email);
      testData.programmeRole.name = `programmeRole_${faker.random.alphaNumeric(5)}`;
      cy.createRole(testData.programmeRole).then(roleId => {
        testData.programmeUser.userRoleId = roleId;
        testData.programmeUser.email = faker.internet.email();
        cy.createUser(testData.programmeUser);
        cy.loginByRequest(testData.programmeUser.email);

        cy.visit('app/project/detail/1', {failOnStatusCode: false});
        cy.contains('Project privileges').should('not.exist');

        cy.visit('app/project/detail/1/privileges', {failOnStatusCode: false});
        cy.get('jems-application-form-privileges-expansion-panel').should('not.exist');
      });
    });
  });
})
