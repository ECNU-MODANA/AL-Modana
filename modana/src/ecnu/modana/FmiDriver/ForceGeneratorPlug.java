package ecnu.modana.FmiDriver;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/11/12.
 */
public class ForceGeneratorPlug {
    double[][] timeForceQue = {{1,1,2},{2,2,3},{3,1,4},{4,2,4},{5,1,3},
                                        {6,2,10},{7,1,3},{8,2,3},{9,1,3}};
    int currentIndex = 0;
    int effectBall1 = 0;
    int effectBall2 = 0;
    int eventNum = 0;
    public ForceGeneratorPlug(double[][] timeForceQue){
        this.timeForceQue = timeForceQue;
    }
    public ForceGeneratorPlug(){
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
        double effect = timeForceQue[currentIndex][1];
        if(effect!=0) {
            double ddddd = curTime + stepSize - timeForceQue[currentIndex][0];
            if (ddddd>0.00001)
                return -1;
            else if (Math.abs(ddddd) <= 0.00001) {
                eventNum++;
                if (effect == 1) {
                    effectBall2 = 0;
                    effectBall1 = 1;
                }
                else if (effect == 2) {
                    effectBall1 = 0;
                    effectBall2 = 1;
                }
                currentIndex++;
                return stepSize;
            }
            else {
                effectBall1 = 0;
                effectBall2 = 0;
                return stepSize;
            }
        }else{
            effectBall1 = 0;
            effectBall2 = 0;
            return stepSize;
        }
    }


    public double getValues(String varNames) {
        if(varNames.equals("effectBall1")){
            return effectBall1*timeForceQue[currentIndex][2];
        }else if(varNames.equals("effectBall2")){
            return effectBall2*timeForceQue[currentIndex][2];
        }else if(varNames.equals("currentIndex"))
            return currentIndex;

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
    public double Predict(double Current){
        int i = currentIndex;
        for(;i<timeForceQue.length;i++)
            if(timeForceQue[i][1]!=0)
                break;
        return timeForceQue[i][0]-Current;
    }
    public boolean terminate(){
        if(currentIndex>timeForceQue.length-1)
            return true;
        else
            return false;
    }
}
