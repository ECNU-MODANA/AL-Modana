package ecnu.modana.FmiDriver;

import java.util.Random;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/12/24.
 */
public class PipeController extends PlugInSlave{
    double Tank1PipeIn = 2.0,Tank1PipeOut = 1.0, Tank2PipeIn = 1.0,Tank2PipeOut = 0.57;
    double Tank1Height = 0.0, Tank2Height = 0.0;
    double Tank1MaxHeight = 20.0,Tank2MaxHeight = 10.0;
    @Override
    public void NewPath() {

    }

    @Override
    public double DoStep(double curTime, double stepSize) {
        Random randomNum = new Random(System.currentTimeMillis());
        if(Math.abs(Tank1MaxHeight - Tank1Height) <= 0.01)
            Tank1PipeIn = 0.0;
        else if(Tank1MaxHeight< Tank1Height)
            return -1;
        else if(Math.abs(Tank1Height)<=0.01)
            Tank1PipeIn = 2.0;
        else if(Tank1Height < 0)
            return -1;

        if(Math.abs(Tank2MaxHeight - Tank2Height) <= 0.01) {
            Tank1PipeOut = Tank2PipeIn = 0.0;
            Tank2PipeOut = 0.57;
        } else if(Tank2MaxHeight< Tank2Height)
            return -1;
        else if(Math.abs(Tank2Height)<=0.02 && Tank2Height>0) {
            Tank1PipeOut = Tank2PipeIn = 1.0;
            Tank2PipeOut = 0.0;
        }
        else if(Tank2Height < -0.01)
            return -1;

        return stepSize;
    }

    @Override
    public double GetValues(String varNames) {
        return 0;
    }

    @Override
    public boolean SetValues(String varNames, double values) {
        if(varNames.equals("Tank1PipeIn"))
                Tank1PipeIn = values;
        if(varNames.equals("Tank1PipeOut"))
                Tank1PipeOut = values;
        if(varNames.equals("Tank2PipeIn"))
                Tank2PipeIn = values;
        if(varNames.equals("Tank2PipeOut"))
                Tank2PipeOut = values;
        if(varNames.equals("Tank1Height"))
            Tank1Height = values;
        if(varNames.equals("Tank2Height"))
            Tank2Height = values;
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
