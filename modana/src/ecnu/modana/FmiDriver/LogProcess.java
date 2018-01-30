package ecnu.modana.FmiDriver;

import java.util.Arrays;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/11/15.
 */
public class LogProcess {
    /**
     * 获取特征数据
     */


    /**
     * 获取标签数据
     */


    /**
     * sigmoid函数
     */
    public static double sigmoid(double src) {
        return (double) (1.0 / (1 + Math.exp(-src)));
    }

//    public static double[] calculate(double[][] data, double[][] lable){
//
//        Matrix dataMat= new Matrix(data);
//        //标签矩阵m
//        Matrix labelMat= new Matrix(lable);
//        //获取维数
//        int m=dataMat.getRowDimension();
//        int n=dataMat.getColumnDimension();
//
//        //步长
//        double alpha=0.001;
//        //迭代次数
//        int maxCycles=1000;
//        //weights权重列向量
//        double [][] weight=new double[n][1];
//        for(int i = 0;i<n;i++)
//            weight[i][0] = 1;
//        Matrix mm ;
//        Matrix h;
//        Matrix e;
//        Matrix weightMat = new Matrix(weight);
//        for(int i=1;i<maxCycles;i++){
//            mm = dataMat.times(weightMat).times(-1);
//            h = sigmoid(mm);
//            e = labelMat.minus(h);
//            weightMat = weightMat.plus(dataMat.transpose().times(e).times(alpha));
//
////            System.out.println("-------------------"+i+"------------------");
////            for(int j=0;j<weightMat.getRowDimension();j++){
//////                System.out.println(weightMat.get(j, 0));
////            }
//        }
//        return weightMat.getColumnPackedCopy();
//    }
    public double[] train(double[][] tdatas,double[] tlabels, double step, int maxIt, short algorithm) {

        double[][] datas = tdatas;
        double[] labels = tlabels;
        int size = datas.length;
        int dim = datas[0].length;
        double[] w = new double[dim];

        for(int i = 0; i < dim; i++) {
            w[i] = 1;
        }

        switch(algorithm){
            //批量梯度下降
            case 1:
                for(int i = 0; i < maxIt; i++) {
                    //求输出
                    double[] out = new double[size];
                    for(int s = 0; s < size; s++) {
                        double lire = innerProduct(w, datas[s]);
                        out[s] = sigmoid(lire);
                    }
                    for(int d = 0; d < dim; d++) {
                        double sum = 0;
                        for(int s = 0; s < size; s++) {
                            sum  += (labels[s] - out[s]) * datas[s][d];
                        }
//                        w[d] = w[d] + step * sum;
                        w[d] = (float) (w[d] + step * sum-0.01*Math.abs(w[d]));  //L1正则
                    }
                    System.out.println(Arrays.toString(w));
                }
                break;
            //随机梯度下降
            case 2:
                for(int i = 0; i < maxIt; i++) {
                    for(int s = 0; s < size; s++) {
                        double lire = innerProduct(w, datas[s]);
                        double out = sigmoid(lire);
                        double error = labels[s] - out;
                        for(int d = 0; d < dim; d++) {
                            w[d] += step * error * datas[s][d];
                        }
                    }
                    System.out.println(Arrays.toString(w));
                }
                break;
        }
        return w;
    }

    private double innerProduct(double[] w, double[] x) {

        double sum = 0;
        for(int i = 0; i < w.length; i++) {
            sum += w[i] * x[i];
        }

        return sum;
    }


}

