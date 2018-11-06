package ecnu.oe.cosimulationMaster.ma;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import ecnu.oe.cosimulationMaster.model.FMU;
import ecnu.oe.cosimulationMaster.model.State;
import ecnu.oe.cosimulationMaster.model.Trace;
import ecnu.oe.cosimulationMaster.utils.IODependencyClassifier;
import ecnu.oe.cosimulationMaster.utils.KeyFMUsExtractor;
import org.ptolemy.fmi.*;
import org.ptolemy.fmi.driver.FMUDriver;

import java.io.File;
import java.util.*;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/3/1.
 */
public class PSRMA extends FMUDriver {
    private final boolean showResult;
    private final boolean showTimeConsumption;
    private final boolean showPartion;

    public PSRMA(boolean sr, boolean stc, boolean sp) {
        showResult = sr;
        showTimeConsumption = stc;
        showPartion = sp;
    }

    public static ArrayList<double[][]> plotArray = null;

    public long simulate(String fmuFileName, double endTime, double stepSize,
                         boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {
        return 0L;
    }

    public Trace simulateOne(ArrayList<FMU> fmuList, HashMap<String, String> dataExchange_map, String fmuFileName, double endTime, double stepSize,
                             boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {

        ArrayList<State> statesList = new ArrayList<>();
        ArrayList<String> variableList = new ArrayList<>();
        Long store_pre_time = 0L;
        List<Long[]> simulateTime_analyse = new ArrayList<>();
        Long dataExchangeTotalTime = 0L;
        Long storeCurrentTotalTime = 0L;
        Long doStepTotalTime = 0L;
        double maxStepSize = stepSize;
        Long step_revision_time = 0L;
        int revision_num = 0;
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        String[] fmuFileNameArray = fmuFileName.split(",");
        int FMUNumbers = fmuFileNameArray.length;
        FMIModelDescription[] fmiModelDescriptions = new FMIModelDescription[FMUNumbers];
        for (int i = 0; i < fmiModelDescriptions.length; i++) {
            fmiModelDescriptions[i] = FMUFile
                    .parseFMUFile(fmuFileNameArray[i]);
        }
        // Load the shared library.
        NativeLibrary[] nativeLibrary = new NativeLibrary[FMUNumbers];
        for (int i = 0; i < FMUNumbers; i++)
            nativeLibrary[i] = NativeLibrary.getInstance(FMUFile.fmuSharedLibrary(fmiModelDescriptions[i]));

        // The URL of the fmu file.
        String[] fmuLocations = new String[FMUNumbers];
        for (int i = 0; i < FMUNumbers; i++)
            fmuLocations[i] = new File(fmuFileName.split(",")[i]).toURI().toURL().toString();
        // The tool to use if we have tool coupling.
        String mimeType = "application/x-fmu-sharedlibrary";
        // Timeout in ms., 0 means wait forever.
        double timeout = 100000;

        byte visible = 0;
        // Run the simulator without user interaction.
        byte interactive = 0;

        // Callbacks
        FMICallbackFunctions.ByReference callbacks = new FMICallbackFunctions.ByReference(
                new FMULibrary.FMULogger(), new FMULibrary.FMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished(),
                new FMULibrary.FMUComponentEnvironment());
        // Logging tends to cause segfaults because of vararg callbacks.
        byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;
        loggingOn = (byte) 0;

        Pointer[] fmiComponent = new Pointer[nativeLibrary.length];
        for (int i = 0; i < FMUNumbers; i++) {
            if (fmiModelDescriptions[i] != null) {
                fmiModelDescriptions[i].dispose();
            }
        }

        for (int i = 0; i < FMUNumbers; i++) {
            Function instantiateSlave = nativeLibrary[i].getFunction("fmi2Instantiate");
            fmiComponent[i] = (Pointer) instantiateSlave.invoke(Pointer.class,
                    new Object[]{fmiModelDescriptions[i].modelIdentifier, fmiModelDescriptions[i].guid,
                            fmuLocations[i], callbacks, visible, loggingOn});
            if (fmiComponent[i].equals(Pointer.NULL)) {
                throw new RuntimeException("Could not instantiate model.");
            }
        }
        double startTime = 0;
        for (int i = 0; i < FMUNumbers; i++) {
            Function setupExperiment = nativeLibrary[i].getFunction("fmi2SetupExperiment");
            Double fmi2Flag = (Double) setupExperiment.invoke(Double.class, new Object[]{fmiComponent[i], (byte) 1, 2, startTime,
                    (byte) 1, endTime});
//            System.err.println(fmi2Flag);

            Function enterInitializationMode = nativeLibrary[i].getFunction("fmi2EnterInitializationMode");
            fmi2Flag = (Double) enterInitializationMode.invoke(Double.class, new Object[]{fmiComponent[i]});
//            System.err.println(fmi2Flag);

            Function exitInitializationMode = nativeLibrary[i].getFunction("fmi2ExitInitializationMode");
            fmi2Flag = (Double) exitInitializationMode.invoke(Double.class, new Object[]{fmiComponent[i]});
//            System.err.println(fmi2Flag);
            Function getEventType = nativeLibrary[i].getFunction("fmi2GetEventType");
            int ifKey = (Integer) getEventType.invoke(Integer.class, new Object[]{fmiComponent[i]});
            if (ifKey == 1)
                fmuList.get(i).setType(FMU.FMUType.de);
            else
                fmuList.get(i).setType(FMU.FMUType.ct);
        }

        HashMap ExtractedFMUSets = KeyFMUsExtractor.extracts(fmuList, dataExchange_map);
        int[] errorFMUQue = (int[]) ExtractedFMUSets.get("error");
        int[] dependFMUQue = (int[]) ExtractedFMUSets.get("depend");
        int[] restFMUQue = (int[]) ExtractedFMUSets.get("rest");
        HashMap[] ClassifiedIO = IODependencyClassifier.classify((int[]) ExtractedFMUSets.get("error"), dataExchange_map);
        HashMap<String, String> simulIO = ClassifiedIO[1];
        HashMap<String, String> delayedIO = ClassifiedIO[0];

        try {
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            double time = startTime;
            Long start = System.currentTimeMillis();
            //store variable references
            HashMap<String, FMIScalarVariable> FMUVariableReferences = new HashMap<>();
            HashMap<String, Object> CurrentVariableValues = new HashMap<>();
            HashMap<String, Object> PreviousVariableValues = new HashMap<>();
            for (int i = 0; i < fmiModelDescriptions.length; i++) {
                FMIModelDescription fmd = fmiModelDescriptions[i];
                for (FMIScalarVariable fsv : fmd.modelVariables) {
                    FMUVariableReferences.put(i + "." + fmd.modelName + "." + fsv.valueReference, fsv);
                }
            }
            Pointer[] currentSystemState = new Pointer[FMUNumbers];
            for (int i = 0; i < FMUNumbers; i++) {
                Pointer csf = null;
                Function getFMUstate = nativeLibrary[i].getFunction("fmi2GetFMUstate");
                csf = (Pointer) getFMUstate.invoke(Pointer.class, new Object[]{fmiComponent[i]});
                if (csf != null) {
                    currentSystemState[i] = csf;
                } else
                    System.err.println("cant's retrieve state of the FMU" + fmuList.get(i).getFMUName());
            }

            int badStepNum = 0;
            List<Double[][]> dataSet = new ArrayList<Double[][]>();
            //store previous states
            double pre_Time = 0;
            double h_p = 0;
            Long doStep_time = System.currentTimeMillis();
            List<Long> doStep_timeList = new ArrayList<>();
            double current_Time = 0;
//            stepSize = 1e-5;
            storeVariableNames(fmiModelDescriptions, fmiComponent, variableList);
            while (time < endTime) {
                Long[] timeConsumptionQue = new Long[3];
                //updating IO using simultaneous method

                printAllinLine(fmiModelDescriptions, fmiComponent, statesList, time);
                IODependenciesUpdating(simulIO, FMUVariableReferences, fmiComponent);
                //store current state;
                for (int i = 0; i < FMUNumbers; i++) {
                    Pointer csf = null;
                    Function getFMUstate = nativeLibrary[i].getFunction("fmi2GetFMUstate");
                    csf = (Pointer) getFMUstate.invoke(Pointer.class, new Object[]{fmiComponent[i]});
                    if (csf != null) {
                        currentSystemState[i] = csf;
                    } else
                        System.err.println("cant's retrieve state of the FMU" + fmuList.get(i).getFMUName());
                }
//                printSelected(time,fmiModelDescriptions,fmiComponent);
                while (true) {
                    //call dostep for FMU of Cs
                    for (int i = 0; i < dependFMUQue.length; i++) {
                        nativeLibrary[dependFMUQue[i]].getFunction("fmi2DoStep").invoke(Double.class, new Object[]{fmiComponent[dependFMUQue[i]], time, stepSize,
                                (byte) 1});
                    }
                    double fmuStatus = 0;
                    // update IO using delayed updating method
                    IODependenciesUpdating(delayedIO, FMUVariableReferences, fmiComponent);
                    //call dostep for FMU of error
                    for (int i = 0; i < errorFMUQue.length; i++) {
                        nativeLibrary[errorFMUQue[i]].getFunction("fmi2DoStep").invoke(Double.class, new Object[]{fmiComponent[errorFMUQue[i]], time, stepSize,
                                (byte) 1});
                        fmuStatus = 1;
                        if (fmuStatus >= 2)
                            break;
                    }
//                    printSelected(time,fmiModelDescriptions,fmiComponent);
//                    printAllinLine(fmiModelDescriptions,fmiComponent);
                    // partial step reversion
                    if (fmuStatus >= 2) {
                        //roll back these
                        rollbackFMU(currentSystemState, errorFMUQue, fmiComponent, nativeLibrary);
                        rollbackFMU(currentSystemState, dependFMUQue, fmiComponent, nativeLibrary);
                        stepSize = stepSize / 2;

//                        printAllinLine(fmiModelDescriptions,fmiComponent);
                    } else
                        break;
                }
                for (int i = 0; i < restFMUQue.length; i++) {
                    nativeLibrary[restFMUQue[i]].getFunction("fmi2DoStep").invoke(Double.class, new Object[]{fmiComponent[restFMUQue[i]], time, stepSize,
                            (byte) 1});
                }
//                printAllinLine(fmiModelDescriptions,fmiComponent);
                time += stepSize;
                stepSize = maxStepSize;
            }

            System.out.println("dostep time" + (System.currentTimeMillis() - doStep_time));

            // Don't throw an exception while freeing a slave.  Some
            // fmiTerminateSlave calls free the slave for us.
            for (int i = 0; i < FMUNumbers; i++) {
                Function freeSlave = nativeLibrary[i].getFunction("fmi2FreeInstance");
                int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
                        new Object[]{fmiComponent[i]})).intValue();
                if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
                    System.out.println(fmiFlag);
                }
            }
            System.out.println("simulate origin:" + (System.currentTimeMillis() - start) + "revision time:" + step_revision_time + "revision number:" + revision_num);
            Trace trace = new Trace(statesList, variableList);
            return trace;
        } finally {
////            if (file != null) {
////                file.close();
//            }
            for (int i = 0; i < FMUNumbers; i++) {
                if (fmiModelDescriptions[i] != null) {
                    fmiModelDescriptions[i].dispose();
                }
            }
        }
    }

    public void IODependenciesUpdating(HashMap IODependencies, HashMap<String, FMIScalarVariable> FMUVariableReferences, Pointer[] fmiComponent) {
        Iterator it = IODependencies.entrySet().iterator();
        int index = 0;
        //psrma的思路是先更新一部分io,然后Cd dostep ,然后更新部分io,然后Cerr dostep, 然后剩下的
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String variableTarget = (String) entry.getKey();
            String variableSource = (String) entry.getValue();
            FMIScalarVariable target = FMUVariableReferences.get(variableTarget);
            FMIScalarVariable source = FMUVariableReferences.get(variableSource);
            if (source != null && target != null) {
//                        System.out.println(index++);
                target.setDouble(fmiComponent[Integer.parseInt(variableTarget.split("\\.")[0])],
                        source.getDouble(fmiComponent[Integer.parseInt(variableSource.split("\\.")[0])]));
            }
        }
    }

    public void rollbackFMU(Pointer[] currentSystemState, int[] errorFMUQue, Pointer[] fmiComponent, NativeLibrary[] nativeLibrary) {
        for (int i = 0; i < errorFMUQue.length; i++) {
            Function setFMUstate = nativeLibrary[errorFMUQue[i]].getFunction("fmi2SetFMUstate");
            setFMUstate.invoke(Double.class, new Object[]{fmiComponent[errorFMUQue[i]], fmiComponent[i]});
        }
    }

    public void printAllinLine(FMIModelDescription[] fmiModelDescriptions, Pointer[] fmiComponent, ArrayList<State> trace, double time) {
        String line = "";
        State state = new State();
        state.time = time;
        ArrayList<Object> currentTrace = new ArrayList<>();
        for (int i = 0; i < fmiModelDescriptions.length; i++) {
            FMIModelDescription fmiModelDescription = fmiModelDescriptions[i];
            List<FMIScalarVariable> fmd = fmiModelDescriptions[i].modelVariables;
            for (int j = 0; j < fmd.size(); j++) {

                String value = Double.toString(fmd.get(j).getDouble(fmiComponent[i]));
                line += value;
                currentTrace.add(value);
            }
        }
        state.values = currentTrace;
        trace.add(state);
        System.out.println(line);
    }
    //保存变量名称
    public void storeVariableNames(FMIModelDescription[] fmiModelDescriptions, Pointer[] fmiComponent,ArrayList<String> list){
        for (int i = 0; i < fmiModelDescriptions.length; i++) {
            FMIModelDescription fmiModelDescription = fmiModelDescriptions[i];
            List<FMIScalarVariable> fmd = fmiModelDescriptions[i].modelVariables;
            for (int j = 0; j < fmd.size(); j++) {
                list.add(fmiModelDescription.modelName + fmd.get(j).name);
            }
        }
    }
}

