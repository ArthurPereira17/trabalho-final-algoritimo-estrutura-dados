#!/bin/bash
# run.sh - roda o backend (compila antes, se necessario)
# Uso:
#   ./run.sh                          # sobe o servidor
#   ./run.sh --indexar "/caminho/pasta"  # indexa e sobe
# Funciona de qualquer pasta

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

find_java_bin() {
    if command -v java &> /dev/null; then
        dirname "$(command -v java)"
        return 0
    fi
    
    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
        echo "$JAVA_HOME/bin"
        return 0
    fi
    
    for local in /usr/lib/jvm/java-21-openjdk-*/bin \
                 /usr/lib/jvm/java-17-openjdk-*/bin \
                 /usr/lib/jvm/java-11-openjdk-*/bin; do
        if [ -f "$local/java" ]; then
            echo "$local"
            return 0
        fi
    done
    
    echo "❌ JDK (java) nao encontrado" >&2
    exit 1
}

BIN_DIR="$ROOT/bin"
APP_CLASS="$BIN_DIR/App.class"

# Compila se necessário
if [ ! -f "$APP_CLASS" ]; then
    echo "📦 bin ausente - compilando primeiro..."
    "$ROOT/build.sh"
fi

JAVA_BIN=$(find_java_bin)
echo "🚀 Usando JDK: $JAVA_BIN"

# Vai para backend/ para que o indice (pasta dados/) fique aqui
cd "$ROOT"

# Passa os argumentos para o App
"$JAVA_BIN/java" -cp "$BIN_DIR" App "$@"