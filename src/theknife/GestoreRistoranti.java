package theknife;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GestoreRistoranti {
    private List<Ristorante> ristoranti;
    private static final String FILE_RISTORANTI = "../data/ristoranti.csv";
    private static final String FILE_RECENSIONI = "../data/recensioni.txt";

    public GestoreRistoranti() {
        this.ristoranti = new ArrayList<>();
    }

    /**
     * Carica i ristoranti da file
     */
    public void caricaRistoranti() throws IOException {
        caricaRistorantiDaCSV();
        // Carica le recensioni
        caricaRecensioni();
    }

    /**
     * Carica i ristoranti dal file CSV fornito dal docente
     */
    private void caricaRistorantiDaCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("../data/ristoranti.csv"))) {
            String line = reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> fields = parseCSVLine(line);
                if (fields.size() >= 14) {
                    try {
                        String nome = cleanField(fields.get(0));
                        String indirizzo = cleanField(fields.get(1));
                        String location = cleanField(fields.get(2));
                        String[] locationParts = location.split(",");
                        String citta = locationParts.length > 0 ? locationParts[0].trim() : "Unknown";
                        String nazione = locationParts.length > 1 ? locationParts[1].trim() : "Unknown";

                        // Gestione prezzo - converti da formato €€€€ a numero
                        String prezzoStr = cleanField(fields.get(3));
                        double prezzoMedio = convertPrezzoToNumber(prezzoStr);

                        String tipoCucina = cleanField(fields.get(4));

                        // Coordinate geografiche
                        double longitudine = parseDouble(cleanField(fields.get(5)), 0.0);
                        double latitudine = parseDouble(cleanField(fields.get(6)), 0.0);

                        // Valori derivati dai servizi
                        String servizi = cleanField(fields.get(12));
                        boolean delivery = servizi.toLowerCase().contains("delivery") ||
                                servizi.toLowerCase().contains("takeaway");
                        boolean prenotazione = servizi.toLowerCase().contains("reservation") ||
                                servizi.toLowerCase().contains("booking") ||
                                Math.random() > 0.3; // Default probabilistico

                        Ristorante ristorante = new Ristorante(nome, nazione, citta, indirizzo,
                                latitudine, longitudine, prezzoMedio, delivery, prenotazione, tipoCucina);
                        ristoranti.add(ristorante);
                    } catch (Exception e) {
                        System.err.println("Errore nel parsing della riga: " + line);
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("File CSV non trovato, continuazione con dati vuoti");
        }
    }

    /**
     * Parser CSV che gestisce correttamente le virgole all'interno delle virgolette
     */
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields;
    }


    /**
     * Pulisce un campo rimuovendo virgolette e spazi extra
     */
    private String cleanField(String field) {
        if (field == null) return "";
        return field.replaceAll("^\"|\"$", "").trim();
    }

    /**
     * Converte il formato prezzo €€€€ in numero
     */
    private double convertPrezzoToNumber(String prezzoStr) {
        if (prezzoStr == null || prezzoStr.isEmpty()) return 50.0;

        int euroCount = 0;
        for (char c : prezzoStr.toCharArray()) {
            if (c == '€') euroCount++;
        }

        switch (euroCount) {
            case 1: return 25.0;  // €
            case 2: return 45.0;  // €€
            case 3: return 70.0;  // €€€
            case 4: return 100.0; // €€€€
            default: return 50.0;
        }
    }

    /**
     * Parse sicuro di double
     */
    private double parseDouble(String str, double defaultValue) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Carica le recensioni da file
     */
    private void caricaRecensioni() throws IOException {
        File file = new File(FILE_RECENSIONI);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    String idRistorante = parts[0];
                    Recensione recensione = Recensione.fromString(parts[1]);

                    if (recensione != null) {
                        Ristorante ristorante = getRistoranteById(idRistorante);
                        if (ristorante != null) {
                            ristorante.aggiungiRecensione(recensione);
                        }
                    }
                }
            }
        }
    }

    /**
     * Salva i ristoranti su file
     */
    public void salvaRistoranti() throws IOException {
        File dir = new File("../data/ristoranti.csv");
        if (!dir.exists()) dir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_RISTORANTI))) {
            for (Ristorante ristorante : ristoranti) {
                writer.println(ristorante.toString());
            }
        }

        // Salva le recensioni
        salvaRecensioni();
    }

    /**
     * Salva le recensioni su file
     */
    private void salvaRecensioni() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_RECENSIONI))) {
            for (Ristorante ristorante : ristoranti) {
                for (Recensione recensione : ristorante.getRecensioni()) {
                    writer.println(ristorante.getId() + "|" + recensione.toString());
                }
            }
        }
    }

    /**
     * Aggiunge un nuovo ristorante
     */
    public void aggiungiRistorante(Ristorante ristorante) {
        if (ristorante == null) {
            throw new IllegalArgumentException("Il ristorante non può essere null");
        }

        // Verifica che non esista già un ristorante con lo stesso ID
        if (getRistoranteById(ristorante.getId()) != null) {
            throw new IllegalArgumentException("Esiste già un ristorante con ID: " + ristorante.getId());
        }

        ristoranti.add(ristorante);
    }

    /**
     * Rimuove un ristorante
     */
    public boolean rimuoviRistorante(String id) {
        return ristoranti.removeIf(r -> r.getId().equals(id));
    }

    /**
     * Aggiorna un ristorante esistente
     */
    public boolean aggiornaRistorante(Ristorante ristoranteAggiornato) {
        if (ristoranteAggiornato == null) return false;

        for (int i = 0; i < ristoranti.size(); i++) {
            if (ristoranti.get(i).getId().equals(ristoranteAggiornato.getId())) {
                ristoranti.set(i, ristoranteAggiornato);
                return true;
            }
        }
        return false;
    }

    /**
     * Cerca ristoranti in base ai filtri
     */
    public List<Ristorante> cercaRistoranti(FiltroRicerca filtro) {
        if (filtro == null) return new ArrayList<>(ristoranti);

        return ristoranti.stream()
                .filter(r -> filtraRistorante(r, filtro))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se un ristorante rispetta i filtri
     */
    private boolean filtraRistorante(Ristorante ristorante, FiltroRicerca filtro) {
        // Filtro per luogo (obbligatorio)
        if (filtro.getLuogo() != null && !filtro.getLuogo().isEmpty()) {
            String luogo = filtro.getLuogo().toLowerCase();
            if (!ristorante.getCitta().toLowerCase().contains(luogo) &&
                    !ristorante.getNazione().toLowerCase().contains(luogo) &&
                    !ristorante.getIndirizzo().toLowerCase().contains(luogo)) {
                return false;
            }
        }

        // Filtro per tipo di cucina
        if (filtro.getTipoCucina() != null && !filtro.getTipoCucina().isEmpty()) {
            if (!ristorante.getTipoCucina().toLowerCase().contains(filtro.getTipoCucina().toLowerCase())) {
                return false;
            }
        }

        // Filtro per prezzo minimo
        if (filtro.getPrezzoMin() != null) {
            if (ristorante.getFasciaPrezzo() < filtro.getPrezzoMin()) {
                return false;
            }
        }

        // Filtro per prezzo massimo
        if (filtro.getPrezzoMax() != null) {
            if (ristorante.getFasciaPrezzo() > filtro.getPrezzoMax()) {
                return false;
            }
        }

        // Filtro per delivery
        if (filtro.getDelivery() != null) {
            if (ristorante.isDelivery() != filtro.getDelivery()) {
                return false;
            }
        }

        // Filtro per prenotazione online
        if (filtro.getPrenotazioneOnline() != null) {
            if (ristorante.isPrenotazioneOnline() != filtro.getPrenotazioneOnline()) {
                return false;
            }
        }

        // Filtro per media stelle
        if (filtro.getMediaMinStelle() != null) {
            if (ristorante.getMediaStelle() < filtro.getMediaMinStelle()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Restituisce i ristoranti vicini a un luogo
     */
    public List<Ristorante> getRistorantiVicini(String luogo) {
        if (luogo == null || luogo.isEmpty()) {
            return new ArrayList<>();
        }

        return ristoranti.stream()
                .filter(r -> r.getCitta().toLowerCase().contains(luogo.toLowerCase()) ||
                        r.getNazione().toLowerCase().contains(luogo.toLowerCase()) ||
                        r.getIndirizzo().toLowerCase().contains(luogo.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce un ristorante per ID
     */
    public Ristorante getRistoranteById(String id) {
        if (id == null) return null;

        return ristoranti.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Restituisce i ristoranti di un proprietario
     */
    public List<Ristorante> getRistorantiDi(String proprietario) {
        if (proprietario == null) return new ArrayList<>();

        return ristoranti.stream()
                .filter(r -> proprietario.equals(r.getProprietario()))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i ristoranti che hanno recensioni di un utente
     */
    public List<Ristorante> getRistorantiConRecensioneDi(String username) {
        if (username == null) return new ArrayList<>();

        return ristoranti.stream()
                .filter(r -> r.getRecensione(username) != null)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutti i ristoranti
     */
    public List<Ristorante> getTuttiRistoranti() {
        return new ArrayList<>(ristoranti);
    }

    /**
     * Restituisce il numero totale di ristoranti
     */
    public int getNumeroRistoranti() {
        return ristoranti.size();
    }

    /**
     * Verifica se esistono ristoranti
     */
    public boolean hasRistoranti() {
        return !ristoranti.isEmpty();
    }
}