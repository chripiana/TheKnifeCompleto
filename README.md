THEKNIFE - ISTRUZIONI DI INSTALLAZIONE E ESECUZIONE

1) Posizione dei file eseguibili
- I jar eseguibili forniti nel repository sono:
  - src/main/java/db/bin/bin/theknife-server.jar
  - src/main/java/db/bin/bin/theknife-client.jar
  Per comodità puoi copiare questi jar nella cartella `bin/` (già presente).

2) Build con Maven
- Compilare e pacchettizzare il progetto:
  mvn clean package

- Per copiare tutte le dipendenze nella cartella `lib/`:
  mvn dependency:copy-dependencies -DoutputDirectory=lib

- Nota: se vuoi creare un "fat-jar" (tutte le dipendenze in un singolo jar), puoi usare il plugin Shade:
  mvn clean package
  (configura il plugin `maven-shade-plugin` nel `pom.xml` se necessario)

3) Esecuzione
- Script Windows (già presenti in `bin/`):
  bin\run-server.cmd
  bin\run-client.cmd

- Script Unix (già presenti in `bin/`):
  sh bin/run-server.sh
  sh bin/run-client.sh

- Esempio di esecuzione diretto (se usi JavaFX SDK esterno):
  java --module-path lib --add-modules javafx.controls,javafx.fxml -jar bin/theknife-client.jar

4) Librerie non standard / note su JavaFX
- Il progetto utilizza JavaFX (moduli jar) forniti localmente in `src/main/java/db/bin/bin/lib/` nel repository.
  Se il tuo JDK non include JavaFX, scarica JavaFX SDK (versione 21 consigliata) da https://openjfx.io/ e copia i jar in `lib/` oppure usa `mvn javafx:run` con la corretta configurazione.

- Librerie esterne usate (possono trovarsi nella cartella di origine `src/main/java/db/bin/bin/lib/`):
  - javafx-base, javafx-controls, javafx-fxml, javafx-graphics, javafx-media, javafx-swing, javafx-web
  - controlsfx (controlsfx-11.2.1.jar)
  - tilesfx (tilesfx-21.0.3.jar)
  - validatorfx (validatorfx-0.5.0.jar)
  - ikonli-javafx (ikonli-javafx-12.3.1.jar)
  - fxgl (fxgl-17.3.jar)
  - formsfx (formsfx-core-11.6.0.jar)
  - bootstrapfx (bootstrapfx-core-0.4.0.jar)

5) Suggerimenti
- Se vuoi che `bin/` contenga effettivamente i jar, copia i file da `src/main/java/db/bin/bin/` in `bin/`:
  copy "src\\main\\java\\db\\bin\\bin\\theknife-server.jar" "bin\\"
  copy "src\\main\\java\\db\\bin\\bin\\theknife-client.jar" "bin\\"

- Se riscontri errori legati a JavaFX, assicurati che la versione di Java usata sia compatibile con JavaFX 21 (JDK 17+ consigliato).

Per assistenza ulteriori, dimmi quale piattaforma usi (Windows/Linux) e se vuoi che copi i jar in `bin/` automaticamente.
