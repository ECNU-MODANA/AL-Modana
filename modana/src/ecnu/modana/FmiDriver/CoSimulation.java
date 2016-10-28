package ecnu.modana.FmiDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
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
import ecnu.modana.alsmc.main.State;
import ecnu.modana.model.ModelManager;
import ecnu.modana.util.MyLineChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CoSimulation extends FMUDriver {
	Logger logger = Logger.getRootLogger();
	String host="127.0.0.1";
	int port=40000;
	public CoSimulation(String host,int port)
	{
		this.host=host;
		this.port=port;
	}
	FMIModelDescription fmiModelDescription;
	Pointer fmiComponent;
	StringBuilder sb=new StringBuilder();
	 public LineChart<Object,Number> simulate(String prismModelPath,String prismModelType,String fmuFileName, double endTime, 
			 double stepSize,boolean enableLogging, char csvSeparator,ArrayList<State> res,LinkedHashSet<String> needsVariables)
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
					return dtmcSimulate(prismModelPath, prismClient.modelType, fmuFileName, endTime, stepSize, enableLogging, csvSeparator,res,needsVariables);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	else if("ctmc".equals(prismClient.modelType))
				try {
					return ctmcSimulate(prismModelPath, prismClient.modelType, fmuFileName, endTime, stepSize, enableLogging, csvSeparator, "");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	return null;
	 }
	 public Trace simulateTrace(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, 
			 double stepSize,boolean enableLogging, char csvSeparator, String outputFileName)
	 {
	    	if("dtmc".equals(prismModelType))
				try {
					return dtmcSimulate(prismModelPath, prismModelType, fmiModelDescription, endTime, stepSize, enableLogging, outputFileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	else if("ctmc".equals(prismModelType))
				try {
					//return ctmcSimulate(prismModelPath, prismClient.modelType, fmuFileName, endTime, stepSize, enableLogging, csvSeparator, outputFileName);
					return ctmcSimulate(prismModelPath, prismModelType, fmiModelDescription, endTime, stepSize, enableLogging, outputFileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	return null;
	 }
	 public Trace simulateTradition(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, 
			 double stepSize,boolean enableLogging, char csvSeparator, String outputFileName)
	 {
	    	if("dtmc".equals(prismModelType))
				try {
					return dtmcSimulateTradition(prismModelPath, prismModelType, fmiModelDescription, endTime, stepSize, enableLogging, outputFileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	else if("ctmc".equals(prismModelType))
				try {
					return ctmcSimulateTradition(prismModelPath, prismModelType, fmiModelDescription, endTime, stepSize, enableLogging, outputFileName);
					//return null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	return null;
	 }
	 
	 
   public LineChart<Object,Number> dtmcSimulate(String prismModelPath,String prismModelType,String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, char csvSeparator,ArrayList<State> res,LinkedHashSet<String> needsVariables)
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

        //String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
        
        String[]prismS=prismVars.split(",");
        String[]fmuS=GetFMUVariables(fmuFileName);//fmuVars.split(",");
        for(int i=0;i<fmuS.length;i++){
        	fmuS[i]=fmuS[i].substring(fmuS[i].indexOf('.')+1);
        }
        List<String>sharedVarsPrism=new ArrayList<>();
        if(null==needsVariables){
        	needsVariables=new LinkedHashSet<String>();
        	for(int i=0;i<fmuS.length;i++)
        		needsVariables.add("fmu."+fmuS[i]);
        	for(int i=0;i<prismS.length;i++)
        		needsVariables.add("prism."+prismS[i]);
        }
        for(int i=0;i<prismS.length;i++)
        {
        	if(prismS[i].startsWith("in_"))
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("out_"))
        			{
        				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
        	}
        	else if (prismS[i].startsWith("out_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("in_"))
        			{
        				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
			}
        	else if (prismS[i].startsWith("con_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].equals(prismS[i]))
        				sharedVarsPrism.add(prismS[i]);
			}
        }
        ModelManager.getInstance().logger.debug(sharedVarsPrism);
        HashSet<String> twoModelsVariables=GetFmuVariables(fmuFileName);
        ArrayList<Object> values=new ArrayList<>();
        State state;
        String[] markovValues;
        try {
	    // gcj does not have this constructor
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to write header");
            }
            // Generate header row
            values=new ArrayList<>();
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,fmiComponent, startTime, true, needsVariables, values);
//            //markovValues=prismClient.GetVariables().split(",");
//            for(int i=0;i<prismS.length;i++){
//            	if(needsVariables.contains(prismS[i]))
//            		values.add("prism."+prismS[i]);
//            }
            for(String str:needsVariables) values.add(str);
            state=new State();
            state.SetTime(-1);
            state.SetValues(values);
            if(res!=null) res.add(state);
            
            // Output the initial values.
            values=new ArrayList<>();
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,fmiComponent, startTime, false, needsVariables, values);
            markovValues=prismClient.GetAllValues().split(",");
            for(int i=0;i<markovValues.length;i++){
            	if(needsVariables.contains("prism."+prismS[i]))
            		values.add(markovValues[i]);
            }
            state=new State();
            state.SetTime(startTime);
            state.SetValues(values);
            if(res!=null) res.add(state);

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
                // Pass stepEvent in by reference. See
                // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                        //if (enableLogging) 
                        {
                            for (i = 0; i < numberOfEventIndicators; i++)
                            {
                                System.out.println("state event "
                                     + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
                                                        : "-/-")
                                                + " eventIndicator[" + i+ "], time: " + time);
                            }
                        }
                      i=0;
                      //if(!isEventLastHappen[i])
                      if(time-lastTime>stepSize*2)
                      {
                    	  lastTime=time;
	                      isEventHappen[i]=true;
	                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
                		  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//                			if (ModelManager.getInstance().logger.isDebugEnabled()) {
//                				sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//							}
//                		  
                       	  //prismClient.NewPath();
                		  SetValueToPrism(sharedVarsPrism,prismClient);
                       	  //logger.debug(prismClient.CurValues());
                       	  prismClient.DoStep(true);
                       	  //prismClient.DoStep(false);
                       	  SetValueToFmu(sharedVarsPrism,prismClient);
//                		  sb.append(GetValue(fmiModelDescription, "out_v", fmiComponent)+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" "+states[1]+"\n");
//                		  states[1]=Double.valueOf(prismClient.GetValue("out_v"));
//    	                      double v=(double)GetValue(fmiModelDescription, "v", fmiComponent);
////    	                      String tString=(String) new PrismWrapper("", "").simulate("./fmu.pm","-simpath 10,sep=comma","./test.txt" , 1, 1, "v").get(0).get(0);
//                       	  String tString=prismClient.DoStep(false);
//    	                      double tv=Double.parseDouble(prismClient.GetValue("v"));
//    	                      SetValue(fmiModelDescription, "v", fmiComponent, v*tv*1.0/0.7);
//    	                      isEventLastHappen[i]=true;
//    	                      prismClient.DoStep(false);
                       	  //isEventHappen[i]=false;
                       	  //logger.debug(prismClient.GetValue("out_v"));
//                          if(Math.abs(Double.valueOf(prismClient.GetValue("out_v"))/PrismClient.xiaoShuWei)<=2*stepSize*10)
//                        	  isEnd=true;
                		}
                }
                for(int i=0;i<numberOfEventIndicators;i++)
                	if(isEventHappen[i])
                	{
                		isEventLastHappen[i]=true;
                		isEventHappen[i]=false;
                	}
                	else isEventLastHappen[i]=false;
                timeList.add(time);
                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "in_v", vNumberList);

                // Generate a line for this step
                if(null!=res){
	                state=new State();
	                state.SetTime(time);
	                values=new ArrayList<>();
	                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,fmiComponent, time, false, needsVariables, values);
	                markovValues=prismClient.GetAllValues().split(",");
	                for(int i=0;i<markovValues.length;i++){
	                	if(needsVariables.contains("prism."+prismS[i]))
	                		values.add(markovValues[i]);
	                }
	                state.SetValues(values);
	                res.add(state);
                }
                numberOfSteps++;
            }
            invoke("_fmiTerminate", new Object[] { fmiComponent },
                    "Could not terminate: ");
            myLineChart=new JLineChart();
            myLineChart.SetX(timeList, "Time");
            myLineChart.SetY(hNumberList, "h");
            myLineChart.SetY(vNumberList, "v");
        } finally {
		    if (fmiModelDescription != null) {
			fmiModelDescription.dispose();
		    }
        }
        
        System.out.println("Simulation from " + startTime + " to " + endTime
                + " was successful");
        System.out.println("  steps: " + numberOfSteps);
        System.out.println("  step size: " + stepSize);
        System.out.println("  stateEvents: " + numberOfStateEvents);
        System.out.println("  stepEvents: " + numberOfStepEvents);
        System.out.println("  timeEvents: " + numberOfTimeEvents);
	System.out.flush();
	prismClient.Close();
	//System.err.println(sb);
//	prismClient.EndServer();
	return myLineChart.getJLineChart();
    }
   public Trace dtmcSimulate(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, double stepSize,
           boolean enableLogging, String outputFileName)
           throws Exception 
   {
	PrismClient prismClient=PrismClient.getInstance();
   	//System.err.println("geted:"+prismClient.Id);
   	if(!prismClient.Start(host, port))
   	{
   		logger.error("no PrismServer,host:"+host+"port:"+port);
   		//return null;
   	}
   	String prismVars=prismClient.OpenModel(prismModelPath);
   	if(null==prismVars)
   	{
   		logger.error("Model open fail!!!"+prismModelPath);
   		return null;
   	}
   	Trace trace=new Trace();
   	this.fmiModelDescription=fmiModelDescription;
       // Avoid a warning from FindBugs.
       FMUDriver._setEnableLogging(enableLogging);

       // Parse the .fmu file.
       //fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

       // Load the shared library.
       String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
       if (enableLogging) {
           logger.debug("FMUModelExchange: about to load "
                   + sharedLibrary);
       }
       _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

       // The modelName may have spaces in it.
       _modelIdentifier = fmiModelDescription.modelIdentifier;

//       String fmuFileName="./MyBouncingBall.fmu";
//       new File(fmuFileName).toURI().toURL().toString();
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

       String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
       
       String[]prismS=prismVars.split(",");
       String[]fmuS=fmuVars.split(",");
       List<String>sharedVarsPrism=new ArrayList<>();
       for(int i=0;i<prismS.length;i++)
       {
       	if(prismS[i].startsWith("in_"))
       	{
       		for (int j = 0; j < fmuS.length; j++) 
       			if(fmuS[j].startsWith("out_"))
       			{
       				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
       					sharedVarsPrism.add(prismS[i]);
       			}
       	}
       	else if (prismS[i].startsWith("out_")) 
       	{
       		for (int j = 0; j < fmuS.length; j++) 
       			if(fmuS[j].startsWith("in_"))
       			{
       				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
       					sharedVarsPrism.add(prismS[i]);
       			}
			}
       	else if (prismS[i].startsWith("con_")) 
       	{
       		for (int j = 0; j < fmuS.length; j++) 
       			if(fmuS[j].equals(prismS[i]))
       				sharedVarsPrism.add(prismS[i]);
			}
       }
//       System.err.println(sharedVarsPrism);
//       trace.AddNames("time,"+fmuVars);
       trace.AddNames("time,"+fmuVars+","+prismVars);
//       System.err.println("time,"+fmuVars);
       ModelManager.getInstance().logger.debug(sharedVarsPrism);
       //File outputFile = new File(outputFileName);
       //PrintStream file = null;
       try {
	    // gcj does not have this constructor
           //file = new PrintStream(outputFile);
           //file = new PrintStream(outputFileName);
           if (enableLogging) {
               System.out.println("FMUModelExchange: about to write header");
           }
           // Generate header row
//           OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                   fmiComponent, startTime, file, ',', Boolean.TRUE);
//           // Output the initial values.
//           OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                   fmiComponent, startTime, file, ',', Boolean.FALSE);

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
           
//           MyLineChart myLineChart=new MyLineChart();
//           List<Object> timeList=new ArrayList<>();
//           List<Number> hNumberList=new ArrayList<Number>();
//           List<Number>vNumberList=new ArrayList<>();
//           List<Number>alterVNumberList=new ArrayList<>();     
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
//               int isStop=0;
//               for(isStop=0;isStop<numberOfStates;isStop++)
//               	if(!(Math.abs(derivatives[isStop])<=0.01)) break;
//               if(isStop==numberOfStates) isEnd=true;
               
               invoke(setContinuousStates, new Object[] { fmiComponent,
                       states, numberOfStates },
                       "Could not set continuous states, time was " + time
                               + ": ");
              
               // Check to see if we have completed the integrator step.
               // Pass stepEvent in by reference. See
               // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                       //if (enableLogging) 
                       {
                           for (i = 0; i < numberOfEventIndicators; i++)
                           {
                               logger.debug("state event "
                                    + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
                                                       : "-/-")
                                               + " eventIndicator[" + i+ "], time: " + time);
                           }
                       }
                     i=0;
                     //if(!isEventLastHappen[i])
                     if(time-lastTime>stepSize*2)
                     {
                   	  lastTime=time;
	                      isEventHappen[i]=true;
	                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
               		  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//               			if (ModelManager.getInstance().logger.isDebugEnabled()) {
//               				sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//               			}
                      	  //prismClient.NewPath();
               		      SetValueToPrism(sharedVarsPrism,prismClient);
                      	  //logger.debug(prismClient.CurValues());
                      	  //prismClient.DoStep(true);
               		      prismClient.DoStep(false);
               		      trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent)+","+prismClient.CurValues());
               		      prismClient.DoStep(false);
                      	  //prismClient.DoStep(false);
                      	  SetValueToFmu(sharedVarsPrism,prismClient);
               		  //sb.append(GetValue(fmiModelDescription, "out_v", fmiComponent)+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" "+states[1]+"\n");
               		  //states[1]=Double.valueOf(prismClient.GetValue("out_v"));
//   	                      double v=(double)GetValue(fmiModelDescription, "v", fmiComponent);
////   	                      String tString=(String) new PrismWrapper("", "").simulate("./fmu.pm","-simpath 10,sep=comma","./test.txt" , 1, 1, "v").get(0).get(0);
//                      	  String tString=prismClient.DoStep(false);
//   	                      double tv=Double.parseDouble(prismClient.GetValue("v"));
//   	                      SetValue(fmiModelDescription, "v", fmiComponent, v*tv*1.0/0.7);
//   	                      isEventLastHappen[i]=true;
//   	                      prismClient.DoStep(false);
                      	  //isEventHappen[i]=false;
                      	  //logger.debug(prismClient.GetValue("out_v"));
//                         if(Math.abs(Double.valueOf(prismClient.GetValue("out_v"))/PrismClient.xiaoShuWei)<=2*stepSize*10)
//                       	  isEnd=true;
               		}
               }
               for(int i=0;i<numberOfEventIndicators;i++)
               	if(isEventHappen[i])
               	{
               		isEventLastHappen[i]=true;
               		isEventHappen[i]=false;
               	}
               	else isEventLastHappen[i]=false;
//               timeList.add(time);
//               OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
//               OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "in_v", vNumberList);

               // Generate a line for this step
//               OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                       fmiComponent, time, file, ',', Boolean.FALSE);
               //trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent));
               trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent)+","+prismClient.CurValues());
               numberOfSteps++;
           }
           invoke("_fmiTerminate", new Object[] { fmiComponent },
                   "Could not terminate: ");
       } finally {
//           if (file != null) {
//               file.close();
//           }
	    if (fmiModelDescription != null) {
		fmiModelDescription.dispose();
	    }
       }
       
       logger.debug("Simulation from " + startTime + " to " + endTime
               + " was successful");
       logger.debug("  steps: " + numberOfSteps);
       logger.debug("  step size: " + stepSize);
       logger.debug("  stateEvents: " + numberOfStateEvents);
       logger.debug("  stepEvents: " + numberOfStepEvents);
       logger.debug("  timeEvents: " + numberOfTimeEvents);
	System.out.flush();
	prismClient.Close();
	//System.err.println(sb);
//	prismClient.EndServer();
	return trace;
   }
   public Trace dtmcSimulateTradition(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, double stepSize,
           boolean enableLogging, String outputFileName)
           throws Exception 
   {
	   PrismClient prismClient=PrismClient.getInstance();
	   	//System.err.println("geted:"+prismClient.Id);
	   	if(!prismClient.Start(host, port))
	   	{
	   		logger.error("no PrismServer,host:"+host+"port:"+port);
	   		//return null;
	   	}
	   	String prismVars=prismClient.OpenModel(prismModelPath);
	   	if(null==prismVars)
	   	{
	   		logger.error("Model open fail!!!"+prismModelPath);
	   		return null;
	   	}
	   	Trace trace=new Trace();
	   	this.fmiModelDescription=fmiModelDescription;
	       // Avoid a warning from FindBugs.
	       FMUDriver._setEnableLogging(enableLogging);

	       // Parse the .fmu file.
	       //fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

	       // Load the shared library.
	       String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
	       if (enableLogging) {
	           logger.debug("FMUModelExchange: about to load "
	                   + sharedLibrary);
	       }
	       _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

	       // The modelName may have spaces in it.
	       _modelIdentifier = fmiModelDescription.modelIdentifier;

//	       String fmuFileName="./MyBouncingBall.fmu";
//	       new File(fmuFileName).toURI().toURL().toString();
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

	       String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
	       
	       String[]prismS=prismVars.split(",");
	       String[]fmuS=fmuVars.split(",");
	       List<String>sharedVarsPrism=new ArrayList<>();
	       for(int i=0;i<prismS.length;i++)
	       {
	       	if(prismS[i].startsWith("in_"))
	       	{
	       		for (int j = 0; j < fmuS.length; j++) 
	       			if(fmuS[j].startsWith("out_"))
	       			{
	       				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
	       					sharedVarsPrism.add(prismS[i]);
	       			}
	       	}
	       	else if (prismS[i].startsWith("out_")) 
	       	{
	       		for (int j = 0; j < fmuS.length; j++) 
	       			if(fmuS[j].startsWith("in_"))
	       			{
	       				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
	       					sharedVarsPrism.add(prismS[i]);
	       			}
				}
	       	else if (prismS[i].startsWith("con_")) 
	       	{
	       		for (int j = 0; j < fmuS.length; j++) 
	       			if(fmuS[j].equals(prismS[i]))
	       				sharedVarsPrism.add(prismS[i]);
				}
	       }
//	       System.err.println(sharedVarsPrism);
//	       trace.AddNames("time,"+fmuVars);
	       trace.AddNames("time,"+fmuVars+","+prismVars);
//	       System.err.println("time,"+fmuVars);
	       ModelManager.getInstance().logger.debug(sharedVarsPrism);
//	       File outputFile = new File(outputFileName);
//	       PrintStream file = null;
	       try {
		    // gcj does not have this constructor
	           //file = new PrintStream(outputFile);
	           //file = new PrintStream(outputFileName);
	           if (enableLogging) {
	               System.out.println("FMUModelExchange: about to write header");
	           }
//	           // Generate header row
//	           OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//	                   fmiComponent, startTime, file, ',', Boolean.TRUE);
//	           // Output the initial values.
//	           OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//	                   fmiComponent, startTime, file, ',', Boolean.FALSE);

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
	           
//	           MyLineChart myLineChart=new MyLineChart();
//	           List<Object> timeList=new ArrayList<>();
//	           List<Number> hNumberList=new ArrayList<Number>();
//	           List<Number>vNumberList=new ArrayList<>();
//	           List<Number>alterVNumberList=new ArrayList<>();     
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
//	               int isStop=0;
//	               for(isStop=0;isStop<numberOfStates;isStop++)
//	               	if(!(Math.abs(derivatives[isStop])<=0.01)) break;
//	               if(isStop==numberOfStates) isEnd=true;
	               
	               invoke(setContinuousStates, new Object[] { fmiComponent,
	                       states, numberOfStates },
	                       "Could not set continuous states, time was " + time
	                               + ": ");
	              
	               // Check to see if we have completed the integrator step.
	               // Pass stepEvent in by reference. See
	               // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
	                       //if (enableLogging) 
	                       {
	                           for (i = 0; i < numberOfEventIndicators; i++)
	                           {
	                               logger.debug("state event "
	                                    + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
	                                                       : "-/-")
	                                               + " eventIndicator[" + i+ "], time: " + time);
	                           }
	                       }
	                     i=0;
	                     //if(!isEventLastHappen[i])
	                     if(time-lastTime>stepSize*2)
	                     {
	                   	  lastTime=time;
		                      isEventHappen[i]=true;
		                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
	               		  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//	               			if (ModelManager.getInstance().logger.isDebugEnabled()) {
//	               				sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//	               			}
	                      	  //prismClient.NewPath();
	               		  SetValueToPrism(sharedVarsPrism,prismClient);
	                      	  //logger.debug(prismClient.CurValues());
	                      	  //prismClient.DoStep(true);
	               	          prismClient.DoStep(false);
	               		      trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent)+","+prismClient.CurValues());
	               		      prismClient.DoStep(false);
	                      	  SetValueToFmu(sharedVarsPrism,prismClient);
	               		  //sb.append(GetValue(fmiModelDescription, "out_v", fmiComponent)+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" "+states[1]+"\n");
	               		  //states[1]=Double.valueOf(prismClient.GetValue("out_v"));
//	   	                      double v=(double)GetValue(fmiModelDescription, "v", fmiComponent);
////	   	                      String tString=(String) new PrismWrapper("", "").simulate("./fmu.pm","-simpath 10,sep=comma","./test.txt" , 1, 1, "v").get(0).get(0);
//	                      	  String tString=prismClient.DoStep(false);
//	   	                      double tv=Double.parseDouble(prismClient.GetValue("v"));
//	   	                      SetValue(fmiModelDescription, "v", fmiComponent, v*tv*1.0/0.7);
//	   	                      isEventLastHappen[i]=true;
//	   	                      prismClient.DoStep(false);
	                      	  //isEventHappen[i]=false;
	                      	  //logger.debug(prismClient.GetValue("out_v"));
//	                         if(Math.abs(Double.valueOf(prismClient.GetValue("out_v"))/PrismClient.xiaoShuWei)<=2*stepSize*10)
//	                       	  isEnd=true;
	               		}
	               }
	               for(int i=0;i<numberOfEventIndicators;i++)
	               	if(isEventHappen[i])
	               	{
	               		isEventLastHappen[i]=true;
	               		isEventHappen[i]=false;
	               	}
	               	else isEventLastHappen[i]=false;
	               SetValueToPrism(sharedVarsPrism,prismClient);
               	  //logger.debug(prismClient.CurValues());
//	               System.err.println(prismClient.GetValue("in_v"));
               	  prismClient.DoStep(true);
               	  //FakeSetValueToFmu(sharedVarsPrism,prismClient);
               	  //SetValueToPrism(sharedVarsPrism, prismClient);
//               	  prismClient.SetValue("out_v", Double.valueOf(prismClient.GetValue("in_v"))*10);
//               	  System.err.println(prismClient.GetValue("in_v"));
               	  FakeSetValueToFmu(sharedVarsPrism, prismClient);
               	  //SetValueToFmu(sharedVarsPrism, prismClient);
//	               timeList.add(time);
//	               OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
//	               OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "in_v", vNumberList);

	               // Generate a line for this step
//	               OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//	                       fmiComponent, time, file, ',', Boolean.FALSE);
//	               trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent));
               	 trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent)+","+prismClient.CurValues());
	               numberOfSteps++;
	           }
	           invoke("_fmiTerminate", new Object[] { fmiComponent },
	                   "Could not terminate: ");
	       } finally {
//	           if (file != null) {
//	               file.close();
//	           }
		    if (fmiModelDescription != null) {
			fmiModelDescription.dispose();
		    }
	       }
	       
	       logger.debug("Simulation from " + startTime + " to " + endTime
	               + " was successful");
	       logger.debug("  steps: " + numberOfSteps);
	       logger.debug("  step size: " + stepSize);
	       logger.debug("  stateEvents: " + numberOfStateEvents);
	       logger.debug("  stepEvents: " + numberOfStepEvents);
	       logger.debug("  timeEvents: " + numberOfTimeEvents);
		System.out.flush();
		prismClient.Close();
		//System.err.println(sb);
//		prismClient.EndServer();
		return trace;
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

        //String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
        
        String[]prismS=prismVars.split(",");
        String[]fmuS=GetFMUVariables(fmuFileName);// fmuVars.split(",");
        List<String>sharedVarsPrism=new ArrayList<>();
        for(int i=0;i<prismS.length;i++)
        {
        	if(prismS[i].startsWith("in_"))
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("out_"))
        			{
        				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
        	}
        	else if (prismS[i].startsWith("out_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("in_"))
        			{
        				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
			}
        	else if (prismS[i].startsWith("con_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].equals(prismS[i]))
        				sharedVarsPrism.add(prismS[i]);
			}
        }
        ModelManager.getInstance().logger.debug(sharedVarsPrism);
        File outputFile = new File(outputFileName);
        PrintStream file = null;
        HashSet<String> twoModelsVariables=GetFmuVariables(fmuFileName);
        try {
	    // gcj does not have this constructor
            //file = new PrintStream(outputFile);
            file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to write header");
            }
            // Generate header row
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE,twoModelsVariables);
//            // Output the initial values.
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE,twoModelsVariables);

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
            PriorityQueue<Double>eventQueue=new PriorityQueue<>();
            double waitEventTime=-1;
            // Loop until the time is greater than the end time.
            while (time < endTime&&!isEnd) 
            {
//            	for(;waitEventTime>0&&waitEventTime<time;)
//            	{
//            		eventQueue.remove(waitEventTime);
//        			if(eventQueue.size()>0) waitEventTime=eventQueue.peek();
//        			else break;
//            	}
            	if(eventQueue.size()==0)
            	{
            		//while(true)
            		{
	            		prismClient.DoStep(false);
	            		System.err.println("time:"+prismClient.GetTime());
	            		double t=prismClient.GetTime();
	            		if(t<time) t+=waitEventTime;
	            		//if(t>=time)
	            		{
		            		eventQueue.add(t);
		            		waitEventTime=t;
		            		System.err.println("t:"+time+"wt:"+waitEventTime);
		            		//break;
	            		}
            		}
            	}
            	if(time>=waitEventTime&&(time-stepSize)<=waitEventTime)
        		{
            		logger.debug("prism event,time:"+time+" waitEventTime:"+waitEventTime);
            		//SetValueToFmu(sharedVarsPrism);
            		SetConditionToFmu(sharedVarsPrism,prismClient);
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
                   
                    // Check to see if we have completed the integrator step.
                    // Pass stepEvent in by reference. See
                    // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                            //if (enableLogging) 
                            {
                                for (i = 0; i < numberOfEventIndicators; i++)
                                {
                                    System.out.println("state event "
                                         + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
                                                            : "-/-")
                                                    + " eventIndicator[" + i+ "], time: " + time);
                                }
                            }
                          i=0;
                          //if(!isEventLastHappen[i])
                          //if(time-lastTime>stepSize)
                          {
                        	  lastTime=time;
    	                      isEventHappen[i]=true;
    	                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
//                    		  logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//                    		  sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//                    		  logger.debug(GetValue(fmiModelDescription, "in_c", fmiComponent));
                  			  SetValueToPrism(sharedVarsPrism,prismClient);
                  			  prismClient.DoStep(false);
                  			  SetValueToFmu(sharedVarsPrism,prismClient);
                  			  
//                  			  myLineChart=new MyLineChart();
//                              myLineChart.SetX(timeList, "Time");
//                              myLineChart.SetY(hNumberList, "h");
//                              //myLineChart.SetY(vNumberList, "in_v");
//                  			  return myLineChart;
                    		}
                    }
                   
            		logger.debug("in_v:"+GetValue(fmiModelDescription, "in_v", fmiComponent)+" out_v:"+GetValue(fmiModelDescription, "out_v", fmiComponent));
//        			SetValueToPrism(sharedVarsPrism);
//        			prismClient.DoStep(false);
//        			SetValueToFmu(sharedVarsPrism);
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
               
                // Check to see if we have completed the integrator step.
                // Pass stepEvent in by reference. See
                // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                
                timeList.add(time);
                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "in_v", vNumberList);

                // Generate a line for this step
//                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                        fmiComponent, time, file, csvSeparator, Boolean.FALSE,twoModelsVariables);
                numberOfSteps++;
            }
            invoke("_fmiTerminate", new Object[] { fmiComponent },
                    "Could not terminate: ");
            myLineChart=new JLineChart();
            myLineChart.SetX(timeList, "Time");
            myLineChart.SetY(hNumberList, "h");
            myLineChart.SetY(vNumberList, "in_v");
        } finally {
            if (file != null) {
                file.close();
            }
	    if (fmiModelDescription != null) {
		fmiModelDescription.dispose();
	    }
        }
        
        System.out.println("Simulation from " + startTime + " to " + endTime
                + " was successful");
        System.out.println("  steps: " + numberOfSteps);
        System.out.println("  step size: " + stepSize);
        System.out.println("  stateEvents: " + numberOfStateEvents);
        System.out.println("  stepEvents: " + numberOfStepEvents);
        System.out.println("  timeEvents: " + numberOfTimeEvents);
	System.out.flush();
	prismClient.Close();
	//System.err.println(sb);
//	prismClient.EndServer();
	return myLineChart.getJLineChart();
    }
    public Trace ctmcSimulate(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, double stepSize,
            boolean enableLogging, String outputFileName)
            throws Exception 
    {
    	PrismClient prismClient=PrismClient.getInstance();
    	if(!prismClient.Start(host, port))
    	{
    		logger.error("no PrismServer,host:"+host+"port:"+port);
    		//return null;
    	}
    	String prismVars=prismClient.OpenModel(prismModelPath);
    	if(null==prismVars)
    	{
    		logger.error("Model open fail!!!"+prismModelPath);
    		return null;
    	}
    	Trace trace=new Trace();
    	
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        //fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
        this.fmiModelDescription=fmiModelDescription;

        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
        if (enableLogging) {
            System.out.println("FMUModelExchange: about to load "
                    + sharedLibrary);
        }
        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

        // The modelName may have spaces in it.
        _modelIdentifier = fmiModelDescription.modelIdentifier;

        //new File(fmuFileName).toURI().toURL().toString();
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

        String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
        
        String[]prismS=prismVars.split(",");
        String[]fmuS=fmuVars.split(",");
        List<String>sharedVarsPrism=new ArrayList<>();
        for(int i=0;i<prismS.length;i++)
        {
        	if(prismS[i].startsWith("in_"))
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("out_"))
        			{
        				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
        	}
        	else if (prismS[i].startsWith("out_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("in_"))
        			{
        				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
			}
        	else if (prismS[i].startsWith("con_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].equals(prismS[i]))
        				sharedVarsPrism.add(prismS[i]);
			}
        }
        trace.AddNames("time,"+fmuVars+","+prismVars);
        ModelManager.getInstance().logger.debug(sharedVarsPrism);
//        File outputFile = new File(outputFileName);
//        PrintStream file = null;
        try {
	    // gcj does not have this constructor
            //file = new PrintStream(outputFile);
            //file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to write header");
            }
//            // Generate header row
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, ',', Boolean.TRUE);
//            // Output the initial values.
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, ',', Boolean.FALSE);

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
            
//            List<Object> timeList=new ArrayList<>();
//            List<Number> hNumberList=new ArrayList<Number>();
//            List<Number>vNumberList=new ArrayList<>();
//            List<Number>alterVNumberList=new ArrayList<>();     
            boolean isEnd=false;
            double lastTime=-1;
            PriorityQueue<Double>eventQueue=new PriorityQueue<>();
            double waitEventTime=-1;
            // Loop until the time is greater than the end time.
            while (time < endTime&&!isEnd) 
            {
//            	for(;waitEventTime>0&&waitEventTime<time;)
//            	{
//            		eventQueue.remove(waitEventTime);
//        			if(eventQueue.size()>0) waitEventTime=eventQueue.peek();
//        			else break;
//            	}
            	if(eventQueue.size()==0)
            	{
            		//while(true)
            		{
	            		prismClient.DoStep(false);
	            		//System.err.println("time:"+prismClient.GetTime());
	            		double t=prismClient.GetTime();
	            		if(t<time) t+=waitEventTime;
	            		//if(t>=time)
	            		{
		            		eventQueue.add(t);
		            		waitEventTime=t;
		            		//System.err.println("t:"+time+"wt:"+waitEventTime);
		            		//break;
	            		}
            		}
            	}
            	if(time>=waitEventTime&&(time-stepSize)<=waitEventTime)
        		{
            		logger.debug("prism event,time:"+time+" waitEventTime:"+waitEventTime);
            		//SetValueToFmu(sharedVarsPrism);
            		SetConditionToFmu(sharedVarsPrism,prismClient);
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
                   
                    // Check to see if we have completed the integrator step.
                    // Pass stepEvent in by reference. See
                    // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                            //if (enableLogging) 
                            {
                                for (i = 0; i < numberOfEventIndicators; i++)
                                {
                                    logger.debug("state event "
                                         + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
                                                            : "-/-")
                                                    + " eventIndicator[" + i+ "], time: " + time);
                                }
                            }
                          i=0;
                          //if(!isEventLastHappen[i])
                          //if(time-lastTime>stepSize)
                          {
                        	  lastTime=time;
    	                      isEventHappen[i]=true;
    	                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
//                    		  logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//                    		  sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//                    		  logger.debug(GetValue(fmiModelDescription, "in_c", fmiComponent));
                  			  SetValueToPrism(sharedVarsPrism,prismClient);
                  			  prismClient.DoStep(false);
                  			  SetValueToFmu(sharedVarsPrism,prismClient);
                    		}
                    }
                   
            		logger.debug("in_v:"+GetValue(fmiModelDescription, "in_v", fmiComponent)+" out_v:"+GetValue(fmiModelDescription, "out_v", fmiComponent));
//        			SetValueToPrism(sharedVarsPrism);
//        			prismClient.DoStep(false);
//        			SetValueToFmu(sharedVarsPrism);
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
               
                // Check to see if we have completed the integrator step.
                // Pass stepEvent in by reference. See
                // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                
//                timeList.add(time);
//                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
//                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "in_v", vNumberList);

                // Generate a line for this step
//                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
                trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent)+","+prismClient.curValuses);
                numberOfSteps++;
            }
            invoke("_fmiTerminate", new Object[] { fmiComponent },
                    "Could not terminate: ");
//            myLineChart=new MyLineChart();
//            myLineChart.SetX(timeList, "Time");
//            myLineChart.SetY(hNumberList, "h");
//            myLineChart.SetY(vNumberList, "in_v");
        } finally {
//            if (file != null) {
//                file.close();
//            }
	    if (fmiModelDescription != null) {
		fmiModelDescription.dispose();
	    }
        }
        
//        System.out.println("Simulation from " + startTime + " to " + endTime
//                + " was successful");
//        System.out.println("  steps: " + numberOfSteps);
//        System.out.println("  step size: " + stepSize);
//        System.out.println("  stateEvents: " + numberOfStateEvents);
//        System.out.println("  stepEvents: " + numberOfStepEvents);
//        System.out.println("  timeEvents: " + numberOfTimeEvents);
	System.out.flush();
	prismClient.Close();
	//System.err.println(sb);
//	prismClient.EndServer();
	return trace;
    }
    public Trace ctmcSimulateTradition(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, double stepSize,
            boolean enableLogging, String outputFileName)
            throws Exception 
    {
    	PrismClient prismClient=PrismClient.getInstance();
    	if(!prismClient.Start(host, port))
    	{
    		logger.error("no PrismServer,host:"+host+"port:"+port);
    		//return null;
    	}
    	String prismVars=prismClient.OpenModel(prismModelPath);
    	if(null==prismVars)
    	{
    		logger.error("Model open fail!!!"+prismModelPath);
    		return null;
    	}
    	Trace trace=new Trace();
    	
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        //fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
        this.fmiModelDescription=fmiModelDescription;

        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
        if (enableLogging) {
            System.out.println("FMUModelExchange: about to load "
                    + sharedLibrary);
        }
        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

        // The modelName may have spaces in it.
        _modelIdentifier = fmiModelDescription.modelIdentifier;

        //new File(fmuFileName).toURI().toURL().toString();
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

        String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
        
        String[]prismS=prismVars.split(",");
        String[]fmuS=fmuVars.split(",");
        List<String>sharedVarsPrism=new ArrayList<>();
        for(int i=0;i<prismS.length;i++)
        {
        	if(prismS[i].startsWith("in_"))
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("out_"))
        			{
        				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
        	}
        	else if (prismS[i].startsWith("out_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].startsWith("in_"))
        			{
        				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
        					sharedVarsPrism.add(prismS[i]);
        			}
			}
        	else if (prismS[i].startsWith("con_")) 
        	{
        		for (int j = 0; j < fmuS.length; j++) 
        			if(fmuS[j].equals(prismS[i]))
        				sharedVarsPrism.add(prismS[i]);
			}
        }
        trace.AddNames("time,"+fmuVars+","+prismVars);
        ModelManager.getInstance().logger.debug(sharedVarsPrism);
//        File outputFile = new File(outputFileName);
//        PrintStream file = null;
        try {
	    // gcj does not have this constructor
            //file = new PrintStream(outputFile);
            //file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to write header");
            }
//            // Generate header row
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, ',', Boolean.TRUE);
//            // Output the initial values.
//            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, ',', Boolean.FALSE);

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
            
//            List<Object> timeList=new ArrayList<>();
//            List<Number> hNumberList=new ArrayList<Number>();
//            List<Number>vNumberList=new ArrayList<>();
//            List<Number>alterVNumberList=new ArrayList<>();     
            boolean isEnd=false;
            double lastTime=-1;
            PriorityQueue<Double>eventQueue=new PriorityQueue<>();
            double waitEventTime=-1;
            // Loop until the time is greater than the end time.
            while (time < endTime&&!isEnd) 
            {
//            	for(;waitEventTime>0&&waitEventTime<time;)
//            	{
//            		eventQueue.remove(waitEventTime);
//        			if(eventQueue.size()>0) waitEventTime=eventQueue.peek();
//        			else break;
//            	}
            	if(eventQueue.size()==0)
            	{
            		//while(true)
            		{
	            		prismClient.DoStep(false);
	            		//System.err.println("time:"+prismClient.GetTime());
	            		double t=prismClient.GetTime();
	            		if(t<time) t+=waitEventTime;
	            		//if(t>=time)
	            		{
		            		eventQueue.add(t);
		            		waitEventTime=t;
		            		//System.err.println("t:"+time+"wt:"+waitEventTime);
		            		//break;
	            		}
            		}
            	}
            	if(time>=waitEventTime&&(time-stepSize)<=waitEventTime)
        		{
            		logger.debug("prism event,time:"+time+" waitEventTime:"+waitEventTime);
            		//SetValueToFmu(sharedVarsPrism);
            		SetConditionToFmu(sharedVarsPrism,prismClient);
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
                   
                    // Check to see if we have completed the integrator step.
                    // Pass stepEvent in by reference. See
                    // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                            //if (enableLogging) 
                            {
                                for (i = 0; i < numberOfEventIndicators; i++)
                                {
                                    logger.debug("state event "
                                         + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
                                                            : "-/-")
                                                    + " eventIndicator[" + i+ "], time: " + time);
                                }
                            }
                          i=0;
                          //if(!isEventLastHappen[i])
                          //if(time-lastTime>stepSize)
                          {
                        	  lastTime=time;
    	                      isEventHappen[i]=true;
    	                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
//                    		  logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//                    		  sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//                    		  logger.debug(GetValue(fmiModelDescription, "in_c", fmiComponent));
                  			  SetValueToPrism(sharedVarsPrism,prismClient);
                  			  prismClient.DoStep(false);
                  			  SetValueToFmu(sharedVarsPrism,prismClient);
                    		}
                    }
                   
            		logger.debug("in_v:"+GetValue(fmiModelDescription, "in_v", fmiComponent)+" out_v:"+GetValue(fmiModelDescription, "out_v", fmiComponent));
//        			SetValueToPrism(sharedVarsPrism);
//        			prismClient.DoStep(false);
//        			SetValueToFmu(sharedVarsPrism);
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
               
                // Check to see if we have completed the integrator step.
                // Pass stepEvent in by reference. See
                // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
                SetValueToPrism(sharedVarsPrism,prismClient);
             	prismClient.DoStep(false);
             	FakeSetValueToFmu(sharedVarsPrism, prismClient);

                // Generate a line for this step
//                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
                trace.AddState(time+","+GetAllValue(fmiModelDescription, fmiComponent)+","+prismClient.CurValues());
                numberOfSteps++;
            }
            invoke("_fmiTerminate", new Object[] { fmiComponent },
                    "Could not terminate: ");
//            myLineChart=new MyLineChart();
//            myLineChart.SetX(timeList, "Time");
//            myLineChart.SetY(hNumberList, "h");
//            myLineChart.SetY(vNumberList, "in_v");
        } finally {
//            if (file != null) {
//                file.close();
//            }
	    if (fmiModelDescription != null) {
		fmiModelDescription.dispose();
	    }
        }
        
//        System.out.println("Simulation from " + startTime + " to " + endTime
//                + " was successful");
//        System.out.println("  steps: " + numberOfSteps);
//        System.out.println("  step size: " + stepSize);
//        System.out.println("  stateEvents: " + numberOfStateEvents);
//        System.out.println("  stepEvents: " + numberOfStepEvents);
//        System.out.println("  timeEvents: " + numberOfTimeEvents);
	System.out.flush();
	prismClient.Close();
	//System.err.println(sb);
//	prismClient.EndServer();
	return trace;
    }
    
    
    private void SetValueToPrism(List<String>sharedVarsPrism,PrismClient prismClient)
    {
    	for(int i=0;i<sharedVarsPrism.size();i++)
    		if(sharedVarsPrism.get(i).startsWith("in_"))
    		{
    			Object tObject=GetValue(fmiModelDescription,"out_"+sharedVarsPrism.get(i).substring(3,sharedVarsPrism.get(i).length()), fmiComponent);
    			if(tObject instanceof Double)
    			{
    				double td=Double.valueOf(tObject.toString())*PrismClient.xiaoShuWei;
    				tObject=(int)Math.floor(td);
    			}
    			prismClient.SetValue(sharedVarsPrism.get(i),tObject);
    			ModelManager.getInstance().logger.debug("set value to Prism,"+sharedVarsPrism.get(i)+":"+tObject);
    		}
    		else if(sharedVarsPrism.get(i).startsWith("con_"))
    		{
    			Object tObject=GetValue(fmiModelDescription,sharedVarsPrism.get(i), fmiComponent);
    			prismClient.SetValue(sharedVarsPrism.get(i),tObject);
//    			if("1".equals(tObject.toString()))
//    			ModelManager.getInstance().logger.error("set condition to Prism,"+sharedVarsPrism.get(i)+":"+tObject);
    		}
    }
    private void SetValueToFmu(List<String>sharedVarsPrism,PrismClient prismClient)
    {
    	for(int i=0;i<sharedVarsPrism.size();i++)
    		if(sharedVarsPrism.get(i).startsWith("out_")) 
    		{
    			SetValue(fmiModelDescription,"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent, prismClient.GetValue(sharedVarsPrism.get(i)));
    			logger.debug("set value to fmu,"+"in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length())+":"+prismClient.GetValue(sharedVarsPrism.get(i)));
    			logger.debug("in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length())+" in fmu is:"+GetValue(fmiModelDescription, "in_"+sharedVarsPrism.get(i).substring(4,sharedVarsPrism.get(i).length()), fmiComponent));
    		}
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
	public HashSet<String> GetFmuVariables(String fmuPath){
		HashSet<String> res=new HashSet<>();
		String[] tStrings=GetFMUVariables(fmuPath);
		String ts;
		for(int i=0;i<tStrings.length;i++){
			ts=tStrings[i].substring(tStrings[i].lastIndexOf('.')+1);
			if(!res.contains(ts)) res.add(ts);
		}
		return res;
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
	
	double maxTimes=0;
	public MyLineChart ctmcMulti(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, double stepSize,
            int times) throws Exception 
    {
		   List<Object> timeList=new ArrayList<>();
//           List<Number> hNumberList=new ArrayList<Number>();
//           List<Number>vNumberList=new ArrayList<>();
//           List<Number>alterVNumberList=new ArrayList<>();   
           MyLineChart myLineChart=new MyLineChart();
           List<List<Number>> resList=new ArrayList<>();
           for(int i=0;i<times;i++)
           {
        	   List<Number> hNumberList=ctmcOne(prismModelPath,prismModelType,fmiModelDescription,endTime,stepSize,false,',',"./1.xml");
        	   //myLineChart.SetY(hNumberList, String.valueOf(i));
        	   resList.add(hNumberList);
        	   maxTimes=maxTimes<hNumberList.size()?hNumberList.size():maxTimes;
        	   //System.err.println("size:"+hNumberList.size());
           }
           int i=0,j,cnt;
           for(i=0;i<resList.size();i++)
           {
        	   List<Number> hNumberList=resList.get(i);
//        	   for(cnt=hNumberList.size()-1,j=0;j<5&&cnt+j<maxTimes;j++)
//        		   hNumberList.add(hNumberList.get(cnt));
        	   myLineChart.SetY(hNumberList, String.valueOf(i));
        	   //resList.set(i, hNumberList);
           }
           i=0;
           for(double t=0;i<=maxTimes;t+=stepSize,i++)
        	   timeList.add(t);
           myLineChart.SetX(timeList, "Time");
           System.err.println("max steps:"+timeList.size());
           
//           myLineChart.SetY(hNumberList, "h");
//           myLineChart.SetY(vNumberList, "in_v");
           return myLineChart;
    }
	 private List<Number> ctmcOne(String prismModelPath,String prismModelType,FMIModelDescription fmiModelDescription, double endTime, double stepSize,
	            boolean enableLogging, char csvSeparator, String outputFileName)
	            throws Exception 
	    {
		    int cnt=0;
		    int moreSteps=13;
		    boolean isHappened=false;
            List<Number> hNumberList=new ArrayList<Number>();
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
	    	
	        // Avoid a warning from FindBugs.
	        FMUDriver._setEnableLogging(enableLogging);

	        // Parse the .fmu file.
	        //fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
	        this.fmiModelDescription=fmiModelDescription;
	        
	        // Load the shared library.
	        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
	        if (enableLogging) {
	            System.out.println("FMUModelExchange: about to load "
	                    + sharedLibrary);
	        }
	        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

	        // The modelName may have spaces in it.
	        _modelIdentifier = fmiModelDescription.modelIdentifier;

	        //new File(fmuFileName).toURI().toURL().toString();
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

	        String fmuVars=OutputRow.GetVars(_nativeLibrary, fmiModelDescription, fmiComponent);
	        
	        String[]prismS=prismVars.split(",");
	        String[]fmuS=fmuVars.split(",");
	        List<String>sharedVarsPrism=new ArrayList<>();
	        for(int i=0;i<prismS.length;i++)
	        {
	        	if(prismS[i].startsWith("in_"))
	        	{
	        		for (int j = 0; j < fmuS.length; j++) 
	        			if(fmuS[j].startsWith("out_"))
	        			{
	        				if(prismS[i].substring(3,prismS[i].length()).equals(fmuS[j].substring(4,fmuS[j].length())))
	        					sharedVarsPrism.add(prismS[i]);
	        			}
	        	}
	        	else if (prismS[i].startsWith("out_")) 
	        	{
	        		for (int j = 0; j < fmuS.length; j++) 
	        			if(fmuS[j].startsWith("in_"))
	        			{
	        				if(prismS[i].substring(4,prismS[i].length()).equals(fmuS[j].substring(3,fmuS[j].length())))
	        					sharedVarsPrism.add(prismS[i]);
	        			}
				}
	        	else if (prismS[i].startsWith("con_")) 
	        	{
	        		for (int j = 0; j < fmuS.length; j++) 
	        			if(fmuS[j].equals(prismS[i]))
	        				sharedVarsPrism.add(prismS[i]);
				}
	        }
	        ModelManager.getInstance().logger.debug(sharedVarsPrism);
//	        File outputFile = new File(outputFileName);
//	        PrintStream file = null;
	        try {
		    // gcj does not have this constructor
	            //file = new PrintStream(outputFile);
//	            file = new PrintStream(outputFileName);
	            if (enableLogging) {
	                System.out.println("FMUModelExchange: about to write header");
	            }
//	            // Generate header row
//	            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//	                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
//	            // Output the initial values.
//	            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//	                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);

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
	            
//	            List<Object> timeList=new ArrayList<>();
//	            List<Number> hNumberList=new ArrayList<Number>();
//	            List<Number>vNumberList=new ArrayList<>();
//	            List<Number>alterVNumberList=new ArrayList<>();     
	            boolean isEnd=false;
	            double lastTime=-1;
	            PriorityQueue<Double>eventQueue=new PriorityQueue<>();
	            double waitEventTime=-1;
	            // Loop until the time is greater than the end time.
	            while (time < endTime&&!isEnd) 
	            {
	            	if(isHappened)
	            	{
	            		if(--moreSteps==0) 
	            		{
	            			prismClient.Close();
	            			return hNumberList;
	            		}
	            	}
//	            	for(;waitEventTime>0&&waitEventTime<time;)
//	            	{
//	            		eventQueue.remove(waitEventTime);
//	        			if(eventQueue.size()>0) waitEventTime=eventQueue.peek();
//	        			else break;
//	            	}
	            	if(eventQueue.size()==0)
	            	{
	            		//while(true)
	            		{
		            		prismClient.DoStep(false);
		            		logger.debug("time:"+prismClient.GetTime());
		            		double t=prismClient.GetTime();
		            		if(t<time) t+=waitEventTime;
		            		//if(t>=time)
		            		{
			            		eventQueue.add(t);
			            		waitEventTime=t;
			            		logger.debug("t:"+time+"wt:"+waitEventTime);
			            		//break;
		            		}
	            		}
	            	}
	            	if(time>=waitEventTime&&(time-stepSize)<=waitEventTime)
	        		{
	            		logger.debug("prism event,time:"+time+" waitEventTime:"+waitEventTime);
	            		//SetValueToFmu(sharedVarsPrism);
	            		SetConditionToFmu(sharedVarsPrism,prismClient);
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
	                   
	                    // Check to see if we have completed the integrator step.
	                    // Pass stepEvent in by reference. See
	                    // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
	                            //if (enableLogging) 
	                            {
	                                for (i = 0; i < numberOfEventIndicators; i++)
	                                {
	                                    logger.debug("state event "
	                                         + (preEventIndicators[i] > 0&& eventIndicators[i] < 0 ? "-\\-"
	                                                            : "-/-")
	                                                    + " eventIndicator[" + i+ "], time: " + time);
	                                }
	                            }
	                          i=0;
	                          //if(!isEventLastHappen[i])
	                          //if(time-lastTime>stepSize)
	                          {
	                        	  lastTime=time;
	    	                      isEventHappen[i]=true;
	    	                	  //logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
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
//	                    		  logger.debug(GetValue(fmiModelDescription, "out_v", fmiComponent));
//	                    		  sb.append(time+" "+GetValue(fmiModelDescription, "in_v", fmiComponent)+" ");
//	                    		  logger.debug(GetValue(fmiModelDescription, "in_c", fmiComponent));
	                    			if(!isHappened)
	                    			{
		                  			  SetValueToPrism(sharedVarsPrism,prismClient);
		                  			  prismClient.DoStep(false);
		                  			  SetValueToFmu(sharedVarsPrism,prismClient);
	                    			}
	                  			  if(++cnt==1)
	                  			  {
	                  				  isHappened=true;
//		                  			  maxTime=time>maxTime?time:maxTime;
//		                  			  prismClient.Close();
//		                  			  return hNumberList;
	                  			  }
	                  			  
//	                  			  myLineChart=new MyLineChart();
//	                              myLineChart.SetX(timeList, "Time");
//	                              myLineChart.SetY(hNumberList, "h");
//	                              //myLineChart.SetY(vNumberList, "in_v");
//	                  			  return myLineChart;
	                    		}
	                    }
	                   
	            		logger.debug("in_v:"+GetValue(fmiModelDescription, "in_v", fmiComponent)+" out_v:"+GetValue(fmiModelDescription, "out_v", fmiComponent));
//	        			SetValueToPrism(sharedVarsPrism);
//	        			prismClient.DoStep(false);
//	        			SetValueToFmu(sharedVarsPrism);
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
	               
	                // Check to see if we have completed the integrator step.
	                // Pass stepEvent in by reference. See
	                // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
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
	                
	                //timeList.add(time);
	                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
//	                OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "in_v", vNumberList);

	                // Generate a line for this step
//	                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//	                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
	                numberOfSteps++;
	            }
	            invoke("_fmiTerminate", new Object[] { fmiComponent },
	                    "Could not terminate: ");
//	            myLineChart=new MyLineChart();
//	            myLineChart.SetX(timeList, "Time");
//	            myLineChart.SetY(hNumberList, "h");
//	            myLineChart.SetY(vNumberList, "in_v");
	        } finally {
//	            if (file != null) {
//	                file.close();
//	            }
		    if (fmiModelDescription != null) {
			fmiModelDescription.dispose();
		    }
	        }
	        
//	        System.out.println("Simulation from " + startTime + " to " + endTime
//	                + " was successful");
//	        System.out.println("  steps: " + numberOfSteps);
//	        System.out.println("  step size: " + stepSize);
//	        System.out.println("  stateEvents: " + numberOfStateEvents);
//	        System.out.println("  stepEvents: " + numberOfStepEvents);
//	        System.out.println("  timeEvents: " + numberOfTimeEvents);
		System.out.flush();
		prismClient.Close();
		//System.err.println(sb);
//		prismClient.EndServer();
		return hNumberList;
	    }
}
