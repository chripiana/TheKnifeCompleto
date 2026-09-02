# TheKnife - Documentazione Utente

## 1. Premessa

TheKnife è un'applicazione JavaFX pensata per aiutare gli utenti a:
- cercare ristoranti in base a città, cucina, prezzo e valutazione;
- visualizzare i dettagli di un locale;
- aggiungere ristoranti ai preferiti;
- lasciare recensioni;
- prenotare un tavolo online;
- gestire il proprio profilo personale.

Per i ristoratori, l'applicazione consente inoltre di:
- gestire i propri locali;
- aggiungere nuovi ristoranti;
- modificare i dettagli di un locale;
- consultare recensioni ricevute;
- rispondere e monitorare l'attività del proprio account.

Il prodotto è stato progettato come applicazione desktop multi-utente con architettura client-server, orientata a un'esperienza moderna, semplice e coerente dal punto di vista grafico.

---

## 2. Requisiti di sistema

Per utilizzare correttamente l'applicazione sono richiesti:
- Java 21 o superiore;
- Maven 3.9+;
- sistema operativo con supporto JavaFX;
- accesso a un server locale o configurato in rete;
- database PostgreSQL disponibile per il backend.

In ambiente di sviluppo, l'applicazione è stata verificata con Java 21 e Maven standard.

---

## 3. Installazione e avvio

### 3.1 Clonare ed eseguire il progetto

1. Aprire un terminale nella cartella del progetto.
2. Eseguire:

```bash
mvn clean compile
```

3. Avviare l'applicazione client tramite il main JavaFX del progetto, oppure utilizzare la configurazione di esecuzione dell'IDE.

Il punto di ingresso principale del client è:

```text
theknife.ClientMain
```

Per il server è disponibile il relativo entry point:

```text
theknife.ServerMain
```

---

## 4. Accesso e tipologie di utente

L'applicazione distingue due principali modalità d'uso:

### 4.1 Utente standard (cliente)
L'utente cliente può:
- registrarsi;
- accedere con credenziali;
- cercare ristoranti;
- filtrare la ricerca;
- aprire la scheda dettagliata del locale;
- prenotare online;
- aggiungere ai preferiti;
- scrivere recensioni;
- consultare le proprie prenotazioni, recensioni e preferiti;
- accedere al proprio profilo personale.

### 4.2 Utente ristoratore
L'utente ristoratore può:
- accedere al proprio account gestionale;
- creare un nuovo ristorante;
- modificare i propri locali;
- verificare statistiche base e recensioni ricevute;
- gestire i dati del proprio profilo.

---

## 5. Flusso di utilizzo

### 5.1 Registrazione

All'apertura dell'applicazione viene mostrata la home non autenticata.

Per registrarsi:
1. scegliere "Registrati" oppure accedere alla schermata di registrazione;
2. inserire nome, cognome, email, data di nascita, domicilio e password;
3. confermare i dati.

Regole applicate:
- la password deve contenere almeno:
  - una lettera maiuscola;
  - una lettera minuscola;
  - un numero;
  - una lunghezza minima di 7 caratteri;
- la data di nascita non può essere oggi o superiore ad oggi;
- non è consentita una data di nascita futura.

### 5.2 Login

Per accedere:
1. inserire email e password;
2. premere "Accedi";
3. il sistema reindirizza alla home autenticata o alla dashboard corretta in base al tipo di account.

### 5.3 Ricerca ristoranti

Dalla home autenticata o non autenticata è possibile:
- cercare per nome o tipologia di cucina;
- applicare filtri per città, prezzo massimo, valutazione minima e disponibilità di prenotazione;
- ordinare la ricerca per nome, prezzo, valutazione, popolarità o distanza;
- ricercare in base alla posizione del domicilio dell'utente.

### 5.4 Scheda dettaglio ristorante

Selezionando un ristorante si apre la scheda dettagliata con:
- nome, indirizzo e città;
- tipologia di cucina;
- valutazione media;
- lista delle recensioni;
- opzioni di prenotazione;
- pulsante per aggiungere ai preferiti;
- possibilità di pubblicare una recensione.

### 5.5 Prenotazione online

Per prenotare un tavolo:
1. aprire la scheda del ristorante;
2. premere "Prenota Ora";
3. compilare data, ora, numero di persone e note opzionali;
4. confermare la prenotazione.

La prenotazione viene salvata sul server e successivamente consultabile nella pagina dedicata.

### 5.6 Preferiti

Un utente può:
- aggiungere un locale ai preferiti;
- rimuoverlo in qualsiasi momento;
- consultare l'elenco completo nella pagina "Preferiti".

### 5.7 Recensioni

Nella scheda dettaglio è possibile:
- selezionare una valutazione da 1 a 5 stelle;
- scrivere un commento;
- pubblicare la recensione.

Le recensioni sono visibili nella pagina dedicata e nel dettaglio del locale.

### 5.8 Gestione profilo utente

Dalla pagina di profilo è possibile:
- aggiornare nome e cognome;
- modificare il domicilio;
- cambiare la password;
- consultare le informazioni personali.

### 5.9 Gestione ristoratore

Un utente ristoratore può:
- entrare nella propria dashboard;
- creare nuovi ristoranti;
- modificare i dati dei locali esistenti;
- consultare le recensioni ricevute;
- navigare rapidamente alla propria area personale.

---

## 6. Funzionalità principali

### 6.1 Cliente
- registrazione e login;
- ricerca avanzata di ristoranti;
- dettagli completi del locale;
- recensioni e votazioni;
- prenotazione online;
- preferiti;
- gestione profilo;
- cronologia prenotazioni e recensioni personali.

### 6.2 Ristoratore
- dashboard personale;
- creazione nuovo locale;
- modifica dati ristorante;
- lista dei propri ristoranti;
- visualizzazione delle recensioni ricevute;
- accesso rapido alla scheda locale e al profilo personale.

---

## 7. Sicurezza e validazione

L'applicazione applica controlli sia lato client che lato server per garantire coerenza dei dati.

Principali regole:
- lunghezza password minima di 7 caratteri;
- almeno una lettera maiuscola;
- almeno una lettera minuscola;
- almeno un numero;
- data di nascita non superiore a oggi;
- controllo dell'accesso alla sessione utente;
- validazione delle richieste verso il backend.

---

## 8. Troubleshooting e problemi comuni

### 8.1 Impossibile connettersi al server
- verificare che il server sia avviato;
- controllare host e porta configurate;
- verificare che PostgreSQL sia disponibile;
- controllare i log del server.

### 8.2 Password non valida
- verificare che la password contenga almeno una maiuscola, una minuscola e un numero;
- assicurarsi che la lunghezza sia almeno di 7 caratteri.

### 8.3 Data di nascita non accettata
- la data inserita non può essere oggi o futura.

### 8.4 Bottone o navigazione non funzionante
- verificare che l'applicazione sia stata avviata correttamente;
- verificare lo stato di autenticazione;
- controllare che la sessione utente non sia scaduta.

---

## 9. Considerazioni finali

TheKnife è un'applicazione completa, pensata come progetto universitario di tipo desktop con architettura JavaFX + server TCP + database. Il sistema combina interfaccia moderna, autenticazione, gestione recensori e ristoratori, prenotazioni, preferiti e validazione avanzata dei dati.

Il prodotto è stato sviluppato con attenzione alle esigenze di usabilità, coerenza visiva e robustezza del flusso applicativo.

---

## 10. Contatti / crediti

Progetto sviluppato in ambiente JavaFX e Java/Swing-based architecture per applicazione desktop con backend server e database relazionale.

Per informazioni aggiuntive, fare riferimento alla documentazione tecnica e alle classi del progetto in `src/main/java`.
