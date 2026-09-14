package project.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

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
    private record NavigationState(String fxmlFile, String title) {}

    /** Istanza singleton */
    private static Navigator instance;
    /** Stage principale dell'app su cui viene caricata la Scene */
    private Stage stage;

    /** Id dell'utente attualmente loggato (-1 = anonimo).*/
    private int idUtenteLoggato = -1;
    /** Ruolo dell'utente (CLIENTE/GESTORE), usato per routing.*/
    private String ruoloUtenteLoggato = null;
    /** Cronologia delle pagine visitate per tornare indietro in modo pulito. */
    private final Deque<NavigationState> backStack = new ArrayDeque<>();
    /** Pagina da raggiungere dopo un login corretto. */
    private String pendingReturnTarget = null;
    /** Titolo della pagina da raggiungere dopo un login corretto. */
    private String pendingReturnTitle = null;
    /** Pagina corrente attiva nella navigazione. */
    private String currentRoute = null;
    /** Titolo della pagina corrente. */
    private String currentTitle = null;
    /** Ultima pagina di ricerca usata come destinazione di ritorno dal dettaglio. */
    private String lastSearchRoute = null;
    /** Titolo dell'ultima pagina di ricerca. */
    private String lastSearchTitle = null;
    /** Stato della ricerca precedente per ripristinare i criteri usati. */
    private String lastSearchQuery = null;
    private String lastSearchCitta = null;
    private String lastSearchPrezzo = null;
    private String lastSearchStelle = null;
    private String lastSearchOrdine = null;

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
        if (fxmlFile != null && isLoginRoute(fxmlFile) && !isLoginRoute(currentRoute)) {
            pendingReturnTarget = currentRoute != null && !currentRoute.isBlank() ? currentRoute : "home-view.fxml";
            pendingReturnTitle = currentTitle != null && !currentTitle.isBlank() ? currentTitle : "Home";
        }
        navigateToInternal(fxmlFile, title, true);
    }

    private boolean isLoginRoute(String route) {
        if (route == null) return false;
        String normalized = route.trim();
        return normalized.endsWith("login-view.fxml") || normalized.equals("login-view.fxml");
    }

    private void navigateToInternal(String fxmlFile, String title, boolean recordHistory) {
        if (stage == null) {
            System.err.println("[NAVIGATOR] Errore: Lo Stage non è stato configurato!");
            return;
        }

        if (fxmlFile == null || fxmlFile.isBlank()) {
            System.err.println("[NAVIGATOR] Route vuota, salto la navigazione.");
            return;
        }

        if (recordHistory && currentRoute != null && !currentRoute.equals(fxmlFile)) {
            backStack.addLast(new NavigationState(currentRoute, currentTitle));
        }
        currentRoute = fxmlFile;
        currentTitle = title;

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

    public void goBack() {
        if (backStack.isEmpty()) {
            navigateToHomeIntelligent();
            return;
        }

        NavigationState previous = backStack.removeLast();
        String target = previous.fxmlFile();
        String targetTitle = previous.title();

        currentRoute = target;
        currentTitle = targetTitle;

        navigateToInternal(target, targetTitle, false);
    }

    public void navigateToLoginWithReturn(String returnTarget, String returnTitle) {
        String target = returnTarget != null && !returnTarget.isBlank() ? returnTarget : currentRoute != null ? currentRoute : "home-view.fxml";
        pendingReturnTarget = target;
        pendingReturnTitle = returnTitle != null && !returnTitle.isBlank() ? returnTitle : currentTitle != null ? currentTitle : "Home";
        navigateTo("login-view.fxml", "Accedi");
    }

    public boolean hasPendingReturnTarget() {
        return pendingReturnTarget != null && !pendingReturnTarget.isBlank();
    }

    public void navigateAfterLogin() {
        if (!hasPendingReturnTarget()) {
            navigateToHomeIntelligent();
            return;
        }
        String target = pendingReturnTarget;
        String title = pendingReturnTitle;
        pendingReturnTarget = null;
        pendingReturnTitle = null;
        navigateToInternal(target, title, false);
    }


/**
 * Method: navigateToSearchWithQueryLogged
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public void navigateToSearchWithQueryLogged(String queryTesto) {
        rememberSearchState("search-view-logged.fxml", "Cerca Ristoranti", queryTesto, null, null, null, null);
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
        rememberSearchState("search-view.fxml", "Cerca Ristoranti", queryTesto, null, null, null, null);
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
        rememberSearchState("search-view.fxml", "Cerca Ristoranti", null, citta, prezzoMax, stelle, ordine);
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
        rememberSearchState("search-view-logged.fxml", "Cerca Ristoranti", null, citta, prezzoMax, stelle, ordine);
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
    public void rememberSearchRoute(String route, String title) {
        rememberSearchState(route, title, lastSearchQuery, lastSearchCitta, lastSearchPrezzo, lastSearchStelle, lastSearchOrdine);
    }

    public void rememberSearchState(String route, String title, String query, String citta, String prezzo, String stelle, String ordine) {
        if (route == null || route.isBlank()) {
            return;
        }
        this.lastSearchRoute = route;
        this.lastSearchTitle = title != null && !title.isBlank() ? title : "Cerca Ristoranti";
        this.lastSearchQuery = query;
        this.lastSearchCitta = citta;
        this.lastSearchPrezzo = prezzo;
        this.lastSearchStelle = stelle;
        this.lastSearchOrdine = ordine;
    }

    public void navigateToRestaurantDetails(SearchController.RistoranteOggetto ristorante) {
        if (stage != null && stage.getScene() != null) {
            cachedSearchView = stage.getScene().getRoot();
            cachedSearchTitle = stage.getTitle().replace("TheKnife — ", "");
        }

        if (currentRoute != null && currentRoute.contains("search-view")) {
            rememberSearchRoute(currentRoute, currentTitle != null ? currentTitle : "Cerca Ristoranti");
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
        if (lastSearchRoute != null && !lastSearchRoute.isBlank()) {
            String route = lastSearchRoute;
            String title = lastSearchTitle != null ? lastSearchTitle : "Cerca Ristoranti";

            if (route.endsWith("search-view-logged.fxml") && lastSearchQuery != null && !lastSearchQuery.isBlank()) {
                navigateToSearchWithQueryLogged(lastSearchQuery);
            } else if (route.endsWith("search-view-logged.fxml") && (lastSearchCitta != null || lastSearchPrezzo != null || lastSearchStelle != null || lastSearchOrdine != null)) {
                navigateToSearchWithAdvancedFiltersLogged(lastSearchCitta, lastSearchPrezzo, lastSearchStelle, lastSearchOrdine);
            } else if (route.endsWith("search-view.fxml") && lastSearchQuery != null && !lastSearchQuery.isBlank()) {
                navigateToSearchWithQuery(lastSearchQuery);
            } else if (route.endsWith("search-view.fxml") && (lastSearchCitta != null || lastSearchPrezzo != null || lastSearchStelle != null || lastSearchOrdine != null)) {
                navigateToSearchWithAdvancedFilters(lastSearchCitta, lastSearchPrezzo, lastSearchStelle, lastSearchOrdine);
            } else {
                navigateTo(route, title);
            }
            System.out.println("[NAVIGATOR] Ritorno alla pagina di ricerca memorizzata: " + route);
            return;
        }

        if (cachedSearchView != null) {
            updateSceneRoot(cachedSearchView, cachedSearchTitle);
            System.out.println("[NAVIGATOR] Schermata di ricerca precedente ripristinata con successo.");
        } else if (idUtenteLoggato != -1) {
            navigateTo("search-view-logged.fxml", "Cerca Ristoranti");
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
            navigateToLoginWithReturn(currentRoute != null ? currentRoute : "home-view.fxml", currentTitle != null ? currentTitle : "Home");
            return;
        } else if ("CLIENTE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("customer-profile-view.fxml", "Il Mio Profilo");
        } else if ("GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("owner-profile-view.fxml", "Dashboard Ristoratore");
        }
    }

    /**
     * Forza il ricaricamento della route corrente ricreando la view (utile dopo
     * cambi di stato globali come login/logout che devono aggiornare la navbar).
     */
    public void reloadCurrentRoute() {
        if (this.currentRoute == null || this.currentRoute.isBlank()) return;
        // Ricarica la stessa route senza registrare la navigazione nella history
        navigateToInternal(this.currentRoute, this.currentTitle != null ? this.currentTitle : "", false);
    }

/**
 * Method: isGuest
 * Purpose: describe what this method does, its inputs and observable effects.
 * Parameters: document important parameters and expected formats.
 * Returns: describe the return value or side-effects.
 */
    public String getCurrentRoute() {
        return currentRoute;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

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
            navigateToLoginWithReturn(currentRoute != null ? currentRoute : "home-view.fxml", currentTitle != null ? currentTitle : "Home");
            return;
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