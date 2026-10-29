#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_SOURCE_DIR="$PROJECT_ROOT/tests"
CLASS_DIR="$PROJECT_ROOT/out/classes"

if [[ $# -gt 1 ]]; then
    echo "Usage: ./test.sh [fully.qualified.TestClass]" >&2
    exit 2
fi

if [[ $# -eq 1 && ! -f "$TEST_SOURCE_DIR/${1//.//}.java" ]]; then
    echo "Unknown test class: $1" >&2
    exit 2
fi

test_sources="$(find "$TEST_SOURCE_DIR" -type f -name '*Test.java' | sort)"
if [[ -z "$test_sources" ]]; then
    echo "No test classes found under $TEST_SOURCE_DIR" >&2
    exit 1
fi

cd "$PROJECT_ROOT"
"$PROJECT_ROOT/compile.sh" --tests

if [[ $# -eq 1 ]]; then
    exec java -ea -cp "$CLASS_DIR" "$1"
fi

while IFS= read -r source; do
    test_class="${source#"$TEST_SOURCE_DIR/"}"
    test_class="${test_class%.java}"
    test_class="${test_class//\//.}"
    java -ea -cp "$CLASS_DIR" "$test_class"
done <<< "$test_sources"
