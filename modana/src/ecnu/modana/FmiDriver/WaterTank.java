package ecnu.modana.FmiDriver;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/12/24.
 */
public class WaterTank extends PlugInSlave{
    double PipeIn = 0.0, PipeOut = 0.0, area = 5.0, h = 10.0;
    double maxHeight = 20.0;
    public WaterTank(double pi, double po, double area, double h,double maxHeight){
        this.PipeIn = pi;
        this.PipeOut = po;
        this.area = area;
        this.h = h;
        this.maxHeight = maxHeight;
    }


    @Override
    public void NewPath() {
        // TODO Auto-generated method stub
        h=10.0;
        area=5.0;
    }

    @Override
    public double DoStep(double curTime,double stepSize) {
        double calculatedHeight = 0.00;
        calculatedHeight = ODESolver.RungeKutta(area*h,curTime,curTime+stepSize,5,PipeIn-PipeOut)/area;
        if (calculatedHeight <= -0.001 || calculatedHeight - maxHeight > 0.01) {
            return -1;
        }
        h = calculatedHeight;
        return stepSize;
    }

    @Override
    public double GetValues(String varNames) {
        if(null==varNames){
            return h;
        }
        else if("h".equals(varNames)) return h;
        return 0.0;
    }

    @Override
    public boolean SetValues(String varNames, double values) {
        if(null==varNames){
//            String[]tem=values.split(",");
//            for(int i=0;i<tem.length;i++){
//                h=Double.valueOf(tem[i]);
//            }
            return true;
        }
        else if("PipeIn".equals(varNames)) PipeIn =  values;
        else if("PipeOut".equals(varNames)) PipeOut =  values;

        return true;
    }


}
