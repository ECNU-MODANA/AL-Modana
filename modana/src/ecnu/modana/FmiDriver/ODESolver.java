package ecnu.modana.FmiDriver;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/12/24.
 */
public class ODESolver {
    static double de = 0.0;
    public static double function(double a,double b)
    {
        return de;
    }
    /*n表示几等分，n+1表示他输出的个数*/
    public static double RungeKutta(double y0,double a,double b,int n, double de)
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
//        for(i=0;i<=n;i++)
//            System.out.printf("x[%d]=%f,y[%d]=%f\n",i,x[i],i,y[i]);
        return y[5];
    }
    public static void main(String[] args)
    {
        //例子求y'=y-2*x/y(0<x<1);y0=1;
        System.out.println("用四阶龙格-库塔方法");
        RungeKutta(1,0,5,5,1.0);
    }
}
