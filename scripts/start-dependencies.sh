#!/usr/bin/env bash

set -e

CURRENT_DIR=$(pwd)
echo "CURRENT_DIR=$CURRENT_DIR"

docker stop minio localstack
docker rm minio localstack
docker-compose -f ./docker-compose.yml up -d minio localstack

sleep 40

echo -e "Docker ps."
docker ps

sbt it:test
