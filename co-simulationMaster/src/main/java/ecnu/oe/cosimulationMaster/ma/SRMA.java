package ecnu.oe.cosimulationMaster.ma;


import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import ecnu.oe.cosimulationMaster.model.FMU;
import ecnu.oe.cosimulationMaster.model.State;
import org.ptolemy.fmi.*;
import org.ptolemy.fmi.driver.FMUDriver;


import java.io.File;
import java.util.*;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/3/1.
 */
public class SRMA extends FMUDriver {
    private final boolean showResult;
    private final boolean showTimeConsumption;
    private final boolean showPartion;
    public SRMA(boolean sr, boolean stc, boolean sp){
        showResult = sr;
        showTimeConsumption = stc;
        showPartion = sp;
    }


    /** Perform co-simulation using the named Functional Mock-up Unit (FMU) file.
     *  @param fmuFileName The pathname of the co-simulation .fmu file
     *  @param endTime The ending time in seconds.
     *  @param stepSize The step size in seconds.
     *  @param enableLogging True if logging is enabled.
     *  @param csvSeparator The character used for separating fields.
     *  Note that sometimes the decimal point in floats is converted to ','.
     *  @param outputFileName The output file.
     *  @exception Exception If there is a problem parsing the .fmu file or invoking
     *  the methods in the shared library.
     */
    public static ArrayList<double[][]> plotArray = null;
    public long simulate(String fmuFileName, double endTime, double stepSize,
                         boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {
        return 0L;
    }
    public List<State> simulateOne(ArrayList<FMU> fmuList, String fmuFileName, HashMap IO, double endTime, double stepSize,
                                   boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {

        List<State> statesList = new ArrayList<>();
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
        for(int i=0;i<fmiModelDescriptions.length;i++) {
            fmiModelDescriptions[i] = FMUFile
                    .parseFMUFile(fmuFileNameArray[i]);
        }
        // Load the shared library.
        NativeLibrary[] nativeLibrary = new NativeLibrary[FMUNumbers];
        for(int i = 0;i<FMUNumbers;i++)
            nativeLibrary[i] = NativeLibrary.getInstance(FMUFile.fmuSharedLibrary(fmiModelDescriptions[i]));

        // The URL of the fmu file.
        String[] fmuLocations = new String[FMUNumbers];
        for(int i=0;i<FMUNumbers;i++)
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
        for(int i=0;i<FMUNumbers;i++) {
            if (fmiModelDescriptions[i] != null) {
                fmiModelDescriptions[i].dispose();
            }
        }

        for(int i=0;i<FMUNumbers;i++) {
            Function instantiateSlave = nativeLibrary[i].getFunction("fmi2Instantiate");
            fmiComponent[i] = (Pointer) instantiateSlave.invoke(Pointer.class,
                    new Object[]{fmiModelDescriptions[i].modelIdentifier, fmiModelDescriptions[i].guid,
                            fmuLocations[i], callbacks, visible, loggingOn});
            if (fmiComponent[i].equals(Pointer.NULL)) {
                throw new RuntimeException("Could not instantiate model.");
            }
        }
        double startTime = 0;
        for(int i=0;i<FMUNumbers;i++) {
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
        }
        try {
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            double time = startTime;
            Long start = System.currentTimeMillis();
            //store variable references
            HashMap<String,FMIScalarVariable> FMUVariableReferences = new HashMap<>();
            for(int i=0;i < fmiModelDescriptions.length;i++){
                FMIModelDescription fmd = fmiModelDescriptions[i];
                for(FMIScalarVariable fsv : fmd.modelVariables) {
                    FMUVariableReferences.put(i + "."+fmd.modelName + "." + fsv.valueReference, fsv);
                }
            }
            Pointer[] currentSystemState = new Pointer[FMUNumbers];
            for(int i=0;i<FMUNumbers;i++){
                Pointer csf = null;
                Function getFMUstate = nativeLibrary[i].getFunction("fmi2GetFMUstate");
                csf = (Pointer) getFMUstate.invoke(Pointer.class, new Object[]{fmiComponent[i]});
                if(csf!= null){
                    currentSystemState[i] = csf;
                }else
                    System.err.println("cant's retrieve state of the FMU"+fmuList.get(i).getFMUName());
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
            Pointer[] previousState = new Pointer[FMUNumbers];
            while (time < endTime) {
//                printAllinLine(fmiModelDescriptions,fmiComponent);
//                State current_state = new State();
//                current_state.SetTime(current_Time);
                Long[] timeConsumptionQue = new Long[3];
                //updating IO using simultaneous method
                printSelected(time,fmiModelDescriptions,fmiComponent);
//                printAllinLine(fmiModelDescriptions,fmiComponent);
                IODependenciesUpdating(IO,FMUVariableReferences, fmiComponent);
                //store current state;
//                printSelected(time,fmiModelDescriptions,fmiComponent);
                for(int i=0;i<FMUNumbers;i++){
                    Pointer csf = null;
                    Function getFMUstate = nativeLibrary[i].getFunction("fmi2GetFMUstate");
                    csf = (Pointer) getFMUstate.invoke(Pointer.class, new Object[]{fmiComponent[i]});
                    if(csf!= null){
                        currentSystemState[i] = csf;
                    }else
                        System.err.println("cant's retrieve state of the FMU"+fmuList.get(i).getFMUName());
                }
//                printSelected(time,fmiModelDescriptions,fmiComponent);
                double fmuStatus = 0;
                //call dostep for FMU of Cs
                for (int i = 0; i < FMUNumbers; i++) {
                    nativeLibrary[i].getFunction("fmi2DoStep").invoke(Double.class, new Object[]{fmiComponent[i], time, stepSize,
                            (byte) 1});
                    fmuStatus = 1;
                    if(fmuStatus>=2)
                        break;
                }

                // partial step reversion
                if(fmuStatus>=2){
                    //roll back these
                    rollbackFMU(previousState, FMUNumbers,fmiComponent,nativeLibrary);
                    stepSize = stepSize/2;
//                        printAllinLine(fmiModelDescriptions,fmiComponent);
                }
                previousState = currentSystemState;
                time += stepSize;
                stepSize = maxStepSize;
            }
            System.out.println("dostep time"+ (System.currentTimeMillis()-doStep_time));

            // Don't throw an exception while freeing a slave.  Some
            // fmiTerminateSlave calls free the slave for us.
            for(int i=0;i<FMUNumbers;i++) {
                Function freeSlave = nativeLibrary[i].getFunction("fmi2FreeInstance");
                int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
                        new Object[]{fmiComponent[i]})).intValue();
                if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
                    new Exception("Warning: Could not free slave instance: " + fmiFlag)
                            .printStackTrace();
                }
            }
            System.out.println("simulate origin:"+(System.currentTimeMillis()-start)+"revision time:"+ step_revision_time+"revision number:"+ revision_num);

            return statesList;
        } finally {
////            if (file != null) {
////                file.close();
//            }
            for(int i=0;i<FMUNumbers;i++) {
                if (fmiModelDescriptions[i] != null) {
                    fmiModelDescriptions[i].dispose();
                }
            }
        }
    }
    public void IODependenciesUpdating(HashMap IODependencies ,HashMap<String,FMIScalarVariable> FMUVariableReferences, Pointer[] fmiComponent ){
        Iterator it = IODependencies.entrySet().iterator();
        int index = 0;
        //psrma的思路是先更新一部分io,然后Cd dostep ,然后更新部分io,然后Cerr dostep, 然后剩下的
        while(it.hasNext()){
            Map.Entry  entry = (Map.Entry)it.next();
            String variableTarget = (String)entry.getKey();
            String variableSource = (String)entry.getValue();
            FMIScalarVariable target = FMUVariableReferences.get(variableTarget);
            FMIScalarVariable source = FMUVariableReferences.get(variableSource);
            if(source!=null && target != null) {
//                        System.out.println(index++);
                target.setDouble(fmiComponent[Integer.parseInt(variableTarget.split("\\.")[0])],
                        source.getDouble(fmiComponent[Integer.parseInt(variableSource.split("\\.")[0])]));
            }
        }
    }
    public void rollbackFMU(Pointer[] currentSystemState, int FMUNumbers,Pointer[] fmiComponent,NativeLibrary[] nativeLibrary){
        for(int i = 0; i<FMUNumbers;i++) {
            Function setFMUstate = nativeLibrary[i].getFunction("fmi2SetFMUstate");
            setFMUstate.invoke(Double.class, new Object[]{fmiComponent[i],fmiComponent[i]});
        }
    }
    public void printAllinLine(FMIModelDescription[] fmiModelDescriptions,Pointer[] fmiComponent){
        String line = "";
        for(int i=0;i< fmiModelDescriptions.length; i++) {
            FMIModelDescription fmiModelDescription = fmiModelDescriptions[i];
            List<FMIScalarVariable> fmd = fmiModelDescriptions[i].modelVariables;
            for(int j=0;j<fmd.size();j++){
                line +=(fmiModelDescription.modelName+fmd.get(j).name+fmd.get(0).getDouble(fmiComponent[i])+", ");
            }
        }
        System.out.println(line);
    }
    public void printSelected(double time, FMIModelDescription[] fmiModelDescriptions,Pointer[] fmiComponent){
        System.out.println(time/60+","+fmiModelDescriptions[0].modelVariables.get(0).getDouble(fmiComponent[0])+",    "+
                        fmiModelDescriptions[0].modelVariables.get(1).getDouble(fmiComponent[0])
                        +",    "+fmiModelDescriptions[1].modelVariables.get(0).getDouble(fmiComponent[1])
                        +",    "+fmiModelDescriptions[1].modelVariables.get(1).getDouble(fmiComponent[1])
//                        +",    "+fmiModelDescriptions[2].modelVariables.get(0).getDouble(fmiComponent[2])
//                        +",    "+fmiModelDescriptions[3].modelVariables.get(0).getDouble(fmiComponent[3])
//                        +",    "+fmiModelDescriptions[6].modelVariables.get(0).getDouble(fmiComponent[6])
//                        +",    "+fmiModelDescriptions[7].modelVariables.get(0).getDouble(fmiComponent[7])
//                        +",    "+fmiModelDescriptions[8].modelVariables.get(0).getDouble(fmiComponent[8])
//                        +",    "+fmiModelDescriptions[9].modelVariables.get(0).getDouble(fmiComponent[9])
                        +",    "+fmiModelDescriptions[5].modelVariables.get(0).getDouble(fmiComponent[5])
                        +",    "+fmiModelDescriptions[5].modelVariables.get(1).getDouble(fmiComponent[5])
                        +",    "+fmiModelDescriptions[5].modelVariables.get(2).getDouble(fmiComponent[5])
                        +",    "+fmiModelDescriptions[5].modelVariables.get(3).getDouble(fmiComponent[5])
                        +",    "+fmiModelDescriptions[5].modelVariables.get(4).getDouble(fmiComponent[5])
                        +",    "+fmiModelDescriptions[5].modelVariables.get(5).getDouble(fmiComponent[5])

        );
    }
    public enum LineChartTitle{
        dataExchangeSR,storeCurrentSR,doStepSR,
        dataExchangeNew,storeCurrentNew,doStepNew,
    }
}
