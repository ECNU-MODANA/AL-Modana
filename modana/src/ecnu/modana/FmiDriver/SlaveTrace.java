package ecnu.modana.FmiDriver;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ecnu.modana.FmiDriver.bean.State;

public class SlaveTrace {	
	public String slaveName="slaveName";
//	public ArrayList<String> varNames=new ArrayList<>();
//	/**
//	 * Double,Float,String,Integer,Byte....coming from java.lang.
//	 */
//	public ArrayList<String> varTypes=new ArrayList<>();
	/**
	 * varName,varType
	 * varType:  Double,Float,String,Integer,Byte....coming from java.lang.
	 */
	public LinkedHashMap<String, String> varNameType=new LinkedHashMap<>();
	public ArrayList<State> statesList=new ArrayList<State>();
	public ArrayList<String> GetVarNames(){
		ArrayList<String> res=new ArrayList<>();
		if(varNameType.size()==0) return res;
		for (String str : varNameType.keySet()) {
			res.add(str);
		}
		return res;
	}
//	public String GetVarNames(){
//		String res="";
//		if(varNameType.size()==0) return res;
//		for (String str : varNameType.keySet()) {
//			res=res+","+str;
//		}
//		if(res.length()>0) return res.substring(1);
//		return res;
//	}
}