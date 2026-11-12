#!/bin/sh
# Tạo bucket private + tài khoản riêng cho ứng dụng (backend không dùng root)
set -eu

# Tên bucket giống hệt ở application.properties, proxy.conf.json, nginx template — đừng đổi
BUCKET=asset-images
POLICY=asset-app-policy

mc alias set asset http://minio:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"

# 1. Bucket (đã có thì bỏ qua)
mc mb --ignore-existing "asset/$BUCKET"

# 2. Private — mọi lượt đọc phải qua URL ký
mc anonymous set none "asset/$BUCKET"

# 3. Policy: chỉ đọc / ghi / xóa trong đúng bucket này
cat > /tmp/policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    { "Effect": "Allow", "Action": ["s3:GetBucketLocation", "s3:ListBucket"], "Resource": ["arn:aws:s3:::$BUCKET"] },
    { "Effect": "Allow", "Action": ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"], "Resource": ["arn:aws:s3:::$BUCKET/*"] }
  ]
}
EOF
mc admin policy create asset "$POLICY" /tmp/policy.json

# 4. Tài khoản ứng dụng (có rồi thì chỉ cập nhật mật khẩu) + gắn policy
mc admin user add asset "$MINIO_APP_USER" "$MINIO_APP_PASSWORD"
mc admin policy attach asset "$POLICY" --user "$MINIO_APP_USER" || echo "Policy already exists - skipping"

echo "MinIO is ready: bucket $BUCKET (private), user $MINIO_APP_USER"