/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import xdmexamples.preloader.Keys;

/**
 *
 * @author sakit
 */
public class XDMExamples extends Application {

    private Stage stage;
    private BooleanProperty ready = new SimpleBooleanProperty(false);
    private ResourceBundle bundle;

    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            
            //start preloader
            longStart();
            //set resource file to current locale
            bundle = ResourceBundle.getBundle("resources.bundles.Bundle", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/fxmlXDMExamples.fxml"),bundle);
            Parent parent = (Parent)loader.load();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/azer_emblem.png")));
            stage.setTitle(bundle.getString(Keys.PRG_NAME));
            ready.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                if (Boolean.TRUE.equals(t1)) {
                    Platform.runLater(() -> {
                        stage.show();
                    });
                }
            });
        } catch (IOException ex) {
            showErrorMessage(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void longStart() {
        //simulate long init in background
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int max = 23;
                for (int i = 1; i <= max; i++) {
                    Thread.sleep(250);
                    // Send progress to preloader
                    notifyPreloader(new Preloader.ProgressNotification(((double) i) / max));
                }
                // After init is ready, the app is ready to be shown
                // Do this before hiding the preloader stage to prevent the 
                // app from exiting prematurely
                Thread.sleep(600);
                ready.setValue(Boolean.TRUE);
                notifyPreloader(new Preloader.StateChangeNotification(
                        Preloader.StateChangeNotification.Type.BEFORE_START));

                return null;
            }
        };
        new Thread(task).start();
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
}
