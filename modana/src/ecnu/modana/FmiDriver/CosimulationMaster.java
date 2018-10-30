package ecnu.modana.FmiDriver;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;

import com.sun.jna.Pointer;

import ecnu.modana.FmiDriver.bean.State;
import ecnu.modana.util.MinaServer;
import ecnu.modana.util.MyLineChart;
public class CosimulationMaster extends FMUDriver{
	Logger logger = Logger.getRootLogger();
	private String host="127.0.0.1";
	private int port=40001;
	MinaServer minaServer;
	FMIModelDescription fmiModelDescription;
	Pointer fmiComponent;
	//public static String variables="";
	
	private LinkedHashMap<String, Exchange> IniMapping(){
		LinkedHashMap<String, Exchange> mappingMap=new LinkedHashMap<>();
		Exchange exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="h1";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="h[1]";
		mappingMap.put("smartBuildGc_smartBuildingNo.h[1]", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="h2";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="h[2]";
		mappingMap.put("smartBuildGc_smartBuildingNo.h[2]", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="h3";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="h[3]";
		mappingMap.put("smartBuildGc_smartBuildingNo.h[3]", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="h4";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="h[4]";
		mappingMap.put("smartBuildGc_smartBuildingNo.h[4]", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="h5";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="h[5]";
		mappingMap.put("smartBuildGc_smartBuildingNo.h[5]", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="derAllUn";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="derAllUn";
		mappingMap.put("smartBuildGc_smartBuildingNo.derAllUn", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="Controller";
		exchange.fromVariable="derEnergy";
		exchange.targetSlave="smartBuildGc_smartBuildingNo";
		exchange.targetVariable="derEnergy";
		mappingMap.put("smartBuildGc_smartBuildingNo.derEnergy", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="smartBuildGc_smartBuildingNo";
		exchange.fromVariable="Room[1]";
		exchange.targetSlave="Controller";
		exchange.targetVariable="Room1";
		mappingMap.put("Controller.Room1", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="smartBuildGc_smartBuildingNo";
		exchange.fromVariable="Room[2]";
		exchange.targetSlave="Controller";
		exchange.targetVariable="Room2";
		mappingMap.put("Controller.Room2", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="smartBuildGc_smartBuildingNo";
		exchange.fromVariable="Room[3]";
		exchange.targetSlave="Controller";
		exchange.targetVariable="Room3";
		mappingMap.put("Controller.Room3", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="smartBuildGc_smartBuildingNo";
		exchange.fromVariable="Room[4]";
		exchange.targetSlave="Controller";
		exchange.targetVariable="Room4";
		mappingMap.put("Controller.Room4", exchange);		
		
		exchange=new Exchange();
		exchange.fromSlave="smartBuildGc_smartBuildingNo";
		exchange.fromVariable="Room[5]";
		exchange.targetSlave="Controller";
		exchange.targetVariable="Room5";
		mappingMap.put("Controller.Room5", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="HumansActivity";
		exchange.fromVariable="humanNums1";
		exchange.targetSlave="Controller";
		exchange.targetVariable="humans1";
		mappingMap.put("Controller.humans1", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="HumansActivity";
		exchange.fromVariable="humanNums2";
		exchange.targetSlave="Controller";
		exchange.targetVariable="humans2";
		mappingMap.put("Controller.humans2", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="HumansActivity";
		exchange.fromVariable="humanNums3";
		exchange.targetSlave="Controller";
		exchange.targetVariable="humans3";
		mappingMap.put("Controller.humans3", exchange);
		
		exchange=new Exchange();
		exchange.fromSlave="HumansActivity";
		exchange.fromVariable="humanNums4";
		exchange.targetSlave="Controller";
		exchange.targetVariable="humans4";
		mappingMap.put("Controller.humans4", exchange);
		
		
		exchange=new Exchange();
		exchange.fromSlave="HumansActivity";
		exchange.fromVariable="humanNums5";
		exchange.targetSlave="Controller";
		exchange.targetVariable="humans5";
		mappingMap.put("Controller.humans5", exchange);
//		exchange.fromSlave="smartBuildingHuman";
//		exchange.fromVariable="humanNum1";
//		exchange.targetSlave="smartBuildGc_smartBuilding";
//		exchange.targetVariable="humans[1]";
//		mappingMap.put("smartBuildingHuman.humans[1]", exchange);
//		exchange=new Exchange();
//		exchange.fromSlave="smartBuildingHuman";
//		exchange.fromVariable="humanNum2";
//		exchange.targetSlave="smartBuildGc_smartBuilding";
//		exchange.targetVariable="humans[2]";
//		mappingMap.put("smartBuildingHuman.humans[2]", exchange);
		return mappingMap;
	}
	LinkedHashMap<String, Exchange>mappingMap=null;
	LinkedHashMap<String, FMUMESlave> fmusMap;
	LinkedHashMap<String, Object>toolSlaveMap;
	private void Ini(){
		//mappingMap=IniMapping();
		//variables="";
	}
	//double minStepSize=1e30;
	/**
	 * 热插拔式联合仿真算法
	 * @param fmus fmu路径列表
	 * @param toolModelMap
	 * @param mappingMap
	 * param eventHandleHt slaveIdentifier,eventName 作为key，操作Operation作为value
	 * @param startT
	 * @param endT
	 * @param stepSize
	 * @param masterType, 0 default, 1 rollback,2 predictable, 3 1+2
	 * @param resPath 
	 */
	public Trace CoSimulation(LinkedList<String>fmus,LinkedHashMap<String, String>toolModelMap,
			LinkedHashMap<String, Exchange>mappingMap,//Hashtable<MonitorVariables, Operation> plugConfig,
			double startT,double endT,double stepSize,int masterType,String resPath){
			Trace trace=new Trace();
			Ini();
			//mappingMap=IniMapping();
	    	PrintStream file = null;
	    	double iniStepsize=stepSize;
	    	
	    	ArrayList<String> slaveIdxName = null;
	    	ArrayList<Double> slaveDoneStepsize=null;
	    	ArrayList<Double> slavePredictStepsize=null;
		try {
			if(null==fmus) fmus=new LinkedList<>();
			if(null==toolModelMap) toolModelMap= new LinkedHashMap<>();
			if(null==mappingMap) mappingMap=new LinkedHashMap<>();
			if(masterType!=0)  slaveIdxName=new ArrayList<>();
			if(masterType==1||masterType==3) slaveDoneStepsize=new ArrayList<>();
			if(masterType==2||masterType==3) slavePredictStepsize=new ArrayList<>();
			
			//ini fmu
			LinkedHashMap<String, FMUMESlave> fmusMap=new LinkedHashMap<>();
			Exchange exchange = null;
			ArrayList<Object> tal=null;
			for(int i=0;i<fmus.size();i++){
				//ini    fmu
				FMUMESlave fmuSlave=new FMUMESlave(fmus.get(i));
//				FMIScalarVariable ii = fmuSlave.GetScalarVariable(fmuSlave.fmiModelDescription, "i");
//				double va = ii.getDouble(fmuSlave.fmiComponent);
//				ii.setDouble(fmuSlave.fmiComponent, 34);
//				va = ii.getDouble(fmuSlave.fmiComponent);
				fmusMap.put(fmuSlave.fmiModelDescription.modelIdentifier, fmuSlave);
				if(null!=slaveIdxName) slaveIdxName.add(fmuSlave.fmiModelDescription.modelIdentifier);
				//System.out.println("ah:"+fmuSlave._modelIdentifier);
				
				//get fmu varNames
				SlaveTrace slaveTrace=new SlaveTrace();
				slaveTrace.slaveName=fmuSlave.fmiModelDescription.modelIdentifier;
				tal= OutputRow.outputRowStr(fmuSlave._nativeLibrary, fmuSlave.fmiModelDescription,
						fmuSlave.fmiComponent, startT, file, ',', Boolean.TRUE);
				for(int ij=0;ij<tal.size();ij++)
					slaveTrace.varNameType.put((String) tal.get(ij), fmuSlave.GetVarType((String) tal.get(ij))); 
				
				//add ini State
				State state=new State();
				state.time=startT;
				tal= OutputRow.outputRowStr(fmuSlave._nativeLibrary, fmuSlave.fmiModelDescription,
						fmuSlave.fmiComponent, startT, file, ',', Boolean.FALSE);
				for(int ij=0;ij<tal.size();ij++)
					state.values.add(tal.get(ij));
				slaveTrace.statesList.add(state);
				
				trace.slaveMap.put(slaveTrace.slaveName, slaveTrace);
			}
			Entry entry;
			Iterator iter;
			//minaServer.toolFmuHt.clear();
			
			FMUMESlave fmumeSlave,tFmumeSlave = null;
			//PrismClient prismClient=null; //ToolSlave toolSlave=null;
			ToolSlave toolSlave=null;
			int fmumeSlaveCnt=fmusMap.size(),i=0;

			LinkedHashMap<String, Object> toolSlaveMap=new LinkedHashMap<>();
			iter = toolModelMap.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Entry) iter.next();
				String modelPath = (String) entry.getKey();
				String toolName = (String) entry.getValue();
				switch (toolName) {
				case "SourceCode":
					//Object object=Class.forName(modelPath).newInstance();
					toolSlave= (ToolSlave) Class.forName(modelPath).newInstance();
					break;
				default:
					break;
				}
				
				SlaveTrace slaveTrace=new SlaveTrace();
				slaveTrace.slaveName=toolSlave.slaveId;
		    	i=modelPath.lastIndexOf('/')+1;
		    	String modelName=modelPath.substring(i);
		    	modelName=modelName.substring(0,modelName.lastIndexOf('.'));
		    	String[] tem=toolSlave.OpenModel(modelPath).split(",");
		    	//if("SourceCode".equals(toolName))
		    	{
			    	for(int j=0;j<tem.length;j++){
			    		slaveTrace.varNameType.put(tem[j], "String"); //temp valued string			
			    	}
		    	}
		    	if(null!=slaveIdxName) slaveIdxName.add(modelName);
		    	
		    	//get ini State
		    	State state=new State();
				state.time=startT;
		    	tem=toolSlave.GetValues(null).split(",");
		    	for(int j=0;j<tem.length;j++)
		    		state.values.add(tem[j]);
		    	slaveTrace.statesList.add(state);
		    	
		    	trace.slaveMap.put(toolSlave.slaveId, slaveTrace);
		    	//toolSlave.OpenModel("");
		    	toolSlaveMap.put(toolSlave.slaveId, toolSlave);
			}
			this.fmusMap=fmusMap;
			this.toolSlaveMap=toolSlaveMap;
			
			double time=startT;
			boolean isExchangeTableIni=false;
			String tem,slaveId;
			int idx=0,slaveNums=0;
			double doneStepsize;
			if(null!=slaveIdxName) slaveNums=slaveIdxName.size();
			//int times=0;
			while(time<=endT){
				idx = 0;
				//Predict stepSize;
				if(null!=slavePredictStepsize){
					double minStepsize=1e30,td;
					iter = fmusMap.entrySet().iterator();
					while (iter.hasNext()) {
						entry = (Entry) iter.next();
						slaveId=(String) entry.getKey();
						fmumeSlave=(FMUMESlave) entry.getValue();
						td=fmumeSlave.Predict(time, iniStepsize);
						minStepsize=Math.min(minStepsize, td);
					}
					iter = toolSlaveMap.entrySet().iterator();
					while (iter.hasNext()) {
						entry = (Entry) iter.next();
						toolSlave=(ToolSlave) entry.getValue();
						td=toolSlave.Predict(time, iniStepsize);
						minStepsize=Math.min(minStepsize, td);
					}
					if(minStepsize>stepSize) stepSize=minStepsize;
					else if(stepSize!=iniStepsize) stepSize=iniStepsize;
				}
				
				iter = fmusMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Entry) iter.next();
 					slaveId=(String) entry.getKey();
					//if(slaveStateHt.get(slaveId)==slaveState.notActive) continue;
					fmumeSlave=(FMUMESlave) entry.getValue();
					doneStepsize= fmumeSlave.doStep(time, stepSize);
					if(null!=slaveDoneStepsize) slaveDoneStepsize.add(idx++, doneStepsize);
//					if(++times%100==0)
//						fmumeSlave.RollBackByStep(trace.slaveMap.get(fmumeSlave.fmiModelDescription.modelIdentifier), 90);
				}
				iter = toolSlaveMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Entry) iter.next();
					toolSlave=(ToolSlave) entry.getValue();
					//if(slaveStateHt.get(toolSlave.slaveId)==slaveState.notActive) continue;
					doneStepsize= toolSlave.DoStep(time,stepSize);
					if(null!=slaveDoneStepsize) slaveDoneStepsize.add(idx++, doneStepsize);
				}
				
				//need RollBack?
				if(null!=slaveDoneStepsize&&slaveDoneStepsize.size()>1){
					double minStepsize=slaveDoneStepsize.get(0);
					boolean needRollBack=false;
					for(int j=1;j<slaveNums;j++){
						minStepsize = Math.min(minStepsize, slaveDoneStepsize.get(j-1));
						minStepsize=Math.min(minStepsize, slaveDoneStepsize.get(j));
						if(slaveDoneStepsize.get(j).doubleValue()!=slaveDoneStepsize.get(j-1).doubleValue()) 
							needRollBack=true;
					}
					if(needRollBack){
						iter = fmusMap.entrySet().iterator();
						while (iter.hasNext()) {
							entry = (Entry) iter.next();
							slaveId=(String) entry.getKey();
							fmumeSlave=(FMUMESlave) entry.getValue();
							//RollBack one Step
							fmumeSlave.RollBackByStep(trace.slaveMap.get(fmumeSlave.fmiModelDescription.modelIdentifier), 1);
						}
						iter = toolSlaveMap.entrySet().iterator();
						while (iter.hasNext()) {
							entry = (Entry) iter.next();
							toolSlave=(ToolSlave) entry.getValue();
							toolSlave.RollBackByStep(trace.slaveMap.get(toolSlave.slaveId), 1);
						}
						if(minStepsize>0) stepSize=minStepsize;
						else System.err.println("Done stepsize <=0");
						continue; //time not updated, 
					}
				}
				else if(stepSize!=iniStepsize) stepSize=iniStepsize;
				
				//dataExchange
				iter=mappingMap.entrySet().iterator();
				if(!isExchangeTableIni){
					mappingMap= ExchangeTableIni(iter,mappingMap,fmusMap, toolSlaveMap);
					isExchangeTableIni=true;
				}
				DataExchange(mappingMap.entrySet().iterator(),null);
				//FMUEventTimeCollect(time,stepSize);
				
				time+=stepSize;
				
				//add State to Trace
				i=0;
				iter = fmusMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Entry) iter.next();
					fmumeSlave=(FMUMESlave) entry.getValue();
					State state=new State();
					state.time=time;
					tal= OutputRow.outputRowStr(fmumeSlave._nativeLibrary, fmumeSlave.fmiModelDescription,
							fmumeSlave.fmiComponent, startT, file, ',', Boolean.FALSE);
					for(int ij=0;ij<tal.size();ij++)
						state.values.add(tal.get(ij));
					SlaveTrace slaveTrace=trace.slaveMap.get(fmumeSlave.fmiModelDescription.modelIdentifier);
					slaveTrace.statesList.add(state);
				}
				iter = toolSlaveMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Entry) iter.next();
					String values=((ToolSlave)entry.getValue()).GetValues(null);
					//values=values.substring(values.indexOf(",")+1);
					if(null!=values&&values.length()>0){
						String[] tems=values.split(",");
						State state=new State();
						state.time=time;
						for(int ij=0;ij<tems.length;ij++)
							state.values.add(tems[ij]);
						SlaveTrace slaveTrace=trace.slaveMap.get(((ToolSlave)entry.getValue()).slaveId);
						slaveTrace.statesList.add(state);
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}finally {
			if(null!=file)
				file.close();
		}
		trace.TraceOut(resPath);
		return trace;
	}
	private Object GetValue(String slaveName,String varName){
		String res="";
		try {
			if(null!=fmusMap){
				FMUMESlave fmumeSlave=fmusMap.get(slaveName);
				if(null!=fmumeSlave)
					return fmumeSlave.GetValue(fmumeSlave.fmiModelDescription, varName, fmumeSlave.fmiComponent);
			}
			if(null!=toolSlaveMap){
				ToolSlave prismClient=(ToolSlave) toolSlaveMap.get(slaveName);
				if(null!=prismClient)
					return prismClient.GetValues(varName);
			}
		} catch (Exception e) {
			System.err.println("variable not found:"+slaveName+","+varName);
		}
		return res;
	}
	private boolean SetValue(String slaveName,String varName,String value){
		try {
			if(null!=fmusMap){
				FMUMESlave fmumeSlave=fmusMap.get(slaveName);
				if(null!=fmumeSlave)
					fmumeSlave.SetValue(fmumeSlave.fmiModelDescription, varName, fmumeSlave.fmiComponent, value);
			}
			if(null!=toolSlaveMap){
				ToolSlave prismClient=(ToolSlave) toolSlaveMap.get(slaveName);
				if(null!=prismClient)
					prismClient.SetValues(varName, value);
			}
		} catch (Exception e) {
			System.err.println("variable not found:"+slaveName+","+varName);
			return false;
		}
		return true;
	}
	/**
	 * 两类数据交换
	 * @param iter
	 * @param slaveName 该项为null表示为全部交换，  若指向一个slaveName则仅仅是它的交换
	 */
	private void DataExchange(Iterator iter,String slaveName){
		Exchange exchange;
		Entry entry;
		FMUMESlave fmumeSlave,tFmumeSlave;
		//PrismClient prismClient;
		ToolSlave toolSlave;
		while(iter.hasNext()){
			entry = (Entry) iter.next();
			exchange=(Exchange) entry.getValue();
			if(null!=slaveName && (!exchange.fromSlave.equals(slaveName)||!exchange.targetSlave.equals(slaveName))) continue; 
			if(exchange.targetSlave instanceof FMUMESlave){
				fmumeSlave=(FMUMESlave) exchange.targetSlave;
				//if(slaveStateHt.get(fmumeSlave._modelIdentifier)!=slaveState.exchangeableActive) continue;
				if(exchange.fromSlave instanceof FMUMESlave){
					tFmumeSlave=(FMUMESlave) exchange.fromSlave;
					//if(slaveStateHt.get(tFmumeSlave._modelIdentifier)!=slaveState.exchangeableActive) continue;
				fmumeSlave.SetValue(fmumeSlave.fmiModelDescription, exchange.targetVariable, fmumeSlave.fmiComponent,
						tFmumeSlave.GetValue(tFmumeSlave.fmiModelDescription, exchange.fromVariable, tFmumeSlave.fmiComponent));
				}
				else if(exchange.fromSlave instanceof ToolSlave)
				{
					//if(slaveStateHt.get(((ToolSlave)exchange.fromSlave).slaveId)!=slaveState.exchangeableActive) continue;
					fmumeSlave.SetValue(fmumeSlave.fmiModelDescription, exchange.targetVariable, fmumeSlave.fmiComponent,
							((ToolSlave)exchange.fromSlave).GetValues(exchange.fromVariable));
				}
			}
			else if(exchange.targetSlave instanceof ToolSlave){
				toolSlave=(ToolSlave) exchange.targetSlave;
				//if(slaveStateHt.get(toolSlave.slaveId)!=slaveState.exchangeableActive) continue;
				if(exchange.fromSlave instanceof FMUMESlave){
					tFmumeSlave=(FMUMESlave) exchange.fromSlave;
					//if(slaveStateHt.get(tFmumeSlave._modelIdentifier)!=slaveState.exchangeableActive) continue;
					toolSlave.SetValues(exchange.targetVariable, tFmumeSlave.GetValue(tFmumeSlave.fmiModelDescription, exchange.fromVariable, tFmumeSlave.fmiComponent).toString());
				}
				else if(exchange.fromSlave instanceof  ToolSlave)
				{
					//if(slaveStateHt.get(((ToolSlave)exchange.fromSlave).slaveId)!=slaveState.exchangeableActive) continue;
					toolSlave.SetValues(exchange.targetVariable,((ToolSlave)exchange.fromSlave).GetValues(exchange.fromVariable));
				}
			}
		}
	}
	private LinkedHashMap<String, Exchange> ExchangeTableIni(Iterator iter,LinkedHashMap<String, Exchange>mappingMap,
			LinkedHashMap<String, FMUMESlave> fmusMap,LinkedHashMap<String, Object>toolSlaveMap){
		LinkedHashMap<String, Exchange>res=new LinkedHashMap<>();
		while(iter.hasNext()){
			Entry entry = (Entry) iter.next();
			Exchange exchange = (Exchange) entry.getValue();
			String tem = (String) exchange.fromSlave;
			Object tObject=fmusMap.get(tem);
			if(null==tObject) tObject=toolSlaveMap.get(tem);
			if(null==tObject){
				System.err.println("标识符:"+tem+" 未找到实例slave");
			}
			else{
				exchange.fromSlave=tObject;
			}
			
			tem=(String) exchange.targetSlave;
			tObject=fmusMap.get(tem);
			if(null==tObject) tObject=toolSlaveMap.get(tem);
			if(null==tObject){
				System.err.println("标识符:"+tem+" 未找到实例slave");
			}
			else{
				exchange.targetSlave=tObject;
				//mappingMap.remove(tem);
				//mappingMap.put(tem+"."+exchange.targetVariable, exchange);
				res.put(tem+"."+exchange.targetVariable, exchange);
			}
		}
		return res;
	}
	class Exchange{
		/**
		 * 最初为String，而后被换成slave对象
		 */
		public Object fromSlave;
		public String fromVariable;
		public Object targetSlave;
		public String targetVariable;
		/**
		 * targetVariable=ration *fromVariable;
		 */
		public double multiplier;
	}
	public Exchange newEchange(){
		return new Exchange();
	}
	@Override
	public MyLineChart simulate(String fmuFileName, double endTime, double stepSize, boolean enableLogging,
			char csvSeparator, String outputFileName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 *获取所有FMU变量名以及顺序
	 *@param fmus FMU路径列表
	 */
	public List<String> getFMUVariables(List<String> fmus){
		List<String> FMUVariables = new ArrayList<>();
		try {
			for (String fmu : fmus) {
				FMIModelDescription fmuModel = new FMUMESlave(fmu).fmiModelDescription;
				String name = fmuModel.modelName;
				List scalarVariableList = fmuModel.modelVariables;
				for(int i=0;i<scalarVariableList.size();i++){
					FMIScalarVariable fv = (FMIScalarVariable)scalarVariableList.get(i);
					FMUVariables.add(name+"."+fv.name+"_"+i);
				}
			}
			return FMUVariables;
		}catch(Exception e){
			e.printStackTrace();
		}
		return FMUVariables;


	}
}