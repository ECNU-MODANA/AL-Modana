/* Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.

   Copyright (c) 2012 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

 */
package ecnu.modana.FmiDriver.ptolemy.fmi.driver;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import ecnu.modana.FmiDriver.HeaterController;
import ecnu.modana.FmiDriver.Room;
import ecnu.modana.FmiDriver.ptolemy.fmi.*;
import ecnu.modana.FmiDriver.ptolemy.fmi.type.FMIRealType;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


///////////////////////////////////////////////////////////////////
//// FMUCoSimulation

/** Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.
 *
 * <p>This file is based on fmusdk/src/model_exchange/fmusim_me/main.c
 * by Jakob Mauss, which has the following license:</p>
 *
 * <p>FMU SDK license</p>
 *
 * <p>Copyright (c) 2008-2011, QTronic GmbH. All rights reserved.
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <br>- Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * <br>- Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</p>
 *
 * <p>THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p>
 *
 * @author Christopher Brooks, based on fmusim_cs/main.c by Jakob Mauss
 * @version $Id: FMUCoSimulation.java 66026 2013-04-07 16:41:23Z cxh $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUCoSimulation extends FMUDriver {

    /** Perform co-simulation using the named Functional Mock-up Unit (FMU) file.
     *
     *  <p>Usage:</p>
     *  <pre>
     *  java -classpath ../../../lib/jna.jar:../../.. org.ptolemy.fmi.driver.FMUCoSimulation \
     *  file.fmu [endTime] [stepTime] [loggingOn] [csvSeparator] [outputFile]
     *  </pre>
     *  <p>For example, under Mac OS X or Linux:
     *  <pre>
     *  java -classpath $PTII/lib/jna.jar:${PTII} org.ptolemy.fmi.driver.FMUCoSimulation \
     *  $PTII/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu 1.0 0.1 true c foo.csv
     *  </pre>
     *
     *  <p>The command line arguments have the following meaning:</p>
     *  <dl>
     *  <dt>file.fmu</dt>
     *  <dd>The co-simulation Functional Mock-up Unit (FMU) file.  In FMI-1.0,
     *  co-simulation fmu files contain a modelDescription.xml file that
     *  has an &lt;Implementation&gt; element.  Model exchange fmu files do not
     *  have this element.</dd>
     *  <dt>endTime</dt>
     *  <dd>The endTime in seconds, defaults to 1.0.</dd>
     *  <dt>stepTime</dt>
     *  <dd>The time between steps in seconds, defaults to 0.1.</dd>
     *  <dt>enableLogging</dt>
     *  <dd>If "true", then enable logging.  The default is false.</dd>
     *  <dt>separator</dt>
     *  <dd>The comma separated value separator, the default value is
     *  ',', If the separator is ',', columns are separated by ',' and
     *  '.' is used for floating-point numbers.  Otherwise, the given
     *  separator (e.g. ';' or '\t') is to separate columns, and ','
     *  is used as decimal dot in floating-point numbers.
     *  <dt>outputFile</dt>
     *  <dd>The name of the output file.  The default is results.csv</dd>
     *  </dl>
     *
     *  <p>The format of the arguments is based on the fmusim command from the fmusdk
     *  by QTronic Gmbh.</p>
     *
     *  @param args The arguments: file.fmu [endTime] [stepTime]
     *  [loggingOn] [csvSeparator] [outputFile]
     *  @exception Exception If there is a problem parsing the .fmu file or invoking
     *  the methods in the shared library.
     */

    public static void main(String[] args) throws Exception {
//        FMUDriver._processArgs(args);
//        new FMUCoSimulation().simulate("F:\\ECNU\\AL-Modana\\modana\\src\\ecnu\\modana\\FmiDriver\\ptolemy\\fmi\\fmu2\\bouncingBall.fmu", 10, 0.01,
//                true, _csvSeparator, _outputFileName);
//        new FMUCoSimulation().simulate("G:\\Downloads\\fmusdk-linux\\fmu\\cs\\bouncingBall.fmu", 10, 0.01,
//                true, _csvSeparator, _outputFileName);
        FMUCoSimulationBeta fBeta  = new FMUCoSimulationBeta();
        double timeBeta = 0.0;
        for(int i = 0;i<5;i++)
            timeBeta+= fBeta.simulate("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding.fmu", 48, 0.05,
                false, _csvSeparator, _outputFileName);
        FMUCoSimulation fOrigin  = new FMUCoSimulation();
        double time = 0.0;
        for(int i = 0;i<5;i++)
            time+= fOrigin.simulate("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuildingOrigin.fmu", 48, 0.05,
                    false, _csvSeparator, _outputFileName);
        System.out.println("fmiOrigin:"+time/10+ ",,,,,FmiBeta"+ timeBeta/10);
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
                    time += stepSize;

                    heaterController.DoStep(time, stepSize);
                    roomFlag = (Double) roomDoStep.invoke(Double.class, new Object[]{fmiComponent, time, stepSize,
                            (byte) 1});


                    // Generate a line for this step
//                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
                    StringBuffer printVariables = new StringBuffer("");

                    //store the "succeful step state";
                    for (FMIScalarVariable variable : fmiModelDescription.modelVariables) {
                        if (variable.type instanceof FMIRealType) {
                            printVariables.append(variable.getDouble(fmiComponent));
                            printVariables.append(", ");
                        } else
                            continue;
                    }
                    System.out.println(printVariables + "stepSIze:" + stepSize);
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

//            double[][] plot = new double[dataSet.size()][2];
//            for(int i=0;i<dataSet.size();i++){
//                plot[i][0] = dataSet.get(i)[2];
//                plot[i][1] = dataSet.get(i)[0];
//            }
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
}
