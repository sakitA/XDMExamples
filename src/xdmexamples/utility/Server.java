/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author sakit
 */
public class Server {

    private final int portNumber = 9000;
    private int maxClientCount;
    private ServerSocket server;
    private Socket clientSocket;
    private TextArea textArea;
    private TextField textField;
    private String clientName;

    private boolean running;

    public Server(TextArea area, TextField field, int maxClientCount) {
        this.maxClientCount = maxClientCount;
        textArea = area;
        textField = field;
    }

    public boolean isServerRunnning() {
        return running;
    }

    public void startServer() {
        try {
            server = new ServerSocket(portNumber, maxClientCount);
            displayMessage("Server is running. There is no active client");
            displayMessage("Server is waiting a client");
            running = true;
        } catch (IOException ex) {
            System.err.println("startServer error:" + ex.getMessage());
        }
        run();
    }

    private void run() {
        try {
            while (isServerRunnning()) {
                clientSocket = server.accept();
                ClientThread client = new ClientThread(clientSocket, textArea, textField, clientName);
                client.startClientThread();
            }
        } catch (IOException e) {
            if (!isServerRunnning()) {
                clientSocket = null;
            }
            throw new RuntimeException(
                    "Error accepting client connection", e);
        } finally {
            try {
                server.close();
            } catch (IOException ex) {
                System.err.println("stopping server error:" + ex.getMessage());
            }
        }
    }

    public synchronized void setName(String name) {
        clientName = name;
    }

    private synchronized void displayMessage(final String message) {
        Platform.runLater(() -> {
            textArea.appendText(message + "\n");
        });
    }

    public synchronized void stopServer() {
        running = false;
        if (!server.isClosed()) {
            try {
                if (server != null) {
                    server.close();
                    server = null;
                }
            } catch (IOException ex) {
                System.err.println("server stopping error:" + ex.getMessage());
            }
            displayMessage("Server is stopped");
        }
    }
}
