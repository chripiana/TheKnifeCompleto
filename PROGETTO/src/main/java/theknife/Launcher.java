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
public class Launcher {
    public static void main(String[] args) {
        theknife.ClientMain.main(args);
     }
}