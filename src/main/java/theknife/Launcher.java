/*
 * TheKnife - Launcher per l'avvio del client da jar eseguibile.
 */
package theknife;

/**
 * Classe di avvio separata da ClientMain.
 * Necessaria perché quando la classe main di un jar estende direttamente
 * javafx.application.Application, il comando "java -jar" restituisce
 * l'errore "JavaFX runtime components are missing" anche se i moduli
 * JavaFX sono presenti nel jar. Passando da una classe intermedia che
 * NON estende Application, il problema viene aggirato.
 */
/**
 * Launcher
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
public class Launcher {
/**
 * Method: main
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public static void main(String[] args) {
        theknife.ClientMain.main(args);
     }
}