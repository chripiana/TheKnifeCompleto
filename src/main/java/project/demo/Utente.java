package project.demo;

import java.time.LocalDate;

public class Utente {
    private String nome;
    private String cognome;
    private final String username;
    private final String email;
    private final String password;
    private String domicilio;
    private LocalDate dataNascita;
    private final String tipoAccount; // "CLIENTE" o "RISTORATORE"

    public Utente(String nome, String cognome, String username, String email, String password, String domicilio, LocalDate dataNascita, String tipoAccount) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.domicilio = domicilio;
        this.dataNascita = dataNascita;
        this.tipoAccount = tipoAccount;
    }

    // Getter e Setter
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public LocalDate getDataNascita() { return dataNascita; }
    public void setDataNascita(LocalDate dataNascita) { this.dataNascita = dataNascita; }
    public String getTipoAccount() { return tipoAccount; }
}