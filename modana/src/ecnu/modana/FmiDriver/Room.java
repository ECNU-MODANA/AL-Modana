package ecnu.modana.FmiDriver;

public class Room extends PlugInSlave {
    double temperature = 0.0;
    static double de = 0.0;
    static double Room1switch;
    public static double function(double t,double a)
    {
        double e = 2.71828;
        double Gauss1 = -((t-23.16)/ 11.58)*((t-23.16)/ 11.58);
        double Gauss2 = -((t-14.24)/ 6.194)*((t-14.24)/ 6.194);
        double Gauss3 = -((t+1.212)/ 5.069)*((t+1.212)/ 5.069);
        return (900*Room1switch + 25*(3.868*Math.pow(e,Gauss1)+8.556*Math.pow(e,Gauss2)+6.057*Math.pow(e,Gauss3)-a))/100;
    }

    public Room(double temp){
        this.temperature = temp;
    }
    /*n表示几等分，n+1表示他输出的个数*/
    public static double RungeKutta(double y0,double a,double b,int n)
    {
        ODESolver.de = de;
        double[] x=new double[n+1];
        double[] y=new double[n+1];
        double h=(b-a)/n,k1,k2,k3,k4;
        int i;
        x[0]=a;
        y[0]=y0;
        for(i=0;i<n;i++)
        {
            x[i+1]=x[i]+h;
            k1=function(x[i],y[i]);
            k2=function(x[i]+h/2,y[i]+h*k1/2);
            k3=function(x[i]+h/2,y[i]+h*k2/2);
            k4=function(x[i]+h,y[i]+h*k3);
            y[i+1]=y[i]+h*(k1+2*k2+2*k3+k4)/6;
        }

        return y[5];
    }

    @Override
    public void NewPath() {
        // TODO Auto-generated method stub
        temperature=20.0;
    }

    @Override
    public double DoStep(double curTime,double stepSize) {
        temperature = RungeKutta(temperature,curTime,curTime+stepSize,5);
        return stepSize;
    }

    @Override
    public double GetValues(String varNames) {
        if(null==varNames){
            return temperature;
        }
        else if("t".equals(varNames)) return temperature;
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
        else if("t".equals(varNames)) temperature =  values;

        return true;
    }
}
