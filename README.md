TheKnife — Istruzioni di installazione e compilazione
=======================================================

REQUISITI
---------
- JDK 21 (o superiore)
- Apache Maven 3.9+
- PostgreSQL in esecuzione e raggiungibile dalla macchina su cui gira serverTK

STRUTTURA DEL PROGETTO
-----------------------
L'applicazione è divisa in due moduli eseguibili distinti:

- serverTK  (classe main: db.net.ServerTK)
  Si interfaccia direttamente con PostgreSQL via JDBC. All'avvio chiede da
  console host/utente/password del database e la porta TCP su cui restare
  in ascolto. Gestisce più client contemporaneamente: ogni connessione
  viene affidata a un thread dedicato con una propria Connection JDBC.

- clientTK  (classe main: project.demo.TheKnifeApp)
  Applicazione grafica JavaFX. Non si connette più direttamente al
  database: dialoga con serverTK tramite un semplice protocollo a oggetti
  serializzati su socket TCP (pacchetto db.net: Richiesta / Risposta).

COMPILAZIONE (Maven)
---------------------
Dalla cartella radice del progetto (dove si trova questo pom.xml):

    mvn clean package

Il comando compila tutto il codice ed esegue anche l'assemblaggio dei due
jar eseguibili tramite maven-shade-plugin. Al termine, dentro target/
troverai:

    demo-1.0-SNAPSHOT-server.jar   -> jar eseguibile del server (ServerTK)
    demo-1.0-SNAPSHOT-client.jar   -> jar eseguibile del client (TheKnifeApp)

Copiali (o rinominali) nella cartella bin/ del repository come:

    bin/theknife-server.jar
    bin/theknife-client.jar

ESECUZIONE
----------
1. Avvia per primo il server:

       java -jar bin/theknife-server.jar

   Ti verranno chiesti in sequenza:
     - host del database PostgreSQL (invio per "localhost")
     - utente PostgreSQL (invio per "postgres")
     - password PostgreSQL
     - porta di ascolto del server TheKnife (invio per "5000")

   Alla prima esecuzione il server crea automaticamente il database
   "theknife", le tabelle (script theknife_create_db.sql) e importa i
   ristoranti da ristoranti_clean.csv.

2. Avvia uno o più client (anche da macchine diverse sulla stessa rete):

       java -jar bin/theknife-client.jar

   Per default il client cerca il server su "localhost:5000". Per
   connettersi a un server remoto, avvia il client specificando le
   system property tk.server.host e tk.server.port, es.:

       java -Dtk.server.host=192.168.1.50 -Dtk.server.port=5000 -jar bin/theknife-client.jar

AVVIO ALTERNATIVO IN FASE DI SVILUPPO
--------------------------------------
Per lo sviluppo del solo client con JavaFX gestito automaticamente da
Maven (senza dover impacchettare i moduli JavaFX a mano):

    mvn clean javafx:run

Il server, non avendo dipendenze JavaFX, si può sempre lanciare anche
direttamente con:

    mvn clean compile exec:java -Dexec.mainClass=db.net.ServerTK

(se si aggiunge exec-maven-plugin) oppure più semplicemente eseguendo il
jar prodotto da "mvn package" come descritto sopra.

NOTA SUL JAR CLIENT E JAVAFX
------------------------------
Il jar client prodotto da maven-shade-plugin include tutte le dipendenze
JavaFX risolte per il sistema operativo sul quale viene eseguito "mvn
package". Se il jar viene eseguito su un sistema operativo diverso da
quello di build, JavaFX potrebbe non trovare le librerie native corrette.
In tal caso, per un client garantito multipiattaforma, generare
un'immagine runtime autonoma con:

    mvn clean javafx:jlink

che produce in target/app un eseguibile nativo autosufficiente (non
richiede una JVM separata installata sulla macchina di destinazione).

LIBRERIE NON STANDARD
-----------------------
Tutte le librerie utilizzate (JavaFX, ControlsFX, FormsFX, ValidatorFX,
Ikonli, BootstrapFX, TilesFX, FXGL, driver JDBC PostgreSQL) sono
dipendenze standard risolte automaticamente da Maven Central tramite il
pom.xml incluso: non è necessario scaricare o collocare manualmente
alcuna libreria nella cartella lib/, che pertanto non è presente in
questo repository.

GESTIONE DELLA CONCORRENZA
-----------------------------
serverTK usa un ExecutorService (thread pool) in db.net.ServerTK: ogni
connessione client accettata viene gestita da db.net.ClientHandler su un
thread indipendente, con una propria Connection JDBC (db.DatabaseManager
.newConnection()), così più utenti possono operare in parallelo sulla
piattaforma senza bloccarsi a vicenda.
