package ecnu.modana.alsmc.util;


public class UserFile {
	//---------------file path prefix--------------------
	
	//public static String pathPrefix = "C:/Users/cb219/Desktop/temp/";
	
	public static String pathPrefix = "traceFolder\\";
	
	//-----------------------------------------------------
	//-----------------------------------------------------
	//--------------------Uppaal file--------------------
	
	/*public static String verifytaPath = "D:\\Progra~1\\uppaal-4.1.19\\bin-Win32\\verifyta";
	public static String modelPath = "C:\\Users\\cb219\\Desktop\\2016硕士论文-程贝\\model\\2room.xml";
	public static String queryPath = "C:\\Users\\cb219\\Desktop\\2016硕士论文-程贝\\model\\room.q";*/
	
	public static String verifytaPath = "uppaal-4.1.19\\bin-Win32\\verifyta";
	
	/*public static String modelPath = "C:\\Users\\JKQ\\Desktop\\model\\2room.xml";
	public static String queryPath = "C:\\Users\\JKQ\\Desktop\\model\\room.q";*/
	
/*	public static String modelPath = "C:/Users/JKQ/Desktop/model/bluetooth.cav.xml";
	public static String queryPath = "C:\\Users\\JKQ\\Desktop\\model\\bluetooth.cav.q";*/
	
	public static String markovPath = null;
	public static String fmuPath = null;
	public static String praNameList = null;
	/**
	 * smartbuilding P=?[F discomfort>=15 ]
	 */

//	public static String properties = "P=?[F energy>=2200 ]";
	public static String properties = null;
	
	//------------------------state doubleNum intNum    -----------------------------
	
	/**
	 * smartbuilding 15/43
	 * robot 4/9
	 * energy 2/6
	 */
	public static int stateDoubleNum = 0;
	public static int stateIntNum = 0;
	//------------------------BIET k c    -----------------------------
	public static double bietK = 0.1;
	public static double bietC = 0.9;
	//------------------------EXEuppaal LearnTraceNum extractTraceNum extractTraceProbability   -----------------------------
    public static int learnTraceNum = 50;
    public static int extractTraceNum = 2;
    public static double extractTraceProbability = 0.5;
    //------------------------PCAthreshold    -----------------------------
  	public static double PCAthreshold = 0.5;
    //------------------------PCAthreshold    -----------------------------
  	public static boolean fileSwitch = false;  //False:do not write file, True:write file. Files are used to analysis the trace  
}

