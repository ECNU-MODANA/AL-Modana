package ecnu.modana.FmiDriver;

import java.util.Random;

public class HeaterController extends PlugInSlave{
    double Room1switch,temperatureRoom1;
    Room room1 = null;
    @Override
    public void NewPath() {
    }
    public HeaterController(Room room){
        room1 = room;
    }

    @Override
    public double DoStep(double curTime, double stepSize) {
        if(temperatureRoom1>=19 && temperatureRoom1<=23) {
            if (Math.abs(room1.temperature - 23) <= 0.01){
                Room1switch = 0;
                return stepSize;
            }else if(room1.temperature>23){
                return -1;
            }else if(Math.abs(room1.temperature - 19) <= 0.01){
                Room1switch = 0;
                return stepSize;
            }else if(room1.temperature< 19){
                return -1;
            }else
                Room1switch = 0;
                return stepSize;
        }else if(temperatureRoom1>23){
            if(room1.temperature<=19) {
                return -1;
            }else
                Room1switch = 0.0;
                return stepSize;
        }else {
            if(room1.temperature>=23)
                return -1;
            else{
                Room1switch = 1.0;
                return stepSize;
            }
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
