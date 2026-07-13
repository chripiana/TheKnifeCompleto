TheKnife - Guida rapida
=======================

Struttura del progetto
- src/main/java: sorgenti Java del client, del server e del database layer
- src/main/resources: file FXML, CSS, CSV e script SQL
- bin: jar eseguibili prodotti dalla build
- doc: manuale utente, manuale tecnico, Javadoc e diagrammi

Prerequisiti
- JDK 21 o superiore (consigliato Java 21 o 24)
- Maven 3.8+ oppure Maven Wrapper incluso nel progetto (mvnw / mvnw.cmd)
- PostgreSQL in esecuzione, con un database raggiungibile da localhost
- Variabile d'ambiente JAVA_HOME impostata correttamente

Installazione e preparazione dell'ambiente
1. Installa JDK 21+ e assicurati che JAVA_HOME punti alla cartella del JDK.
2. Installa Maven oppure usa il wrapper del progetto.
3. Verifica che PostgreSQL sia attivo e che il database di destinazione esista oppure venga creato automaticamente dal server.
4. Se usi Windows, puoi eseguire i comandi Maven con mvnw.cmd; su Linux/macOS usare ./mvnw.

Compilazione
Eseguire i seguenti comandi dalla cartella del progetto:

- Test e verifica del progetto:
  mvn clean test
  oppure, se si usa il wrapper:
  mvnw.cmd clean test

- Build completa e produzione dei jar:
  mvn clean package
  oppure:
  mvnw.cmd clean package

- Generazione della documentazione Javadoc:
  mvn javadoc:javadoc
  oppure:
  mvnw.cmd javadoc:javadoc

Artefatti prodotti
- bin/clientTK.jar: eseguibile del client JavaFX
- bin/serverTK.jar: eseguibile del server
- doc/javadoc: documentazione Javadoc generata

Eseguire il client
Il client usa JavaFX e richiede il percorso delle librerie JavaFX in fase di esecuzione:

java --module-path <path-to-javafx-libs> --add-modules javafx.controls,javafx.fxml,javafx.web -jar bin/clientTK.jar

Esempio su Windows:
java --module-path C:\javafx-sdk-21\lib --add-modules javafx.controls,javafx.fxml,javafx.web -jar bin/clientTK.jar

Eseguire il server
Il server inizializza automaticamente il database e importa i dati iniziali se necessario:

java -jar bin/serverTK.jar --host localhost --port 5432 --db theknife --user postgres --password password

Librerie particolari e uso non standard
- Il progetto usa JavaFX 21 tramite le dipendenze Maven org.openjfx; il suo avvio non è standard perché richiede il parametro --module-path e l'aggiunta esplicita dei moduli JavaFX.
- Sono inoltre usate librerie UI esterne come controlsfx, formsfx, validatorfx, tilesfx, fxgl, bootstrapfx e ikonli: queste non fanno parte del JDK e vengono risolte tramite Maven durante la build.
- Il backend usa PostgreSQL JDBC tramite org.postgresql e il server può creare/inizializzare lo schema del database automaticamente.

Note finali
- Per un'avvio locale completo, assicurarsi che PostgreSQL sia raggiungibile e che le credenziali siano corrette.
- Se il database non esiste, il server provvede a inizializzarlo e a caricare i dati iniziali dalle risorse interne.
