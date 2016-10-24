package ecnu.modana.FmiDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import com.sun.media.jfxmedia.track.Track.Encoding;

import ecnu.modana.util.MyUtil;

public class PrismWrapper 
{
	Logger logger = Logger.getRootLogger();
	//public static String jre32Path="D:\\ProgramFiles\\java\\jre32\\bin";
	//public static String prismPath="E:\\postGraduate\\工具\\prism-4.2.beta1";
	
	public static String jre32Path=null;//"C:\\Program Files (x86)\\Java\\jdk32\\bin";
	public static String prismPath=null;//"E:\\FMI\\prism";
	
//	public static String jre32Path="/usr/lib/jvm/jdk1.8.0_65/bin";
//	public static String prismPath="/home/ljf/postg/reseacher/prism-4.3-linux64";
	private String codePath,propertyPath;
	private String preStr;
	private String[]pathStr;
	public PrismWrapper()
	{
		Ini();
	}
	public PrismWrapper(String codePath,String propertyPath)
	{
		this.codePath=codePath;
		this.propertyPath=propertyPath;
		Ini();
	}
	private void Ini()
	{
		if(null==jre32Path) 
		{
			prismPath=System.getProperty("user.dir")+"/files/"+"customedPrism";
			InputStreamReader read;
			try {
				read = new InputStreamReader(
				new FileInputStream(System.getProperty("user.dir")+"/configuration.txt"),"utf-8");
			    BufferedReader bufferedReader = new BufferedReader(read);
			    jre32Path=bufferedReader.readLine();
			    jre32Path=jre32Path.substring(8).replace('\\', '/');
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				MyUtil.showAlertDialog("configuration file no exist!");
				e.printStackTrace();
			} catch (IOException e) {
				MyUtil.showAlertDialog(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String osName=System.getProperty("os.name");
		//logger.debug("osName:"+osName);
		if(osName.startsWith("Windows"))
		{
			String CP=prismPath+"\\lib\\prism.jar;"+prismPath+"\\classes;"+prismPath+";"+prismPath+"\\lib\\pepa.zip;"+prismPath+"\\lib\\*"; //classpath
			preStr=jre32Path+"\\java -Djava.library.path=\""+prismPath+"\\lib\""+" -classpath \""+CP+"\" prism.PrismCL ";
			pathStr=new String[]{"path="+prismPath+"\\lib;"+System.getenv("path")}; //add path?
		}
		else if(osName.startsWith("Linux"))
		{
			String CP=prismPath+"/lib/prism.jar;"+prismPath+"/classes;"+prismPath+";"+prismPath+"/lib/pepa.zip;"+prismPath+"/lib/*"; //classpath
			preStr=jre32Path+"/java -Djava.library.path=\""+prismPath+"/lib\""+" -classpath \""+CP+"\" prism.PrismCL ";
			pathStr=new String[]{"path="+prismPath+"/lib;"+System.getenv("path")}; //add path?sm ";
			//preStr=prismPath+"prism ";
		}
	}
	public void ExePrismCL(String prismCmd)
	{
		exeCmd(preStr+prismCmd, pathStr,false);
	}
	public void Check(String modelPath,String propertyPath)
	{
		String commandStr=preStr+modelPath+" "+propertyPath;
		exeCmd(commandStr, pathStr,true);
	}
	/**
	 * Debugging Models With The Simulator
	 * @param modelPath Prism model path
	 * @param Options -simpath Options
	 * @param outPath output trace path
	 */
	public void simulate(String modelPath,String Options,String outPath)
	{
		logger.debug(modelPath+" "+Options+" "+outPath);
		exeCmd(preStr+modelPath+" "+Options+" "+outPath, pathStr,true);
	}
	/**
	 * Debugging Models With The Simulator
	 * @param modelPath Prism model path
	 * @param Options -simpath Options
	 * @param outPath output trace path
	 * @param startStep get the startStep-th's result ;0 represent the initial state
	 * @param endStep get the endStep-th's result,from startStep to endStep
	 * @param getResults the parameter name
	 * @return
	 */
	public List<List<Object>> simulate(String modelPath,String Options,String outPath,int startStep,int endStep,String...getResults)
	{
		List<List<Object>> res=new ArrayList<List<Object>>();
		List<Object> temRes;//=new ArrayList<Object>();
		//logger.debug(modelPath+" "+Options+" "+outPath+"from "+startStep+" to"+endStep+" "+getResults);
		exeCmd(preStr+modelPath+" "+Options+" "+outPath, pathStr,true);
		try {
            String encoding="GBK";
            File file=new File(outPath);
            if(file.isFile() && file.exists())
            { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int i=-1,j;
                String[] temStrs;
                HashMap<String, Integer>tableHeader=new HashMap<>();
                while((lineTxt = bufferedReader.readLine()) != null)
                {
                    //System.out.println(lineTxt);
                	if(i==-1)
                	{
                		temStrs=lineTxt.split(",");
                		for(j=0;j<temStrs.length;j++) tableHeader.put(temStrs[j], j);
                		i++;
                		continue;
                	}
                	if(i>=startStep&&i<=endStep)
                	{
                		temStrs=lineTxt.split(",");
                		temRes=new ArrayList<>();
                		for(j=0;j<getResults.length;j++)
                			temRes.add(temStrs[tableHeader.get(getResults[j])]);
                		res.add(temRes);
                	}
                	i++;
                	if(i>endStep) break;
                }
                read.close();
             }else{
            	 logger.error("找不到指定的文件:"+outPath);
             }
		} catch (Exception e) {
        logger.error("读取文件内容出错"+e.getMessage());
		}
		return res;
	}
	private void exeCmd(String commandStr, String[] envStr,boolean isOutput) 
	{  
		//commandStr="/home/ljf/postg/reseacher/prism/bin/prism";
//		try {
//			new File("/home/ljf/ah.txt").createNewFile();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		logger.debug(commandStr);
        BufferedReader br = null;  
        try {  
        	//logger.debug(System.getProperty("user.dir"));
        	 Process process = Runtime.getRuntime().exec(commandStr, envStr);//,new File(System.getProperty("user.dir")));
        	 if(isOutput)
        	 {
	        	 BufferedReader strCon = new BufferedReader(new InputStreamReader(process.getInputStream()));
		        String line = null;
		        try
	            {
	            String path="./22";
	            File file=new File(path);
	            if(!file.exists())
	                file.createNewFile();
	            FileOutputStream out=new FileOutputStream(file,false); 
		        while ((line = strCon.readLine()) != null) 
		        {
		        	if("".equals(line.trim())) continue;
		            line+="\r\n";
		            out.write(line.toString().getBytes("utf-8"));
		        }
		        out.flush();
	            out.close();
		        }
		        catch(IOException ex)
	            {
	                logger.error(ex.getStackTrace());
	            }
        	 }
        } catch (Exception e) {  
            logger.error("cmd error:"+commandStr);
            e.printStackTrace();  
        }   
        finally  
        {  
            if (br != null)  
            {  
                try {  
                    br.close();  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
}
