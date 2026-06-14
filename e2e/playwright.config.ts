import { defineConfig, devices } from '@playwright/test';

const port = parseInt(process.env.PORT || '8086', 10);
const baseURL = process.env.BASE_URL || `http://localhost:${port}`;

export default defineConfig({
  testDir: 'tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  timeout: 90_000,
  expect: { timeout: 15_000 },

  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 10_000,
  },

  webServer: process.env.SKIP_WEBSERVER
    ? undefined
    : {
        command: `java -Dfile.encoding=UTF8 -Dquarkus.profile=dev -Dquarkus.http.port=${port} -Dnop.datasource.jdbc-url=jdbc:h2:mem:e2e -Dnop.orm.init-database-schema=true -jar app-mall-app/target/quarkus-app/quarkus-run.jar`,
        cwd: '..',
        port,
        timeout: 180_000,
        reuseExistingServer: !process.env.CI,
        stdout: 'pipe',
        stderr: 'pipe',
      },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
