package project.shared.models;

import java.io.Serializable;

/**
 * Configurazione di connessione al server
 * Condivisa tra client e server
 */
public class ServerConnection implements Serializable {
    private static final long serialVersionUID = 1L;

    private String host;
    private int port;
    private int dbPort;
    private String databaseName;
    private String user;
    private String password;

    public ServerConnection() {
        this.host = "localhost";
        this.port = 8080;
        this.dbPort = 5432;
        this.databaseName = "theknife";
        this.user = "postgres";
        this.password = "";
    }

    public ServerConnection(String host, int port, int dbPort, String databaseName, String user, String password) {
        this.host = host;
        this.port = port;
        this.dbPort = dbPort;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public int getDbPort() { return dbPort; }
    public String getDatabaseName() { return databaseName; }
    public String getUser() { return user; }
    public String getPassword() { return password; }

    // Setters
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setDbPort(int dbPort) { this.dbPort = dbPort; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public void setUser(String user) { this.user = user; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "ServerConnection{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", dbPort=" + dbPort +
                ", databaseName='" + databaseName + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
