package ecnu.modana.FmiDriver;

import java.util.ArrayList;

public abstract class ToolSlave{// extends FMUDriver{
	public String slaveId="";
	public String toolPath="";
	public ArrayList<String> variableTypes;
	//public MinaClient minaClient;
	public abstract boolean ConnectMaster(String host,int port); //MinaClient 返回
	public abstract String OpenModel(String modelPath);
	public abstract void NewPath();
	public abstract double DoStep(double curTime,double stepSize);
	public abstract String GetValues(String varNames);
	public abstract boolean SetValues(String varNames,String values);
	public void FreeSlave(){};
}
