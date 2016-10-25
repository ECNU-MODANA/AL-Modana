package ecnu.modana.alsmc.main;

import java.util.ArrayList;

import ecnu.modana.FmiDriver.CoSimulation;

public class VerifierConfig {
	public static ArrayList<State> getConsimTrace(String markovModel,String fmuModel,ArrayList<String>names){
		CoSimulation coSimulation=new CoSimulation("127.0.0.1", 40000);
		ArrayList<State> res=new ArrayList<>();
		coSimulation.simulate(markovModel,markovModel.endsWith(".pm")?"dtmc":"ctmc",fmuModel, 5.5, 0.01,false, ',',new ArrayList<State>(),null);
		return res;
	}
}
