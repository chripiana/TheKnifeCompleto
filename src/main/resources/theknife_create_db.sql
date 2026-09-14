-- =============================================================
--  TheKnife - theknife_create_db.sql
--  Script unico di creazione del database modificato per il CSV
-- =============================================================

-- =============================================================
--  SEZIONE 1 - TABELLE
-- =============================================================

-- -------------------------------------------------------------
--  Tabella: Utenti
-- -------------------------------------------------------------
CREATE TABLE Utenti (
    id_utente       SERIAL          PRIMARY KEY,
    nome            VARCHAR(100)    NOT NULL,
    cognome         VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    data_nascita    DATE,
    luogo_domicilio VARCHAR(255)    NOT NULL,
    lat_domicilio   DECIMAL(9, 6),
    lon_domicilio   DECIMAL(9, 6),
    ruolo           VARCHAR(10)     NOT NULL,

    CONSTRAINT ck_ruolo         CHECK (ruolo IN ('cliente', 'gestore')),
    CONSTRAINT ck_lat_domicilio CHECK (lat_domicilio BETWEEN -90  AND  90),
    CONSTRAINT ck_lon_domicilio CHECK (lon_domicilio BETWEEN -180 AND 180)
);


-- -------------------------------------------------------------
--  Tabella: RistorantiTheKnife
--  Modificata: id_ristorante diventa VARCHAR per ospitare gli UUID del CSV.
--  Nomi colonne adattati a quelli attesi dal codice di importazione.
-- -------------------------------------------------------------
CREATE TABLE RistorantiTheKnife (
    id_ristorante       VARCHAR(50)     PRIMARY KEY, -- Cambiato in VARCHAR per gli UUID del CSV
    nome                VARCHAR(255)    NOT NULL,
    nazione             VARCHAR(100)    NOT NULL,
    citta               VARCHAR(100)    NOT NULL,
    indirizzo           VARCHAR(255)    NOT NULL,
    latitudine          DECIMAL(9, 6)   NOT NULL,
    longitudine         DECIMAL(9, 6)   NOT NULL,
    prezzo_medio        INT             NOT NULL,    -- Richiesto dal CSV al posto di fascia_prezzo
    stellato            INT             NOT NULL DEFAULT 0,
    prenotazione_obbligatoria BOOLEAN   NOT NULL DEFAULT FALSE,
    tipologia_cucina    VARCHAR(100)    NOT NULL,    -- Richiesto dal CSV al posto di tipo_cucina
    id_gestore          INT             NULL,        -- NULL consentito per l'importazione automatica iniziale
    delivery            BOOLEAN         NOT NULL DEFAULT FALSE,
    prenotazione_online BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT ck_latitudine  CHECK (latitudine  BETWEEN -90  AND  90),
    CONSTRAINT ck_longitudine CHECK (longitudine BETWEEN -180 AND 180),
    CONSTRAINT ck_prezzo      CHECK (prezzo_medio >= 0),

    CONSTRAINT fk_gestore FOREIGN KEY (id_gestore)
        REFERENCES Utenti(id_utente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- -------------------------------------------------------------
--  Tabella: Recensioni
--  Modificata: id_ristorante diventa VARCHAR(50) per fare il JOIN
-- -------------------------------------------------------------
CREATE TABLE Recensioni (
    id_recensione   SERIAL      PRIMARY KEY,
    id_ristorante   VARCHAR(50) NOT NULL,            -- Cambiato a VARCHAR(50) per combaciare con la PK
    id_utente       INT         NOT NULL,
    stelle          SMALLINT    NOT NULL,
    testo           TEXT        NOT NULL,
    data_recensione TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_stelle CHECK (stelle BETWEEN 1 AND 5),
    CONSTRAINT uq_una_recensione_per_utente UNIQUE (id_ristorante, id_utente),

    CONSTRAINT fk_rec_ristorante FOREIGN KEY (id_ristorante)
        REFERENCES RistorantiTheKnife(id_ristorante)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_rec_utente FOREIGN KEY (id_utente)
        REFERENCES Utenti(id_utente)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- -------------------------------------------------------------
--  Tabella: RisposteRecensioni
-- -------------------------------------------------------------
CREATE TABLE RisposteRecensioni (
    id_risposta   SERIAL    PRIMARY KEY,
    id_recensione INT       NOT NULL UNIQUE,
    id_gestore    INT       NOT NULL,
    testo         TEXT      NOT NULL,
    data_risposta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_risp_recensione FOREIGN KEY (id_recensione)
        REFERENCES Recensioni(id_recensione)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_risp_gestore FOREIGN KEY (id_gestore)
        REFERENCES Utenti(id_utente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- -------------------------------------------------------------
--  Tabella: Preferiti
--  Modificata: id_ristorante diventa VARCHAR(50)
-- -------------------------------------------------------------
CREATE TABLE Preferiti (
    id_utente     INT         NOT NULL,
    id_ristorante VARCHAR(50) NOT NULL,            -- Cambiato a VARCHAR(50)

    PRIMARY KEY (id_utente, id_ristorante),

    CONSTRAINT fk_pref_utente FOREIGN KEY (id_utente)
        REFERENCES Utenti(id_utente)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_pref_ristorante FOREIGN KEY (id_ristorante)
        REFERENCES RistorantiTheKnife(id_ristorante)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- -------------------------------------------------------------
--  Tabella: Prenotazioni
-- -------------------------------------------------------------
CREATE TABLE Prenotazioni (
    id_prenotazione     SERIAL       PRIMARY KEY,
    id_utente           INT          NOT NULL,
    id_ristorante       VARCHAR(50)  NOT NULL,
    data_prenotazione   DATE         NOT NULL,
    ora_prenotazione    TIME         NOT NULL,
    numero_persone      INT          NOT NULL DEFAULT 1,
    codice_prenotazione VARCHAR(50)  NOT NULL UNIQUE,
    note                TEXT,
    stato               VARCHAR(20)  NOT NULL DEFAULT 'attiva',
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_numero_persone CHECK (numero_persone BETWEEN 1 AND 20),
    CONSTRAINT fk_prenot_utente FOREIGN KEY (id_utente)
        REFERENCES Utenti(id_utente)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_prenot_ristorante FOREIGN KEY (id_ristorante)
        REFERENCES RistorantiTheKnife(id_ristorante)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- =============================================================
--  SEZIONE 2 - INDICI
-- =============================================================
CREATE UNIQUE INDEX idx_utenti_email ON Utenti (email);
CREATE INDEX idx_rist_citta ON RistorantiTheKnife (citta);
CREATE INDEX idx_rist_nazione ON RistorantiTheKnife (nazione);
CREATE INDEX idx_rist_cucina ON RistorantiTheKnife (tipologia_cucina); -- Aggiornato il nome colonna
CREATE INDEX idx_rist_prezzo ON RistorantiTheKnife (prezzo_medio);    -- Aggiornato il nome colonna
CREATE INDEX idx_rist_delivery ON RistorantiTheKnife (delivery);
CREATE INDEX idx_rist_prenot ON RistorantiTheKnife (prenotazione_online);
CREATE INDEX idx_rist_gestore ON RistorantiTheKnife (id_gestore);
CREATE INDEX idx_rist_gestore_nome ON RistorantiTheKnife (id_gestore, nome);
CREATE INDEX idx_rist_citta_prezzo ON RistorantiTheKnife (citta, prezzo_medio);
CREATE INDEX idx_rist_citta_cucina ON RistorantiTheKnife (citta, tipologia_cucina);
CREATE INDEX idx_rist_delivery_prenot ON RistorantiTheKnife (delivery, prenotazione_online);
-- New spatial-friendly indexes to accelerate bounding-box searches
CREATE INDEX idx_rist_latitudine ON RistorantiTheKnife (latitudine);
CREATE INDEX idx_rist_longitudine ON RistorantiTheKnife (longitudine);
CREATE INDEX idx_rist_lat_long ON RistorantiTheKnife (latitudine, longitudine);
CREATE INDEX idx_rec_ristorante ON Recensioni (id_ristorante);
CREATE INDEX idx_rec_ristorante_stelle ON Recensioni (id_ristorante, stelle);
CREATE INDEX idx_rec_utente ON Recensioni (id_utente);
CREATE INDEX idx_rec_utente_data ON Recensioni (id_utente, data_recensione DESC);
CREATE INDEX idx_risp_recensione ON RisposteRecensioni (id_recensione);
CREATE INDEX idx_risp_gestore ON RisposteRecensioni (id_gestore);
CREATE INDEX idx_pref_utente ON Preferiti (id_utente);
CREATE INDEX idx_pref_ristorante ON Preferiti (id_ristorante);
CREATE INDEX idx_prenot_utente ON Prenotazioni (id_utente);
CREATE INDEX idx_prenot_utente_data ON Prenotazioni (id_utente, data_prenotazione DESC, ora_prenotazione DESC);
CREATE INDEX idx_prenot_ristorante ON Prenotazioni (id_ristorante);
CREATE INDEX idx_prenot_ristorante_data ON Prenotazioni (id_ristorante, data_prenotazione DESC);


-- =============================================================
--  SEZIONE 3 - FUNZIONI TRIGGER
-- =============================================================
CREATE OR REPLACE FUNCTION fn_check_ruolo_gestore()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    -- Controllo solo se id_gestore viene effettivamente inserito (visto che ora può essere NULL)
    IF NEW.id_gestore IS NOT NULL AND (SELECT ruolo FROM Utenti WHERE id_utente = NEW.id_gestore) <> 'gestore' THEN
        RAISE EXCEPTION 'Vincolo violato: l''utente % non ha ruolo ''gestore''', NEW.id_gestore;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_check_ruolo_gestore
BEFORE INSERT OR UPDATE ON RistorantiTheKnife
FOR EACH ROW EXECUTE FUNCTION fn_check_ruolo_gestore();


CREATE OR REPLACE FUNCTION fn_check_ruolo_cliente()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (SELECT ruolo FROM Utenti WHERE id_utente = NEW.id_utente) <> 'cliente' THEN
        RAISE EXCEPTION 'Vincolo violato: l''utente % non ha ruolo ''cliente''', NEW.id_utente;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_check_ruolo_cliente
BEFORE INSERT OR UPDATE ON Recensioni
FOR EACH ROW EXECUTE FUNCTION fn_check_ruolo_cliente();


CREATE OR REPLACE FUNCTION fn_check_gestore_risposta()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_gestore_atteso INT;
BEGIN
    SELECT r.id_gestore
      INTO v_gestore_atteso
      FROM RistorantiTheKnife r
      JOIN Recensioni rec ON rec.id_ristorante = r.id_ristorante
     WHERE rec.id_recensione = NEW.id_recensione;

    IF v_gestore_atteso IS DISTINCT FROM NEW.id_gestore THEN
        RAISE EXCEPTION 'Vincolo violato: il gestore % non gestisce il ristorante di questa recensione', NEW.id_gestore;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_check_gestore_risposta
BEFORE INSERT OR UPDATE ON RisposteRecensioni
FOR EACH ROW EXECUTE FUNCTION fn_check_gestore_risposta();