/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlDownloadController implements Initializable {

    /*-------------------------------------FXML decleration---------------------------*/
    @FXML
    private TextField urlText;
    @FXML
    private Button btnDownload;
    @FXML
    private Button btnCancel;
    @FXML
    private ProgressIndicator prgIndicator;
    @FXML
    private VBox vbox;
    @FXML
    private ResourceBundle bundle;
    /*----------------------------------End of FXML decleration-----------------------*/

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;
        urlText.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            btnDownload.setDisable(newValue.isEmpty());
        });
    }

    @FXML
    private void download() {
        String fileName = "", fileLocation = "";
        while (fileName.isEmpty()) {
            fileName = getFileName();
        }
        while (fileLocation.isEmpty()) {
            fileLocation = getFileLocation();
        }
        if (fileName != null && fileLocation != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxmlControlDownload.fxml"), bundle);
                TilePane tp = (TilePane)loader.load();
                FxmlControlDownloadController fcdc = loader.<FxmlControlDownloadController>getController();
                fcdc.initData(fileName, fileLocation);
                vbox.getChildren().add(tp);
            } catch (IOException ex) {
                showErrorMessage(ex.getMessage(), ex);
            }
        }
    }

    @FXML
    private void cancelAll() {
    }

    private void showErrorMessage(String message, Exception... excp) {
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

    private void showDialog(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(bundle.getString(Keys.PRG_NAME));
        alert.setContentText(message);
        Stage st = (Stage) alert.getDialogPane().getScene().getWindow();
        st.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/azer_emblem.png")));

        if (type == Alert.AlertType.CONFIRMATION) {
            ButtonType btnYes = new ButtonType(bundle.getString(Keys.YES), ButtonBar.ButtonData.YES);
            ButtonType btnCancel = new ButtonType(bundle.getString(Keys.CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnYes, btnCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == btnYes) {

            }
        } else {
            alert.showAndWait();
        }
    }

    private String getFileName() {
        String fileName = null;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(bundle.getString(Keys.FLNM));
        dialog.setHeaderText(bundle.getString(Keys.PRG_NAME));
        dialog.setContentText(bundle.getString(Keys.ENT_FLNM));

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            fileName = result.get();
        }
        return fileName;
    }

    private String getFileLocation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
