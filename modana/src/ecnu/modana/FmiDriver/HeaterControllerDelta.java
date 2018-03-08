package ecnu.modana.FmiDriver;

public class HeaterControllerDelta extends PlugInSlave{
    public double Room1switch,temperatureRoom1,pre,assistantVar;
    Room room1 = null;
    @Override
    public void NewPath() {
    }
    public HeaterControllerDelta(double t){
        pre = temperatureRoom1 = t;
    }

    @Override
    public double DoStep(double curTime, double stepSize) {
        if(curTime%24 >=5 && curTime%24 <=22) {
            if(curTime%24+stepSize-22>=0.01)
                return -1;
            if (temperatureRoom1 >= 19 && temperatureRoom1 <= 23) {
                if (Math.abs(assistantVar - 23) <= 0.01) {
                    Room1switch = 0;
                    return stepSize;
                } else if (assistantVar > 23) {
                    return -1;
                } else if (assistantVar>=19) {
                    return stepSize;
                } else if ((19-assistantVar) <=0.01) {
                    Room1switch = 1;
                    return stepSize;
                } else
                    return -1;
            } else if (temperatureRoom1 > 23) {
                if ((assistantVar - 23) > 0.01) {
                    return -1;
                } else
                    Room1switch = 0.0;
                return stepSize;
            } else {
//                    Room1switch = 1.0;
//                    return stepSize;
                if (19 - temperatureRoom1 <= 0.01){
                    Room1switch = 1;
                    return stepSize;
                }
                else {
                    Room1switch = 1.0;
                    return stepSize;
                }
            }
        }else if(curTime%24 <5){
            if(Math.abs(5-(curTime%24+stepSize)) <=0.01){
                if(temperatureRoom1 < 19){
                    Room1switch = 1;
                    return stepSize;
                }else
                    return stepSize;
            }else if(curTime%24+stepSize >5){
                return -1;
            }else
                return stepSize;
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
}
