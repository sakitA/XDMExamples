/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

/**
 *
 * @author sakit
 */
public class ClientThread extends Thread {

    private final ResourceBundle rb = ResourceBundle.getBundle("resources.bundles.Bundle", Locale.getDefault());
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
        displayMessage(clientName+rb.getString(Keys.CS_LOG5));
        displayMessage(rb.getString(Keys.CS_LOG6)+counter);
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
                terminate = message.contains(Keys.TERMINATE);
                if (terminate) {
                    displayMessage("\n" + message.substring(0, message.indexOf(Keys.TERMINATE)-1) + rb.getString(Keys.CS_LOG7));
                    --counter;
                    displayMessage(rb.getString(Keys.CS_LOG6)+counter);
                    break;
                } else {
                    displayMessage(message);
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            Platform.runLater(()->{showErrorMessage(ex.getMessage(), ex);});
        } finally {
            clear();
        }
    }

    private synchronized void displayMessage(final String message) {
        Platform.runLater(() -> {
            textArea.appendText(message + "\n");
        });
    }

    public synchronized void sendData(String message) {
        try {
            message = rb.getString(Keys.CS_SER) + message;
            output.writeObject(message);
            output.flush();
            displayMessage(message);
        } catch (IOException ex) {
            Platform.runLater(()->{showErrorMessage(ex.getMessage(), ex);});
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
            Platform.runLater(()->{showErrorMessage(ex.getMessage(), ex);});
        }
    }
    
    public static int getClientCount(){
        return counter;
    }
    
    private void showErrorMessage(String message, Throwable... excp) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(rb.getString(Keys.ERROR));
        alert.setHeaderText(rb.getString(Keys.PRG_NAME));
        alert.setContentText(message);
        Stage st = (Stage) alert.getDialogPane().getScene().getWindow();
        st.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/azer_emblem.png")));
        if (excp.length > 0) {
            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            excp[0].printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label(rb.getString(Keys.EXCEPTİON_LABEL));

            TextArea ta = new TextArea(exceptionText);
            ta.setEditable(false);
            ta.setWrapText(true);

            ta.setMaxWidth(Double.MAX_VALUE);
            ta.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(ta, Priority.ALWAYS);
            GridPane.setHgrow(ta, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(ta, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);
        }
        alert.showAndWait();
    }
}
