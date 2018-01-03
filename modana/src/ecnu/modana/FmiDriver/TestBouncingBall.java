package ecnu.modana.FmiDriver;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.Style;

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
        double[][] timeForceQue = new double[10][3];
        Random selectBall = new Random(20);
        Random selectEffect = new Random(10);
        for(int i =0;i<10;i++){
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
                BB.SetValues("v", Double.toString(BB.v + fg.getValues("effectBall1")));
                BB2.SetValues("v", Double.toString(BB2.v + fg.getValues("effectBall2")));
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


            //训练集取大一点：

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
                stepSize *= (6.0/10.0);
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
        double[] doublelable = new double[dataSet.size()];
        for(int t = 0;t<dataSet.size();t++){
            Double[] DoubleData = dataSet.get(t);
            for(int tempI=0;tempI< 6;tempI++){
                doubleData[t][tempI] = DoubleData[tempI];
            }
            doubleData[t][6] = DoubleData[7];
            doublelable[t] = DoubleData[8];
        }
        //逻辑回归获取权重向量
        LogProcess lp = new LogProcess();
        double[] weight = lp.train(doubleData,doublelable, 0.1f, 200, (short)1);

        System.out.println(System.currentTimeMillis()-start);
        return weight;
    }
    public double[][] withLR(double[] weight,int seed1,int seed2,int length){
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

        height_ball1.add(BB.h);
        height_ball2.add(BB2.h);
        time.add(0.0);


        //simulation start
        double[] parameters = null;
        double w0 = weight[0];
        double w1 = weight[1];
        double w2 = weight[2];
        double w3 = weight[3];
        double w4 = weight[4];
        double w5 = weight[5];
        double w6 = weight[6];
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


            // stepsize prediction
            double tempBB = BB.Predict();
            double tempFg = fg.Predict(current);
            if(rollback != 1)
                stepSize = tempBB> tempFg ? tempFg : tempBB;
//            System.out.println(tempBB+ "   "+ tempFg + " stepSIze: "+stepSize);
            double classifyResult = w0 * data[0]+
                    w1 * data[1]+
                    w2 * data[2]+
                    w3 * BB.v+
                    w4 * data[4]+
                    w5 * BB2.v;
            for(int i=2;i>0;i--){
                if(classifyResult + w6 * stepSize>0)
                    break;
//                System.out.println(classifyResult + "   "+ weight[6] * stepSize);
                stepSize = stepSize*(6.0/10.0);

            }
            data[7] = stepSize;
            if(-1 != BB2.DoStep(current,stepSize)) {
                BB.DoStep(current,stepSize);
                fg.doStep(current,stepSize);
                current += stepSize;
                rollback = 0;
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
            }
        }
        Long stop = System.currentTimeMillis();
        System.out.println("with LR,rollback:"+ rollbackNum +", time cost:" + (stop-start)+
                "eventNum :"+BB.eventNum+"   ,"+ BB2.eventNum+"    ," + fg.eventNum+"    ," + time.get(time.size()-1)+"likelyhood:"+(double)index/(double)(index+rollbackNum));

        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = height_ball1.get(i);
        }
        return plot;


    }
    public double[][] withOutLR(int seed1,int seed2,int length){
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
                fg.SetValues(1,data[0]);
                fg.SetValues(2,data[1]);
                BB.SetValues("h",Double.toString(data[2]));
                BB.SetValues("v",Double.toString(data[3]));
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
                fg.SetValues(1,data[0]);
                fg.SetValues(2,data[1]);
                BB.SetValues("h",Double.toString(data[2]));
                BB.SetValues("v",Double.toString(data[3]));
                BB2.SetValues("h",Double.toString(data[4]));
                BB2.SetValues("v",Double.toString(data[5]));
                fg.SetValues(3,data[6]);
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
            plot[i][1] = height_ball1.get(i);
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

        double stepSize = 0.1;
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
                stepSize = 0.1;
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
        TestBouncingBall tbb = new TestBouncingBall();
        double[][] plot_h_ball1 = tbb.withLR(tbb.Train(),30 ,10,8);
        double[][] plot_h_ball2 = tbb.withOutLR(30,10,8);
        double[][] plot_h_ball3 = tbb.fixedStep(30,10,8);

//
        JavaPlot jp = new JavaPlot();
        DataSetPlot dsp1 = new DataSetPlot(plot_h_ball1);
        DataSetPlot dsp2 = new DataSetPlot(plot_h_ball2);
        DataSetPlot dsp3 = new DataSetPlot(plot_h_ball3);
        jp.addPlot(dsp1);
        jp.addPlot(dsp2);
        jp.addPlot(dsp3);
        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINESPOINTS);
        ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Logistic Regression Prediction");
        ((AbstractPlot)(jp.getPlots().get(1))).getPlotStyle().setStyle(Style.LINESPOINTS);
        ((AbstractPlot)(jp.getPlots().get(1))).setTitle("Normal Prediction");
        ((AbstractPlot)(jp.getPlots().get(2))).getPlotStyle().setStyle(Style.LINESPOINTS);
        ((AbstractPlot)(jp.getPlots().get(2))).setTitle("Fixed stepSize Normal Rollback");
        jp.plot();

    }
}
