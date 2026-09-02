package project.server;

import db.DatabaseManager;
import db.DatabaseInitializer;
import project.shared.models.ServerConnection;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server TheKnife - Ascolta i client e gestisce le richieste
 * Il server ha accesso ESCLUSIVO al database
 */
public class TheKnifeServer {
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private static final int DEFAULT_BACKLOG = 50;
    private final int port;
    private final ServerConnection dbConfig;
    private final String bindHost;
    private final int maxClients;
    private final int backlog;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running;

    public TheKnifeServer(int port, ServerConnection dbConfig) {
        this(port, dbConfig, "0.0.0.0", DEFAULT_THREAD_POOL_SIZE, DEFAULT_BACKLOG);
    }

    public TheKnifeServer(int port, ServerConnection dbConfig, String bindHost) {
        this(port, dbConfig, bindHost, DEFAULT_THREAD_POOL_SIZE, DEFAULT_BACKLOG);
    }

    public TheKnifeServer(int port, ServerConnection dbConfig, String bindHost, int maxClients, int backlog) {
        this.port = port;
        this.dbConfig = dbConfig;
        this.bindHost = bindHost;
        this.maxClients = Math.max(1, maxClients);
        this.backlog = Math.max(1, backlog);
        this.threadPool = Executors.newFixedThreadPool(this.maxClients);
    }

    /**
     * Avvia il server
     */
    public void start() {
        try {
            running = true;
            serverSocket = new java.net.ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(bindHost), port), backlog);
            System.out.println("[Server] Avviato sulla porta " + port + " su " + bindHost + " (max clients=" + maxClients + ", backlog=" + backlog + ")");

            DatabaseManager.configure(
                dbConfig.getHost(),
                dbConfig.getDbPort(),
                dbConfig.getDatabaseName(),
                dbConfig.getUser(),
                dbConfig.getPassword()
            );
            DatabaseManager.initialize();
            System.out.println("[Server] Database connesso: " + dbConfig.getDatabaseName());

            ServerStatusRegistry.clear();
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Nuovo client connesso: " + clientSocket.getInetAddress());
                ServerStatusRegistry.addClient(clientSocket.getInetAddress().getHostAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[Server] Errore: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            shutdown();
        }
    }

    /**
     * Arresta il server
     */
    public void stop() {
        running = false;
        shutdown();
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[Server] Errore durante l'arresto: " + e.getMessage());
        } finally {
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
            System.out.println("[Server] Arresto completato");
        }
    }

}
