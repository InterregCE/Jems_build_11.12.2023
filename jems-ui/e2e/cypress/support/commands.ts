// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
import faker from '@faker-js/faker';

declare global {

  namespace Cypress {
    interface Chainable {
      parsePDF();

      parseCSV();

      parseXLSX();

      clickToDownload(requestToIntercept: string, fileExtension: string);
    }
  }
}

Cypress.Commands.add('parsePDF', {prevSubject: true}, (subject) => {
  cy.task('parsePDF', subject);
});

Cypress.Commands.add('parseCSV', {prevSubject: true}, (subject) => {
  cy.task('parseCSV', subject);
});

Cypress.Commands.add('parseXLSX', {prevSubject: true}, (subject) => {
  cy.task('parseXLSX', subject);
});

Cypress.Commands.add('clickToDownload', {prevSubject: true}, (subject, requestToIntercept, fileExtension) => {
  const randomizeDownload = `downloadRequest ${faker.random.alphaNumeric(5)}`;
  cy.intercept(requestToIntercept).as(randomizeDownload);
  cy.wrap(subject).click();
  cy.wait(`@${randomizeDownload}`).then(result => {
    const regex = new RegExp(`filename="(.*\.${fileExtension})"`);
    const fileNameMatch = regex.exec(result.response.headers['content-disposition'].toString());
    if (!fileNameMatch) {
      throw new Error(`Downloaded file does not have ${fileExtension} extension`);
    }
    const fileName = fileNameMatch[1];
    if (fileExtension === 'pdf') {
      cy.readFile('./cypress/downloads/' + fileName, null).parsePDF().then(file => {
        file.fileName = fileName;
        cy.wrap(file);
      });
    } else if (fileExtension === 'csv') {
      cy.readFile('./cypress/downloads/' + fileName).parseCSV().then(content => {
        const file = {fileName: fileName, content: content};
        cy.wrap(file);
      });
    } else if (fileExtension === 'xlsx') {
      cy.readFile('./cypress/downloads/' + fileName, null).parseXLSX().then(content => {
        const file = {fileName: fileName, content: content};
        cy.wrap(file);
      });
    } else {
      throw new Error('No implementation for: ' + fileExtension);
    }
  });
});

export {}