# Guía de despliegue — SmartRoute (Laboratorios 6 y 7)

Todo el código y la configuración ya están listos (`render.yaml`, `backend/Dockerfile`,
`application-prod.properties`, CORS configurable). Lo que queda son los pasos que solo se pueden
hacer desde el navegador, con tu propia cuenta: crear la base de datos en Azure, el cluster en
MongoDB Atlas, y conectar el repositorio en Render.

## 1. Azure SQL Database (base relacional)

1. Entrá a [portal.azure.com](https://portal.azure.com) (cuenta gratuita de Azure for Students o
   la cuenta personal con el free tier — no hace falta tarjeta si usás Azure for Students con
   correo `.ac.cr`/universitario).
2. **Crear un recurso** → **SQL Database**.
   - **Base de datos**: `smartroute`.
   - **Servidor**: crear uno nuevo — anotá el nombre completo, algo como
     `smartroute-server.database.windows.net`.
   - **Autenticación**: usuario y contraseña SQL (anotá ambos — van a `DB_USERNAME`/`DB_PASSWORD`
     en Render, nunca en el repo).
   - **Proceso de proceso de cómputo**: elegí **Serverless**, tier **General Purpose**, con
     auto-pausa habilitada (es el "plan gratuito que se suspende" que ya contempla la rúbrica).
3. Una vez creada, andá a **Redes** del servidor (no de la base) y activá
   **Allow Azure services and resources to access this server** — sin esto, Render no puede
   conectarse.
4. La cadena de conexión JDBC para `DB_URL` (property de Render, no un archivo):
   ```
   jdbc:sqlserver://smartroute-server.database.windows.net:1433;databaseName=smartroute;encrypt=true;trustServerCertificate=false
   ```
   (reemplazá `smartroute-server` por el nombre real del servidor que te asignó Azure).
5. No hace falta correr las migraciones a mano: `smartroute-api` las aplica solo al arrancar
   (Flyway, `V1` a `V3`) — es justo lo que pide la rúbrica ("las migraciones se aplican en el
   arranque").

## 2. MongoDB Atlas (base documental)

1. Entrá a [cloud.mongodb.com](https://cloud.mongodb.com) y creá un cluster **M0 (free tier)**.
2. **Database Access** → creá un usuario de base de datos (usuario/contraseña, no el de tu cuenta
   de Atlas).
3. **Network Access** → **Add IP Address** → **Allow access from anywhere** (`0.0.0.0/0`). Render
   no tiene IPs fijas en el plan gratuito, así que no hay una IP concreta que permitir.
4. **Connect** → **Drivers** → copiá la cadena `mongodb+srv://...`. Esa es tu `MONGODB_URI`
   completa (ya incluye usuario y contraseña) — pegala tal cual en Render.

## 3. Render — conectar el Blueprint

1. Entrá a [render.com](https://render.com), conectá tu cuenta de GitHub y dale acceso al
   repositorio `SmartRoute`.
2. **New** → **Blueprint** → elegí el repositorio. Render lee `render.yaml` de la raíz y propone
   crear `smartroute-api` (Docker) y `smartroute-web` (sitio estático) juntos.
3. Al crear el Blueprint, Render va a pedir valores para las variables marcadas `sync: false`
   en `render.yaml`:
   - `DB_URL` → la cadena JDBC del paso 1.
   - `DB_USERNAME` / `DB_PASSWORD` → las credenciales del servidor SQL del paso 1.
   - `MONGODB_URI` → la cadena del paso 2.
   - `JWT_SECRET` **no** la pide — `generateValue: true` hace que Render genere un valor aleatorio
     por su cuenta, así queda garantizado distinto al de desarrollo sin que nadie lo escriba a
     mano.
4. Dejá que termine el primer deploy de `smartroute-api` y anotá su URL pública (algo como
   `https://smartroute-api.onrender.com`).
5. Si esa URL no coincide con la que `render.yaml` puso por defecto en `VITE_API_BASE_URL`
   (servicio `smartroute-web`) o en `CORS_ALLOWED_ORIGIN` (servicio `smartroute-api`), corregilas a
   mano en **Environment** de cada servicio y volvé a desplegar ese servicio (el build de la SPA
   necesita rehacerse para que Vite incruste la URL correcta).
6. Repetí el mismo chequeo con la URL real de `smartroute-web` contra `CORS_ALLOWED_ORIGIN`.

## 4. Verificar que quedó funcionando

```bash
# Swagger UI
open https://smartroute-api.onrender.com/swagger-ui/index.html

# Login (usuarios de la semilla, password Demo1234!)
curl -X POST https://smartroute-api.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"kimberley.demo@ejemplo.com","password":"Demo1234!"}'

# Módulo admin
open https://smartroute-api.onrender.com/login

# SPA
open https://smartroute-web.onrender.com
```

Recordá: el plan gratuito de Render suspende `smartroute-api` tras 15 minutos sin tráfico y tarda
~1 minuto en reactivarse con la primera petición — normal, ya lo contempla la rúbrica. El sitio
estático (`smartroute-web`) no se suspende nunca (se sirve desde CDN, no desde una instancia).

## 5. Laboratorio 7 — despliegue automático desde CI

El `deploy` job de `.github/workflows/ci.yml` dispara el deploy de cada servicio llamando a su
**Deploy Hook** de Render (una URL que equivale a una credencial — cualquiera con esa URL puede
disparar un deploy, así que nunca va en el repo ni en `AUDITORIA-SEGURIDAD.md`, solo en GitHub
Secrets).

1. En Render, por cada servicio (`smartroute-api` y `smartroute-web`): **Settings** → **Deploy
   Hook** → copiá la URL.
2. En el mismo **Settings**, apagá **Auto-Deploy** (ponelo en "No" / "Off") para los dos servicios.
   Ya quedó declarado en `render.yaml` (`autoDeployTrigger: off`), pero si los servicios se
   crearon con el Blueprint del Laboratorio 6 antes de este cambio, confirmá que el toggle
   manual también esté apagado — si no, Render va a desplegar en cada push sin esperar a que las
   pruebas de GitHub Actions pasen, que es justo lo que este laboratorio pide evitar.
3. Agregá las dos URLs como *secrets* del repositorio — **desde tu propia terminal o la web de
   GitHub**, nunca pegándolas en el chat (son sensibles, igual que una contraseña):
   ```bash
   gh secret set RENDER_DEPLOY_HOOK_API
   gh secret set RENDER_DEPLOY_HOOK_SPA
   ```
   (`gh secret set NOMBRE` sin `--body` te pide el valor de forma interactiva, sin dejarlo en el
   historial de la terminal). O por la web: **Settings → Secrets and variables → Actions → New
   repository secret**.
4. Hecho esto, cualquier push a `main` que pase `backend-build` y `spa-build` dispara el deploy
   automáticamente. Podés verlo en la pestaña **Actions** del repo, job "Desplegar a Render".

## 6. Para el aula virtual, el día de la entrega

- URL de la API: `https://smartroute-api.onrender.com`
- URL de la SPA: `https://smartroute-web.onrender.com`
- URL de Swagger UI: `https://smartroute-api.onrender.com/swagger-ui/index.html`
- Usuario de prueba USUARIO: `reachel.demo@ejemplo.com` / `Demo1234!`
- Usuario de prueba ADMIN: `kimberley.demo@ejemplo.com` / `Demo1234!`
