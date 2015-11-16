package xdmexamples.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import static javafx.concurrent.Worker.State.RUNNING;
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
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

public class FxmlDownloadController implements Initializable {

    /*================================FXML Decleration============================*/
    @FXML
    private TextField urlText;
    @FXML
    private Button btnDownload;
    @FXML
    private Button btnCancel;
    @FXML
    private ProgressIndicator prgIndicator;
    @FXML
    private ResourceBundle bundle;
    @FXML
    private VBox vbox;
    /*============================End of FXML Decleration=========================*/

    private String fileName;
    private String fileLocation;
    protected final static ThreadGroup threads = new ThreadGroup("Downloads");
    private Thread thread;
    private Task<TitledPane> task;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        threads.setDaemon(true);
        //add text change listener to control download button disable
        urlText.textProperty().addListener((obser, oldValue, newValue) -> {
            btnDownload.setDisable(newValue.isEmpty());
        });
    }

    /*============================FXML functions==================================*/
    @FXML
    private void download() {
        String name = urlText.getText();
        fileName = name.substring(name.lastIndexOf("/") + 1).trim();
        fileLocation = getFileLocation();

        task = createWork(fileName, fileLocation);
        prgIndicator.visibleProperty().bind(task.stateProperty().isEqualTo(RUNNING));

        thread = new Thread(task, fileName);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void cancelAll() {
        if(threads.activeCount()>0){
            showAlertDialog(Alert.AlertType.CONFIRMATION, bundle.getString(Keys.QUESTION), bundle.getString(Keys.DWN_MSG1));
        }else
            showAlertDialog(Alert.AlertType.INFORMATION, bundle.getString(Keys.INFORMATION), bundle.getString(Keys.DWN_MSG2));
    }
    /*=======================End of FXML functions================================*/

    private String getFileLocation() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        dc.setTitle(bundle.getString(Keys.DWN_MSG2));
        File location = dc.showDialog(btnDownload.getScene().getWindow());
        return location == null ? System.getProperty("user.home") : location.getAbsolutePath();
    }

    private Task createWork(String fileName, String fileLocation) {
        return new Task() {

            private TitledPane ttlPane = null;

            @Override
            protected Object call() {
                if (!task.isCancelled()) {
                    try {
                        System.out.println("call started");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxmlControlDownload.fxml"), bundle);
                        ttlPane = (TitledPane) loader.load();
                        FxmlControlDownloadController fcdc = loader.<FxmlControlDownloadController>getController();
                        fcdc.initData(urlText.getText(), fileName, fileLocation);                        
                    } catch (IOException ex) {
                        System.out.println("Error: "+ex.getMessage());                        
                    }
                }
                return true;
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                System.out.println(thread.getName() + " the task was cancelled");
            }

            @Override
            protected void failed() {
                super.failed();
                if(task.getException()!=null)
                    showErrorMessage(task.getException().getMessage(), task.getException());
                System.out.println(thread.getName() + " the task was faild");
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println(thread.getName() + " the task was succeeded");
                vbox.getChildren().add(ttlPane);
                prgIndicator.progressProperty().unbind();
                prgIndicator.setProgress(0);
                btnCancel.setDisable(false);
                
            }
        };
    }

    private void showAlertDialog(Alert.AlertType alertType, String title, String message) {
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
            if (result.get() == btnYes) {
                threads.interrupt();
            }
        } else {
            alert.showAndWait();
        }        
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
}