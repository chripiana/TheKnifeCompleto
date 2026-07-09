package project.demo;

import db.DatabaseManager;
import db.TheKnifeDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

public class Navigator {
    private static Navigator instance;
    private Stage stage;
    private TheKnifeDAO dao;

    // Variabili di sessione per l'utente loggato
    private int idUtenteLoggato = -1;
    private String ruoloUtenteLoggato = null;

    // --- CACHING STATO DELLA RICERCA ---
    private Parent cachedSearchView = null;
    private String cachedSearchTitle = "Risultati Ricerca";

    private Navigator() {
        try {
            DatabaseManager.initialize();
            Connection conn = DatabaseManager.getConnection();
            dao = new TheKnifeDAO(conn);
            System.out.println("[NAVIGATOR] Connessione a PostgreSQL e DAO inizializzati con successo.");
        } catch (SQLException e) {
            System.err.println("[NAVIGATOR] Errore critico durante l'inizializzazione del database!");
            e.printStackTrace();
        }
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

    public TheKnifeDAO getDao() {
        return dao;
    }

    public int getIdUtenteLoggato() { return idUtenteLoggato; }
    public void setIdUtenteLoggato(int idUtenteLoggato) { this.idUtenteLoggato = idUtenteLoggato; }
    public String getRuoloUtenteLoggato() { return ruoloUtenteLoggato; }
    public void setRuoloUtenteLoggato(String ruoloUtenteLoggato) { this.ruoloUtenteLoggato = ruoloUtenteLoggato; }

    public void logout() {
        this.idUtenteLoggato = -1;
        this.ruoloUtenteLoggato = null;
        System.out.println("[NAVIGATOR] Utente disconnesso.");
    }

    /**
     * Algoritmo di scansione dinamica dei percorsi FXML.
     * Cerca il file in ordine di priorità nelle varie cartelle del progetto.
     */
    private URL risolviPercorsoFXML(String fxmlFile) {
        if (fxmlFile == null || fxmlFile.trim().isEmpty()) return null;

        // Pulizia del nome del file da eventuali percorsi parziali già inseriti
        String nomeFilePuro = fxmlFile.substring(fxmlFile.lastIndexOf("/") + 1);

        // Array dei possibili percorsi in cui la tua gerarchia dichiara i file FXML
        String[] tentativiPercorso = {
                "/project/demo/NotLoggedUser/" + nomeFilePuro,
                "/project/demo/LoggedUser/" + nomeFilePuro,
                "/project/demo/OwnerUser/" + nomeFilePuro
        };

        // Scansione attiva dei percorsi nel Classpath
        for (String percorso : tentativiPercorso) {
            URL urlTrovato = getClass().getResource(percorso);
            if (urlTrovato != null) {
                return urlTrovato; // Ritorna il primo percorso valido che esiste fisicamente
            }
        }
        return null;
    }


    /**
     * Naviga alla Home corretta (pubblica o loggata) in base alla sessione corrente.
     */
    public void navigateToHome() {
        if (this.idUtenteLoggato == -1) {
            navigateTo("home-view.fxml", "Trova il tuo ristorante");
        } else {
            navigateTo("home-view-logged.fxml", "Benvenuto su TheKnife");
        }
    }
    /**
     * Navigazione generica (es. Login, Home, ecc.)
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
     * Navigazione alla schermata di ricerca globale tramite stringa testuale
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
     * Navigazione alla ricerca avanzata tramite filtri combinati
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

    public void navigateToSearchWithAdvancedFiltersLogged(String citta, String prezzoMax, String stelle, String ordine) {
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
     * Naviga ai dettagli del ristorante salvando preventivamente lo stato visivo corrente
     */
    public void navigateToRestaurantDetails(SearchController.RistoranteOggetto ristorante) {
        // Salva la schermata dei risultati prima di sovrascriverla
        if (stage != null && stage.getScene() != null) {
            cachedSearchView = stage.getScene().getRoot();
            cachedSearchTitle = stage.getTitle().replace("TheKnife — ", "");
        }

        URL fxmlUrl = risolviPercorsoFXML("restaurant-details-view.fxml");
        if (fxmlUrl == null) {
            System.err.println("[ERRORE CRITICO] Impossibile trovare restaurant-details-view.fxml");
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
     * Torna indietro istantaneamente ripristinando la ricerca precedente così com'era rimasta
     */
    public void backToSearchResults() {
        if (cachedSearchView != null) {
            updateSceneRoot(cachedSearchView, cachedSearchTitle);
            System.out.println("[NAVIGATOR] Schermata di ricerca precedente ripristinata con successo.");
        } else {
            navigateTo("search-view.fxml", "Cerca");
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
        stage.show();
    }


}