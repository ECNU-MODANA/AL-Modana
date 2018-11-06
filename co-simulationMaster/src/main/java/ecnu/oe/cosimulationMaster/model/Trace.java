package ecnu.oe.cosimulationMaster.model;

import java.util.ArrayList;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/10/30.
 */
public class Trace {
    //轨迹中的具体值
    ArrayList<State> traceValues;
    //轨迹的值所代表的变量
    ArrayList<String> traceNames;

    public Trace(ArrayList<State> traceValues, ArrayList<String> traceNames) {
        this.traceValues = traceValues;
        this.traceNames = traceNames;
    }

    public ArrayList<State> getTraceValues() {
        return traceValues;
    }

    public ArrayList<String> getTraceNames() {
        return traceNames;
    }

    public void setTraceValues(ArrayList<State> traceValues) {
        this.traceValues = traceValues;
    }

    public void setTraceNames(ArrayList<String> traceNames) {
        this.traceNames = traceNames;
    }
}
