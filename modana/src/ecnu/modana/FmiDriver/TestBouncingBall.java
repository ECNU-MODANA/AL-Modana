package ecnu.modana.FmiDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/11/21.
 */
public class TestBouncingBall {
    public double[] Train(){
        Long start = System.currentTimeMillis();
        double[][] plot = new double[83][2];
        BBallPlug1 BB = new BBallPlug1(4,0,-0.8);
        BBallPlug1 BB2 = new BBallPlug1(7,2,-0.8);
        double[][] timeForceQue = new double[100][3];
        Random selectBall = new Random(20);
        Random selectEffect = new Random(10);
        for(int i =0;i<100;i++){
            timeForceQue[i][0] = i+1;
            timeForceQue[i][1] = i%2+1;
            timeForceQue[i][2] = selectEffect.nextInt(10);
        }
        ForceGeneratorPlug fg = new ForceGeneratorPlug(timeForceQue);
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
            if(fg.terminate())
                break;
            Double[] data = new Double[9];

            //数据交换
            if(rollback!=1) {
                BB.SetValues("v", Double.toString(BB.v + fg.getValues("effectBall1")));
                BB2.SetValues("v", Double.toString(BB2.v + fg.getValues("effectBall2")));
            }
            //记录数据交换后的FMU状态，以便回滚FMU以及逻辑回归的训练
            data[0] = fg.getValues("effectBall1");
            data[1] = fg.getValues("effectBall2");
            data[2] = Double.parseDouble(BB.GetValues("h"));
            data[3] = Double.parseDouble(BB.GetValues("v"));
            data[4] = Double.parseDouble(BB2.GetValues("h"));
            data[5] = Double.parseDouble(BB2.GetValues("v"));
            data[6] = fg.getValues("currentIndex");
            // 根据可预测步长FMU进行步长预测
            double tempBB = BB.Predict();
            double tempFg = fg.Predict(current);
            if(rollback != 1)
                stepSize = tempBB> tempFg ? tempFg : tempBB;
//            System.out.println(tempBB+ "   "+ tempFg + " stepSIze: "+stepSize);

            //记录最终选择的步长
            data[7] = stepSize;
            //根据剩余的FMU执行情况选择接受该步长，或者回滚
            if(-1 != BB2.DoStep(current,stepSize)) {
                BB.DoStep(current,stepSize);
                fg.doStep(current,stepSize);
                current += stepSize;
                rollback = 0;
                time.add(current);
                height_ball1.add(BB.GetValues("h"));
                height_ball2.add(BB2.GetValues("h"));
                effect.add(fg.getValues("effect"));
                stepList.add(stepSize);
                index++;
                data[8] = 1.0;
            }
            else {
                rollback = 1;
                stepSize /= 2.0;
                rollbackNum++;
                fg.SetValues(1,data[0]);
                fg.SetValues(2,data[1]);
                BB.SetValues("h",Double.toString(data[2]));
                BB.SetValues("v",Double.toString(data[3]));
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
                data[8] = 0.0;
            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("training-rollback:"+ rollbackNum +", time cost:" + (stop-start));

        double[][] doubleData = new double[dataSet.size()][7];
        double[][] doublelable = new double[dataSet.size()][1];
        for(int t = 0;t<dataSet.size();t++){
            Double[] DoubleData = dataSet.get(t);
            for(int tempI=0;tempI< 6;tempI++){
                doubleData[t][tempI] = DoubleData[tempI];
            }
            doubleData[t][6] = DoubleData[7];
            doublelable[t][0] = DoubleData[8];
        }
        //逻辑回归获取权重向量
        LogProcess lp = new LogProcess();
        double[] weight = lp.calculate(doubleData,doublelable);
        return weight;
    }
    public double[][] withLR(double[] weight,int seed1,int seed2){
        Long start = System.currentTimeMillis();
        BBallPlug1 BB = new BBallPlug1(4,0,-0.8);
        BBallPlug1 BB2 = new BBallPlug1(7,2,-0.8);
        double[][] timeForceQue = new double[100][3];
        Random selectBall = new Random(seed1);
        Random selectEffect = new Random(seed2);
        for(int i =0;i<100;i++){
            timeForceQue[i][0] = i+1;
            timeForceQue[i][1] = i%2+1;
            timeForceQue[i][2] = selectEffect.nextInt(10);
        }
        ForceGeneratorPlug fg = new ForceGeneratorPlug(timeForceQue);
        double current = 0.00;

        double stepSize = 0.0;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<Double> height_ball1 = new ArrayList<Double>();
        List<Double> height_ball2 = new ArrayList<Double>();
        List<Double> effect = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();


        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(fg.terminate())
                break;
            Double[] data = new Double[9];
            data[0] = fg.getValues("effectBall1");
            data[1] = fg.getValues("effectBall2");
            data[2] = Double.parseDouble(BB.GetValues("h"));
            data[3] = Double.parseDouble(BB.GetValues("v"));
            data[4] = Double.parseDouble(BB2.GetValues("h"));
            data[5] = Double.parseDouble(BB2.GetValues("v"));
            data[6] = fg.getValues("currentIndex");
            //data exchange
//            if(BB.h!=0.0)
            if(rollback != 1) {
                BB.SetValues("v", Double.toString(BB.v + fg.getValues("effectBall1")));
//            if(BB2.h!=0.0)
                BB2.SetValues("v", Double.toString(BB2.v + fg.getValues("effectBall2")));
            }
            // stepsize prediction
            double tempBB = BB.Predict();
            double tempFg = fg.Predict(current);
            if(rollback != 1)
                stepSize = tempBB> tempFg ? tempFg : tempBB;
            System.out.println(tempBB+ "   "+ tempFg + " stepSIze: "+stepSize);
            double classifyResult = weight[0] * data[0]+
                    weight[1] * data[1]+
                    weight[2] * data[2]+
                    weight[3] * BB.v+
                    weight[4] * data[4]+
                    weight[5] * BB2.v;
            for(int i=10;i>0;i--){
                if(classifyResult + weight[6] * stepSize>0)
                    break;
//                System.out.println(classifyResult + "   "+ weight[6] * stepSize);
                stepSize = stepSize*(9.0/10.0);

            }
            data[7] = stepSize;
            if(-1 != BB2.DoStep(current,stepSize)) {
                BB.DoStep(current,stepSize);
                fg.doStep(current,stepSize);
                current += stepSize;
                rollback = 0;
                time.add(current);
                height_ball1.add(Double.parseDouble(BB.GetValues("h")));
                height_ball2.add(Double.parseDouble(BB2.GetValues("h")));
                effect.add(fg.getValues("effect"));
                stepList.add(stepSize);
                index++;
                data[8] = 1.0;
            }
            else {
                rollback = 1;
                stepSize *= (9.0/10.0);
                rollbackNum++;
                fg.SetValues(1,data[0]);
                fg.SetValues(2,data[1]);
                BB.SetValues("h",Double.toString(data[2]));
                BB.SetValues("v",Double.toString(data[3]));
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
                data[8] = 0.0;

            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("with LR,rollback:"+ rollbackNum +", time cost:" + (stop-start)+
                "eventNum :"+BB.eventNum+"   ,"+ BB2.eventNum+"    ," + fg.eventNum+"    ," + time.get(time.size()-1));

        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = effect.get(i);
        }
        return plot;


    }
    public double[][] withOutLR(int seed1,int seed2){
        Long start = System.currentTimeMillis();
        BBallPlug1 BB = new BBallPlug1(4,0,-0.8);
        BBallPlug1 BB2 = new BBallPlug1(7,2,-0.8);
        double[][] timeForceQue = new double[100][3];
        Random selectBall = new Random(seed1);
        Random selectEffect = new Random(seed2);
        for(int i =0;i<100;i++){
            timeForceQue[i][0] = i+1;
            timeForceQue[i][1] = i%2+1;
            timeForceQue[i][2] = selectEffect.nextInt(10);
        }
        ForceGeneratorPlug fg = new ForceGeneratorPlug(timeForceQue);
        double current = 0.00;

        double stepSize = 0.3;
        int rollback = 0;
        int index = 0;
        int rollbackNum = 0;

        List<Double> time = new ArrayList<Double>();
        List<Double> height_ball1 = new ArrayList<Double>();
        List<Double> height_ball2 = new ArrayList<Double>();
        List<Double> effect = new ArrayList<Double>();
        List<Double> stepList = new ArrayList<Double>();
        List<Double[]> dataSet = new ArrayList<Double[]>();


        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
            if(fg.terminate())
                break;
            Double[] data = new Double[9];
            data[0] = fg.getValues("effectBall1");
            data[1] = fg.getValues("effectBall2");
            data[2] = Double.parseDouble(BB.GetValues("h"));
            data[3] = Double.parseDouble(BB.GetValues("v"));
            data[4] = Double.parseDouble(BB2.GetValues("h"));
            data[5] = Double.parseDouble(BB2.GetValues("v"));
            data[6] = fg.getValues("currentIndex");
            //data exchange
//            if(BB.h!=0.0)
            if(rollback != 1) {
                BB.SetValues("v", Double.toString(BB.v + fg.getValues("effectBall1")));
//            if(BB2.h!=0.0)
                BB2.SetValues("v", Double.toString(BB2.v + fg.getValues("effectBall2")));
            }
            // stepsize prediction
            double tempBB = BB.Predict();
            double tempFg = fg.Predict(current);
            if(rollback != 1)
                stepSize = tempBB> tempFg ? tempFg : tempBB;
//            System.out.println(tempBB+ "   "+ tempFg + " stepSIze: "+stepSize);

            if(-1 != BB2.DoStep(current,stepSize)) {
                BB.DoStep(current,stepSize);
                fg.doStep(current,stepSize);
                current += stepSize;
                rollback = 0;
                time.add(current);
                height_ball1.add(Double.parseDouble(BB.GetValues("h")));
                height_ball2.add(Double.parseDouble(BB2.GetValues("h")));
                effect.add(fg.getValues("effect"));
                stepList.add(stepSize);
                index++;
            }
            else {
                rollback = 1;
                stepSize *= (9.0/10.0);
                rollbackNum++;
                fg.SetValues(1,data[0]);
                fg.SetValues(2,data[1]);
                BB.SetValues("h",Double.toString(data[2]));
                BB.SetValues("v",Double.toString(data[3]));
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
            }
            dataSet.add(data);
        }
        Long stop = System.currentTimeMillis();
        System.out.println("without LR,rollback:"+ rollbackNum +", time cost:" + (stop-start)+
        "eventNum :"+BB.eventNum+"   ,"+ BB2.eventNum+"    ," + fg.eventNum+"    ," + time.get(time.size()-1));
        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = effect.get(i);
        }
        return plot;
    }
    public static void main(String[] args){
        TestBouncingBall tbb = new TestBouncingBall();
        double[][] plot_h_ball1 = tbb.withLR(tbb.Train(),30 ,10);
        double[][] plot_h_ball2 = tbb.withOutLR(30,10);
//
//        JavaPlot jp = new JavaPlot();
//        DataSetPlot dsp1 = new DataSetPlot(plot_h_ball1);
//        DataSetPlot dsp2 = new DataSetPlot(plot_h_ball2);
//        jp.addPlot(dsp1);
//        jp.addPlot(dsp2);
//        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINESPOINTS);
//        ((AbstractPlot)(jp.getPlots().get(1))).getPlotStyle().setStyle(Style.LINESPOINTS);
//        jp.plot();

    }
}
