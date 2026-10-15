#!/usr/bin/env bash
# ==============================================================================
# Complete Docker Container Lifecycle Management Script
# ==============================================================================
set -euo pipefail

IMAGE_NAME="fitnessequipment/portal"
IMAGE_TAG="1.0.0"
CONTAINER_NAME="fitness-portal-instance"
HOST_PORT="8080"

echo "=== 1. Building Docker Image [${IMAGE_NAME}:${IMAGE_TAG}] ==="
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" -t "${IMAGE_NAME}:latest" .

echo "=== 2. Inspecting Built Image Layers & Size ==="
docker images "${IMAGE_NAME}"

echo "=== 3. Cleaning Up Any Existing Container ==="
docker stop "${CONTAINER_NAME}" 2>/dev/null || true
docker rm "${CONTAINER_NAME}" 2>/dev/null || true

echo "=== 4. Running New Docker Container with Port Mapping ${HOST_PORT}:8080 ==="
docker run -d \
  --name "${CONTAINER_NAME}" \
  -p "${HOST_PORT}:8080" \
  -e PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --restart always \
  "${IMAGE_NAME}:${IMAGE_TAG}"

echo "=== 5. Inspecting Container Logs ==="
sleep 5
docker logs --tail 30 "${CONTAINER_NAME}"

echo "=== 6. Validating Container Health Check ==="
for i in {1..10}; do
    HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' "${CONTAINER_NAME}" 2>/dev/null || echo '"starting"')
    echo "Container Health Status: ${HEALTH}"
    if [ "${HEALTH}" == '"healthy"' ]; then
        echo "Container is HEALTHY and ready!"
        break
    fi
    sleep 3
done

echo "=== 7. Querying Live Application Endpoint ==="
curl -i "http://localhost:${HOST_PORT}/api/dashboard/health" || true

echo "=== Container Lifecycle Test Complete ==="
