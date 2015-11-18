/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import static javafx.scene.control.Alert.AlertType.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import xdmexamples.preloader.Keys;
import xdmexamples.utility.Client;
import xdmexamples.utility.Server;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlClientServerController implements Initializable {

    /*================================FXML Decleration============================*/
    @FXML
    private Button btnClear;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;
    @FXML
    private Slider slider;
    @FXML
    private TextArea txtArea;
    @FXML
    private TextField serverMessage;
    @FXML
    private TabPane tabPane;
    @FXML
    private GridPane gridPane;
    @FXML
    private ResourceBundle bundle;
    /*============================End of FXML Decleration=========================*/

    private int maxClientCount;
    private static int clientCount;
    private Server server;
    private Client client;
    private Service serverService, clientService;
    private static boolean serverflag, clientflag;
    
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        maxClientCount = (int) slider.getValue();
        bundle = rb;
        clientCount = 0;

        System.out.println("max client count:" + maxClientCount);
        serverflag = clientflag = false;
        setDisable(false);

        txtArea.textProperty().addListener((obser, oldValue, newValue) -> {
            btnClear.setDisable(txtArea.getText().isEmpty());
        });

        slider.valueProperty().addListener((obser, oldValue, newValue) -> {
            maxClientCount = newValue.intValue();
            System.out.println("max client count:" + maxClientCount);
        });

        server = new Server(txtArea, serverMessage, maxClientCount);
        serverService = new Service() {

            @Override
            protected Task createTask() {
                return serverTask();
            }
        };
        clientService = new Service(){

            @Override
            protected Task createTask() {
                return clientTask();
            }            
        };
        
        tabPane.getTabs().addListener(new ListChangeListener<Tab>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends Tab> c) {
                clientCount = tabPane.getTabs().size();
                System.out.println("clientCount:"+clientCount);
            }
        });
    }

    /*=======================================FXML Function============================================*/
    @FXML
    private void addClient() {
        if (clientCount >= maxClientCount) {
            showAlertDialog(WARNING, bundle.getString(Keys.WARNING), bundle.getString(Keys.CS_MSG1));
        } else {
            String clientName = null;
            while (clientName == null) {
                clientName = getClientName();
            }

            tabPane.getTabs().add(createTab(clientName));
        }
    }

    @FXML
    private void clearContent() {
        txtArea.clear();
    }

    @FXML
    private void startServer() {
        Stage stage = (Stage) btnStart.getScene().getWindow();
        stage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        setDisable(true);

        if (serverflag) {
            serverService.restart();
        } else {
            serverService.start();
            serverflag = true;
        }
    }

    @FXML
    private void stopServer() {
        boolean result = true;
        if (!tabPane.getTabs().isEmpty()) {
            result = showAlertDialog(CONFIRMATION, bundle.getString(Keys.QUESTION), bundle.getString(Keys.CS_MSG2));
            if (result) {
                tabPane.getTabs().clear();
                clientCount = 0;
            }
        }
        if (result) {
            setDisable(false);
            if (serverflag) {
                serverService.restart();
            } else {
                serverService.start();
                serverflag = true;
            }
        }
    }

    /*===================================End of FXML Function============================================*/
    private boolean showAlertDialog(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(bundle.getString(Keys.PRG_NAME));
        alert.setContentText(message);
        Stage st = (Stage) alert.getDialogPane().getScene().getWindow();
        st.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/azer_emblem.png")));

        if (alertType == Alert.AlertType.CONFIRMATION) {
            ButtonType btnYes = new ButtonType(bundle.getString(Keys.YES), ButtonBar.ButtonData.YES);
            ButtonType btnCnl = new ButtonType(bundle.getString(Keys.CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnYes, btnCnl);
            Optional<ButtonType> result = alert.showAndWait();
            return result.get() == btnYes;
        } else {
            alert.showAndWait();
        }
        return true;
    }

    private void showErrorMessage(String message, Throwable... excp) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString(Keys.ERROR));
        alert.setHeaderText(bundle.getString(Keys.PRG_NAME));
        alert.setContentText(message);

        if (excp.length > 0) {
            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            excp[0].printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label(bundle.getString(Keys.EXCEPTİON_LABEL));

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);
        }
        alert.showAndWait();
    }

    private String getClientName() {
        String fileName = null;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(bundle.getString(Keys.FLNM));
        dialog.setHeaderText(bundle.getString(Keys.PRG_NAME));
        dialog.setContentText(bundle.getString(Keys.ENT_FLNM));
        dialog.getEditor().setPromptText("Naruto Uzumaki");
        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            fileName = result.get();
        }
        return fileName == null || fileName.isEmpty() ? "Client " + clientCount : fileName;
    }

    private void setDisable(boolean disable) {
        btnStart.setDisable(disable);
        slider.setDisable(disable);
        btnStop.setDisable(!disable);
        btnAdd.setDisable(!disable);
    }

    private Tab createTab(final String clientName) {
        TextField txtField = new TextField();

        TextArea area = new TextArea();
        txtArea.setEditable(false);

        VBox vbox = new VBox(5, txtField, area);
        VBox.setMargin(txtField, new Insets(5, 5, 0, 5));
        VBox.setMargin(area, new Insets(0, 5, 5, 5));

        Tab tab = new Tab(clientName);
        tab.setClosable(true);
        tab.setContent(vbox);
        client = new Client(tab);
        if(clientflag)
            clientService.restart();
        else{
            clientService.start();
            clientflag = true;
        }        
        return tab;
    }
    
    private Task serverTask(){
        return new Task(){

            @Override
            protected Object call() throws Exception {
                if(server.isServerRunnning()){
                    server.stopServer();
                }else{
                    server.startServer();
                }
                return null;
            }
        };
    }
    
    private Task clientTask(){
        return new Task(){

            @Override
            protected Object call() throws Exception {
                client.runClient();
                return null;
            }
        };
    }
}