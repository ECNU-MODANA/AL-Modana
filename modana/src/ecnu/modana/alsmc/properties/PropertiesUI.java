package ecnu.modana.alsmc.properties;

import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import ecnu.modana.alsmc.util.UserFile;

/**
 * @author JKQ
 *
 */
public class PropertiesUI {
	//Properties window
    private Stage propertiesStage = null;
    private MenuBar menuBar = null;
    private PropertiesManager pManager = new PropertiesManager();
	 public void start(Stage propertiesStage) throws Exception  {
		 propertiesStage.getIcons().add(new Image("modana-logo.png"));
			this.propertiesStage = propertiesStage;
			propertiesStage.initModality(Modality.WINDOW_MODAL);
			propertiesStage.setOpacity(0.87);
			propertiesStage.setTitle("Properties List");
			propertiesStage.setWidth(500);
			propertiesStage.setHeight(450);
			propertiesStage.centerOnScreen();
//			propertiesStage.setResizable(false);
			BorderPane propertiesRoot = new BorderPane();
			initPlotPanel(propertiesRoot);
			Scene plotScene = new Scene(propertiesRoot);
			propertiesStage.setScene(plotScene);
			propertiesStage.show();
		}
	private void initPlotPanel(BorderPane propertiesRoot) {
		 VBox menuPane = new VBox(8);
	 	 addPropertiesMenu(menuPane);
	 	 propertiesRoot.setTop(menuPane);
	 	 SplitPane centerPane = new SplitPane();
	 	 centerPane.setDividerPositions(0.1f);
	 	 centerPane.setOrientation(Orientation.VERTICAL);
	 	 addPropertiesList(centerPane);
	 	 propertiesRoot.setCenter(centerPane);
	}
	private void addPropertiesList(SplitPane centerPane) {
		VBox buttonPane = new VBox(8);
		buttonPane.setPrefHeight(100);
		buttonPane.setMaxHeight(100);
		buttonPane.setMinHeight(0);
		buttonPane.setAlignment(Pos.CENTER);
		initPropertyButton(buttonPane);
		TableView<Properties> propertiesTableView = new TableView<Properties>();
		propertiesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		propertiesTableView.setEditable(true);
		// set data of table view
		propertiesTableView.setItems(pManager.propertiesObervableList);
		// add columns
		TableColumn<Properties, String> propertiesCol = new TableColumn<Properties, String>(
				"Property    Value");
		propertiesCol.setEditable(true);
		propertiesCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Properties, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<Properties, String> f) {
						return f.getValue().getProperties();
					}
				});
		propertiesCol.setCellFactory(new Callback<TableColumn<Properties, String>, TableCell<Properties, String>>() {
			@Override
			public TableCell<Properties, String> call(TableColumn<Properties, String> f) {
				TextFieldTableCell<Properties, String> cell = new TextFieldTableCell<Properties, String>(new MyTextConvertor());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		propertiesCol.setMinWidth(300);
		propertiesCol.setMaxWidth(500);
		propertiesCol.setPrefWidth(500);
		addPropertyOperation(propertiesCol,propertiesTableView);
		propertiesTableView.getColumns().add(propertiesCol);
		centerPane.getItems().addAll(buttonPane,propertiesTableView);
		
	}
	private void initPropertyButton(VBox buttonPane) {
		Button t = new Button("True");
		Button f = new Button("False");
		Button and = new Button("And");
		Button not = new Button("Not");
		Button or = new Button("Or");
		Button next = new Button("Next");
		Button until = new Button("Until");
		Button always = new Button("Always");
		Button eventually = new Button("Eventually");
		Button boundeduntil = new Button("Bounded Until");
		HBox hBox1 = new HBox(15);
		hBox1.setAlignment(Pos.CENTER);
		HBox hBox2 = new HBox(15);
		hBox2.setAlignment(Pos.CENTER);
		hBox1.getChildren().addAll(t,f,and,not,or);
		hBox2.getChildren().addAll(next,until,always,eventually,boundeduntil);
		buttonPane.getChildren().addAll(hBox1,hBox2);
	}
	private void addPropertyOperation(
			TableColumn<Properties, String> propertiesCol,
			TableView<Properties> propertiesTableView) {
		propertiesTableView.addEventHandler(MouseEvent.MOUSE_RELEASED,
				new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				if(e.getButton().equals(MouseButton.SECONDARY)){
				ContextMenu rightClickCol = new ContextMenu();
				MenuItem deleteProperty = new MenuItem("Delete Property");
				MenuItem verify = new MenuItem("Set Current Property");
				rightClickCol.getItems().addAll(deleteProperty,verify);
				propertiesTableView.setContextMenu(rightClickCol);
				deleteProperty.setOnAction(new EventHandler<ActionEvent>() {
		        	public void handle(ActionEvent e){
		        		pManager.propertiesObervableList.removeAll(propertiesTableView.getSelectionModel().getSelectedItems());   
		        	}
				});
				verify.setOnAction(new EventHandler<ActionEvent>() {
		        	public void handle(ActionEvent e){
		        		String proValue = propertiesTableView.getSelectionModel().getSelectedItems().get(0).getProperties().getValue();
		        		if (proValue!=null&&!proValue.equals("")) {
							UserFile.properties = proValue;
						}
		        	}
				});
			   }
			}
		});
		
	}
	private void addPropertiesMenu(VBox menuPane) {
		menuBar = new MenuBar();
        Menu menuProperties= new Menu("Properties");
        Menu menuAdd = new Menu("Add Property");
        MenuItem saveItem = new MenuItem("Save Properties");
        saveProperties(saveItem);
        MenuItem openItem = new MenuItem("Open Properties");
        openProperties(openItem);
        MenuItem addItem = new MenuItem("Add Property");
        addProperty(addItem);
        menuProperties.getItems().addAll(saveItem,openItem);
        menuAdd.getItems().add(addItem);
        menuBar.getMenus().addAll(menuProperties,menuAdd);
        menuPane.getChildren().add(menuBar);
	}
	private void addProperty(MenuItem addItem) {
		addItem.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e){
        		Properties pro = new Properties("P=?[F energy>=2200 ]");
        		pManager.propertiesObervableList.add(pro);
        	}
		});
		
	}
	private void openProperties(MenuItem openItem) {
		openItem.setOnAction(new EventHandler<ActionEvent>() {
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
	private void saveProperties(MenuItem saveItem) {
		saveItem.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e){
				FileChooser fileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
						"TXT files (*.txt)", "*.txt");
				fileChooser.getExtensionFilters().add(extFilter);
				Stage s = new Stage();
				File file = fileChooser.showSaveDialog(s);
				if (file == null)
					return;
				String FilePath = file.getAbsolutePath().toString().replace("\\","/");//.replaceAll(".txt", "")+ ".txt";
        	}
		});
	}
	public Stage getPropertiesStage() {
 		return propertiesStage;
 	}
}
