package ecnu.modana.FmiDriver;

import ecnu.modana.alsmc.parameter.HeuristicUI;
import ecnu.modana.alsmc.parameter.ParameterUI;
import ecnu.modana.alsmc.properties.PropertiesUI;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.table.*;

public class Verifier {
	private static TableView resultTable = new TableView();
	//verifier========================================================================
	public static void initVerifierPanel(BorderPane plotTree) {
		SplitPane CenterPane = new SplitPane();
		plotTree.setCenter(CenterPane);
		initTopPane(plotTree);
		SplitPane leftPane = new SplitPane();
		leftPane.setPrefWidth(250);
		leftPane.setMaxWidth(250);
		leftPane.setMinWidth(250);
 		BorderPane rightPane = new BorderPane();
 		CenterPane.setDividerPositions(0.1f);
 		CenterPane.setOrientation(Orientation.HORIZONTAL);
		initLeftPane(leftPane);
		initTopRightPane(rightPane);
 		CenterPane.getItems().addAll(leftPane,rightPane);
// 		BorderPane topRightPane = new BorderPane();
// 		BorderPane bottomRightPane = new BorderPane();
// 		topRightPane.setPrefHeight(400);
// 		topRightPane.setMaxHeight(400);
// 		topRightPane.setMinHeight(400);
// 		rightPane.setDividerPositions(0.1f);
// 		rightPane.setOrientation(Orientation.VERTICAL);
// 		rightPane.getItems().addAll(topRightPane,bottomRightPane);
	}
	
	private static void initLeftPane(SplitPane leftPane) {
     	VBox algorithmVbox = new VBox(10);
		//select algorithm pane
		BorderPane saPane = new BorderPane();
		saPane.setPrefHeight(50);
		saPane.setMaxHeight(50);
		saPane.setMinHeight(50);
		//add parameter pane
		BorderPane pPane = new BorderPane();
		pPane.setPrefHeight(200);
//		apPane.setMinHeight(leftPane.getHeight()*0.4);
		//distribution pane
		TabPane dPane = new TabPane();
		initDistributionPane(dPane);
		dPane.setPrefHeight(300);
//		dPane.setMinHeight(leftPane.getHeight()*0.4);
//		dPane.setMaxHeight(leftPane.getHeight()*0.4);
		Label algLable = new Label("Algorithms:");
		Text algText = new Text("Select SMC Algorithms:");
		algText.setStyle("-fx-font-size: 10pt;");
		CoSimulationUI.algLists.addAll("select SMC algorithm", "SSP",
				   								    "BHT", "SPRT", "BIE",
				   									"Approximate verification markov chain");
		ChoiceBox alg = new ChoiceBox(CoSimulationUI.algLists);
		alg.setMinWidth(170);
		alg.setMaxWidth(170);
		alg.getSelectionModel().select(0);
		//SSP parameter
		Text sspSamplesLabel = new Text("Samples:");
		TextField sspSamplesText = new TextField();
		sspSamplesText.setMinWidth(170);
		sspSamplesText.setMaxWidth(170);
		Text  theta = new Text("theta:");
		TextField thetaField = new TextField();
		Text alpha = new Text("alpha:");
		TextField alphaField = new TextField();
		Text beta = new Text("beta:");
		TextField betaField = new TextField();
		Text delta = new Text("delta:");
		TextField deltaField = new TextField();

		//choose algorithm and show different parameter
		   alg.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue observable, Number oldValue, Number newValue) {
					String alg_name = CoSimulationUI.algLists.get(alg.getSelectionModel().getSelectedIndex());
					if (alg_name.equals("SSP")) {
						Text ssp = new Text("Single Smapling Plan");
						VBox box = new VBox(20);
						box.getChildren().addAll(ssp,sspSamplesLabel,sspSamplesText);
						pPane.getChildren().removeAll(pPane.getChildren());
						pPane.setCenter(box);
					}
					else if (alg_name.equals("BHT")) {
						//BHT parameter
						pPane.getChildren().removeAll(pPane.getChildren());
						Text ssp = new Text("Baysian hypothesis testing");
						VBox box = new VBox();
						box.getChildren().addAll(ssp,theta,thetaField);
						pPane.getChildren().removeAll(pPane.getChildren());
						pPane.setCenter(box);
					}
					else if (alg_name.equals("SPRT")) {

						pPane.getChildren().removeAll(pPane.getChildren());
						Text ssp = new Text("Sequential probability ratio test");

						VBox box = new VBox(10);
						box.getChildren().addAll(ssp,theta,thetaField,alpha,alphaField,beta,betaField,delta, deltaField);
						pPane.getChildren().removeAll(pPane.getChildren());
						pPane.setCenter(box);
					}
					else if (alg_name.equals("BIE")) {

						pPane.getChildren().removeAll(pPane.getChildren());
						Text ssp = new Text("Baysian interval estimation");

						VBox box = new VBox(10);
						box.getChildren().addAll(ssp,theta,thetaField,alpha,alphaField,beta,betaField,delta, deltaField);
						pPane.getChildren().removeAll(pPane.getChildren());
						pPane.setCenter(box);
					}
				}
		   });
		algorithmVbox.getChildren().addAll(algText,alg);
		saPane.getChildren().add(algorithmVbox);
		leftPane.getItems().addAll(saPane,pPane,dPane);
		leftPane.setOrientation(Orientation.VERTICAL);
		leftPane.setDividerPositions(0.1f);
	}
	private static void initTopPane(BorderPane plotTree) {
		    VBox menuPane = new VBox();
		 	initMenu(menuPane);
		 	plotTree.setTop(menuPane);
	}
	   

	private static void initMenu(VBox menuPane) {
		CoSimulationUI.menuBar = new MenuBar();
        Menu Properties= new Menu("Add Properties");
        Menu parameters = new Menu("Setting");
        Menu heuristics = new Menu("Heuristic method");
        MenuItem property = new MenuItem("Property List");
        MenuItem parameter = new MenuItem("Set Parameters");
        MenuItem heuristic = new MenuItem("Heuristic Setting");
        Properties.getItems().addAll(property);
        parameters.getItems().add(parameter);
        heuristics.getItems().add(heuristic);
        CoSimulationUI.menuBar.getMenus().addAll(Properties,parameters,heuristics);
        menuPane.getChildren().add(CoSimulationUI.menuBar);
        
        property.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {	
				try {
					new PropertiesUI().start(new Stage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
        parameters.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {	
				try {
					new ParameterUI().start(new Stage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
        heuristics.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {	
				try {
					new HeuristicUI().start(new Stage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
	}
	private static void initDistributionPane(TabPane dPane){
     	Tab local = new Tab("local");
     	local.setClosable(false);
     	Tab distribution = new Tab("distributed");
     	distribution.setClosable(false);
     	dPane.getTabs().addAll(local,distribution);
     	BorderPane disPane = new BorderPane();
     	Label desc = new Label();
     	desc.setText("set the IP address and port");
     	desc.setStyle("-fx-font-size: 10pt;");
		desc.setMinWidth(Region.USE_PREF_SIZE);
		desc.setMaxWidth(Region.USE_PREF_SIZE);
		VBox vBox = new VBox(18);
		vBox.getChildren().add(desc);
		addHost(vBox);
		Button execute = new Button("Execute");
		execute.setStyle("-fx-font-size: 10pt;");
		execute.setMinWidth(250);
		vBox.getChildren().add(execute);
		disPane.getChildren().addAll(vBox);


		distribution.setContent(disPane);
	}
	private static void addHost(VBox vBox){
		HBox host1Box = new HBox();
		Label ip1 = new Label("Host1--IP:");
		ip1.setMinWidth(Region.USE_PREF_SIZE);
		ip1.setMaxWidth(Region.USE_PREF_SIZE);
		TextField ip1Field = new TextField();
		ip1Field.setMinWidth(120);

		Label port1 = new Label("port:");
		port1.setMinWidth(Region.USE_PREF_SIZE);
		port1.setMaxWidth(Region.USE_PREF_SIZE);
		TextField port1Field = new TextField();
		port1Field.setMinWidth(50);
		host1Box.getChildren().addAll(ip1,ip1Field,port1,port1Field);

		HBox host2Box = new HBox();
		Label ip2 = new Label("Host2--IP:");
		ip2.setMinWidth(Region.USE_PREF_SIZE);
		ip2.setMaxWidth(Region.USE_PREF_SIZE);
		TextField ip2Field = new TextField();
		ip2Field.setMinWidth(120);

		Label port2 = new Label("port:");
		port2.setMinWidth(Region.USE_PREF_SIZE);
		port2.setMaxWidth(Region.USE_PREF_SIZE);
		TextField port2Field = new TextField();
		port2Field.setMinWidth(50);
		host2Box.getChildren().addAll(ip2,ip2Field,port2,port2Field);

		HBox host3Box = new HBox();Label ip3 = new Label("Host4--IP:");
		ip3.setMinWidth(Region.USE_PREF_SIZE);ip3.setMaxWidth(Region.USE_PREF_SIZE);
		TextField ip3Field = new TextField();ip3Field.setMinWidth(120);

		Label port3 = new Label("port:");port3.setMinWidth(Region.USE_PREF_SIZE);
		port3.setMaxWidth(Region.USE_PREF_SIZE);TextField port3Field = new TextField();
		port3Field.setMinWidth(50);
		host3Box.getChildren().addAll(ip3,ip3Field,port3,port3Field);

		HBox host4Box = new HBox();Label ip4 = new Label("Host4--IP:");
		ip4.setMinWidth(Region.USE_PREF_SIZE);ip4.setMaxWidth(Region.USE_PREF_SIZE);
		TextField ip4Field = new TextField();ip4Field.setMinWidth(120);

		Label port4 = new Label("port:");port4.setMinWidth(Region.USE_PREF_SIZE);
		port4.setMaxWidth(Region.USE_PREF_SIZE);TextField port4Field = new TextField();
		port4Field.setMinWidth(50);
		host4Box.getChildren().addAll(ip4,ip4Field,port4,port4Field);
		vBox.getChildren().addAll(host1Box,host2Box,host3Box,host4Box);
	}
	public static void initTopRightPane(BorderPane rightPane){
		VBox resultBox = new VBox(10);
		Label resultLabel = new Label("Execution Result:");
		resultLabel.setStyle("-fx-font-size: 10pt;");
		resultTable.setEditable(true);
		resultLabel.setMinWidth(Region.USE_PREF_SIZE);
		resultLabel.setMaxWidth(Region.USE_PREF_SIZE);
		TableColumn num = new TableColumn("#");
		num.setMinWidth(50);
		num.setCellValueFactory(
				new PropertyValueFactory<Element,String>("num"));
		TableColumn prop = new TableColumn("Property");
		prop.setMinWidth(200);
		prop.setCellValueFactory(
				new PropertyValueFactory<Element,String>("prop"));
		TableColumn time = new TableColumn("Time");
		time.setMinWidth(50);
		time.setCellValueFactory(
				new PropertyValueFactory<Element,String>("time"));
		TableColumn simulationNum = new TableColumn("simulationNum");
		simulationNum.setMinWidth(150);
		simulationNum.setCellValueFactory(
				new PropertyValueFactory<Element,String>("simulationNum"));
		TableColumn pResult = new TableColumn("Positive Sample Number");
		pResult.setMinWidth(150);
		pResult.setCellValueFactory(
				new PropertyValueFactory<Element,String>("pResult"));
		TableColumn posibility = new TableColumn("posibility");
		posibility.setMinWidth(150);

		posibility.setCellValueFactory(
				new PropertyValueFactory<Element,String>("posibility"));
		resultTable.setMinWidth(800);
		resultTable.setMaxWidth(800);
		resultTable.setMinHeight(500);
		final ObservableList<Element> data = FXCollections.observableArrayList(
				new Element("1", "Pr[<=100] (<> disc>=25)", "0.0","200","42","[0.203,0.233]"),
				new Element("2", "Pr[<=100] (<> disc>=25)", "1.0","200","43","[0.209,0.229]"),
				new Element("3", "Pr[<=100] (<> disc>=25)", "2.0","200","46","[0.212,0.232]"),
				new Element("4", "Pr[<=100] (<> disc>=25)", "3.0","200","47","[0.235,0.255]"),
				new Element("5", "Pr[<=100] (<> disc>=25)", "4.0","200","50","[0.251,0.271]")
		);
		resultTable.setItems(data);
		resultTable.getColumns().addAll(num,prop,time,simulationNum,pResult,posibility);
		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().addAll(resultLabel, resultTable);
		rightPane.getChildren().add(vbox);
//		Stage stage = new Stage();
//		Scene scene = new Scene(new Group());
//		stage.setTitle("Table View Sample");
//		stage.setWidth(800);
//		stage.setHeight(500);
//		((Group) scene.getRoot()).getChildren().addAll(vbox);
//
//		stage.setScene(scene);
//		stage.show();
	}
	public static class Element{
		private final SimpleStringProperty num;
		private final SimpleStringProperty prop;
		private final SimpleStringProperty time;
		private final SimpleStringProperty simulationNum;
		private final SimpleStringProperty pResult;
		private final SimpleStringProperty posibility;

		public String getNum() {
			return num.get();
		}

		public SimpleStringProperty numProperty() {
			return num;
		}

		public String getProp() {
			return prop.get();
		}

		public SimpleStringProperty propProperty() {
			return prop;
		}

		public String getTime() {
			return time.get();
		}

		public SimpleStringProperty timeProperty() {
			return time;
		}

		public String getSimulationNum() {
			return simulationNum.get();
		}

		public SimpleStringProperty simulationNumProperty() {
			return simulationNum;
		}

		public String getpResult() {
			return pResult.get();
		}

		public SimpleStringProperty pResultProperty() {
			return pResult;
		}

		public String getPosibility() {
			return posibility.get();
		}

		public SimpleStringProperty posibilityProperty() {
			return posibility;
		}

		public void setNum(String num) {

			this.num.set(num);
		}

		public void setProp(String prop) {
			this.prop.set(prop);
		}

		public void setTime(String time) {
			this.time.set(time);
		}

		public void setSimulationNum(String simulationNum) {
			this.simulationNum.set(simulationNum);
		}

		public void setpResult(String pResult) {
			this.pResult.set(pResult);
		}

		public void setPosibility(String posibility) {
			this.posibility.set(posibility);
		}

		private Element(String num, String prop, String time, String simulationNum, String pResult, String posibility){
			this.num = new SimpleStringProperty(num);
			this.prop = new SimpleStringProperty(prop);
			this.time = new SimpleStringProperty(time);
			this.simulationNum = new SimpleStringProperty(simulationNum);
			this.pResult = new SimpleStringProperty(pResult);
			this.posibility = new SimpleStringProperty(posibility);

		}
	}
}
