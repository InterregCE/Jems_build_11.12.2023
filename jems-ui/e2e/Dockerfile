FROM cypress/included:12.14.0

WORKDIR e2e

# copy test files
COPY .git .git
COPY jems-ui/e2e/cypress cypress
COPY jems-ui/e2e/package-lock.json .
COPY jems-ui/e2e/package.json .
COPY jems-ui/e2e/tsconfig.json .
COPY jems-ui/e2e/cypress.config.ts .
COPY jems-ui/e2e/currents.config.js .

ENV NO_COLOR=1

RUN npm i

ENTRYPOINT ["npm", "run", "test"]
