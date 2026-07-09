package project.demo;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class HomeController {

    @FXML private TextField searchField;
    @FXML private TextField filterCitta;
    @FXML private TextField filterPrezzo;
    @FXML private ComboBox<String> filterStelle;
    @FXML private ComboBox<String> filterOrdine;

    /**
     * 1. BARRA DI RICERCA GLOBALE (Hero Section)
     * Reindirizza alla pagina dei risultati passando la stringa digitata
     */
    @FXML
    private void onGlobalSearch() {
        String testoCercato = searchField.getText().trim();
        System.out.println("[HOME] Ricerca globale per: " + testoCercato);

        // Passa il controllo al Navigator verso la search-view
        Navigator.getInstance().navigateToSearchWithQuery(testoCercato);
    }

    /**
     * 2. SCELTA RAPIDA CUCINE (I 5 Bottoni delle categorie sotto la barra)
     * Ciascuno chiama il Navigator passando la query specifica della cucina
     */
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

    /**
     * 3. PANNELLO FILTRI AVANZATI (Bottone "Applica Filtri ed Esplora")
     * Raccoglie i valori inseriti dall'utente e li manda alla search-view
     */
    @FXML
    private void onApplicaFiltriClick() {
        String citta = (filterCitta != null) ? filterCitta.getText().trim() : "";
        String prezzo = (filterPrezzo != null) ? filterPrezzo.getText().trim() : "";
        String stelle = (filterStelle != null) ? filterStelle.getValue() : null;
        String ordine = (filterOrdine != null) ? filterOrdine.getValue() : null;

        System.out.println("[HOME] Applicazione filtri avanzati -> Città: " + citta +
                ", Prezzo Max: " + prezzo + ", Stelle: " + stelle + ", Ordine: " + ordine);

        // Invia i dati al Navigator per processare la query combinata nella search-view
        Navigator.getInstance().navigateToSearchWithAdvancedFilters(citta, prezzo, stelle, ordine);
    }

    @FXML
    private void handleGoToHome(javafx.scene.input.MouseEvent event) {
        project.demo.Navigator.getInstance().navigateToHome();
    }

    /**
     * 4. NAVBAR (Bottone Accedi)
     */
    @FXML
    private void goToLogin() {
        System.out.println("[HOME] Spostamento alla pagina Login");
        Navigator.getInstance().navigateTo("login-view.fxml", "Accedi");
    }
}