# syntax=docker/dockerfile:1
FROM node:26-alpine AS build
WORKDIR /app

# VITE_API_URL é embutida no bundle em tempo de build. O padrão aponta pro
# backend publicado em localhost:8080 (visto do navegador, não de dentro
# da rede do Docker) — funciona out-of-the-box com o docker-compose local.
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=${VITE_API_URL}

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
