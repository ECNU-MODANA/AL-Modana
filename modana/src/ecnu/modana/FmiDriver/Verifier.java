package ecnu.modana.FmiDriver;

import ecnu.modana.alsmc.parameter.HeuristicUI;
import ecnu.modana.alsmc.parameter.ParameterUI;
import ecnu.modana.alsmc.properties.PropertiesUI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Verifier {
	 //verifier========================================================================
     public static void initVerifierPanel(BorderPane plotTree) {
		SplitPane CenterPane = new SplitPane();
		plotTree.setCenter(CenterPane);
		initTopPane(plotTree);
		BorderPane leftPane = new BorderPane();
		leftPane.setPrefWidth(250);
		leftPane.setMaxWidth(250);
		leftPane.setMinWidth(250);
 		SplitPane rightPane = new SplitPane();
 		CenterPane.setDividerPositions(0.1f);
 		CenterPane.setOrientation(Orientation.HORIZONTAL);
 		CenterPane.getItems().addAll(leftPane,rightPane);
 		BorderPane topRightPane = new BorderPane();
 		BorderPane bottomRightPane = new BorderPane();
 		topRightPane.setPrefHeight(400);
 		topRightPane.setMaxHeight(400);
 		topRightPane.setMinHeight(400);
 		rightPane.setDividerPositions(0.1f);
 		rightPane.setOrientation(Orientation.VERTICAL);
 		initLeftPane(leftPane);
 		initTopRightPane(topRightPane);
 		rightPane.getItems().addAll(topRightPane,bottomRightPane);
	}
	
	private static void initTopRightPane(BorderPane topRightPane) {
		
	}
	private static void initLeftPane(BorderPane leftPane) {
		   VBox algorithmVbox = new VBox(10);
		   Label algLable = new Label("Algorithms:");
		   CoSimulationUI.algLists.addAll("select SMC algorithm", "SSP","BHT", "SPRT", "BIE","APMC");
		   ChoiceBox alg = new ChoiceBox(CoSimulationUI.algLists);  
		   alg.setMinWidth(170);
		   alg.setMaxWidth(170);
		   alg.getSelectionModel().select(0);
		   //SSP parameter
		   Label sspSamplesLabel = new Label("Samples:");
		   TextField sspSamplesText = new TextField();
		   sspSamplesText.setMinWidth(170);
		   sspSamplesText.setMaxWidth(170);
		   sspSamplesLabel.setVisible(false);
		   sspSamplesText.setVisible(false);
		   //BHT parameter
		   Label bhtSamplesLabel = new Label("alpha:");
		   TextField bhtSamplesText = new TextField();
		   bhtSamplesLabel.setMinWidth(170);
		   bhtSamplesText.setMaxWidth(170);
		   bhtSamplesLabel.setVisible(false);
		   bhtSamplesText.setVisible(false);
		   //choose algorithm and show different parameter
		   alg.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue observable, Number oldValue, Number newValue) {
					String alg_name = CoSimulationUI.algLists.get(alg.getSelectionModel().getSelectedIndex());
					if (alg_name.equals("SSP")) {
						bhtSamplesLabel.setVisible(false);
						bhtSamplesText.setVisible(false);
						sspSamplesLabel.setVisible(true);
						sspSamplesText.setVisible(true);
					}
					else if (alg_name.equals("BHT")) {
						bhtSamplesLabel.setVisible(true);
						bhtSamplesText.setVisible(true);
						sspSamplesLabel.setVisible(false);
						sspSamplesText.setVisible(false);
					}
				}
		   });
		   algorithmVbox.getChildren().addAll(algLable,alg,sspSamplesLabel,sspSamplesText,bhtSamplesLabel,bhtSamplesText);
		   leftPane.setCenter(algorithmVbox);
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
}
