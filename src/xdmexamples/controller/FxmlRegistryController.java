/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import xdmexamples.utility.DataRegistry;
import xdmexamples.utility.Registry;
import xdmexamples.utility.WindowsRegistry;
import xdmexamples.preloader.Keys;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlRegistryController implements Initializable {
    
    /*---------------------------------FXML Decleration----------------------------*/
    @FXML
    private TableView<DataRegistry> registryTable;
    @FXML
    private TableColumn<DataRegistry, String> enable,key,program,data;
    @FXML
    private Button btnRegistry,btnOpenFolder,btnClear;
    @FXML
    private Text txtProgress,txtLog;
    @FXML
    private ImageView imgIcon;
    @FXML
    private ProgressBar prgBar;
    /*------------------------------End of FXML Declerration-----------------------*/
    
    private final ObservableList<DataRegistry> datas = FXCollections.observableArrayList();
    private ResourceBundle bundle;
    private Task createRegistryTask;
    private final BooleanProperty ready = new SimpleBooleanProperty(false);
    

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;
        setVisible(false);
        registryTable.setPlaceholder(new Text(bundle.getString(Keys.EMPTY)));
        txtLog.setText(bundle.getString(Keys.REG_LOG1));
        enable.setCellValueFactory(new PropertyValueFactory<>("enable"));
        key.setCellValueFactory(new PropertyValueFactory<>("key"));
        program.setCellValueFactory(new PropertyValueFactory<>("program"));
        data.setCellValueFactory(new PropertyValueFactory<>("data"));
        datas.clear();
        setDisable(datas.isEmpty());
        registryTable.setItems(datas);
        registryTable.getSelectionModel().selectedIndexProperty().addListener(new RowSelectListener());
        datas.addListener((ListChangeListener.Change<? extends DataRegistry> c) -> {
            setDisable(datas.isEmpty());
        });
    }    
    
    private void setVisible(boolean flag) {
        imgIcon.setVisible(flag);
        txtProgress.setVisible(flag);
        prgBar.setVisible(flag);
        prgBar.setProgress(0);
        prgBar.progressProperty().unbind();
    }
    
    private class RowSelectListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if (newValue != null && !datas.isEmpty()) {
                DataRegistry dreg = datas.get((int) newValue);
                txtLog.setText(dreg.getProgram() + " " + dreg.getKey() + " " + dreg.getData());
            }
        }
    }
    
    private void setDisable(boolean empty) {
        btnOpenFolder.setDisable(empty);
        btnClear.setDisable(empty);
    }
    
    private void showDialog(Alert.AlertType type, String title, String head, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
                datas.clear();
            }
        } else {
            alert.showAndWait();
        }
    }  
    
    private Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                List<Registry> list = new ArrayList<>();
                list.addAll(Arrays.asList(WindowsRegistry.readRegistry(Keys.LOCATION[0])));
                list.addAll(Arrays.asList(WindowsRegistry.readRegistry(Keys.LOCATION[1])));
                String[] progtxt = bundle.getString(Keys.LD_SEQ).split(" ");
                if (!list.isEmpty()) {
                    for (int i = 0, len = list.size(); i < len; i++) {
                        txtProgress.setText(progtxt[i % 3]);
                        updateProgress((double) i, (double) len);
                        Registry reg = list.get(i);
                        boolean flag = WindowsRegistry.isRegistryEnable(Keys.PATHS, reg.getName());
                        DataRegistry dreg = new DataRegistry(flag, reg.getType(), reg.getName(), reg.getValue());
                        dreg.setPath(reg.getPath());
                        datas.add(dreg);
                    }
                    txtProgress.setText(bundle.getString(Keys.STOP));
                    txtLog.setText(bundle.getString(Keys.FNSH) + list.size() + bundle.getString(Keys.FOUND));
                    ready.setValue(Boolean.TRUE);
                    setVisible(false);
                }
                return true;
            }
        };
    }
    
    private DataRegistry setValues(DataRegistry dreg, boolean enable, String key, String program, String data) {
        dreg.setEnable(enable);
        dreg.setKey(key);
        dreg.setProgram(program);
        dreg.setData(data);
        return dreg;
    }
    
    @FXML
    private void opnFol() {
        DataRegistry reg = registryTable.getSelectionModel().getSelectedItem();
        if (reg == null) {
            txtLog.setText(bundle.getString(Keys.CHSREG));
            showDialog(Alert.AlertType.WARNING, bundle.getString(Keys.WARNING), 
                    bundle.getString(Keys.PRG_NAME), bundle.getString(Keys.REG_MSG1));
        } else {
            String location = reg.getData();
            location = location.substring(0, location.indexOf(".") + 4);
            if (location.startsWith("\"")) {
                location = location.substring(1, location.length());
            }
            Path path = Paths.get(location).getParent();
            try {
                txtLog.setText(path.toString() + bundle.getString(Keys.OPNFL));
                Runtime.getRuntime().exec("explorer " + path.toString());
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    @FXML
    private void clear() {
        showDialog(Alert.AlertType.CONFIRMATION, bundle.getString(Keys.QUESTION), 
                bundle.getString(Keys.PRG_NAME), bundle.getString(Keys.REG_CLS));
        txtLog.setText(bundle.getString(Keys.REG_LOG2));
    }
    
    @FXML
    private void getRegistrList() {
        datas.clear();
        txtLog.setText(bundle.getString(Keys.START));
        setVisible(true);
        createRegistryTask = createTask();
        if (!prgBar.progressProperty().isBound()) {
            prgBar.progressProperty().bind(createRegistryTask.progressProperty());
        }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           new Thread(createRegistryTask).start();
        ready.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (Boolean.TRUE.equals(t1)) {
                Platform.runLater(() -> {
                    showDialog(Alert.AlertType.INFORMATION, bundle.getString(Keys.INFORMATION), bundle.getString(Keys.PRG_NAME),
                            bundle.getString(Keys.FNSH)
                            + datas.size() + bundle.getString(Keys.FOUND));
                    prgBar.progressProperty().unbind();
                });
            }
        });
    }
}