package ecnu.oe.cosimulationMaster.model;

import java.util.ArrayList;

public class State {
	public double time;
	public ArrayList<Object> values=new ArrayList<>();
	public String GetValues(){
		String res="";
		for (Object object : values) {
			res=res+","+object;
		}
		if(res.length()>0) return res.substring(1);
		return res;
	}
}
