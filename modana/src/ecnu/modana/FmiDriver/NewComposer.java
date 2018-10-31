package ecnu.modana.FmiDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ecnu.modana.FmiDriver.bean.State;
import ecnu.modana.FmiDriver.bean.Trace;
import ecnu.modana.PlotComposer.PlotComposer;

public class NewComposer {
	public static HBox variableBox = null;
	public static Trace plotTrace = null;
	static int variableIndex = 0;
	//plotComposer=================================================================
	 public static void initPlotPanel(Stage stage, Trace trace) {
		 plotTrace = trace;
		 //add menu in PlotComposer
		 VBox menuPane = new VBox();
		 addPlotComposerMenu(menuPane);	
		 BorderPane plotRoot = new BorderPane();
		 plotRoot.setTop(menuPane);
		 //add leftPane and rightPane in plotRoot
		 BorderPane centerPane = new BorderPane();
		plotRoot.setCenter(centerPane);
		//add topPane and bottomPane on rightPane
		BorderPane bottomPane = new BorderPane();
		plotRoot.setBottom(bottomPane);
		addplotproperties(centerPane, bottomPane);
		Scene scene = SceneBuilder.create().width(800).height(500).root(plotRoot).build();
		stage.setScene(scene);
		stage.show();
		//add plot properties
		//add plot explorer tree
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
	private static void addplotproperties(BorderPane topPane, BorderPane bottomPane) {
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
		List<String> variableNameList = plotTrace.getTraceNames();
		vBox.getChildren().addAll(hBox1,varNameText);
		if(variableNameList!=null && variableNameList.size()>0){
			variableBox = new HBox();
			variableBox.setMaxSize(800, 400);
			for(int i = 0;i<2;i++){
				variableIndex = i;
				CheckBox cb = new CheckBox(variableNameList.get(i));
				cb.addEventHandler(MouseEvent.MOUSE_CLICKED,
						new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent e) {
								List<String> cList = new ArrayList<String>();
							    List<String> checkList = new ArrayList<String>();
								try {
									PlotComposer plocCom = new PlotComposer();
									
									final NumberAxis xAxis = new NumberAxis();
									final NumberAxis yAxis = new NumberAxis();
									xAxis.setLabel(" "+ "X 轴：时间");
									yAxis.setLabel("Y 轴："+ cb.getText());
									final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
									XYChart.Series series = new XYChart.Series();
								 	series.setName("测试统计图");
								 	
								 	for(int i = 0;i<plotTrace.getTraceValues().size();i++){
								 		State state = plotTrace.getTraceValues().get(i);
								 		ArrayList<Object> values = state.values;
								 		series.getData().add(new XYChart.Data(Double.parseDouble((String)values.get(0)), Double.parseDouble(((String)values.get(variableIndex+1)))));
								 		
								 	}
								 	Scene scene  = new Scene(lineChart,600,400);
								 	lineChart.getData().add(series);
								 	lineChart.setMaxHeight(400);
								 	lineChart.setMaxWidth(800);

									bottomPane.setTop(lineChart);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							}
						});
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
		
		StackPane stackPane = new StackPane();
		leftPane.setCenter(stackPane);
	}
}
