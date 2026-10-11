# FirebaseCSVManager · Sistema de Solicitudes de Crédito

[![CI y GitHub Pages](https://github.com/gadarangoa/fintech-app/actions/workflows/ci-pages.yml/badge.svg)](https://github.com/gadarangoa/fintech-app/actions/workflows/ci-pages.yml)

Sitio del proyecto: https://gadarangoa.github.io/fintech-app/

Aplicación web en **Java (Spring Boot + Thymeleaf)** conectada de forma híbrida a una base de datos NoSQL en la nube
(**Google Cloud Firestore**). Permite a un analista autenticado cargar solicitudes de crédito de forma masiva desde un
archivo **CSV** y administrarlas con las cuatro operaciones básicas: **inserción, consulta, edición y eliminación**.

Proyecto de la actividad sumativa de la Unidad 4 del módulo *Fundamentos de la Tecnología Cloud*,
Maestría en Arquitectura de Software, Institución Universitaria Politécnico Grancolombiano.

**Autores:** Gustavo Arango Angulo, Harold Glasser Urueña

---

## Funcionalidades

| Operación | Dónde se hace | Implementación |
|---|---|---|
| Autenticación | Pantalla `/login` | `SecurityConfig` + `CustomUserDetailsService` validan el usuario contra la colección `analistas` de Firestore |
| Inserción masiva (CSV) | Botón **CARGAR ARCHIVO** | `CSVReaderUtil.cargarDatosDesdeCSVFlujoWeb()` → `CreditoDAO.insertarCredito()` |
| Inserción manual | Formulario de captura → **Procesar Operación** | `CreditoWebController.accionGuardarOEditar()` → `CreditoDAO.insertarCredito()` |
| Consulta | Campo de cédula → **BUSCAR** | `CreditoDAO.procesarBusqueda()` (consulta `whereEqualTo("cedula", …)`) |
| Edición | Buscar una cédula, modificar el formulario precargado y **Procesar Operación** | El documento se reescribe con la cédula como ID (`set`) |
| Eliminación | **VER TABLA DE DATOS** → **Eliminar** (con confirmación) | `CreditoDAO.eliminarCredito()` |
| Reporte general | **VER TABLA DE DATOS** | `CreditoDAO.obtenerTodosLosCreditos()` |

## Arquitectura

Patrón **Modelo-Vista-Controlador (MVC)**: el navegador consume vistas Thymeleaf servidas por Spring Boot (Tomcat embebido),
y la capa de servicio se comunica con Cloud Firestore mediante el **Firebase Admin SDK**, autenticado con una cuenta de servicio.

```
Navegador ──HTTP──▶ Spring Boot :8085 ──────────────▶ Cloud Firestore (Google Cloud)
                    ├─ Spring Security (login)         ├─ solicitudes_credito
                    ├─ Controladores (MVC)             └─ analistas
                    ├─ CreditoDAO / FirebaseService  ◀── Firebase Admin SDK (gRPC + TLS)
                    └─ Vistas Thymeleaf
```

| Paquete | Responsabilidad |
|---|---|
| `com.proyecto.main` | Clase de arranque (`FintechWebApplication`) y configuración de seguridad (`SecurityConfig`) |
| `com.proyecto.controlador` | Controladores web: créditos (`CreditoWebController`) y login (`LoginController`) |
| `com.proyecto.servicio` | Acceso a Firestore (`FirebaseService`, `CreditoDAO`) y usuarios (`CustomUserDetailsService`) |
| `com.proyecto.modelo` | Entidad `Credito` |
| `com.proyecto.util` | Lectura del CSV (`CSVReaderUtil`) |
| `src/main/resources/templates` | Vistas `login.html`, `index.html` y `tabla.html` |
| `src/main/resources/messages.properties` | Textos de la interfaz externalizados |

## Tecnologías

| Componente | Versión |
|---|---|
| Java (JDK) | 8 o superior (probado con JDK 21) |
| Spring Boot (web, Thymeleaf, Security) | 2.7.15 |
| Firebase Admin SDK | 9.2.0 |
| OpenCSV | 5.7.1 |
| Maven | 3.9.9 (incluido con el Maven Wrapper `./mvnw`) |
| Playwright (pruebas E2E) | 1.56.1 |

## Requisitos

1. **JDK 8 o superior.** Compruebe con `java -version`. Si no lo tiene, instale
   [Eclipse Temurin](https://adoptium.net/temurin/releases/) (en macOS con Apple Silicon elija el instalador `.pkg` *aarch64*).
2. **Maven no es necesario:** el proyecto trae el *Maven Wrapper* (`./mvnw` en macOS/Linux, `mvnw.cmd` en Windows),
   que descarga Maven automáticamente la primera vez.
3. **Un proyecto de Firebase con Cloud Firestore** y la llave de una cuenta de servicio (ver la siguiente sección).
4. *(Opcional, solo para las pruebas automatizadas)* **Node.js 18 o superior.**

## Configuración de Firebase

1. En [Firebase Console](https://console.firebase.google.com/) cree un proyecto y habilite **Firestore Database**.
2. Cree la colección **`analistas`** con un documento por usuario. El **ID del documento es el nombre de usuario**:

   | Campo | Tipo | Ejemplo |
   |---|---|---|
   | `username` | string | `admin` |
   | `password` | string | *(la contraseña del analista)* |
   | `rol` | string | `ROLE_ANALISTA` |

3. La colección **`solicitudes_credito`** se crea sola con la primera carga del CSV. Cada documento tiene:

   | Campo | Tipo | Descripción |
   |---|---|---|
   | `cedula` | string | Documento de identidad (también es el ID del documento) |
   | `nombreCliente` | string | Nombre completo |
   | `correo` | string | Correo de notificación |
   | `ciudad` | string | Ciudad de residencia |
   | `montoSolicitado` | number (int64) | Valor solicitado en pesos |
   | `plazoMeses` | number (int64) | Número de cuotas |
   | `tasaInteres` | number (double) | Tasa mensual aplicada (%) |
   | `ingresosMensuales` | number (int64) | Ingresos reportados |
   | `scoreCrediticio` | number (int64) | Puntaje de 0 a 1000 |
   | `aprobado` | boolean | Resultado de la evaluación |

4. Descargue la llave: **Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada**.
5. Guarde el archivo descargado en la **raíz del proyecto** con el nombre **`firebase-key.json`**.

> ⚠️ La llave da acceso de administrador a todo el proyecto de Firebase. Está excluida en el `.gitignore`:
> no la suba a GitHub ni la incluya en archivos comprimidos que vaya a compartir. Si se filtra, revóquela en la
> consola y genere una nueva.

## Instalación y compilación

```bash
# 1. Clonar el repositorio
git clone https://github.com/gadarangoa/fintech-app.git
cd fintech-app

# 2. Copiar la llave de Firebase a la raíz del proyecto
cp ~/Downloads/<archivo-descargado>.json firebase-key.json

# 3. Compilar y empaquetar (genera target/FintechWebMVC-0.0.1-SNAPSHOT.jar)
./mvnw clean package
```

En Windows use `mvnw.cmd clean package`.

## Ejecución

Desde la **raíz del proyecto** (la aplicación busca `firebase-key.json` en la carpeta desde donde se ejecuta):

```bash
java -jar target/FintechWebMVC-0.0.1-SNAPSHOT.jar
# o, sin empaquetar:
./mvnw spring-boot:run
```

En la consola debe aparecer `[FIREBASE FIRESTORE]: Conectado exitosamente.` y
`Corriendo exitosamente en http://localhost:8085/creditos`.

Abra **http://localhost:8085** en el navegador e ingrese con un usuario de la colección `analistas`.

## Formato del archivo CSV

- Primera fila de encabezados (se omite automáticamente) y una fila por solicitud, con las columnas en este orden:

  ```csv
  cedula,nombreCliente,correo,ciudad,montoSolicitado,plazoMeses,tasaInteres,ingresosMensuales,scoreCrediticio,aprobado
  1078123401,Valentina Gómez Ruiz,vgomez@fintechmail.com,Bogotá,8500000,24,1.85,4200000,720,true
  ```

- Acepta coma (`,`) o punto y coma (`;`) como separador.
- Se lee con codificación **Windows-1252**, la que usa Microsoft Excel en Windows al guardar como *CSV (delimitado por comas)*,
  para conservar tildes y eñes.
- Si la cédula ya existe, el registro se sobrescribe (la cédula es el ID del documento).
- En [`datos/solicitudes_credito.csv`](datos/solicitudes_credito.csv) hay un archivo de ejemplo con 10 solicitudes.

## Pruebas automatizadas (Playwright)

La carpeta [`e2e/`](e2e) contiene pruebas *end-to-end* que abren un navegador Chromium y recorren el flujo completo
contra Firestore: rutas protegidas, login rechazado y exitoso, carga del CSV, consulta, búsqueda sin resultados,
edición, creación y eliminación. Cada paso guarda una captura en `e2e/evidencias/`.

```bash
# Con la aplicación corriendo en http://localhost:8085
cd e2e
npm install
npx playwright install chromium          # solo la primera vez
APP_USER=<usuario> APP_PASSWORD=<clave> npx playwright test
npx playwright show-report reporte-html  # reporte HTML
```

En Windows (PowerShell): `$env:APP_USER="<usuario>"; $env:APP_PASSWORD="<clave>"; npx playwright test`.

Las credenciales se pasan por variables de entorno para no escribirlas en el código. Las pruebas insertan los
10 registros del CSV de ejemplo, modifican uno de ellos (`1078123403`) y crean y eliminan un registro temporal (`1078123499`).

El archivo [`.mcp.json`](.mcp.json) configura el servidor **Playwright MCP**, para que un asistente de IA compatible
(por ejemplo Claude Code) pueda controlar el navegador y explorar la aplicación.

## Integración continua y GitHub Pages

El flujo [`.github/workflows/ci-pages.yml`](.github/workflows/ci-pages.yml) se ejecuta en cada push a `main`:

1. **Compilar:** `./mvnw -B clean package` con JDK 21 y publica el `.jar` como artefacto.
2. **Pruebas E2E:** levanta la aplicación contra Firestore y ejecuta Playwright. Solo corre si el repositorio tiene
   estos secretos (*Settings → Secrets and variables → Actions*):
   - `FIREBASE_KEY_JSON`: contenido completo del archivo de la llave de la cuenta de servicio.
   - `APP_USER` y `APP_PASSWORD`: un analista registrado en la colección `analistas`.
   Si faltan, el paso se omite con un aviso y el resto del flujo continúa. No se ejecuta en *pull requests*
   porque escribe en la base de datos real.
3. **Publicar:** sube a GitHub Pages la carpeta [`docs/`](docs) con la descripción del proyecto, las evidencias y el
   reporte HTML de Playwright de la última ejecución.

GitHub Pages solo sirve archivos estáticos, por lo que la aplicación Spring Boot no se ejecuta allí: el sitio documenta
el proyecto y muestra los resultados de las pruebas. Para habilitarlo, en *Settings → Pages* elija
**Source: GitHub Actions**.

## Estructura del proyecto

```
fintech-app/
├── pom.xml                         Dependencias Maven (Spring Boot, Firebase Admin, OpenCSV)
├── mvnw, mvnw.cmd, .mvn/           Maven Wrapper
├── firebase-key.json               Llave de la cuenta de servicio (NO versionada)
├── datos/solicitudes_credito.csv   CSV de ejemplo
├── e2e/                            Pruebas Playwright y capturas de evidencia
├── docs/                           Sitio publicado en GitHub Pages
├── .github/workflows/              Integración continua y publicación
└── src/main/
    ├── java/com/proyecto/
    │   ├── main/                   FintechWebApplication, SecurityConfig
    │   ├── controlador/            CreditoWebController, LoginController, CreditoCRUD
    │   ├── servicio/               FirebaseService, CreditoDAO, CustomUserDetailsService
    │   ├── modelo/                 Credito
    │   └── util/                   CSVReaderUtil
    └── resources/
        ├── templates/              login.html, index.html, tabla.html
        ├── static/estilos.css
        └── messages.properties
```

## Solución de problemas

| Síntoma | Causa probable |
|---|---|
| `Error al conectar con Firestore: firebase-key.json (No such file or directory)` | La llave no está en la raíz o la aplicación se ejecutó desde otra carpeta |
| "Usuario o Contraseña incorrectos en la nube" con datos correctos | El ID del documento en `analistas` no coincide con el usuario digitado |
| Tildes como `Ã©` después de cargar el CSV | El archivo se guardó en UTF-8; guárdelo como CSV de Excel (Windows-1252) |
| `Port 8085 was already in use` | Otra instancia de la aplicación sigue abierta |

## Seguridad y mejoras pendientes

- Las contraseñas de `analistas` se guardan en texto plano en Firestore; en producción deberían almacenarse con un hash
  (por ejemplo BCrypt) o delegarse a Firebase Authentication.
- La protección CSRF de Spring Security está desactivada para simplificar los formularios.
- Las reglas de Firestore en *modo de prueba* vencen a los 30 días; el SDK de administrador no depende de ellas, pero
  cualquier cliente web sí.

## Licencia

Proyecto académico.
