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
		tArrayList.add(0,"time");
		res.remove(0);
		
		if(res.size()<1) return res;
		//移动Integer
		ArrayList<Integer>needApa=new ArrayList<>();
		ArrayList<Object> tArry=res.get(0).values;
		for(int i=0;i<tArry.size();i++)
			if(tArry.get(i) instanceof Integer)
				needApa.add(i);
		for(int i:needApa){
			Object t=tArrayList.get(i);
			tArrayList.remove(i);
			tArrayList.add(t);
			
			for(int j=0;j<res.size();j++){
				tArry=res.get(j).values;
				t=tArry.get(i);
				tArry.remove(i);
				tArry.add(t);
				res.get(j).SetValues(tArry);
			}
		}
		String []tStrings=new String[tArrayList.size()];
		for(int i=0;i<tArrayList.size();i++)
			tStrings[i]=tArrayList.get(i).toString();
		State.SetValueNames(tStrings,doubleNum,intNum);
		if(sName==null||sName=="") return res;
		if(sList==null||sList.size()==0){
			String tt="prism."+sName;
			int p=0;
			for(;p<tArrayList.size();p++)
				if(tt.equals(tArrayList.get(p))) break;
			if(p==tArrayList.size()){
				return res;
			}
			sList=new ArrayList<>();
			int tv;
			for(int i=0;i<res.size();i++){
				tv=Integer.valueOf(res.get(i).values.get(p).toString());
				//System.out.println("ah,"+tv);
				if(!sList.contains(tv))
					sList.add(tv);
			}
//			if(sList.size()>1) {
//				int ij=sList.size();
//				if(ij>0) ij++;
//			}
		}
		
		int p=0;
		for(int i=0;i<tArrayList.size();i++){
			if(!("prism."+sName).equals(tArrayList.get(i))) continue;
		    tArrayList.remove(i);
		    for(int j=0;j<sList.size();j++) tArrayList.add("prism."+sName+sList.get(j));
		    p=i;
		    break;
		}
		int pos=0;
		ArrayList<Object> tL;
		for(int i=0,n=res.size(),j;i<n;i++){			
		   	pos=(int) Double.parseDouble(res.get(i).values.get(p).toString());
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
