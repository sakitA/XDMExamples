/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import static javafx.concurrent.Worker.State.RUNNING;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xdmexamples.preloader.Keys;
import xdmexamples.utility.DataProcess;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlProcessController implements Initializable {

    /*================================FXML Decleration==============================*/
    @FXML
    private Button btnProc;
    @FXML
    private Button btnClear;
    @FXML
    private TableView<DataProcess> tbView;
    @FXML
    private TableColumn<DataProcess, String> pName;
    @FXML
    private TableColumn<DataProcess, String> pSessionName;
    @FXML
    private TableColumn<DataProcess, Integer> pPid;
    @FXML
    private TableColumn<DataProcess, Integer> pSession;
    @FXML
    private TableColumn<DataProcess, Long> pMemUsage;
    @FXML
    private VBox prgIndi;
    @FXML
    private ResourceBundle bundle;
    @FXML
    private PieChart pieChart;
    /*==============================End of FXML Decleration=========================*/

    private final ObservableList<DataProcess> proList = FXCollections.observableArrayList();
    private Service service;
    private static boolean oncerun = false;

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

        service = new Service() {

            @Override
            protected Task createTask() {
                return runTask();
            }
        };

        tbView.setPlaceholder(new Text(bundle.getString(Keys.EMPTY)));
        pName.setCellValueFactory(new PropertyValueFactory<>("pName"));
        pPid.setCellValueFactory(new PropertyValueFactory<>("pPid"));
        pSession.setCellValueFactory(new PropertyValueFactory<>("pSession"));
        pSessionName.setCellValueFactory(new PropertyValueFactory<>("pSessionName"));
        pMemUsage.setCellValueFactory(new PropertyValueFactory<>("pMemUsage"));

        pieChart.visibleProperty().bind(Bindings.isNotEmpty(proList));

        btnProc.disableProperty().bind(service.stateProperty().isEqualTo(RUNNING));
        btnClear.disableProperty().bind(Bindings.isEmpty(proList));
        
        prgIndi.visibleProperty().bind(service.stateProperty().isEqualTo(RUNNING));
    }

    @FXML
    private void getProcess() {
        if (oncerun) {
            service.restart();
        } else {
            service.start();
            oncerun = true;
        }
    }
    
    @FXML
    private void clear(){
        proList.clear();
        pieChart.getData().clear();
    }

    private Task runTask() {
        return new Task() {

            @Override
            protected Object call() throws Exception {
                if (!this.isCancelled()) {
                    getRunningProcess();
                }
                return true;
            }

            @Override
            protected void failed() {
                super.failed(); //To change body of generated methods, choose Tools | Templates.
                System.err.println("task failed");
            }

            @Override
            protected void cancelled() {
                super.cancelled(); //To change body of generated methods, choose Tools | Templates.
                System.err.println("task cancelled");
            }

            @Override
            protected void succeeded() {
                super.succeeded(); //To change body of generated methods, choose Tools | Templates.
                initialPieChart();
                initialPieChartNode();
                System.err.println("task succeeded");
            }

            private void getRunningProcess() {
                try {
                    System.out.println("process run");
                    List<String> lines = new ArrayList<>();
                    Process p = Runtime.getRuntime().exec("tasklist.exe");
                    BufferedReader reader
                            = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("") || line.equals("\n")) {
                            continue;
                        }
                        lines.add(line);
                    }

                    //first 2 line we don't need so that we remove first 2 line from the list
                    lines.remove(0);
                    lines.remove(0);
                    proList.clear();
                    for (String result : lines) {
                        String[] str = result.split("  +");
                        DataProcess dp = new DataProcess();
                        dp.setPName(str[0]);
                        String[] tmp = str[1].split(" ");
                        dp.setPPid(Integer.valueOf(tmp[0]));
                        dp.setPSessionName(tmp.length == 1 ? "" : tmp[1]);
                        dp.setPSession(Integer.valueOf(str[2]));
                        tmp = str[3].split(" ");
                        tmp[0] = tmp[0].contains(".") ? tmp[0].replaceAll("\\.", "") : tmp[0];
                        dp.setPMemUsage(Long.valueOf(tmp[0]));
                        proList.add(dp);
                    }
                    tbView.getItems().addAll(proList);
                } catch (IOException | NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }

            private void initialPieChart() {
                System.out.println("process piechart");
                ObservableList<PieChart.Data> datas = FXCollections.<PieChart.Data>observableArrayList();
                SortedList<DataProcess> sl = proList.sorted();
                int len = sl.size() <= 7 ? sl.size() : 7;
                for (DataProcess dp : sl) {
                    PieChart.Data data = new PieChart.Data(dp.getPName(), dp.getPMemUsage());
                    datas.add(data);
                    if (len-- < 0) {
                        break;
                    }
                }
                pieChart.getData().addAll(datas);
            }

            private void initialPieChartNode() {
                for (final PieChart.Data data : pieChart.getData()) {
                    Node node = data.getNode();
                    final String percentage = "";
                    node.setOnMouseEntered(e->{
                        Tooltip.install(node, new Tooltip((long)data.getPieValue()+" K"));
                    });
                }
            }
        };
    }
}
