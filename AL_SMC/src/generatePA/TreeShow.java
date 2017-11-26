package generatePA;
import java.io.File;
import java.util.List;

import parameter.HeuristicUI;
import parameter.ParameterUI;
import properties.PropertiesUI;
import util.UserFile;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.ExeUppaal;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class TreeShow extends Application {
	public  TreeView<String> treeView = new TreeView<String>();
	public  TreeView<String> treeReduceView = new TreeView<String>();
	public  TreeView<String> treeReduceView2 = new TreeView<String>();
	public Stage result = null;
	TextField pathField = null;
 	TextField queryField = null;
 	TextField douField = null;
 	TextField intField = null;
 	Button buttonSampling = null;
 	Button buttonBuild = null;
 	Button buttonReduce = null;
 	Button buttonReduce2 = null;
 	private MenuBar menuBar = null;
 	public static StringProperty progressLabelString = new SimpleStringProperty();
 	Label progressLabel = null;
    final ProgressBar pb = new ProgressBar(0.0);
    final Text pbText = new Text();
    public static DoubleProperty progressValue = new SimpleDoubleProperty();
    
    public static DoubleProperty finalResultMark = new SimpleDoubleProperty(0.0);
 	
	public void start(Stage treeStage) throws Exception  {
		treeStage.getIcons().add(new Image("modana-logo.png"));
		treeStage.setTitle("AL-SMC");
		treeStage.setWidth(900);
		treeStage.setHeight(600);
		treeStage.centerOnScreen();
		BorderPane plotTree = new BorderPane();
		initPlotPanel(plotTree);
		Scene plotScene = new Scene(plotTree);
		treeStage.setScene(plotScene);
		treeStage.show();
	}
	
	private void initPlotPanel(BorderPane plotTree) {
		SplitPane CenterPane = new SplitPane();
		plotTree.setCenter(CenterPane);
		BorderPane topPane = new BorderPane();
		topPane.setPrefHeight(150);
		topPane.setMaxHeight(180);
		topPane.setMinHeight(150);
		initTopPane(topPane);
 		BorderPane bottomPane = new BorderPane();
 		CenterPane.setDividerPositions(0.1f);
 		CenterPane.setOrientation(Orientation.VERTICAL);
 		CenterPane.getItems().addAll(topPane,bottomPane);
		SplitPane centerBPane = new SplitPane();
		bottomPane.setCenter(centerBPane);
 		BorderPane leftPane = new BorderPane();
 		SplitPane rightPane = new SplitPane();
 		rightPane.setPrefWidth(300);
 		
 		BorderPane leftRightPane = new BorderPane();
 		BorderPane rightRightPane = new BorderPane();
 		rightRightPane.setPrefWidth(400);
 	
 		centerBPane.getItems().addAll(leftPane, rightPane);
 		rightPane.getItems().addAll(leftRightPane,rightRightPane);
 		rightPane.setDividerPositions(0.1f);
		centerBPane.setDividerPositions(0.1f);
		leftPane.setCenter(treeView);
		leftRightPane.setCenter(treeReduceView);
		rightRightPane.setCenter(treeReduceView2);
	}
	
	   private void initTopPane(BorderPane topPane) {
		    VBox menuPane = new VBox();
		 	initMenu(menuPane);
		 	topPane.setTop(menuPane);
		    Font font2 = new Font(12);
			VBox vBox = new VBox(10);
			//add Uppaal Model choose Button
			HBox hBox1 = new HBox(7);
			Label cho = new Label("      ");
			Button getModel = new Button("Choose Uppaal Model:");
			initGetModel(getModel);
			pathField = new TextField();
			pathField.setId("pathField");
			pathField.setPrefWidth(200);
			//choose verify formula
			HBox hBox2 = new HBox(7);
			Label query = new Label("      ");
			Button getQuery = new Button("Choose Query Formula:");
			initGetQuery(getQuery);
			queryField = new TextField();
			queryField.setId("queryField");
			queryField.setPrefWidth(200);
			//add double Number
			HBox hBox3 = new HBox(7);
			Label Dou = new Label(" Input Continuous Variable Number:   ");
			Dou.setFont(font2);
			douField = new TextField();
			douField.setId("douField");
			douField.setPrefWidth(100);
			//add int Number
			HBox hBox4 = new HBox(7);
			Label Int = new Label(" Input Location Number:   ");
			Int.setFont(font2);
			intField = new TextField();
			intField.setId("intField");
			intField.setPrefWidth(100);
			
			hBox1.getChildren().addAll(cho,getModel,pathField);
			hBox2.getChildren().addAll(query,getQuery,queryField);
			hBox3.getChildren().addAll(Dou,douField);
			hBox4.getChildren().addAll(Int,intField);
			
			progressLabel = new Label("Trace training set (N="+UserFile.learnTraceNum+") is generating: ");
			progressLabelString.addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> o, String v0,
						String v1) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							progressLabel.setText("progress--> " + v1);
						}
					});
				}
			});
			
	        final HBox progressHb = new HBox();
	        progressHb.setSpacing(8);
	        progressHb.setAlignment(Pos.CENTER);
	        progressHb.getChildren().addAll(progressLabel, pb, pbText);
	        pbText.setTextAlignment(TextAlignment.LEFT);
			progressValue.addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> o, Number v0,
						Number v1) {
					pb.setProgress(v1.doubleValue());
					pbText.setText((int) (v1.doubleValue() * 100) + "%");
				}
			});

			buttonSampling = new Button("Sampling");
			buttonSampling.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					UserFile.stateDoubleNum = Integer.parseInt(douField.getText().toString());
					UserFile.stateIntNum = Integer.parseInt(intField.getText().toString());
					
					new Thread(new Runnable() {			
						@Override
						public void run() {
							ExeUppaal.exe();
						}
					}).start();
					
				}
			});
			
			buttonBuild = new Button("Build PFT");
			buttonBuild.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					PreTree.createPreTree();
					PreTree.iteratorTree2(PreTree.root);
					treeView.setRoot(new TreeItem<>(PreTree.root.nodeId+"-->f:"+PreTree.root.f+"-->n:"+PreTree.root.n));
					showTree(PreTree.root, treeView.getRoot());
				}
			});
			
			buttonReduce = new Button("Reduce PFT1");
			buttonReduce.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					PreTree.reduce_recur(PreTree.root);
					PreTree.iteratorTree2(PreTree.root);
					TreeItem<String> root= new TreeItem<>(PreTree.root.nodeId+"-->f:"+PreTree.root.f+"-->n:"+PreTree.root.n);
					treeReduceView.setRoot(root);
					showTree(PreTree.root,root);
				}
			});
			
			buttonReduce2 = new Button("Reduce PFT2");
			buttonReduce2.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					PreTree.reduce2_recur(PreTree.root);
					PreTree.iteratorTree2(PreTree.root);
					TreeItem<String> root= new TreeItem<>(PreTree.root.nodeId+"-->f:"+PreTree.root.f+"-->n:"+PreTree.root.n);
					treeReduceView2.setRoot(root);
					showTree(PreTree.root,root);
				}
			});
			
			Button verifyButton = new Button("Check And Return Result");
			
			Task<Void> finalTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {		
					updateProgress(-1, 1);
					BIETool.finalProbability(progressLabelString);
					updateProgress(1, 1);
					pbText.setText("finish!");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							resultShow(BIETool.rs);
						}
					});
					return null;
				}
			};

			verifyButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					pbText.setText("checking...");
					pb.progressProperty().bind(finalTask.progressProperty());
					new Thread(finalTask).start();
				}
			});
			
			HBox hBox5 = new HBox();
			hBox5.alignmentProperty().setValue(Pos.CENTER);
			hBox5.setSpacing(50);
			hBox5.getChildren().addAll(verifyButton, progressHb);
			hBox1.getChildren().addAll(hBox3,buttonSampling, buttonBuild);
			hBox2.getChildren().addAll(hBox4,buttonReduce,buttonReduce2);
			vBox.alignmentProperty().setValue(Pos.CENTER);
			vBox.getChildren().addAll(hBox1,hBox2,hBox5);
			topPane.setCenter(vBox);
	}
	   

	private void initMenu(VBox menuPane) {
		menuBar = new MenuBar();
        Menu Properties= new Menu("Add Properties");
        Menu parameters = new Menu("Setting");
        Menu heuristics = new Menu("Heuristic method");
        MenuItem property = new MenuItem("Property List");
        MenuItem parameter = new MenuItem("Set Parameters");
        MenuItem heuristic = new MenuItem("Heuristic Setting");
        Properties.getItems().addAll(property);
        parameters.getItems().add(parameter);
        heuristics.getItems().add(heuristic);
        menuBar.getMenus().addAll(Properties,parameters,heuristics);
        menuPane.getChildren().add(menuBar);
        
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

	protected void resultShow(List<double[]> rs) {
		result = new Stage();
		result.getIcons().add(new Image("modana-logo.png"));
		result.setOpacity(0.91);
		result.setTitle("ResultView");
		result.setWidth(800);
		result.setHeight(450);
		result.centerOnScreen();
		BorderPane resultShowPane = new BorderPane();
		initResultPanel(resultShowPane,rs);
		Scene resultScene = new Scene(resultShowPane);
		result.setScene(resultScene);
		result.show();
	}

	private void initResultPanel(BorderPane resultShow,List<double[]> rs) {
		VBox vBox = new VBox(8);
		Font font2 = new Font(13);
		HBox[] hBoxs = new HBox[rs.size()];
		for (int i = 0; i < hBoxs.length-1; i++) {
			hBoxs[i] = new HBox(8);
			double[] re = rs.get(i);
			String rsString = "num: "+re[0]+", x:"+re[1]+", n:"+re[2]+", p:"+re[3]+"\n"+
			"-----------------------------------------------------------";
			Label label = new Label(rsString);
			label.setFont(font2);
			hBoxs[i].getChildren().add(label);
			hBoxs[i].alignmentProperty().setValue(Pos.CENTER);
		}
		double[] re = rs.get(rs.size()-1);
		String rsString = "finalResult = ["+(re[0]-UserFile.bietK)+","+(re[0]+UserFile.bietK)+"]";
		Label label = new Label(rsString);
		label.setFont(font2);
		hBoxs[hBoxs.length-1] = new HBox(8);
		hBoxs[hBoxs.length-1].getChildren().add(label);
		hBoxs[hBoxs.length-1].alignmentProperty().setValue(Pos.CENTER);
		vBox.alignmentProperty().setValue(Pos.CENTER);
		vBox.getChildren().addAll(hBoxs);
		resultShow.setCenter(vBox);
	}

	public void initGetModel(Button getModel) {
        getModel.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e){
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File("demo"));
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
						"XML files (*.xml)", "*.xml");
				fileChooser.getExtensionFilters().add(extFilter);
				Stage s = new Stage();
				File file = fileChooser.showOpenDialog(s);
				if (file == null)
					return;
				String path =  file.getAbsolutePath().toString().replace("\\","/");
				UserFile.modelPath = path;
				pathField.setText(path);
        	}
		});
	}
	
	private void initGetQuery(Button getQuery) {
		getQuery.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e){
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File("demo"));
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
						"QUERY files (*.q)", "*.q");
				fileChooser.getExtensionFilters().add(extFilter);
				Stage s = new Stage();
				File file = fileChooser.showOpenDialog(s);
				if (file == null)
					return;
				String path =  file.getAbsolutePath().toString().replace("\\","/");
				UserFile.queryPath = path;
				queryField.setText(path);
        	}
		});
	}
	
	public  void showTree(TreeNode treeNode,TreeItem<String> root)  
	    {  
	        if(treeNode != null)   
	        {     
	            for (TreeNode index : treeNode.childList)   
	            {  
	            StringBuffer NodeId = new StringBuffer();
	            NodeId.append(index.nodeId + "-->f=" + index.f + ",n="+ index.n+": ");
	            for (String endString : index.endList) {
					NodeId.append("+" + endString);
				}
				for (int i = 0; i < index.seqList.size(); i++) {
					NodeId.append(" ["+index.seqList.get(i).nodeId+"(");
					for (String str : index.seqList.get(i).endList) {
						NodeId.append("+"+str);
					}
					NodeId.append(")],");
				}
				TreeItem<String> ti  = new TreeItem<>(NodeId.toString());
				ti.setExpanded(true);
				root.getChildren().add(ti);
				showTree(index, ti);
	            }  
	        } 
	    } 
	
	public static void main(String[] args) {
		launch(args);
	}
}
