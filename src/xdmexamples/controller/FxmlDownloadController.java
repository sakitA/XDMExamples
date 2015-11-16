package xdmexamples.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import static javafx.concurrent.Worker.State.RUNNING;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import xdmexamples.preloader.Keys;

public class FxmlDownloadController implements Initializable {

    /*================================FXML Decleration============================*/
    @FXML
    private TextField urlText;
    @FXML
    private Button btnDownload;
    @FXML
    private Button btCancell;
    @FXML
    private ProgressIndicator prgIndicator;
    @FXML
    private ResourceBundle bundle;
    @FXML
    private VBox vbox;
    /*============================End of FXML Decleration=========================*/

    private String fileName;
    private String fileLocation;
    private final static ThreadGroup threads = new ThreadGroup("Downloads");
    private Thread thread;
    private Task<TitledPane> task;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;

        //add text change listener to control download button disable
        urlText.textProperty().addListener((obser, oldValue, newValue) -> {
            btnDownload.setDisable(newValue.isEmpty());
        });
    }

    /*============================FXML functions==================================*/
    @FXML
    private void download() {
        String name = urlText.getText();
        fileName = name.substring(name.lastIndexOf("/") + 1);
        fileLocation = getFileLocation();

        task = createWork(fileName, fileLocation);
        prgIndicator.visibleProperty().bind(task.stateProperty().isEqualTo(RUNNING));

        thread = new Thread(threads, task, fileName);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void cancelAll() {
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
                    } catch (IOException ex) {
                        ex.printStackTrace();
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
                System.out.println(thread.getName() + " the task was faild");
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                vbox.getChildren().add(ttlPane);
                System.out.println(thread.getName() + " the task was succeeded");
            }
        };
    }
}