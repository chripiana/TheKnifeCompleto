package project.shared.dto;
/**
 * RestaurantSummary
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

public class RestaurantSummary {
/**
 * Field: id
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String id;
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
 * Field: mediaStelle
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final double mediaStelle;
/**
 * Field: numRecensioni
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final int numRecensioni;

    public RestaurantSummary(String id, String nome, String citta, double mediaStelle, int numRecensioni) {
        this.id = id;
        this.nome = nome;
        this.citta = citta;
        this.mediaStelle = mediaStelle;
        this.numRecensioni = numRecensioni;
    }

/**
 * Method: getId
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public String getId() {
        return id;
    }

/**
 * Method: getNome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public String getNome() {
        return nome;
    }

/**
 * Method: getCitta
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public String getCitta() {
        return citta;
    }

/**
 * Method: getMediaStelle
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public double getMediaStelle() {
        return mediaStelle;
    }

/**
 * Method: getNumRecensioni
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public int getNumRecensioni() {
        return numRecensioni;
    }
}
