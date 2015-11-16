package xdmexamples.controller;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import static javafx.concurrent.Worker.State.READY;
import static javafx.concurrent.Worker.State.RUNNING;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

public class FxmlControlDownloadController implements Initializable {

    /*================================FXML Decleration============================*/
    @FXML
    private TitledPane titledPane;
    @FXML
    private Text fileAddress;
    @FXML
    private ProgressBar prgBar;
    @FXML
    private Label percentage;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnOpen;
    @FXML
    private ResourceBundle bundle;
    /*============================End of FXML Decleration=========================*/

    private String fileName;
    private String downloadURL;
    private File fileLocation;
    private Task task;
    private Thread thread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("download controller started");
        bundle = resources;
    }

    /*====================FXML functions==========================================*/
    @FXML
    private void dwnStart() {
        System.out.println("start not ready yet");

        task = createWork();

        btnStart.disableProperty().bind(task.stateProperty().isNotEqualTo(READY));
        btnCancel.disableProperty().bind(task.stateProperty().isNotEqualTo(RUNNING));
        prgBar.progressProperty().bind(task.progressProperty());
        percentage.textProperty().bind(task.messageProperty());

        thread = new Thread(FxmlDownloadController.threads, task, fileName);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void dwnCancel() {
        System.out.println("in cancel");
        showAlertDialog(Alert.AlertType.CONFIRMATION, bundle.getString(Keys.QUESTION), bundle.getString(Keys.DWN_MSG3));
    }

    @FXML
    private void openFile() {
        try {
            System.out.println("in openfile");
            Desktop.getDesktop().open(fileLocation);
        } catch (IOException ex) {
            showErrorMessage(ex.getMessage(), ex);
        }
    }
    /*=================End of FXML functions======================================*/

    public void initData(String url, String fileName, String fileLocation) {
        downloadURL = url;
        this.fileName = fileName;
        final String path = fileLocation + File.separator + fileName;
        this.fileLocation = new File(path);
        titledPane.setText(fileName);
        fileAddress.setText(downloadURL);
    }

    private Task createWork() {
        return new Task() {

            @Override
            protected Object call() {
                try {
                    System.out.println("task started");
                    URL url = new URL(downloadURL);
                    URLConnection connection = url.openConnection();
                    long fileSize = connection.getContentLengthLong();
                    if (fileSize == -1) {
                        FileNotFoundException fnfe = new FileNotFoundException(downloadURL);
                        showErrorMessage(fnfe.getMessage(), fnfe);
                    } else {
                        byte[] buffer = new byte[1024];
                        int byteCount = -1;
                        int bytesRead = 0;
                        try (InputStream input = new BufferedInputStream(connection.getInputStream());
                                OutputStream output = new FileOutputStream(fileLocation)) {
                            while (!task.isCancelled() && byteCount <= fileSize) {
                                byteCount = input.read(buffer);
                                if (byteCount == -1) {
                                    break;
                                } else {
                                    output.write(buffer, 0, byteCount);
                                    bytesRead += byteCount;
                                    updateProgress(bytesRead, fileSize);
                                    updateMessage(String.format("%d bytes / %d bytes", bytesRead, fileSize));
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return true;
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                System.out.println(thread.getName() + " the task was cancelled");
                updateMessage(bundle.getString(Keys.DWN_CAN));
                updateProgress(1, 1);
            }

            @Override
            protected void failed() {
                super.failed();
                System.out.println(thread.getName() + " the task was faild");
                updateMessage(bundle.getString(Keys.DWN_CAN));
                updateProgress(1, 1);
                if (task.getException() != null) {
                    showErrorMessage(task.getException().getMessage(), task.getException());
                }
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println(thread.getName() + " the task was succeeded");
                btnOpen.setDisable(false);
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
                task.cancel();
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
