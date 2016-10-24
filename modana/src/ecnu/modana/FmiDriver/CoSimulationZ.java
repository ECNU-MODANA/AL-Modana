package ecnu.modana.FmiDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMIEventInfo;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;

import ecnu.modana.PlotComposer.JLineChart;
import ecnu.modana.Properties.Trace;
import ecnu.modana.model.ModelManager;
import ecnu.modana.util.MyLineChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CoSimulationZ extends FMUDriver {
	Logger logger = Logger.getRootLogger();
	String host="127.0.0.1";
	int port=40000;
	public CoSimulationZ(String host,int port)
	{
		this.host=host;
		this.port=port;
	}
	FMIModelDescription fmiModelDescription;
	Pointer fmiComponent;
	StringBuilder sb=new StringBuilder();
	 public LineChart<Object,Number> simulate(String prismModelPath,String prismModelType,String fmuFileName, double endTime, 
			 double stepSize,boolean enableLogging, char csvSeparator, String outputFileName)
	 {
		 PrismClient prismClient=PrismClient.getInstance();
	    	prismClient.StartServer();
	    	if(!prismClient.Start(host, port))
	    	{
	    		logger.debug("no PrismServer,host:"+host+"port:"+port);
	    		return null;
	    	}
	    	String prismVars=prismClient.OpenModel(prismModelPath);
	    	prismClient.Close();
	    	if(null==prismVars)
	    	{
	    		logger.debug("Model open fail!!!"+prismModelPath);
	    		return null;
	    	}
	    	if("dtmc".equals(prismClient.modelType))
				try {
					return dtmcSimulate(prismModelPath, prismClient.modelType, fmuFileName, endTime, stepSize, enableLogging, csvSeparator, outputFileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	else if("ctmc".equals(prismClient.modelType))
				try {
					return ctmcSimulate(prismModelPath, prismClient.modelType, fmuFileName, endTime, stepSize, enableLogging, csvSeparator, outputFileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	return null;
	 }
	
    private void SetConditionToFmu(List<String>sharedVarsPrism,PrismClient prismClient)
    {
    	for(int i=0;i<sharedVarsPrism.size();i++)
    		if(sharedVarsPrism.get(i).startsWith("con_"))
    		{
    			SetValue(fmiModelDescription,sharedVarsPrism.get(i), fmiComponent, prismClient.GetValue(sharedVarsPrism.get(i)));
    			logger.debug("set con to fmu:"+ prismClient.GetValue(sharedVarsPrism.get(i)));
    		}
    }
    private void FakeSetValueToFmu(List<String>sharedVarsPrism,PrismClient prismClient)
    {
    	for(int i=0;i<sharedVarsPrism.size();i++)
    		if(sharedVarsPrism.get(i).startsWith("out_")) 
    		{
    			//double tv=Double.valueOf(GetValue(fmiModelDescription,"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent).toString());
    			SetValue(fmiModelDescription,"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent, Double.valueOf(GetValue(fmiModelDescription,"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent).toString())*PrismClient.xiaoShuWei);
    			//tv=Double.valueOf(GetValue(fmiModelDescription,"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent).toString());
    			logger.debug("set value to fmu,"+"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length())+":"+prismClient.GetValue(sharedVarsPrism.get(i)));
    			logger.debug("in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length())+" in fmu is:"+GetValue(fmiModelDescription, "in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent));
    		}
    }
	@Override
	public MyLineChart simulate(String fmuFileName, double endTime, double stepSize, boolean enableLogging,
			char csvSeparator, String outputFileName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String[]GetMarkovVariables(String path)
	{
		PrismClient prismClient=PrismClient.getInstance();
		String prismVars=prismClient.OpenModel(path);
		String[]tems=prismVars.split(",");
		String name=path.substring(path.lastIndexOf('/')+1);
		name=name.substring(0,name.lastIndexOf('.')+1);
		for(int i=0;i<tems.length;i++)
			tems[i]=name+tems[i];
		return tems;
	}
	public String[]GetFMUVariables(String fmuPath)
	{
        try {
   		    // Parse the .fmu file.
			fmiModelDescription = FMUFile.parseFMUFile(fmuPath);
	        // Load the shared library.
	        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
	        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);
			 String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
			 String[] tems=fmuVars.split(",");
			 String name=fmuPath.substring(fmuPath.lastIndexOf('/')+1);
			 name=name.substring(0,name.lastIndexOf('.')+1);
//			 for(int i=0;i<tems.length;i++)
//					tems[i]=name+tems[i];
//			 return tems;
			 
			 String res="";
			 for(int i=0;i<tems.length;i++)
					if(!tems[i].startsWith("der(")&&!tems[i].startsWith("_")&&!tems[i].startsWith("temp_1"))
						res+=name+tems[i]+",";
			 if("".equals(res)) return null;
			 return res.substring(0, res.length()-1).split(",");
		} catch (IOException e) {
			logger.error("GetFMUVariables error:"+e.getMessage());
			return null;
		}
	}

	 private Hashtable<String, String> prismToFMUHts,fmuToPrismHts;
	 private void SetValueToPrism(Hashtable<String,String>fmuToPrismHts,PrismClient prismClient)
	 {
		 Enumeration<String> keys = fmuToPrismHts.keys();
		 String fmuStr="",prismStr="";
         while(keys.hasMoreElements())
         {
            fmuStr=keys.nextElement();
            prismStr=fmuToPrismHts.get(fmuStr);
            
            Object tObject=GetValue(fmiModelDescription, fmuStr, fmiComponent);
            if(tObject instanceof Double)
			{
				double td=Double.valueOf(tObject.toString())*PrismClient.xiaoShuWei; //1000
				tObject=(int)Math.floor(td);
			}
            prismClient.SetValue(prismStr,tObject);
        }
    }
	 private void SetValueToFmu(Hashtable<String, String>prismToFMUHts,PrismClient prismClient)
	 {
		 Enumeration<String> keys = fmuToPrismHts.keys();
		 String fmuStr="",prismStr="";
         while(keys.hasMoreElements())
         {
        	prismStr=keys.nextElement();
            fmuStr=fmuToPrismHts.get(prismStr);
            
            SetValue(fmiModelDescription, fmuStr, fmiComponent, prismClient.GetValue(prismStr));
         }
    }
	 public LineChart<Object,Number> dtmcSimulate(String prismModelPath,String prismModelType,String fmuFileName, double endTime, double stepSize,
	            boolean enableLogging, char csvSeparator, String outputFileName)
	            throws Exception 
	    {
		   PrismClient prismClient=PrismClient.getInstance();
	    	prismClient.StartServer();
	    	if(!prismClient.Start(host, port))
	    	{
	    		logger.debug("no PrismServer,host:"+host+"port:"+port);
	    		//return null;
	    	}
	    	String prismVars=prismClient.OpenModel(prismModelPath);
	    	if(null==prismVars)
	    	{
	    		logger.debug("Model open fail!!!"+prismModelPath);
	    		return null;
	    	}
	    	JLineChart myLineChart=null;
	    	
	        // Avoid a warning from FindBugs.
	        FMUDriver._setEnableLogging(enableLogging);

	        // Parse the .fmu file.
	        fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

	        // Load the shared library.
	        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
	        if (enableLogging) {
	            logger.debug("FMUModelExchange: about to load "
	                    + sharedLibrary);
	        }
	        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

	        // The modelName may have spaces in it.
	        _modelIdentifier = fmiModelDescription.modelIdentifier;

	        new File(fmuFileName).toURI().toURL().toString();
	        int numberOfStateEvents = 0;
	        int numberOfStepEvents = 0;
	        int numberOfSteps = 0;
	        int numberOfTimeEvents = 0;

	        // Callbacks
	        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
		        new FMULibrary.FMULogger(), fmiModelDescription.getFMUAllocateMemory(),
	                new FMULibrary.FMUFreeMemory(),
	                new FMULibrary.FMUStepFinished());
	        // Logging tends to cause segfaults because of vararg callbacks.
	        byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;
	        loggingOn = (byte) 0;

	        // Instantiate the model.
	        Function instantiateModelFunction;
	        try {
	            instantiateModelFunction = getFunction("_fmiInstantiateModel");
	            //instantiateModelFunction = getFunction("_fmiInstantiateSlave");
	        } catch (UnsatisfiedLinkError ex) {
	            UnsatisfiedLinkError error = new UnsatisfiedLinkError(
	                    "Could not load " + _modelIdentifier
	                            + "_fmiInstantiateModel()"
	                            + ". This can happen when a co-simulation .fmu "
	                            + "is run in a model exchange context.");
	            error.initCause(ex);
	            throw error;
	        }
	        fmiComponent = (Pointer) instantiateModelFunction.invoke(
	                Pointer.class, new Object[] { _modelIdentifier,
	                        fmiModelDescription.guid, callbacks, loggingOn });
	        if (fmiComponent.equals(Pointer.NULL)) {
	            throw new RuntimeException("Could not instantiate model.");
	        }

	        // Should these be on the heap?
	        final int numberOfStates = fmiModelDescription.numberOfContinuousStates;
	        final int numberOfEventIndicators = fmiModelDescription.numberOfEventIndicators;
	        double[] states = new double[numberOfStates];
	        double[] derivatives = new double[numberOfStates];

	        double[] eventIndicators = null;
	        double[] preEventIndicators = null;
	        boolean[]isEventLastHappen=null,isEventHappen=null;
	        if (numberOfEventIndicators > 0) {
	            eventIndicators = new double[numberOfEventIndicators];
	            preEventIndicators = new double[numberOfEventIndicators];
	            isEventLastHappen=new boolean[numberOfEventIndicators];
	            isEventHappen=new boolean[numberOfEventIndicators];
	        }

	        // Set the start time.
	        double startTime = 0.0;
	        Function setTime = getFunction("_fmiSetTime");
	        invoke(setTime, new Object[] { fmiComponent, startTime },
	                "Could not set time to start time: " + startTime + ": ");

	        // Initialize the model.
	        byte toleranceControlled = 0;
	        FMIEventInfo eventInfo = new FMIEventInfo();
	        invoke("_fmiInitialize", new Object[] { fmiComponent,
	                toleranceControlled, startTime, eventInfo },
	                "Could not initialize model: ");

	        double time = startTime;
	        if (eventInfo.terminateSimulation != 0) {
	            System.out.println("Model terminated during initialization.");
	            endTime = time;
	        }

	        File outputFile = new File(outputFileName);
	        PrintStream file = null;
	        try {
		    // gcj does not have this constructor
	            //file = new PrintStream(outputFile);
	            file = new PrintStream(outputFileName);
	            if (enableLogging) {
	                System.out.println("FMUModelExchange: about to write header");
	            }
	            // Generate header row
	            OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
	                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
	            // Output the initial values.
	            OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
	                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);

	            // Functions used within the while loop, organized
	            // alphabetically.
	            Function completedIntegratorStep = getFunction("_fmiCompletedIntegratorStep");
	            Function eventUpdate = getFunction("_fmiEventUpdate");
	            Function getContinuousStates = getFunction("_fmiGetContinuousStates");
	            Function getDerivatives = getFunction("_fmiGetDerivatives");
	            Function getEventIndicators = getFunction("_fmiGetEventIndicators");
	            Function setContinuousStates = getFunction("_fmiSetContinuousStates");

	            boolean stateEvent = false;

	            byte stepEvent = (byte) 0;
	            
	            List<Object> timeList=new ArrayList<>();
	            List<Number> hNumberList=new ArrayList<Number>();
	            List<Number>vNumberList=new ArrayList<>();
	            List<Number>alterVNumberList=new ArrayList<>();     
	            boolean isEnd=false;
	            double lastTime=-1;
	            // Loop until the time is greater than the end time.
	            while (time < endTime&&!isEnd) 
	            {
	                invoke(getContinuousStates, new Object[] { fmiComponent,
	                        states, numberOfStates },
	                        "Could not get continuous states, time was " + time
	                                + ": ");

	                invoke(getDerivatives, new Object[] { fmiComponent,
	                        derivatives, numberOfStates },
	                        "Could not get derivatives, time was " + time + ": ");

	                // Update time.
	                double stepStartTime = time;
	                time = Math.min(time + stepSize, endTime);
	                boolean timeEvent = eventInfo.upcomingTimeEvent == 1
	                        && eventInfo.nextEventTime < time;
	                if (timeEvent) {
	                    time = eventInfo.nextEventTime;
	                }
	                double dt = time - stepStartTime;
	                invoke(setTime, new Object[] { fmiComponent, time },
	                        "Could not set time, time was " + time + ": ");

	                // Perform a step.
	                for (int i = 0; i < numberOfStates; i++) {
	                    // The forward Euler method.
	                    states[i] += dt * derivatives[i];
	                }
	                int isStop=0;
	                for(isStop=0;isStop<numberOfStates;isStop++)
	                	if(!(Math.abs(derivatives[isStop])<=0.01)) break;
	                if(isStop==numberOfStates) isEnd=true;
	                
	                invoke(setContinuousStates, new Object[] { fmiComponent,
	                        states, numberOfStates },
	                        "Could not set continuous states, time was " + time
	                                + ": ");
	               
	                // Check to see if we have completed the integrator step.
	                ByteByReference stepEventReference = new ByteByReference(
	                        stepEvent);
	                invoke(completedIntegratorStep, new Object[] { fmiComponent,
	                        stepEventReference },
	                        "Could not set complete integrator step, time was "
	                                + time + ": ");
	                
	                // Save the state events.
	                for (int i = 0; i < numberOfEventIndicators; i++) {
	                    preEventIndicators[i] = eventIndicators[i];
	                }

	                // Get the eventIndicators.
	                invoke(getEventIndicators, new Object[] { fmiComponent,
	                        eventIndicators, numberOfEventIndicators },
	                        "Could not set get event indicators, time was " + time
	                                + ": ");

	                stateEvent = Boolean.FALSE;
	                for (int i = 0; i < numberOfEventIndicators; i++) {
	                    stateEvent = stateEvent
	                            || preEventIndicators[i] * eventIndicators[i] < 0;
	                }
	                
	                // Handle Events
	                if (stateEvent || stepEvent != (byte) 0 || timeEvent)
	                {
	                    if (stateEvent) 
	                    {
	                        numberOfStateEvents++;
	                        int i=0;
	                      if(time-lastTime>stepSize*2)
	                      {
	                    	  lastTime=time;
		                      isEventHappen[i]=true;
	                      }
	                    }
	                    if (stepEvent != (byte) 0) {
	                        numberOfStepEvents++;
	                    }
	                    if (timeEvent) {
	                        numberOfTimeEvents++;
	                    }

	                    invoke(eventUpdate, new Object[] { fmiComponent, (byte) 0,
	                            eventInfo },
	                            "Could not set update event, time was " + time
	                                    + ": ");

	                    if (eventInfo.terminateSimulation != (byte) 0) {
	                        System.out.println("Termination requested: " + time);
	                        break;
	                    }

	                    if (eventInfo.stateValuesChanged != (byte) 0
	                            && enableLogging) {
	                        System.out.println("state values changed: " + time);
	                    }
	                    if (eventInfo.stateValueReferencesChanged != (byte) 0
	                            && enableLogging) {
	                        System.out.println("new state variables selected: "
	                                + time);
	                    }
	                    for(int i=0;i<numberOfEventIndicators;i++)
	                		if(isEventHappen[i])
	                		{
	                		  SetValueToPrism(fmuToPrismHts,prismClient);
	                       	  prismClient.DoStep(true);
	                       	  SetValueToFmu(prismToFMUHts,prismClient);
	                		}
	                }
	                for(int i=0;i<numberOfEventIndicators;i++)
	                	if(isEventHappen[i])
	                	{
	                		isEventLastHappen[i]=true;
	                		isEventHappen[i]=false;
	                	}
	                	else isEventLastHappen[i]=false;

	                // Generate a line for this step
	                OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
	                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
	                numberOfSteps++;
	            }
	            invoke("_fmiTerminate", new Object[] { fmiComponent },
	                    "Could not terminate: ");
	        } finally {
	            if (file != null) {
	                file.close();
	            }
		    if (fmiModelDescription != null) {
			fmiModelDescription.dispose();
		    }
	        }
	        
		prismClient.Close();
		return myLineChart.getJLineChart();
	    }
	 public LineChart<Object,Number> ctmcSimulate(String prismModelPath,String prismModelType,String fmuFileName, double endTime, double stepSize,
	            boolean enableLogging, char csvSeparator, String outputFileName)
	            throws Exception 
	    {
	    	PrismClient prismClient=PrismClient.getInstance();
	    	if(!prismClient.Start(host, port))
	    	{
	    		logger.debug("no PrismServer,host:"+host+"port:"+port);
	    		//return null;
	    	}
	    	String prismVars=prismClient.OpenModel(prismModelPath);
	    	if(null==prismVars)
	    	{
	    		logger.debug("Model open fail!!!"+prismModelPath);
	    		return null;
	    	}
	    	JLineChart myLineChart=null;
	    	
	        // Avoid a warning from FindBugs.
	        FMUDriver._setEnableLogging(enableLogging);

	        // Parse the .fmu file.
	        fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

	        // Load the shared library.
	        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
	        if (enableLogging) {
	            System.out.println("FMUModelExchange: about to load "
	                    + sharedLibrary);
	        }
	        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

	        // The modelName may have spaces in it.
	        _modelIdentifier = fmiModelDescription.modelIdentifier;

	        new File(fmuFileName).toURI().toURL().toString();
	        int numberOfStateEvents = 0;
	        int numberOfStepEvents = 0;
	        int numberOfSteps = 0;
	        int numberOfTimeEvents = 0;

	        // Callbacks
	        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
		        new FMULibrary.FMULogger(), fmiModelDescription.getFMUAllocateMemory(),
	                new FMULibrary.FMUFreeMemory(),
	                new FMULibrary.FMUStepFinished());
	        // Logging tends to cause segfaults because of vararg callbacks.
	        byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;
	        loggingOn = (byte) 0;

	        // Instantiate the model.
	        Function instantiateModelFunction;
	        try {
	            instantiateModelFunction = getFunction("_fmiInstantiateModel");
	            //instantiateModelFunction = getFunction("_fmiInstantiateSlave");
	        } catch (UnsatisfiedLinkError ex) {
	            UnsatisfiedLinkError error = new UnsatisfiedLinkError(
	                    "Could not load " + _modelIdentifier
	                            + "_fmiInstantiateModel()"
	                            + ". This can happen when a co-simulation .fmu "
	                            + "is run in a model exchange context.");
	            error.initCause(ex);
	            throw error;
	        }
	        fmiComponent = (Pointer) instantiateModelFunction.invoke(
	                Pointer.class, new Object[] { _modelIdentifier,
	                        fmiModelDescription.guid, callbacks, loggingOn });
	        if (fmiComponent.equals(Pointer.NULL)) {
	            throw new RuntimeException("Could not instantiate model.");
	        }

	        // Should these be on the heap?
	        final int numberOfStates = fmiModelDescription.numberOfContinuousStates;
	        final int numberOfEventIndicators = fmiModelDescription.numberOfEventIndicators;
	        double[] states = new double[numberOfStates];
	        double[] derivatives = new double[numberOfStates];

	        double[] eventIndicators = null;
	        double[] preEventIndicators = null;
	        boolean[]isEventLastHappen=null,isEventHappen=null;
	        if (numberOfEventIndicators > 0) {
	            eventIndicators = new double[numberOfEventIndicators];
	            preEventIndicators = new double[numberOfEventIndicators];
	            isEventLastHappen=new boolean[numberOfEventIndicators];
	            isEventHappen=new boolean[numberOfEventIndicators];
	        }

	        // Set the start time.
	        double startTime = 0.0;
	        Function setTime = getFunction("_fmiSetTime");
	        invoke(setTime, new Object[] { fmiComponent, startTime },
	                "Could not set time to start time: " + startTime + ": ");

	        // Initialize the model.
	        byte toleranceControlled = 0;
	        FMIEventInfo eventInfo = new FMIEventInfo();
	        invoke("_fmiInitialize", new Object[] { fmiComponent,
	                toleranceControlled, startTime, eventInfo },
	                "Could not initialize model: ");

	        double time = startTime;
	        if (eventInfo.terminateSimulation != 0) {
	            System.out.println("Model terminated during initialization.");
	            endTime = time;
	        }

	        File outputFile = new File(outputFileName);
	        PrintStream file = null;
	        try {
	            file = new PrintStream(outputFileName);
	            if (enableLogging) {
	                System.out.println("FMUModelExchange: about to write header");
	            }
	            // Generate header row
	            OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
	                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
	            // Output the initial values.
	            OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
	                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);

	            // Functions used within the while loop, organized alphabetically.
	            Function completedIntegratorStep = getFunction("_fmiCompletedIntegratorStep");
	            Function eventUpdate = getFunction("_fmiEventUpdate");
	            Function getContinuousStates = getFunction("_fmiGetContinuousStates");
	            Function getDerivatives = getFunction("_fmiGetDerivatives");
	            Function getEventIndicators = getFunction("_fmiGetEventIndicators");
	            Function setContinuousStates = getFunction("_fmiSetContinuousStates");

	            boolean stateEvent = false;

	            byte stepEvent = (byte) 0;
	            
	            List<Object> timeList=new ArrayList<>();
	            List<Number> hNumberList=new ArrayList<Number>();
	            List<Number>vNumberList=new ArrayList<>();
	            List<Number>alterVNumberList=new ArrayList<>();     
	            boolean isEnd=false;
	            double lastTime=-1;
	            PriorityQueue<Double>eventQueue=new PriorityQueue<>();
	            double waitEventTime=-1;
	            // Loop until the time is greater than the end time.
	            while (time < endTime&&!isEnd) 
	            {
	            	//如果当前没有事件时间点，则由CTMC产生一个
	            	if(eventQueue.size()==0)
	            	{
	            		//while(true)
	            		{
		            		prismClient.DoStep(false);
		            		System.err.println("time:"+prismClient.GetTime());
		            		double t=prismClient.GetTime();
		            		if(t<time) t+=waitEventTime;
		            		{
			            		eventQueue.add(t);
			            		waitEventTime=t;
		            		}
	            		}
	            	}
	            	if(time>=waitEventTime&&(time-stepSize)<=waitEventTime)
	        		{
	            		SetValueToFmu(prismToFMUHts, prismClient);
	            		invoke(getContinuousStates, new Object[] { fmiComponent,
	                            states, numberOfStates },
	                            "Could not get continuous states, time was " + time
	                                    + ": ");

	                    invoke(getDerivatives, new Object[] { fmiComponent,
	                            derivatives, numberOfStates },
	                            "Could not get derivatives, time was " + time + ": ");

	                    // Update time.
	                    double stepStartTime = time;
	                    time = Math.min(time + stepSize, endTime);
	                    boolean timeEvent = eventInfo.upcomingTimeEvent == 1
	                            && eventInfo.nextEventTime < time;
	                    if (timeEvent) {
	                        time = eventInfo.nextEventTime;
	                    }
	                    double dt = time - stepStartTime;
	                    invoke(setTime, new Object[] { fmiComponent, time },
	                            "Could not set time, time was " + time + ": ");

	                    // Perform a step.
	                    for (int i = 0; i < numberOfStates; i++) {
	                        // The forward Euler method.
	                        states[i] += dt * derivatives[i];
	                    }
	                    
	                    invoke(setContinuousStates, new Object[] { fmiComponent,
	                            states, numberOfStates },
	                            "Could not set continuous states, time was " + time
	                                    + ": ");
	                   
	                    ByteByReference stepEventReference = new ByteByReference(
	                            stepEvent);
	                    invoke(completedIntegratorStep, new Object[] { fmiComponent,
	                            stepEventReference },
	                            "Could not set complete integrator step, time was "
	                                    + time + ": ");
	                    
	                    // Save the state events.
	                    for (int i = 0; i < numberOfEventIndicators; i++) {
	                        preEventIndicators[i] = eventIndicators[i];
	                    }

	                    // Get the eventIndicators.
	                    invoke(getEventIndicators, new Object[] { fmiComponent,
	                            eventIndicators, numberOfEventIndicators },
	                            "Could not set get event indicators, time was " + time
	                                    + ": ");

	                    stateEvent = Boolean.FALSE;
	                    for (int i = 0; i < numberOfEventIndicators; i++) {
	                        stateEvent = stateEvent
	                                || preEventIndicators[i] * eventIndicators[i] < 0;
	                    }
	                    
	                    // Handle Events
	                    if (stateEvent || stepEvent != (byte) 0 || timeEvent)
	                    {
	                        if (stateEvent) 
	                        {
	                            numberOfStateEvents++;
	                            int i=0;
	                          {
	                        	  lastTime=time;
	    	                      isEventHappen[i]=true;
	                          }
	                        }
	                        if (stepEvent != (byte) 0) {
	                            numberOfStepEvents++;
	                            if (enableLogging) {
	                                System.out.println("step event at " + time);
	                            }
	                        }
	                        if (timeEvent) {
	                            numberOfTimeEvents++;
	                            if (enableLogging) {
	                                System.out.println("Time event at " + time);
	                            }
	                        }

	                        invoke(eventUpdate, new Object[] { fmiComponent, (byte) 0,
	                                eventInfo },
	                                "Could not set update event, time was " + time
	                                        + ": ");

	                        if (eventInfo.terminateSimulation != (byte) 0) {
	                            System.out.println("Termination requested: " + time);
	                            break;
	                        }

	                        if (eventInfo.stateValuesChanged != (byte) 0
	                                && enableLogging) {
	                            System.out.println("state values changed: " + time);
	                        }
	                        if (eventInfo.stateValueReferencesChanged != (byte) 0
	                                && enableLogging) {
	                            System.out.println("new state variables selected: "
	                                    + time);
	                        }
	                        for(int i=0;i<numberOfEventIndicators;i++)
	                    		if(isEventHappen[i])
	                    		{
	                  			  SetValueToPrism(fmuToPrismHts,prismClient);
	                  			  prismClient.DoStep(false);
	                  			  SetValueToFmu(prismToFMUHts,prismClient);
	                    		}
	                    }
	                   
	            		logger.debug("in_v:"+GetValue(fmiModelDescription, "in_v", fmiComponent)+" out_v:"+GetValue(fmiModelDescription, "out_v", fmiComponent));
	        			eventQueue.remove(waitEventTime);
	                	if(eventQueue.size()>0)
	                		waitEventTime=eventQueue.peek();
	        		}
	                invoke(getContinuousStates, new Object[] { fmiComponent,
	                        states, numberOfStates },
	                        "Could not get continuous states, time was " + time
	                                + ": ");

	                invoke(getDerivatives, new Object[] { fmiComponent,
	                        derivatives, numberOfStates },
	                        "Could not get derivatives, time was " + time + ": ");

	                // Update time.
	                double stepStartTime = time;
	                time = Math.min(time + stepSize, endTime);
	                boolean timeEvent = eventInfo.upcomingTimeEvent == 1
	                        && eventInfo.nextEventTime < time;
	                if (timeEvent) {
	                    time = eventInfo.nextEventTime;
	                }
	                double dt = time - stepStartTime;
	                invoke(setTime, new Object[] { fmiComponent, time },
	                        "Could not set time, time was " + time + ": ");

	                // Perform a step.
	                for (int i = 0; i < numberOfStates; i++) {
	                    // The forward Euler method.
	                    states[i] += dt * derivatives[i];
	                }
	                
	                invoke(setContinuousStates, new Object[] { fmiComponent,
	                        states, numberOfStates },
	                        "Could not set continuous states, time was " + time
	                                + ": ");
	               
	                ByteByReference stepEventReference = new ByteByReference(
	                        stepEvent);
	                invoke(completedIntegratorStep, new Object[] { fmiComponent,
	                        stepEventReference },
	                        "Could not set complete integrator step, time was "
	                                + time + ": ");
	                
	                // Save the state events.
	                for (int i = 0; i < numberOfEventIndicators; i++) {
	                    preEventIndicators[i] = eventIndicators[i];
	                }

	                // Get the eventIndicators.
	                invoke(getEventIndicators, new Object[] { fmiComponent,
	                        eventIndicators, numberOfEventIndicators },
	                        "Could not set get event indicators, time was " + time
	                                + ": ");

	                stateEvent = Boolean.FALSE;
	                for (int i = 0; i < numberOfEventIndicators; i++) {
	                    stateEvent = stateEvent
	                            || preEventIndicators[i] * eventIndicators[i] < 0;
	                }

	                // Generate a line for this step
	                OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
	                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
	                numberOfSteps++;
	            }
	            invoke("_fmiTerminate", new Object[] { fmiComponent },
	                    "Could not terminate: ");
	        } finally {
	            if (file != null) {
	                file.close();
	            }
		    if (fmiModelDescription != null) {
			fmiModelDescription.dispose();
		    }
	        }
	        
		prismClient.Close();
		return myLineChart.getJLineChart();
	    }
}
