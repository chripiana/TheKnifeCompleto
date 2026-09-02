# TheKnife - Documentazione Tecnica

## 1. Introduzione

TheKnife è un'applicazione desktop JavaFX sviluppata come progetto universitario per la gestione di ristoranti, prenotazioni, recensioni, preferiti e profili utente. Il sistema è organizzato in un'architettura a più livelli composta da:
- client JavaFX;
- server TCP dedicato;
- database relazionale PostgreSQL;
- codice condiviso per DTO, validazione e logica di business.

L'obiettivo del progetto è fornire un'esperienza coerente sia per gli utenti clienti sia per i ristoratori, garantendo un'interfaccia moderna, funzioni complete e robusti controlli di validazione.

---

## 2. Obiettivi del sistema

Il sistema è stato realizzato per supportare i seguenti scenari:
1. ricerca e navigazione di ristoranti;
2. registrazione e autenticazione utenti;
3. gestione profilo personale;
4. preferiti e recensioni;
5. prenotazioni online;
6. dashboard gestione ristoratore;
7. gestione completa dei locali e delle recensioni ricevute.

---

## 3. Stack tecnologico

### 3.1 Linguaggio e runtime
- Java 21
- JavaFX 21
- Maven

### 3.2 Database
- PostgreSQL

### 3.3 Librerie principali
- JavaFX Controls / FXML
- HikariCP per la gestione del pool di connessioni
- PostgreSQL JDBC Driver
- JUnit 5 per test
- SLF4J per logging

### 3.4 Struttura build
Il progetto usa Maven con plugin JavaFX configurato nel file `pom.xml`.

Punto di partenza build:

```bash
mvn clean compile
```

---

## 4. Architettura del sistema

### 4.1 Vista ad alto livello

```text
+-------------------------+
|       Client JavaFX     |
|  Controllers + FXML     |
|  Navigator              |
+-----------+-------------+
            |
            | TCP socket / request-response
            v
+-------------------------+
|       TheKnifeServer     |
|  Server thread / socket |
|  ClientHandler          |
+-----------+-------------+
            |
            | JDBC / DB queries
            v
+-------------------------+
|    PostgreSQL Database  |
+-------------------------+
```

### 4.2 Componenti principali

#### Client
Il client JavaFX realizza la UI dell'applicazione e invia richieste al server.
Le classi principali sono collocate in:
- `src/main/java/project/controllers`
- `src/main/java/project/client`
- `src/main/java/project/shared`

#### Server
Il backend ascolta richieste e le elabora tramite `ClientHandler`, che interpreta il protocollo di richieste e interroga il database.

#### Database layer
Il layer di persistenza è responsabile della connessione e delle operazioni SQL, con gestione delle transazioni e dati persistenti.

---

## 5. Struttura delle cartelle

```text
src/
├── main/
│   ├── java/
│   │   ├── db/
│   │   ├── project/
│   │   │   ├── client/
│   │   │   ├── controllers/
│   │   │   ├── server/
│   │   │   └── shared/
│   │   └── theknife/
│   └── resources/
│       └── project/controllers/
└── test/
```

### 5.1 Package principali

#### `project.controllers`
Contiene i controller JavaFX che gestiscono:
- login e registrazione;
- home;
- ricerca;
- dettagli ristorante;
- recensioni;
- preferiti;
- prenotazioni;
- profilo utente;
- dashboard ristoratore.

#### `project.server`
Contiene la gestione del server e del protocollo di comunicazione TCP.
Classi di riferimento:
- `TheKnifeServer`
- `ClientHandler`

#### `project.client`
Contiene i servizi client per le chiamate al server e la configurazione di connessione.

#### `project.shared`
Contiene:
- DTO;
- utility parser;
- modelli condivisi;
- validazione request;
- logica comune a client e server.

---

## 6. Componenti logici chiave

### 6.1 `ClientMain`
Entry point del client JavaFX. Avvia la finestra principale e inizializza la connessione automatica al server.

### 6.2 `Navigator`
Classe centrale di navigazione per la UI. Gestisce il passaggio tra pagine FXML, i profili autenticati e la route corretta a seconda del tipo di utente (cliente o ristoratore).

### 6.3 `ServerApiClient`
Classe client per inviare richieste al backend. È responsabile della gestione della socket, della codifica e della lettura delle risposte.

### 6.4 `ClientHandler`
Server-side request router. Riceve i comandi e li invoca in base al tipo di richiesta (login, registrazione, prenotazione, preferiti, recensioni, ecc.).

### 6.5 `RequestValidator`
Classe di validazione condivisa. Garantisce che le richieste rispettino i vincoli di business, tra cui:
- password con complessità minima;
- obbligatorietà dei campi;
- vincoli di data e sessione;
- controllo della validità dei parametri richiesti.

### 6.6 `DatabaseManager`
Responsabile dell'apertura dei database e delle query SQL.

---

## 7. Pattern di comunicazione

La comunicazione client-server segue un pattern testuale semplice e robusto:

```text
COMANDO:PARAMETRO1:PARAMETRO2:...
```

Esempi:
- `LOGIN:email:password`
- `REGISTER:nome:cognome:...`
- `ADD_PREFERITO:userId:restaurantId`
- `CREATE_RESERVATION:userId:restaurantId:date:time:guests:note`
- `GET_OWNER_RESTAURANT:userId`

Il server ritorna una risposta formattata con prefisso di stato:
- `OK:`
- `FAIL:`
- `ERROR:`

Questo approccio consente un'interazione semplice tra UI e backend, facile da estendere e da testare.

---

## 8. Gestione delle schermate

Le schermate sono definite in FXML, in cartella `src/main/resources/project/controllers`.

Principali gruppi di schermate:
- `NotLoggedUser/` → login, registrazione, home non autenticata, ricerca e dettagli pubblici;
- `LoggedUser/` → home, profilo, preferiti, recensioni, prenotazioni, dettagli loggato;
- `OwnerUser/` → dashboard ristoratore, gestione locali, creazione e profilo.

Il nome dei file FXML e il controller associato sono collegati tramite `fx:controller`.

---

## 9. Flussi principali di business

### 9.1 Registrazione utente
1. l'utente inserisce i dati nel form di registrazione;
2. la UI invoca il metodo di registrazione;
3. il client invia una richiesta al server;
4. il server valida i parametri;
5. viene inserita la voce nel database;
6. si ritorna un esito positivo o negativo alla UI.

### 9.2 Login
1. il client manda la coppia email/password;
2. il server verifica le credenziali;
3. in caso positivo, viene generata/validata la sessione;
4. il client aggiorna lo stato di autenticazione e naviga alla home corretta.

### 9.3 Ricerca e dettaglio locale
1. l'utente compila filtri o ricerca testuale;
2. la UI invoca la logica di ricerca nel controller;
3. il client invoca il server con la richiesta appropriata;
4. il backend esegue query SQL e ritorna risultati;
5. la UI costruisce le card e i dettagli del ristorante.

### 9.4 Prenotazione
1. il cliente apre la scheda del locale;
2. compila data, ora e dettagli;
3. la prenotazione viene inviata al server;
4. il backend salva la richiesta nel database;
5. viene restituito un codice di prenotazione;
6. l'utente viene reindirizzato alla pagina delle prenotazioni.

### 9.5 Preferiti e recensioni
Sono trattate come entità correlate all'utente e al locale, con controlli di unicità e validazione del contenuto.

---

## 10. Validazione e regole di business

Le regole sono state implementate con particolare attenzione a usabilità e sicurezza.

### 10.1 Password
Requisiti minimi:
- lunghezza minima: 7;
- almeno una lettera maiuscola;
- almeno una lettera minuscola;
- almeno un numero.

### 10.2 Data di nascita
- non può essere superiore alla data odierna;
- non può essere oggi;
- la data deve essere coerente con la logica di registrazione.

### 10.3 Sessione
L'applicazione controlla che il token/sessione dell'utente sia valido prima di consentire operazioni sensibili (prenotazioni, preferiti, modifiche profilo, ecc.).

---

## 11. UI e pattern di design

La parte visuale è costruita con JavaFX e FXML, con CSS condiviso.

File chiave:
- `src/main/resources/project/controllers/style.css`

Il CSS centralizza:
- navbar;
- palette cromatica;
- card e layout;
- componenti modali e form;
- stato di errore e successo.

---

## 12. Sicurezza e affidabilità

### 12.1 Controlli lato client
- validazione del form;
- prevenzione di input non coerenti;
- gestione di alert e feedback utente.

### 12.2 Controlli lato server
- validazione di parametri in input;
- verifica delle sessioni;
- check delle relazioni tra utente e ristorante;
- minimizzazione della possibilità di dati inconsistenti.

### 12.3 Gestione errori
L'applicazione usa alert di errore e messaggi espliciti per casi come:
- connessione server assente;
- sessione scaduta;
- dati non validi;
- violazioni di business rules.

---

## 13. Esecuzione in ambiente di sviluppo

### 13.1 Compilazione
```bash
mvn -q -DskipTests compile
```

### 13.2 Esecuzione client
Da IDE o tramite JavaFX Maven plugin:

```bash
mvn javafx:run
```

È necessario che il server sia già attivo e raggiungibile.

### 13.3 Esecuzione server
Il server viene avviato tramite la classe:

```text
theknife.ServerMain
```

oppure via entry point dedicato `NonGuiServerMain`.

---

## 14. Dipendenze e pacchetti

Il progetto usa una serie di librerie JavaFX e di supporto per UI e interfacce avanzate. Il file `pom.xml` è il punto centrale per la gestione di dipendenze e plugin Maven.

Principali dipendenze:
- `javafx-controls`
- `javafx-fxml`
- `javafx-graphics`
- `javafx-swing`
- `controlsfx`
- `formsfx-core`
- `validatorfx`
- `HikariCP`
- `postgresql`
- `junit-jupiter`

---

## 15. Punti di forza del progetto

- interfaccia utente moderna e coerente;
- struttura modulare e ben separata tra UI, server e backend;
- gestione di diversi profili di utenza;
- validazione forte dei dati;
- logica di prenotazione e recensione completa;
- potenziale di evoluzione per nuove funzionalità (filtri avanzati, mappa, pagamento, notifica, ecc.).

---

## 16. Limiti e possibili miglioramenti

Il progetto presenta alcune aree naturalmente migliorabili:
- integrazione completa di una mappa interattiva;
- gestione di notifiche push o email;
- sistemi di paginazione più avanzati;
- autenticazione JWT o token più robusti;
- dashboard analitica per ristoratori e amministratori;
- test automatici più estesi.

In ambito universitario, comunque, il prodotto raggiunge un buon livello di completezza funzionale e architetturale.

---

## 17. Conclusione

TheKnife è un sistema completo di gestione di ristoranti e prenotazioni pensato per un uso desktop con architettura client-server, orientato a un'esperienza utente moderna e a una logica di business solida e verificabile.

La soluzione combina:
- interfaccia grafica JavaFX;
- server applicativo;
- database relazionale;
- validazione avanzata;
- logica di navigazione e accesso per ruoli differenti.

Questo lo rende un progetto maturo per una consegna accademica e per un'eventuale evoluzione futura verso un sistema più completo e scalabile.
