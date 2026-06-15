#!/usr/bin/env bash
#
# BANTADS — build & run (RNF do enunciado).
#
# Compila as imagens de todos os microsserviços (cada Dockerfile faz o
# `package` do jar internamente, multi-stage) + gateway/UI e sobe o ambiente
# completo via Docker Compose. Reset de volumes é OBRIGATÓRIO entre execuções
# (args de DLX nas filas + bancos CQRS exigem volumes limpos).
#
# Uso:
#   ./build.sh            # down -v + build + up -d
#   ./build.sh --logs     # idem, seguindo os logs ao final
#   ./build.sh --no-cache # build sem cache do Docker
#
set -euo pipefail

COMPOSE_FILE="docker-compose-bantads.yaml"
cd "$(dirname "$0")"

BUILD_ARGS=()
FOLLOW_LOGS=0
for arg in "$@"; do
  case "$arg" in
    --no-cache) BUILD_ARGS+=(--no-cache) ;;
    --logs)     FOLLOW_LOGS=1 ;;
    *) echo "Argumento desconhecido: $arg" >&2; exit 1 ;;
  esac
done

echo "==> [1/3] Derrubando ambiente e limpando volumes (down -v)..."
docker compose -f "$COMPOSE_FILE" down -v

echo "==> [2/3] Construindo imagens (jars via Dockerfile multi-stage)..."
docker compose -f "$COMPOSE_FILE" build "${BUILD_ARGS[@]}"

echo "==> [3/3] Subindo o ambiente (up -d)..."
docker compose -f "$COMPOSE_FILE" up -d

echo ""
echo "==> BANTADS no ar:"
echo "    API Gateway  : http://localhost:8000   (Swagger: /api-docs)"
echo "    RabbitMQ     : http://localhost:15672"
echo "    Front Angular: http://localhost:4200"
echo ""
echo "    Inicialize a base:  curl http://localhost:8000/reboot"
echo "    Login (senha tads): cli1@bantads.com.br | ger1@bantads.com.br | adm1@bantads.com.br"

if [ "$FOLLOW_LOGS" -eq 1 ]; then
  docker compose -f "$COMPOSE_FILE" logs -f
fi
