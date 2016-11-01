package ecnu.modana.ui.prism;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ecnu.modana.base.PluginMessage;
import ecnu.modana.model.ModelManager;
import ecnu.modana.model.ModelManager.ModelType;
import ecnu.modana.ui.MuiGEditor;
import ecnu.modana.ui.MuiModule;

public class PrismModule implements MuiModule {

	//module menu
	private Menu moduleMenu = null;
	//module name
	private String name="Markov CTMC";
	//XXX list of all created tabs
	private ArrayList<Tab> tabList = new ArrayList<Tab>();
	//XXX list of all created tree items
	private ArrayList<TreeItem<String>> treeItemList = new ArrayList<TreeItem<String>>();
	///note that tabs and tree items with the same index represent the same model!!!
	
	@Override
	public void recvMsg(PluginMessage pMsg, Object[] data) {}

	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getDescription() {
		return "Simple Prism DTMC or MDP model";
	}

	@Override
	public Menu getMainMenu() {
		return null;
	}

	@Override
	public Menu getModuleMenu() {
//		if (moduleMenu == null) {
//			moduleMenu = new Menu("Prism");
//			MenuItem checkMenuItem = new MenuItem("Check Model");
//			checkMenuItem.setOnAction(new EventHandler<ActionEvent>() {
//				@Override
//				public void handle(ActionEvent e) {
//					//TODO check model
//					System.out.println("chk result!");
//				}
//			});
//			moduleMenu.getItems().add(checkMenuItem);
//		}
		return moduleMenu;
	}

	@Override
	public TreeItem<String> createTreeModelExplorer(String modelName) {
		//TODO bind observable list of data
		TreeItem<String> treeItem = new TreeItem<String>(modelName);
		ModelManager.getInstance().CreateModel("./files/prism.ecore",modelName,ModelType.PrismModel);
		treeItem.setExpanded(true);
//		TreeItem<String> item = new TreeItem<> ("PrismDiagram");
//		treeItem.getChildren().add(item);
//		for (int i = 1; i < 10; i++) {
//			TreeItem<String> item = new TreeItem<> ("component" + i); 
//			treeItem.addEventHandler(MouseEvent.MOUSE_CLICKED,
//					new EventHandler<MouseEvent>() {
//				@Override
//				public void handle(MouseEvent e) {
//					if(e.getButton()==MouseButton.PRIMARY&&e.getClickCount()>1)
//					{
//						System.err.println("ah");
//					}
//				}});
//			treeItem.getChildren().add(item);
//		}
		AddModelContextMenu();
//		treeItem.addEventHandler(MouseEvent.MOUSE_CLICKED,
//				new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent e) {
//				//if(e.getButton()==MouseButton.SECONDARY) //右键事件
//				{
//					System.err.println("ah");
//				}
//			}});
		//TreeView<String> treeView=new TreeView<>(treeItem);
		return treeItem;
	}

	@Override
	public MuiGEditor createGaphicalEditor() {
		return new PrismEditor();
	}
	@Override
	public void AddModelContextMenu()
	{
		modelContextMenu.getItems().add(new MenuItem("new diagram"));
//		modelContextMenu.setOnShown(this);
//		modelContextMenu.setOnHidden(this);
	}
	@Override
	public void AddDiagramContextMenu()
	{
		
	}
}
