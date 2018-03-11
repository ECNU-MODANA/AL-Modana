package ecnu.modana.FmiDriver;

public class HeaterController extends PlugInSlave{
    public double Room1switch,temperatureRoom1,pre,assistantVar;
    public boolean strategy_Room1;
    Room room1 = null;
    @Override
    public void NewPath() {
    }
    public HeaterController(double t){
        pre = temperatureRoom1 = t;
        Room1switch = 0;
    }

    @Override
    public double DoStep(double curTime, double stepSize) {
        double offset = 1e-5;
        if(curTime%24 >=5 && curTime%24 <=22) {
            if(strategy_Room1) {
                if (curTime % 24 + stepSize - 22 >= offset)
                    return -1;
                if (temperatureRoom1 >= 19 && temperatureRoom1 <= 23) {
                    return stepSize;
                } else if (temperatureRoom1 > 23) {
                    if ((temperatureRoom1 - 23) > offset) {
                        return -1;
                    } else
                        Room1switch = 0.0;
                    return stepSize;
                } else {

                    if (19 - temperatureRoom1 > offset)
                        if (pre < 19) {
                            Room1switch = 1;
                            return stepSize;
                        } else
                            return -1;
                    else {
                        Room1switch = 1.0;
                        return stepSize;
                    }
                }
            }else{
                Room1switch = 0.0;
                return stepSize;
            }
        }else if(curTime%24 <5){
            if(strategy_Room1) {
                if (Math.abs(5 - (curTime % 24 + stepSize)) <= offset) {
                    if (temperatureRoom1 < 19) {
                        Room1switch = 1;
                        return stepSize;
                    } else
                        return 1;
                } else if (curTime % 24 + stepSize > 5) {
                    return -2;
                } else
                    return stepSize;
            }else {
                Room1switch = 0;
                return stepSize;
                }
            }else{
            Room1switch = 0;
            return stepSize;
        }

    }

    @Override
    public double GetValues(String varNames) {
        if(varNames.equals("Room1switch"))
            return Room1switch;
        return Room1switch;
    }

    @Override
    public boolean SetValues(String varNames, double values) {
        if(varNames.equals("temperatureRoom1"))
            temperatureRoom1 = values;
        return true;
    }

    @Override
    public void FreeSlave() {
        super.FreeSlave();
    }

    @Override
    public boolean RollBack(SlaveTrace slaveTrace, State state) {
        return super.RollBack(slaveTrace, state);
    }

    public void setRoom1switch(double room1switch,double temperatureRoom1,boolean strategy_Room1) {
        Room1switch = room1switch;
        this.Room1switch = room1switch;
        this.strategy_Room1 = strategy_Room1;
    }
}
