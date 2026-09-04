@echo off
IF EXIST "C:\javafx-sdk-21\lib" (
  java --module-path "C:\javafx-sdk-21\lib" --add-modules=javafx.controls,javafx.fxml -jar "%~dp0ClientTK.jar"
) ELSE (
  java -jar "%~dp0ClientTK.jar"
)
