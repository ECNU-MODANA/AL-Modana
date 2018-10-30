package ecnu.modana.FmiDriver;

import java.util.ArrayList;

import ecnu.modana.FmiDriver.bean.State;

public class TraceBean {
	
	ArrayList<State> trace = new ArrayList<State>();

	public ArrayList<State> getTrace() {
		return trace;
	}

	public void setTrace(ArrayList<State> trace) {
		this.trace = trace;
	}
	

}
