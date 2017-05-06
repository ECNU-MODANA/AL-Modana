package ecnu.modana.FmiDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMIEventInfo;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;

import ecnu.modana.util.MyLineChart;

public class FMUMESlave extends FMUDriver{
	boolean loggingOn=false;
	double startTime=0;
	FMIModelDescription fmiModelDescription;
	Pointer fmiComponent;
	FMICallbackFunctions.ByValue callbacks;
    Function instantiateModelFunction;
    Function completedIntegratorStep ;
    Function eventUpdate ;
    Function getContinuousStates ;
    Function getDerivatives;
    Function getEventIndicators ;
    Function setContinuousStates ; 
    Function setTime ;
    int numberOfStateEvents = 0;
    int numberOfStepEvents = 0;
    int numberOfSteps = 0;
    int numberOfTimeEvents = 0;
    FMIEventInfo eventInfo = new FMIEventInfo();
 // Should these be on the heap?
     int numberOfStates ;
     int numberOfEventIndicators ;
    double[] states;
    double[] derivatives ;
    boolean enableLogging=true;
    double endTime=9e30;
    double[] eventIndicators = null;
    double[] preEventIndicators = null;
    boolean stateEvent = false;
    byte stepEvent = (byte) 0;
    
	//Function doStep ;
    String fmuPath;
    public FMUMESlave(String fmuPath) throws IOException{
    	this.fmuPath=fmuPath;
    	// Parse the .fmu file.
        fmiModelDescription = FMUFile.parseFMUFile(fmuPath);
        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);
    	_modelIdentifier=fmiModelDescription.modelIdentifier;
    	Ini();
    }
    public double doStep(double time,double stepSize){
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
        if (stateEvent || stepEvent != (byte) 0 || timeEvent) {
            if (stateEvent) {
                numberOfStateEvents++;
                if (enableLogging) {
                    for (int i = 0; i < numberOfEventIndicators; i++) {
                        System.out
                                .println("state event "
                                        + (preEventIndicators[i] > 0
                                                && eventIndicators[i] < 0 ? "-\\-"
                                                : "-/-")
                                        + " eventIndicator[" + i
                                        + "], time: " + time);
                    }
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
                //break;
                return stepSize;
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
        }
//       System.out.print("time:"+time);
//       if(states.length>0) System.out.println(","+states[0]);
//       else System.out.println();
    	return stepSize;
    }
    public void Ini2() throws MalformedURLException{
    	 // Callbacks
        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
		new FMULibrary.FMULogger(), fmiModelDescription.getFMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished());
        // Logging tends to cause segfaults because of vararg callbacks.
        byte loggingOn = 0;
        loggingOn = (byte) 0;

     // The URL of the fmu file.
        String fmuLocation = new File(fmuPath).toURI().toURL().toString();
        Function instantiateSlave = getFunction("_fmiInstantiateSlave");
        Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
                new Object[] { _modelIdentifier, fmiModelDescription.guid,
                        fmuLocation, "application/x-fmu-sharedlibrary", 1000, 0, 0,
                        callbacks, loggingOn });
    }
	public void Ini(){
        numberOfStates = fmiModelDescription.numberOfContinuousStates;
        numberOfEventIndicators = fmiModelDescription.numberOfEventIndicators;
        states = new double[numberOfStates];
        derivatives = new double[numberOfStates];
        
       if (numberOfEventIndicators > 0) {
           eventIndicators = new double[numberOfEventIndicators];
           preEventIndicators = new double[numberOfEventIndicators];
       }
		 // Callbacks
       callbacks = new FMICallbackFunctions.ByValue(
	        new FMULibrary.FMULogger(), fmiModelDescription.getFMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished());
       
       // Instantiate the model.
       //Function instantiateModelFunction;
       try {
           instantiateModelFunction = getFunction("_fmiInstantiateModel");
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
        completedIntegratorStep = getFunction("_fmiCompletedIntegratorStep");
        eventUpdate = getFunction("_fmiEventUpdate");
        getContinuousStates = getFunction("_fmiGetContinuousStates");
        getDerivatives = getFunction("_fmiGetDerivatives");
        getEventIndicators = getFunction("_fmiGetEventIndicators");
        setContinuousStates = getFunction("_fmiSetContinuousStates"); 
        setTime = getFunction("_fmiSetTime");
        invoke(setTime, new Object[] { fmiComponent, startTime },
                "Could not set time to start time: " + startTime + ": ");
        
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
	}
	@Override
	public MyLineChart simulate(String fmuFileName, double endTime, double stepSize, boolean enableLogging,
			char csvSeparator, String outputFileName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
