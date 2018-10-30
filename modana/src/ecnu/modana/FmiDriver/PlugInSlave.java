package ecnu.modana.FmiDriver;

import java.util.ArrayList;

import ecnu.modana.FmiDriver.bean.State;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2017/12/24.
 */
public abstract class PlugInSlave {
    public String slaveId="";
    public String toolPath="";
    public ArrayList<String> variableTypes;
    //public MinaClient minaClient;
    public abstract void NewPath();
    public abstract double DoStep(double curTime,double stepSize);
    public abstract double GetValues(String varNames);
    public abstract boolean SetValues(String varNames,double values);
    public void FreeSlave(){};

    public boolean RollBackByStep(SlaveTrace slaveTrace, int stepsOfBack){
        int to=slaveTrace.statesList.size()-1-stepsOfBack;
        if(to<0) to=0;
        return RollBack(slaveTrace, slaveTrace.statesList.get(to));
    }
    public boolean RollBack(SlaveTrace slaveTrace, State state){
        try {
            int i=0;
            Object obj;
            for(String varName:slaveTrace.varNameType.keySet()){
                obj=state.values.get(i++);
//                SetValues(varName,  Double.valueOf(obj));
            }
//        	invoke(setTime, new Object[] { fmiComponent, state.time },
//                    "RollBack Could not set time, time was " + state.time + ": ");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public double Predict(double time,double stepSize){
        return stepSize;
    }
}
