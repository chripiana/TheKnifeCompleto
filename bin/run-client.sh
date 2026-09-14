#!/usr/bin/env bash
set -euo pipefail

# Usage:
# export JAVA_FX_LIB=/path/to/javafx-sdk-21/lib
# ./run-client.sh

if [ -z "${JAVA_FX_LIB:-}" ]; then
  echo "ERROR: Please set JAVA_FX_LIB to the 'lib' directory of your JavaFX SDK (e.g. /Users/you/Downloads/javafx-sdk-21/lib)"
  exit 1
fi

JAR=target/theknife-launcher-client.jar
if [ ! -f "$JAR" ]; then
  echo "ERROR: $JAR not found. Build the project first (mvn -DskipTests package)."
  exit 1
fi

java \
  --module-path "$JAVA_FX_LIB" \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.web \
  -jar "$JAR" "$@"
