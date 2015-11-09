/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlChangeLanguageController implements Initializable {

    /*-----------------------------FXML paramters start---------------------------*/
    @FXML
    private ImageView imgGlob;

    /*-----------------------------FXML paramters end---------------------------*/
    private double xOffset;
    private double yOffset;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        imgGlob.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        imgGlob.setOnMouseDragged((MouseEvent event) -> {
            Stage stage = (Stage)imgGlob.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    @FXML
    private void setLangAZ() {
        System.out.println("local change azerbaijan");
        Locale.setDefault(new Locale("az", "AZ"));
        ((Stage)imgGlob.getScene().getWindow()).close();
    }

    @FXML
    private void setLangEN() {
        System.out.println("local change english");
        Locale.setDefault(Locale.ENGLISH);
        ((Stage)imgGlob.getScene().getWindow()).close();
    }
}
