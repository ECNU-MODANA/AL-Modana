package ecnu.modana.FmiDriver;

import ecnu.modana.PlotComposer.PlotComposer;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Composer {
	public static HBox variableBox = null;
	public static TreeView<String> plotTree = null;
	//plotComposer=================================================================
	 public static void initPlotPanel(BorderPane plotRoot) {
   	 //add menu in PlotComposer
   	 VBox menuPane = new VBox();
		addPlotComposerMenu(menuPane);
		plotRoot.setTop(menuPane);
   	 //add leftPane and rightPane in plotRoot
		SplitPane centerPane = new SplitPane();
		plotRoot.setCenter(centerPane);
		BorderPane leftPane = new BorderPane();
		BorderPane rightPane = new BorderPane();
		rightPane.setPrefWidth(700);
		rightPane.setMaxWidth(700);
		rightPane.setMinWidth(300);
		centerPane.getItems().addAll(leftPane, rightPane);
		centerPane.setDividerPositions(0.1f);
		//add topPane and bottomPane on rightPane
		SplitPane rightCenterPane = new SplitPane();
		rightPane.setCenter(rightCenterPane);
		BorderPane topPane = new BorderPane();
		topPane.setPrefHeight(180);
		topPane.setMaxHeight(180);
		topPane.setMinHeight(120);
		BorderPane bottomPane = new BorderPane();
		rightCenterPane.setDividerPositions(0.1f);
		rightCenterPane.setOrientation(Orientation.VERTICAL);
		rightCenterPane.getItems().addAll(topPane,bottomPane);
		//add plot properties
		addplotproperties(topPane);
		//add plot explorer tree
		addTreePlotExplorer(leftPane, bottomPane);
	}
	private static void addPlotComposerMenu(VBox menuPane) {
	   CoSimulationUI.menuBar = new MenuBar();
       Menu menuExport = new Menu("Export Diagram");
       Menu menuImport = new Menu("Import Data");
       Menu menuHelp = new Menu("Help");
       MenuItem exportItem = new MenuItem("Export JPG Diagram");
       exportDiagram(exportItem);
       MenuItem importItem = new MenuItem("Import Data");
       importDiagram(importItem);
       menuExport.getItems().add(exportItem);
       menuImport.getItems().add(importItem);
       CoSimulationUI.menuBar.getMenus().addAll(menuExport, menuImport,menuHelp);
       menuPane.getChildren().add(CoSimulationUI.menuBar);
		
	}
	private static void importDiagram(MenuItem importItem) {
		importItem.setOnAction(new EventHandler<ActionEvent>() {
       	public void handle(ActionEvent e){
				FileChooser fileChooser = new FileChooser();
				Stage s = new Stage();
				File file = fileChooser.showSaveDialog(s);
				if (file == null)
					return;
				String FilePath = file.getAbsolutePath().toString().replace("\\","/");//.replaceAll(".txt", "")+ ".txt";
       	}
		});
		
	}
	private static void exportDiagram(MenuItem exportItem) {
		exportItem.setOnAction(new EventHandler<ActionEvent>() {
	        	public void handle(ActionEvent e){
					FileChooser fileChooser = new FileChooser();
					FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"JPG files (*.jpg)", "*.jpg");
					fileChooser.getExtensionFilters().add(extFilter);
					Stage s = new Stage();
					File file = fileChooser.showSaveDialog(s);
					if (file == null)
						return;
					String FilePath = file.getAbsolutePath().toString().replace("\\","/");//.replaceAll(".txt", "")+ ".txt";
	        	}
			});
	}
	private static void addplotproperties(BorderPane topPane) {
		Font font2 = new Font(12);
		VBox vBox = new VBox(10);
		//add Title
		HBox hBox1 = new HBox(7);
		Label composerTitle = new Label("       Composer Title:  ");
		composerTitle.setFont(font2);
		CoSimulationUI.TitleField = new TextField();
		CoSimulationUI.TitleField.setId("composerTitle");
		//add X-axis
		HBox hBox2 = new HBox(7);
		Label xAxis = new Label("           X-axis label:   ");
		xAxis.setFont(font2);
		CoSimulationUI.xAxisField = new TextField();
		CoSimulationUI.xAxisField.setId("xAxis");
		//add Y-axis
		HBox hBox3 = new HBox(7);
		Label yAxis = new Label("           y-axis label:   ");
		yAxis.setFont(font2);
		CoSimulationUI.yAxisField = new TextField();
		CoSimulationUI.yAxisField.setId("xAxis");
		Date day=new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		CoSimulationUI.TitleField.setText(df.format(day));
		hBox1.getChildren().addAll(composerTitle,CoSimulationUI.TitleField);
		hBox2.getChildren().addAll(xAxis,CoSimulationUI.xAxisField);
		hBox3.getChildren().addAll(yAxis,CoSimulationUI.yAxisField);
		Text varNameText = new Text("Variable List:");
		vBox.alignmentProperty().setValue(Pos.TOP_LEFT);
		List<String> variableNameList = CoSimulationUI.variableNameList;
		vBox.getChildren().addAll(hBox1,varNameText);
		if(variableNameList!=null && variableNameList.size()>0){
			variableBox = new HBox();
			for(int i = 0;i<variableNameList.size();i++){
				CheckBox cb = new CheckBox(variableNameList.get(i).substring(0,variableNameList.get(i).indexOf("_")));
				cb.setId(variableNameList.get(i));
				variableBox.getChildren().add(cb);
			}
			vBox.getChildren().add(variableBox);
		}else
			vBox.getChildren().add(new Text("simulate first!!"));

		topPane.setLeft(vBox);

	}
	private static void addTreePlotExplorer(BorderPane leftPane,BorderPane bottomPane) {
		CheckBoxTreeItem<String> checkBoxRootItem = new CheckBoxTreeItem<String>(
				"Pick Chart");
		checkBoxRootItem.setExpanded(true);
		plotTree = new TreeView<String>(checkBoxRootItem);
		plotTree.setEditable(true);
		plotTree.setCellFactory(CheckBoxTreeCell.<String> forTreeView());
		CheckBoxTreeItem<String> lineChartCheckItem = new CheckBoxTreeItem<String>(
				"Line Chart");
		CheckBoxTreeItem<String> pieChartCheckItem = new CheckBoxTreeItem<String>(
				"Pie Chart");
		CheckBoxTreeItem<String> areaChartCheckItem = new CheckBoxTreeItem<String>(
				"Area Chart");
		CheckBoxTreeItem<String> bubbleChartCheckItem = new CheckBoxTreeItem<String>(
				"Bubble Chart");
		CheckBoxTreeItem<String> scatterChartCheckItem = new CheckBoxTreeItem<String>(
				"Scatter Chart");
		CheckBoxTreeItem<String> barChartCheckItem = new CheckBoxTreeItem<String>(
				"Bar Chart");
		checkBoxRootItem.getChildren().addAll(lineChartCheckItem,
				pieChartCheckItem, areaChartCheckItem, bubbleChartCheckItem,
				scatterChartCheckItem, barChartCheckItem);
		plotTree.setRoot(checkBoxRootItem);
		plotTree.setShowRoot(true);
		plotTree.addEventHandler(MouseEvent.MOUSE_CLICKED,
						new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent e) {
								List<String> cList = new ArrayList<String>();
							    List<String> checkList = new ArrayList<String>();
								for(TreeItem<String> checkItem :plotTree.getSelectionModel().getSelectedItems()){
									checkList.add(checkItem.getValue());
//									System.out.println(checkItem.getValue());
								}
								try {
									PlotComposer plocCom = new PlotComposer();
									if(CoSimulationUI.trace == null) {
										Stage errorWindow = new Stage();
										errorWindow.setTitle("error");
										errorWindow.setMinHeight(70);
										errorWindow.setMinWidth(130);
										Button button = new Button("ok");
										button.setOnAction(f -> errorWindow.close());
										Text textField = new Text("please simulate first!");
										VBox vBox  = new VBox();
										vBox.getChildren().addAll(textField,button);
										vBox.setAlignment(Pos.CENTER);
										Scene scene = new Scene(vBox);
										errorWindow.setScene(scene);
										errorWindow.showAndWait();
										return;
									}
									if(variableBox !=null){
										ObservableList cbList = variableBox.getChildren();
										for(int i=0;i<cbList.size();i++){
											CheckBox cb = (CheckBox)cbList.get(i);
											String id = "";
											if(cb.isSelected()) {
												cList.add(cb.getId());
											}

										}
									}
									plocCom.SetXYList(CoSimulationUI.trace,cList);
									if (checkList!=null&&checkList.get(0).equals("Bar Chart")) {
										BarChart<String, Number> barChart = plocCom.getBarChart(CoSimulationUI.xAxisField,CoSimulationUI.yAxisField);
										barChart.titleProperty().bindBidirectional(CoSimulationUI.TitleField.textProperty());
										bottomPane.setCenter(barChart);
									} else if(checkList!=null&&checkList.get(0).equals("Line Chart")) {
										LineChart<Object, Number> lineChart = plocCom.getLineChart(CoSimulationUI.xAxisField,CoSimulationUI.yAxisField);
										lineChart.titleProperty().bindBidirectional(CoSimulationUI.TitleField.textProperty());
										bottomPane.setTop(lineChart);
										CoSimulationUI.downPane.setRight(lineChart);
									}

									else {

									}
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							}
						});
		StackPane stackPane = new StackPane();
		stackPane.getChildren().add(plotTree);
		leftPane.setCenter(stackPane);
	}
}
