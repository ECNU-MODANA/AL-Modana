package ecnu.modana.FmiDriver;

import Jama.Matrix;

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
    public static Matrix sigmoid(Matrix intX){
        double [][] tmp = new double[intX.getRowDimension()][intX.getColumnDimension()];
        for(int i=0;i<intX.getRowDimension();i++){
            tmp[i][0]=1.0/(1+Math.exp(intX.get(i, 0)));
        }

        Matrix tmpMat = new Matrix(tmp);
        return tmpMat;
    }

    public static double[] calculate(double[][] data, double[][] lable){

        Matrix dataMat= new Matrix(data);
        //标签矩阵m
        Matrix labelMat= new Matrix(lable);
        //获取维数
        int m=dataMat.getRowDimension();
        int n=dataMat.getColumnDimension();

        //步长
        double alpha=0.001;
        //迭代次数
        int maxCycles=1000;
        //weights权重列向量
        double [][] weight=new double[n][1];
        for(int i = 0;i<n;i++)
            weight[i][0] = 1;
        Matrix mm ;
        Matrix h;
        Matrix e;
        Matrix weightMat = new Matrix(weight);
        for(int i=1;i<maxCycles;i++){
            mm = dataMat.times(weightMat).times(-1);
            h = sigmoid(mm);
            e = labelMat.minus(h);
            weightMat = weightMat.plus(dataMat.transpose().times(e).times(alpha));

//            System.out.println("-------------------"+i+"------------------");
//            for(int j=0;j<weightMat.getRowDimension();j++){
////                System.out.println(weightMat.get(j, 0));
//            }
        }
        return weightMat.getColumnPackedCopy();
    }


}

