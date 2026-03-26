#!/bin/sh
set -e

mc alias set myminio $S3_INTERNAL_URL "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" --insecure 2>/dev/null;

# Создание бакета, если не существует
if ! mc ls myminio/"$S3_BUCKET" --insecure >/dev/null 2>&1; then
  echo "Creating bucket: $S3_BUCKET"
  mc mb myminio/"$S3_BUCKET" --insecure
fi

# Создание пользователя, если не существует
if ! mc admin user list myminio --insecure | grep -q "$S3_ACCESS_KEY"; then
  echo "Creating user: $S3_ACCESS_KEY"
  mc admin user add myminio "$S3_ACCESS_KEY" "$S3_SECRET_KEY" --insecure
fi

# Назначение политики пользователю
mc admin policy attach myminio readwrite --user "$S3_ACCESS_KEY" --insecure

echo "MinIO initialization completed."