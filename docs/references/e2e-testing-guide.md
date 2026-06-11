# E2E Testing Guide

## Current Status

E2E testing is not currently configured for this project. There is no Playwright, Selenium, or other E2E framework set up.

When E2E testing is needed in the future, this document should be updated with the chosen framework, configuration, and common diagnostic patterns.

For now, verification is done via:

- `./mvnw test` — unit and integration tests
- manual testing via the running Quarkus application
- `docs/testing/` — manual and exploratory testing notes
