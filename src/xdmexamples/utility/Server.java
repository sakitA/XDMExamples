/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import xdmexamples.controller.FxmlClientServerController;
import xdmexamples.preloader.Keys;

/**
 *
 * @author sakit
 */
public class Server {

    private final ResourceBundle rb = ResourceBundle.getBundle("resources.bundles.Bundle", Locale.getDefault());
    private final int portNumber = 9000;
    private static int maxClientCount;
    private ServerSocket server;
    private Socket clientSocket;
    private final TextArea textArea;
    private TextField textField;
    private String clientName;
    private static ClientThread[] threads;

    private boolean running;

    public Server(TextArea area, TextField field, int maxClientCount) {
        Server.maxClientCount = maxClientCount;
        textArea = area;
        textField = field;
        textField.setOnKeyPressed(e->{
            if(e.getCode()==KeyCode.ENTER){
                String tabName = FxmlClientServerController.selectedtab;
                for(int i=0;i<Server.maxClientCount;i++){
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
            displayMessage(rb.getString(Keys.CS_LOG1));
            displayMessage(rb.getString(Keys.CS_LOG2));
            running = true;
        } catch (IOException ex) {
            Platform.runLater(()->{showErrorMessage(ex.getMessage(), ex);});
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
                    rb.getString(Keys.CS_LOG3), e);
        } finally {
            try {
                server.close();
            } catch (IOException ex) {
                Platform.runLater(()->{showErrorMessage(ex.getMessage(), ex);});
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
                Platform.runLater(()->{showErrorMessage(ex.getMessage(), ex);});
            }
            displayMessage(rb.getString(Keys.CS_LOG4));
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
