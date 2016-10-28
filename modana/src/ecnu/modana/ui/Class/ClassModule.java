package ecnu.modana.ui.Class;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import ecnu.modana.base.PluginMessage;
import ecnu.modana.ui.MuiGEditor;
import ecnu.modana.ui.MuiModule;

public class ClassModule implements MuiModule {

	//module menu
	private Menu moduleMenu = null;
	//XXX list of all created tabs
	private ArrayList<Tab> tabList = new ArrayList<Tab>();
	//XXX list of all created tree items
	private ArrayList<TreeItem<String>> treeItemList = new ArrayList<TreeItem<String>>();
	///note that tabs and tree items with the same index represent the same model!!!
	
	@Override
	public void recvMsg(PluginMessage pMsg, Object[] data) {}

	@Override
	public String getName() {
		return "Markov DTMC";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getDescription() {
		return "Simple Class Diagram";
	}

	@Override
	public Menu getMainMenu() {
		return null;
	}

	@Override
	public Menu getModuleMenu() {
		if (moduleMenu == null) {
			moduleMenu = new Menu("Class");
			MenuItem checkMenuItem = new MenuItem("Check Model");
			checkMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					//TODO check model
					System.out.println("chk result!");
				}
			});
			moduleMenu.getItems().add(checkMenuItem);
		}
		return moduleMenu;
	}

	@Override
	public TreeItem<String> createTreeModelExplorer(String modelName) {
		//TODO bind observable list of data
		TreeItem<String> treeItem = new TreeItem<String>("Class_Model");
		//treeItem.setExpanded(true);
		for (int i = 1; i < 10; i++) {
			TreeItem<String> item = new TreeItem<> ("component" + i);         
			treeItem.getChildren().add(item);
		}
		return treeItem;
	}

	@Override
	public MuiGEditor createGaphicalEditor() {
		return new ClassEditor();
	}
	@Override
	public void AddModelContextMenu()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void AddDiagramContextMenu()
	{
		// TODO Auto-generated method stub
		
	}

}
