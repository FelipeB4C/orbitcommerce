#!/bin/bash
# Automatically executed by the official Postgres image on the first
# initialization of the volume (docker-entrypoint-initdb.d).
# Creates one database per service (Database per Service pattern).
set -e

DATABASES="identity_db catalog_db order_db inventory_db payment_db shipping_db notification_db"

for DB in $DATABASES; do
  echo "Creating database: $DB"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE $DB;
    GRANT ALL PRIVILEGES ON DATABASE $DB TO $POSTGRES_USER;
EOSQL
done

echo "All OrbitCommerce databases have been successfully created."
