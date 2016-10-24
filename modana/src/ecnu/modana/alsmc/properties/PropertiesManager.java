package ecnu.modana.alsmc.properties;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
/**
 * @author JKQ
 *
 * 
 */
public class PropertiesManager {
	public ObservableList<Properties> propertiesObervableList=FXCollections.observableArrayList();
	public  int n = 0; // number of traces drawn so far
	public  int x = 0;// number of traces satisfying PBLTL so far
	public List<String> trace;
	public Random random = new Random();
	
	
}
