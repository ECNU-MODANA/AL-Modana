package ecnu.modana.FmiDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import org.apache.log4j.Logger;
import org.omg.CORBA.PRIVATE_MEMBER;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Default;

import ecnu.modana.FmiDriver.CosimulationMaster.Exchange;
import ecnu.modana.PlotComposer.PlotComposer;
import ecnu.modana.alsmc.generatePA.BIETool;
import ecnu.modana.alsmc.generatePA.PreTree;
import ecnu.modana.alsmc.generatePA.TreeNode;
import ecnu.modana.alsmc.main.ExeUppaal;
import ecnu.modana.alsmc.parameter.HeuristicUI;
import ecnu.modana.alsmc.parameter.ParameterUI;
import ecnu.modana.alsmc.properties.PropertiesUI;
import ecnu.modana.alsmc.util.UserFile;
import ecnu.modana.ui.MyTextConvertor;

import static ecnu.modana.FmiDriver.Composer.initPlotPanel;
import static ecnu.modana.FmiDriver.Verifier.initVerifierPanel;

/**
 * @author JKQ
 *         <p>
 *         2015年11月29日下午3:41:30
 */
public class CoSimulationUI {
    //simulator window=================================================================
    private Stage propertiesStage = null;
    private String prismModelPath = "./fmu.pm", FMUPath = "./MyBouncingBall.fmu";
    CoSimulation coSimulation = new CoSimulation("127.0.0.1", 40000);
    LineChart<Object, Number> lineChart = null;
    SplitPane rootPane;
    public ObservableList<MappingList> mappingLists = FXCollections.observableArrayList();
    public ObservableList<MappingList> resultLists = FXCollections.observableArrayList();
    public ObservableList<FMUPath> FMULists = FXCollections.observableArrayList();

    public static ObservableList<String> algLists=FXCollections.observableArrayList();
    Label resultLable = null;
    String markovType = "dtmc";
    Logger logger = Logger.getRootLogger();
    BorderPane downPane = null;
    public static Trace trace = null;
    TableView<ObservableList<StringProperty>> simulateTable = null;
    //verifier window================================================================
    public static MenuBar menuBar = null;

    //plotComposer window===========================================================
    private MenuBar plotMenuBar = null;
    public static TextField TitleField = null;
    public static TextField xAxisField = null;
    public static TextField yAxisField = null;
    public static List<String> variableNameList = null;

    public void start(Stage plotComposerStage) throws Exception {
        plotComposerStage.getIcons().add(new Image("modana-logo.png"));
        int width = 1100, height = 650;
        this.propertiesStage = plotComposerStage;
        plotComposerStage.initModality(Modality.WINDOW_MODAL);
        plotComposerStage.setOpacity(1);
        plotComposerStage.setTitle("Co-verification");
        plotComposerStage.setWidth(width);
        plotComposerStage.setHeight(height);
        plotComposerStage.centerOnScreen();
        plotComposerStage.setResizable(false);

        Group root = new Group();
        Scene scene = new Scene(root, Color.WHITE);//, 1000, 800, Color.WHITE);

        //CREATE THE SPLITPANE
        SplitPane splitPane = new SplitPane();
        splitPane.setPrefSize(width, height);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.38);
        //splitPane.setStyle("-fx-background: rgb(0,0,0);");

        int fmuPaneWidth = 500;
        BorderPane fmuPane = initFMUTable(fmuPaneWidth);
        fmuPane.setMinSize(fmuPaneWidth, 130);

        BorderPane simulatePane = initSimulate(width - fmuPaneWidth - 50);
        simulatePane.setMinSize(width - fmuPaneWidth - 50, 130);

        BorderPane mappingPane = initReward(width);
        mappingPane.setMinSize(width, 130);

	        /*Button btnWork=new Button("Co-Simulation");
            btnWork.setMaxSize(150, 30);
	        btnWork.setMinSize(150, 30);*/
        Label fmuLable = new Label("Co-simulation model table");
        fmuLable.setMinSize(250, 30);
        Label mappingLable = new Label("Congruent relationship table");
        mappingLable.setMinSize(220, 30);
        //mappingLable.setVisible(false);

        HBox hBox1 = new HBox(5);
        hBox1.setTranslateX(10);
        hBox1.setTranslateY(10);
        HBox hBox2 = new HBox(5);
        hBox2.setTranslateX(10);
        hBox2.setTranslateY(10);
        HBox hBox3 = new HBox(5);
        hBox3.setTranslateY(10);
        HBox hBox4 = new HBox(5);
        hBox4.setTranslateX(10);
        hBox4.setTranslateY(10);
        VBox vBox = new VBox(5);
//	        hBox.getChildren().addAll(fmuPane,mappingPane,fmuLable,btnWork,mappingLable);

        BorderPane upPane = new BorderPane();
        upPane.setMinHeight(350);
        upPane.setPrefHeight(350);
        //leftPane.setStyle("-fx-background-color: #0000AA;");
        //upPane.getChildren().add(hBox);
//	        upPane.getChildren().addAll(btnPrism,btnFMU);

        downPane = new BorderPane();
        downPane.setSnapToPixel(true);
//	        downPane.setStyle("-fx-background: rgb(10,10,10);");
        resultLable = new Label("Co-simulation result");
        downPane.setCenter(resultLable);

        hBox1.getChildren().addAll(fmuPane, simulatePane);
        ChoiceBox ma = new ChoiceBox(FXCollections.observableArrayList(
                "select master algorithm", "step regular", "step roll back", "step prediction", "step prediction & learning")
        );
        ma.setMinWidth(300);
        ma.getSelectionModel().select(0);
        // hBox2.getChildren().addAll(fmuLable,btnWork);
        hBox2.getChildren().addAll(fmuLable, ma);

        hBox3.getChildren().add(mappingPane);
        hBox4.getChildren().add(mappingLable);
        vBox.getChildren().addAll(hBox1, hBox2, hBox3, hBox4);
        upPane.getChildren().addAll(vBox);

        splitPane.getItems().addAll(upPane, downPane);
        TabPane perpane = new TabPane();
        Tab coSimTab = new Tab();
        coSimTab.setClosable(false);
        coSimTab.setText("Simulator");
        Tab coVerTab = new Tab();
        coVerTab.setText("verifier");
        coVerTab.setClosable(false);
        BorderPane verifierUI = new BorderPane();
        Verifier.initVerifierPanel(verifierUI);
        Tab coPlotTab = new Tab();
        coPlotTab.setText("plot-Composer");
        coPlotTab.setClosable(false);
        BorderPane plotRoot = new BorderPane();
        Composer.initPlotPanel(plotRoot);
        coPlotTab.setOnSelectionChanged(e -> initPlotPanel(plotRoot));
        perpane.getTabs().addAll(coSimTab, coVerTab, coPlotTab);
        coSimTab.setContent(splitPane);
        coVerTab.setContent(verifierUI);
        coPlotTab.setContent(plotRoot);
        //ADD SPLITPANE TO ROOT
        root.getChildren().add(perpane);

        propertiesStage.initStyle(StageStyle.DECORATED);
        scene.getStylesheets().add("./Stage.css");
        propertiesStage.setScene(scene);
        propertiesStage.show();
    }

    private int step = 0;

    private BorderPane initSimulate(int width) {
        BorderPane simulatePane = new BorderPane();


        VBox explorationBox = new VBox(5);
        explorationBox.setMinSize(175, 100);
        Label explorationLable = new Label();
        Font font1 = new Font(15);
        explorationLable.setText("Automatic exploration");
        explorationLable.setFont(font1);
        Button simulateWork = new Button("Co-Simulate");
        simulateWork.setMaxSize(140, 25);
        simulateWork.setMinSize(140, 25);
        CosimulationMaster cm = new CosimulationMaster();
        simulateWork.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {

            public void handle(Event e) {
                step++;
                if (trace == null) {
                    for (int i = 0; i < FMULists.size(); i++)
                        System.out.println("currently FMU address are" + FMULists.get(i).getPath().get());
                    for (int i = 0; i < mappingLists.size(); i++)
                        System.out.println(mappingLists.get(i).getSource().get() + " =====" + mappingLists.get(i).getTarget().get());
                    LinkedList<String> fmus = new LinkedList<>();
                    fmus.add("./files/zeroDetectorNew.fmu");
                    fmus.add("./files/simpleMath2.fmu");
                    LinkedHashMap<String, Exchange> mappingMap = new LinkedHashMap<>();
                    Exchange exchange = new CosimulationMaster().newEchange();

                    exchange.fromSlave = "simpleMath";
                    exchange.fromVariable = "y";
                    exchange.targetSlave = "zeroDetector";
                    exchange.targetVariable = "i";
                    mappingMap.put("simpleMath", exchange);
                    LinkedHashMap<String, String> toolSlave = new LinkedHashMap<String, String>();
                    long start = System.currentTimeMillis();
                    ObservableList<Node> oblist = explorationBox.getChildren();
                    HBox stepsizeBox = (HBox) oblist.get(4);
                    TextField stepsizetextField = (TextField) stepsizeBox.getChildren().get(1);
                    double stepsize = Double.parseDouble(stepsizetextField.getText());
                    trace = cm.CoSimulation(fmus, toolSlave, mappingMap,
                            0, 20, stepsize, 1, "./files/res.csv");
                    long end = System.currentTimeMillis();
                    System.out.println("仿真时间：" + (end - start));
                    variableNameList = cm.getFMUVariables(fmus);
                }
                TableView<ObservableList<StringProperty>> simulateTable = new TableView();
                ArrayList<TableColumn<ObservableList<StringProperty>, String>> cList = new ArrayList<TableColumn<ObservableList<StringProperty>, String>>();
                Iterator it = cm.fmusMap.entrySet().iterator();
                int vNumber = 0;
                cList.add(createColumn(0, "time"));
                while (it.hasNext()) {
                    Entry entry = (Map.Entry) it.next();
                    FMUMESlave fmuslave = (FMUMESlave) entry.getValue();
                    SlaveTrace st = trace.slaveMap.get(fmuslave.fmiModelDescription.modelIdentifier);
                    for (String vName : st.GetVarNames()) {
                        vNumber++;
                        cList.add(createColumn(vNumber, vName));
                    }
                }
                ObservableList<ObservableList<StringProperty>> csvData = FXCollections.observableArrayList();
                SlaveTrace st;
                for (int i = 0; i < step; i++) {
                    it = trace.slaveMap.entrySet().iterator();
                    ObservableList<StringProperty> row = FXCollections.observableArrayList();
                    while (it.hasNext()) {
                        Entry entry = (Map.Entry) it.next();
                        st = (SlaveTrace) entry.getValue();
                        if (row.size() == 0)
                            row.add(new SimpleStringProperty(Double.toString(st.statesList.get(i).time)));
                        for (Object rowData : st.statesList.get(i).values) {
                            StringProperty sTemp = new SimpleStringProperty();
                            sTemp.set(rowData.toString());
                            row.add(sTemp);
                        }
                    }
                    csvData.add(row); // add each row to cvsData
                }
                simulateTable.getColumns().addAll(cList);
                simulateTable.setItems(csvData); // finally add data to tableview

                downPane.setTop(simulateTable);

            }
        });

        Label exStepNum = new Label();
        Label exEmptyLable = new Label();
        exStepNum.setText("Step Num");
        TextField exStepNumField = new TextField();
        exStepNumField.setId("exStepNumField");
        exStepNumField.setMaxWidth(70);
        HBox stepNumBox = new HBox();
        stepNumBox.getChildren().addAll(exStepNum, exStepNumField);
        Label stepSizeLable = new Label();
        stepSizeLable.setText("Step Size");
        TextField stepSizeField = new TextField();
        stepSizeField.setId("exStepNumField");
        stepSizeField.setMaxWidth(70);
        HBox stepSizeBox = new HBox();
        stepSizeBox.getChildren().addAll(stepSizeLable, stepSizeField);

        explorationBox.getChildren().addAll(exEmptyLable, explorationLable, simulateWork, stepNumBox, stepSizeBox);


        VBox backtrackBox = new VBox(5);
//		backtrackBox.setStyle("-fx-border-style: none");
        backtrackBox.setMinSize(175, 130);
        Label BKEmptyLable = new Label();
        Label backtrackLable = new Label();
        backtrackLable.setText("Backtracking");
        backtrackLable.setFont(font1);
        Button backtrackWork = new Button("Backtracking");
        backtrackWork.setMaxSize(140, 25);
        backtrackWork.setMinSize(140, 25);
        backtrackWork.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            public void handle(Event e) {
                if (trace == null || step < 0) {
                    step = 0;
                    e.consume();
                } else
                    step--;
                simulateTable = new TableView();
                ArrayList<TableColumn<ObservableList<StringProperty>, String>> cList = new ArrayList<TableColumn<ObservableList<StringProperty>, String>>();
                Iterator it = cm.fmusMap.entrySet().iterator();
                int vNumber = 0;
                cList.add(createColumn(0, "time"));
                while (it.hasNext()) {
                    Entry entry = (Map.Entry) it.next();
                    FMUMESlave fmuslave = (FMUMESlave) entry.getValue();
                    SlaveTrace st = trace.slaveMap.get(fmuslave.fmiModelDescription.modelIdentifier);
                    for (String vName : st.GetVarNames()) {
                        vNumber++;
                        cList.add(createColumn(vNumber, vName));
                    }
                }
                ObservableList<ObservableList<StringProperty>> csvData = FXCollections.observableArrayList();
                SlaveTrace st;
                for (int i = 0; i < step; i++) {
                    it = trace.slaveMap.entrySet().iterator();
                    ObservableList<StringProperty> row = FXCollections.observableArrayList();
                    while (it.hasNext()) {
                        Entry entry = (Map.Entry) it.next();
                        st = (SlaveTrace) entry.getValue();
                        if (row.size() == 0)
                            row.add(new SimpleStringProperty(Double.toString(st.statesList.get(i).time)));
                        for (Object rowData : st.statesList.get(i).values) {
                            StringProperty sTemp = new SimpleStringProperty();
                            sTemp.set(rowData.toString());
                            row.add(sTemp);
                        }
                    }
                    csvData.add(row); // add each row to cvsData
                }
                simulateTable.getColumns().addAll(cList);
                simulateTable.setItems(csvData); // finally add data to tableview

                downPane.setTop(simulateTable);
            }
        });
        Label bkStepNum = new Label();
        bkStepNum.setText("Step Num");
        TextField bkStepNumField = new TextField();
        bkStepNumField.setId("exStepNumField");
        bkStepNumField.setMaxWidth(70);
        HBox setbkStepNumBox = new HBox();
        setbkStepNumBox.getChildren().addAll(bkStepNum, bkStepNumField);
        Button clearAll = new Button("clearAll");
        clearAll.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                step = 0;
                if (simulateTable != null)
                    simulateTable.getItems();
                trace = null;
                downPane.getChildren().clear();

            }
        });

        backtrackBox.getChildren().addAll(BKEmptyLable, backtrackLable, backtrackWork, setbkStepNumBox, clearAll);


        HBox executeBox = new HBox();
        executeBox.getChildren().addAll(explorationBox, backtrackBox);
//		simulatePane.setStyle("-fx-border-color: black");
        simulatePane.setCenter(executeBox);
        simulatePane.setMaxWidth(width);
        return simulatePane;
    }

    private TableColumn<ObservableList<StringProperty>, String> createColumn(
            final int columnIndex, String columnTitle) {
        TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
        String title;
        System.out.println(columnIndex);
        if (columnTitle == null || columnTitle.trim().length() == 0) {
            title = "Column " + (columnIndex + 1);
        } else {
            title = columnTitle;
        }
        column.setText(title);
        column
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(
                            CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
                        ObservableList<StringProperty> values = cellDataFeatures.getValue();
                        if (columnIndex >= values.size()) {
                            return new SimpleStringProperty("");
                        } else {
                            return cellDataFeatures.getValue().get(columnIndex);
                        }
                    }
                });
        return column;
    }

    VBox opreateVbox;
    ObservableList<String> mapNames = FXCollections.observableArrayList();
    ObservableList<String> sourceMapNames = FXCollections.observableArrayList();
    ObservableList<String> targetMapNames = FXCollections.observableArrayList();


    private BorderPane initReward(int width) {
        //reward table view
        BorderPane rewardPane = new BorderPane();
        opreateVbox = new VBox(50);
        opreateVbox.setPadding(new Insets(10));
        opreateVbox.setPrefWidth(10);
        opreateVbox.setTranslateY(90);

        TableView<MappingList> rewardTableView = new TableView<MappingList>();
        rewardTableView.setTooltip(new Tooltip("在表格空白处点击右键，设置协同仿真模型属性之间的对应关系"));
        rewardTableView.setTableMenuButtonVisible(true);
        rewardTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rewardTableView.setEditable(true);
        // set data of table view
        rewardTableView.setItems(mappingLists);
        rewardTableView.setMinWidth(width - 100);
        // add columns
        TableColumn<MappingList, String> sourceCol = new TableColumn<MappingList, String>("Source");
        sourceCol.setEditable(true);
        sourceCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingList, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<MappingList, String> f) {
                return f.getValue().getSource();
            }
        });
        sourceCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), mapNames));
        sourceCol.setMinWidth((width) / 2);
        sourceCol.setMaxWidth((width) / 2);
        sourceCol.setPrefWidth(200);
        TableColumn<MappingList, String> rewardValueCol = new TableColumn<MappingList, String>("Target");
        rewardValueCol.setEditable(true);
        rewardValueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MappingList, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<MappingList, String> f) {
                return f.getValue().getTarget();
            }
        });
        rewardValueCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), mapNames));
        rewardValueCol.setMinWidth((width) / 2);
        rewardValueCol.setMaxWidth((width) / 2);
        rewardValueCol.setPrefWidth((width) / 2);
        rewardTableView.getColumns().addAll(sourceCol, rewardValueCol);
        rewardTableView.setMinWidth(width - 100);
        rewardTableView.setMaxWidth(width - 50);
        rewardPane.setCenter(rewardTableView);
        rewardPane.setRight(opreateVbox);

        rewardTableView.setTableMenuButtonVisible(true);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Delete");
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                mappingLists.removeAll(rewardTableView.getSelectionModel().getSelectedItems());
                e.consume();
            }
        });
        MenuItem menuItemAdd = new MenuItem("Add");
        menuItemAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                mappingLists.add(new MappingList("source", "target"));
                e.consume();
            }
        });
        contextMenu.getItems().addAll(menuItemAdd, menuItem);
        rewardTableView.setContextMenu(contextMenu);
        //mappingLists.add(new MappingList("source: from one model", "target: to another model"));

        return rewardPane;
        //return rewardTableView;
    }

    public class MappingList {
        private StringProperty source;
        private StringProperty target;

        public MappingList(String source, String target) {
            this.source = new SimpleStringProperty(source);
            this.target = new SimpleStringProperty(target);
        }

        public StringProperty getSource() {
            return source;
        }

        public StringProperty getTarget() {
            return target;
        }
    }

    String[] fmuVariables, markovVariable;

    private void FillMappingList() {
        if (null == fmuVariables || fmuVariables.length == 0 || null == markovVariable || markovVariable.length == 0) {
            logger.error("lack of models or there no variables in model");
            return;
        }
        int i, j;
        String tailFix = "";
        for (i = 0; i < fmuVariables.length; i++) {
            if (fmuVariables[i].contains(".in_")) {
                sourceMapNames.add(fmuVariables[i]);
                tailFix = "out_" + fmuVariables[i].substring(fmuVariables[i].indexOf(".in_") + 4);
                for (j = 0; j < markovVariable.length; j++) {
                    if (markovVariable[j].endsWith(tailFix))
                        targetMapNames.add(markovVariable[j]);
                }
            }
        }
        for (i = 0; i < markovVariable.length; i++) {
            if (markovVariable[i].contains(".in_")) {
                sourceMapNames.add(markovVariable[i]);
                tailFix = "out_" + markovVariable[i].substring(markovVariable[i].indexOf(".in_") + 4);
                for (j = 0; j < fmuVariables.length; j++) {
                    if (fmuVariables[j].endsWith(tailFix))
                        targetMapNames.add(fmuVariables[j]);
                }
            }
        }
        if (sourceMapNames.size() != targetMapNames.size()) {
            logger.error("model names not match");
            return;
        }
        for (i = 0; i < sourceMapNames.size(); i++)
            mappingLists.add(new MappingList(sourceMapNames.get(i), targetMapNames.get(i)));
    }

    private BorderPane initFMUTable(int width) {
        //reward table view
        BorderPane rewardPane = new BorderPane();
        opreateVbox = new VBox(50);
        opreateVbox.setPadding(new Insets(10));
        opreateVbox.setPrefWidth(10);
        opreateVbox.setTranslateY(90);
        //System.out.print(modelTree.getParent().getValue()+"00000000000000000000");
        //PrismModel prismModel = (PrismModel)ModelManager.getInstance().modelListMap.get(modelTree.getParent().getValue());
        // table view for showing all loaded reward
        TableView<FMUPath> pathTableView = new TableView<FMUPath>();
        pathTableView.setTooltip(new Tooltip("在表格空白处点击右键，选择需要联合仿真的模型（markov模型，modelica模型）"));
        pathTableView.setTableMenuButtonVisible(true);
        //rewardTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pathTableView.setEditable(true);
        // set data of table view
        pathTableView.setItems(FMULists);
        pathTableView.setMinWidth(width - 100);
        // add columns
        TableColumn<FMUPath, String> pathCol = new TableColumn<FMUPath, String>("Path");
        pathCol.setText("right-click table and select co-simulation model");
        pathCol.setEditable(true);
        pathCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FMUPath, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    CellDataFeatures<FMUPath, String> f) {
                return f.getValue().getPath();
            }
        });
        pathCol.setCellFactory(new Callback<TableColumn<FMUPath, String>, TableCell<FMUPath, String>>() {
            @Override
            public TableCell<FMUPath, String> call(TableColumn<FMUPath, String> f) {
                TextFieldTableCell<FMUPath, String> cell = new TextFieldTableCell<FMUPath, String>(new MyTextConvertor());
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });
        pathCol.setMinWidth((width));
        pathCol.setMaxWidth((width));
        pathCol.setPrefWidth(200);

        pathTableView.getColumns().addAll(pathCol);

        rewardPane.setCenter(pathTableView);
        rewardPane.setRight(opreateVbox);

        pathTableView.setTableMenuButtonVisible(true);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Delete");
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String path = pathTableView.getSelectionModel().getSelectedItems().get(0).toString();
                System.err.println(path);
                String name = path.substring(path.lastIndexOf('/') + 1);
                name = name.substring(0, name.lastIndexOf('.') + 1);
                for (int i = 0; i < mapNames.size(); i++)
                    if (mapNames.get(i).startsWith(name))
                        mapNames.remove(i--);
                FMULists.removeAll(pathTableView.getSelectionModel().getSelectedItems());
                e.consume();
            }
        });
        MenuItem menuItemAdd = new MenuItem("Add FMU");
        menuItemAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String file = ChooseFile("./files/", "FMU(*.fmu)", "*.fmu");
                if (null == file) return;
                fmuVariables = coSimulation.GetFMUVariables(file);
                if (null == fmuVariables || fmuVariables.length == 0) return;
                for (int i = 0; i < fmuVariables.length; i++) {
                    mapNames.add(fmuVariables[i]);
                }
                FMUPath = file;
                FMULists.add(new FMUPath(file));
                FillMappingList();
                e.consume();
            }
        });
        MenuItem menuItemAddMarkov = new MenuItem("Add Markov");
        menuItemAddMarkov.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String file = ChooseFile("./files/", "Markov(*.pm|*.sm)", "*.pm");
                if (null == file) return;
                markovVariable = coSimulation.GetMarkovVariables(file);
                if (null == markovVariable || markovVariable.length == 0) return;
                for (int i = 0; i < markovVariable.length; i++) {
                    System.out.println(markovVariable[i]);
                    mapNames.add(markovVariable[i]);
                }
                prismModelPath = file;
                FMULists.add(new FMUPath(file));
                FillMappingList();
                if (file.endsWith(".pm")) markovType = "dtmc";
                else if (file.endsWith(".cm")) markovType = "ctmc";
                e.consume();
            }
        });
        contextMenu.getItems().addAll(menuItemAdd, menuItemAddMarkov, menuItem);
        pathTableView.setContextMenu(contextMenu);

        return rewardPane;
        //return rewardTableView;
    }

    public class FMUPath {
        private StringProperty path;

        public FMUPath(String path) {
            this.path = new SimpleStringProperty(path);
        }

        public StringProperty getPath() {
            return path;
        }
    }

    private String ChooseFile(String initialDirectory, String name, String extension) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(initialDirectory));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(name, extension);
        fileChooser.getExtensionFilters().add(extFilter);
        String fp;
        File file = fileChooser.showOpenDialog(new Stage());
        fp = file.getAbsolutePath().replace("\\", "/");
//		fp="D:/WorkSpace/Java/win32/modana/fmu.xml";
        //fp="D:/WorkSpace/Java/win32/modana/CTMC.xml";
        if (file != null) {
            return fp;
            //TODO open model!!
            //new PrismModel("","./files/prism.ecore", "PrismModel").LoadFromFile(fp);
//			logger.debug("model opened!");
        }
        return null;
    }
}
