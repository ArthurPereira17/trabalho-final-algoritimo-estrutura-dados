#!/bin/bash
# test.sh - roda os testes JUnit

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "🧪 Compilando antes dos testes..."
"$ROOT/build.sh"

JUNIT="$ROOT/lib/junit-platform-console-standalone-6.0.0-RC2.jar"
BIN="$ROOT/bin"

echo "🧪 Executando testes..."
java -jar "$JUNIT" execute -cp "$BIN" --scan-class-path