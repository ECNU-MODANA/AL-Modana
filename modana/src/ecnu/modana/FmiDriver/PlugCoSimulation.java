package ecnu.modana.FmiDriver;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.org.apache.bcel.internal.generic.NEW;

import ecnu.modana.util.MinaServer;
import ecnu.modana.util.MyLineChart;
enum slaveState{
	exchangeableActive,notExchangeableActive,notActive;
}
/**
 * sendSlave,eventIndicatorName,value,executeTime,delay,targetSlave,targetState;
 * @author LiuJufu
 */
class PlugConfig{
	public String sendSlave,eventIndicatorName,value;
	public double executeTime,delay;
	public String targetSlave,targetState;
}
class Operation{
	public double executeTime;
	public String targetSlave,targetState;
}
public class PlugCoSimulation extends FMUDriver{
	Logger logger = Logger.getRootLogger();
	private String host="127.0.0.1";
	private int port=40001;
	MinaServer minaServer;
	FMIModelDescription fmiModelDescription;
	Pointer fmiComponent;
	public static String variables="";
	
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
	private Hashtable<String, slaveState>slaveStateHt=null;
	private Queue<Operation> operationQ ;
	LinkedHashMap<String, Exchange>mappingMap=null;
	private LinkedList<MonitorVariables> monitorVariableList;
	LinkedHashMap<String, FMUMESlave> fmusMap;
	LinkedHashMap<String, Object>toolSlaveMap;
	private ArrayList<PlugConfig> plugConfigList=null;
	private void Ini(){
		slaveStateHt=new Hashtable<>();
		//mappingMap=IniMapping();
		variables="";
		plugConfigList=new ArrayList();
//		PlugConfig pC=new PlugConfig();
//		pC.sendSlave="smartBuildingHuman2";
//		pC.eventIndicatorName="eventIndicator_1";
//		pC.value="1";
//		pC.executeTime=-1;
//		pC.delay=0;
//		pC.targetSlave=pC.sendSlave;
//		pC.targetState="notActive";
//		plugConfigList.add(pC);
		operationQ = new PriorityQueue<>(1, TimeComparator);
	}
	//double minStepSize=1e30;
	private boolean isOut=true;
	/**
	 * 热插拔式联合仿真算法
	 * @param fmus fmu路径列表
	 * @param toolModelMap
	 * @param mappingMap
	 * @param eventHandleHt slaveIdentifier,eventName 作为key，操作Operation作为value
	 * @param startT
	 * @param endT
	 * @param stepSize
	 * @param resPath
	 */
	public void CoSimulation(LinkedList<String>fmus,LinkedHashMap<String, String>toolModelMap,
			LinkedHashMap<String, Exchange>mappingMap,//Hashtable<MonitorVariables, Operation> plugConfig,
			double startT,double endT,double stepSize,String resPath){
			Ini();
			mappingMap=IniMapping();
	    	PrintStream file = null;
		try {
			if(null==fmus) fmus=new LinkedList<>();
			if(null==toolModelMap) toolModelMap= new LinkedHashMap<>();
			if(null==mappingMap) mappingMap=new LinkedHashMap<>();
			
			//初始化fmu
			LinkedHashMap<String, FMUMESlave> fmusMap=new LinkedHashMap<>();
			Exchange exchange = null;
			for(int i=0;i<fmus.size();i++){
				FMUMESlave fmuSlave=new FMUMESlave(fmus.get(i));
				fmusMap.put(fmuSlave.fmiModelDescription.modelIdentifier, fmuSlave);
				slaveStateHt.put(fmuSlave._modelIdentifier, slaveState.exchangeableActive);
				//System.out.println("ah:"+fmuSlave._modelIdentifier);
			}
			Map.Entry entry;
			Iterator iter;
			//minaServer.toolFmuHt.clear();
			
			if(isOut){
				File outputFile = new File(resPath);
			    file = new PrintStream(resPath);
			    file.format("time");
			}
			FMUMESlave fmumeSlave,tFmumeSlave = null;
			//PrismClient prismClient=null; //ToolSlave toolSlave=null;
			ToolSlave toolSlave=null;
			int fmumeSlaveCnt=fmusMap.size(),i=0;
			iter = fmusMap.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry) iter.next();
//				Object key = entry.getKey();
//				Object val = entry.getValue();
				fmumeSlave=(FMUMESlave) entry.getValue();
				if(isOut)
				OutputRow.outputRowPlug(fmumeSlave._nativeLibrary, fmumeSlave.fmiModelDescription,
			    		fmumeSlave.fmiComponent, startT, file, ',', Boolean.TRUE);
				//if(++i!=fmumeSlaveCnt) file.format(",");
				//else file.format("%n");
			}

			LinkedHashMap<String, Object> toolSlaveMap=new LinkedHashMap<>();
			iter = toolModelMap.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry) iter.next();
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
//				prismClient=PrismClient.getInstance();
//		    	prismClient.StartServer();
//		    	if(!prismClient.Start(host, 40000))
//		    	{
//		    		logger.debug("no PrismServer,host:"+host+"port:"+port);
//		    	}
		    	i=modelPath.lastIndexOf('/')+1;
		    	String modelName=modelPath.substring(i);
		    	modelName=modelName.substring(0,modelName.lastIndexOf('.'));
		    	String[] tem=toolSlave.OpenModel(modelPath).split(",");
		    	//if("SourceCode".equals(toolName))
		    	{
			    	for(int j=0;j<tem.length;j++){
			    		if(isOut)
			    		file.format(","+toolSlave.slaveId+"."+tem[j]);
			    		variables+=","+toolSlave.slaveId+"."+tem[j];
			    	}
		    	}
		    	slaveStateHt.put(toolSlave.slaveId, slaveState.exchangeableActive);
		    	toolSlave.OpenModel("");
		    	toolSlaveMap.put(toolSlave.slaveId, toolSlave);
			}
			if(isOut)
			file.format("%n");
			monitorVariableList=GetMonitorVariables(variables);
			this.fmusMap=fmusMap;
			this.toolSlaveMap=toolSlaveMap;
			//print ini value
			i=0;
			if(isOut)
			file.format(String.valueOf(startT));
			iter = fmusMap.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry) iter.next();
				fmumeSlave=(FMUMESlave) entry.getValue();
				if(isOut)
				OutputRow.outputRowPlug(fmumeSlave._nativeLibrary, fmumeSlave.fmiModelDescription,
			    		fmumeSlave.fmiComponent, startT, file, ',', Boolean.FALSE);
				//if(++i!=fmumeSlaveCnt) file.format(",");
				//else file.format("%n");
			}
			iter = toolSlaveMap.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry) iter.next();
				String values=((ToolSlave)entry.getValue()).GetValues(null);
				if(null!=values&&values.length()>0){
					if(isOut)
					file.format(","+values);
				}
			}
			if(isOut)
			file.format("%n");
			
			double time=startT;
			boolean isExchangeTableIni=false;
			String tem,slaveId;
			while(time<=endT){
//				if(time>2){
//					time=time+1e-30;
//				}
				//dostep
				//minStepSize=1e30;
				iter = fmusMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Map.Entry) iter.next();
					slaveId=(String) entry.getKey();
					if(slaveStateHt.get(slaveId)==slaveState.notActive) continue;
					fmumeSlave=(FMUMESlave) entry.getValue();
					fmumeSlave.doStep(time, stepSize);
					//if(minStepSize>stepSize) minStepSize=stepSize;
				}
				iter = toolSlaveMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Map.Entry) iter.next();
					toolSlave=(ToolSlave) entry.getValue();
					if(slaveStateHt.get(toolSlave.slaveId)==slaveState.notActive) continue;
					double td= toolSlave.DoStep(time,stepSize);
					if(td>stepSize){
						slaveStateHt.put(toolSlave.slaveId, slaveState.notActive);
						Operation op=new Operation();
						op.executeTime=td+time;
						op.targetSlave=toolSlave.slaveId;
						op.targetState="exchangeableActive";
						operationQ.add(op);
					}
				}
				
				//dataExchange
				iter=mappingMap.entrySet().iterator();
				if(!isExchangeTableIni){
					mappingMap= ExchangeTableIni(iter,mappingMap,fmusMap, toolSlaveMap);
					isExchangeTableIni=true;
				}
				DataExchange(iter,null);
				//FMUEventTimeCollect(time,stepSize);
				
				//handle plug
				while(operationQ.size()>0){
					Operation op=operationQ.peek();
					if(time<=op.executeTime&&time+stepSize>=op.executeTime){
						switch (op.targetState) {
						case "exchangeableActive":
							slaveStateHt.put(op.targetSlave, slaveState.exchangeableActive);
							DataExchange(mappingMap.entrySet().iterator(), op.targetSlave);
							break;
						case "notExchangeableActive":
							slaveStateHt.put(op.targetSlave, slaveState.exchangeableActive);
							DataExchange(mappingMap.entrySet().iterator(), op.targetSlave);
							break;
						case "notActive":
							slaveStateHt.put(op.targetSlave, slaveState.notActive);
							break;
						default:
							break;
						}						
						operationQ.remove();
					}
					else if(time+stepSize<op.executeTime) break;
					else operationQ.remove();
				}
				time+=stepSize;
				
				//output trace
				i=0;
				if(isOut){
					file.format(String.valueOf(time));
					file.flush();
				}
				iter = fmusMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Map.Entry) iter.next();
					fmumeSlave=(FMUMESlave) entry.getValue();
					if(isOut)
					OutputRow.outputRowPlug(fmumeSlave._nativeLibrary, fmumeSlave.fmiModelDescription,
				    		fmumeSlave.fmiComponent, time, file, ',', Boolean.FALSE);
				}
				iter = toolSlaveMap.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Map.Entry) iter.next();
					String values=((ToolSlave)entry.getValue()).GetValues(null);
					if (slaveStateHt.get(((ToolSlave)entry.getValue()).slaveId)==slaveState.notActive) {
						int n=values.split(",").length;
						while(--n>=0)
							if(isOut)
							file.format(",");
						continue;
					}
					//values=values.substring(values.indexOf(",")+1);
					if(null!=values&&values.length()>0)
						if(isOut)
						file.format(","+values);
				}
				if(isOut){
					file.format("%n");
					file.flush();
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}finally {
			if(null!=file)
				file.close();
		}
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
				if(slaveStateHt.get(fmumeSlave._modelIdentifier)!=slaveState.exchangeableActive) continue;
				if(exchange.fromSlave instanceof FMUMESlave){
					tFmumeSlave=(FMUMESlave) exchange.fromSlave;
					if(slaveStateHt.get(tFmumeSlave._modelIdentifier)!=slaveState.exchangeableActive) continue;
				fmumeSlave.SetValue(fmumeSlave.fmiModelDescription, exchange.targetVariable, fmumeSlave.fmiComponent,
						tFmumeSlave.GetValue(tFmumeSlave.fmiModelDescription, exchange.fromVariable, tFmumeSlave.fmiComponent));
				}
				else if(exchange.fromSlave instanceof ToolSlave)
				{
					if(slaveStateHt.get(((ToolSlave)exchange.fromSlave).slaveId)!=slaveState.exchangeableActive) continue;
					fmumeSlave.SetValue(fmumeSlave.fmiModelDescription, exchange.targetVariable, fmumeSlave.fmiComponent,
							((ToolSlave)exchange.fromSlave).GetValues(exchange.fromVariable));
				}
			}
			else if(exchange.targetSlave instanceof ToolSlave){
				toolSlave=(ToolSlave) exchange.targetSlave;
				if(slaveStateHt.get(toolSlave.slaveId)!=slaveState.exchangeableActive) continue;
				if(exchange.fromSlave instanceof FMUMESlave){
					tFmumeSlave=(FMUMESlave) exchange.fromSlave;
					if(slaveStateHt.get(tFmumeSlave._modelIdentifier)!=slaveState.exchangeableActive) continue;
					toolSlave.SetValues(exchange.targetVariable, tFmumeSlave.GetValue(tFmumeSlave.fmiModelDescription, exchange.fromVariable, tFmumeSlave.fmiComponent).toString());
				}
				else if(exchange.fromSlave instanceof  ToolSlave)
				{
					if(slaveStateHt.get(((ToolSlave)exchange.fromSlave).slaveId)!=slaveState.exchangeableActive) continue;
					toolSlave.SetValues(exchange.targetVariable,((ToolSlave)exchange.fromSlave).GetValues(exchange.fromVariable));
				}
			}
		}
	}
	private void FMUEventTimeCollect(double nowTime,double stepSize){ //LinkedHashMap<String, FMUMESlave> fmusMap,LinkedHashMap<String, Object>toolSlaveMap
		String str;
		for(MonitorVariables mr:monitorVariableList){
			str=(String) GetValue(mr.slaveId, mr.varName);
			if(str.startsWith("Ini")) continue;
			if(mr.varName.startsWith("eventIndicator_")) {
				for(PlugConfig plugConfig:plugConfigList){
					if(mr.slaveId.equals(plugConfig.sendSlave)&&mr.varName.equals(plugConfig.eventIndicatorName)
						&&str.equals(plugConfig.value)){
							Operation op=new Operation();
							if(plugConfig.executeTime>0) op.executeTime=plugConfig.executeTime;
							else op.executeTime=plugConfig.delay+nowTime+stepSize;
							op.targetSlave=plugConfig.targetSlave;
							op.targetState=plugConfig.targetState;
							operationQ.add(op);
						}
				}
			}
			else if(mr.varName.startsWith("mountRequest_")){
				try {
					String []tems=str.split("/");
					if(tems.length!=5) continue;
					Operation op=new Operation();
					op.targetSlave=tems[0];
					if(!"".equals(tems[1]))
						op.executeTime=Double.valueOf(tems[1]);
					else 
						op.executeTime=Double.valueOf(tems[2])+nowTime;
					op.targetState=tems[4];
					operationQ.add(op);
					SetValue(mr.slaveId, mr.varName, "Ini");
				} catch (Exception e) {
					// TODO: handle exception
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
	public static Comparator<Operation> TimeComparator = new Comparator<Operation>(){
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.executeTime-o2.executeTime>0?1:-1;
		}
	};
	/**
	 * 获取需要监听的变量名称表
	 * @param variables
	 * @return
	 */
	private LinkedList<MonitorVariables> GetMonitorVariables(String variables){
		LinkedList<MonitorVariables> res=new LinkedList<>();
		if(null==variables) return res;
		String []tems=variables.split(","),ts;
		for(int i=0;i<tems.length;i++){
			ts=(tems[i]).split("\\.");
			if(ts.length!=2) continue;
			if(!ts[1].startsWith("eventIndicator_")&&!ts[1].startsWith("mountRequest_")) continue;
			MonitorVariables monitorVariables=new MonitorVariables();
			monitorVariables.slaveId=ts[0];
			monitorVariables.varName=ts[1];
			res.add(monitorVariables);
		}
		return res;
	}
	class MonitorVariables{
		public String slaveId,varName;
		public String value;
	}
	@Override
	public MyLineChart simulate(String fmuFileName, double endTime, double stepSize, boolean enableLogging,
			char csvSeparator, String outputFileName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
