package project.controllers;
/**
 * Ristorante
 *
 * Purpose: Brief description of the class responsibilities and role in the application.
 *
 * Responsibilities/Usage:
 * - Describe main responsibilities and how this class is used at a high level.
 *
 * Design notes / Dependencies:
 * - List key dependencies and rationale for design choices (separation of concerns, performance, simplicity).
 *
 * Implementation details:
 * - Mention important collaborators, expected inputs/outputs and lifecycle (initialization, cleanup, threading if relevant).
 */

public class Ristorante {
/**
 * Field: nome
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String nome;
/**
 * Field: citta
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String citta;
/**
 * Field: nazione
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String nazione;
/**
 * Field: indirizzo
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String indirizzo;
/**
 * Field: latitudine
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final double latitudine;
/**
 * Field: longitudine
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final double longitudine;
    private final String fasciaPrezzo; // e.g. "€€ — Medio (~45€)"
/**
 * Field: tipoCucina
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String tipoCucina;
/**
 * Field: delivery
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final boolean delivery;
/**
 * Field: prenotazioneOnline
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final boolean prenotazioneOnline;

    public Ristorante(String nome, String citta, String nazione, String indirizzo, double latitudine, double longitudine, String fasciaPrezzo, String tipoCucina, boolean delivery, boolean prenotazioneOnline) {
        this.nome = nome;
        this.citta = citta;
        this.nazione = nazione;
        this.indirizzo = indirizzo;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.tipoCucina = tipoCucina;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
    }

    // Getter e Setter standard...
    public String getNome() { return nome; }
    public String getCitta() { return citta; }
    public String getTipoCucina() { return tipoCucina; }
    public boolean isDelivery() { return delivery; }
    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
}