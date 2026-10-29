#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAIN_SOURCE_DIR="$PROJECT_ROOT/src"
TEST_SOURCE_DIR="$PROJECT_ROOT/tests"
CLASS_DIR="$PROJECT_ROOT/out/classes"
SOURCE_LIST="$PROJECT_ROOT/out/sources.txt"

INCLUDE_TESTS=false

if [[ "${1:-}" == "--tests" ]]; then
    INCLUDE_TESTS=true
elif [[ -n "${1:-}" ]]; then
    echo "Usage: ./compile.sh [--tests]" >&2
    exit 2
fi

mkdir -p "$CLASS_DIR"
find "$CLASS_DIR" -type f -name '*.class' -delete

if [[ "$INCLUDE_TESTS" == true ]]; then
    find "$MAIN_SOURCE_DIR" "$TEST_SOURCE_DIR" -type f -name '*.java' | sort > "$SOURCE_LIST"
else
    find "$MAIN_SOURCE_DIR" -type f -name '*.java' | sort > "$SOURCE_LIST"
fi

if [[ ! -s "$SOURCE_LIST" ]]; then
    echo "No Java source files were found under src/." >&2
    exit 1
fi

javac --release 17 -d "$CLASS_DIR" @"$SOURCE_LIST"

if [[ "$INCLUDE_TESTS" == true ]]; then
    echo "Compiled production and test sources to $CLASS_DIR"
else
    echo "Compiled production sources to $CLASS_DIR"
fi
