package project.client.controllers;

/**
 * Utility class vuota che funge da placeholder per eventuali helper comuni
 * ai controller client-side. Attualmente non espone API pubbliche.
 *
 * Motivazione: mantenere uno spazio dedicato per helper evita di disperdere
 * metodi statici in classi di dominio. La classe è final e ha un costruttore
 * privato per prevenire istanziazione e sottoclassificazione.
 *
 */
public final class ClientControllerSupport {
    // Costruttore privato per impedire istanziazione
    private ClientControllerSupport() {
    }
}
