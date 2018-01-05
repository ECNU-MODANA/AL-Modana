package ecnu.modana.FmiDriver;

import java.io.*;
import java.util.ArrayList;

public class Room extends PlugInSlave {
    double temperature = 0.0;
    double otherRoomEffect = 0.0;
    static double de = 0.0;
    static double Room1switch;
    static ArrayList<Double[]> amat = new ArrayList();
    public static double function(double t,double a)
    {
        int index = 0;
        for(;index<amat.size()-1;index++){
            if(Math.abs(amat.get(index)[0]-t%48)<=0.1)
                break;
        }
//        double e = 2.71828;
//        double Gauss1 = -((t-23.16)/ 11.58)*((t-23.16)/ 11.58);
//        double Gauss2 = -((t-14.24)/ 6.194)*((t-14.24)/ 6.194);
//        double Gauss3 = -((t+1.212)/ 5.069)*((t+1.212)/ 5.069);
//        System.out.println(1+ -1*Math.sin(t*2*3.14159265));
        return (900*Room1switch + 25*(1+ -1*Math.sin(t*2*3.14159265)-a)+amat.get(index)[1])/100;
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
        String absolutePath = Room.class.getClass().getResource("/").getPath();
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(absolutePath + "../files/variance.txt"));
            while((line = bufferedReader.readLine()) != null){
                if(line.startsWith("#"))
                    continue;
                Double[] temp = new Double[2];
                temp[0] = Double.parseDouble(line.split(" ")[0]);
                temp[1] = Double.parseDouble(line.split(" ")[1]);
                amat.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
