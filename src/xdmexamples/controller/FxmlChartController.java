/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import xdmexamples.helper.ValuteParser;
import xdmexamples.preloader.Keys;

/**
 * FXML Controller class
 *
 * @author sakit
 */
public class FxmlChartController implements Initializable {

    /*--------------------------------FXML Decleration----------------------------*/
    @FXML
    private Button btnResult, btnClear;
    @FXML
    private VBox vbox;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private LineChart lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private StackPane stackPane;
    @FXML
    private ProgressIndicator prgIndicator;
    @FXML
    private ResourceBundle bundle;
    /*-----------------------------End of FXML Decleration------------------------*/
    private final static ObservableList<String> VALYUTA = FXCollections.observableArrayList(Arrays.asList(Keys.VALUES));
    private static List selectedValues;
    private final CheckComboBox<String> checkComboBox = new CheckComboBox<>(VALYUTA);

    private final BooleanProperty ready = new SimpleBooleanProperty(false);
    private Task taskWork;

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

        checkComboBox.setPrefHeight(30.0);
        checkComboBox.setPrefWidth(176.0);
        checkComboBox.setMaxSize(150, 30);
        checkComboBox.setTooltip(new Tooltip(bundle.getString(Keys.CCB)));

        vbox.getChildren().add(checkComboBox);
        startDate.setConverter(converter);
        endDate.setConverter(converter);

        yAxis.setLabel(bundle.getString(Keys.YAXIS));

//        prgIndicator.progressProperty().addListener((observable, oldValue, newValue) -> {
//            Text text = (Text) prgIndicator.lookup(".percentage");
//            if (text != null && text.getText().equals("Done")) {
//                text.setText("New Text");
//                prgIndicator.setPrefWidth(text.getLayoutBounds().getWidth());
//            }
//        });
        // and listen to the relevant events (e.g. when the selected indices or 
        // selected items change).
        checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            if (checkComboBox.getCheckModel().isEmpty()) {
                showDialog(Alert.AlertType.WARNING, bundle.getString(Keys.WARNING), bundle.getString(Keys.PRG_NAME),
                        bundle.getString(Keys.CHT_MSG2));
                checkComboBox.getCheckModel().check(0);
            } else {
                ObservableList<Integer> in = checkComboBox.getCheckModel().getCheckedIndices();
                for (int i = 0, len = lineChart.getData().size(); i < len; i++) {
                    boolean visible = in.contains(i);
                    XYChart.Series ser = (XYChart.Series) lineChart.getData().get(i);
                    ser.getNode().setVisible(visible);
                    ObservableList<XYChart.Data> datas = ser.getData();
                    for (XYChart.Data data : datas) {
                        data.getNode().setVisible(visible);
                    }
                }
            }
        });
    }

    public static List getValutes() {
        return selectedValues;
    }

    private final StringConverter converter = new StringConverter<LocalDate>() {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern(Keys.PATTERN);

        @Override
        public String toString(LocalDate date) {
            if (date != null) {

                return dateFormatter.format(date);
            } else {
                return "";
            }
        }

        @Override
        public LocalDate fromString(String string) {
            if (string != null && !string.isEmpty()) {

                return LocalDate.parse(string, dateFormatter);
            } else {
                return null;
            }
        }
    };

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
                lineChart.getData().clear();
            }
        } else {
            alert.showAndWait();
        }
    }

    private void setDisable(boolean b) {
        startDate.setDisable(b);
        endDate.setDisable(b);
        btnClear.setDisable(b);
        btnResult.setDisable(b);
        checkComboBox.setDisable(b);
    }

    private void setVisible(boolean b) {
        prgIndicator.setVisible(b);
        prgIndicator.progressProperty().unbind();
        prgIndicator.setProgress(0);
    }

    @FXML
    private void clear() {
        showDialog(Alert.AlertType.CONFIRMATION, bundle.getString(Keys.QUESTION),
                bundle.getString(Keys.PRG_NAME), bundle.getString(Keys.CHT_CLS));
        ready.setValue(Boolean.FALSE);
        xAxis.setLabel(bundle.getString(Keys.XAXIS));
    }

    private Task createTask(LocalDate sd, LocalDate ed) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    long duration = 0, count = 0;
                    boolean f = false;
                    for (int i = 0, len = selectedValues.size(); i < len; i++) {
                        ValuteParser vp = new ValuteParser();
                        String[] result = vp.startParsing(sd, ed, selectedValues.get(i).toString(), i);
                        //updateProgress(count, duration);
                        if (!f) {
                            duration = len * result.length;
                            f = true;
                        }
                        for (String str : result) {
                            updateProgress(++count, duration);
                            ValuteParser.setValue(str, i);
                        }
                    }
                    Thread.sleep(1500);
                    ready.setValue(Boolean.TRUE);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return true;
            }
            
            private long getDuration(Date d1, Date d2, TimeUnit timeUnit) {
                return timeUnit.convert(d2.getTime() - d1.getTime(), timeUnit.DAYS) / 86400000;
            }
        };
    }

    private void doChartOperation(LocalDate sd, LocalDate ed) {
        lineChart.getData().clear();
        lineChart.setTitle(bundle.getString(Keys.LCHT_TITLE));
        xAxis.setLabel(bundle.getString(Keys.XAXIS));
        if (sd.getYear() != ed.getYear()) {
            drawChartForYear(sd, ed);
        } else if (sd.getMonthValue() != ed.getMonthValue()) {
            drawChartForMonth(sd, ed);
        } else {
            drawChartForDay(sd.getDayOfMonth(), ed.getDayOfMonth(), sd.getMonthValue(), sd.getYear());
        }
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

    @FXML
    private void getResult() {
        boolean connection = checkInternetConnection();
        if (connection) {
            lineChart.getData().clear();
            LocalDate sd = startDate.getValue();
            LocalDate ed = endDate.getValue();
            LocalDate now = LocalDate.now();
            lineChart.setTitle(bundle.getString(Keys.LCHT_TITLE));
            if (sd == null || ed == null || sd.getYear() < 2000 || sd.isAfter(now) || ed.getYear() < 2000 || ed.isAfter(now)
                    || sd.isAfter(ed) || sd.isEqual(ed)) {
                showDialog(Alert.AlertType.WARNING, bundle.getString(Keys.WARNING), bundle.getString(Keys.PRG_NAME),
                        bundle.getString(Keys.CHT_MSG3) + converter.toString(now));
            } else if (checkComboBox.getCheckModel().getCheckedItems().isEmpty()) {
                showDialog(Alert.AlertType.WARNING, bundle.getString(Keys.WARNING), bundle.getString(Keys.PRG_NAME),
                        bundle.getString(Keys.CHT_MSG2));
            } else {
                setDisable(true);
                setVisible(true);
                selectedValues = checkComboBox.getCheckModel().getCheckedItems();
                prgIndicator.setProgress(-1);
                taskWork = createTask(sd, ed);

                prgIndicator.progressProperty().unbind();
                prgIndicator.progressProperty().bind(taskWork.progressProperty());

                ready.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                    if (Boolean.TRUE.equals(t1)) {
                        Platform.runLater(() -> {
                            setVisible(false);
                            doChartOperation(sd, ed);
                            setDisable(false);
                            taskWork.cancel(true);
                            System.out.println("task cancel is done");
                            ready.setValue(Boolean.FALSE);
                        });
                    }
                });
                new Thread(taskWork).start();
            }
        }
    }

    private void drawChartForYear(LocalDate sd, LocalDate ed) {
        System.out.println("Start to draw line char for years");
        xAxis.setLabel(bundle.getString(Keys.XAXISY));
        lineChart.setTitle(bundle.getString(Keys.LCHT_TITLE) + converter.toString(sd) + "-" + converter.toString(ed));
        for (int i = 0, len = selectedValues.size(); i < len; i++) {
            XYChart.Series ser = new XYChart.Series();
            int sy = sd.getYear();
            int ey = ed.getYear();

            for (; sy <= ey; sy++) {
                XYChart.Data<String, Double> data = null;
                double val = 0.0;
                if (sy == sd.getYear()) {
                    val = ValuteParser.getValute(sy, sd.getMonthValue(), sd.getDayOfMonth(), i);
                    data = new XYChart.Data(sd.getMonthValue() + " "
                            + bundle.getString(sd.getMonthValue() + "s")
                            + " " + sy, val);
                } else if (sy == ed.getYear()) {
                    val = ValuteParser.getValute(sy, ed.getMonthValue(), ed.getDayOfMonth(), i);
                    data = new XYChart.Data(ed.getDayOfMonth() + " "
                            + bundle.getString(ed.getMonthValue() + "s")
                            + " " + sy, val);
                } else {
                    val = ValuteParser.getValuteForYear(sd.getYear(), i);
                    data = new XYChart.Data(String.valueOf(sy), val);
                }
                ser.getData().add(data);
            }

            ser.setName(selectedValues.get(i).toString());
            lineChart.getData().add(ser);
        }
    }

    private void drawChartForMonth(LocalDate sd, LocalDate ed) {
        System.out.println("Start to draw line char for months");
        xAxis.setLabel(bundle.getString(Keys.XAXISM));
        lineChart.setTitle(bundle.getString(Keys.LCHT_TITLE)
                + sd.getDayOfMonth() + " " + bundle.getString(sd.getMonthValue() + "s") + " " + sd.getYear()
                + "-" + ed.getDayOfMonth() + " " + bundle.getString(ed.getMonthValue() + "s") + " " + ed.getYear());
        for (int i = 0, len = selectedValues.size(); i < len; i++) {
            XYChart.Series ser = new XYChart.Series();
            ser.setName(selectedValues.get(i).toString());
            int sm = sd.getMonthValue();
            int em = ed.getMonthValue();

            for (; sm <= em; sm++) {
                XYChart.Data<String, Double> data = null;
                double val = 0.0;
                if (sm == sd.getMonthValue()) {
                    val = ValuteParser.getValute(sd.getYear(), sm, sd.getDayOfMonth(), i);
                    data = new XYChart.Data(sd.getDayOfMonth() + " " + bundle.getString(sm + "s"), val);
                } else if (sm == ed.getMonthValue()) {
                    val = ValuteParser.getValute(ed.getYear(), sm, ed.getDayOfMonth(), i);
                    data = new XYChart.Data(ed.getDayOfMonth() + " " + bundle.getString(sm + "s"), val);
                } else {
                    val = ValuteParser.getValuteForMonth(sd.getYear(), sm, i);
                    data = new XYChart.Data(DateFormatSymbols.getInstance(Locale.getDefault()).getShortMonths()[sm - 1], val);
                }
                ser.getData().add(data);
            }
            lineChart.getData().add(ser);
        }
    }

    private void drawChartForDay(int sDay, int eDay, int month, int year) {
        System.out.println("draw Operation for days");
        xAxis.setLabel(bundle.getString(Keys.XAXISD));
        System.out.println(Locale.getDefault());
        lineChart.setTitle(bundle.getString(Keys.LCHT_TITLE)
                + bundle.getString(month + "l"));
        for (int i = 0, len = selectedValues.size(); i < len; i++) {
            XYChart.Series ser = new XYChart.Series();
            ser.setName(selectedValues.get(i).toString());

            for (int day = sDay; day <= eDay; day++) {
                double val = ValuteParser.getValute(year, month, day, i);
                ser.getData().add(new XYChart.Data(String.valueOf(day), val));
            }
            lineChart.getData().add(ser);
        }
    }

    @FXML
    private void resetResult() {
        lineChart.setTitle(bundle.getString(Keys.LCHT_TITLE));
        doChartOperation(startDate.getValue(), endDate.getValue());
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
}
