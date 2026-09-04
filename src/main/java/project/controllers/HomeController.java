package project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * HomeController
 *
 * Controller della home page pubblica (non loggata). Espone hander per la
 * ricerca globale, filtri e scorciatoie per categorie di cucina.
 **/
public class HomeController {

    /** Campo di ricerca principale (bindato dalla FXML).*/
    @FXML private TextField searchField;
    /** Campo di filtro città (bindato dalla FXML).*/
    @FXML private TextField filterCitta;
    /** Campo di filtro prezzo (bindato dalla FXML).*/
    @FXML private TextField filterPrezzo;
    /** ComboBox per filtrare per stelle (bindato dalla FXML).*/
    @FXML private ComboBox<String> filterStelle;
    /** ComboBox per scegliere l'ordine dei risultati (bindato dalla FXML).*/
    @FXML private ComboBox<String> filterOrdine;


    @FXML
    private void onGlobalSearch() {
        String testoCercato = searchField.getText().trim();
        System.out.println("[HOME] Ricerca globale per: " + testoCercato);

        // Passa il controllo al Navigator verso la search-view
        Navigator.getInstance().navigateToSearchWithQuery(testoCercato);
    }


    @FXML
    private void onCucinaItalianaClick() {
        System.out.println("[HOME] Categoria Rapida: Italiana");
        Navigator.getInstance().navigateToSearchWithQuery("Italian");
    }

    @FXML
    private void onCucinaGiapponeseClick() {
        System.out.println("[HOME] Categoria Rapida: Giapponese");
        Navigator.getInstance().navigateToSearchWithQuery("Japanese");
    }

    @FXML
    private void onCucinaFranceseClick() {
        System.out.println("[HOME] Categoria Rapida: Francese");
        Navigator.getInstance().navigateToSearchWithQuery("French");
    }

    @FXML
    private void onCucinaGrillsClick() {
        System.out.println("[HOME] Categoria Rapida: Meats & Grills");
        Navigator.getInstance().navigateToSearchWithQuery("Meats and Grills");
    }

    @FXML
    private void onCucinaMediterraneaClick() {
        System.out.println("[HOME] Categoria Rapida: Mediterranea");
        Navigator.getInstance().navigateToSearchWithQuery("Mediterranean Cuisine");
    }


    @FXML
    private void onApplicaFiltriClick() {
        String citta = (filterCitta != null) ? filterCitta.getText().trim() : "";
        String prezzo = (filterPrezzo != null) ? filterPrezzo.getText().trim() : "";
        String stelle = (filterStelle != null) ? filterStelle.getValue() : null;
        String ordine = (filterOrdine != null) ? filterOrdine.getValue() : null;

        System.out.println("[HOME] Applicazione filtri avanzati -> Città: " + citta +
                ", Prezzo Max: " + prezzo + ", Stelle: " + stelle + ", Ordine: " + ordine);


        Navigator.getInstance().navigateToSearchWithAdvancedFilters(citta, prezzo, stelle, ordine);
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        Navigator.getInstance().navigateToHomeIntelligent();
    }

    @FXML
    private void goToLogin() {
        System.out.println("[HOME] Spostamento alla pagina Login");
        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
    }

    @FXML
    private void visualizzaPreferiti() {
        System.out.println("[HOME] Spostamento alla pagina Preferiti");
        Navigator.getInstance().navigateTo("favorites-view.fxml", "I tuoi preferiti");
    }

    @FXML
    private void handleGoToProfile(javafx.scene.input.MouseEvent event) {
        Navigator.getInstance().navigateToProfile();
    }
}