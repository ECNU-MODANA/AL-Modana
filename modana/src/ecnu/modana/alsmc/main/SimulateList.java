package ecnu.modana.alsmc.main;

import java.util.ArrayList;

public class SimulateList {
	
	public ArrayList<OneSimulate> simulateResult;
	
	public SimulateList() {
		
		super();
		this.simulateResult = new ArrayList<>();
	}

	public SimulateList(ArrayList<OneSimulate> simulateResult) {
		
		super();
		this.simulateResult = simulateResult;
	}
	
	public void addNewTraceToList(OneSimulate os) {
		simulateResult.add(os);
	}
	
	
}
