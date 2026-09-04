#!/bin/sh
DIR=$(cd "$(dirname "$0")" && pwd)
cd "$DIR"
if [ -d "/opt/javafx-sdk-21/lib" ]; then
  java --module-path "/opt/javafx-sdk-21/lib" --add-modules=javafx.controls,javafx.fxml -jar ClientTK.jar
else
  java -jar ClientTK.jar
fi
