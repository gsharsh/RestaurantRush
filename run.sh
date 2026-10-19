#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAIN_CLASS="restaurantrush.app.RestaurantRushApp"

"$PROJECT_ROOT/compile.sh"

exec java -cp "$PROJECT_ROOT/out/classes" "$MAIN_CLASS" "$@"
