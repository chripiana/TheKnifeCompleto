package project.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;

import project.client.services.ServerApiClient;

public class Navigator {
    private static Navigator instance;
    private Stage stage;

    private int idUtenteLoggato = -1;
    private String ruoloUtenteLoggato = null;

    private Parent cachedSearchView = null;
    private String cachedSearchTitle = "Risultati Ricerca";

    private Navigator() {
        System.out.println("[NAVIGATOR] Client inizializzato: nessun accesso diretto al database. Tutte le richieste passano tramite il server.");
    }

    public static synchronized Navigator getInstance() {
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public int getIdUtenteLoggato() {
        return idUtenteLoggato;
    }

    public void setIdUtenteLoggato(int idUtenteLoggato) {
        this.idUtenteLoggato = idUtenteLoggato;
    }

    public String getRuoloUtenteLoggato() {
        return ruoloUtenteLoggato;
    }

    public void setRuoloUtenteLoggato(String ruoloUtenteLoggato) {
        this.ruoloUtenteLoggato = ruoloUtenteLoggato;
    }

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


    public void navigateToHome() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("home-view.fxml", "Trova il tuo ristorante");
        } else if ("GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("home-view-owner.fxml", "Home Ristoratore");
        } else {
            navigateTo("home-view-logged.fxml", "Benvenuto su TheKnife");
        }
    }

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


    public void backToSearchResults() {
        if (cachedSearchView != null) {
            updateSceneRoot(cachedSearchView, cachedSearchTitle);
            System.out.println("[NAVIGATOR] Schermata di ricerca precedente ripristinata con successo.");
        } else {
            navigateTo("search-view.fxml", "Cerca");
        }
    }


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

    public void navigateToProfile() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("login-view.fxml", "Accedi");
        } else if ("CLIENTE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("customer-profile-view.fxml", "Il Mio Profilo");
        } else if ("GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato)) {
            navigateTo("owner-profile-view.fxml", "Dashboard Ristoratore");
        }
    }

    public boolean isGuest() {
        return this.idUtenteLoggato == -1;
    }

    public boolean isLoggedCustomer() {
        return this.idUtenteLoggato > 0 && "CLIENTE".equalsIgnoreCase(this.ruoloUtenteLoggato);
    }

    public boolean isLoggedOwner() {
        return this.idUtenteLoggato > 0 && "GESTORE".equalsIgnoreCase(this.ruoloUtenteLoggato);
    }

    public void navigateToReservations() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("login-view.fxml", "Accedi");
        } else if (isLoggedCustomer()) {
            navigateTo("reservations-view.fxml", "Le mie prenotazioni");
        } else {
            navigateToHomeIntelligent();
        }
    }

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