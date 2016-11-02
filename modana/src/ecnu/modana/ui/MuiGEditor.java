package ecnu.modana.ui;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ecnu.modana.model.ModelManager;
import ecnu.modana.model.PrismModel;
import ecnu.modana.model.PrismModel.*;
import ecnu.modana.ui.MyTextConvertor;

import org.apache.log4j.Logger;

import ecnu.modana.ui.ModanaUI;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Callback;
import  javafx.scene.control.Control;
import ecnu.modana.model.PrismModel;;
/**
 * Abstract graphical editor for drawing graphics, all kinds of diagram.This is a basic template of
 * graphical editor. To use it, you should add your own graphical elements
 * (extending MuiGArea or MuiGLink) to the lib and then implement the data
 * binding method for operating model...
 * 
 * @author cb
 */
public abstract class MuiGEditor {
	public int stateIndex=0;
	Logger logger = Logger.getRootLogger();

	/**
	 * Mouse action flag for resizing, relocating and relink
	 * 
	 * @param DEFAULT
	 *            No action
	 * @param NW_RESIZE
	 *            Resize northwest
	 * @param SW_RESIZE
	 *            Resize southwest
	 * @param W_RESIZE
	 *            Resize west
	 * @param NE_RESIZE
	 *            Resize northeast
	 * @param SE_RESIZE
	 *            Resize southeast
	 * @param E_RESIZE
	 *            Resize east
	 * @param N_RESIZE
	 *            Resize north
	 * @param S_RESIZE
	 *            Resize south
	 * @param RELOCATE
	 *            Relocate
	 * @param SOURCE_RELINK
	 *            Relink source point
	 * @param TARGET_RELINK
	 *            Relink target point
	 */
	public enum MOUSE_ACTION {
		DEFAULT, NW_RESIZE, SW_RESIZE, W_RESIZE, NE_RESIZE, SE_RESIZE, E_RESIZE, N_RESIZE, S_RESIZE, RELOCATE, SOURCE_RELINK, TARGET_RELINK
	}

	public StringProperty diagramName;
	// base tab pane
	private TabPane root = null;

	// this graphical tab, including text tab.
	private Tab graphicalTab = null;

	/**
	 * get related tab
	 * 
	 * @return tab
	 */
	public Tab getGraphicalTab() {
		return graphicalTab;
	}

	// scroll pane
	private ScrollPane scrollPane = null;

	// for rendering the snapshot of this editor
	private Canvas snapshotCanvas = null;

	// for add and remove user module menu
	private MenuBar menuBar = null;

	// tree view for selection control
	private TreeView<String> treeView = null;

	// the corresponding tree item
	private TreeItem<String> modelTree = null;

	// related MuiModule
	private MuiModule module = null;

	// root group containing background and all graphics
	private Group graphRoot = null;

	// the children property of root group for the convenience of adding
	// graphics
	private ObservableList<Node> graphContent =null;// FXCollections.observableArrayList();

	public ObservableList<Node> getGraphContent()
	{
		return graphContent;
	}
	public void setGraphContent(ObservableList<Node> graphContent)
	{
		this.graphContent = graphContent;
	}

	// graphics library on the left side for choosing element to be drawn
	private ToggleGroup libToggleGroup = null;

	// graphical object focused
	private Node tempNode = null;

	// current mouse action for mouse pressed event use
	private MOUSE_ACTION currentAction = MOUSE_ACTION.DEFAULT;

	// temporarily saving the effect of the node for undoing highlight
	private Effect tempEffect = null;

	// recording last x and y
	private double lastX, lastY;

	// denoting is dragging
	private boolean dragging = false;

	// denoting the index of the first area node (where we insert link node)
	private int indexOf1stArea = 1;

	// default canvas size
	private static final int graphWidth = 1500, graphHeight = 720;
	// opreateVbox include create delete update
	VBox opreateVbox;
	
	private ContextMenu rightClickArea;

	/**
	 * dropshadow offsetX constant
	 */
	public final static double dropshadowOffsetX = 3;

	/**
	 * dropshadow offsetY constant
	 */
	public final static double dropshadowOffsetY = 3;

	/**
	 * for expanding the selectable area with dropshadow effect: southeastern
	 * direction
	 */
	public double selectSEShadowBound = dropshadowOffsetX * 5;

	/**
	 * for expanding the selectable area with dropshadow effect: northwestern
	 * direction
	 */
	public double selectNWShadowBound = dropshadowOffsetX * 3;

	/**
	 * for expanding the selectable link points of this area
	 */
	public double selectPointBound = 12;

	/**
	 * offset x for showing property window
	 */
	public double propWindowOffsetX = 10;

	/**
	 * offset y for showing property window
	 */
	public double propWindowOffsetY = 10;

	// hold reference to main window
	private static Stage primaryStage = null;

	/**
	 * get main window stage
	 * 
	 * @return main window stage
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * build editor tab to the root TabPane
	 * 
	 * @param root
	 *            parent of the editor tab
	 * @param snapshotCanvas
	 *            for drawing snapshot of the graphical editor
	 */
	public Tab buildEditorTab(String tabName, MuiModule module, Stage pStage,
			TabPane root, Canvas snapshotCanvas, MenuBar menuBar,
			TreeView<String> treeView, TreeItem<String> modelTree) {

		this.root = root;
		this.snapshotCanvas = snapshotCanvas;
		this.menuBar = menuBar;
		this.treeView = treeView;
		this.modelTree = modelTree;
		this.module = module;
		primaryStage = pStage;

		// init graphical editor tab
		graphicalTab = new Tab();
		graphicalTab.setText(tabName);
		this.root.getTabs().add(graphicalTab);
		graphicalTab.setClosable(true);
		// add tool tip
		graphicalTab.setTooltip(new Tooltip(module.getName() + " "
				+ module.getVersion() + System.getProperty("line.separator")
				+ "- " + module.getDescription()));
		// add select and unselect event for tab
		addTabEvent();

		scrollPane = new ScrollPane();
		graphRoot = new Group();
		scrollPane.setContent(graphRoot);
	    rightClickArea = new ContextMenu();
	    scrollPane.setContextMenu(rightClickArea);
		graphContent = graphRoot.getChildren();

		SplitPane tabcenterPane = new SplitPane();
		BorderPane graphicalPane = new BorderPane();
		BorderPane inputPane = new BorderPane();
		inputPane.setMinHeight(150);
		inputPane.setMaxHeight(170);
		tabcenterPane.setOrientation(Orientation.VERTICAL);
		tabcenterPane.getItems().addAll(graphicalPane, inputPane);
		graphicalTab.setContent(tabcenterPane);
		graphicalPane.setCenter(scrollPane);

		TabPane perpane = new TabPane();
		Tab labletab = new Tab();
		Tab formulatab = new Tab();
		Tab rewardtab = new Tab();
		Tab moduletab = new Tab();
		Tab variabletab = new Tab();
		
		labletab.setText("Label");
		labletab.setClosable(false);
		initLable(labletab,(PrismModel) ModelManager.getInstance().GetModel(module.getName().toString()));

		formulatab.setText("Formula");
		formulatab.setClosable(false);
		initFormula(formulatab,(PrismModel) ModelManager.getInstance().GetModel(module.getName().toString()));

		rewardtab.setText("Reward");
		rewardtab.setClosable(false);
		initReward(rewardtab,(PrismModel) ModelManager.getInstance().GetModel(module.getName().toString()));
		
		moduletab.setText("Module");
		moduletab.setClosable(false);
		initModule(moduletab,(PrismModel) ModelManager.getInstance().GetModel(module.getName().toString()));
		
		variabletab.setText("Variable");
		variabletab.setClosable(false);
		initVariable(variabletab,(PrismModel) ModelManager.getInstance().GetModel(module.getName().toString()));
		
		perpane.getTabs().addAll(labletab, formulatab, rewardtab,moduletab,variabletab);
		inputPane.setCenter(perpane);

		// init graphics lib
		VBox graphLibPane = new VBox();
		graphicalPane.setLeft(graphLibPane);
		graphLibPane.setSpacing(5);
		graphLibPane.setPadding(new Insets(5));
		addLibToggles(graphLibPane);
		addLibToggleGroup(graphLibPane);

		// init mouse event
		addMouseEvent();

		// init editor background and snapshot
		drawBackground();
		drawSnapShot();
		return graphicalTab;
	}

	private void initReward(Tab rewardtab,PrismModel prismModel) {
		//reward table view
				BorderPane rewardPane = new BorderPane();
				opreateVbox = new VBox(18);
				opreateVbox.setPadding(new Insets(25));
				opreateVbox.setPrefWidth(200);
				//System.out.print(modelTree.getParent().getValue()+"00000000000000000000");
				//PrismModel prismModel = (PrismModel)ModelManager.getInstance().modelListMap.get(modelTree.getParent().getValue());
				// table view for showing all loaded reward
				TableView<Reward> rewardTableView = new TableView<Reward>();
				rewardTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
				rewardTableView.setEditable(true);
				// set data of table view
				rewardTableView.setItems(prismModel.rewardObervableList);
				// add columns
				TableColumn<Reward, String> rewardNameCol = new TableColumn<Reward, String>(
						"Reward Name");
				rewardNameCol.setEditable(true);
				rewardNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Reward, String>, ObservableValue<String>>() {

							@Override
							public ObservableValue<String> call(
									CellDataFeatures<Reward, String> f) {
								return f.getValue().getName();
							}
						});
				rewardNameCol.setCellFactory(new Callback<TableColumn<Reward, String>, TableCell<Reward, String>>() {
					@Override
					public TableCell<Reward, String> call(TableColumn<Reward, String> f) {
						TextFieldTableCell<Reward, String> cell = new TextFieldTableCell<Reward, String>(new MyTextConvertor());
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
				rewardNameCol.setMinWidth(150);
				rewardNameCol.setMaxWidth(320);
				rewardNameCol.setPrefWidth(200);
				TableColumn<Reward, String> rewardValueCol = new TableColumn<Reward, String>(
						"Reward Value");
				rewardValueCol.setEditable(true);
				rewardValueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Reward, String>, ObservableValue<String>>() {

							@Override
							public ObservableValue<String> call(
									CellDataFeatures<Reward, String> f) {
								return f.getValue().getValue();
							}
						});
				rewardValueCol.setCellFactory(new Callback<TableColumn<Reward, String>, TableCell<Reward, String>>() {
					@Override
					public TableCell<Reward, String> call(TableColumn<Reward, String> f) {
						TextFieldTableCell<Reward, String> cell = new TextFieldTableCell<Reward, String>(new MyTextConvertor());
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
				rewardValueCol.setMinWidth(250);
				rewardValueCol.setMaxWidth(752);
				rewardValueCol.setPrefWidth(449);
				rewardTableView.getColumns().add(rewardNameCol);
				rewardTableView.getColumns().add(rewardValueCol);
				
				rewardPane.setCenter(rewardTableView);
				rewardPane.setRight(opreateVbox);
				rewardtab.setContent(rewardPane);
				Button add = new Button("Add");
				add.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
					public void handle(Event e) {		
						prismModel.rewardObervableList.add(new Reward("reward name","reward value"));
					}
					} );
				Button delete = new Button("Delete");
				delete.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
					public void handle(Event e) {		
						prismModel.rewardObervableList.removeAll(rewardTableView.getSelectionModel().getSelectedItems());
					}
					} );
				add.setMinSize(120, 25);
				delete.setMinSize(120, 25);
				opreateVbox.getChildren().addAll(add, delete);
				
				
	}

	private void initFormula(Tab formulatab,PrismModel prismModel) {
		//formula table view
		BorderPane formulaPane = new BorderPane();
		opreateVbox = new VBox(18);
		opreateVbox.setPadding(new Insets(25));
		opreateVbox.setPrefWidth(200);
		
		
		//PrismModel prismModel =(PrismModel) ModelManager.getInstance().GetModel(modelTree.getParent().getValue());
		// table view for showing all loaded formula
		TableView<Formula> formulaTableView = new TableView<Formula>();
		formulaTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		formulaTableView.setEditable(true);
		// set data of table view
		formulaTableView.setItems(prismModel.formulaObervableList);
		// add columns
		TableColumn<Formula, String> formulaNameCol = new TableColumn<Formula, String>(
				"Formula Name");
		formulaNameCol.setEditable(true);
		formulaNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Formula, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Formula, String> f) {
						return f.getValue().getName();
					}
				});
		formulaNameCol.setCellFactory(new Callback<TableColumn<Formula, String>, TableCell<Formula, String>>() {
			@Override
			public TableCell<Formula, String> call(TableColumn<Formula, String> f) {
				TextFieldTableCell<Formula, String> cell = new TextFieldTableCell<Formula, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		formulaNameCol.setMinWidth(150);
		formulaNameCol.setMaxWidth(320);
		formulaNameCol.setPrefWidth(200);
		TableColumn<Formula, String> formulaValueCol = new TableColumn<Formula, String>(
				"Formula Value");
		formulaValueCol.setEditable(true);
		formulaValueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Formula, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Formula, String> f) {
						return f.getValue().getValue();
					}
				});
		formulaValueCol.setCellFactory(new Callback<TableColumn<Formula, String>, TableCell<Formula, String>>() {
			@Override
			public TableCell<Formula, String> call(TableColumn<Formula, String> f) {
				TextFieldTableCell<Formula, String> cell = new TextFieldTableCell<Formula, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		formulaValueCol.setMinWidth(250);
		formulaValueCol.setMaxWidth(752);
		formulaValueCol.setPrefWidth(449);
		formulaTableView.getColumns().add(formulaNameCol);
		formulaTableView.getColumns().add(formulaValueCol);
		
		formulaPane.setCenter(formulaTableView);
		formulaPane.setRight(opreateVbox);
		formulatab.setContent(formulaPane);
		Button add = new Button("Add");
		add.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				prismModel.formulaObervableList.add(new Formula("formula name","formula value"));
			}
			} );
		Button delete = new Button("Delete");
		delete.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				prismModel.formulaObervableList.removeAll(formulaTableView.getSelectionModel().getSelectedItems());
			}
			} );
		
		add.setMinSize(120, 25);
		delete.setMinSize(120, 25);
		
		opreateVbox.getChildren().addAll(add, delete);
		
	}

	private void initLable(Tab labletab,PrismModel prismModel) {
		// TODO Auto-generated method stub
		//lable table view
		BorderPane lablePane = new BorderPane();
		opreateVbox = new VBox(18);
		opreateVbox.setPadding(new Insets(25));
		opreateVbox.setPrefWidth(200);
//		PrismModel prismModel=null;
//		try
//		{
//			if(null==modelTree.getParent())
//			{
//				System.err.println("error "+modelTree);
//			}
//			String tString=modelTree.getParent().getValue();
//			System.err.println("en:"+tString);
//			prismModel=(PrismModel) ModelManager.getInstance().GetModel(modelTree.getParent().getValue());
//		} catch (Exception e)
//		{
//			System.err.println("ah "+e.getMessage()+modelTree+" "+prismModel);
//		}
		
		//PrismModel prismModel=(PrismModel) ModelManager.getInstance().GetModel(modelTree.getParent().getValue());
		// table view for showing all loaded lable
		TableView<Lable> lableTableView = new TableView<Lable>();
		lableTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		lableTableView.setEditable(true);
		// set data of table view
		lableTableView.setItems(prismModel.lableObervableList);
		// add columns
		TableColumn<Lable, String> lableNameCol = new TableColumn<Lable, String>(
				"Label Name");
		lableNameCol.setEditable(true);
		lableNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Lable, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Lable, String> l) {
						return l.getValue().getName();
					}
				});
		lableNameCol.setCellFactory(new Callback<TableColumn<Lable, String>, TableCell<Lable, String>>() {
			@Override
			public TableCell<Lable, String> call(TableColumn<Lable, String> l) {
				TextFieldTableCell<Lable, String> cell = new TextFieldTableCell<Lable, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		lableNameCol.setMinWidth(150);
		lableNameCol.setMaxWidth(320);
		lableNameCol.setPrefWidth(200);
		TableColumn<Lable, String> lableValueCol = new TableColumn<Lable, String>(
				"Label Value");
		lableValueCol.setEditable(true);
		lableValueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Lable, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Lable, String> l) {
						return l.getValue().getValue();
					}
				});
		lableValueCol.setCellFactory(new Callback<TableColumn<Lable, String>, TableCell<Lable, String>>() {
			@Override
			public TableCell<Lable, String> call(TableColumn<Lable, String> l) {
				TextFieldTableCell<Lable, String> cell = new TextFieldTableCell<Lable, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		lableValueCol.setMinWidth(250);
		lableValueCol.setMaxWidth(752);
		lableValueCol.setPrefWidth(449);
		lableTableView.getColumns().add(lableNameCol);
		lableTableView.getColumns().add(lableValueCol);
		
		lablePane.setCenter(lableTableView);
		lablePane.setRight(opreateVbox);
		labletab.setContent(lablePane);
		Button add = new Button("Add");
		add.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				//ModanaParameter.lableList.add(new Lable("label name","label value"));
				prismModel.lableObervableList.add(new Lable("label name","label value"));
			}
			} );
		Button delete = new Button("Delete");
		delete.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				//ModanaParameter.lableList.removeAll(lableTableView.getSelectionModel().getSelectedItems());
				prismModel.lableObervableList.removeAll(lableTableView.getSelectionModel().getSelectedItems());
			}
			} );
		
		add.setMinSize(120, 25);
		delete.setMinSize(120, 25);
		
		opreateVbox.getChildren().addAll(add, delete);
	}
	private void initModule(Tab moduletab, PrismModel prismModel) {
		// TODO Auto-generated method stub
		BorderPane modulePane = new BorderPane();
		opreateVbox = new VBox(18);
		opreateVbox.setPadding(new Insets(25));
		opreateVbox.setPrefWidth(200);
		//System.out.print(modelTree.getParent().getValue()+"00000000000000000000");
		//PrismModel prismModel = (PrismModel)ModelManager.getInstance().modelListMap.get(modelTree.getParent().getValue());
		// table view for showing all loaded reward
		TableView<Module> moduleTableView = new TableView<Module>();
		moduleTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		moduleTableView.setEditable(true);
		// set data of table view
		moduleTableView.setItems(prismModel.moduleObervableList);
		// add columns
		TableColumn<Module, String> moduleNameCol = new TableColumn<Module, String>(
				"Module Name");
		moduleNameCol.setEditable(true);
		moduleNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Module, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Module, String> f) {
						return f.getValue().getName();
					}
				});
		moduleNameCol.setCellFactory(new Callback<TableColumn<Module, String>, TableCell<Module, String>>() {
			@Override
			public TableCell<Module, String> call(TableColumn<Module, String> f) {
				TextFieldTableCell<Module, String> cell = new TextFieldTableCell<Module, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		moduleNameCol.setMinWidth(150);
		moduleNameCol.setMaxWidth(320);
		moduleNameCol.setPrefWidth(200);
		TableColumn<Module, String> moduleValueCol = new TableColumn<Module, String>(
				"Module Value");
		moduleValueCol.setEditable(true);
		moduleValueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Module, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Module, String> f) {
						return f.getValue().getValue();
					}
				});
		moduleValueCol.setCellFactory(new Callback<TableColumn<Module, String>, TableCell<Module, String>>() {
			@Override
			public TableCell<Module, String> call(TableColumn<Module, String> f) {
				TextFieldTableCell<Module, String> cell = new TextFieldTableCell<Module, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		moduleValueCol.setMinWidth(250);
		moduleValueCol.setMaxWidth(752);
		moduleValueCol.setPrefWidth(449);
		moduleTableView.getColumns().add(moduleNameCol);
		moduleTableView.getColumns().add(moduleValueCol);
		
		modulePane.setCenter(moduleTableView);
		modulePane.setRight(opreateVbox);
		moduletab.setContent(modulePane);
		Button add = new Button("Add");
		add.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				prismModel.moduleObervableList.add(new Module("module name","module value"));
			}
			} );
		Button delete = new Button("Delete");
		delete.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				prismModel.moduleObervableList.removeAll(moduleTableView.getSelectionModel().getSelectedItems());
			}
			} );
		add.setMinSize(120, 25);
		delete.setMinSize(120, 25);
		opreateVbox.getChildren().addAll(add, delete);
		
	}

	private void initVariable(Tab variabletab, PrismModel getModel) {
		// TODO Auto-generated method stub
		BorderPane variablePane = new BorderPane();
		opreateVbox = new VBox(18);
		opreateVbox.setPadding(new Insets(25));
		opreateVbox.setPrefWidth(200);
		//System.out.print(modelTree.getParent().getValue()+"00000000000000000000");
		//PrismModel prismModel = (PrismModel)ModelManager.getInstance().modelListMap.get(modelTree.getParent().getValue());
		// table view for showing all loaded reward
		TableView<Variable> variableTableView = new TableView<Variable>();
		variableTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		variableTableView.setEditable(true);
		// set data of table view
		variableTableView.setItems(getModel.variableObervableList);
		// add columns
		TableColumn<Variable, String> variableNameCol = new TableColumn<Variable, String>(
				"Variable Name");
		variableNameCol.setEditable(true);
		variableNameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Variable, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Variable, String> f) {
						return f.getValue().getName();
					}
				});
		variableNameCol.setCellFactory(new Callback<TableColumn<Variable, String>, TableCell<Variable, String>>() {
			@Override
			public TableCell<Variable, String> call(TableColumn<Variable, String> f) {
				TextFieldTableCell<Variable, String> cell = new TextFieldTableCell<Variable, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		variableNameCol.setMinWidth(150);
		variableNameCol.setMaxWidth(320);
		variableNameCol.setPrefWidth(200);
		TableColumn<Variable, String> variableValueCol = new TableColumn<Variable, String>(
				"Variable Value");
		variableValueCol.setEditable(true);
		variableValueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Variable, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Variable, String> f) {
						return f.getValue().getValue();
					}
				});
		variableValueCol.setCellFactory(new Callback<TableColumn<Variable, String>, TableCell<Variable, String>>() {
			@Override
			public TableCell<Variable, String> call(TableColumn<Variable, String> f) {
				TextFieldTableCell<Variable, String> cell = new TextFieldTableCell<Variable, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		variableValueCol.setMinWidth(250);
		variableValueCol.setMaxWidth(752);
		variableValueCol.setPrefWidth(449);
		variableTableView.getColumns().add(variableNameCol);
		variableTableView.getColumns().add(variableValueCol);
		
		variablePane.setCenter(variableTableView);
		variablePane.setRight(opreateVbox);
		variabletab.setContent(variablePane);
		Button add = new Button("Add");
		add.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				getModel.variableObervableList.add(new Variable("variable name","variable value"));
			}
			} );
		Button delete = new Button("Delete");
		delete.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			public void handle(Event e) {		
				getModel.variableObervableList.removeAll(variableTableView.getSelectionModel().getSelectedItems());
			}
			} );
		add.setMinSize(120, 25);
		delete.setMinSize(120, 25);
		opreateVbox.getChildren().addAll(add, delete);
		
	}

	
	
	// add default event for tab
	private void addTabEvent() {
		// tab event to change menu and tree view
		graphicalTab.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(Event e) {
				if (graphicalTab.isSelected()) {
					drawSnapShot();
					// for menu
					if (module.getModuleMenu() != null) {
						menuBar.getMenus().add(menuBar.getMenus().size() - 1,
								module.getModuleMenu());
					}
					// for tree selecting
					treeView.getSelectionModel().select(modelTree);
				} else {
					// for menu
					if (module.getModuleMenu() != null) {
						menuBar.getMenus().remove(module.getModuleMenu());
					}
				}
			}
		});
		graphicalTab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event e) {
				// delete tree item
				treeView.getRoot().getChildren().remove(modelTree);
			}
		});
//		treeView.setOnMouseClicked(new EventHandler<MouseEvent>(){
//
//			@Override
//			public void handle(MouseEvent arg0) {
//				// TODO Auto-generated method stub
//				
//				root.getSelectionModel().select(graphicalTab);
//				
//			}});
	}

	// create toggle group and automatically add all children of graphLibPane to
	// the group
	private void addLibToggleGroup(VBox graphLibPane) {
		libToggleGroup = new ToggleGroup();
		List<Node> list = graphLibPane.getChildren();
		for (Node node : list) {
			if (node instanceof ToggleButton) {
				((ToggleButton) node).setToggleGroup(libToggleGroup);
			}
		}
	}

	// adding default mouse event for root
	private void addMouseEvent() {
		graphRoot.addEventHandler(MouseEvent.MOUSE_RELEASED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// relink action for pressed
						if (currentAction == MOUSE_ACTION.SOURCE_RELINK) {
							if (e.getButton().equals(MouseButton.SECONDARY) // right
																			// click
									&& tempNode != null) {
								MuiGLink glink = (MuiGLink) tempNode
										.getUserData();
								// reset link source point
								finalUpdateLink(glink, currentAction);
								currentAction = MOUSE_ACTION.DEFAULT;
								unselectNode();
							}
							return;
						} else if (currentAction == MOUSE_ACTION.TARGET_RELINK) {
							// cancel process of creating new link
							if (e.getButton().equals(MouseButton.SECONDARY) // right
																			// click
									&& tempNode != null) {
								MuiGLink glink = (MuiGLink) tempNode
										.getUserData();
								if (glink.getTargetArea() == null) { // if in
																		// creating
																		// process
									// delete temporary link node
									indexOf1stArea--;
									graphContent.remove(indexOf1stArea);
								} else { // reset link target point
									finalUpdateLink(glink, currentAction);
								}
								currentAction = MOUSE_ACTION.DEFAULT;
								unselectNode();
							}
							return;
						}
						// check if left click
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						// unselect node firstly for creating new node
						unselectNode();
						if (e.getPickResult().getIntersectedNode() == graphContent
								.get(0)
								&& currentAction == MOUSE_ACTION.DEFAULT
								&& libToggleGroup.getSelectedToggle() != null) {
							Object obj = libToggleGroup.getSelectedToggle()
									.getUserData();
							if (obj != null && obj instanceof Class) {
								Class<?> cls = (Class<?>) obj;
								try {
									// ready to create area node
									if (MuiGArea.class.isAssignableFrom(cls)) {
										MuiGArea gArea = (MuiGArea) cls
												.newInstance();
										// create javafx Node
										Node node = gArea.createNode(e
												.getPickResult()
												.getIntersectedPoint().getX(),
												e.getPickResult()
														.getIntersectedPoint()
														.getY(),stateIndex++);
										// save area object to the userData of
										// the node
										node.setUserData(gArea);
										// add the node to root group
										graphContent.add(node);
										// add mouse event for the node
										addMouseEvent2Area(node);
										// select new node
										selectNode(node);
									}
								} catch (InstantiationException
										| IllegalAccessException ex) {
									logger.error(
											"Creating Area Graphics Error! The userData of the toggle button may be wrong.",
											ex);
								}
							}
						}
						currentAction = MOUSE_ACTION.DEFAULT;
						dragging = false;
						adjustBackgroundSize();
						drawSnapShot();
						e.consume();
					}
				});
		graphRoot.addEventHandler(MouseEvent.MOUSE_MOVED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						if (currentAction == MOUSE_ACTION.TARGET_RELINK
								|| currentAction == MOUSE_ACTION.SOURCE_RELINK) {
							if (tempNode != null) {
								((MuiGLink) tempNode.getUserData())
										.updateNodes(e.getPickResult()
												.getIntersectedPoint().getX(),
												e.getPickResult()
														.getIntersectedPoint()
														.getY(), currentAction);
							}
						}
					}
				});
	}

	// adding default mouse event for the area node
	protected void addMouseEvent2Area(Node node) {
		// change cursor when moving over different parts of the node
		node.addEventHandler(MouseEvent.MOUSE_MOVED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						Point3D p3d = e.getPickResult().getIntersectedPoint();
						double x = p3d.getX(), y = p3d.getY();
						double minX = node.getBoundsInLocal().getMinX();
						double maxX = node.getBoundsInLocal().getMaxX();
						double minY = node.getBoundsInLocal().getMinY();
						double maxY = node.getBoundsInLocal().getMaxY();

						// check area of link points
						if (currentAction == MOUSE_ACTION.SOURCE_RELINK
								|| currentAction == MOUSE_ACTION.TARGET_RELINK) {
							ArrayList<Point2D> ptList = ((MuiGArea) node
									.getUserData()).getAllLinkPoints();
							for (Point2D point2d : ptList) {
								if (point2d.distance(x, y) < selectPointBound) {
									node.setCursor(Cursor.CROSSHAIR);
									return;
								}
							}
						} else if (libToggleGroup.getSelectedToggle() != null) {
							Object obj = libToggleGroup.getSelectedToggle()
									.getUserData();
							if (obj != null
									&& obj instanceof Class
									&& MuiGLink.class
											.isAssignableFrom((Class<?>) obj)) {
								ArrayList<Point2D> ptList = ((MuiGArea) node
										.getUserData()).getAllLinkPoints();
								for (Point2D point2d : ptList) {
									if (point2d.distance(x, y) < selectPointBound) {
										node.setCursor(Cursor.CROSSHAIR);
										return;
									}
								}
							}
						}

						// for expanding the selectable area with/without
						// dropshadow effect
						double selectSEBound; // southeastern
						double selectNWBound; // northwestern
						if (node.getEffect() == null) {
							selectSEBound = dropshadowOffsetX * 1.2;
							selectNWBound = dropshadowOffsetX * 1.2;
						} else {
							selectSEBound = selectSEShadowBound;
							selectNWBound = selectNWShadowBound;
						}
						// check area for resizing or relocating
						if (x >= minX && x <= (minX + selectNWBound)) {
							if (y >= minY && y <= (minY + selectNWBound)) {
								node.setCursor(Cursor.NW_RESIZE);
							} else if (y <= maxY && y >= (maxY - selectSEBound)) {
								node.setCursor(Cursor.SW_RESIZE);
							} else {
								node.setCursor(Cursor.W_RESIZE);
							}
						} else if (x <= maxX && x >= (maxX - selectSEBound)) {
							if (y >= minY && y <= (minY + selectNWBound)) {
								node.setCursor(Cursor.NE_RESIZE);
							} else if (y <= maxY && y >= (maxY - selectSEBound)) {
								node.setCursor(Cursor.SE_RESIZE);
							} else {
								node.setCursor(Cursor.E_RESIZE);
							}
						} else if (y >= minY && y <= (minY + selectNWBound)) {
							node.setCursor(Cursor.N_RESIZE);
						} else if (y <= maxY && y >= (maxY - selectSEBound)) {
							node.setCursor(Cursor.S_RESIZE);
						} else {
							node.setCursor(Cursor.MOVE);
						}
					}
				});
		// MOUSE_PRESSED for determining the type of mouse drag action
		node.addEventHandler(MouseEvent.MOUSE_PRESSED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// check if left click
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						// relink action for pressed
						if (currentAction == MOUSE_ACTION.SOURCE_RELINK
								|| currentAction == MOUSE_ACTION.TARGET_RELINK) {
							return;
						}
						// reselect node
						unselectNode();
						selectNode(node);
						// determine suitable mouse action
						Point2D pt = node.localToParent(e.getX(), e.getY());
						lastX = pt.getX();
						lastY = pt.getY();
						Point3D p3d = e.getPickResult().getIntersectedPoint();
						double x = p3d.getX(), y = p3d.getY();
						double minX = node.getBoundsInLocal().getMinX();
						double maxX = node.getBoundsInLocal().getMaxX();
						double minY = node.getBoundsInLocal().getMinY();
						double maxY = node.getBoundsInLocal().getMaxY();
						if (x >= minX && x <= (minX + selectNWShadowBound)) {
							if (y >= minY && y <= (minY + selectNWShadowBound)) {
								currentAction = MOUSE_ACTION.NW_RESIZE;
							} else if (y <= maxY
									&& y >= (maxY - selectSEShadowBound)) {
								currentAction = MOUSE_ACTION.SW_RESIZE;
							} else {
								currentAction = MOUSE_ACTION.W_RESIZE;
							}
						} else if (x <= maxX
								&& x >= (maxX - selectSEShadowBound)) {
							if (y >= minY && y <= (minY + selectNWShadowBound)) {
								currentAction = MOUSE_ACTION.NE_RESIZE;
							} else if (y <= maxY
									&& y >= (maxY - selectSEShadowBound)) {
								currentAction = MOUSE_ACTION.SE_RESIZE;
							} else {
								currentAction = MOUSE_ACTION.E_RESIZE;
							}
						} else if (y >= minY
								&& y <= (minY + selectNWShadowBound)) {
							currentAction = MOUSE_ACTION.N_RESIZE;
						} else if (y <= maxY
								&& y >= (maxY - selectSEShadowBound)) {
							currentAction = MOUSE_ACTION.S_RESIZE;
						} else {
							currentAction = MOUSE_ACTION.RELOCATE;
						}
						dragging = true;
					}
				});
		// MOUSE_RELEASED for post-operation of dragging or selecting
		node.addEventHandler(MouseEvent.MOUSE_RELEASED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// check if left click
//						if (currentAction == MOUSE_ACTION.TARGET_RELINK) {
						if(e.getButton().equals(MouseButton.SECONDARY)){
									System.out.println("==========");
//									if(rightClickArea.getItems().size()!=0){
//										rightClickArea.getItems().clear();
//									}
									 MenuItem deleteItem = new MenuItem("Delete");
									 MenuItem renameItem = new MenuItem("Rename");
									 rightClickArea.getItems().removeAll();
									 rightClickArea.getItems().addAll(deleteItem,renameItem);
									 deleteItem.setOnAction(new EventHandler(){
											@Override
											public void handle(Event e) {
												// TODO Auto-generated method stub
												 MuiGArea area = (MuiGArea)node.getUserData();
												 LinkedList<MuiGLink> lList =  area.getAllLinks();
												 System.out.println(lList.size());
												 for(MuiGLink link : lList){
													 graphRoot.getChildren().remove(link.getLinkNode());
												 };
												 for(MuiGLink link : lList){
												     link.getTargetArea().removeLinkOnArea(link);
												     link.getSourceArea().removeLinkOnArea(link);	
												 }											
//												 area.removeAllLink();
												 graphRoot.getChildren().remove(node);
												 rightClickArea.getItems().clear();
											}});

								    
									
							 }
//						 }
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						Point3D p3d = e.getPickResult().getIntersectedPoint();
						double x = p3d.getX();
						double y = p3d.getY();
						boolean newLink = false; // denoting the process of
													// creating a new link
						MuiGArea gArea = (MuiGArea) node.getUserData();
						if (currentAction == MOUSE_ACTION.TARGET_RELINK
								&& tempNode != null
								&& MuiGLink.class.isAssignableFrom(tempNode
										.getUserData().getClass())) {
							// create or target relink this link node
							ArrayList<Point2D> ptList = gArea
									.getAllLinkPoints();
							int i;
							for (i = 0; i < ptList.size(); i++) {
								if (ptList.get(i).distance(x, y) < selectPointBound) {
									MuiGLink lnk = (MuiGLink) tempNode
											.getUserData();
									// remove this link from old area
									if (lnk.getTargetArea() == null) { // for
																		// new
																		// created
																		// link
										lnk.getSourceArea().addLinkOnArea(lnk);
									} else { // for old link
										lnk.getTargetArea().removeLinkOnArea(
												lnk);
									}
									// record target
									lnk.setTarget(i, gArea);
									// final update of link location
									finalUpdateLink(lnk, currentAction);
									// add this link to new area
									lnk.getTargetArea().addLinkOnArea(lnk);
									break;
								}
							}
							if (i >= ptList.size()) {// not click on any link
														// point
								return;
							}
						} else if (currentAction == MOUSE_ACTION.SOURCE_RELINK
								&& tempNode != null
								&& MuiGLink.class.isAssignableFrom(tempNode
										.getUserData().getClass())) {
							// source relink this link node
							ArrayList<Point2D> ptList = gArea
									.getAllLinkPoints();
							int i;
							for (i = 0; i < ptList.size(); i++) {
								if (ptList.get(i).distance(x, y) < selectPointBound) {
									MuiGLink lnk = (MuiGLink) tempNode
											.getUserData();
									// remove this link from old area
									if (lnk.getSourceArea() != null) {
										lnk.getSourceArea().removeLinkOnArea(
												lnk);
									}
									// record source
									lnk.setSource(i, gArea);
									// final update of link location
									finalUpdateLink(lnk, currentAction);
									// add this link to new area
									lnk.getSourceArea().addLinkOnArea(lnk);
									break;
								}
							}
							if (i >= ptList.size()) {// not click on any link
														// point
								return;
							}
						} else {
							// check if we should draw a temporary new link
							if (libToggleGroup.getSelectedToggle() != null) {
								Object obj = libToggleGroup.getSelectedToggle()
										.getUserData();
								if (obj != null
										&& obj instanceof Class
										&& MuiGLink.class
												.isAssignableFrom((Class<?>) obj)) {
									ArrayList<Point2D> ptList = gArea
											.getAllLinkPoints();
									for (int i = 0; i < ptList.size(); i++) {
										if (ptList.get(i).distance(x, y) < selectPointBound) {
											try {
												unselectNode();
												MuiGLink gLink = (MuiGLink) ((Class<?>) obj)
														.newInstance();
												Node lnkNode = gLink
														.createNodes(
																ptList.get(i)
																		.getX(),
																ptList.get(i)
																		.getY(),
																x, y);
												// record source
												gLink.setSource(i, gArea);
												// save link object to the
												// userData of the node as well
												// as seletable node
												lnkNode.setUserData(gLink);
												gLink.getMouseSelectableArea()
														.setUserData(gLink);
												// add the node to graphContent
												// (before all area nodes)
										graphContent.add(indexOf1stArea,lnkNode);
												//graphContent.add(lnkNode);
												// Important: change index for
												// inserting link node !!!
												// Important: no need to add
												// link to area for it's
												// temporary !!!
												indexOf1stArea++;
												// add mouse event for the
												// selectable node of this link
												MouseEvent2Link(gLink);
												currentAction = MOUSE_ACTION.TARGET_RELINK;
												// select temporary link node
												selectNode(lnkNode);
												// mark of creating new link
												newLink = true;
											} catch (InstantiationException
													| IllegalAccessException ex) {
												logger.error(
														"Creating Link Graphics Error! The userData of the toggle button may be wrong.",
														ex);
											}
											break;
										}
									}
								}
							}

						}
						// reset mouse action first
						if (!newLink) {
							currentAction = MOUSE_ACTION.DEFAULT;
						}
						dragging = false;
						adjustBackgroundSize();
						drawSnapShot();
						e.consume(); // not allow root group to receive the
										// event
						if (e.getClickCount() > 1) {
							// double click to show property window
							Stage propWindow = gArea.getPropertyWindow();
							if (!propWindow.isShowing()) {
								propWindow.setX(e.getScreenX()
										+ propWindowOffsetX);
								propWindow.setY(e.getScreenY()
										+ propWindowOffsetY);
								if (propWindow.getOwner() == null) {
									propWindow.initOwner(primaryStage);
								}
								propWindow.show();
							}
						}

					}
				});
		// DRAG always happens after MOUSE_PRESSED
		node.addEventHandler(MouseEvent.MOUSE_DRAGGED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// check if left click
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						if (!dragging) {
							return;
						}
						Point2D pt = node.localToParent(e.getX(), e.getY());
						MuiGArea gArea = (MuiGArea) node.getUserData();
						// update node location and size
						gArea.updateNode(pt.getX() - lastX, pt.getY() - lastY,
								currentAction);
						// record last x and y
						lastX = pt.getX();
						lastY = pt.getY();
						// update the location of all links on this area
						List<MuiGLink> list = gArea.getAllLinks();
						for (MuiGLink gLink : list) {
							if (gLink.getSourceArea() == gLink.getTargetArea()) {
								// for self link node
								gLink.updateSelfLinkNodes(gLink
										.getSourcePoint().getX(), gLink
										.getSourcePoint().getY(), gLink
										.getTargetPoint().getX(), gLink
										.getTargetPoint().getY());
							} else if (gArea == gLink.getSourceArea()) {
								gLink.updateNodes(
										gLink.getSourcePoint().getX(), gLink
												.getSourcePoint().getY(),
										MOUSE_ACTION.SOURCE_RELINK);
							} else if (gArea == gLink.getTargetArea()) {
								gLink.updateNodes(
										gLink.getTargetPoint().getX(), gLink
												.getTargetPoint().getY(),
										MOUSE_ACTION.TARGET_RELINK);
							} else {
								logger.error("GArea contains invalid reference to GLink!");
							}
						}
					}
				});

	}

	// finally update link when source and target areas and points are changed
	protected void finalUpdateLink(MuiGLink lnk, MOUSE_ACTION action) {
		ArrayList<Point2D> ptList = null;
		int i;
		if (action == MOUSE_ACTION.SOURCE_RELINK) {
			ptList = lnk.getSourceArea().getAllLinkPoints();
			i = lnk.getSourcePtIndex();
		} else if (action == MOUSE_ACTION.TARGET_RELINK) {
			ptList = lnk.getTargetArea().getAllLinkPoints();
			i = lnk.getTargetPtIndex();
		} else {
			return;
		}
		if (lnk.getTargetArea() == lnk.getSourceArea()) { // if the same area
			if (action == MOUSE_ACTION.SOURCE_RELINK) {
				i = lnk.getTargetPtIndex() - 1;
				if (i == -1) {
					i = ptList.size() - 1;
				}
				// set the source or target link point index again
				lnk.setSource(i, lnk.getSourceArea());
			} else if (action == MOUSE_ACTION.TARGET_RELINK) {
				i = lnk.getSourcePtIndex() + 1;
				if (i == ptList.size()) {
					i = 0;
				}
				// set the source or target link point index again
				lnk.setTarget(i, lnk.getTargetArea());
			}
			lnk.updateSelfLinkNodes(lnk.getSourcePoint().getX(), lnk
					.getSourcePoint().getY(), lnk.getTargetPoint().getX(), lnk
					.getTargetPoint().getY());
		} else { // if different areas
			lnk.updateNodes(ptList.get(i).getX(), ptList.get(i).getY(),
					currentAction);
		}

	}

	// add default mouse event for link node
	protected void MouseEvent2Link(MuiGLink gLink) {
		Node node = gLink.getMouseSelectableArea();
		// MOUSE_PRESSED for set currentAction as RELINK
		node.addEventHandler(MouseEvent.MOUSE_PRESSED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// check if left click
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						if (currentAction != MOUSE_ACTION.DEFAULT) {
							return;
						}
						// reselect node
						unselectNode();
						selectNode(node);
						dragging = true;
					}
				});
		// DRAG always happens after MOUSE_PRESSED
		node.addEventHandler(MouseEvent.MOUSE_DRAGGED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						// check if left click
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						if (!dragging) {
							return;
						}
						if (currentAction == MOUSE_ACTION.DEFAULT) {
							double distance1 = gLink.getSourcePoint().distance(
									node.localToParent(e.getX(), e.getY()));
							double distance2 = gLink.getTargetPoint().distance(
									node.localToParent(e.getX(), e.getY()));
							if (distance1 < distance2) {
								currentAction = MOUSE_ACTION.SOURCE_RELINK;
							} else {
								currentAction = MOUSE_ACTION.TARGET_RELINK;
							}
						}
						Point2D pt = node.localToParent(e.getX(), e.getY());
						// update node location
						gLink.updateNodes(pt.getX(), pt.getY(), currentAction);
					}
				});
		// MOUSE_RELEASED for post-operation
		node.addEventHandler(MouseEvent.MOUSE_RELEASED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						if(e.getButton().equals(MouseButton.SECONDARY)){
							System.out.println("++++++");
						     MenuItem deleteItem = new MenuItem("Delete");
							 MenuItem renameItem = new MenuItem("Rename");
							 rightClickArea.getItems().addAll(deleteItem,renameItem);
							 deleteItem.setOnAction(new EventHandler(){
									@Override
									public void handle(Event e) {
										// TODO Auto-generated method stub
										 graphRoot.getChildren().remove(gLink.getLinkNode());
										 gLink.getSourceArea().removeLinkOnArea(gLink);
										 gLink.getTargetArea().removeLinkOnArea(gLink);
										 rightClickArea.getItems().clear();
									}});

//							System.out.println(graphRoot.getChildren().size());
							
					 }
						// check if left click
						if (!e.getButton().equals(MouseButton.PRIMARY)) {
							return;
						}
						if (!dragging) {
							return;
						}
						dragging = false;
						Point2D pt = node.localToParent(e.getX(), e.getY());
						// update node location
						gLink.updateNodes(pt.getX(), pt.getY(), currentAction);
						e.consume();
						if (e.getClickCount() > 1) {
							// double click to show property window
							Stage propWindow = gLink.getPropertyWindow();
							if (!propWindow.isShowing()) {
								propWindow.setX(e.getScreenX()
										+ propWindowOffsetX);
								propWindow.setY(e.getScreenY()
										+ propWindowOffsetY);
								if (propWindow.getOwner() == null) {
									propWindow.initOwner(primaryStage);
								}
								propWindow.show();
							}
						}
					}
				});

	}

	// select node and set highlight effect
	private void selectNode(Node node) {
		tempNode = node;
		tempEffect = node.getEffect();
		// do highlight effect
		DropShadow ds = new DropShadow();
		ds.setOffsetY(dropshadowOffsetY);
		ds.setOffsetX(dropshadowOffsetX);
		ds.setColor(Color.BLACK);
		node.setEffect(ds);
	}

	// unselect node and clear highlight effect
	private void unselectNode() {
		if (tempNode != null) {
			// undo highlight effect
			tempNode.setEffect(tempEffect);
			tempNode = null;
		}
	}

	// drawing background
	private void drawBackground() {
		Rectangle rect = new Rectangle(0, 0, graphWidth, graphHeight);
		rect.setFill(getBackGroundColor());
		graphContent.add(rect);
	}

	// drawing snapshot
	protected void drawSnapShot() {
		SnapshotParameters sp = new SnapshotParameters();
		sp.setFill(Color.WHITE);
		WritableImage img = graphRoot.snapshot(sp, null);
		snapshotCanvas.getGraphicsContext2D().drawImage(img, 0, 0,
				snapshotCanvas.getWidth(), snapshotCanvas.getHeight());
	}

	// invoke this method after any event to adjust background size
	protected void adjustBackgroundSize() {
		if (tempNode != null) {
			Rectangle bkgRect = (Rectangle) graphContent.get(0);
			Bounds bounds = tempNode.getBoundsInParent();
			double leftGap = bkgRect.getX() - bounds.getMinX(), rightGap = bounds
					.getMaxX() - (bkgRect.getX() + bkgRect.getWidth()), topGap = bkgRect
					.getY() - bounds.getMinY(), bottomGap = bounds.getMaxY()
					- (bkgRect.getY() + bkgRect.getHeight());
			// check if left is out of bound
			leftGap = (leftGap < 0) ? 0 : leftGap;
			// check if right is out of bound
			rightGap = (rightGap < 0) ? 0 : rightGap;
			// check if top is out of bound
			topGap = (topGap < 0) ? 0 : topGap;
			// check if bottom is out of bound
			bottomGap = (bottomGap < 0) ? 0 : bottomGap;
			// compute new background rectangle size
			if (leftGap > topGap) {
				topGap = leftGap;
			} else {
				leftGap = topGap;
			}
			if (rightGap > bottomGap) {
				bottomGap = rightGap;
			} else {
				rightGap = bottomGap;
			}
			// resize and relocate background rectangle
			bkgRect.setX(bkgRect.getX() - leftGap);
			bkgRect.setWidth(bkgRect.getWidth() + leftGap + rightGap);
			bkgRect.setY(bkgRect.getY() - topGap);
			bkgRect.setHeight(bkgRect.getHeight() + topGap + bottomGap);
		}
	}
	public abstract void AddMuiGArea(String... property);
	public abstract void AddMuiGLink(String... property);
	/**
	 * Must add toggle buttons to graphLibPane, and set the userData of each
	 * toggle button as its corresponding Class extending GAbstractArea or
	 * GAbstractLink
	 * 
	 * @param graphLibPane
	 *            parent of toggle buttons
	 */
	public abstract void addLibToggles(VBox graphLibPane);

	/**
	 * get user-defined background fill color
	 * 
	 * @return Paint specified to fill background
	 */
	public abstract Paint getBackGroundColor();

}
