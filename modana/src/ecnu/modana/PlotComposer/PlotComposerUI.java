package ecnu.modana.PlotComposer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ecnu.modana.model.ModelManager;
import ecnu.modana.ui.MyTreeItem;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ecnu.modana.PlotComposer.PlotComposer;
/**
 * @author JKQ
 *
 * 2015年11月29日下午3:42:17
 */
public class PlotComposerUI {
	//plotComposer window
     private Stage plotComposerStage = null;
 	//plot menu bar
 	 private MenuBar menuBar = null;
 	 TextField TitleField = null;
 	 TextField xAxisField = null;
 	 TextField yAxisField = null;
     public void start(Stage plotComposerStage) throws Exception  {
    	plotComposerStage.getIcons().add(new Image("modana-logo.png"));
		this.plotComposerStage = plotComposerStage;
		plotComposerStage.initModality(Modality.WINDOW_MODAL);
		plotComposerStage.setOpacity(0.87);
		plotComposerStage.setTitle("Plot Composer");
		plotComposerStage.setWidth(900);
		plotComposerStage.setHeight(600);
		plotComposerStage.centerOnScreen();
		plotComposerStage.setResizable(false);
		BorderPane plotRoot = new BorderPane();
		initPlotPanel(plotRoot);
		Scene plotScene = new Scene(plotRoot);
		plotComposerStage.setScene(plotScene);
		plotComposerStage.show();
	}
     private void initPlotPanel(BorderPane plotRoot) {
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
		topPane.setPrefHeight(110);
		topPane.setMaxHeight(180);
		topPane.setMinHeight(70);
 		BorderPane bottomPane = new BorderPane();
 		rightCenterPane.setDividerPositions(0.1f);
 		rightCenterPane.setOrientation(Orientation.VERTICAL);
 		rightCenterPane.getItems().addAll(topPane,bottomPane);
 		//add plot properties
 		addplotproperties(topPane);
 		//add plot explorer tree
 		addTreePlotExplorer(leftPane, bottomPane);
	}
	private void addPlotComposerMenu(VBox menuPane) {
		menuBar = new MenuBar();
        Menu menuExport = new Menu("Export Diagram");
        Menu menuImport = new Menu("Import Data");
        Menu menuHelp = new Menu("Help");
        MenuItem exportItem = new MenuItem("Export JPG Diagram");
        exportDiagram(exportItem);
        MenuItem importItem = new MenuItem("Import Data");
        importDiagram(importItem);
        menuExport.getItems().add(exportItem);
        menuImport.getItems().add(importItem);
        menuBar.getMenus().addAll(menuExport, menuImport,menuHelp);
        menuPane.getChildren().add(menuBar);
		
	}
	private void importDiagram(MenuItem importItem) {
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
	private void exportDiagram(MenuItem exportItem) {
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
	private void addplotproperties(BorderPane topPane) {
		Font font2 = new Font(12);
		VBox vBox = new VBox(10);
		//add Title
		HBox hBox1 = new HBox(7);
		Label composerTitle = new Label("       Composer Title:  ");
		composerTitle.setFont(font2);
		TitleField = new TextField();
		TitleField.setId("composerTitle");
		//add X-axis
		HBox hBox2 = new HBox(7);
		Label xAxis = new Label("           X-axis label:   ");
		xAxis.setFont(font2);
		xAxisField = new TextField();
		xAxisField.setId("xAxis");
		//add Y-axis
		HBox hBox3 = new HBox(7);
		Label yAxis = new Label("           y-axis label:   ");
		yAxis.setFont(font2);
		yAxisField = new TextField();
		yAxisField.setId("xAxis");
		hBox1.getChildren().addAll(composerTitle,TitleField);
		hBox2.getChildren().addAll(xAxis,xAxisField);
		hBox3.getChildren().addAll(yAxis,yAxisField);
		vBox.alignmentProperty().setValue(Pos.CENTER);
		vBox.getChildren().addAll(hBox1,hBox2,hBox3);
//		StackPane stackPane = new StackPane();
//	    stackPane.getChildren().add(vBox);
		topPane.setCenter(vBox);
	}
	private void addTreePlotExplorer(BorderPane leftPane,BorderPane bottomPane) {
		CheckBoxTreeItem<String> checkBoxRootItem = new CheckBoxTreeItem<String>(
				"Pick Chart");
		checkBoxRootItem.setExpanded(true);
		TreeView<String> plotTree = new TreeView<String>(checkBoxRootItem);
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
							    List<String> checkList = new ArrayList<String>();
								for(TreeItem<String> checkItem :plotTree.getSelectionModel().getSelectedItems()){
									checkList.add(checkItem.getValue());
//									System.out.println(checkItem.getValue());
								}
								try {
									PlotComposer plocCom = new PlotComposer();
									plocCom.SetXYList();
									if (checkList!=null&&checkList.get(0).equals("Bar Chart")) {
										BarChart<String, Number> barChart = plocCom.getBarChart(xAxisField,yAxisField);
										barChart.titleProperty().bindBidirectional(TitleField.textProperty());
										bottomPane.setCenter(barChart);
									} else if(checkList!=null&&checkList.get(0).equals("Line Chart")) {
										LineChart<Object, Number> lineChart = plocCom.getLineChart(xAxisField,yAxisField);
										lineChart.titleProperty().bindBidirectional(TitleField.textProperty());
										bottomPane.setCenter(lineChart);
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
	public Stage getComposerStage() {
 		return plotComposerStage;
 	}
}
