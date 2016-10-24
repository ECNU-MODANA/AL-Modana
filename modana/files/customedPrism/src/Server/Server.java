package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismCL;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.GenerateSimulationPath;
import simulator.SimulatorEngine;

public class Server
{
	public static boolean isStart=false;
	ServerSocket listen = null;
	char split=222;
	private ArrayList<HandOne>handOneList=new ArrayList<>();
	//public static PrismLog log=new PrismFileLog("stdout");
	public static PrismLog log=new PrismFileLog("D:/log.txt");
	private static Server server=null;
	public static Server getInstance() {  
        if (server == null) {    
            synchronized (Server.class) {    
               if (server == null) {    
            	   server = new Server(); 
               }    
            }    
        }    
        return server;   
    }  
	public void StartServer() 
	{		
		int port=40000;
		if(isStart)
		{
			log.println("Server has been started at port:"+port);
			log.flush();
			return;
		}
		try {
			listen = new ServerSocket(port);
			isStart=true;
			log.println("waiting request...at port:"+port);
			log.flush();
			while(true)
			{
				Socket client = listen.accept();
				HandOne handOne=new HandOne(client);
				new Thread(handOne).start();
				handOneList.add(handOne);
			}
		} catch (IOException e) {
			log.println("port in use:"+port+",can not start Server!!!");
			log.flush();
			e.printStackTrace();
			try {
				listen.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	/**
	 * Shut down the server, there exist some problem about exist the threads
	 * @return
	 */
	public boolean ShutDown()
	{
		try {
			log.println("try to end server");
			log.flush();
			listen.close();
			for(HandOne handOne:handOneList)
			{
				handOne.Close();
//				try {
//					handOne.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
			isStart=false;
			log.println("prism server closed!!!");
			log.flush();
			return true;
		} catch (IOException e) {
			log.println(e.getMessage());
			log.flush();
			e.printStackTrace();
			return false;
		}
	}
	public class HandOne implements Runnable
	{
//		private GUIPrism guiPrism=null;
//		private GUIMultiModel guiMultiModel=null;
//		private GUIMultiModelHandler guiMultiModelHandler=null;
//		private GUISimulator guiSimulator=null;
		
		private PrismCL prismCL=null;
		private Prism prism=null;
		GenerateSimulationPath genPath=null;
		private SimulatorEngine simulatorEngine=null;
		ModulesFile modulesFile=null;
		
		Socket socket=null;
		private boolean stop=false;
		private BufferedReader in;
		private PrintWriter out;
		public HandOne(Socket s)
		{
			try {
				socket=s;
				this.in=new BufferedReader(new InputStreamReader(s.getInputStream()));
				this.out=new PrintWriter(new OutputStreamWriter(s.getOutputStream()),true);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		@Override
		public void run() 
		{
			log.println("start:");
			log.flush();
			String cmd="",option="";
			long startTime=new Date().getTime();
			long time=2*60*1000; //two min
			while(!stop)
			{
				try {
					if((new Date().getTime()-startTime)>time)
					{	
						Out("chao shi tui chu:"+this.hashCode());
						return; 					
					}
					cmd=in.readLine();
					startTime=new Date().getTime();
					if(null==cmd) continue;
					int split=cmd.indexOf(':');
					if(split==-1)
					{
						log.println("error cmd:"+cmd);
						continue;
					}
					option="";
					option=cmd.substring(split+1,cmd.length());
					cmd=cmd.substring(0,split);
//					log.println("recive:"+cmd+" "+option);
//					log.flush();
					switch (cmd)
					{
//					case "test":
//						//this.test("ah.txt");
//						out.println("exist");
//						//socket.close();
//						break;
					case "cmd":
						if(null==prismCL) prismCL=new PrismCL();
						prismCL.run(option.split(" "));
						break;
					case "Open model":
						this.OpenModel(option);
						break;
					case "newPath":
						this.NewPath(modulesFile);
						break;
					case "doStep":
						DoStep("0".equals(option)?false:true,false);
						break;
					case "doSteps":
						DoStep("0".equals(option)?false:true,true);
						break;
					case "curValues":
						GetCurValues();
						break;
					case "setValue":
						if(option.length()>0)
							SetValue(option);
						break;
					case "getTime":
						out.println(String.valueOf(simulatorEngine.getTotalTimeForPath()));
						out.flush();
						break;
					case "exportPath":
						this.ExportPath(option);
						break;
					case "close":
						this.Close();
						return;
					default:
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			log.println("thread end");
			log.flush();
			if((new Date().getTime()-startTime)>time)
			{	
				Out("chao shi tui chu:"+this.hashCode());
				return; 					
			}
		}
		public boolean OpenModel(String filePath)
		{
			try {
				if(null==prism)
				{
					prism=new Prism(log);
					genPath=new GenerateSimulationPath(prism.getSimulator(), log);
					//simulatorEngine=new SimulatorEngine(prism);
					simulatorEngine=prism.getSimulator();
				}
				File file=new File(filePath);
				if(!file.exists())
				{
					log.println("file does not exist:"+filePath);
					log.flush();
					return false;
				}
				modulesFile= prism.parseModelFile(file);
				if(null==modulesFile)
				{
					log.println("parse error,please cheack model file:"+filePath);
					log.flush();
					return false;
				}
				simulatorEngine.createNewPath(modulesFile);
				simulatorEngine.initialisePath(null);
//				log.println("Model opened:"+filePath+",Type:"+modulesFile.getModelType());
//				out.flush();
				out.println(modulesFile.getModelType());
				Vector<String>vector= modulesFile.getVarNames();
				//log.print("vars:");
				if(vector.capacity()==0)
				{
					log.println();
					out.println("no variables in this model:"+filePath);
					out.flush();
					return true;
				}
				String res=vector.get(0);
				for(int i=1;i<vector.size();i++)
					res+=","+vector.get(i);
//				log.println(res);
//				log.flush();
				out.println(res);
				out.flush();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		public void NewPath(ModulesFile modulesFile)
		{
			try {
				if(null==modulesFile)
				{
					String resString="No model has been opened";
					Out(resString);
				}
				simulatorEngine.createNewPath(modulesFile);
				simulatorEngine.initialisePath(null);
			} catch (PrismException e) {
				e.printStackTrace();
			}
		}
		public void DoStep(boolean isReturnRes,boolean isMulti)
		{
			try {
				//simulatorEngine.automaticTransition();
				int steps=0;
				if(!isMulti)
					steps=simulatorEngine.automaticTransitions(1);
				else
					steps=simulatorEngine.automaticTransitions();
				if(isReturnRes)
				{
					out.println(simulatorEngine.GetCurState());
				    out.flush();
				}
//				log.println("how much steps did:"+steps);
//				log.flush();
			} catch (PrismException e) {
				log.println("doStep error:"+e.getMessage());
				log.flush();
				e.printStackTrace();
			} catch (Exception e) {
				out.println("error");
				out.flush();
				log.println("there is no currentState!");
				log.flush();
				e.printStackTrace();
			}
		}
		private void GetCurValues()
		{
			try {
				out.println(simulatorEngine.GetCurState());
			    out.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void SetValue(String option)
		{
			String[]temS=option.split(",");
			for(int i=0;i<temS.length;i+=2)
				simulatorEngine.SetValue(Integer.valueOf(temS[i]), temS[i+1]);
		}
		public void ExportPath(String filePath)
		{
			File file=new File(filePath);
			try {
				if(!file.exists())
					file.createNewFile();
				simulatorEngine.exportPath(file, false,",", null);
			} catch (IOException e) {
				log.println(e.getMessage());
				log.flush();
			} catch (PrismException e) {
				log.println(e.getMessage());
				log.flush();
			}
		}
		public void Close()
		{
			try {
				in.close();
				out.close();
				socket.close();
				stop=false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void test(String fileName)
		{
			String file="/home/ljf/workspace/java64/modana/"+fileName;
			try {
				new File(file).createNewFile();
			} catch (IOException e) {
				log.println("can not:"+file);
				log.flush();
				e.printStackTrace();
			}
		}
		private void Out(String msg)
		{
			log.println(msg);
			log.flush();
		}
	}
}
