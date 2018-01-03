package ecnu.modana.FmiDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/12/24.
 */
public class TestWaterTank {
    public double[] Train(){
        Long start = System.currentTimeMillis();
        double[][] plot = new double[83][2];
        WaterTank tank1 = new WaterTank(2.0,1.0,5.0,10.0,20.0);
        WaterTank tank2 = new WaterTank(1.0,0.76,7.0,5.0,10.0);
        PipeController pipeController = new PipeController();

        int i = 0;
        double current = 0.00;
        double stepSize = 0.3;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<String> height_ball1 = new ArrayList<String>();
        List<String> height_ball2 = new ArrayList<String>();
        List<Double> effect = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();


        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(current > 250)
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
            Double[] data = new Double[12];
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
            data[10] = stepSize;
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
                stepSize = 10.0;
                rollback = 0;
                time.add(current);
                data[11] = 1.0;
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
                data[11] = 0.0;
            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("training-rollback:"+ rollbackNum +", time cost:" + (stop-start));

        double[][] doubleData = new double[dataSet.size()][10];
        double[] doublelable = new double[dataSet.size()];
        for(int t = 0;t<dataSet.size();t++){
            Double[] DoubleData = dataSet.get(t);
            for(int tempI=0;tempI< 6;tempI++){
                doubleData[t][tempI] = DoubleData[tempI];
            }
            doublelable[t] = DoubleData[11];
        }
        //逻辑回归获取权重向量
        LogProcess lp = new LogProcess();
        double[] weight = lp.train(doubleData,doublelable, 0.1f, 200, (short)1);


        return weight;
    }
    public double[][] withLR(double[] weight){
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
        time.add(0.0);
        height_tank1.add(tank1.h);
        height_tank2.add(tank2.h);
        double w0 = weight[0];
        double w1 = weight[1];
        double w2 = weight[2];
        double w3 = weight[3];
        double w4 = weight[4];
        double w5 = weight[5];
        double w6 = weight[6];


        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(current > 1000)
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
            Double[] data = new Double[12];
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
            data[10] = stepSize;


            double classifyResult = w0 * data[0]+
                    w1 * data[1]+
                    w2 * data[2]+
                    w3 * data[3]+
                    w4 * data[4]+
                    w5 * data[5];
            for(int i=2;i>0;i--){
                if(classifyResult + w6 * stepSize>0)
                    break;
//                System.out.println(classifyResult + "   "+ weight[6] * stepSize);
                stepSize = stepSize*(6.0/10.0);

            }
            if(-1 != tank1.DoStep(current,stepSize) && -1 != tank2.DoStep(current,stepSize) &&
                    -1 != pipeController.DoStep(current,stepSize) ) {
                current += stepSize;
                stepSize = 100.0;
                rollback = 0;
                height_tank1.add(tank1.h);
                height_tank2.add(tank2.h);
                index++;
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
        }
        Long stop = System.currentTimeMillis();
        System.out.println("with LR,rollback:"+ rollbackNum +", time cost:" + (stop-start)+"likelyhood:"+(double)index/(double)(index+rollbackNum));

        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = height_tank1.get(i);
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
            if(current > 2000)
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
//                effect.add(fg.getValues("effect"));
//                stepList.add(stepSize);
//                index++;
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
    public double[][] fixedStep(int seed1,int seed2,int length){
        Long start = System.currentTimeMillis();
        BBallPlug1 BB = new BBallPlug1(4,0,-0.8);
        BBallPlug1 BB2 = new BBallPlug1(7,2,-0.8);
        double[][] timeForceQue = new double[length][3];
        Random selectBall = new Random(seed1);
        Random selectEffect = new Random(seed2);
        for(int i =0;i<length;i++){
            timeForceQue[i][0] = i+1;
            timeForceQue[i][1] = i%2+1;
            timeForceQue[i][2] = selectEffect.nextInt(5);
        }
        ForceGeneratorPlug fg = new ForceGeneratorPlug(timeForceQue);
        double current = 0.00;

        double stepSize = 1;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<Double> height_ball1 = new ArrayList<Double>();
        List<Double> height_ball2 = new ArrayList<Double>();
        List<Double> effect = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();

        height_ball1.add(BB.h);
        height_ball2.add(BB2.h);
        time.add(0.0);
        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(fg.terminate())
                break;
            //data exchange
            if(rollback!=1) {
                BB.SetValues("v", Double.toString(BB.v + fg.getValues("effectBall1")));
                BB2.SetValues("v", Double.toString(BB2.v + fg.getValues("effectBall2")));
            }
            Double[] data = new Double[9];
            data[0] = fg.getValues("effectBall1");
            data[1] = fg.getValues("effectBall2");
            data[2] = Double.parseDouble(BB.GetValues("h"));
            data[3] = Double.parseDouble(BB.GetValues("v"));
            data[4] = Double.parseDouble(BB2.GetValues("h"));
            data[5] = Double.parseDouble(BB2.GetValues("v"));
            data[6] = fg.getValues("currentIndex");

//            System.out.println(" stepSIze: "+stepSize+"current index"+fg.currentIndex);
            if(-1 != BB2.DoStep(current,stepSize) && -1 != BB.DoStep(current,stepSize) &&-1 != fg.doStep(current,stepSize)) {


                current += stepSize;
                rollback = 0;
                stepSize = 1;
                time.add(current);
                height_ball1.add(BB.h);
                height_ball2.add(BB2.h);
                effect.add(fg.getValues("effect"));
                stepList.add(stepSize);
                index++;
            }
            else {
                rollback = 1;
                stepSize *= (6.0/10.0);
                rollbackNum++;
                fg.SetValues(1,data[0]);
                fg.SetValues(2,data[1]);
                BB.SetValues("h",Double.toString(data[2]));
                BB.SetValues("v",Double.toString(data[3]));
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("Fixed stepSize,rollback:"+ rollbackNum +", time cost:" + (stop-start)+
                "eventNum :"+BB.eventNum+"   ,"+ BB2.eventNum+"    ," + fg.eventNum+"    ," + time.get(time.size()-1));
        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = height_ball1.get(i);
        }
        return plot;
    }
    public static void main(String[] args){
        TestWaterTank tbb = new TestWaterTank();
        double[][] plot_h_ball3 = tbb.withLR(tbb.Train());
        double[][] plot_h_ball2 = tbb.withOutLR();

        double[][] tank1 = new double[plot_h_ball2.length][2];
        double[][] tank2 = new double[plot_h_ball2.length][2];
        for(int i = 0 ;i<plot_h_ball2.length;i++ ){
            tank1[i][0] = tank2[i][0] = plot_h_ball2[i][0];
            tank1[i][1] = plot_h_ball2[i][1];
            tank2[i][1] = plot_h_ball2[i][2];
        }
//        double[][] plot_h_ball3 = tbb.fixedStep(30,10,8);
//
////
//        JavaPlot jp = new JavaPlot();
////        DataSetPlot dsp1 = new DataSetPlot(plot_h_ball1);
//        DataSetPlot dsp2 = new DataSetPlot(tank1);
//        DataSetPlot dsp3 = new DataSetPlot(tank2);
////        jp.addPlot(dsp1);
//        jp.addPlot(dsp2);
//        jp.addPlot(dsp3);
//        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINESPOINTS);
//        ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Tank 1 height");
//        ((AbstractPlot)(jp.getPlots().get(1))).setTitle("Tank 2 height");
//        ((AbstractPlot)(jp.getPlots().get(1))).getPlotStyle().setStyle(Style.LINESPOINTS);
////        ((AbstractPlot)(jp.getPlots().get(2))).setTitle("Fixed stepSize Normal Rollback");
//        jp.plot();

    }
}
