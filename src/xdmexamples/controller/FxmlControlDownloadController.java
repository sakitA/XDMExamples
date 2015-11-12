/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import com.sun.javafx.binding.StringFormatter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlControlDownloadController implements Initializable {

    /*-------------------------------------FXML decleration---------------------------*/
    @FXML
    private Text fileAddress;
    @FXML
    private Text percentage;
    @FXML
    private Text dwnFileSize;
    @FXML
    private Text txtSize;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnResume;
    @FXML
    private Button btnOpen;
    @FXML
    private ProgressBar prgBar;
    @FXML
    private ResourceBundle bundle;
    @FXML
    private TitledPane titledPane;
    /*----------------------------------End of FXML decleration-----------------------*/

    private URL downloadURL;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer;

    private String fileName;
    private String fileLocation;
    private int fileSize;
    private boolean dwnStop;
    private int bytesRead;
    private final static int BUFFER_SIZE = 1024;
    private final BooleanProperty ready = new SimpleBooleanProperty(false);
    private Task worker;
    private Thread thread;

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
        dwnStop = false;
    }

    @FXML
    private void dwnStart() {
        boolean connection = checkInternetConnection();
        if (connection) {
            btnStart.setDisable(true);
            btnStop.setDisable(false);
            btnPause.setDisable(false);
            btnResume.setDisable(true);

            worker = createTask();
            worker.valueProperty().addListener(new ChangeListener<Object>() {

                @Override
                public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                    if (!(newValue instanceof Boolean)) {
                        int per = (int) (100.0 * (Integer) newValue / fileSize);
                        percentage.setText(String.valueOf(per) + " %");
                        dwnFileSize.setText(newValue.toString() + " bytes");
                    }
                }
            });

            prgBar.setProgress(0);
            prgBar.progressProperty().unbind();
            prgBar.progressProperty().bind(worker.progressProperty());

            ready.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                if (Boolean.TRUE.equals(t1)) {
                    Platform.runLater(() -> {
                        System.out.println("Download finished");
                        cleanSource();
                        //ready.setValue(Boolean.FALSE);
                    });
                }
            });
            thread = new Thread(worker);
            //thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    private void dwnStop() {
        System.exit(0);
    }

    @FXML
    private void dwnPause() {
    }

    @FXML
    private void dwnResume() {
    }

    @FXML
    private void openFile() {
    }

    public void initData(String url, String fileName, String fileLocation) {

        this.fileName = fileName;
        this.fileLocation = fileLocation;
        bytesRead = 0;
        String path = "";
        try {
            downloadURL = new URL(url);
            URLConnection urlConnection = downloadURL.openConnection();
            fileSize = urlConnection.getContentLength();
            if (fileSize == -1) {
                throw new FileNotFoundException(url);
            }

            buffer = new byte[BUFFER_SIZE];
            dwnStop = false;
            String ext = url.substring(url.lastIndexOf("."));
            path = fileLocation + File.separator + fileName+ext;
            File file = new File(path);
            outputStream = new FileOutputStream(file);
            inputStream = new BufferedInputStream(
                    urlConnection.getInputStream());
           
        } catch (MalformedURLException | FileNotFoundException ex) {
            showErrorMessage(ex.getMessage(), ex);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            showErrorMessage(ex.getMessage(), ex);
        }

        titledPane.setText(fileName + "(" + path + ")");
        fileAddress.setText(url);
        txtSize.setText(String.valueOf(fileSize) + " bytes");

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
        Platform.runLater(() -> {
            alert.showAndWait();
        });

    }

    private Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                int byteCount = -1;
                //A correct implementation of a Task will always check for cancellation.
                while (!isCancelled() && (bytesRead < fileSize) && !dwnStop) {
                    try {
                        byteCount = inputStream.read(buffer);
                        if (byteCount == -1) {
                            dwnStop = true;
                            break;
                        } else {
                            outputStream.write(buffer, 0, byteCount);
                            bytesRead += byteCount;
                            updateProgress(bytesRead, fileSize);
                            //updateMessage(String.valueOf(bytesRead));
                            updateValue(bytesRead);
                        }
                    } catch (IOException ioe) {
                        dwnStop = true;
                        showErrorMessage(ioe.getMessage(), ioe);
                        break;
                    }
                }
                outputStream.close();
                inputStream.close();
                ready.setValue(byteCount >= fileSize);
                return true;
            }
        };
    }

    private boolean checkInternetConnection() {
        Socket socket = new Socket();
        InetSocketAddress adrs = new InetSocketAddress("www.google.com", 80);
        try {
            socket.connect(adrs, 3000);
            return true;
        } catch (Exception e) {
            showDialog(Alert.AlertType.ERROR, bundle.getString(Keys.ERROR), bundle.getString(Keys.PRG_NAME),
                    bundle.getString(Keys.CHK_INT));
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(FxmlChartController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private void showDialog(Alert.AlertType type, String title, String head, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(head);
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

    private void cleanSource() {
        System.out.println("clean source started");
        prgBar.progressProperty().unbind();
        prgBar.setProgress(0);

        worker.cancel(true);

        ready.setValue(Boolean.FALSE);
        btnStart.setDisable(true);
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnPause.setDisable(true);
    }
}
