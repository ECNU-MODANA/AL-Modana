package ecnu.modana.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import org.apache.log4j.Logger;
import org.ptolemy.fmi.FMUFile;

import ecnu.modana.Modana;
import ecnu.modana.FmiDriver.CoSimulation;
import ecnu.modana.FmiDriver.CoSimulationUI;
import ecnu.modana.FmiDriver.FMUModelExchange;
import ecnu.modana.FmiDriver.PrismClient;
import ecnu.modana.FmiDriver.PrismWrapper;
import ecnu.modana.abstraction.IUserInterface;
import ecnu.modana.alsmc.generatePA.TreeShow;
import ecnu.modana.base.PluginMessage;
import ecnu.modana.model.AbstractModel;
import ecnu.modana.model.ModelManager;
import ecnu.modana.model.ModelManager.DiagramType;
import ecnu.modana.model.ModelManager.ModelType;
import ecnu.modana.model.PrismModel;
import ecnu.modana.model.PrismModel.*;
import ecnu.modana.ui.prism.PrismModule;
import ecnu.modana.util.MyLineChart;
import ecnu.modana.PlotComposer.PlotComposerUI;
import ecnu.modana.Properties.PropertiesUI;
/**
 * Default GUI of Modana Platform
 * @author cb
 */
@SuppressWarnings("all")
public class ModanaUI extends Application implements IUserInterface {
//	private ModanaUI()
//	{
//		//initPanel(new BorderPane());
//	}
//	private static ModanaUI modanaUI=null;
//	public static ModanaUI getInstance() {  
//        if (modanaUI == null) {    
//            synchronized (ModelManager.class) {    
//               if (modanaUI == null) {    
//            	   modanaUI = new ModanaUI();   
//               }    
//            }    
//        }    
//        return modanaUI;   
//    }  
	
	Logger logger = Logger.getRootLogger();
	
	//default window size
	private final int windowWidth = 1080, windowHeight = 800;
	//minimum window size
	private final int winMinWidth = 700, winMinHeight = 480;
	//default main editor width
	private final int editorPrefWidth = 875;
	//width bound of model explorer tree and snapshot
	private final int treeMinWidth = 205, treeMaxWidth = 300;
	//snapshot constant size
	private final int snapshotWidth = 200, snapshotHeight = 200;
	
	//main window
	private Stage primaryStage = null;
	//main menu bar
	private MenuBar menuBar = null;
	//graphical editor tab pane
	private TabPane tabPane = null;
	//model explorer tree
	private TreeView<String> modelTreeView = null;
	//snapshot canvas
	private Canvas snapshotCanvas = null;
	//ModanaUI module map
	private HashMap<String, MuiModule> moduleMap = null;

	@Override
	public void recvMsg(PluginMessage pMsg, Object[] data) {}

	@Override
	public String getName() {
		return "ModanaUI";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String getDescription() {
		return "ModanaUI is a JavaFX-based GUI of Modana Platform by default.";
	}

	@Override
	public void launchWithParam(String[] paramValues) {
		if (Logger.getRootLogger().isDebugEnabled()) {
			Logger.getRootLogger().debug("ModanaUI launched!");
		}
		launch(paramValues); //for launching JavaFX window application
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.getIcons().add(new Image("modana-logo.png"));
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root);
		initPanel(root);

		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		if (bounds.getWidth() > windowWidth && bounds.getHeight() > windowHeight) {
			primaryStage.setWidth(windowWidth);
			primaryStage.setHeight(windowHeight);
		} else {
			primaryStage.setMaximized(true);
		}
		if (bounds.getWidth() > winMinWidth && bounds.getHeight() > winMinHeight) {
			primaryStage.setMinWidth(winMinWidth);
			primaryStage.setMinHeight(winMinHeight);
		} else {
			primaryStage.setMinWidth(bounds.getWidth());
			primaryStage.setMinHeight(bounds.getHeight());
		}
		primaryStage.setTitle(getName() + " " + getVersion());
		primaryStage.setScene(scene);
		primaryStage.show();

		/////IMPORTANT/////
		//replacing old instance with this new application instance FOR consistency
		Modana.getInstance().addOrReplacePluginInstance(this);
	}
	
	/**
	 * get main window
	 * @return main window
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	/**
	 * initialize borderLayout root panel
	 * @param root root panel
	 */
	private void initPanel(BorderPane root) {
		//get all module plugins
		moduleMap = new HashMap<String, MuiModule>();
		ArrayList<MuiModule> list = Modana.getInstance().getPluginInstancesByType(MuiModule.class);
		for (MuiModule muiModule : list) {
			moduleMap.put(muiModule.getClass().getName(), muiModule);
		}
		//add menu bar
		VBox menuPane = new VBox();
		newNewBarWithModelMenu(menuPane);
		root.setTop(menuPane);
		
		SplitPane centerPane = new SplitPane();
		root.setCenter(centerPane);
		
		BorderPane leftPane = new BorderPane();
		tabPane = new TabPane();
		//set property of tab pane
		tabPane.setPrefWidth(editorPrefWidth);

		centerPane.getItems().addAll(leftPane, tabPane);
		centerPane.setDividerPositions(0.1f);
		//add model explorer tree
		addTreeModelExplorer(leftPane);
		//add snapshot of graphical editor
		addSnapshot(leftPane);
		
	}
	public void AddModelTree(String className)
	{
		MuiModule muiModule=moduleMap.get(className);
		if(muiModule==null)
		{
			System.err.println("can not find class when add new model:"+className);
			System.err.println(moduleMap.values());
			return;
		}
		TreeItem<String> newTreeItem = muiModule.createTreeModelExplorer("PrismModel");
		modelTreeView.getRoot().getChildren().add(newTreeItem);
	}
	/**
	 * create menu bar and add model menu
	 * @param root root panel
	 */
	private void newNewBarWithModelMenu(VBox root) {
		menuBar = new MenuBar();
		//main model menu (always shown)
        Menu menuModel = new Menu("Model");
        Menu menuNewModel = new Menu("New Model");
        //add sub-menus of new model menu
        Collection<MuiModule> collection = moduleMap.values();
      
        for (MuiModule muiModule : collection) {
			MenuItem item = new MenuItem(muiModule.getName()+"-"+muiModule.getVersion());
			Stage CreateModelWindow = new Stage(StageStyle.DECORATED);
			CreateModelWindow.getIcons().add(new Image("modana-logo.png"));
			CreateModelWindow.initModality(Modality.WINDOW_MODAL);
			CreateModelWindow.setOpacity(0.87);
			CreateModelWindow.setTitle("New"+"  "+muiModule.getName()+"-"+muiModule.getVersion());
			CreateModelWindow.setWidth(400);
			CreateModelWindow.setHeight(200);
			CreateModelWindow.centerOnScreen();
			CreateModelWindow.setResizable(false);
			VBox vBox = new VBox(10);
			Font font1 = new Font(16);
			Font font2 = new Font(12);
			//add head label
			Label label1 = new Label("  "+muiModule.getName()+muiModule.getVersion());
			label1.setFont(font1);
			Label label2 = new Label("     Create  a"+"  New  "+muiModule.getName()+muiModule.getVersion()+"  Model");
			label2.setFont(font2);
			//add modelField
			HBox hBox1 = new HBox(7);
			Label modelFieldLabel = new Label("           Model Name:  ");
			modelFieldLabel.setFont(font2);
			TextField modelField = new TextField();
			modelField.setId("modelName");
			//add DiagramField
			HBox hBox2 = new HBox(7);
			Label diagramFieldLabel = new Label("       Diagram Name:   ");
			diagramFieldLabel.setFont(font2);
			TextField diagramField = new TextField();
			diagramField.setId("diagramName");
			//add Button
			HBox hBox3 = new HBox(7);
			Button cancel = new Button("Cancel");
			Button finish = new Button("Finish");
			//set Button Action
			cancel.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
				public void handle(Event e) {		
					CreateModelWindow.close();
				}
				} );
			finish.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
				public void handle(Event e) {
					System.out.println(modelField.getText()+diagramField.getText()+"+++++++++++++++");
					AddModel(ModelType.PrismModel, modelField.getText());
					AddDiagram(modelField.getText(),diagramField.getText());
					CreateModelWindow.close();
				}
				} );
			//add label or field or button to hbox 
			hBox1.getChildren().addAll(modelFieldLabel,modelField);
			hBox2.getChildren().addAll(diagramFieldLabel,diagramField);
			hBox3.getChildren().addAll(cancel,finish);
			hBox3.setAlignment(Pos.CENTER);
			//add label or hbox to vbox
			vBox.getChildren().addAll(label1,label2,hBox1,hBox2,hBox3);
			Scene scene = new Scene(vBox);
			CreateModelWindow.setScene(scene);
//			MuiModule tModule=new PrismModule();
//			tModule.AddModelContextMenu();
//	        modelTreeView.setContextMenu(new ContextMenu());
	        item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
//					modelTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED,
//							new EventHandler<MouseEvent>() {
//						@Override
//						public void handle(MouseEvent e) {
//							//if(e.getButton()==MouseButton.SECONDARY) 
//							{
//								System.err.println("ah");
//							}
//						}});
					CreateModelWindow.show();
//					//create new tree item
//					TreeItem<String> newTreeItem = muiModule.createTreeModelExplorer("PrismModel");
//					modelTreeView.getRoot().getChildren().add(newTreeItem);
					
					
//					AddModel(ModelType.PrismModel, "PrismModel");
//					AddDiagram("PrismModel","PrismDiagram");
				}
			});
	        //add item to menuNewModel
	        menuNewModel.getItems().add(item);
		}

        
        
        MenuItem menuItemSaveModel = new MenuItem("Save Model...");
        menuItemSaveModel.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e){
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File("D:/"));
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
						"XML files (*.xml)", "*.xml");
				fileChooser.getExtensionFilters().add(extFilter);
				Stage s = new Stage();
				File file = fileChooser.showSaveDialog(s);
				if (file == null)
					return;
				String FilePath=null;
				FilePath = file.getAbsolutePath().toString().replace("\\","/");//.replaceAll(".txt", "")+ ".txt";
				//FilePath="D:/1.xml";
				if(modelTreeView.getSelectionModel().getSelectedItem().isLeaf()){
				    System.err.println(modelTreeView.getSelectionModel().getSelectedItem().getParent().getValue()+"============");
        		    ModelManager.getInstance().SaveModel(modelTreeView.getSelectionModel().getSelectedItem().getParent().getValue(), FilePath);
				}
				else{
					ModelManager.getInstance().SaveModel(modelTreeView.getSelectionModel().getSelectedItem().getValue(), FilePath);
				}
//        		save(FilePath);
        	}
		});
        
        
        
        MenuItem menuItemOpenModel = new MenuItem("Open Model...");
        
        final FileChooser fileChooser = new FileChooser();
		menuItemOpenModel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")+"/files"));
				fileChooser.setTitle("Choose an xml which will be transformed into prism model");
				 fileChooser.getExtensionFilters().addAll(
			                new FileChooser.ExtensionFilter("XML", "*.xml*")
//			                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
//			                new FileChooser.ExtensionFilter("PNG", "*.png")
			            );
				String fp;
//				fp="D:/WorkSpace/Java/win32/modana/fmu.xml";
				//fp="D:/WorkSpace/Java/win32/modana/CTMC.xml";
				File file = fileChooser.showOpenDialog(primaryStage);
				fp=file.getAbsolutePath().replace("\\", "/");
				if (file != null) {
					//TODO open model!!
					new PrismModel("","./files/prism.ecore", "PrismModel").LoadFromFile(fp);
					logger.debug("model opened!");
				}				
			}
		});
        menuModel.getItems().addAll(menuNewModel,menuItemSaveModel, menuItemOpenModel);
        
        //add user-module main menu
        addUserModuleMainMenu();
        
        //help menu
        Menu menuHelp = new Menu("Help");
        addHelpMenuItem(menuHelp);
        //tool menu
        Menu menuTool = new Menu("Tool");
        addToolMenuItem(menuTool);
        //properties menu
       /* Menu menuProperties = new Menu("Properties");
        addPropertiesMenuItem(menuProperties);*/
        //Co-Simulation
        Menu menuSim = new Menu("Co-Simulation");
        addSimulationMenuItem(menuSim);
        //Al-Smc Verifier
        Menu alsmc = new Menu("Al-Smc");
        addAlSmcMenuItem(alsmc);
        menuBar.getMenus().addAll(menuModel,menuSim,alsmc,menuTool, menuHelp);
        //menuBar.getMenus().addAll(menuModel,menuProperties,menuSim,alsmc,menuTool, menuHelp);
        root.getChildren().add(menuBar);
	}
	private void addAlSmcMenuItem(Menu alsmc) {
		// Add Properties
				MenuItem addPropertiesList = new MenuItem("Al-smc");
				addPropertiesList.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {	
						try {
							new TreeShow().start(new Stage());
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
		        
				alsmc.getItems().add(addPropertiesList);
		
	}

	private void addSimulationMenuItem(Menu menu) {
		// Add Properties
		MenuItem addPropertiesList = new MenuItem("Co-Simulation");
		addPropertiesList.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {	
				try {
					new CoSimulationUI().start(new Stage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
		menu.getItems().add(addPropertiesList);
	}
	/*private void addPropertiesMenuItem(Menu menuProperties) {
		// Add Properties
		MenuItem addPropertiesList = new MenuItem("Add Properties List");
		addPropertiesList.setOnAction(new EventHandler<ActionEvent>() {
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
        
		menuProperties.getItems().add(addPropertiesList);
	}*/

	private void addToolMenuItem(Menu menuTool) {
		//Create PlotComposer
		MenuItem plotComposer = new MenuItem("Plot Composer");
		plotComposer.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {	
				try {
					new PlotComposerUI().start(new Stage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
		menuTool.getItems().add(plotComposer);
		
	}
	/**
	 * Find the TreeItem via modelName;
	 */
	private HashMap<String, TreeItem<String>> treeModelMap=new HashMap<>();
	public boolean AddModel(ModelType modelType,String modelName)
	{
		if(treeModelMap.containsKey(modelName))
		{
			System.err.println("modelname:"+modelName+" already contains!");
			return false;
		}
		TreeItem<String> newTreeItem =new PrismModule().createTreeModelExplorer(modelName);
		modelTreeView.getRoot().getChildren().add(newTreeItem);
		treeModelMap.put(modelName, newTreeItem);
		//newTreeItem.getChildren().add(AddDiagram(modelName, "PrismDiagram"));
		return true;
	}
	public MyTreeItem AddDiagram(String modelName,String diagramName)
	{
        //diagramName=getName();
		AbstractModel model=ModelManager.getInstance().GetModel(modelName);
		if(null==model)
		{
			System.err.println("model not find:"+modelName);
			return null;
		}
        MyTreeItem treeItem = new MyTreeItem();
        treeItem.setValue(diagramName);
//       System.out.println(modelTreeView.getSelectionModel().getSelectedIndex()+"---");
    		//check if there is tab selected
			Tab oldTab = tabPane.getSelectionModel().getSelectedItem();
			PrismModule prismModule=new PrismModule();
			prismModule.setName(modelName);
			//create new tab
			MuiGEditor muiGEditor=prismModule.createGaphicalEditor();
			Tab tab = muiGEditor.buildEditorTab(
					diagramName, prismModule, primaryStage, tabPane, 
				snapshotCanvas, menuBar, modelTreeView, treeItem);
			tab.textProperty().bindBidirectional(treeItem.valueProperty());
//			tab.setText(treeItem.getValue().toString());
			//select new created tab
			tabPane.getSelectionModel().select(tab);
			treeItem.setMyTab(tab);
			//manually send select event message to new tab
			if (oldTab != null) {
				Event.fireEvent(oldTab, new Event(Tab.SELECTION_CHANGED_EVENT));
			}
			Event.fireEvent(tab, new Event(Tab.SELECTION_CHANGED_EVENT));
          ModelManager.getInstance().CreateDiagram(muiGEditor,modelName,DiagramType.PrismDiagram, diagramName);
          treeModelMap.get(modelName).getChildren().add(treeItem);
		return treeItem;
	}

	private void addUserModuleMainMenu() {
		Collection<MuiModule> collection = moduleMap.values();
		for (MuiModule muiModule : collection) {
			if (muiModule.getMainMenu() != null) {
				menuBar.getMenus().add(muiModule.getMainMenu());
			}
		}
	}

	//for display loaded plugins in table view
	static class Plugin {
        private StringProperty name;
 
        public Plugin(String name) {
            this.name = new SimpleStringProperty(name);
        }

		public StringProperty getName() {
			return name;
		}
    }
	static class Message {
		private StringProperty name;
		private StringProperty receiver;
		
		public Message(String name, String receiver) {
			this.name = new SimpleStringProperty(name);
			this.receiver = new SimpleStringProperty(receiver);
		}

		public StringProperty getName() {
			return name;
		}

		public StringProperty getReceiver() {
			return receiver;
		}	
	}
	/**
	 * add help menu item
	 * @param menuHelp help menu
	 */
	private void addHelpMenuItem(Menu menuHelp) {	 
        MenuItem menuItemLoadedPlugins = new MenuItem("Show Loaded Plugins&Msgs");
    	Stage pluginWindow = new Stage(StageStyle.DECORATED);
    	pluginWindow.getIcons().add(new Image("modana-logo.png"));
		pluginWindow.initModality(Modality.WINDOW_MODAL);
		pluginWindow.setOpacity(0.87);
		pluginWindow.setTitle("Loaded  Plugins and Messages");
		pluginWindow.setWidth(453);
		pluginWindow.setHeight(450);
		pluginWindow.centerOnScreen();
		
		///plugin table view
		ObservableList<Plugin> pluginList = FXCollections.observableArrayList();
		//table view for showing all loaded plugins
		TableView<Plugin> pluginTableView = new TableView<Plugin>();
		pluginTableView.setEditable(false);
		//set data of table view
		pluginTableView.setItems(pluginList);
		//add columns
		TableColumn<Plugin, String> pluginNameCol = new TableColumn<Plugin, String>("Plugin Name");
		pluginNameCol.setCellValueFactory(
			new Callback<TableColumn.CellDataFeatures<Plugin,String>, ObservableValue<String>>() {
				
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<Plugin, String> p) {
				return p.getValue().getName();
			}
		});
		pluginNameCol.setMinWidth(300);
		pluginNameCol.setPrefWidth(450);
		pluginTableView.getColumns().add(pluginNameCol);
		//add data to observable list
		Set<String> set = Modana.getInstance().getAllPluginClasses();
		for (String string : set) {
			pluginList.add(new Plugin(string));
		}
		
		///message table view
		ObservableList<Message> msgList = FXCollections.observableArrayList();
		//table view for showing all loaded messages
		TableView<Message> msgTableView = new TableView<Message>();
		msgTableView.setEditable(false);
		//set data of table view
		msgTableView.setItems(msgList);
		//add columns
		TableColumn<Message, String> msgNameCol = new TableColumn<Message, String>("Message Name");
		msgNameCol.setCellValueFactory(
			new Callback<TableColumn.CellDataFeatures<Message,String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(
					CellDataFeatures<Message, String> m) {
				return m.getValue().getName();
			}
		});
		msgNameCol.setMinWidth(150);
		msgNameCol.setPrefWidth(200);
		TableColumn<Message, String> msgReceiverCol = new TableColumn<Message, String>("Receiver");
		msgReceiverCol.setCellValueFactory(
			new Callback<TableColumn.CellDataFeatures<Message,String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(
					CellDataFeatures<Message, String> m) {
				return m.getValue().getReceiver();
			}
		});
		msgReceiverCol.setMinWidth(150);
		msgReceiverCol.setPrefWidth(250);
		msgTableView.getColumns().add(msgNameCol);
		msgTableView.getColumns().add(msgReceiverCol);
		//add data to observable list
		Collection<PluginMessage> collection = Modana.getInstance().getAllMessages();
		for (PluginMessage pMsg : collection) {
			msgList.add(new Message(pMsg.getName(), pMsg.getReceiverClass()));
		}
		
		VBox vBox = new VBox(7);
		vBox.getChildren().addAll(pluginTableView, msgTableView);
		Scene scene = new Scene(vBox);
		pluginWindow.setScene(scene);
		
        menuItemLoadedPlugins.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {	
				pluginWindow.show();
			}
		});
        
        menuHelp.getItems().add(menuItemLoadedPlugins);
	}
	
	/**
	 * add tree model explorer
	 * @param root root panel
	 */
	private void addTreeModelExplorer(BorderPane root) {
        root.setMaxWidth(treeMaxWidth);
        root.setMinWidth(treeMinWidth);
        modelTreeView = new TreeView<String>();
        root.setCenter(modelTreeView);
        //set root tree item without showing root item
        modelTreeView.setRoot(new TreeItem<String>());
        modelTreeView.setShowRoot(false);
        
        modelTreeView.setCellFactory(new Callback<TreeView<String>,TreeCell<String>>(){
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                return new TextFieldTreeCellImpl();
            }
        });
        modelTreeView.setEditable(true);
	}
	
	/**
	 * add snapshot panel
	 * @param root root panel
	 */
	private void addSnapshot(BorderPane root) {
		snapshotCanvas = new Canvas(snapshotWidth, snapshotHeight);
		BorderPane snapshotPane = new BorderPane();
		root.setBottom(snapshotPane);
		snapshotPane.setCenter(snapshotCanvas);
	}
	private final class TextFieldTreeCellImpl extends TreeCell<String> {
        private TextField textField;
        private ContextMenu addMenu = new ContextMenu();
        private ContextMenu addLeafMenu = new ContextMenu();
        public TextFieldTreeCellImpl() {
        	 MenuItem addMenuItem = new MenuItem("Add PrismDiagram");
             MenuItem addMenuItem0 = new MenuItem("Check Model");
             MenuItem addMenuItem1 = new MenuItem("Delete Model");
             MenuItem fmItem=new MenuItem("co-simulation");
             MenuItem addLeafMenuItem0 = new MenuItem("Open Diagram");
             MenuItem addLeafMenuItem1 = new MenuItem("delete Diagram");
             addMenu.getItems().addAll(addMenuItem,addMenuItem0,addMenuItem1,fmItem);
             fmItem.setOnAction(new EventHandler<ActionEvent>() {
  				@Override
  				public void handle(ActionEvent event) {
  					try {
  						FileChooser fileChooser=new FileChooser();
  						fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")+"/files"));
  						fileChooser.setTitle("Choose an fmu model");
  						 fileChooser.getExtensionFilters().addAll(
  					                new FileChooser.ExtensionFilter("FMU", "*fmu*")
//  					                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
//  					                new FileChooser.ExtensionFilter("PNG", "*.png")
  					            );
  						String fp;
  						File file = fileChooser.showOpenDialog(primaryStage);
  						fp=file.getAbsolutePath().replace("\\", "/");
  						if(null==file) return;
 						//new MyCoSimulation().simulate("./bouncingBall.fmu", 2, 0.01, false, ',', "./fmuOut.txt").start(new Stage());
 						//new FMUModelExchange().simulate("./me_bouncingBall.fmu", 2, 0.01, true, ',', "./exchangeOut.txt").start(new Stage());
 						//new FMUModelExchange().simulate("./me_bouncingBall.fmu", 10, 0.04, false, ',', "./1.xml").start(new Stage());
 						
 						TreeItem c = (TreeItem)modelTreeView.getSelectionModel().getSelectedItem();
 						//ModelManager.getInstance().GetModel(treeItem.getValue())
 				        PrismModel model=(PrismModel) ModelManager.getInstance().GetModel(c.getValue().toString());
 				        LineChart<Object, Number> lineChart=null;
				        //lineChart=model.CoSimulation("./files/MyBouncingBall.fmu", 5.5, 0.01/2, false, "./1.xml");
 				        lineChart=model.CoSimulation(fp, 5.5, 0.01/2, false, "./files/CosimulationResult.csv");
				        lineChart.getStylesheets().add("LineChart.css");
				        Scene scene  = new Scene(lineChart,800,600);
				        scene.getStylesheets().add("./Stage.css");
				        Stage stage=new Stage();
				        stage.setScene(scene);
				        stage.show();
				        return;
 				        
 				        //MyLineChart myLineChart=null;
 				       //myLineChart=model.CoSimulation("./MyBouncingBall.fmu", 5.5, 0.01/2, false, "./1.xml"); 				       
 				       //myLineChart=model.CoSimulation("./ctmcBouncingBall.fmu", 5.5, 0.01, false, "./1.xml");
  				       //myLineChart.SetTitle("1", "DTMC co-simulate with BouncingBall");
  				       
// 				       myLineChart=new CoSimulation("127.0.0.1",40000).ctmcMulti("./CTMC.pm", "ctmc", FMUFile.parseFMUFile("./tryCtmcBouncingBall.fmu"), 50, 0.02, 100);
// 				    		   //("./ctmcBouncingBall.fmu", 5.5, 0.01, false, "./1.xml");
// 				       myLineChart=model.CoSimulation("./tryCtmcBouncingBall.fmu", 5.5, 0.01, false, "./1.xml");
// 				       myLineChart.SetTitle("1", "CTMC co-simulate with BouncingBall");
 				       
 				       //myLineChart.start(new Stage());
 				       
// 				       long startTime=new Date().getTime();
// 				       PrismClient.getInstance();
// 				       int n=200;
// 				       double stepsize=0.001;
// 				       for(int k=0;k<n;k++)
// 				       {
//	 				       model.CoSimulationTrace("./MyBouncingBall.fmu", 5.5, stepsize, false, "./1.xml");
// 				       }
// 				       System.err.println("time:"+(new Date().getTime()-startTime));
// 				       startTime=new Date().getTime();
// 				       for(int k=0;k<n;k++)
//				       {
//	 				       model.CoSimulationTradition("./MyBouncingBall.fmu", 5.5, stepsize, false, "./1.xml");
//				       }
// 				       System.err.println("time:"+(new Date().getTime()-startTime));
// 				       logger.debug("time:"+(new Date().getTime()-startTime));
// 				        
 				       //model.CoSimulation("./ctmcBouncingBall.fmu", 8, 0.01, false, "./1.xml").start(new Stage());
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}	
 				}
  			});
            addLeafMenu.getItems().addAll(addLeafMenuItem0,addLeafMenuItem1);
            Stage createDiagramWindow = new Stage(StageStyle.DECORATED);
            createDiagramWindow.getIcons().add(new Image("modana-logo.png"));
            createDiagramWindow.initModality(Modality.WINDOW_MODAL);
            createDiagramWindow.setOpacity(0.87);
            createDiagramWindow.setTitle("New"+"  Diagram");
            createDiagramWindow.setWidth(400);
            createDiagramWindow.setHeight(200);
            createDiagramWindow.centerOnScreen();
            createDiagramWindow.setResizable(false);
			VBox vBox = new VBox(10);
			Font font1 = new Font(16);
			Font font2 = new Font(12);
			//add head label
			Label label1 = new Label("  ");
			label1.setFont(font1);
			Label label2 = new Label("     Create  a"+"  New Diagram");
			label2.setFont(font2);
			//add DiagramField
			HBox hBox2 = new HBox(7);
			Label diagramFieldLabel = new Label("       Diagram Name:   ");
			diagramFieldLabel.setFont(font2);
			TextField diagramField = new TextField();
			diagramField.setId("diagramName");
			//add Button
			HBox hBox3 = new HBox(7);
			Button cancel = new Button("Cancel");
			Button finish = new Button("Finish");
			//set Button Action
			cancel.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
				public void handle(Event e) {		
					createDiagramWindow.close();
				}
				} );
			finish.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
				public void handle(Event e) {
					String diagramName=diagramField.getText();
	                  //getTreeItem().getChildren().add(AddDiagram(diagramName));
	                  //diagramName=getName();
	                  MyTreeItem treeItem = new MyTreeItem();
	                  treeItem.setValue(diagramName);
	                  getTreeItem().getChildren().add(treeItem);
//	                 System.out.println(modelTreeView.getSelectionModel().getSelectedIndex()+"---");
	              		//check if there is tab selected
						Tab oldTab = tabPane.getSelectionModel().getSelectedItem();
						PrismModule prismModule=new PrismModule();
						prismModule.setName(treeItem.getParent().getValue().toString());
						//create new tab
						MuiGEditor muiGEditor=prismModule.createGaphicalEditor();
						Tab tab = muiGEditor.buildEditorTab(
								diagramName, prismModule, primaryStage, tabPane, 
							snapshotCanvas, menuBar, modelTreeView, treeItem);
						tab.textProperty().bindBidirectional(treeItem.valueProperty());
//						tab.setText(treeItem.getValue().toString());
						//select new created tab
						tabPane.getSelectionModel().select(tab);
						treeItem.setMyTab(tab);
						//manually send select event message to new tab
						if (oldTab != null) {
							Event.fireEvent(oldTab, new Event(Tab.SELECTION_CHANGED_EVENT));
						}
						Event.fireEvent(tab, new Event(Tab.SELECTION_CHANGED_EVENT));
						
		                ModelManager.getInstance().CreateDiagram(muiGEditor,treeItem.getParent().getValue().toString(),DiagramType.PrismDiagram, diagramName);	
		                createDiagramWindow.close();
				}
				} );
			//add label or field or button to hbox 
			hBox2.getChildren().addAll(diagramFieldLabel,diagramField);
			hBox3.getChildren().addAll(cancel,finish);
			hBox3.setAlignment(Pos.CENTER);
			//add label or hbox to vbox
			vBox.getChildren().addAll(label1,label2,hBox2,hBox3);
			Scene scene = new Scene(vBox);
			createDiagramWindow.setScene(scene);
            addMenuItem.setOnAction(new EventHandler() { 
                public void handle(Event t) {
                	createDiagramWindow.show();
                }
            });
            addMenuItem0.setOnAction(new EventHandler(){
				@Override
				public void handle(Event e) {
					 TreeItem c = (TreeItem)modelTreeView.getSelectionModel().getSelectedItem();
					//ModelManager.getInstance().GetModel(treeItem.getValue())
			         PrismModel model=(PrismModel) ModelManager.getInstance().GetModel(c.getValue().toString());
			         model.Check("./11.xml","dice.pctl");
			         Stage pluginWindow = new Stage(StageStyle.DECORATED);
						pluginWindow.initModality(Modality.WINDOW_MODAL);
						pluginWindow.setOpacity(0.87);
						pluginWindow.setTitle("Check Result");
						pluginWindow.setWidth(400);
						pluginWindow.setHeight(200);
						pluginWindow.centerOnScreen();
						VBox vBox = new VBox(10);
			         try
			         {
			        	 String path="./22";
			        	 String tempstr;
			             File file=new File(path);
			             if(!file.exists())
			                 throw new FileNotFoundException();            
//			             BufferedReader br=new BufferedReader(new FileReader(file));            
//			             while((tempstr=br.readLine())!=null)
//			                 sb.append(tempstr);    
			             //��һ�ֶ�ȡ��ʽ
			             FileInputStream fis=new FileInputStream(file);
			             BufferedReader br=new BufferedReader(new InputStreamReader(fis));
			             while((tempstr=br.readLine())!=null){
			            	 Label label = new Label(tempstr);
			            	 vBox.getChildren().add(label);
			             }
			                 
			         }
			         catch(IOException ex)
			         {
			             System.out.println(ex.getStackTrace());
			         }
			        
						Scene scene = new Scene(vBox);
						pluginWindow.setScene(scene);
						pluginWindow.show();
				}});
  
        //module delete            
         addMenuItem1.setOnAction(new EventHandler(){
				@Override
				public void handle(Event e) {
					 TreeItem c = (TreeItem)modelTreeView.getSelectionModel().getSelectedItem();
					 treeModelMap.remove(c.getValue().toString());
					 ModelManager.getInstance().DeleteModel(c.getValue().toString());
			         c.getParent().getChildren().remove(c);
			         ObservableList<TreeItem> list = c.getChildren();
			 		 for (TreeItem treeItem : list) {
			 			MyTreeItem myTreeItem = (MyTreeItem)treeItem;
			 			tabPane.getTabs().remove(myTreeItem.getMyTab());
			 		 }
				}});
            
         //open diagram         
            addLeafMenuItem0.setOnAction(new EventHandler(){
				@Override
				public void handle(Event e) {
					// TODO Auto-generated method stub
					 TreeItem<String> treeItem = (TreeItem<String>)modelTreeView.getSelectionModel().getSelectedItem();
			         MyTreeItem mytreeItem = (MyTreeItem)treeItem;
			         tabPane.getTabs().add(mytreeItem.getMyTab());
//					getTreeItem().getChildren().remove(modelTreeView.getSelectionModel().getSelectedIndex());
				}});
            
          //remove diagram  
            addLeafMenuItem1.setOnAction(new EventHandler(){
				@Override
				public void handle(Event e) {
					// TODO Auto-generated method stub
					 TreeItem treeItem = (TreeItem)modelTreeView.getSelectionModel().getSelectedItem();
					 treeItem.getParent().getChildren().remove(treeItem);
			         MyTreeItem mytreeItem = (MyTreeItem)treeItem;
			         tabPane.getTabs().remove(mytreeItem.getMyTab());
//					getTreeItem().getChildren().remove(modelTreeView.getSelectionModel().getSelectedIndex());
				}});
            
            //tree tab relation
            modelTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED,
					new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
//							int index = modelTreeView.getSelectionModel()
//									.getSelectedIndex();
//					     	index -= 2;
//							 System.out.println(index);
//							if (index >= 0 && index < tabPane.getTabs().size())
//								tabPane.getSelectionModel().select(index);
//							TreeItem treeItem  = (TreeItem)modelTreeView.getSelectionModel().getSelectedItem();
//							TreeItem myTreeItem = new MyTreeItem();
//							myTreeItem = (MyTreeItem)treeItem;
//							tabPane.getSelectionModel().select(myTreeItem.getMyTab());
							TreeItem<String> treeItem = new MyTreeItem();
							treeItem  = (TreeItem<String>)modelTreeView.getSelectionModel().getSelectedItem();
							if(treeItem instanceof MyTreeItem)
							{
								MyTreeItem myTreeItem = (MyTreeItem)treeItem;
								tabPane.getSelectionModel().select(myTreeItem.getMyTab());
							}
							
							
						}
					});
        }
            
        @Override
        public void startEdit() {
            super.startEdit();
 
            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
 
            setText((String) getItem());
            setGraphic(getTreeItem().getGraphic());
        }
 
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    if (!getTreeItem().isLeaf())
                    {
                        setContextMenu(addMenu);
                    }
                    else{
//                    	System.out.println(getTreeItem().getParent()+"-----------");
                    	if(getTreeItem().getParent().getValue() == null)
                    	      setContextMenu(addMenu);
                    
                        else 
                    	       setContextMenu(addLeafMenu);
                    
                    }
                }
            }
        }
        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(new EventHandler<KeyEvent>() { 
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });              
        } 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }	
}
