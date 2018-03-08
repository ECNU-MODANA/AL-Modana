package ecnu.modana.FmiDriver.ptolemy.fmi.driver;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.Style;
import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import ecnu.modana.FmiDriver.HeaterController;
import ecnu.modana.FmiDriver.RandomGenerator;
import ecnu.modana.FmiDriver.Room;
import ecnu.modana.FmiDriver.ptolemy.fmi.*;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/3/1.
 */
public class FMUCoSimulationDelta extends FMUDriver{
    public static void main(String[] args) throws Exception {
//        FMUDriver._processArgs(args);
//        new FMUCoSimulation().simulate("F:\\ECNU\\AL-Modana\\modana\\src\\ecnu\\modana\\FmiDriver\\ptolemy\\fmi\\fmu2\\bouncingBall.fmu", 10, 0.01,
//                true, _csvSeparator, _outputFileName);
//        new FMUCoSimulation().simulate("G:\\Downloads\\fmusdk-linux\\fmu\\cs\\bouncingBall.fmu", 10, 0.01,
//                true, _csvSeparator, _outputFileName);
        plotArray = new ArrayList<>();
        FMUCoSimulationDelta fBeta  = new FMUCoSimulationDelta();

        double stepSize = 0.01;
//        for(int i = 0;i<5;i++)
//        fBeta.simulate("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuildingOrigin.fmu", 48, 0.001,
//                false, _csvSeparator, _outputFileName);
//            fBeta.simulate("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding.fmu", 48, 0.01,
//                    false, _csvSeparator, _outputFileName);
        fBeta.simulateDelta("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomA.fmu,E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomB.fmu", 48,0.01,
                false, _csvSeparator, _outputFileName);
            JavaPlot jp = new JavaPlot();
            for(int i=0;i<2;i++) {
                DataSetPlot dsp1 = new DataSetPlot(plotArray.get(i));
                jp.addPlot(dsp1);
                ((AbstractPlot)(jp.getPlots().get(i))).getPlotStyle().setStyle(Style.LINES);
//                ((AbstractPlot)(jp.getPlots().get(i))).setTitle(i==0 ? "step revision": "variable chaining");
                ((AbstractPlot)(jp.getPlots().get(i))).setTitle("ff"+i);
            }
//            System.out.println(badStepNum+"doStep time: "+(System.currentTimeMillis()-start)+","+FMULog.eventNum+","+ FMULog.total);
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
        double maxStepSize = stepSize;
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        FMIModelDescription fmiModelDescription = FMUFile
                .parseFMUFile(fmuFileName);

        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);

        if (enableLogging) {
            System.out.println("FMUCoSimulation: about to load "
                    + sharedLibrary);
        }
        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

        // The modelName may have spaces in it.
        _modelIdentifier = fmiModelDescription.modelIdentifier;

        // The URL of the fmu file.
        String fmuLocation = new File(fmuFileName).toURI().toURL().toString();
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

        Function instantiateSlave = getFunction("fmi2Instantiate");
//        Function instantiateSlave = getFunction("_fmiInstantiateSlave");
        Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
                new Object[] { _modelIdentifier, fmiModelDescription.guid,
                        fmuLocation, callbacks, visible, loggingOn });
        //c = fmu->instantiate(instanceName, fmi2CoSimulation, guid, fmuResourceLocation,
        //&callbacks, visible, loggingOn);
//        Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
//                new Object[] { _modelIdentifier, fmiModelDescription.guid,
//                        fmuLocation, mimeType, timeout, visible, interactive,
//                        callbacks, loggingOn });

        //         loggingOn   visible  callbacks  fmuLocation  guid   identifier
        //fmi1.0   char        char     struct 4     char*      char*  char*
        //fmi2.0   char        char     struct 5     char*      char*  char*
        //java
        if (fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException("Could not instantiate model.");
        }

        double startTime = 0;
        Function setupExperiment = getFunction("fmi2SetupExperiment");
        Double fmi2Flag = (Double)setupExperiment.invoke(Double.class, new Object[] { fmiComponent,(byte) 1,2, startTime,
                (byte) 1, endTime });
        System.err.println(fmi2Flag);

        Function enterInitializationMode = getFunction("fmi2EnterInitializationMode");
        fmi2Flag = (Double)enterInitializationMode.invoke(Double.class, new Object[] { fmiComponent});
        System.err.println(fmi2Flag);

        Function exitInitializationMode = getFunction("fmi2ExitInitializationMode");
        fmi2Flag = (Double)exitInitializationMode.invoke(Double.class, new Object[] { fmiComponent});
        System.err.println(fmi2Flag);



        File outputFile = new File(outputFileName);
        PrintStream file = null;
        try {
            // gcj does not have this constructor
            //file = new PrintStream(outputFile);
            file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            // Generate header row
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
            // Output the initial values.
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);
            // Loop until the time is greater than the end time.
            double time = startTime;

            Function doStep = getFunction("fmi2DoStep");
            Long start = System.currentTimeMillis();
            Room room = new Room(5);
            room.NewPath();

            FMIScalarVariable roomTemp = fmiModelDescription.modelVariables.get(0);
            HeaterController heaterController = new HeaterController(roomTemp.getDouble(fmiComponent));
            FMIScalarVariable heaterSwitch = fmiModelDescription.modelVariables.get(2);
            int badStepNum = 0;
            List<Double[]> dataSet = new ArrayList<Double[]>();

            double pre_Time = 0;
            double pre_t = 0;
            double pre_controller_switch = 0;
            double pre_room_switch = 0.0;
            boolean mark_revision = false;
            double h_p = 0;
            double current_Time = 0;
            double current_t = 0;
            double current_controller_switch = 0;
            double current_room_switch = 0.0;

            while (time < endTime) {
                if (enableLogging) {
                    System.out.println("FMUCoSimulation: about to call "
                            + _modelIdentifier
                            + "_fmiDoStep(Component, /* time */ " + time
                            + ", /* stepSize */" + stepSize + ", 1)");
                }



                //data exchange
                heaterController.SetValues("temperatureRoom1",roomTemp.getDouble(fmiComponent));
                heaterSwitch.setDouble(fmiComponent, heaterController.Room1switch);
                current_Time = time;
                current_t = roomTemp.getDouble(fmiComponent);
                current_controller_switch = heaterController.Room1switch;
                current_room_switch = heaterSwitch.getDouble(fmiComponent);
                //store dataset

                Double[] data = new Double[4];
                data[0] = roomTemp.getDouble(fmiComponent);
                data[1] = 0.0;
                data[2] = time;
                Function roomDoStep = getFunction("fmi2DoStep");
                stepSize = (Double) roomDoStep.invoke(Double.class, new Object[]{fmiComponent, time, stepSize,
                        (byte) 1});
                double controllerFlag = heaterController.DoStep(time, stepSize);
                if(controllerFlag==-1){
//                    System.out.println("chushile1");
                    mark_revision = true;
                    //bad step size , need step size reversion
                    //step reversion
                    heaterController.SetValues("temperatureRoom1",pre_t);
                    heaterSwitch.setDouble(fmiComponent, pre_room_switch);
                    roomTemp.setDouble(fmiComponent,pre_t);
                    time = pre_Time;
                    badStepNum++;
                    stepSize = 0.9*h_p;
                    dataSet.remove(dataSet.size()-1);
                    h_p = stepSize;
                    continue;
                }else {
                    heaterController.Room1switch = current_controller_switch;
                    heaterController.temperatureRoom1 = current_t;
                    heaterSwitch.setDouble(fmiComponent, current_room_switch);
                    roomTemp.setDouble(fmiComponent, current_t);
//                    Function setTime = getFunction("fmi2SetTime");
//                    fmi2Flag = (Double)setTime.invoke(Double.class, new Object[] { fmiComponent,current_Time});
                    //save the valid states
                    pre_Time = time;
                    pre_t = roomTemp.getDouble(fmiComponent);
                    pre_controller_switch = heaterController.Room1switch;
                    pre_room_switch = heaterSwitch.getDouble(fmiComponent);
                    heaterController.DoStep(time, stepSize);
                    double roomFlag = (Double) roomDoStep.invoke(Double.class, new Object[]{fmiComponent, time, stepSize,
                            (byte) 1});
                    time += stepSize;


                    // Generate a line for this step
//                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
                    StringBuffer printVariables = new StringBuffer("");

                    //store the "succeful step state";
//                    for (FMIScalarVariable variable : fmiModelDescription.modelVariables) {
//                        if (variable.type instanceof FMIRealType) {
//                            printVariables.append(variable.getDouble(fmiComponent));
//                            printVariables.append(", ");
//                        } else
//                            continue;
//                    }
//                    System.out.println(printVariables + "stepSIze:" + stepSize);
                    dataSet.add(data);
                    h_p = stepSize;
                    stepSize = maxStepSize;

                }
            }

            invoke("fmi2Terminate", new Object[] { fmiComponent },
                    "Could not terminate slave: ");

            // Don't throw an exception while freeing a slave.  Some
            // fmiTerminateSlave calls free the slave for us.
            Function freeSlave = getFunction("fmi2FreeInstance");
            int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
                    new Object[] { fmiComponent })).intValue();
            if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
                new Exception("Warning: Could not free slave instance: " + fmiFlag)
                        .printStackTrace();
            }

            double[][] plot = new double[dataSet.size()][2];
            for(int i=0;i<dataSet.size();i++){
                plot[i][0] = dataSet.get(i)[2];
                plot[i][1] = dataSet.get(i)[0];
            }
            plotArray.add(plot);
//
//            JavaPlot jp = new JavaPlot();
//            DataSetPlot dsp1 = new DataSetPlot(plot);
//            jp.addPlot(dsp1);
//            ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINES);
//            ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Tank 1 height");
//            System.out.println(badStepNum+"doStep time: "+(System.currentTimeMillis()-start)+","+FMULog.eventNum+","+ FMULog.total);
//            jp.plot();
            System.out.println("simulate origin:"+(System.currentTimeMillis()-start));
            return (System.currentTimeMillis()-start);
        } finally {
            if (file != null) {
                file.close();
            }
            if (fmiModelDescription != null) {
                fmiModelDescription.dispose();
            }
        }
    }

    public long simulateOrigin(String fmuFileName, double endTime, double stepSize,
                         boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {
        double maxStepSize = stepSize;
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        FMIModelDescription fmiModelDescription = FMUFile
                .parseFMUFile(fmuFileName);

        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);

        if (enableLogging) {
            System.out.println("FMUCoSimulation: about to load "
                    + sharedLibrary);
        }
        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

        // The modelName may have spaces in it.
        _modelIdentifier = fmiModelDescription.modelIdentifier;

        // The URL of the fmu file.
        String fmuLocation = new File(fmuFileName).toURI().toURL().toString();
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

        Function instantiateSlave = getFunction("fmi2Instantiate");
//        Function instantiateSlave = getFunction("_fmiInstantiateSlave");
        Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
                new Object[] { _modelIdentifier, fmiModelDescription.guid,
                        fmuLocation, callbacks, visible, loggingOn });
        //c = fmu->instantiate(instanceName, fmi2CoSimulation, guid, fmuResourceLocation,
        //&callbacks, visible, loggingOn);
//        Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
//                new Object[] { _modelIdentifier, fmiModelDescription.guid,
//                        fmuLocation, mimeType, timeout, visible, interactive,
//                        callbacks, loggingOn });

        //         loggingOn   visible  callbacks  fmuLocation  guid   identifier
        //fmi1.0   char        char     struct 4     char*      char*  char*
        //fmi2.0   char        char     struct 5     char*      char*  char*
        //java
        if (fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException("Could not instantiate model.");
        }

        double startTime = 0;
        Function setupExperiment = getFunction("fmi2SetupExperiment");
        Double fmi2Flag = (Double)setupExperiment.invoke(Double.class, new Object[] { fmiComponent,(byte) 1,2, startTime,
                (byte) 1, endTime });
        System.err.println(fmi2Flag);

        Function enterInitializationMode = getFunction("fmi2EnterInitializationMode");
        fmi2Flag = (Double)enterInitializationMode.invoke(Double.class, new Object[] { fmiComponent});
        System.err.println(fmi2Flag);

        Function exitInitializationMode = getFunction("fmi2ExitInitializationMode");
        fmi2Flag = (Double)exitInitializationMode.invoke(Double.class, new Object[] { fmiComponent});
        System.err.println(fmi2Flag);



        File outputFile = new File(outputFileName);
        PrintStream file = null;
        try {
            // gcj does not have this constructor
            //file = new PrintStream(outputFile);
            file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            // Generate header row
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
            // Output the initial values.
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);
            // Loop until the time is greater than the end time.
            double time = startTime;

            Function doStep = getFunction("fmi2DoStep");
            Long start = System.currentTimeMillis();
            Room room = new Room(5);
            room.NewPath();

            FMIScalarVariable roomTemp = fmiModelDescription.modelVariables.get(0);
            HeaterController heaterController = new HeaterController(roomTemp.getDouble(fmiComponent));
            FMIScalarVariable heaterSwitch = fmiModelDescription.modelVariables.get(2);
            int badStepNum = 0;
            List<Double[]> dataSet = new ArrayList<Double[]>();

            double pre_Time = 0;
            double pre_t = 0;
            double pre_controller_switch = 0;
            double pre_room_switch = 0.0;
            boolean mark_revision = false;
            double h_p = 0;
            double current_Time = 0;
            double current_t = 0;
            double current_controller_switch = 0;
            double current_room_switch = 0.0;

            while (time < endTime) {
                if (enableLogging) {
                    System.out.println("FMUCoSimulation: about to call "
                            + _modelIdentifier
                            + "_fmiDoStep(Component, /* time */ " + time
                            + ", /* stepSize */" + stepSize + ", 1)");
                }



                //data exchange
                heaterController.SetValues("temperatureRoom1",roomTemp.getDouble(fmiComponent));
                heaterSwitch.setDouble(fmiComponent, heaterController.Room1switch);
                current_Time = time;
                current_t = roomTemp.getDouble(fmiComponent);
                current_controller_switch = heaterController.Room1switch;
                current_room_switch = heaterSwitch.getDouble(fmiComponent);
                //store dataset

                Double[] data = new Double[4];
                data[0] = roomTemp.getDouble(fmiComponent);
                data[1] = 0.0;
                data[2] = time;
                double controllerFlag = heaterController.DoStep(time, stepSize);
                Function roomDoStep = getFunction("fmi2DoStep");
                double roomFlag = (Double) roomDoStep.invoke(Double.class, new Object[]{fmiComponent, time, stepSize,
                        (byte) 1});
                if(controllerFlag==-1){
                    mark_revision = true;
                    //bad step size , need step size reversion
                    //step reversion
                    heaterController.SetValues("temperatureRoom1",pre_t);
                    heaterSwitch.setDouble(fmiComponent, pre_room_switch);
                    roomTemp.setDouble(fmiComponent,pre_t);
                    time = pre_Time;
                    badStepNum++;
                    stepSize = 0.9*h_p;
                    dataSet.remove(dataSet.size()-1);
                    h_p = stepSize;
                    continue;
                }else {
                    heaterController.Room1switch = current_controller_switch;
                    heaterController.temperatureRoom1 = current_t;
                    heaterSwitch.setDouble(fmiComponent, current_room_switch);
                    roomTemp.setDouble(fmiComponent, current_t);
                    //save the valid states
                    pre_Time = time;
                    pre_t = roomTemp.getDouble(fmiComponent);
                    pre_controller_switch = heaterController.Room1switch;
                    pre_room_switch = heaterSwitch.getDouble(fmiComponent);


                    heaterController.DoStep(time, stepSize);
                    roomFlag = (Double) roomDoStep.invoke(Double.class, new Object[]{fmiComponent, time, stepSize,
                            (byte) 1});
                    time += stepSize;

                    // Generate a line for this step
//                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
//                    StringBuffer printVariables = new StringBuffer("");
//
//                    //store the "succeful step state";
//                    for (FMIScalarVariable variable : fmiModelDescription.modelVariables) {
//                        if (variable.type instanceof FMIRealType) {
//                            printVariables.append(variable.getDouble(fmiComponent));
//                            printVariables.append(", ");
//                        } else
//                            continue;
//                    }
//                    System.out.println(printVariables + "stepSIze:" + stepSize);
                    dataSet.add(data);
                    h_p = stepSize;


                    stepSize = maxStepSize;

                }
            }

            invoke("fmi2Terminate", new Object[] { fmiComponent },
                    "Could not terminate slave: ");

            // Don't throw an exception while freeing a slave.  Some
            // fmiTerminateSlave calls free the slave for us.
            Function freeSlave = getFunction("fmi2FreeInstance");
            int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
                    new Object[] { fmiComponent })).intValue();
            if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
                new Exception("Warning: Could not free slave instance: " + fmiFlag)
                        .printStackTrace();
            }

            double[][] plot = new double[dataSet.size()][2];
            for(int i=0;i<dataSet.size();i++){
                plot[i][0] = dataSet.get(i)[2];
                plot[i][1] = dataSet.get(i)[0];
            }
            plotArray.add(plot);
//
//            JavaPlot jp = new JavaPlot();
//            DataSetPlot dsp1 = new DataSetPlot(plot);
//            jp.addPlot(dsp1);
//            ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINES);
//            ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Tank 1 height");
//            jp.plot();
//            System.out.println(badStepNum+"doStep time: "+(System.currentTimeMillis()-start)+","+FMULog.eventNum+","+ FMULog.total)
            return (System.currentTimeMillis()-start);
        } finally {
            if (file != null) {
                file.close();
            }
            if (fmiModelDescription != null) {
                fmiModelDescription.dispose();
            }
        }

    }

    public long simulateDelta(String fmuFileName, double endTime, double stepSize,
                         boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {
        double maxStepSize = stepSize;
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
        NativeLibrary[] nativeLibrary = new NativeLibrary[2];
        for(int i = 0;i<FMUNumbers;i++)
            nativeLibrary[i] = NativeLibrary.getInstance(FMUFile.fmuSharedLibrary(fmiModelDescriptions[i]));

        // The URL of the fmu file.
        String fmuLocation = new File(fmuFileName.split(",")[0]).toURI().toURL().toString();
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
                            fmuLocation, callbacks, visible, loggingOn});
            if (fmiComponent[i].equals(Pointer.NULL)) {
                throw new RuntimeException("Could not instantiate model.");
            }
        }

        double startTime = 0;
        for(int i=0;i<FMUNumbers;i++) {
            Function setupExperiment = nativeLibrary[i].getFunction("fmi2SetupExperiment");
            Double fmi2Flag = (Double) setupExperiment.invoke(Double.class, new Object[]{fmiComponent[i], (byte) 1, 2, startTime,
                    (byte) 1, endTime});
            System.err.println(fmi2Flag);

            Function enterInitializationMode = nativeLibrary[i].getFunction("fmi2EnterInitializationMode");
            fmi2Flag = (Double) enterInitializationMode.invoke(Double.class, new Object[]{fmiComponent[i]});
            System.err.println(fmi2Flag);

            Function exitInitializationMode = nativeLibrary[i].getFunction("fmi2ExitInitializationMode");
            fmi2Flag = (Double) exitInitializationMode.invoke(Double.class, new Object[]{fmiComponent[i]});
            System.err.println(fmi2Flag);
        }


        File outputFile = new File(outputFileName);
        PrintStream file = null;
        try {
            // gcj does not have this constructor
            //file = new PrintStream(outputFile);
            file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            // Generate header row
//            OutputRow.outputRow(RoomA_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
//            // Output the initial values.
//            OutputRow.outputRow(RoomA_nativeLibrary, fmiModelDescription,
//                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);
            // Loop until the time is greater than the end time.
            double time = startTime;

            Long start = System.currentTimeMillis();
            Room room = new Room(5);
            room.NewPath();

            FMIScalarVariable temperature_roomA = fmiModelDescriptions[0].modelVariables.get(0);
            FMIScalarVariable A_needB = fmiModelDescriptions[0].modelVariables.get(3);
            FMIScalarVariable temperature_roomB = fmiModelDescriptions[1].modelVariables.get(0);
            FMIScalarVariable B_needA = fmiModelDescriptions[1].modelVariables.get(3);
            HeaterController heaterController_roomA = new HeaterController(temperature_roomA.getDouble(fmiComponent[0]));
            HeaterController heaterController_roomB = new HeaterController(temperature_roomB.getDouble(fmiComponent[1]));
            FMIScalarVariable heaterSwitch_roomA = fmiModelDescriptions[0].modelVariables.get(2);
            FMIScalarVariable heaterSwitch_roomB = fmiModelDescriptions[1].modelVariables.get(2);
            RandomGenerator randomGenerator = new RandomGenerator(10);
            int badStepNum = 0;
            List<Double[][]> dataSet = new ArrayList<Double[][]>();

            double pre_Time = 0;
            double pre_t = 0;
            double pre_controller_switch = 0;
            double pre_room_switch = 0.0;
            boolean mark_revision = false;
            double h_p = 0;
            double current_Time = 0;
            double current_t = 0;
            double current_controller_switch = 0;
            double current_room_switch = 0.0;

            while (time < endTime) {
                if (enableLogging) {
                    System.out.println("FMUCoSimulation: about to call "
                            + _modelIdentifier
                            + "_fmiDoStep(Component, /* time */ " + time
                            + ", /* stepSize */" + stepSize + ", 1)");
                }



                //data exchange
                heaterController_roomA.strategy_Room1 = randomGenerator.strategy_RoomA;
                heaterController_roomB.strategy_Room1 = randomGenerator.strategy_RoomB;

                heaterController_roomA.temperatureRoom1 = temperature_roomA.getDouble(fmiComponent[0]);
                heaterController_roomB.temperatureRoom1 = temperature_roomB.getDouble(fmiComponent[1]);
                A_needB.setDouble(fmiComponent[0],temperature_roomB.getDouble(fmiComponent[1]));
                B_needA.setDouble(fmiComponent[1],temperature_roomA.getDouble(fmiComponent[0]));
                heaterSwitch_roomA.setDouble(fmiComponent[0], heaterController_roomA.Room1switch);
                heaterSwitch_roomB.setDouble(fmiComponent[1], heaterController_roomB.Room1switch);
                current_Time = time;
                //store dataset

                Double[] data_roomA = new Double[3];
                Double[] data_roomB = new Double[3];
                data_roomA[0] = temperature_roomA.getDouble(fmiComponent[0]);
                data_roomB[0] = temperature_roomB.getDouble(fmiComponent[0]);
                data_roomA[1] = data_roomB[1] = time;
                data_roomA[2] = heaterController_roomA.Room1switch;
                data_roomB[2] = heaterController_roomB.Room1switch;
//                System.out.println(data[2]+"------------"+data[0]);
                Function roomDoStep = nativeLibrary[0].getFunction("fmi2DoStep");
                double controllerFlag = 0;
                while(true){
                    boolean tempTriggered = randomGenerator.triggered;
                    double randomGeneratorFlag = randomGenerator.DoStep(time,stepSize);
                    roomDoStep.invoke(Double.class, new Object[]{fmiComponent[0], time, stepSize,
                            (byte) 1});
                    heaterController_roomA.temperatureRoom1 = temperature_roomA.getDouble(fmiComponent[0]);
                    double controllerFlag_roomA = heaterController_roomA.DoStep(time, stepSize);
                    heaterController_roomB.temperatureRoom1 = temperature_roomB.getDouble(fmiComponent[1]);
                    double controllerFlag_roomB = heaterController_roomB.DoStep(time, stepSize);
                    System.out.println(randomGenerator.triggered+"=---"+time+"----"+heaterController_roomA.strategy_Room1+"---"+stepSize);
                    if(controllerFlag_roomB == -1||controllerFlag_roomA == -1 || randomGeneratorFlag == -1){
                        heaterController_roomA.Room1switch = data_roomA[2];
                        heaterController_roomB.Room1switch = data_roomB[2];
                        heaterController_roomA.temperatureRoom1 = data_roomA[0];
                        heaterController_roomB.temperatureRoom1 = data_roomB[0];
                        heaterSwitch_roomA.setDouble(fmiComponent[0], data_roomA[2]);
                        heaterSwitch_roomB.setDouble(fmiComponent[1], data_roomB[2]);
                        temperature_roomA.setDouble(fmiComponent[0], data_roomA[0]);
                        temperature_roomB.setDouble(fmiComponent[1], data_roomB[0]);
                        randomGenerator.triggered = tempTriggered;
                        stepSize *= 0.9;
                        continue;
                    }else
                        break;

                }
                    time += stepSize;
                Double[][] data = new Double[2][];
                data[0] = data_roomA;
                data[1] = data_roomB;
                    dataSet.add(data);
                    stepSize = maxStepSize;
//                    System.out.println(time);

            }

//            invoke("fmi2Terminate", new Object[] { fmiComponent },
//                    "Could not terminate slave: ",RoomA_nativeLibrary);

            // Don't throw an exception while freeing a slave.  Some
            // fmiTerminateSlave calls free the slave for us.
            Function freeSlave = nativeLibrary[0].getFunction("fmi2FreeInstance");
            int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
                    new Object[] { fmiComponent[0] })).intValue();
            if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) {
                new Exception("Warning: Could not free slave instance: " + fmiFlag)
                        .printStackTrace();
            }

            double[][] plot = new double[dataSet.size()][2];
            for(int i=0;i<dataSet.size();i++){
                plot[i][0] = dataSet.get(i)[0][1];
                plot[i][1] = dataSet.get(i)[0][0];
            }
            plotArray.add(plot);
            double[][] plot_b = new double[dataSet.size()][2];
            for(int i=0;i<dataSet.size();i++){
                plot_b[i][0] = dataSet.get(i)[1][1];
                plot_b[i][1] = dataSet.get(i)[1][0];
            }
            plotArray.add(plot_b);
//
//            JavaPlot jp = new JavaPlot();
//            DataSetPlot dsp1 = new DataSetPlot(plot);
//            jp.addPlot(dsp1);
//            ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINES);
//            ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Tank 1 height");
//            System.out.println(badStepNum+"doStep time: "+(System.currentTimeMillis()-start)+","+FMULog.eventNum+","+ FMULog.total);
//            jp.plot();
            System.out.println("delta:" +(System.currentTimeMillis()-start));
            return (System.currentTimeMillis()-start);
        } finally {
            if (file != null) {
                file.close();
            }
            if (fmiModelDescriptions[0] != null) {
                fmiModelDescriptions[0].dispose();
            }
        }
    }
}
