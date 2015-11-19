/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.EOFException;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

/**
 *
 * @author sakit
 */
public class Client {

    private final ResourceBundle rb = ResourceBundle.getBundle("resources.bundles.Bundle", Locale.getDefault());
    private final static String host = "127.0.0.1";
    private final static int portNumber = 9000;
    private TextArea textArea;
    private TextField textField;
    private String clientName;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket connection;
    private boolean stop;

    public Client(Tab tab) {

        tab.setOnCloseRequest(e -> {
            System.out.println("in tab close request");
            stopClient();
        });

        VBox vbox = (VBox) tab.getContent();

        textField = (TextField) vbox.getChildren().get(0);
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendData(textField.getText());
                textField.setText("");
            }
        });

        textArea = (TextArea) vbox.getChildren().get(1);
        textArea.setEditable(false);

        clientName = tab.getText();
        stop = false;
    }

    public void runClient() {
        try {
            connectClient();
            getStreams();
            processConnection();
            closeClient();
        } catch (IOException ex) {
            Platform.runLater(() -> {
                showErrorMessage(ex.getMessage(), ex);
            });
        }
    }

    private synchronized void displayMessage(String text) {
        Platform.runLater(() -> {
            textArea.appendText(text + "\n");
        });
    }

    //step 1: connect to the client
    private void connectClient() throws IOException {
        displayMessage(clientName + " attempting connect to the server");
        connection = new Socket(host, portNumber);
        displayMessage(clientName + " connected to " + connection.getInetAddress());
    }

    //step 2: get input/output streams
    private void getStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();

        input = new ObjectInputStream(connection.getInputStream());
    }

    //step 3: read from and write to socket
    private void processConnection() throws IOException {
        String message = null;
        while (!stop) {
            try {
                message = (String) input.readObject();
                displayMessage(message);
            } catch (ClassNotFoundException ex) {
                Platform.runLater(() -> {
                    showErrorMessage(ex.getMessage(), ex);
                });
            } catch (EOFException eofe) {
                System.err.println(clientName + " eofe:" + eofe.getMessage());
            }
        }
    }

    //step 4: close onnection
    private synchronized void closeClient() throws IOException {
        if (output != null) {
            output.close();
        }
        if (input != null) {
            input.close();
        }
        if (connection.isConnected()) {
            connection.close();
        }
    }

    public synchronized void stopClient() {
        stop = true;
        sendData(Keys.TERMINATE);
    }

    private synchronized void sendData(String text) {
        String message = null;
        try {
            message = clientName + ">" + text;
            displayMessage(message);
            output.writeObject(message);
            output.flush();
        } catch (IOException ex) {
            Platform.runLater(() -> {
                showErrorMessage(ex.getMessage(), ex);
            });
        }
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
