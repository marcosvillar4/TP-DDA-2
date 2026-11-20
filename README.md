# LogiRed — TP Desarrollo de Aplicaciones II

Monorepo: `DA2Back` (Spring Boot 4 / Java 17), `DA2Front` (React + Vite), `docs/`.

## Levantar todo (un solo comando)

Requisitos: Docker Engine 24+ con Compose v2 (`docker compose version`). No hace falta Java, Node ni MySQL.

```bash
git clone <url-del-repo> && cd <repo>
docker compose up --build
```

Primer arranque: ~5–10 min (descarga de imágenes y compilación). Cuando termina:

| Servicio | URL | Notas |
|---|---|---|
| Frontend | http://localhost:3000 | React servido por nginx |
| API REST | http://localhost:8081 | Spring Boot |
| Consola ActiveMQ | http://localhost:8161 | `admin` / `admin`, solo desde esta máquina |
| MySQL | `127.0.0.1:3307` | usuario/clave en `.env` (o valores demo) |

**Usuarios de demo** (perfil `dev`, clave `admin123`): `admin@da2back.com` (ADMIN) y `urbanshoes@da2back.com` (COMERCIO), entre otros cargados por el seed.

Componentes: MySQL 8.4 (volumen persistente), ActiveMQ Classic **en contenedor propio** y persistente (el broker embebido de la app queda desactivado), backend y frontend.

> El proyecto no tiene servicios SOAP externos ni mocks: si se agregan, deben sumarse como servicios en `docker-compose.yml` para cumplir G1.

### Configuración opcional

Copiar `.env.example` a `.env` (ignorado por Git) para cambiar claves, puertos, `MAPAS_PROVEEDOR` (`local` por defecto, sin red; `osm` usa OpenStreetMap) o el correo SMTP. **No commitear secretos.**

### Comandos útiles

```bash
docker compose logs -f backend      # logs
docker compose stop activemq        # simular caída del broker
docker compose start activemq       # recuperarlo
docker compose down                 # bajar (conserva datos)
docker compose down -v              # bajar y borrar datos
```

## Pruebas y CI

El pipeline (`.github/workflows/ci.yml`) corre en cada push a `main`, tag `v*` y PR:

| Job | Qué verifica |
|---|---|
| `backend` | compilación, todos los tests, cobertura JaCoCo (negocio ≥80% líneas / ≥70% ramas), SonarCloud + Quality Gate |
| `flaky-check` | la suite 3 veces: resultados idénticos y sin fallos |
| `mutation` | PIT sobre el paquete de negocio (umbral 60%; bloqueante con la variable `PIT_ENFORCE=true`) |
| `frontend` | `npm run lint`, `npm test`, `npm run build` |
| `secrets-scan` | gitleaks sobre todo el historial (G6) |
| `dependency-scan` | Trivy: 0 CVE críticas/altas sin mitigación (`.trivyignore`) |
| `docker-smoke` | `docker compose up` desde cero ≤15 min, login, headers de seguridad, caída y recuperación del broker; Newman y k6 si existen `postman/*.json` y `perf/load.js` |

Configurar en GitHub: secret `SONAR_TOKEN`; variables `SONAR_ORGANIZATION` y `SONAR_PROJECT_KEY`.

Local, sin Docker:

```bash
cd DA2Back && ./mvnw verify                   # tests + cobertura + gate
cd DA2Front && npm ci && npm run lint && npm test && npm run build
```
