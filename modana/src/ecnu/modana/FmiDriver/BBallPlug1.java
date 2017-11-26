package ecnu.modana.FmiDriver;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.DataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.Style;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.spark.ml.classification.*;
import org.apache.spark.sql.*;
import org.apache.spark.sql.execution.vectorized.ColumnarBatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author： oe
 * @Description:BouncingBall1 with stepsize prediction
 * @Created by oe on 2017/11/11.
 * @Fields ${h} : height of the ball
 * @Fields ${g} : gravity
 * @Fields ${v} : velocity of the ball
 * @Fields ${c} : attenuation coefficient of velocity when ball hit ground
 */
public class BBallPlug1 extends ToolSlave{

    double h=4,g=9.8,v=0,c=-0.8;
    int eventNum = 0;

    public BBallPlug1(double h, double v ,double c){
        this.v = v;
        this.h = h;
        this.c = c;
    }
    @Override
    public boolean ConnectMaster(String host, int port) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String OpenModel(String modelPath) {
        // TODO Auto-generated method stub
        return "h";
    }

    @Override
    public void NewPath() {
        // TODO Auto-generated method stub
        h=4;
        v=0;
    }

    @Override
    public double DoStep(double curTime,double stepSize) {
        double calculatedHeight = 0.00;
//        if(v>=0) {
            calculatedHeight = h-v*stepSize-g*stepSize*stepSize/2;
            if (calculatedHeight <= -0.001) {
                return -1;
            } else if (calculatedHeight <= 0.001) {
                eventNum++;
//                h = 0;
                v = (v + g * stepSize) * c;
            } else {
                v = v + g * stepSize;
                h = calculatedHeight;
            }
        return stepSize;
    }

    @Override
    public String GetValues(String varNames) {
        if(null==varNames){
            return String.valueOf(h);
        }
        else if("h".equals(varNames)) return String.valueOf(h);
        else if("v".equals(varNames)) return String.valueOf(v);
        return "";
    }

    @Override
    public boolean SetValues(String varNames, String values) {
        if(null==varNames){
            String[]tem=values.split(",");
            for(int i=0;i<tem.length;i++){
                h=Double.valueOf(tem[i]);
            }
            return true;
        }
        else if("v".equals(varNames)) v=  Double.valueOf(values);
        else if("h".equals(varNames)) h=  Double.valueOf(values);

        return true;
    }
    public double Predict(){
        //现在存在的事件bug：如果在小球落地的同时，发生了拍击事件，并使小球原本向上的速度重置为向下，将导致无限预测
        //0.0步长，从而使得仿真“终止”；
//        System.out.println("current h of ball1 is : " + h+"v = " + v);
        double tPredict = 0.0;
//        if(h<1E-4 && v*v<1E-6){
//            return 999;
//        }
        if(v>=0) {
            tPredict = (Math.sqrt(v * v + 2 * g * h) - v) / g;
            if (tPredict >= 0)
                return tPredict;
        }else {
            tPredict = (Math.sqrt(v * v + 2 * g * h) - v) / g;
            if (tPredict >= 0)
                return tPredict;
        }


            return -1;
    }

}
