# Librerie esterne

Questa directory contiene le librerie Java esterne necessarie all'esecuzione del progetto quando sono disponibili nel repository Maven locale.

La gestione ufficiale delle dipendenze resta affidata al file `pom.xml`. Per rigenerare automaticamente questa directory, dalla cartella `PROGETTO` eseguire:

```bash
mvn dependency:copy-dependencies -DoutputDirectory=lib -DincludeScope=runtime -DincludeTypes=jar
```

Le librerie JavaFX possono richiedere il parametro `--module-path` durante l'avvio del client. Le dipendenze usate esclusivamente dai test non vengono incluse in questa directory.
