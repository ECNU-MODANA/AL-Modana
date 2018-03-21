package ecnu.modana.FmiDriver;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.Style;
import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import ecnu.modana.FmiDriver.ptolemy.fmi.*;
import ecnu.modana.FmiDriver.ptolemy.fmi.driver.FMUDriver;
import ecnu.modana.alsmc.main.State;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/3/1.
 */
public class StepRevisionMasterAlgorithm extends FMUDriver{
    private final boolean showResult;
    private final boolean showTimeConsumption;
    private final boolean showPartion;
    public StepRevisionMasterAlgorithm(boolean sr, boolean stc, boolean sp){
        showResult = sr;
        showTimeConsumption = stc;
        showPartion = sp;
    }
    public static void main(String[] args) throws Exception {

        plotArray = new ArrayList<>();
        StepRevisionMasterAlgorithm fBeta  = new StepRevisionMasterAlgorithm(true, false, false);
//        FMUCoSimulationDelta fBeta  = new FMUCoSimulationDelta(true, false);
        double stepSize = 0.01;
        Long newTotal = 0L;
        Long originMATotal = 0L;
//        for(int i=0;i<1;i++)
//            originMATotal += fBeta.simulate("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomA.fmu,E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomB.fmu", 48,0.01,
//                false, _csvSeparator, _outputFileName);
//
//
        for(int i=0;i<1;i++)
            fBeta.simulateOne("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomA.fmu,E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomB.fmu", 48,0.05,
                false, ',', null);
        System.out.println("new:"+ newTotal/10+"origin:"+originMATotal/10);
            JavaPlot jp = new JavaPlot();
            for(int i=0;i<plotArray.size();i++) {
                DataSetPlot dsp1 = new DataSetPlot(plotArray.get(i));
                jp.addPlot(dsp1);
                ((AbstractPlot)(jp.getPlots().get(i))).getPlotStyle().setStyle(Style.LINES);
                ((AbstractPlot)(jp.getPlots().get(i))).setTitle("ff"+i);
                switch(i){
                    case 0: ((AbstractPlot)(jp.getPlots().get(i))).setTitle(LineChartTitle.dataExchangeSR.toString());break;
                    case 1: ((AbstractPlot)(jp.getPlots().get(i))).setTitle(LineChartTitle.storeCurrentSR.toString());break;
                    case 2: ((AbstractPlot)(jp.getPlots().get(i))).setTitle(LineChartTitle.doStepSR.toString());break;
                    case 3: ((AbstractPlot)(jp.getPlots().get(i))).setTitle(LineChartTitle.dataExchangeNew.toString());break;
                    case 4: ((AbstractPlot)(jp.getPlots().get(i))).setTitle(LineChartTitle.storeCurrentNew.toString());break;
                    case 5: ((AbstractPlot)(jp.getPlots().get(i))).setTitle(LineChartTitle.doStepNew.toString());break;
                    default:break;
                }
            }
//        DataSetPlot dsp1 = new DataSetPlot(plotArray.get(0));
//        jp.addPlot(dsp1);
//        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINES);
//        ((AbstractPlot)(jp.getPlots().get(0))).setTitle("ff"+0);
//        DataSetPlot dsp2 = new DataSetPlot(plotArray.get(2));
//        jp.addPlot(dsp2);
//        ((AbstractPlot)(jp.getPlots().get(1))).getPlotStyle().setStyle(Style.LINES);
//        ((AbstractPlot)(jp.getPlots().get(1))).setTitle("ff"+2);
            jp.plot();

    }
    //

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
    public List<State> simulateOne(String fmuFileName, double endTime, double stepSize,
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
        HashMap<String,String> dataExchange_map = new HashMap<>();
        dataExchange_map.put("0.smartBuilding_roomA.3","1.smartBuilding_roomB.0");
        dataExchange_map.put("1.smartBuilding_roomB.4","0.smartBuilding_roomA.0");
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        String[] fmuFileNameArray = fmuFileName.split(",");
        int FMUNumbers = fmuFileNameArray.length;
        FMIModelDescription[] fmiModelDescriptions = new FMIModelDescription[fmuFileNameArray.length];
        for(int i=0;i<fmiModelDescriptions.length;i++)
            fmiModelDescriptions[i] = FMUFile
                    .parseFMUFile(fmuFileNameArray[i]);
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
        double timeout = 1000;
        // There is no simulator UI.
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


//        File outputFile = new File(outputFileName);
//        PrintStream file = null;
        try {
            // gcj does not have this constructor
            //file = new PrintStream(outputFile);
//            file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            double time = startTime;
            Long start = System.currentTimeMillis();
            //store variable references
            HashMap<String,FMIScalarVariable> FMUVariableReferences = new HashMap<>();
            HashMap<String,Object> CurrentVariableValues = new HashMap<>();
            HashMap<String,Object> PreviousVariableValues = new HashMap<>();
            for(int i=0;i < fmiModelDescriptions.length;i++){
                FMIModelDescription fmd = fmiModelDescriptions[i];
                for(FMIScalarVariable fsv : fmd.modelVariables) {
                    FMUVariableReferences.put(i + "."+fmd.modelName + "." + fsv.valueReference, fsv);
                    CurrentVariableValues.put(i + "."+fmd.modelName + "." + fsv.valueReference,null);
                    PreviousVariableValues.put(i + "."+fmd.modelName + "." + fsv.valueReference,null);
                }
            }
            HeaterController heaterController_roomA = new HeaterController(1);
            HeaterController heaterController_roomB = new HeaterController(1);
            RandomGenerator randomGenerator = new RandomGenerator(System.currentTimeMillis());
            int badStepNum = 0;
            List<Double[][]> dataSet = new ArrayList<Double[][]>();
            //store previous states
            double pre_Time = 0;
            double h_p = 0;
            //hcA means heater_Controller_A
            double hcA_pre_temperature = 0;double hcA_pre_switch = 0;boolean hcA_pre_strategy = false;
            double hcB_pre_temperature = 0;double hcB_pre_switch = 0;boolean hcB_pre_strategy = false;
            boolean rj_pre_strategyA = false;boolean rj_pre_strategyB = false;boolean rj_pre_triggered = false;
            double current_Time = 0;double hcA_current_temperature = 0;double hcA_current_switch = 0;
            boolean hcA_current_strategy = false;double hcB_current_temperature = 0;double hcB_current_switch = 0;
            boolean hcB_current_strategy = false;boolean rj_current_strategyA = false;boolean rj_current_strategyB = false;
            boolean rj_current_triggered = false;
            Long doStep_time = System.currentTimeMillis();
            List<Long> doStep_timeList = new ArrayList<>();
            stepSize = 1e-5;
            while (time < endTime) {
                State current_state = new State();
                current_state.SetTime(current_Time);

                Long[] timeConsumptionQue = new Long[3];
                Long t1 = System.currentTimeMillis();
                Long starts = System.currentTimeMillis();
                if (enableLogging) {
                    System.out.println("FMUCoSimulation: about to call "
                            + _modelIdentifier
                            + "_fmiDoStep(Component, /* time */ " + time
                            + ", /* stepSize */" + stepSize + ", 1)");
                }
                //data exchange
                Iterator<String> it = dataExchange_map.keySet().iterator();
                while(it.hasNext()){
                    String variableTarget = it.next();
                    String variableSource = dataExchange_map.get(variableTarget);
                    FMIScalarVariable target = FMUVariableReferences.get(variableTarget);
                    FMIScalarVariable source = FMUVariableReferences.get(variableSource);
                    if(source!=null && target != null) {
                        target.setDouble(fmiComponent[Integer.parseInt(variableTarget.split("\\.")[0])],
                                source.getDouble(fmiComponent[Integer.parseInt(variableSource.split("\\.")[0])]));
                    }
                }
                heaterController_roomA.temperatureRoom1  = fmiModelDescriptions[0].modelVariables.get(0).getDouble(fmiComponent[0]);
                heaterController_roomB.temperatureRoom1 = fmiModelDescriptions[1].modelVariables.get(0).getDouble(fmiComponent[1]);
                heaterController_roomA.strategy_Room1 = randomGenerator.strategy_RoomA;
                heaterController_roomB.strategy_Room1 = randomGenerator.strategy_RoomB;
                fmiModelDescriptions[0].modelVariables.get(2).setDouble(fmiComponent[0],heaterController_roomA.Room1switch);
                fmiModelDescriptions[1].modelVariables.get(3).setDouble(fmiComponent[1],heaterController_roomB.Room1switch);
                dataExchangeTotalTime += System.currentTimeMillis()-t1;
                timeConsumptionQue[0] = dataExchangeTotalTime;
                //store current state;
                Long storeCurrent_start = System.currentTimeMillis();
                current_Time = time;
                it = CurrentVariableValues.keySet().iterator();
                while(it.hasNext()){
                    String variableTarget = it.next();
                    FMIScalarVariable target = FMUVariableReferences.get(variableTarget);
                    if(target != null) {
                        switch (target.variability) {
                            default:System.out.println("chushile--------------------------------"+variableTarget);
                                break;
                            case continuous:
                                CurrentVariableValues.put(variableTarget,target.getDouble(fmiComponent[Integer.parseInt(variableTarget.split("\\.")[0])]));break;
                        }
                    }
                }

                hcA_current_temperature = heaterController_roomA.temperatureRoom1;
                hcA_current_switch = heaterController_roomA.Room1switch;
                hcA_current_strategy = heaterController_roomA.strategy_Room1;
                hcB_current_temperature = heaterController_roomB.temperatureRoom1;
                hcB_current_switch = heaterController_roomB.Room1switch;
                hcB_current_strategy = heaterController_roomB.strategy_Room1;
                rj_current_strategyA = randomGenerator.strategy_RoomA;
                rj_current_strategyB = randomGenerator.strategy_RoomB;
                rj_current_triggered = randomGenerator.triggered;
                storeCurrentTotalTime +=System.currentTimeMillis()-storeCurrent_start;
                timeConsumptionQue[1] = storeCurrentTotalTime;
                //store dataset

                Double[] data_roomA = new Double[3];
                Double[] data_roomB = new Double[4];
                data_roomA[0] = fmiModelDescriptions[0].modelVariables.get(0).getDouble(fmiComponent[0]);
                data_roomB[0] = fmiModelDescriptions[1].modelVariables.get(0).getDouble(fmiComponent[1]);
                data_roomA[1] = data_roomB[1] = time;
                data_roomA[2] = heaterController_roomA.Room1switch;
                data_roomB[2] = heaterController_roomB.Room1switch;
                data_roomB[3] = fmiModelDescriptions[1].modelVariables.get(2).getDouble(fmiComponent[1]);

                Long doStep_start = System.currentTimeMillis();
                ArrayList<Object> valueList = new ArrayList<>();
                valueList.add(data_roomA[0]);
                valueList.add(data_roomB[0]);
                valueList.add(data_roomB[3]);
                current_state.SetValues(valueList);
                String[] tempArray = {"t_roomA","t_roomB","energyRoomA"};
                current_state.valueNames = tempArray;
                for(int i=0;i<nativeLibrary.length;i++){
                    nativeLibrary[i].getFunction("fmi2DoStep").invoke(Double.class, new Object[]{fmiComponent[i], time, stepSize,
                            (byte) 1});
                }
//                double predictedStepSize = randomGenerator.predictStepSize(current_Time);
//                stepSize = stepSize<predictedStepSize? stepSize:predictedStepSize;
                double controller_roomA_Flag = heaterController_roomA.DoStep(time, stepSize);
                double controller_roomB_Flag = heaterController_roomB.DoStep(time, stepSize);
                double rg_Flag = randomGenerator.DoStep(time, stepSize);
                if(controller_roomA_Flag==-1 || controller_roomB_Flag==-1 || rg_Flag==-1){
                    Long begin_stepRevision = System.currentTimeMillis();
                    if(time ==0) {
                        System.err.println("wrong from initial");
                        break;
                    }
                    //bad step size , need step size reversion
                    //step reversion
                    heaterController_roomA.setRoom1switch(hcA_pre_switch,hcA_pre_temperature,hcA_pre_strategy);
                    heaterController_roomB.setRoom1switch(hcB_pre_switch,hcB_pre_temperature,hcB_pre_strategy);
                    randomGenerator.setValues(rj_pre_strategyA,rj_pre_strategyB,rj_pre_triggered);
                    it = FMUVariableReferences.keySet().iterator();
                    while(it.hasNext()){
                        String name = it.next();
//                        System.out.println(name);
                        FMUVariableReferences.get(name).setDouble(fmiComponent[Integer.parseInt(name.split("\\.")[0])],
                                (double)PreviousVariableValues.get(name));
                    }
                    time = pre_Time;
                    badStepNum++;
                    stepSize = 0.5*h_p;
                    dataSet.remove(dataSet.size()-1);
                    revision_num++;
                    h_p = stepSize;
//                    step_revision_time += (System.currentTimeMillis()-begin_stepRevision);
//                    doStep_timeList.add(step_revision_time);
                    doStepTotalTime += System.currentTimeMillis()-doStep_start;
                    timeConsumptionQue[2] = doStepTotalTime;
//                    doStep_timeList.add(step_revision_time);
                    simulateTime_analyse.add(timeConsumptionQue);
                    statesList.remove(statesList.size()-1);
                    continue;
                }else {
                    //save the valid states
                    pre_Time = time;
                    //hcA means heater_Controller_A
                    Long start_store_pre = System.currentTimeMillis();
                    hcA_pre_temperature = hcA_current_temperature;
                    hcA_pre_switch = hcA_current_switch;
                    hcA_pre_strategy = hcA_current_strategy;
                    hcB_pre_temperature = hcB_current_temperature;
                    hcB_pre_switch = hcB_current_switch;
                    hcB_pre_strategy = hcB_current_strategy;
                    rj_pre_strategyA = rj_current_strategyA;
                    rj_pre_strategyB = rj_current_strategyB;
                    rj_pre_triggered = rj_current_triggered;
                    it = PreviousVariableValues.keySet().iterator();
                    while(it.hasNext()){
                        String name = it.next();
//                        System.out.println(name);
                        PreviousVariableValues.put(name,CurrentVariableValues.get(name));
                    }
                    time += stepSize;

                    Double[][] data = new Double[2][];
                    data[0] = data_roomA;
                    data[1] = data_roomB;
                    dataSet.add(data);
                    h_p = stepSize;
                    stepSize = maxStepSize;
//                    step_revision_time += (System.currentTimeMillis()-starts);
                    doStepTotalTime += System.currentTimeMillis()-doStep_start;
                    timeConsumptionQue[2] = doStepTotalTime;
//                    doStep_timeList.add(step_revision_time);
                    simulateTime_analyse.add(timeConsumptionQue);
                    statesList.add(current_state);
                }
            }
            System.out.println("dostep time"+ (System.currentTimeMillis()-doStep_time));

            // Don't throw an exception while freeing a slave.  Some
            // fmiTerminateSlave calls free the slave for us.
            Function freeSlave = nativeLibrary[0].getFunction("fmi2FreeInstance");
            int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
                    new Object[] { fmiComponent[0] })).intValue();
            if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
                new Exception("Warning: Could not free slave instance: " + fmiFlag)
                        .printStackTrace();
            }
            System.out.println("simulate origin:"+(System.currentTimeMillis()-start)+"revision time:"+ step_revision_time+"revision number:"+ revision_num);
            if(showResult) {
                double[][] plot = new double[dataSet.size()][2];
                for (int i = 0; i < dataSet.size(); i++) {
                    plot[i][0] = dataSet.get(i)[0][1];
                    plot[i][1] = dataSet.get(i)[0][0];
                }
                plotArray.add(plot);
                double[][] plot_b = new double[dataSet.size()][2];
                for (int i = 0; i < dataSet.size(); i++) {
                    plot_b[i][0] = dataSet.get(i)[1][1];
                    plot_b[i][1] = dataSet.get(i)[1][0];
                }
                plotArray.add(plot_b);
                plot_b = new double[dataSet.size()][2];
                for (int i = 0; i < dataSet.size(); i++) {
                    plot_b[i][0] = dataSet.get(i)[1][1];
                    plot_b[i][1] = dataSet.get(i)[1][3];
                }
                plotArray.add(plot_b);
            }
            if(showTimeConsumption) {
                double[][] time_plot = new double[doStep_timeList.size()][2];
                for (int i = 0; i < doStep_timeList.size(); i++) {
                    time_plot[i][0] = i;
                    time_plot[i][1] = doStep_timeList.get(i);
                }
                plotArray.add(time_plot);
            }
            if(showPartion) {
                for (int j = 0; j < 3; j++) {
                    double[][] time_plot = new double[simulateTime_analyse.size()][2];
                    for (int i = 0; i < simulateTime_analyse.size(); i++) {
                        time_plot[i][0] = i;
                        time_plot[i][1] = simulateTime_analyse.get(i)[j];
                    }
                    plotArray.add(time_plot);
                }
            }
            return statesList;
        } finally {
////            if (file != null) {
////                file.close();
//            }
            if (fmiModelDescriptions[0] != null) {
                fmiModelDescriptions[0].dispose();
            }
        }
    }

    public enum LineChartTitle{
        dataExchangeSR,storeCurrentSR,doStepSR,
        dataExchangeNew,storeCurrentNew,doStepNew,
    }
}
