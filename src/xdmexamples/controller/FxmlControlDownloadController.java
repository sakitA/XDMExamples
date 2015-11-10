/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlControlDownloadController implements Initializable {
    
    /*-------------------------------------FXML decleration---------------------------*/
    @FXML private Text fileAddress;
    @FXML private Text percentage;
    @FXML private Text dwnFileSize;
    @FXML private Text fileSize;
    @FXML private Button btnStart;
    @FXML private Button btnStop;
    @FXML private Button btnPause;
    @FXML private Button btnResume;
    @FXML private ProgressBar prgBar;
    @FXML private ResourceBundle bundle;
    @FXML private TitledPane titledPane;
    /*----------------------------------End of FXML decleration-----------------------*/
    
    private String fileName;
    private String fileLocation;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;        
        
    }    
    
    @FXML
    private void dwnStart(){}
    
    @FXML
    private void dwnStop(){}
    
    @FXML
    private void dwnPause(){}
    
    @FXML
    private void dwnResume(){}

    public void initData(String fileName, String fileLocation) {
        this.fileName = fileName;
        this.fileLocation = fileLocation;
        
        titledPane.setText(fileName+"("+fileLocation+")");
    }
}
