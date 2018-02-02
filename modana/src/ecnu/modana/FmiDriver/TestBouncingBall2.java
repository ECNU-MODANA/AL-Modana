package ecnu.modana.FmiDriver;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.Style;
import ecnu.modana.util.ForFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/11/21.
 */
public class TestBouncingBall2 {
    public double[] Train(double defaultStep) throws Exception{
        Long start = System.currentTimeMillis();

        double[][] timeForceQue = new double[10][3];
        Random selectBall = new Random(20);
        Random selectEffect = new Random(10);
//        for(int i =0;i<10;i++){
//            timeForceQue[i][0] = i+1;
//            timeForceQue[i][1] = i%2+1;
//            timeForceQue[i][2] = selectEffect.nextInt(10);
//        }
        List<Double[]> dataSet = new ArrayList<Double[]>();

        int rollbackNum = 0;
        String dataWritten = "";
        double[][] wtf = new double[11000][3];
        while(dataSet.size()<10000) {
            //simulation start
            double[] parameters = null;
            BBallPlug2 BB = new BBallPlug2(selectBall.nextInt(100)+1, selectBall.nextInt(5), -0.8);
            ForceGeneratorPlug2 fg = new ForceGeneratorPlug2(timeForceQue);
            int i = 0;
            double current = 0.00;
            double stepSize = defaultStep;
            int rollback = 0;
            int index = 0;

            List<Double> time = new ArrayList<Double>();
            List<Double> effect = new ArrayList<Double>();
            List<Double> stepList = new ArrayList<Double>();
            while (true) {

                //termination check
                //            if(current>3)
                //                break;
                Double[] data = new Double[5];

                //数据交换
                //记录数据交换后的FMU状态，以便回滚FMU以及逻辑回归的训练
                // 根据可预测步长FMU进行步长预测
                BB.effect = fg.effectBall1;
                BB.v = BB.v * BB.effect;

                data[0] = fg.getValues("effectBall1");
                data[1] = Double.parseDouble(BB.GetValues("h"));
                data[2] = Double.parseDouble(BB.GetValues("v"));
                data[3] = BB.effect;

                // stepsize prediction

//                double predictStepSize = fg.Predict(current);
//                stepSize = predictStepSize < stepSize ? predictStepSize : stepSize;
                //            double ballStep = BB.DoStep(current,stepSize);
                while (BB.DoStep(current, stepSize) == -1) {
//                                    Double[] negativeSample = new Double[4];
//                                    negativeSample[0] = data[1];
//                                    negativeSample[1] = data[2];
//                                    negativeSample[2] = stepSize;
//                                    negativeSample[3] = 0.0;
//                                    dataSet.add(negativeSample);
//                                    System.out.println(data[1]+"+"+data[2]+"+"+stepSize+"+"+0.0);
                    stepSize *= 0.999;
                    BB.h = data[1];
                    BB.v = data[2];
                    BB.effect = data[3];

                }
                //无需回滚
                Double[] positiveSample = new Double[4];
                positiveSample[0] = data[1];
                positiveSample[1] = data[2];
                positiveSample[2] = stepSize;
                positiveSample[3] = 1.0;
                dataWritten += data[1]+" "+ data[2]+" "+ stepSize +"\r\n";
                wtf[index][0] = data[1];
                wtf[index][1] = data[2];
                wtf[index][2] = stepSize;
                dataSet.add(positiveSample);
                System.out.println(data[1] + "+" + data[2] + "+" + stepSize + "+" + 1.0);
                //            BB.DoStep(current,stepSize);
//                fg.doStep(current, stepSize);
                current += stepSize;
                time.add(current);
                effect.add(fg.getValues("effect"));
                stepList.add(stepSize);
                index++;
                if (Math.abs(BB.v) <= 4.0826181112991215E-2 && Math.abs(BB.h) <= 4.0826181112991215E-2)
                    break;
                stepSize = defaultStep;



                //            System.out.println(current+ " stepSIze: "+stepSize + " v:"+BB.v+" h:"+BB.h);

            }
        }
        Long stop = System.currentTimeMillis();
        ForFile.writeFileContent("G:\\XMP\\data.txt",dataWritten);
//        JavaPlot jp = new JavaPlot();
//        DataSetPlot dsp1 = new DataSetPlot(wtf);
//        jp.addPlot(dsp1);
//        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINESPOINTS);
//        ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Ball Height");
//        jp.plot();
        System.out.println("training-rollback:"+ rollbackNum +", time cost:" + (stop-start));

        double[][] doubleData = new double[dataSet.size()][dataSet.get(0).length-1];
        double[] doublelable = new double[dataSet.size()];
        for(int t = 0;t<dataSet.size();t++){
            Double[] DoubleData = dataSet.get(t);
            for(int tempI=0;tempI< DoubleData.length-1;tempI++){
                doubleData[t][tempI] = DoubleData[tempI];
            }
            doublelable[t] = DoubleData[DoubleData.length-1];
        }
//        //逻辑回归获取权重向量
        LogProcess lp = new LogProcess();
        double[] weight = lp.train(doubleData,doublelable, 0.1f, 200, (short)1);
//        LinearRegression lr = new LinearRegression(doubleData,doubleData.length,doubleData[0].length);
//        lr.printTrainData();
//        lr.trainTheta();
//        lr.printTheta();
        System.out.println(System.currentTimeMillis()-start);
        return weight;
    }
    public double[][] withLR(double[] weight,int seed1,int seed2,double defaultStep,int length) throws Exception{
        Long start = System.currentTimeMillis();
        BBallPlug2 BB = new BBallPlug2(4,0,-0.8);
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

        double stepSize = defaultStep;
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
        time.add(0.0);


        //simulation start
        double[] parameters = null;
//        double w0 = weight[0];
        double w1 = weight[0];
        double w2 = weight[1];
        double w3 = weight[2];
        while(true){
            //termination check
//            if(current>4.5)
//                break;
            //data exchange
            Double[] data = new Double[5];

            //数据交换
            //记录数据交换后的FMU状态，以便回滚FMU以及逻辑回归的训练
            // 根据可预测步长FMU进行步长预测
            BB.effect = fg.effectBall1;

            data[0] = fg.getValues("effectBall1");
            data[1] = Double.parseDouble(BB.GetValues("h"));
            data[2] = Double.parseDouble(BB.GetValues("v"));
            data[3] = BB.effect;


            // stepsize prediction
            double predictStepSize = fg.Predict(current);
            stepSize = predictStepSize< stepSize ? predictStepSize : stepSize;

////            double classifyResult = Math.abs((w1 * data[1]+ w2 * data[2]));
//            stepSize = classifyResult< stepSize ? classifyResult : stepSize;

            double classifyResult = w1 * data[0]+
                    w2 * data[1];
            for(int i=2;i>0;i--){
                if(classifyResult + w3* stepSize>0)
                    break;
//                System.out.println(classifyResult + "   "+ weight[6] * stepSize);
                stepSize = stepSize*(9.0/10.0);

            }
//            for(int i=1;i>0;i--){
//                if(classifyResult + w2 * stepSize>0)
//                    break;
//                System.out.println(classifyResult + "   "+ w2 * stepSize);
//                stepSize = stepSize*(9.0/10.0);
//
//            }
            while(BB.DoStep(current,stepSize) == -1){
                Double[] negativeSample = new Double[4];
                negativeSample[0] = data[1];
                negativeSample[1] = data[2];
                negativeSample[2] = stepSize;
                negativeSample[3] = 0.0;
                dataSet.add(negativeSample);
                stepSize *= 0.9;
                BB.h = data[1];
                BB.v = data[2];
                BB.effect = data[3];
            }
            //无需回滚
            Double[] positiveSample = new Double[4];
            positiveSample[0] = data[1];
            positiveSample[1] = data[2];
            positiveSample[2] = stepSize;
            positiveSample[3] = 1.0;
            dataSet.add(positiveSample);
            BB.DoStep(current,stepSize);
            fg.doStep(current,stepSize);
            height_ball1.add(BB.h);
            current += stepSize;
            time.add(current);
            effect.add(fg.getValues("effect"));
            stepList.add(stepSize);
            index++;
            System.out.println(current + " stepSIze: "+stepSize);
            stepSize = defaultStep;
            if(Math.abs(BB.v)<=4.0826181112991215E-10)
                break;
        }
        Long stop = System.currentTimeMillis();
        System.out.println("with LR,rollback:"+ rollbackNum +", time cost:" + (stop-start)+
                "eventNum :"+BB.eventNum+"   ," + fg.eventNum);

        double[][] plot = new double[time.size()][2];
        for(int i=0;i<time.size();i++){
            plot[i][0] = time.get(i);
            plot[i][1] = height_ball1.get(i);
        }
        return plot;


    }
    public double[][] withOutLR(int seed1,double defaultStepSize,int length) throws Exception{
        Long start = System.currentTimeMillis();
        BBallPlug2 BB = new BBallPlug2(4,0,-0.8);
//        BBallPlug1 BB2 = new BBallPlug1(7,2,-0.8);
        double[][] timeForceQue = new double[length][3];
        Random selectBall = new Random(seed1);
        for(int i =0;i<length;i++){
            timeForceQue[i][0] = i+1;
            timeForceQue[i][1] = selectBall.nextInt(3)+1;
        }
        ForceGeneratorPlug2 fg = new ForceGeneratorPlug2(timeForceQue);
        double current = 0.00;

        double stepSize = defaultStepSize;
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
//        height_ball2.add(BB2.h);
        time.add(0.0);

        //simulation start
        double[] parameters = null;
        while(true){
            //termination check
//            if(current>4.5)
//                break;
            //data exchange
            BB.effect = fg.effectBall1;
            BB.v = BB.v * BB.effect;
            Double[] data = new Double[9];
            data[0] = fg.getValues("effectBall1");
            data[1] = Double.parseDouble(BB.GetValues("h"));
            data[2] = Double.parseDouble(BB.GetValues("v"));
            data[3] = BB.effect;
            // stepsize prediction

            double predictStepSize = fg.Predict(current);
            stepSize = predictStepSize< stepSize ? predictStepSize : stepSize;
            while(BB.DoStep(current,stepSize) == -1){
                Double[] negativeSample = new Double[4];
                negativeSample[0] = data[1];
                negativeSample[1] = data[2];
                negativeSample[2] = stepSize;
                negativeSample[3] = 0.0;
                dataSet.add(negativeSample);
                stepSize *= 0.9;
                BB.h = data[1];
                BB.v = data[2];
                BB.effect = data[3];
            }
            //无需回滚
            Double[] positiveSample = new Double[4];
            positiveSample[0] = data[1];
            positiveSample[1] = data[2];
            positiveSample[2] = stepSize;
            positiveSample[3] = 1.0;
            dataSet.add(positiveSample);
            BB.DoStep(current,stepSize);
            fg.doStep(current,stepSize);
            current += stepSize;
            time.add(current);
            height_ball1.add(BB.h);
            effect.add(fg.getValues("effect"));
            stepList.add(stepSize);
            index++;
            stepSize = defaultStepSize;
            if(Math.abs(BB.v)<=4.0826181112991215E-10)
                break;
//            System.out.println(current+ " stepSIze: "+stepSize + " v:"+BB.v+" h:"+BB.h);
            dataSet.add(data);
            stepSize = defaultStepSize;
        }
        Long stop = System.currentTimeMillis();
        System.out.println("without LR,rollback:"+ rollbackNum +", time cost:" + (stop-start));
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
    public static void main(String[] args) throws Exception{
        double[][] test = new double[5][10];
        int length = test.length;
        TestBouncingBall2 tbb = new TestBouncingBall2();
        double[][] plot_h_ball1 = tbb.withLR(tbb.Train(100),30 ,10,1,9);
        double[][] plot_h_ball2 = tbb.withOutLR(30,1,9);
//        double[][] plot_h_ball3 = tbb.fixedStep(30,10,8);

//

        JavaPlot jp = new JavaPlot();
        DataSetPlot dsp1 = new DataSetPlot(plot_h_ball1);
        DataSetPlot dsp2 = new DataSetPlot(plot_h_ball2);
//        DataSetPlot dsp3 = new DataSetPlot(plot_h_ball3);
        jp.addPlot(dsp1);
        jp.addPlot(dsp2);
//        jp.addPlot(dsp3);
        ((AbstractPlot)(jp.getPlots().get(0))).getPlotStyle().setStyle(Style.LINESPOINTS);
        ((AbstractPlot)(jp.getPlots().get(0))).setTitle("Ball Height");
        ((AbstractPlot)(jp.getPlots().get(1))).getPlotStyle().setStyle(Style.LINESPOINTS);
        ((AbstractPlot)(jp.getPlots().get(1))).setTitle("Normal Prediction");
//        ((AbstractPlot)(jp.getPlots().get(2))).getPlotStyle().setStyle(Style.LINESPOINTS);
//        ((AbstractPlot)(jp.getPlots().get(2))).setTitle("Ball Height");
        jp.plot();

    }
}
