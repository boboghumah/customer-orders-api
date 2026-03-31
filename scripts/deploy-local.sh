#!/usr/bin/env bash

set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-customer-orders-api}"
IMAGE_TAG="${IMAGE_TAG:-local}"
CONTAINER_NAME="${CONTAINER_NAME:-customer-orders-api}"
HOST_PORT="${HOST_PORT:-8080}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"
HEALTH_URL="${HEALTH_URL:-http://localhost:${HOST_PORT}/actuator/health}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-90}"
SLEEP_SECONDS="${SLEEP_SECONDS:-3}"
STARTUP_GRACE_SECONDS="${STARTUP_GRACE_SECONDS:-10}"

FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"

echo "Deploying ${FULL_IMAGE_NAME} to container ${CONTAINER_NAME}..."

if docker ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  echo "Stopping existing container ${CONTAINER_NAME}..."
  docker rm -f "${CONTAINER_NAME}"
fi

echo "Starting new container ${CONTAINER_NAME} from ${FULL_IMAGE_NAME}..."
docker run -d \
  --name "${CONTAINER_NAME}" \
  -p "${HOST_PORT}:${CONTAINER_PORT}" \
  "${FULL_IMAGE_NAME}"

echo "Giving the application ${STARTUP_GRACE_SECONDS}s to start before health checks..."
sleep "${STARTUP_GRACE_SECONDS}"

echo "Waiting for health endpoint ${HEALTH_URL}..."
attempt=1
until curl --silent --fail "${HEALTH_URL}" >/dev/null; do
  if [ "${attempt}" -ge "${MAX_ATTEMPTS}" ]; then
    echo "Deployment failed: health check did not pass after ${MAX_ATTEMPTS} attempts."
    docker logs "${CONTAINER_NAME}" || true
    exit 1
  fi

  echo "Health check not ready yet (attempt ${attempt}/${MAX_ATTEMPTS}). Retrying in ${SLEEP_SECONDS}s..."
  attempt=$((attempt + 1))
  sleep "${SLEEP_SECONDS}"
done

echo "Deployment successful. ${CONTAINER_NAME} is healthy."
