package ecnu.oe.cosimulationMaster.testCase;


import com.sun.jna.NativeLibrary;
import ecnu.oe.cosimulationMaster.ma.PSRMA;
import ecnu.oe.cosimulationMaster.model.FMU;
import ecnu.oe.cosimulationMaster.utils.IODependencyClassifier;
import ecnu.oe.cosimulationMaster.utils.KeyFMUsExtractor;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * @Author： oe
 * @Description:Comparison between step revision and srp MA  algorithm
 * @Created by oe on 2018/3/26.
 */
public class SmartBuildingTestBand2 {
    public static ArrayList<double[][]> plotArray = new ArrayList();
    public static void main(String[] args) throws Exception {

        double errorBound = 1e-5;
        if(19 - 18.96469702 > 1e-5)
            System.out.println(false);
        Long SRPMAStart = System.currentTimeMillis();
        String filePath = "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\Zone1.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\Zone2.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\Radiator1.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\Radiator2.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\mMixer.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\Controller.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\Boiler.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\AHUheatingCoil.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\AHUSupplyAir.fmu,"+
                "E:\\fmusdk\\fmu20\\fmu\\cs\\随机版本\\OutAir.fmu";
        String[] fileList = filePath.split(",");

        ArrayList<FMU> fmuList = new ArrayList<FMU>();
        for(int i=0;i<fileList.length;i++){
            FMU fmu = new FMU();
            String s = fileList[i];
            fmu.setFMUName(s.substring(s.lastIndexOf("\\")+1,s.indexOf(".")));
            fmu.setFMUAddress(s);
            fmuList.add(fmu);
        }
        for(int i=0;i<fmuList.size();i++) {
            if (i == 5)
                fmuList.get(i).setType(FMU.FMUType.de);
            else
                fmuList.get(i).setType(FMU.FMUType.ct);
        }
        HashMap<String,String> dataExchange_map = new HashMap<>();
        //input output
        dataExchange_map.put("5.Controller.4","0.zone1.0");///////////////
        dataExchange_map.put("5.Controller.5","1.zone2.0");////////////////
//
        dataExchange_map.put("2.radiator1.4","5.Controller.2");//////////////
        dataExchange_map.put("3.radiator2.4","5.Controller.3");/////////
        dataExchange_map.put("6.boiler.6","5.Controller.8");///////////

        dataExchange_map.put("7.AHUHeatingCoil.4","5.Controller.0");////////////
        dataExchange_map.put("8.AHUSupplyAir.4","5.Controller.1");//////////////

//      混合气  dataExchange_map.put("4.mMixer.1","5.Controller.1");
        dataExchange_map.put("4.mMixer.2","0.zone1.0");/////////////
        dataExchange_map.put("4.mMixer.3","1.zone2.0");//////////

        dataExchange_map.put("8.AHUSupplyAir.5","4.mMixer.0");/////////////
        dataExchange_map.put("8.AHUSupplyAir.6","0.zone1.0");//////////////

        dataExchange_map.put("2.radiator1.5","6.boiler.0");//////////
        dataExchange_map.put("2.radiator1.6","0.zone1.0");////////////////

        dataExchange_map.put("3.radiator2.5","6.boiler.0");/////////
        dataExchange_map.put("3.radiator2.6","1.zone2.0");////////////////

        dataExchange_map.put("7.AHUHeatingCoil.5","6.boiler.0");/////////////
        dataExchange_map.put("7.AHUHeatingCoil.6","0.zone1.0");/////////////
        dataExchange_map.put("7.AHUHeatingCoil.7","1.zone2.0");//////////////

        dataExchange_map.put("0.zone1.20","8.AHUSupplyAir.0");///////////////////
        dataExchange_map.put("0.zone1.21","2.radiator1.0");//////////////////

        dataExchange_map.put("1.zone2.20","8.AHUSupplyAir.0");////////////////
        dataExchange_map.put("1.zone2.21","3.radiator2.0");////////////
        dataExchange_map.put("0.zone1.22","9.OutAir.1");/////////////
        dataExchange_map.put("1.zone2.22","9.OutAir.1");//////////////////
        HashMap ExtractedFMUSets = KeyFMUsExtractor.extracts(fmuList,dataExchange_map);
        HashMap[] ClassifiedIO = IODependencyClassifier.classify((int[])ExtractedFMUSets.get("error"),dataExchange_map);
        PSRMA psrma = new PSRMA(false, false, false);
        psrma.simulateOne(fmuList, dataExchange_map,filePath,  3000*15,15,
                false, ',',  null);
//        SRMA srma = new SRMA(false, false, false);
//        srma.simulateOne(fmuList, filePath,  3000*15,15, false, ',', null);
        Long newTotal = System.currentTimeMillis()-SRPMAStart;
//        StepRevisionMasterAlgorithm SRMA  = new StepRevisionMasterAlgorithm(true, false, false);
        Long SRMAStart = System.currentTimeMillis();



//        storeTraceToCSV(0,2);
    }
//    public static void storeTraceToCSV(int data1, int data2){
//        String storePath = "C://data.csv";
//        CsvWriter sw = new CsvWriter(storePath);
//        String SRAMA001 = "C://SRAMA0.01.csv";
//        CsvWriter cw1 = new CsvWriter(SRAMA001);
//        String SRMA001 = "C://SRMA0.01.csv";
//        CsvWriter cw2 = new CsvWriter(SRMA001);
//        String SRMA0001 = "C://SRMA0.001.csv";
//        CsvWriter cw3 = new CsvWriter(SRMA0001);
//        double[][] srpmaData =  plotArray.get(data1);
//        double[][] srmaData = plotArray.get(data2);
//        double[][] minStepSizeTrace = plotArray.get(5);
//        try {
//            int rowNumber = 0;
//            double RMSD_SRP = 0.0;
//            double RMSD_SR = 0.0;
//            String[] header = {"srma0.01","srpma0.01", "srma0.00001","T1","T2","T3","DeviationSRMA","DeviationSRPMA"};
//            sw.writeRecord(header);
//            for (int i = 0, j = 0,k = 0; i < srpmaData.length && j < srmaData.length &&
//                    k<minStepSizeTrace.length; ) {
//                if(i< srpmaData.length){
//                    String[] srpma001Content = {Double.toString(srpmaData[i][0]),Double.toString(srpmaData[i][1])};
//                    cw1.writeRecord(srpma001Content);
//                }
//                if(j< srpmaData.length){
//                    String[] srma001Content = {Double.toString(srmaData[j][0]),Double.toString(srmaData[j][1])};
//                    cw2.writeRecord(srma001Content);
//                }
//                if(k< srpmaData.length){
//                    String[] srma0001Content = {Double.toString(srpmaData[k][0]),Double.toString(srpmaData[k][1])};
//                    cw3.writeRecord(srma0001Content);
//                }
//
//                if(Math.abs(srpmaData[i][0] - srmaData[j][0]) <= 0.01 &&
//                        Math.abs(srpmaData[i][0]-minStepSizeTrace[k][0])<=0.01 &&
//                        Math.abs(srpmaData[i][0]-minStepSizeTrace[k][0]) <= 0.01){
//                    rowNumber++;
//                    RMSD_SRP += Math.pow(srpmaData[i][1] - minStepSizeTrace[k][1],2);
//                    RMSD_SR += Math.pow(srmaData[j][1] - minStepSizeTrace[k][1],2);
//                    String[]  contents = {Double.toString(srpmaData[i][0]),
//                            Double.toString(srmaData[j][0]),
//                            Double.toString(minStepSizeTrace[k][0]),
//                            Double.toString(srpmaData[i][1]),
//                            Double.toString(srmaData[j][1]),
//                            Double.toString(minStepSizeTrace[k][1]),
//                            Double.toString((minStepSizeTrace[k][1]-srmaData[j][1])/minStepSizeTrace[k][1]*10),
//                            Double.toString((minStepSizeTrace[k][1]-srpmaData[i][1])/minStepSizeTrace[k][1]*10)
//                    };
//                    sw.writeRecord(contents);
//                    i++;j++;k++;
//                }else{
//                    if(minStepSizeTrace[k][0]< srpmaData[i][0] || minStepSizeTrace[k][0] < srmaData[j][0])
//                        k++;
//                    else if(srpmaData[i][0] < srmaData[j][0])
//                        i++;
//                    else
//                        j++;
//                    continue;
//                }
//            }
//            sw.close();
//            cw1.close();
//            cw2.close();
//            cw3.close();
//            System.out.println("srp residual:"+Math.sqrt(RMSD_SRP/rowNumber)+"sr residual:"+Math.sqrt(RMSD_SR/rowNumber));
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
}

//srp residual:0.11534997294355277sr residual:0.3434463312201757    0.01 1e-5
//srp residual:0.11534729758333018sr residual:0.34338331610358386   0.01 1e-6
//srp residual:0.11558890995196991sr residual:0.3438390576200933    0.01 1e-7

//srp residual:0.16445012134208914sr residual:0.5911230371268424    0.02 1e-6
//srp residual:0.37852897583563705sr residual:0.8505977456657333    0.03 1e-6
//srp residual:0.34310235302194747sr residual:0.9939075540330153    0.04 1e-6
