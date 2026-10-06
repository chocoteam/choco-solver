#!/bin/bash
#
# Build the two MiniZinc Challenge Docker images:
#   - chocoteam/choco-solver-mzn:<version>     (standard)
#   - chocoteam/choco-solver-mzn:<version>-X   (with LCG enabled)
#
# Usage: ./build-images.sh <version>
# Example: ./build-images.sh 6.0.2

set -euo pipefail

VERSION="${1:?Usage: $0 <version>}"
IMAGE="chocoteam/choco-solver-mzn"
CONTEXT="$(git rev-parse --show-toplevel)"
DOCKERFILE="parsers/src/main/minizinc/docker/Dockerfile.dms"

echo "=== Building ${IMAGE}:${VERSION} (standard) ==="
docker build --platform linux/amd64 -t "${IMAGE}:${VERSION}" \
  -f "${CONTEXT}/${DOCKERFILE}" \
  "${CONTEXT}"

echo ""
echo "=== Building ${IMAGE}:${VERSION}-X (with LCG) ==="
docker build --platform linux/amd64 -t "${IMAGE}:${VERSION}-X" \
  --build-arg EXTRA_FZN_ARGS="-lcg" \
  -f "${CONTEXT}/${DOCKERFILE}" \
  "${CONTEXT}"

echo ""
echo "Done. Images built:"
docker images "${IMAGE}" --format "  {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "${VERSION}"