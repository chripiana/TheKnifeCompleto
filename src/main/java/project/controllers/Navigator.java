package project.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;

import project.client.services.ServerApiClient;

/**
 * Navigator
 *
 * Singleton che si occupa della navigazione tra le diverse viste FXML
 * dell'applicazione. Incapsula la logica di risoluzione dei percorsi FXML
 * (NotLoggedUser / LoggedUser / OwnerUser), il caching di alcune viste e
 * lo stato minimo dell'utente loggato (id, ruolo).
 *
 * Motivazioni di design:
 * - centralizzare la gestione delle Scene e dei percorsi FXML facilita la
 *   manutenzione e permette di applicare comportamenti comuni (es. caching)
 * - mantenere solo l'identificativo e il ruolo semplifica le decisioni
 *   di routing senza esporre ulteriori dettagli sensibili
 **/
/**
 * Navigator
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
public class Navigator {
    /** Istanza singleton */
    private static Navigator instance;
    /** Stage principale dell'app su cui viene caricata la Scene */
    private Stage stage;

    /** Id dell'utente attualmente loggato (-1 = anonimo).*/
    private int idUtenteLoggato = -1;
    /** Ruolo dell'utente (CLIENTE/GESTORE), usato per routing.*/
    private String ruoloUtenteLoggato = null;

    /** Cache della view dei risultati di ricerca per poter tornare indietro velocemente.*/
    private Parent cachedSearchView = null;
    /** Titolo associato alla view cache.*/
    private String cachedSearchTitle = "Risultati Ricerca";

    /** Costruttore privato per singleton.*/
    private Navigator() {
        System.out.println("[NAVIGATOR] Client inizializzato: nessun accesso diretto al database. Tutte le richieste passano tramite il server.");
    }

/**
 * Method: getInstance
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public static synchronized Navigator getInstance() {
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }

/**
 * Method: setStage
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

/**
 * Method: getIdUtenteLoggato
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public int getIdUtenteLoggato() {
        return idUtenteLoggato;
    }

/**
 * Method: setIdUtenteLoggato
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void setIdUtenteLoggato(int idUtenteLoggato) {
        this.idUtenteLoggato = idUtenteLoggato;
    }

/**
 * Method: getRuoloUtenteLoggato
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public String getRuoloUtenteLoggato() {
        return ruoloUtenteLoggato;
    }

/**
 * Method: setRuoloUtenteLoggato
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void setRuoloUtenteLoggato(String ruoloUtenteLoggato) {
        this.ruoloUtenteLoggato = ruoloUtenteLoggato;
    }

/**
 * Method: logout
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void logout() {
        this.idUtenteLoggato = -1;
        this.ruoloUtenteLoggato = null;
        // Rimuove il token statico condiviso e chiude eventuali connessioni client
        try {
            ServerApiClient.clearSessionToken();
        } catch (Exception e) {
            // Non bloccare il logout se la pulizia fallisce
        }
        System.out.println("[NAVIGATOR] Utente disconnesso.");
    }


/**
 * Method: risolviPercorsoFXML
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private URL risolviPercorsoFXML(String fxmlFile) {
        if (fxmlFile == null || fxmlFile.trim().isEmpty())
            return null;


        String nomeFilePuro = fxmlFile.substring(fxmlFile.lastIndexOf("/") + 1);


        String[] tentativiPercorso = {
                "/project/controllers/NotLoggedUser/" + nomeFilePuro,
                "/project/controllers/LoggedUser/" + nomeFilePuro,
                "/project/controllers/OwnerUser/" + nomeFilePuro
        };


        for (String percorso : tentativiPercorso) {
            URL urlTrovato = getClass().getResource(percorso);
            if (urlTrovato != null) {
                return urlTrovato;
            }
        }
        return null;
    }


/**
 * Method: navigateToHome
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToHome() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("home-view.fxml", "Trova il tuo ristorante");
        } else if ("GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("home-view-owner.fxml", "Home Ristoratore");
        } else {
            navigateTo("home-view-logged.fxml", "Benvenuto su TheKnife");
        }
    }

/**
 * Method: navigateTo
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateTo(String fxmlFile, String title) {
        if (stage == null) {
            System.err.println("[NAVIGATOR] Errore: Lo Stage non è stato configurato!");
            return;
        }

        URL fxmlUrl = risolviPercorsoFXML(fxmlFile);
        if (fxmlUrl == null) {
            System.err.println("[ERRORE CRITICO] File FXML non trovato in nessuna cartella: " + fxmlFile);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            updateSceneRoot(root, title);
            System.out.println("[NAVIGATOR] Caricato con successo: " + fxmlUrl.getPath());
        } catch (IOException e) {
            System.err.println("[NAVIGATOR] Impossibile decodificare la struttura del file FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }


/**
 * Method: navigateToSearchWithQueryLogged
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToSearchWithQueryLogged(String queryTesto) {
        URL fxmlUrl = risolviPercorsoFXML("search-view-logged.fxml");
        if (fxmlUrl == null) {
            System.err.println("[ERRORE] Impossibile trovare search-view-logged.fxml");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            SearchController controller = loader.getController();
            controller.inizializzaRicercaGlobale(queryTesto);
            updateSceneRoot(root, "Risultati Ricerca");
        } catch (IOException e) {
            System.err.println("[NAVIGATOR] Errore nel caricamento dinamico di search-view con query.");
            e.printStackTrace();
        }
    }

/**
 * Method: navigateToSearchWithQuery
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToSearchWithQuery(String queryTesto) {
        URL fxmlUrl = risolviPercorsoFXML("search-view.fxml");
        if (fxmlUrl == null) {
            System.err.println("[ERRORE] Impossibile trovare search-view.fxml");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            SearchController controller = loader.getController();
            controller.inizializzaRicercaGlobale(queryTesto);
            updateSceneRoot(root, "Risultati Ricerca");
        } catch (IOException e) {
            System.err.println("[NAVIGATOR] Errore nel caricamento dinamico di search-view con query.");
            e.printStackTrace();
        }
    }


/**
 * Method: navigateToSearchWithAdvancedFilters
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToSearchWithAdvancedFilters(String citta, String prezzoMax, String stelle, String ordine) {
        URL fxmlUrl = risolviPercorsoFXML("search-view.fxml");
        if (fxmlUrl == null) {
            System.err.println("[ERRORE] Impossibile trovare search-view.fxml");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            SearchController controller = loader.getController();
            controller.inizializzaRicercaAvanzata(citta, prezzoMax, stelle, ordine);
            updateSceneRoot(root, "Risultati Ricerca Avanzata");
        } catch (IOException e) {
            System.err.println("[NAVIGATOR] Errore nel caricamento dinamico di search-view con filtri.");
            e.printStackTrace();
        }
    }

    public void navigateToSearchWithAdvancedFiltersLogged(String citta, String prezzoMax, String stelle,
            String ordine) {
        URL fxmlUrl = risolviPercorsoFXML("search-view-logged.fxml");
        if (fxmlUrl == null) {
            System.err.println("[ERRORE] Impossibile trovare search-view-logged.fxml");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            SearchController controller = loader.getController();
            controller.inizializzaRicercaAvanzata(citta, prezzoMax, stelle, ordine);
            updateSceneRoot(root, "Risultati Ricerca Avanzata");
        } catch (IOException e) {
            System.err.println("[NAVIGATOR] Errore nel caricamento dinamico di search-view con filtri.");
            e.printStackTrace();
        }
    }


/**
 * Method: navigateToRestaurantDetails
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToRestaurantDetails(SearchController.RistoranteOggetto ristorante) {
        // Salva la schermata dei risultati prima di sovrascriverla
        if (stage != null && stage.getScene() != null) {
            cachedSearchView = stage.getScene().getRoot();
            cachedSearchTitle = stage.getTitle().replace("TheKnife — ", "");
        }

        boolean loggedUser = this.idUtenteLoggato != -1;
        String detailFile = loggedUser ? "restaurant-details-logged-view.fxml" : "restaurant-details-view.fxml";
        URL fxmlUrl = risolviPercorsoFXML(detailFile);
        if (fxmlUrl == null) {
            System.err.println("[ERRORE CRITICO] Impossibile trovare " + detailFile);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            RestaurantDetailsController controller = loader.getController();
            controller.caricaDatiRistorante(ristorante);

            updateSceneRoot(root, "Dettagli — " + ristorante.nome);
        } catch (IOException e) {
            System.err.println("[NAVIGATOR] Errore nel caricamento dinamico della vista dettagli ristorante.");
            e.printStackTrace();
        }
    }


/**
 * Method: backToSearchResults
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void backToSearchResults() {
        if (cachedSearchView != null) {
            updateSceneRoot(cachedSearchView, cachedSearchTitle);
            System.out.println("[NAVIGATOR] Schermata di ricerca precedente ripristinata con successo.");
        } else {
            navigateTo("search-view.fxml", "Cerca");
        }
    }


/**
 * Method: navigateToHomeIntelligent
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToHomeIntelligent() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("home-view.fxml", "Trova il tuo ristorante");
        } else if ("CLIENTE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("home-view-logged.fxml", "Benvenuto su TheKnife");
        } else if ("GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("home-view-owner.fxml", "Home Ristoratore");
        } else {
            navigateTo("home-view.fxml", "Home");
        }
    }

/**
 * Method: navigateToProfile
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToProfile() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("login-view.fxml", "Accedi");
        } else if ("CLIENTE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("customer-profile-view.fxml", "Il Mio Profilo");
        } else if ("GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("owner-profile-view.fxml", "Dashboard Ristoratore");
        }
    }

/**
 * Method: isGuest
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public boolean isGuest() {
        return this.idUtenteLoggato == -1;
    }

/**
 * Method: isLoggedCustomer
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public boolean isLoggedCustomer() {
        return this.idUtenteLoggato > 0 && "CLIENTE".equalsIgnoreCase(this.ruoloUtenteLoggato);
    }

/**
 * Method: isLoggedOwner
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public boolean isLoggedOwner() {
        return this.idUtenteLoggato > 0 && "GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato);
    }

/**
 * Method: navigateToReservations
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToReservations() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("login-view.fxml", "Accedi");
        } else if (isLoggedCustomer()) {
            navigateTo("reservations-view.fxml", "Le mie prenotazioni");
        } else {
            navigateToHomeIntelligent();
        }
    }

/**
 * Method: updateSceneRoot
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    private void updateSceneRoot(Parent root, String title) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1300, 850);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle("TheKnife — " + title);
        // Temporarily set always-on-top to force window to foreground on macOS
        try {
            stage.setAlwaysOnTop(true);
        } catch (Exception ignored) { }
        stage.show();
        // Restore normal stacking and request focus explicitly
        Platform.runLater(() -> {
            try {
                stage.toFront();
                stage.requestFocus();
                stage.setAlwaysOnTop(false);
            } catch (Exception ignored) {
                // Non critical: if the platform doesn't support these calls, continue silently
            }
        });
    }

}