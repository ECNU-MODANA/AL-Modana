package ecnu.modana.alsmc.main;

import java.util.ArrayList;
import java.util.LinkedHashSet;


import ecnu.modana.FmiDriver.CoSimulation;

public class VerifierConfig {
	public static ArrayList<State> getConsimTrace(String markovModel,String fmuModel,LinkedHashSet<String>names,String sName,ArrayList<Integer>sList, int doubleNum,int intNum){
		
		CoSimulation coSimulation=new CoSimulation("127.0.0.1", 40000);
		ArrayList<State> res=new ArrayList<>();
		coSimulation.simulate(markovModel,markovModel.endsWith(".pm")?"dtmc":"ctmc",fmuModel, 5.5, 0.01,false, ',',res,names);
		if(res.size()<1) return res; 
		ArrayList<Object> tArrayList=res.get(0).values;
		String []tStrings=new String[tArrayList.size()];
		for(int i=0;i<tArrayList.size();i++)
			tStrings[i]=tArrayList.get(i).toString();
		res.remove(0);
		State.SetValueNames(tStrings,doubleNum,intNum);
		if(sName==null||sName==""||sList==null||sList.size()==0) return res;
		
		int p=0;
		for(int i=0;i<tArrayList.size();i++){
			if(!("fmu."+sName).equals(tArrayList.get(i))) continue;
		    tArrayList.remove(i);
		    for(int j=0;j<sList.size();j++) tArrayList.add("prism."+sName+sList.get(j));
		    p=i;
		    break;
		}
		int pos=0;
		ArrayList<Object> tL;
		for(int i=0,n=res.size(),j;i<n;i++){
		   	pos=Integer.valueOf(res.get(i).values.get(p).toString());
		   	tL=res.get(i).values;
		   	for(j=0;j<sList.size();j++){
		   		if(pos==sList.get(j)) tL.add("1");
		   		else tL.add("0");
		   	}
		   	tL.remove(p);
		   	res.get(i).SetValues(tL);
		}
		
		tStrings=new String[tArrayList.size()];
		for(int i=0;i<tArrayList.size();i++)
			tStrings[i]=tArrayList.get(i).toString();
		State.SetValueNames(tStrings,doubleNum,intNum);
		return res;
	}
}
