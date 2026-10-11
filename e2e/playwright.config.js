// Configuración de Playwright para las pruebas end-to-end de FirebaseCSVManager.
// La aplicación debe estar corriendo antes de ejecutar las pruebas (por defecto en http://localhost:8085).
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  // Las pruebas modifican la misma base de datos en la nube, por eso se ejecutan en orden y sin paralelismo
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'reporte-html' }]],
  use: {
    baseURL: process.env.APP_URL || 'http://localhost:8085',
    viewport: { width: 1366, height: 900 },
    locale: 'es-CO',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
});
