#!/bin/bash
set -e

run() {
  rm -f ./db/explorer-db.sqlt
  sqlite3 ./db/explorer-db.sqlt < ./resources/sql/schema.sql
}

$*
