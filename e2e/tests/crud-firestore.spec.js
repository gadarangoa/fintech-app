// Pruebas end-to-end del sistema de solicitudes de crédito contra Cloud Firestore.
//
// Recorren el flujo completo que pide la actividad: autenticación, carga masiva desde CSV,
// consulta, edición, creación y eliminación. Cada paso guarda una captura en e2e/evidencias/
// para usarla como evidencia en el documento.
//
// Credenciales: se leen de las variables de entorno APP_USER y APP_PASSWORD
// (usuario registrado en la colección "analistas"); nunca se escriben en el código.
const { test, expect } = require('@playwright/test');
const path = require('path');

const USUARIO = process.env.APP_USER;
const CLAVE = process.env.APP_PASSWORD;
const CSV = path.resolve(__dirname, '..', '..', 'datos', 'solicitudes_credito.csv');
const EVIDENCIAS = path.resolve(__dirname, '..', 'evidencias');

// Registros del CSV de ejemplo (datos/solicitudes_credito.csv)
const CEDULAS_CSV = Array.from({ length: 10 }, (_, i) => String(1078123401 + i));
const CEDULA_EDITAR = '1078123403';
const CEDULA_NUEVA = '1078123499';

test.describe.configure({ mode: 'serial' });

let page;
let paso = 0;

async function evidencia(nombre, opciones = {}) {
  paso += 1;
  const archivo = path.join(EVIDENCIAS, `${String(paso).padStart(2, '0')}-${nombre}.png`);
  await page.screenshot({ path: archivo, fullPage: true, ...opciones });
}

async function irATabla() {
  await page.goto('/creditos/ver-tabla');
  await expect(page.locator('table.tabla-fintech')).toBeVisible();
}

function filaDe(cedula) {
  return page.locator('table.tabla-fintech tbody tr', { has: page.locator('td', { hasText: new RegExp(`^${cedula}$`) }) });
}

/** Envía el formulario de captura y espera a que el servidor confirme la escritura en Firestore. */
async function enviarFormulario() {
  await Promise.all([
    page.waitForResponse((r) => r.url().endsWith('/creditos/guardar')),
    page.click('button.btn-enviar-master'),
  ]);
  await page.waitForLoadState('networkidle');
}

async function buscar(cedula) {
  await page.goto('/creditos');
  await page.fill('input[name="txtCedulaBuscar"]', cedula);
  await page.click('form.barra-busqueda button[type="submit"]');
  await page.waitForLoadState('networkidle');
}

test.beforeAll(async ({ browser }) => {
  test.skip(!USUARIO || !CLAVE, 'Defina APP_USER y APP_PASSWORD con un analista registrado en Firestore');
  page = await browser.newPage();
});

test.afterAll(async () => {
  if (page) await page.close();
});

test('1. Las rutas protegidas redirigen al login', async () => {
  await page.goto('/creditos');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByText('Ingreso Seguro al Sistema')).toBeVisible();
  await evidencia('login');
});

test('2. Credenciales inválidas son rechazadas', async () => {
  await page.fill('input[name="username"]', USUARIO);
  await page.fill('input[name="password"]', 'clave-incorrecta');
  await page.click('button.btn-login');
  await expect(page).toHaveURL(/\/login\?error=true$/);
  await expect(page.locator('.alerta-error')).toContainText('incorrectos');
  await evidencia('login-rechazado');
});

test('3. Un analista registrado en Firestore inicia sesión', async () => {
  await page.fill('input[name="username"]', USUARIO);
  await page.fill('input[name="password"]', CLAVE);
  await page.click('button.btn-login');
  await expect(page).toHaveURL(/\/creditos$/);
  await expect(page.locator('.header-fintech h1')).toHaveText(/Solicitudes de Cr[eé]dito/i);
  await evidencia('panel-principal');
});

test('4. Carga masiva: el CSV inserta los 10 registros en Firestore', async () => {
  await page.goto('/creditos');
  // El botón CARGAR ARCHIVO abre este input oculto; al elegir el archivo el formulario se envía solo
  await Promise.all([
    page.waitForResponse((r) => r.url().endsWith('/creditos/cargar-csv')),
    page.setInputFiles('#exploradorArchivos', CSV),
  ]);
  await page.waitForLoadState('networkidle');
  await irATabla();
  for (const cedula of CEDULAS_CSV) {
    await expect(filaDe(cedula)).toHaveCount(1);
  }
  await expect(filaDe('1078123401')).toContainText('Valentina Gómez Ruiz');
  await expect(filaDe('1078123401')).toContainText('Bogotá');
  await evidencia('tabla-despues-de-cargar-csv');
});

test('5. Consulta: buscar una solicitud por cédula', async () => {
  await buscar(CEDULA_EDITAR);
  const resultado = page.locator('.caja-resultado-busqueda');
  await expect(resultado).toBeVisible();
  await expect(resultado).toContainText('Laura Martínez Ochoa');
  await expect(resultado).toContainText('$3200000');
  await expect(resultado).toContainText('RECHAZADO');
  // El formulario inferior se precarga y la cédula queda bloqueada para no alterar la llave del documento
  await expect(page.locator('#formCedula')).toHaveValue(CEDULA_EDITAR);
  await expect(page.locator('#formCedula')).toHaveAttribute('readonly', /.*/);
  await evidencia('consulta-por-cedula');
});

test('6. Búsqueda de una cédula inexistente no muestra resultados', async () => {
  await buscar('9999999999');
  await expect(page.locator('.caja-resultado-busqueda')).toHaveCount(0);
  await evidencia('consulta-sin-resultados', { fullPage: false });
});

test('7. Edición: actualizar monto y aprobación de una solicitud', async () => {
  await buscar(CEDULA_EDITAR);
  await page.fill('#formMonto', '4000000');
  await page.check('#chkAprobado');
  await evidencia('edicion-formulario-modificado');
  await enviarFormulario();
  await buscar(CEDULA_EDITAR);
  const resultado = page.locator('.caja-resultado-busqueda');
  await expect(resultado).toContainText('$4000000');
  await expect(resultado).toContainText('APROBADO');
  await evidencia('edicion-resultado');
});

test('8. Inserción manual: crear una solicitud desde el formulario', async () => {
  await page.goto('/creditos');
  page.once('dialog', (d) => d.accept());
  await page.click('button.btn-azul'); // LIMPIAR CAMPOS
  await page.fill('#formCedula', CEDULA_NUEVA);
  await page.fill('#formNombre', 'Camilo Andrés Prueba Pérez');
  await page.fill('#formCorreo', 'cprueba@fintechmail.com');
  await page.fill('#formCiudad', 'Tunja');
  await page.fill('#formMonto', '5000000');
  await page.fill('#formPlazo', '24');
  await page.fill('#formTasa', '1.9');
  await page.fill('#formIngresos', '3500000');
  await page.fill('#formScore', '690');
  await page.check('#chkAprobado');
  await evidencia('creacion-formulario');
  await enviarFormulario();
  await irATabla();
  await expect(filaDe(CEDULA_NUEVA)).toContainText('Camilo Andrés Prueba Pérez');
  await filaDe(CEDULA_NUEVA).scrollIntoViewIfNeeded();
  await evidencia('creacion-en-tabla');
});

test('9. Eliminación: borrar una solicitud con confirmación', async () => {
  await irATabla();
  let mensaje = '';
  page.once('dialog', async (d) => { mensaje = d.message(); await d.accept(); });
  await Promise.all([
    page.waitForURL(/\/creditos\/ver-tabla$/),
    filaDe(CEDULA_NUEVA).locator('button.btn-tabla-rojo').click(),
  ]);
  expect(mensaje).toContain('ELIMINAR');
  await expect(filaDe(CEDULA_NUEVA)).toHaveCount(0);
  await evidencia('eliminacion-resultado');
});
