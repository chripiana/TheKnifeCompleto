# TheKnife

TheKnife è un'applicazione desktop sviluppata in JavaFX per la ricerca e la gestione di ristoranti. Il progetto supporta clienti e ristoratori attraverso un'architettura client-server con database PostgreSQL.

## Funzionalità principali

### Utente cliente

- registrazione e accesso;
- ricerca e filtraggio dei ristoranti;
- visualizzazione dei dettagli dei locali;
- gestione dei preferiti;
- pubblicazione di recensioni e valutazioni;
- prenotazione di tavoli;
- gestione del profilo personale;
- consultazione di prenotazioni, recensioni e preferiti.

### Utente ristoratore

- accesso alla dashboard personale;
- creazione e modifica dei propri ristoranti;
- consultazione delle recensioni ricevute;
- gestione del profilo e dei dati dei locali.

## Tecnologie utilizzate

- Java 21;
- JavaFX 21;
- Maven;
- PostgreSQL;
- PostgreSQL JDBC Driver;
- HikariCP per il pool di connessioni;
- JUnit 5 per i test;
- SLF4J per il logging.

## Requisiti

Per compilare ed eseguire il progetto sono necessari:

- JDK 21 o superiore;
- Maven 3.9 o superiore;
- PostgreSQL in esecuzione;
- variabile d'ambiente `JAVA_HOME` configurata;
- librerie JavaFX disponibili per l'esecuzione del client.

## Struttura del progetto

```text
PROGETTO/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── db/                  # Gestione database
    │   │   ├── project/             # Client, server, controller e modelli condivisi
    │   │   └── theknife/             # Classi di avvio
    │   └── resources/
    │       ├── project/controllers/ # File FXML e CSS
    │       ├── ristoranti_clean.csv  # Dataset iniziale
    │       └── theknife_create_db.sql
    └── test/java/                   # Test automatici
```

## Compilazione

Aprire un terminale nella cartella `PROGETTO` ed eseguire:

```bash
mvn clean compile
```

Per eseguire anche i test:

```bash
mvn clean test
```

Per creare i pacchetti compilati:

```bash
mvn clean package
```

## Avvio dell'applicazione

### Server

Il server può essere avviato dall'IDE tramite la classe:

```text
theknife.ServerMain
```

È disponibile anche l'entry point `theknife.NonGuiServerMain` per l'avvio senza interfaccia grafica.

Il server richiede una connessione a PostgreSQL. Parametri tipici di configurazione:

```text
host: localhost
port: 5432
database: theknife
user: postgres
password: password
```

Le credenziali devono essere adattate alla configurazione locale di PostgreSQL.

### Client JavaFX

Dopo aver avviato il server, il client può essere eseguito dall'IDE tramite la classe:

```text
theknife.ClientMain
```

In alternativa, tramite il plugin Maven JavaFX:

```bash
mvn javafx:run
```

Quando si utilizza un jar direttamente, è necessario specificare il percorso delle librerie JavaFX:

```bash
java --module-path <percorso-javafx>/lib --add-modules javafx.controls,javafx.fxml,javafx.web -jar <client-jar>
```

## Dataset di test

Il dataset iniziale si trova nel file `src/main/resources/ristoranti_clean.csv`. Ogni riga contiene le informazioni principali di un ristorante, tra cui identificativo, nome, Stato, città, indirizzo, coordinate, fascia di prezzo, disponibilità alla prenotazione e tipologia di cucina.

Il dataset viene utilizzato per verificare ricerca, filtraggio, ordinamento e visualizzazione dei dettagli dei locali. Il server lo importa durante l'inizializzazione del database quando i dati iniziali non sono ancora presenti.

## Validazione e sicurezza

Il sistema applica controlli lato client e lato server. Tra le principali regole:

- password di almeno 7 caratteri;
- almeno una lettera maiuscola, una minuscola e un numero nella password;
- data di nascita precedente alla data corrente;
- verifica dei campi obbligatori;
- controllo della sessione utente;
- validazione delle richieste inviate al server.

## Limiti della soluzione

- il dataset è statico e non rappresenta necessariamente dati aggiornati;
- l'applicazione richiede un server e un database PostgreSQL configurati correttamente;
- l'interfaccia è pensata per desktop JavaFX e non include versioni web o mobile;
- non sono presenti mappe interattive, pagamenti online o notifiche email/push;
- la scalabilità per numerosi utenti simultanei non è stata verificata in produzione;
- i test automatici non coprono tutti i flussi client-server e le interazioni grafiche.

## Documentazione aggiuntiva

- [Documentazione tecnica]
- [Documentazione utente]

La documentazione utente contiene anche la sitografia e la bibliografia utilizzate per il progetto.

## Autori

Consultare il file `autori.txt` per l'elenco degli autori del progetto.
