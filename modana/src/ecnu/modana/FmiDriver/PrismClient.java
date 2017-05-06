package ecnu.modana.FmiDriver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ecnu.modana.model.ModelManager;

public class PrismClient 
{
	public String slaveId="";
	public String modelType="ctmc";
	Socket client=null;
	BufferedReader in=null;
	PrintWriter out=null;
	boolean isModelOpened=false;
	HashMap<String, Integer>varMap=null;
	public String curValuses="";
	/**
	 * prism double xiao shu wei,ji wei de int jiu dai biao ji wei xiao shu
	 */
	public static int xiaoShuWei=1000;
	public int Id=0;
	
	private PrismClient(){}
	private PrismClient(int id){Id=id;}
	public static int ClientCnt=8;
	private static List<PrismClient> clientList=new ArrayList<PrismClient>(ClientCnt);
	private static List<Boolean>isInUse=new ArrayList<>(ClientCnt);
	private String allVariables="";
	public static PrismClient getInstance()
	{  
        if (clientList.size() == 0)
        {    
            synchronized (PrismClient.class) 
            {    
               if (clientList.size() == 0) 
               {    
            	   for(int i=0;i<ClientCnt;i++)
            	   {
            		   PrismClient prismClient=new PrismClient(i);
            		   prismClient.StartServer();
            		   prismClient.Start("127.0.0.1", 40000);
            		   clientList.add(prismClient);
            		   isInUse.add(false);
            	   }
               }    
            }    
        }
        //System.err.println(isInUse);
        while(true)
	        synchronized (PrismClient.class)
	        {
	        	for(int i=0;i<ClientCnt;i++)
	        	  if(isInUse.get(i)==false)
	        	  {
	        		  isInUse.set(i, true);
	        		  //System.err.println("get Id:"+clientList.get(i).Id);
	        		  return clientList.get(i);
	        	  }
	        	//ModelManager.getInstance().logger.error("all PrismClient in use");
	        }
    }  
	private static boolean isServerStarted=false;
	public void StartServer()
	{
		if(isServerStarted) return;
		new PrismWrapper().ExePrismCL("startserver");
		isServerStarted=true;
	}
	public void EndServer()
	{
		new PrismWrapper().ExePrismCL("endserver");
		isServerStarted=false;
	}
	private boolean isStart=false;
	public boolean Start(String host,int port)
	{
		try {
			if(isStart) return true;
			client = new Socket(host , port);
			in=new BufferedReader(new InputStreamReader(client.getInputStream()));
			out=new PrintWriter(client.getOutputStream(),true);
			ModelManager.getInstance().logger.debug("PrismClien start at port:"+port);
			isStart=true;
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean PrismCL(String command)
	{
		out.println("cmd:"+command);
		isModelOpened=true;
		return true;
	}
	public String OpenModel(String modelPath)
	{
		out.println("Open model:"+modelPath);
		out.flush();
		String res=null;
		try {
			modelType=in.readLine().toLowerCase();
			res = in.readLine();
			if(!"".equals(res))
			{
				isModelOpened=true;
				varMap=new HashMap<>();
				String[] temS=res.split(",");
				allVariables="";
				curValuses="";
				for(int i=0;i<temS.length;i++){
					varMap.put(temS[i], i);
					allVariables+="prism."+temS[i]+",";
					curValuses+="0,";
				}
				allVariables=allVariables.substring(0,allVariables.length()-1);
				curValuses=curValuses.substring(0,curValuses.length()-1);
				return res;
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void NewPath()
	{
		Out("newPath:");
	}
	/**
	 * 
	 * @param isMulti is do Multiple steps
	 */
	public String DoStep(boolean isMulti) throws Exception
	{
		if(!isModelOpened) throw new NullPointerException("model not Opened!");
		if(!isMulti)
			out.println("doStep:1");
		else 
			out.println("doSteps:1");
		out.flush();
		try {
			String tStr=in.readLine();
			if(tStr.length()>2)
			{
				curValuses=tStr.substring(1,tStr.length()-1);
//				curValuses=curValuses.replace("true", "1");
//				curValuses=curValuses.replace("false", "0");
				ModelManager.getInstance().logger.debug("dostep result:"+curValuses);
				return curValuses;
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public String CurValues()
	{
		out.println("curValues:");
		out.flush();
		try {
			String tStr=in.readLine();
			if(tStr.length()>2)
			{
				curValuses=tStr.substring(1,tStr.length()-1);
//				curValuses=curValuses.replace("true", "1");
//				curValuses=curValuses.replace("false", "0");
				//System.err.println("curValues:"+curValuses);
				return curValuses;
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public boolean Close()
	{
//		System.err.println("close:"+Id);
//		for(int i=0;i<clientList.size();i++)
//			System.err.print(clientList.get(i).Id);
//		System.err.println();
		isInUse.set(Id, false);
		return true;
//		try {
//			out.println("close:");
//			out.flush();
//			in.close();
//			out.close();
//			client.close();
//			isModelOpened=false;
//			return true;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
	}
	public String GetVariables(){
		return allVariables;
	}
	public String GetAllValues(){
		return curValuses;
	}
	public String GetValue(String varName)
	{
		if(!varMap.containsKey(varName))
		{
			ModelManager.getInstance().logger.error("var "+varName+" not in current model:"+varMap.keySet());
			return null;
		}
		return curValuses.split(",")[varMap.get(varName)];
	}
	public void SetValue(String varName,Object val)
	{
		if(!varMap.containsKey(varName))
		{
			ModelManager.getInstance().logger.error("var "+varName+" not in current model:"+varMap.keySet());
		}
		out.println("setValue:"+varMap.get(varName)+","+val);
	}
	public double GetTime()
	{
		Out("getTime:");
		try {
			String tString=in.readLine();
			if(null==tString||tString=="")
			{
				System.err.println("getTime error!!!");
				return 0;
			}
			return Double.valueOf(tString);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	private void Out(String msg)
	{
		out.println(msg);
		out.flush();
	}
	public void test()
	{
		Socket client;
		try {
			client = new Socket("127.0.0.1" , 4000);
			BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out=new PrintWriter(client.getOutputStream(),true);
		    //out.println("test:a");
//		    String c = in.readLine();
//	        System.out.println("收到:" + c);
		    //out.println("cmd:"+"/home/ljf/workspace/java64/modana/fmu.pm -simpath 10,sep=comma,values=1,0,2 /home/ljf/workspace/java64/modana/fmuT.txt");
			out.println("cmd:"+"/home/ljf/workspace/java64/modana/fmu.pm -simpath 10,sep=comma,values=1,0,2 stdout");
			out.println("Open model:/home/ljf/workspace/java64/modana/fmu.pm");
			System.err.println(in.readLine());
			int n=2;
			for(int i=0;i<n;i++)
			{
				out.println("doStep:1");
				String res=in.readLine();
				System.err.println(res);
			}
			out.println("exportPath:/home/ljf/workspace/java64/modana/fmu.txt");
	        out.close();
	        in.close();
	        client.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
