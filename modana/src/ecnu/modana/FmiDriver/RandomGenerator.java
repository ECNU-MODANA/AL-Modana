package ecnu.modana.FmiDriver;

import java.util.Random;

public class RandomGenerator extends PlugInSlave{
    public boolean strategy_Room1,strategy_Room2;
    public boolean triggered;
    public Random random = null;
    Room room1 = null;
    @Override
    public void NewPath() {
    }
    public RandomGenerator(int t){
        this.random = new Random(t);
        this.triggered = false;
        this.strategy_Room1 = false;
        this.strategy_Room2 = false;
    }

    @Override
    public double DoStep(double curTime, double stepSize) {
        double offset = 1e-5;
        if(curTime%1<=offset && (curTime+stepSize)%1<=offset && !triggered){
            triggered = true;
            this.strategy_Room1 = (random.nextInt(10)%2==0);
            this.strategy_Room2 = !this.strategy_Room1;
        }else if(curTime%1<=offset && (curTime+stepSize)%1>offset)
            return -1;
        else if(curTime%1>offset){
            if((curTime+stepSize)%1<=offset){
                triggered = true;
                this.strategy_Room1 = (random.nextInt(10)%2==0);
                this.strategy_Room2 = !this.strategy_Room1;
            }else {
                triggered = false;
                return stepSize;
            }
        }

        return -1;
    }

    @Override
    public double GetValues(String varNames) {
        if(varNames.equals("strategy_Room1"))
            return (this.strategy_Room1 ? 1.0:0.0);
        return (strategy_Room1? 1.0: 0.0);
    }

    @Override
    public boolean SetValues(String varNames, double values) {
        if(varNames.equals("strategy_Room1"))
            strategy_Room1 = true;
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
