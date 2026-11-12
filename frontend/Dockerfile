# syntax=docker/dockerfile:1
FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
# Bản production nhúng font Google vào CSS — máy build cần mạng
RUN npm run build

FROM nginx:1.29-alpine
# Image nginx tự chạy envsubst trên /etc/nginx/templates/*.template lúc khởi động
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80