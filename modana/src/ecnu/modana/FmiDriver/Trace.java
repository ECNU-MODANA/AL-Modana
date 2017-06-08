package ecnu.modana.FmiDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
/**
 * like OneSimulate, contains many slaves
 * @author LiuJufu
 */
import java.util.LinkedHashMap;
public class Trace {
	public LinkedHashMap<String,SlaveTrace> slaveMap=new LinkedHashMap<>();
	public void TraceOut(String filePath){
		//if(null==filePath||filePath)
		File outputFile = new File(filePath);
		PrintStream file=null;
		try {
			file = new PrintStream(filePath);
			String tem="",res="";
			ArrayList<String> ts;
			int i;
			for (SlaveTrace slaveTrace : slaveMap.values()) {
				ts=slaveTrace.GetVarNames();
				for(i=0;i<ts.size();i++)
					res=res+","+slaveTrace.slaveName+"."+ts.get(i);
			}
			file.format("%s", "time"+res+"\n");
			
			//outPut States
			int numOfStates=-1;
			for (SlaveTrace slaveTrace : slaveMap.values()) {
				if(-1==numOfStates)
					numOfStates=slaveTrace.statesList.size();
			}
			boolean isTime=false;
			for(int j=0;j<numOfStates;j++){
				res="";
				for (SlaveTrace slaveTrace : slaveMap.values()) {
					if(isTime==false){ //time
						res=res+slaveTrace.statesList.get(j).time; 
						isTime=true;
					}
					res=res+","+slaveTrace.statesList.get(j).GetValues();
				}
				isTime=false;
				file.format("%s", res+"\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally {
			if(null!=file) {
				file.flush();
				file.close();
			}
		}
	}
}
