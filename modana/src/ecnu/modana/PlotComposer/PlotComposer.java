package ecnu.modana.PlotComposer;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

import ecnu.modana.FmiDriver.SlaveTrace;
import ecnu.modana.FmiDriver.State;
import ecnu.modana.FmiDriver.Trace;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TextField;
import ecnu.modana.PlotComposer.JBarChart;

/**
 * @author JKQ
 *
 * 2015年11月29日下午3:42:11
 */
public class PlotComposer {
	private TextField xProperty,yProperty;
	List<Object> stepList = new ArrayList<Object>();
	List<Number> stateList = new ArrayList<Number>();
    List<Number> variableList = new ArrayList<Number>();
    private List<List<Number>> yDataList=new ArrayList<List<Number>>();
	public void SetXYList() {
		stepList.add(0.1);
		stepList.add(0.2);
		stepList.add(0.3);
		stepList.add(0.4);
		stepList.add(0.5);
		stepList.add(0.6);
		stepList.add(0.7);
		stepList.add(0.8);

		variableList.add(1);
		variableList.add(2);
		variableList.add(3);
		variableList.add(4);
		variableList.add(5);
		variableList.add(4);
		variableList.add(3);
		variableList.add(2);

	}
	public void SetXYList(Trace trace){
	    int stepNum = 0;
		if (trace!=null &&trace.slaveMap.size()>0) {
			Iterator it = trace.slaveMap.keySet().iterator();
			if (it.hasNext())
			    stepNum = trace.slaveMap.get((String)it.next()).statesList.size();
		}
        Iterator it = trace.slaveMap.keySet().iterator();
        Map<String,List<List<Number>>> yMap = new HashMap<>();
        int countX = 0;
        while(it.hasNext()){
            countX++;
            List<List<Number>> sLaveYDataList=new ArrayList<List<Number>>();
            SlaveTrace slave = trace.slaveMap.get(it.next());
            for(int s =0;s<slave.statesList.get(0).values.size();s++)
                sLaveYDataList.add(new ArrayList<Number>());
            for(int i=0;i<slave.statesList.size();i++){
                State state = slave.statesList.get(i);
                for (int j=0;j<state.values.size();j++){
                    if (countX==1)
                        if(j==0)
                            stepList.add(state.time);
                    sLaveYDataList.get(j).add((double)state.values.get(j));
                }
            }
            yMap.put(slave.slaveName,sLaveYDataList);
        }
        it = yMap.keySet().iterator();
        while(it.hasNext()){
            List list = yMap.get(it.next());
            for(int i = 0; i<list.size(); i++)
                yDataList.add((List<Number>) list.get(i));
        }

	}
	public LineChart<Object, Number> getLineChart(TextField xAxisField,TextField yAxisField) throws Exception {
		this.xProperty = xAxisField;
		this.yProperty = yAxisField;
		JLineChart jLineChart = null;
		jLineChart = new JLineChart();
		jLineChart.SetX(stepList);
//		jLineChart.SetY(stateList);
//		jLineChart.SetY(variableList);
        jLineChart.SetYList(yDataList);
		return jLineChart.getJLineChart(xProperty,yProperty);
	}
	public BarChart<String, Number> getBarChart(TextField xAxisField,TextField yAxisField) throws Exception {
		
		this.xProperty = xAxisField;
		this.yProperty = yAxisField;
		JBarChart jbarChart = null;
		jbarChart = new JBarChart();
		jbarChart.SetX(stepList);
		jbarChart.SetY(stateList);
		jbarChart.SetY(variableList);
		return jbarChart.getJBarChart(xProperty,yProperty);
	}
}
