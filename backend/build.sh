#!/bin/bash
# build.sh - compila o backend (src + test) para a pasta bin
# Detecta o JDK automaticamente no Linux

set -e  # para no primeiro erro

ROOT="$(cd "$(dirname "$0")" && pwd)"

find_jdk_bin() {
    # Tenta achar javac no PATH
    if command -v javac &> /dev/null; then
        dirname "$(command -v javac)"
        return 0
    fi
    
    # Tenta JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/javac" ]; then
        echo "$JAVA_HOME/bin"
        return 0
    fi
    
    # Locais comuns no Linux
    for local in /usr/lib/jvm/java-21-openjdk-*/bin \
                 /usr/lib/jvm/java-17-openjdk-*/bin \
                 /usr/lib/jvm/java-11-openjdk-*/bin; do
        if [ -f "$local/javac" ]; then
            echo "$local"
            return 0
        fi
    done
    
    echo "❌ JDK (javac) nao encontrado. Instale: sudo apt install openjdk-21-jdk" >&2
    exit 1
}

JDK_BIN=$(find_jdk_bin)
echo "📦 Usando JDK: $JDK_BIN"

JUNIT="$ROOT/lib/junit-platform-console-standalone-6.0.0-RC2.jar"
BIN="$ROOT/bin"

# Coleta todos os .java (src e test)
FONTES=$(find "$ROOT/src" "$ROOT/test" -name "*.java" 2>/dev/null || true)
QTD=$(echo "$FONTES" | wc -w)

echo "🔨 Compilando $QTD arquivos .java..."
mkdir -p "$BIN"

# Compila
"$JDK_BIN/javac" -encoding UTF-8 -cp "$JUNIT" -d "$BIN" $FONTES

if [ $? -eq 0 ]; then
    echo "✅ BUILD OK -> $BIN"
else
    echo "❌ BUILD FALHOU"
    exit 1
fi