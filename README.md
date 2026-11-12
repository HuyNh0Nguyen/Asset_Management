# AssetProject — Hệ thống Quản lý Tài sản

## Chạy local (hạ tầng trong Docker, ứng dụng chạy bằng Maven / npm)
```bash
docker compose -f infra/local/compose.yaml up -d --build   # lần đầu dựng image MinIO mất vài phút
cd backend && ./mvnw spring-boot:run                        # :8080
cd email-service && ./mvnw spring-boot:run
cd frontend && npm ci && npm start                          # http://localhost:4200
```

## Chạy dev (toàn bộ trong Docker)
```bash
cp infra/dev/.env.example infra/dev/.env   # điền bí mật — một lần
cd infra/dev && docker compose up -d --build   # http://localhost:8000
```

## Cập nhật hợp đồng API
```bash
./scripts/export-openapi.sh          # backend local phải đang chạy
cd frontend && npm run gen:api
```