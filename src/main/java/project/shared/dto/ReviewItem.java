package project.shared.dto;
/**
 * ReviewItem
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

public class ReviewItem {
/**
 * Field: idRecensione
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final int idRecensione;
/**
 * Field: stelle
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final int stelle;
/**
 * Field: testo
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String testo;
/**
 * Field: data
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String data;
/**
 * Field: autoreNome
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String autoreNome;
/**
 * Field: autoreCognome
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String autoreCognome;
/**
 * Field: giaRisposto
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final boolean giaRisposto;
/**
 * Field: risposta
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String risposta;
/**
 * Field: dataRisposta
 * Purpose: concise description of the fields role and how it is used by the class.
 * Notes: mention nullability, lifecycle, and external dependencies if any.
 */
    private final String dataRisposta;

    public ReviewItem(int idRecensione, int stelle, String testo, String data, String autoreNome, String autoreCognome,
                     boolean giaRisposto, String risposta, String dataRisposta) {
        this.idRecensione = idRecensione;
        this.stelle = stelle;
        this.testo = testo;
        this.data = data;
        this.autoreNome = autoreNome;
        this.autoreCognome = autoreCognome;
        this.giaRisposto = giaRisposto;
        this.risposta = risposta;
        this.dataRisposta = dataRisposta;
    }

    public int getIdRecensione() { return idRecensione; }
    public int getStelle() { return stelle; }
    public String getTesto() { return testo; }
    public String getData() { return data; }
    public String getAutoreNome() { return autoreNome; }
    public String getAutoreCognome() { return autoreCognome; }
    public boolean isGiaRisposto() { return giaRisposto; }
    public String getRisposta() { return risposta; }
    public String getDataRisposta() { return dataRisposta; }
}
