package ecnu.modana.FmiDriver;

import java.util.ArrayList;

public class Trace {
	public ArrayList<String> varNames=new ArrayList<>();
	/**
	 * Double,Float,String,Integer,Byte....coming from java.lang.
	 */
	public ArrayList<String> varTypes=new ArrayList<>();
	public ArrayList<ArrayList<Object>> statesList=new ArrayList<ArrayList<Object>>();
}
   