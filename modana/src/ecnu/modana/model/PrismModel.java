package ecnu.modana.model;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.ptolemy.fmi.FMUFile;

import ecnu.modana.Properties.Trace;
import ecnu.modana.alsmc.main.State;
import ecnu.modana.Modana;
import ecnu.modana.model.ModelManager.ModelType;
import ecnu.modana.ui.ModanaUI;
import ecnu.modana.ui.MuiGArea;
import ecnu.modana.ui.MuiGLink;
import ecnu.modana.ui.prism.PrismEditor;
import ecnu.modana.ui.prism.PrismState;
import ecnu.modana.ui.prism.PrismTransition;
import ecnu.modana.util.MyLineChart;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import ecnu.modana.FmiDriver.*;

public class PrismModel extends AbstractModel
{
	Logger logger = Logger.getRootLogger();
	public PrismModel(String name,String ecoreFile, String modelEClassName)
	{
		super(name,ecoreFile, modelEClassName);
	}
	private String projectDir;
	public String getProjectDir() {
		return projectDir;
	}
	public void setProjectDir(String projectDir) {
		this.projectDir = projectDir;
	}
	public ObservableList<Lable> lableObervableList=FXCollections.observableArrayList();
	public ObservableList<Reward> rewardObervableList=FXCollections.observableArrayList();
	public ObservableList<Formula> formulaObervableList=FXCollections.observableArrayList();
	public ObservableList<Module> moduleObervableList=FXCollections.observableArrayList();
	public ObservableList<Variable> variableObervableList=FXCollections.observableArrayList();
	@Override
	public boolean SaveModel(String filePath)
	{
		UpdateDiagram();
		SetModelNameType(name, type);
		int i=0;
		//for(ObservableList<Node> observableList:graphContentMap.values())
		for(String moduleName:graphContentMap.keySet())
		{
			ObservableList<Node> observableList=graphContentMap.get(moduleName);
			if(observableList==null) continue;
			System.err.println(observableList.size());
			//String moduleName="PrismDiagram";
			SaveElement("PrismModule",moduleName,moduleName);
			for(Node node:observableList)
			{
				Object object=node.getUserData();
				if(object instanceof MuiGArea)
				{
					PrismState muiGArea=(PrismState)object;
					SaveElement("PrismState", muiGArea.name.getValue()+muiGArea.index.getValue(),muiGArea.name.getValue()+muiGArea.index.getValue(),muiGArea.index.getValue(),
							Double.toString(muiGArea.offsetX),Double.toString(muiGArea.offsetY),Double.toString(node.getBoundsInLocal().getWidth()),Double.toString(node.getBoundsInLocal().getHeight()));
					modelIO.SetReference(moduleName, "states", muiGArea.name.getValue()+muiGArea.index.getValue());
				}
			}
			for(Node node:observableList)
			{
				Object object=node.getUserData();
				 if (object instanceof MuiGLink)
					{
						PrismTransition prismTransition=(PrismTransition)object;
						String transitionName=Integer.toString(prismTransition.hashCode());
						SaveElement("PrismTransition",transitionName,transitionName,prismTransition.syn.getValue(),prismTransition.condition.getValue(),
								prismTransition.prob.getValue(),prismTransition.assigns.getValue(),Integer.toString(prismTransition.getSourcePtIndex()),Integer.toString(prismTransition.getTargetPtIndex()));

						PrismState muiGArea=(PrismState)prismTransition.getSourceArea();
						modelIO.SetReference(transitionName, "source", muiGArea.name.getValue()+muiGArea.index.getValue());
						muiGArea=(PrismState)prismTransition.getTargetArea();
						modelIO.SetReference(transitionName, "target", muiGArea.name.getValue()+muiGArea.index.getValue());
						
						modelIO.SetReference(moduleName, "transitions", Integer.toString(prismTransition.hashCode()));
					}
			}
			modelIO.SetReference(name, "modules", moduleName);
			
			for(Lable lable:lableObervableList)
			{
				SaveElement("PrismLabel", lable.name.getValue(),lable.name.getValue(), lable.value.getValue());
				modelIO.SetReference(name, "labels", lable.name.getValue());
			}
//			for(Reward reward:rewardObervableList)
//			{
//				SaveElement("PrismLabel", lable.name.toString(), lable.value.toString());
//				modelIO.SetReference(name, "labels", lable.name.toString());
//			}
			for(Formula formula:formulaObervableList)
			{
				SaveElement("PrismFormula", formula.name.getValue(),formula.name.getValue(), formula.value.getValue());
				modelIO.SetReference(name, "formulas", formula.name.getValue());
			}
			for(Module module:moduleObervableList)
			{
				SaveElement("ModuleDef", module.name.getValue(),module.name.getValue(), module.value.getValue());
				modelIO.SetReference(name, "ModuleDefs", module.name.getValue());
			}
			for(Variable variable:variableObervableList)
			{
				SaveElement("Variable", variable.name.getValue(),variable.name.getValue(), variable.value.getValue());
				modelIO.SetReference(name, "Variables", variable.name.getValue());
			}
		}
		
		modelIO.SaveModel(filePath);
		modelIO.DeleteAllElements();
		this.modelPath=filePath;
		return true;
	}
	/**
	 * read model file,and add relate graphic element to last list
	 * @param ecoreFile
	 * @param filePath
	 * @return
	 */
	@Override
	public boolean LoadFromFile(String filePath)
	{
//		Check("C:\\java\\jre32\\bin","E:\\研究生\\工具\\prism-4.2.beta1",
//				"E:\\研究生\\工具\\prism-4.2.beta1\\examples\\dice\\dice.pm","E:\\研究生\\工具\\prism-4.2.beta1\\examples\\dice\\dice.pctl -prop 2");
//		Check("C:\\Program Files (x86)\\Java\\jre1.8.0_60\\bin","C:\\Program Files (x86)\\prism-4.2.beta1",
//				"./11.pm","./dice.pctl");
		System.err.println("open Model:"+filePath);
		modelIO.LoadModel(filePath);
		name=(String) modelIO.getEStructFeature(modelIO.model, "name");
		String modelName=name,diagramName="PrismDiagram";
		ModanaUI modanaUI= Modana.getInstance().getPluginInstancesByType(ModanaUI.class).get(0);
		modanaUI.AddModel(ModelType.PrismModel, modelName);
		for(ArrayList<String> tArrayList:modelIO.loadRes)
		{
			if("PrismModule".equals(tArrayList.get(0)))
			{
				diagramName=tArrayList.get(1);
				modanaUI.AddDiagram(modelName,diagramName);
			}
			//System.err.println(tArrayList);
		}
		
		PrismEditor prismEditor=(PrismEditor) ModelManager.getInstance().GetEditor(modelName, diagramName);
		for(ArrayList<String> tArrayList:modelIO.loadRes)
			if("PrismState".equals(tArrayList.get(0)))
			{
				prismEditor.AddMuiGArea(tArrayList.get(1),tArrayList.get(2),tArrayList.get(3),tArrayList.get(4));
			}
			else if("PrismTransition".equals(tArrayList.get(0)))
			{
				int i=2;
				prismEditor.AddMuiGLink(tArrayList.get(i++),tArrayList.get(i++),tArrayList.get(i++),tArrayList.get(i++),
						tArrayList.get(i++),tArrayList.get(i++),tArrayList.get(i++),tArrayList.get(i++));
			}
			else if("PrismFormula".equals(tArrayList.get(0)))
			{
				((PrismModel)ModelManager.getInstance().GetModel(modelName)).formulaObervableList.
					add(new Formula(tArrayList.get(1), tArrayList.get(2)));
			}
			else if("PrismLabel".equals(tArrayList.get(0)))
			{
				((PrismModel)ModelManager.getInstance().GetModel(modelName)).lableObervableList.
					add(new Lable(tArrayList.get(1), tArrayList.get(2)));
			}
			else if("ModuleDef".equals(tArrayList.get(0)))
			{
				((PrismModel)ModelManager.getInstance().GetModel(modelName)).moduleObervableList.
					add(new Module(tArrayList.get(1), tArrayList.get(2)));
			}
			else if("Variable".equals(tArrayList.get(0)))
			{
				((PrismModel)ModelManager.getInstance().GetModel(modelName)).variableObervableList.
					add(new Variable(tArrayList.get(1), tArrayList.get(2)));
			}
		prismEditor.FinalUpdate();
		modelIO.DeleteAllElements();
		projectDir=filePath.substring(0,filePath.lastIndexOf('/')+1);
		ModelManager.getInstance().modelListMap.put(name, this);
		this.modelPath=filePath;
		return true;
		
//		modelIO.LoadModel(filePath);
//		System.err.println("ha");
//		for(ArrayList<String> tArrayList:modelIO.loadRes)
//			System.err.println(tArrayList);
//		return true;
		
//		ModanaUI.getInstance().AddModelTree("PrismEditor");
//		return true;
	}
	public void GeneratorCode(String modelPath)
	{
		if(new File(this.modelPath).exists()) modelPath=this.modelPath;
		PrismCodeGenerator prismCodeGenerator=new PrismCodeGenerator(this.ecoreFile);
		System.err.println(projectDir);
		prismCodeGenerator.generatePrismFile(modelPath, projectDir+name+".pm");
	}
	public void Check(String modelPath,String propertyPath)
	{
		GeneratorCode(modelPath);
		if(projectDir!=null)
		{
			modelPath=projectDir+name+".pm";
			propertyPath=projectDir+name+propertyPath;
		}
//		new PrismWrapper(modelPath, propertyPath).Check(modelPath, propertyPath);
		new PrismWrapper().simulate(modelPath, "-simpath 10,vars=(s,in_v,out_v),sep=comma", "./test.txt");
//		logger.debug(new PrismWrapper(modelPath, propertyPath).simulate(modelPath, "-simpath 1,sep=comma,values=1,0,2", projectDir+"outFmu.txt",0,1,"s","v"));
//		logger.debug(new PrismWrapper(modelPath, propertyPath).simulate(modelPath, "-simpath 3,vars=(s,c,v),sep=comma", "./test.txt",0,3,"s","v"));
		
//		PrismClient prismClient= new PrismClient();
//		if(!prismClient.Start("127.0.0.1", 4000)) return;
//		prismClient.PrismCL(modelPath+" "+propertyPath);//+" "+projectDir+"22.txt");
//		prismClient.Close();
	}
	public LineChart<Object, Number> CoSimulation(String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, String outputFileName) throws Exception
	{
		//GeneratorCode(modelPath);
		if(projectDir!=null)
		{
			modelPath=projectDir+name+".pm";
		}
		CoSimulation coSimulation=new CoSimulation("127.0.0.1",40000);
		return coSimulation.simulate(modelPath,this.type,fmuFileName, endTime, stepSize,enableLogging, ',',new ArrayList<State>(),null,false);
	}
	public Trace CoSimulationTrace(String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, String outputFileName) throws Exception
	{
		//GeneratorCode(modelPath);
		if(projectDir!=null)
		{
			modelPath=projectDir+name+".pm";
		}
		CoSimulation coSimulation=new CoSimulation("127.0.0.1",40000);
		return coSimulation.simulateTrace(modelPath,this.type,FMUFile.parseFMUFile(fmuFileName), endTime, stepSize,enableLogging,',', outputFileName);
	}
	public Trace CoSimulationTradition(String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, String outputFileName) throws Exception
	{
		//GeneratorCode(modelPath);
		if(projectDir!=null)
		{
			modelPath=projectDir+name+".pm";
		}
		CoSimulation coSimulation=new CoSimulation("127.0.0.1",40000);
		return coSimulation.simulateTradition(modelPath,this.type,FMUFile.parseFMUFile(fmuFileName), endTime, stepSize,enableLogging,',', outputFileName);
	}
	
	//add Lable table view
	public static class Lable implements Serializable
	{
		private StringProperty name;
		private StringProperty value;
		public Lable(){}
		public Lable(String name,String value){
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleStringProperty(value);
		}
		public StringProperty getName() {
			return name;
		}
		public StringProperty getValue() {
			return value;
		}
		 private void writeObject(ObjectOutputStream out) throws IOException { 
			 //out.defaultWriteObject();   
			 out.writeUTF(name.getValue());
			 out.writeUTF(value.getValue());	 
		}  
		 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {       
			 //in.defaultReadObject();       
			 name  = new SimpleStringProperty(in.readUTF());  
			 value  = new SimpleStringProperty(in.readUTF());
		}  
	}
	//add Formula table view
	public static class Formula{
		private StringProperty name;
		private StringProperty value;
		public Formula(String name,String value){
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleStringProperty(value);
		}
		public StringProperty getName() {
			return name;
		}
		public StringProperty getValue() {
			return value;
		}
	}
	//add Reward table view
	public static class Reward{
		private StringProperty name;
		private StringProperty value;
		public Reward(String name,String value){
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleStringProperty(value);
		}
		public StringProperty getName() {
			return name;
		}
		public StringProperty getValue() {
			return value;
		}
		
	}
	public static class Module{
		private StringProperty name;
		private StringProperty value;
		public Module(String name,String value){
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleStringProperty(value);
		}
		public StringProperty getName() {
			return name;
		}
		public StringProperty getValue() {
			return value;
		}
		
	}
	public static class Variable{
		private StringProperty name;
		private StringProperty value;
		public Variable(String name,String value){
			this.name = new SimpleStringProperty(name);
			this.value = new SimpleStringProperty(value);
		}
		public StringProperty getName() {
			return name;
		}
		public StringProperty getValue() {
			return value;
		}
		
	}
}
