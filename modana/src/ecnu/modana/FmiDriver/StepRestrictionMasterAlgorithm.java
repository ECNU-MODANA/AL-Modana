//package ecnu.modana.FmiDriver;
//
//import com.sun.jna.Function;
//import com.sun.jna.NativeLibrary;
//import com.sun.jna.Pointer;
//
//import ecnu.modana.alsmc.main.State;
//
//
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//
//import ecnu.oe.ptolemy.fmi.*;
//
///**
// * @Authorï¼? oe
// * @Description:
// * @Created by oe on 2018/3/1.
// */
//public class StepRestrictionMasterAlgorithm extends ecnu.oe.ptolemy.fmi.driver.FMUDriver{
//    private final boolean showResult;
//    private final boolean showTimeConsumption;
//    private final boolean showPartion;
//    public StepRestrictionMasterAlgorithm(boolean sr, boolean stc, boolean sp){
//        showResult = sr;
//        showTimeConsumption = stc;
//        showPartion = sp;
//    }
//
//    //
//    /** Perform co-simulation using the named Functional Mock-up Unit (FMU) file.
//     *  @param fmuFileName The pathname of the co-simulation .fmu file
//     *  @param endTime The ending time in seconds.
//     *  @param stepSize The step size in seconds.
//     *  @param enableLogging True if logging is enabled.
//     *  @param csvSeparator The character used for separating fields.
//     *  Note that sometimes the decimal point in floats is converted to ','.
//     *  @param outputFileName The output file.
//     *  @exception Exception If there is a problem parsing the .fmu file or invoking
//     *  the methods in the shared library.
//     */
//    public static ArrayList<double[][]> plotArray = null;
//    public long simulate(String fmuFileName, double endTime, double stepSize,
//                         boolean enableLogging, char csvSeparator, String outputFileName)
//            throws Exception {
//        return 0L;
//    }
//
//
//    public List<State> simulateOne(String fmuFileName, double endTime, double stepSize,
//                                                          boolean enableLogging, char csvSeparator, String outputFileName)
//            throws Exception {
//        List<State> statesList = new ArrayList<>();
//        List<Long[]> simulateTime_analyse = new ArrayList<>();
//        Long data_exchange = 0L;
//        Long store_currentState = 0L;
//        Long do_step = 0L;
//        List<double[][]> dataSet = new ArrayList<double[][]>();
//        double maxStepSize = stepSize;
//        Long step_total_time = 0L;
//        // Avoid a warning from FindBugs.
//        FMUDriver._setEnableLogging(enableLogging);
//        HashMap<String,String> dataExchange_map = new HashMap<>();
//        dataExchange_map.put("0.smartBuilding_roomA.3","1.smartBuilding_roomB.0");
//        dataExchange_map.put("1.smartBuilding_roomB.4","0.smartBuilding_roomA.0");
//        // Parse the .fmu file.
//        String[] fmuFileNameArray = fmuFileName.split(",");
//        int FMUNumbers = fmuFileNameArray.length;
//        FMIModelDescription[] fmiModelDescriptions = new FMIModelDescription[fmuFileNameArray.length];
//        for(int i=0;i<fmiModelDescriptions.length;i++)
//            fmiModelDescriptions[i] = FMUFile
//                .parseFMUFile(fmuFileNameArray[i]);
//        // Load the shared library.
//        NativeLibrary[] nativeLibrary = new NativeLibrary[2];
//        for(int i = 0;i<FMUNumbers;i++)
//            nativeLibrary[i] = NativeLibrary.getInstance(FMUFile.fmuSharedLibrary(fmiModelDescriptions[i]));
//
//        // The URL of the fmu file.
//        String fmuLocation = new File(fmuFileName.split(",")[0]).toURI().toURL().toString();
//        // The tool to use if we have tool coupling.
//        String mimeType = "application/x-fmu-sharedlibrary";
//        // Timeout in ms., 0 means wait forever.
//        double timeout = 1000;
//        // There is no simulator UI.
//        byte visible = 0;
//        // Run the simulator without user interaction.
//        byte interactive = 0;
//        double current_Time = 0;double hcA_current_temperature = 0;double hcA_current_switch = 0;
//        boolean hcA_current_strategy = false;double hcB_current_temperature = 0;double hcB_current_switch = 0;
//        boolean hcB_current_strategy = false;boolean rj_current_strategyA = false;boolean rj_current_strategyB = false;
//        boolean rj_current_triggered = false;
//        // Callbacks
//        FMICallbackFunctions.ByReference callbacks = new FMICallbackFunctions.ByReference(
//                new FMULibrary.FMULogger(), new FMULibrary.FMUAllocateMemory(),
//                new FMULibrary.FMUFreeMemory(),
//                new FMULibrary.FMUStepFinished(),
//                new FMULibrary.FMUComponentEnvironment());
//        // Logging tends to cause segfaults because of vararg callbacks.
//        byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;
//        loggingOn = (byte) 0;
//        Pointer[] fmiComponent = new Pointer[nativeLibrary.length];
//        for(int i=0;i<FMUNumbers;i++) {
//            Function instantiateSlave = nativeLibrary[i].getFunction("fmi2Instantiate");
//            fmiComponent[i] = (Pointer) instantiateSlave.invoke(Pointer.class,
//                    new Object[]{fmiModelDescriptions[i].modelIdentifier, fmiModelDescriptions[i].guid,
//                            fmuLocation, callbacks, visible, loggingOn});
//            if (fmiComponent[i].equals(Pointer.NULL)) {
//                throw new RuntimeException("Could not instantiate model.");
//            }
//        }
//
//        double startTime = 0;
//        for(int i=0;i<FMUNumbers;i++) {
//            Function setupExperiment = nativeLibrary[i].getFunction("fmi2SetupExperiment");
//            Double fmi2Flag = (Double) setupExperiment.invoke(Double.class, new Object[]{fmiComponent[i], (byte) 1, 2, startTime,
//                    (byte) 1, endTime});
////            System.err.println(fmi2Flag);
//
//            Function enterInitializationMode = nativeLibrary[i].getFunction("fmi2EnterInitializationMode");
//            fmi2Flag = (Double) enterInitializationMode.invoke(Double.class, new Object[]{fmiComponent[i]});
////            System.err.println(fmi2Flag);
//
//            Function exitInitializationMode = nativeLibrary[i].getFunction("fmi2ExitInitializationMode");
//            fmi2Flag = (Double) exitInitializationMode.invoke(Double.class, new Object[]{fmiComponent[i]});
////            System.err.println(fmi2Flag);
//        }
//
//
////        File outputFile = new File(outputFileName);
////        PrintStream file = null;
//        try {
//            // gcj does not have this constructor
//            //file = new PrintStream(outputFile);
//            if (enableLogging) {
//                System.out.println("FMUCoSimulation: about to write header");
//            }
//            double time = startTime;
//
//            Long start = System.currentTimeMillis();
//            FMIScalarVariable temperature_roomA = fmiModelDescriptions[0].modelVariables.get(0);
//            FMIScalarVariable temperature_roomB = fmiModelDescriptions[1].modelVariables.get(0);
//            HeaterController heaterController_roomA = new HeaterController(temperature_roomA.getDouble(fmiComponent[0]));
//            HeaterController heaterController_roomB = new HeaterController(temperature_roomB.getDouble(fmiComponent[1]));
//            FMIScalarVariable heaterSwitch_roomA = fmiModelDescriptions[0].modelVariables.get(2);
//            FMIScalarVariable heaterSwitch_roomB = fmiModelDescriptions[1].modelVariables.get(3);
//            RandomGenerator randomGenerator = new RandomGenerator(System.currentTimeMillis());
//            int badStepNum = 0;
//            HashMap<String,FMIScalarVariable> FMUVariableReferences = new HashMap<>();
//            HashMap<String,Object> CurrentVariableValues = new HashMap<>();
//            for(int i=0;i < fmiModelDescriptions.length;i++){
//                FMIModelDescription fmd = fmiModelDescriptions[i];
//                for(FMIScalarVariable fsv : fmd.modelVariables) {
//                    FMUVariableReferences.put(i + "."+fmd.modelName + "." + fsv.valueReference, fsv);
//                    CurrentVariableValues.put(i + "."+fmd.modelName + "." + fsv.valueReference,null);
//                }
//            }
//            List<Long> doStep_timeList = new ArrayList<>();
//            while (time < endTime) {
////                if(time >9.42) {
////                    System.out.println("");
////                    System.out.println("");System.out.println("");
////                }
//                State current_state = new State();
//                Long[] timeConsumptionQue = new Long[3];
//                Long starts = System.currentTimeMillis();
//                Long dataExchange_start = System.currentTimeMillis();
//                if (enableLogging) {
//                    System.out.println("FMUCoSimulation: about to call "
//                            + _modelIdentifier
//                            + "_fmiDoStep(Component, /* time */ " + time
//                            + ", /* stepSize */" + stepSize + ", 1)");
//                }
//                //data exchange
//                heaterController_roomA.strategy_Room1 = randomGenerator.strategy_RoomA;
//                heaterController_roomB.strategy_Room1 = randomGenerator.strategy_RoomB;
//                heaterController_roomA.temperatureRoom1 = temperature_roomA.getDouble(fmiComponent[0]);
//                heaterController_roomB.temperatureRoom1 = temperature_roomB.getDouble(fmiComponent[1]);
//                Iterator<String> it = dataExchange_map.keySet().iterator();
//                while(it.hasNext()){
//                    String variableTarget = it.next();
//                    String variableSource = dataExchange_map.get(variableTarget);
//                    FMIScalarVariable target = FMUVariableReferences.get(variableTarget);
//                    FMIScalarVariable source = FMUVariableReferences.get(variableSource);
//                    if(source!=null && target != null) {
//                        target.setDouble(fmiComponent[Integer.parseInt(variableTarget.split("\\.")[0])],
//                                source.getDouble(fmiComponent[Integer.parseInt(variableSource.split("\\.")[0])]));
////                        }
//                    }
//                }
//                heaterSwitch_roomA.setDouble(fmiComponent[0], heaterController_roomA.Room1switch);
//                heaterSwitch_roomB.setDouble(fmiComponent[1], heaterController_roomB.Room1switch);
//                data_exchange += System.currentTimeMillis()-dataExchange_start;
//                timeConsumptionQue[0] = data_exchange;
//                Long storeCurrent_start = System.currentTimeMillis();
//                current_Time = time;
//                //store current states
//                it = CurrentVariableValues.keySet().iterator();
//                while(it.hasNext()){
//                    String variableTarget = it.next();
//                    FMIScalarVariable target = FMUVariableReferences.get(variableTarget);
//                    if(target != null) {
//                        switch (target.variability) {
//                            default:System.out.println("chushile--------------------------------"+variableTarget);
//                                break;
//                            case continuous:
//                                CurrentVariableValues.put(variableTarget,target.getDouble(fmiComponent[Integer.parseInt(variableTarget.split("\\.")[0])]));break;
//                        }
//                    }
//                }
//
//                hcA_current_temperature = heaterController_roomA.temperatureRoom1;
//                hcA_current_switch = heaterController_roomA.Room1switch;
//                hcA_current_strategy = heaterController_roomA.strategy_Room1;
//                hcB_current_temperature = heaterController_roomB.temperatureRoom1;
//                hcB_current_switch = heaterController_roomB.Room1switch;
//                hcB_current_strategy = heaterController_roomB.strategy_Room1;
//                rj_current_strategyA = randomGenerator.strategy_RoomA;
//                rj_current_strategyB = randomGenerator.strategy_RoomB;
//                rj_current_triggered = randomGenerator.triggered;
//                store_currentState += System.currentTimeMillis()-storeCurrent_start;
//                timeConsumptionQue[1] = store_currentState;
//                //store dataset
//
//                double[] data_roomA = new double[3];
//                double[] data_roomB = new double[4];
//                data_roomA[0] = temperature_roomA.getDouble(fmiComponent[0]);
//                data_roomB[0] = temperature_roomB.getDouble(fmiComponent[1]);
//                data_roomA[1] = data_roomB[1] = time;
//                data_roomA[2] = heaterController_roomA.Room1switch;
//                data_roomB[2] = heaterController_roomB.Room1switch;
//                data_roomB[3] = fmiModelDescriptions[1].modelVariables.get(2).getDouble(fmiComponent[1]);
//                ArrayList<Object> valueList = new ArrayList<>();
//                valueList.add(data_roomA[0]);
//                valueList.add(data_roomB[0]);
//                valueList.add(data_roomB[3]);
//                current_state.SetValues(valueList);
//                String[] tempArray = {"t_roomA","t_roomB","energyRoomA"};
//                current_state.valueNames = tempArray;
////                System.out.println(data[2]+"------------"+data[0]);
////                double predictedStepSize = randomGenerator.predictStepSize(current_Time);
////                stepSize = stepSize<predictedStepSize? stepSize:predictedStepSize;
//                Long doStep_start = System.currentTimeMillis();
//                Function roomA_DoStep = nativeLibrary[0].getFunction("fmi2DoStep");
//                Function roomB_DoStep = nativeLibrary[1].getFunction("fmi2DoStep");
//                while(true){
//                    boolean tempTriggered = randomGenerator.triggered;
//                    double randomGeneratorFlag = randomGenerator.DoStep(time,stepSize);
//                    roomA_DoStep.invoke(Double.class, new Object[]{fmiComponent[0], time, stepSize,
//                            (byte) 1});
//                    roomB_DoStep.invoke(Double.class, new Object[]{fmiComponent[1], time, stepSize,
//                            (byte) 1});
//                    heaterController_roomA.pre = heaterController_roomA.temperatureRoom1;
//                    heaterController_roomA.temperatureRoom1 = temperature_roomA.getDouble(fmiComponent[0]);
//                    double controllerFlag_roomA = heaterController_roomA.DoStep(time, stepSize);
//                    heaterController_roomB.pre = heaterController_roomB.temperatureRoom1;
//                    heaterController_roomB.temperatureRoom1 = temperature_roomB.getDouble(fmiComponent[1]);
//                    double controllerFlag_roomB = heaterController_roomB.DoStep(time, stepSize);
////                    System.out.println(randomGenerator.triggered+"=---"+time+"----"+heaterController_roomA.strategy_Room1+"---"+stepSize);
//                    if(controllerFlag_roomB == -1||controllerFlag_roomA == -1 || randomGeneratorFlag == -1){
//                        heaterController_roomA.setRoom1switch(hcA_current_switch,hcA_current_temperature,hcA_current_strategy);
//                        heaterController_roomB.setRoom1switch(hcB_current_switch,hcB_current_temperature,hcB_current_strategy);
//                        it = FMUVariableReferences.keySet().iterator();
//                        while(it.hasNext()){
//                            String name = it.next();
////                        System.out.println(name);
//                            FMUVariableReferences.get(name).setDouble(fmiComponent[Integer.parseInt(name.split("\\.")[0])],
//                                    (double)CurrentVariableValues.get(name));
//                        }
//                        randomGenerator.strategy_RoomA = rj_current_strategyA;
//                        randomGenerator.strategy_RoomB = rj_current_strategyB;
//                        randomGenerator.triggered = rj_current_triggered;
//                        heaterController_roomA.temperatureRoom1 = temperature_roomA.getDouble(fmiComponent[0]);
//                        heaterController_roomB.temperatureRoom1 = temperature_roomB.getDouble(fmiComponent[1]);
//                        stepSize *= 0.5;
//                        continue;
//                    }else
//                        break;
//
//                }
//                    time += stepSize;
//                double[][] data = new double[2][];
//                data[0] = data_roomA;
//                data[1] = data_roomB;
//                dataSet.add(data);
//                    stepSize = maxStepSize;
//                do_step += System.currentTimeMillis()- doStep_start;
//                timeConsumptionQue[2] = do_step;
//                    step_total_time+= System.currentTimeMillis()-starts;
//                    doStep_timeList.add(step_total_time);
//                simulateTime_analyse.add(timeConsumptionQue);
//                statesList.add(current_state);
//            }
//
////            invoke("fmi2Terminate", new Object[] { fmiComponent },
////                    "Could not terminate slave: ",RoomA_nativeLibrary);
//
//            // Don't throw an exception while freeing a slave.  Some
//            // fmiTerminateSlave calls free the slave for us.
//            Function freeSlave = nativeLibrary[0].getFunction("fmi2FreeInstance");
//            int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
//                    new Object[] { fmiComponent[0] })).intValue();
//            if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
//                new Exception("Warning: Could not free slave instance: " + fmiFlag)
//                        .printStackTrace();
//            }
////            System.out.println("delta:" +(System.currentTimeMillis()-start));
//            if(showResult) {
//                double[][] plot = new double[dataSet.size()][2];
//                for (int i = 0; i < dataSet.size(); i++) {
//                    plot[i][0] = dataSet.get(i)[0][1];
//                    plot[i][1] = dataSet.get(i)[0][0];
//                }
//                SmartBuildingTestBand.plotArray.add(plot);
//                double[][] plot_b = new double[dataSet.size()][2];
//                for (int i = 0; i < dataSet.size(); i++) {
//                    plot_b[i][0] = dataSet.get(i)[1][1];
//                    plot_b[i][1] = dataSet.get(i)[1][0];
//                }
//                SmartBuildingTestBand.plotArray.add(plot_b);
//            }
////            if(showTimeConsumption) {
////                double[][] time_plot = new double[doStep_timeList.size()][2];
////                for (int i = 0; i < doStep_timeList.size(); i++) {
////                    time_plot[i][0] = i;
////                    time_plot[i][1] = doStep_timeList.get(i);
////                }
////                plotArray.add(time_plot);
////            }
////            if(showPartion) {
////                for (int j = 0; j < 3; j++) {
////                    double[][] time_plot = new double[simulateTime_analyse.size()][2];
////                    for (int i = 0; i < simulateTime_analyse.size(); i++) {
////                        time_plot[i][0] = i;
////                        time_plot[i][1] = simulateTime_analyse.get(i)[j];
////                    }
////                    plotArray.add(time_plot);
////                }
////            }
//
//            return statesList;
//        } finally {
////            if (file != null) {
////                file.close();
////            }
//            if (fmiModelDescriptions[0] != null) {
//                fmiModelDescriptions[0].dispose();
//            }
//            if (fmiModelDescriptions[1] != null) {
//                fmiModelDescriptions[1].dispose();
//            }
//        }
//    }
//
//    public enum LineChartTitle{
//        dataExchangeSR,storeCurrentSR,doStepSR,
//        dataExchangeNew,storeCurrentNew,doStepNew,
//    }
//}
