package ecnu.modana.FmiDriver;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import ecnu.modana.util.MyLineChart;

///////////////////////////////////////////////////////////////////
////FMUCoSimulation
public class MyCoSimulation extends FMUDriver {

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
// public static void main(String[] args) throws Exception {
// 	for(String str:args) 
// 		System.err.println(str);
//
//     FMUDriver._processArgs(args);
//     new MyCoSimulation().simulate(_fmuFileName, _endTime, _stepSize,
//             _enableLogging, _csvSeparator, _outputFileName);
// }

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
 public MyLineChart simulate(String fmuFileName, double endTime, double stepSize,
         boolean enableLogging, char csvSeparator, String outputFileName)
         throws Exception {
     // Avoid a warning from FindBugs.
     FMUDriver._setEnableLogging(enableLogging);

     // Parse the .fmu file.
     FMIModelDescription fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

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
     FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
		new FMULibrary.FMULogger(), fmiModelDescription.getFMUAllocateMemory(),
             new FMULibrary.FMUFreeMemory(),
             new FMULibrary.FMUStepFinished());
     // Logging tends to cause segfaults because of vararg callbacks.
     byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;
     loggingOn = (byte) 0;

     Function instantiateSlave = getFunction("_fmiInstantiateSlave");
     Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
             new Object[] { _modelIdentifier, fmiModelDescription.guid,
                     fmuLocation, mimeType, timeout, visible, interactive,
                     callbacks, loggingOn });
     if (fmiComponent.equals(Pointer.NULL)) {
         throw new RuntimeException("Could not instantiate model.");
     }

     double startTime = 0;

     invoke("_fmiInitializeSlave", new Object[] { fmiComponent, startTime,
             (byte) 1, endTime }, "Could not initialize slave: ");

     File outputFile = new File(outputFileName);
     PrintStream file = null;
     MyLineChart myLineChart=new MyLineChart();
     try {
	    // gcj does not have this constructor
         //file = new PrintStream(outputFile);
         file = new PrintStream(outputFileName);
         if (enableLogging) {
             System.out.println("FMUCoSimulation: about to write header");
         }
         // Generate header row
         OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
                 fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
         // Output the initial values.
         OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
         		fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);
         // Loop until the time is greater than the end time.
         double time = startTime;

         List<Object> timeList=new ArrayList<>();
         List<Number> hNumberList=new ArrayList<Number>();
         List<Number>vNumberList=new ArrayList<>();
         List<Number>alterVNumberList=new ArrayList<>();         
         
         Function doStep = getFunction("_fmiDoStep");
         while (time < endTime)
         {
             if (enableLogging) {
                 System.out.println("FMUCoSimulation: about to call "
                         + _modelIdentifier
                         + "_fmiDoStep(Component, /* time */ " + time
                         + ", /* stepSize */" + stepSize + ", 1)");
             }
//             double v=(double)GetValue(fmiModelDescription, "v", fmiComponent);
//             if(v<=-4)
//             {
//             	double tv=-v;
//             	SetValue(fmiModelDescription, "v", fmiComponent, tv);
//             }
             invoke(doStep, new Object[] { fmiComponent, time, stepSize,
                     (byte) 1 }, "Could not simulate, time was " + time
                     + ": ");
             time += stepSize;
             // Generate a line for this step
             OutputRow.outputRowIni(_nativeLibrary, fmiModelDescription,
                     fmiComponent, time, file, csvSeparator, Boolean.FALSE);
             timeList.add(time);
             OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "h", hNumberList);
             OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "v", vNumberList);
         }
//         time=0;
//         while (time < endTime)
//         {
//           double v=(double)GetValue(fmiModelDescription, "v", fmiComponent);
//           if(v<=-4)
//           {
//           	double tv=4;
//           	SetValue(fmiModelDescription, "v", fmiComponent, tv);
//           }
//             invoke(doStep, new Object[] { fmiComponent, time, stepSize,
//                     (byte) 1 }, "Could not simulate, time was " + time
//                     + ": ");
//             time += stepSize;
//             // Generate a line for this step
//             OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
//                     fmiComponent, time, file, csvSeparator, Boolean.FALSE);
//             OutputRow.AddRow(this, fmiModelDescription, fmiComponent, "v", alterVNumberList);
//         }
         myLineChart=new MyLineChart();
         myLineChart.SetX(timeList, "Time");
         myLineChart.SetY(hNumberList, "h");
         myLineChart.SetY(vNumberList, "v");
         //myLineChart.SetY(alterVNumberList, "v'");
         //new Thread(myLineChart).start();
         //myLineChart.start(new Stage());
         //myLineChart.start(new Stage());
//         System.err.println(numberList);
//         myLineChart.start(new Stage());
         
	    invoke("_fmiTerminateSlave", new Object[] { fmiComponent },
		   "Could not terminate slave: ");

	    // Don't throw an exception while freeing a slave.  Some
	    // fmiTerminateSlave calls free the slave for us.
	    Function freeSlave = getFunction("_fmiFreeSlaveInstance");
	    int fmiFlag = ((Integer) freeSlave.invoke(Integer.class,
						      new Object[] { fmiComponent })).intValue();
	    if (fmiFlag >= FMILibrary.FMIStatus.fmiWarning) 
	    {
	    	//new Exception("Warning: Could not free slave instance: " + fmiFlag).printStackTrace();
	    	System.err.println("Warning: Could not free slave instance: " + fmiFlag);
	    }
     } finally {
         if (file != null) {
             file.close();
         }
	    if (fmiModelDescription != null) {
		fmiModelDescription.dispose();
	    }
	}

     if (enableLogging) {
         System.out.println("Results are in "
                 + outputFile.getCanonicalPath());
	    System.out.flush();
     }
     return myLineChart;
 }
}
