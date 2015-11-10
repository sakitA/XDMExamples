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
    /*----------------------------------End of FXML decleration-----------------------*/

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    private void dwnStart(){}
    
    @FXML
    private void dwnStop(){}
    
    @FXML
    private void dwnPause(){}
    
    @FXML
    private void dwnResume(){}

    void initData(String fileName, String fileLocation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
