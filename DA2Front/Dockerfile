# syntax=docker/dockerfile:1.7
# ---------------------------------------------------------------------------
# DA2Front - build de Vite + nginx sin privilegios (puerto 8080)
# VITE_API_BASE_URL se "hornea" en el bundle: es la URL del API vista DESDE EL NAVEGADOR.
# API_ORIGIN alimenta la directiva connect-src del CSP (mismo valor, sin path).
# ---------------------------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ARG VITE_API_BASE_URL=http://localhost:8081
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime
ENV API_ORIGIN=http://localhost:8081
# La imagen renderiza /etc/nginx/templates/*.template con envsubst al arrancar.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=6 \
    CMD wget -qO- http://127.0.0.1:8080/ >/dev/null || exit 1
