/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 *
 * @author sakit
 */
public class ClientThread extends Thread {

    private static int counter = 0;
    private TextArea textArea;
    private TextField textField;
    private String clientName;
    private Socket socket;
    private final ObjectOutputStream output;
    private ObjectInputStream input;

    public ClientThread(Socket s, TextArea ta, TextField tf, String name) throws IOException {
        ++counter;
        socket = s;
        textArea = ta;
        textField = tf;
        clientName = name;
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();

        input = new ObjectInputStream(socket.getInputStream());

        textArea = ta;
        textArea.setEditable(false);

        textField = tf;
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendData(textField.getText());
                textField.setText("");
            }
        });
        displayMessage(clientName+" connected to the server");
        setName(clientName);
    }

    public void startClientThread(){
        start();
    }
    
    @Override
    public void run() {
        String message = null;
        boolean terminate = false;

        try {
            while (!terminate) {
                message = (String) input.readObject();
                terminate = message.contains("@@@@@");
                if (terminate) {
                    displayMessage("\n" + message.substring(0, message.indexOf("@@@@@")) + " leave the server");
                    --counter;
                    break;
                } else {
                    displayMessage(message);
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            System.err.println("read/write process clientName:" + clientName + " err:" + ex.getMessage());
        } finally {
            clear();
        }
    }

    private synchronized void displayMessage(final String message) {
        Platform.runLater(() -> {
            textArea.appendText(message + "\n");
        });
    }

    private synchronized void sendData(String message) {
        try {
            message = "Server>" + message;
            output.writeObject(message);
            output.flush();
            displayMessage(message);
        } catch (IOException ex) {
            System.err.println("sendData err clientName:"+clientName+" error:"+ex.getMessage());
        }
    }

    private void clear() {
        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.err.println("clearing process error:" + ex.getMessage());
        }
    }
}
