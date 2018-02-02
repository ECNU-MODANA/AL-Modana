package ecnu.modana.FmiDriver;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/11/12.
 */
public class ForceGeneratorPlug2 {
    double[][] timeForceQue = {{1,1,2},{2,2,3},{3,1,4},{4,2,4},{5,1,3},
                                        {6,2,10},{7,1,3},{8,2,3},{9,1,3}};
    int currentIndex = 0;
    double effectBall1 = 1.0;
    int effectBall2 = 0;
    int eventNum = 0;
    public ForceGeneratorPlug2(double[][] timeForceQue){
        this.timeForceQue = timeForceQue;
    }
    public ForceGeneratorPlug2(){
    }
    public boolean ConnectMaster(String host, int port) {
        // TODO Auto-generated method stub
        return false;
    }

    public String OpenModel(String modelPath) {
        // TODO Auto-generated method stub
        return "ForceGeneratorPlug";
    }

    public void NewPath() {
        // TODO Auto-generated method stub
        currentIndex=0;
    }

    public double doStep(double curTime, double stepSize) {
//        System.out.println(currentIndex);
        if((curTime+stepSize)%1.0==0){
            int index = (int)(curTime - (curTime+stepSize)%1.0);
            effectBall1 = 1.27;
        }else{
            effectBall1 = 1.0;
        }
        return stepSize;
    }


    public double getValues(String varNames) {
        if(currentIndex>0) {
            if (varNames.equals("effectBall1")) {
                return effectBall1 * timeForceQue[currentIndex - 1][2];
            } else if (varNames.equals("effectBall2")) {
                return effectBall2 * timeForceQue[currentIndex - 1][2];
            } else if (varNames.equals("currentIndex"))
                return currentIndex;
        }
        return 0.0;
    }


    public boolean SetValues() {
        effectBall1 = 0;
        effectBall2 = 0;
        return true;
    }
    public boolean SetValues(int varName,double value) {
        switch(varName) {
            case 1:
                effectBall1 = (int) value;
                break;
            case 2:
                effectBall1 = (int) value;
                break;
            case 3:
                currentIndex = (int) value;
                break;
            default:
        }
        return true;
    }
    public double Predict(double Current) throws Exception{
//        int i = currentIndex;
//        for(;i<timeForceQue.length;i++)
//            if(timeForceQue[i][1]!=0)
//                break;
//        try {
//            Thread.sleep(1);
//            System.out.println(Current + "     " + (1.0 - Current%1));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        return 1.0-Current%1.0;
    }
    public boolean terminate(){
        if(currentIndex>timeForceQue.length-1)
            return true;
        else
            return false;
    }
}
