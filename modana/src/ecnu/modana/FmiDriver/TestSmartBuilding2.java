package ecnu.modana.FmiDriver;

import java.util.ArrayList;
import java.util.List;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestSmartBuilding2 {
    public double[] Train(double defaultStep,double trainingTime){
        Long start = System.currentTimeMillis();
        double[][] plot = new double[83][2];
        Room room = new Room(5);
        room.NewPath();
        HeaterController heaterController = new HeaterController(room);

        int i = 0;
        double current = 0.00;
        double stepSize = defaultStep;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<Double> t_room1 = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();
        t_room1.add(room.temperature);
        time.add(0.0);

        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(current > trainingTime)
                break;
            //data exchange
            if(rollback!=1) {
                heaterController.SetValues("temperatureRoom1",room.temperature);
                room.Room1switch = heaterController.Room1switch;
            }
            Double[] data = new Double[4];
            data[0] = room.temperature;
            data[1] = room.Room1switch;
            data[2] = stepSize;
            if(rollback != 1)
                heaterController.SetValues("temperatureRoom1",room.temperature);
            room.Room1switch = heaterController.Room1switch;
            double tempT = room.temperature;
            //            System.out.println(tempBB+ "   "+ tempFg + " stepSIze: "+stepSize);
            room.DoStep(current,stepSize);
            if(-1 !=  heaterController.DoStep(current,stepSize) ) {
                current += stepSize;
                stepSize = defaultStep;
                rollback = 0;
                time.add(current);
                data[3] = 1.0;
            }
            else {
                rollback = 1;
                stepSize *= (9.0/10.0);
                room.temperature = tempT;
                rollbackNum++;

                data[3] = 0.0;
            }
            dataSet.add(data);
        }
        double[][] doubleData = new double[dataSet.size()][3];
        double[] doublelable = new double[dataSet.size()];
        for(int t = 0;t<dataSet.size();t++){
            Double[] DoubleData = dataSet.get(t);
            for(int tempI=0;tempI< 3;tempI++){
                doubleData[t][tempI] = DoubleData[tempI];
            }
            doublelable[t] = DoubleData[3];
        }
        //逻辑回归获取权重向量
        LogProcess lp = new LogProcess();
        double[] weight = lp.train(doubleData,doublelable, 0.1f, 500, (short)1);
        Long stop = System.currentTimeMillis();
        System.out.println("training-rollback:"+ rollbackNum +", time cost:" + (stop-start));
        return weight;
    }
    public double[][] withLR(double[] weight,double defaultStep,double simuTime){
        Long start = System.currentTimeMillis();
        double current = 0.00;

        double stepSize = defaultStep;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        time.add(0.0);
        double w0 = weight[0];
        double w1 = weight[1];
        double w2 = weight[2];

        double[][] plot = new double[83][2];
        Room room = new Room(5);
        room.NewPath();
        HeaterController heaterController = new HeaterController(room);



        List<Double> t_room1 = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();
        t_room1.add(room.temperature);

        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(current > simuTime)
                break;
            //data exchange
            if(rollback!=1) {
                heaterController.SetValues("temperatureRoom1",room.temperature);
                room.Room1switch = heaterController.Room1switch;
            }
            Double[] data = new Double[4];
            data[0] = room.temperature;
            data[1] = room.Room1switch;
            data[2] = stepSize;



            double classifyResult = w0 * data[0]+
                    w1 * data[1];

            for(int i=2;i>0;i--){
                if(classifyResult + w2 * stepSize>0)
                    break;
//                System.out.println(classifyResult + "   "+ weight[6] * stepSize);
                stepSize = stepSize*(9.0/10.0);

            }
            double tempT = room.temperature;
            room.DoStep(current,stepSize);
            if(-1 !=  heaterController.DoStep(current,stepSize) ) {
                current += stepSize;
                t_room1.add(room.temperature);
                stepSize = defaultStep;
                rollback = 0;
                time.add(current);
                data[3] = 1.0;
            }
            else {
                rollback = 1;
                stepSize *= (9.0/10.0);
                room.temperature = tempT;
                rollbackNum++;
//                try {
//                    Thread.sleep(1);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
                data[3] = 0.0;
            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("with LR,rollback:"+ rollbackNum +", time cost:" + (stop-start));

        plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = t_room1.get(i);
        }
        return plot;


    }
    public double[][] withOutLR(){
        Long start = System.currentTimeMillis();
        WaterTank tank1 = new WaterTank(2.0,1.0,5.0,10.0,20.0);
        WaterTank tank2 = new WaterTank(1.0,0.76,7.0,5.0,10.0);
        PipeController pipeController = new PipeController();

        double current = 0.00;

        double stepSize = 100.0;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<Double> height_tank1 = new ArrayList<Double>();
        List<Double> height_tank2 = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();

        height_tank1.add(tank1.h);
        height_tank2.add(tank2.h);
        time.add(0.0);

        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(current > 200)
                break;
            //data exchange
            if(rollback!=1) {
                pipeController.SetValues("Tank1Height",tank1.h);
                pipeController.SetValues("Tank2Height",tank2.h);
                tank1.SetValues("PipeIn", pipeController.Tank1PipeIn);
                tank1.SetValues("PipeOut", pipeController.Tank1PipeOut);
                tank2.SetValues("PipeIn", pipeController.Tank2PipeIn);
                tank2.SetValues("PipeOut", pipeController.Tank2PipeOut);
            }
            Double[] data = new Double[10];
            data[0] = tank1.GetValues("h");
            data[1] = tank2.GetValues("h");
            data[2] = pipeController.Tank1PipeIn;
            data[3] = pipeController.Tank1PipeOut;
            data[4] = pipeController.Tank2PipeIn;
            data[5] = pipeController.Tank2PipeOut;
            data[6] = tank1.PipeIn;
            data[7] = tank1.PipeOut;
            data[8] = tank2.PipeIn;
            data[9] = tank2.PipeOut;
            // stepsize prediction
//            double tempBB = BB.Predict();
//            double tempFg = fg.Predict(current);
//            if(rollback != 1)
//                stepSize = tempBB> tempFg ? tempFg : tempBB;
////            System.out.println(tempBB+ "   "+ tempFg + " stepSIze: "+stepSize);

            if(-1 != tank1.DoStep(current,stepSize) && -1 != tank2.DoStep(current,stepSize) &&
                    -1 != pipeController.DoStep(current,stepSize) ) {
//                BB.DoStep(current,stepSize);
//                fg.doStep(current,stepSize);
                current += stepSize;
                stepSize = 100.0;
                rollback = 0;
                time.add(current);
                height_tank1.add(tank1.h);
                height_tank2.add(tank2.h);

            }
            else {
                rollback = 1;
                stepSize *= (6.0/10.0);
                rollbackNum++;
                tank1.h = data[0];
                tank2.h = data[1];
                pipeController.Tank1PipeIn =  data[2];
                pipeController.Tank1PipeOut = data[3];
                pipeController.Tank2PipeIn= data[4];
                pipeController.Tank2PipeOut= data[5];
                tank1.PipeIn= data[6];
                tank1.PipeOut= data[7];
                tank2.PipeIn= data[8];
                tank2.PipeOut= data[9];
            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("without LR,rollback:"+ rollbackNum +", time cost:" + (stop-start));
//        +
//                "eventNum :"+BB.eventNum+"   ,"+ BB2.eventNum+"    ," + fg.eventNum+"    ," + time.get(time.size()-1)
        double[][] plot = new double[time.size()][3];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = height_tank1.get(i);
            plot[i][2] = height_tank2.get(i);

        }
        return plot;
    }
    public double[][] fixedStep(double ss, double simutTime){
        Long start = System.currentTimeMillis();
        Room room = new Room(5);
        room.NewPath();
        HeaterController heaterController = new HeaterController(room);
        double current = 0.00;

        double stepSize = ss;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<Double> t_room1 = new ArrayList<Double>();
//        List<Double> height_ball2 = new ArrayList<Double>();
//        List<Double> effect = new ArrayList<Double>();
//        List<Double> stepList = new ArrayList<Double>();

        t_room1.add(room.temperature);
        time.add(0.0);
        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(current>simutTime)
                break;
            //data exchange
            if(rollback!=1) {
                heaterController.SetValues("temperatureRoom1",room.temperature);
                room.Room1switch = heaterController.Room1switch;
            }
            double tempT = room.temperature;
            //  System.out.println(" stepSIze: "+stepSize+"current index"+ room.temperature);
            room.DoStep(current,stepSize);
            if(-1 !=  heaterController.DoStep(current,stepSize)) {
                current += stepSize;
                rollback = 0;
                stepSize = ss;
                time.add(current);
                t_room1.add(room.temperature);
                index++;
            }
            else {
                rollback = 1;
                stepSize *= (9.0/10.0);
                room.temperature = tempT;
//                try {
//                    Thread.sleep(1);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

                rollbackNum++;

            }
        }
        Long stop = System.currentTimeMillis();
        System.out.println("Fixed stepSize,rollback:"+ rollbackNum +", time cost:" + (stop-start));
        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = t_room1.get(i);
        }
        return plot;
    }
    public static void main(String[] args){
        TestSmartBuilding tsb = new TestSmartBuilding();
        Long start = System.currentTimeMillis();
        double[][] plot_t_room1 = tsb.withLR(tsb.Train(1,48),1,1000);
        System.out.println(System.currentTimeMillis()-start);
        Long start2 = System.currentTimeMillis();
        double[][] plot_t_room2 = tsb.fixedStep(0.05,1000);
        System.out.println(System.currentTimeMillis()-start2);
//        JavaPlot jp = new JavaPlot();
//        DataSetPlot dsp1 = new DataSetPlot(plot_t_room1);
//        DataSetPlot dsp2 = new DataSetPlot(plot_t_room2);
//        jp.addPlot(dsp1);
//        jp.addPlot(dsp2);
//        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINES);
//        ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Tank 1 height");
//        ((AbstractPlot)(jp.getPlots().get(1))).setTitle("Tank 2 height");
//        ((AbstractPlot)(jp.getPlots().get(1))).getPlotStyle().setStyle(Style.LINES);
//        jp.plot();

    }
}
