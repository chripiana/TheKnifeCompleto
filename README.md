TheKnife
=========

Applicazione JavaFX per la gestione di ristoranti.

Requisiti
--------
- Java JDK 21 o superiore (nel sistema è presente Java 24, ma il progetto è configurato per Java 21)
- Apache Maven
- Windows (sono forniti gli script .cmd per l'avvio)

Installazione e configurazione
-----------------------------
1. Installare JDK 21 o superiore e verificare che sia disponibile da terminale.
2. Impostare la variabile d'ambiente JAVA_HOME al percorso del JDK, ad esempio:
   setx JAVA_HOME "C:\Program Files\Java\jdk-24"
3. Aggiornare il PATH aggiungendo:
   setx PATH "%PATH%;%JAVA_HOME%\bin"
4. Riavviare il terminale o aprire una nuova finestra.

Compilazione
------------
Dalla cartella del progetto eseguire:
- mvnw.cmd -DskipTests package

Questo comando genera gli artefatti nella cartella target.

Avvio
-----
Per avviare l'applicazione sono già disponibili i file nella cartella bin:
- run-server.cmd: avvia il server
- run-client.cmd: avvia il client JavaFX

In alternativa, è possibile avviare direttamente con Maven:
- mvnw.cmd -DskipTests javafx:run

Librerie esterne
---------------
La cartella lib contiene le librerie esterne richieste per l'esecuzione e la compilazione:
- JavaFX: javafx-controls, javafx-fxml, javafx-web, javafx-swing, javafx-media
- ControlsFX
- FormsFX
- ValidatorFX
- Ikonli
- BootstrapFX
- TilesFX
- FXGL
- PostgreSQL JDBC Driver

Nota importante
---------------
Le librerie presenti nella cartella lib sono quelle usate in modo non standard per il progetto e sono necessarie per eseguire l'applicazione con gli script .cmd forniti.
