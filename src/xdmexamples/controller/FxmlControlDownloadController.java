package xdmexamples.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TitledPane;
import javafx.scene.text.Text;

public class FxmlControlDownloadController implements Initializable{

    /*================================FXML Decleration============================*/
    @FXML private TitledPane titledPane;
    @FXML private Text fileAddress;
    @FXML private ProgressBar prgBar;
    @FXML private Label percentage;
    @FXML private Button btnStart;
    @FXML private Button btnCancel;
    @FXML private Button btnResume;
    @FXML private Button btnPause;
    @FXML private Button btnOpen;
    /*============================End of FXML Decleration=========================*/
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("download controller started");
    }
    
    /*====================FXML functions==========================================*/
    @FXML
    private void dwnStart(){
        System.out.println("start not ready yet");
    }
    
    @FXML
    private void dwnPause(){
        System.out.println("start not ready yet");
    }
    
    @FXML
    private void dwnResume(){
        System.out.println("start not ready yet");
    }
    
    @FXML
    private void dwnCancel(){
        System.out.println("start not ready yet");
    }
    
    @FXML
    private void openFile(){
        System.out.println("start not ready yet");
    }
    /*=================End of FXML functions======================================*/

    void initData(String text, String fileName, String fileLocation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}