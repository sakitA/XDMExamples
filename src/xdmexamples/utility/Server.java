/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import xdmexamples.controller.FxmlClientServerController;

/**
 *
 * @author sakit
 */
public class Server {

    private final int portNumber = 9000;
    private static int maxClientCount;
    private ServerSocket server;
    private Socket clientSocket;
    private TextArea textArea;
    private TextField textField;
    private String clientName;
    private static ClientThread[] threads;

    private boolean running;

    public Server(TextArea area, TextField field, int maxClientCount) {
        this.maxClientCount = maxClientCount;
        textArea = area;
        textField = field;
        textField.setOnKeyPressed(e->{
            if(e.getCode()==KeyCode.ENTER){
                String tabName = FxmlClientServerController.selectedtab;
                for(int i=0;i<this.maxClientCount;i++){
                    if(threads[i]!=null && threads[i].getName().equals(tabName))
                        threads[i].sendData(textField.getText());                             
                }
                textField.setText("");
            }
        });
        threads = new ClientThread[maxClientCount];
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
                int index = ClientThread.getClientCount();
                threads[index] = new ClientThread(clientSocket, textArea, textField, clientName);
                threads[index].startClientThread();
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

    public void setName(String name) {
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
