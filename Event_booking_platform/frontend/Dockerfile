FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Same-origin by default: nginx proxies /api to the backend. Override to call an API on another host.
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

FROM nginx:1.27-alpine
# The official image renders /etc/nginx/templates/*.template with the container's environment on start.
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
ENV API_UPSTREAM=http://api:8080
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
