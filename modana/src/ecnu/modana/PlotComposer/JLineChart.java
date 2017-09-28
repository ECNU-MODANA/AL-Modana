package ecnu.modana.PlotComposer;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;

/**
 * @author JKQ
 *
 * 2015年11月29日下午3:42:06
 */
public class JLineChart extends JChart
{
	private List<Object>xDataList=null;
	private String xName;
	private List<List<Number>> yDataList=new ArrayList<List<Number>>();
	private List<String>yNameList=new ArrayList<>();
	//private String chartTitle="chart";
//	private StringProperty chartTitle=new SimpleStringProperty("chart");
	private LineChart<Object, Number> lineChart = null;
	public void SetX(List<Object> xDataList) {this.xDataList=xDataList;}
	public void SetY(List<Number>yDataList){this.yDataList.add(yDataList);}
	public void SetX(List<Object> xDataList,String xName) {this.xDataList=xDataList;this.xName=xName;}
	public void SetY(List<Number>yDataList,String yName){this.yDataList.add(yDataList);this.yNameList.add(yName);}
	public void SetSeriesName(List<String> list){this.yNameList = list;}
	public void SetYList(List<List<Number>> yDataList){this.yDataList = yDataList;}

//	@Override
	public LineChart<Object, Number> getJLineChart(TextField xAxisField,TextField yAxisField)  throws Exception 
	{
		final Axis xAxis;
	    final NumberAxis yAxis = new NumberAxis();
		if(xDataList.get(0) instanceof String)
			xAxis = new CategoryAxis();
		else 
			xAxis=new NumberAxis();
		if(null!=xName) xAxis.setLabel(xName);
		yAxis.labelProperty().bindBidirectional(yAxisField.textProperty());
		xAxis.labelProperty().bindBidirectional(xAxisField.textProperty());
		lineChart = new LineChart<>(xAxis, yAxis);
		for(int i=0,j=yDataList.size();i<j;i++)
		{
			List<Number> yList=yDataList.get(i);
			XYChart.Series series = new XYChart.Series();
			series.setName(yNameList.get(i));
		    for(int k=0;k<yList.size();k++)
		    	series.getData().add(new XYChart.Data(xDataList.get(k),yList.get(k)));
		    lineChart.getData().add(series);
		}  
		return lineChart;
	}
	
	public LineChart<Object, Number> getJLineChart()  throws Exception 
	{
		final Axis xAxis;
	    final NumberAxis yAxis = new NumberAxis();
		if(xDataList.get(0) instanceof String)
			xAxis = new CategoryAxis();
		else 
			xAxis=new NumberAxis();
		if(null!=xName) xAxis.setLabel(xName);
//		yAxis.labelProperty().bindBidirectional(new TextField().textProperty());
//		xAxis.labelProperty().bindBidirectional(new TextField().textProperty());
		lineChart = new LineChart<>(xAxis, yAxis);
		//this.chartTitle=stringProperty;
		for(int i=0,j=yDataList.size();i<j;i++)
		{
			List<Number> yList=yDataList.get(i);
			XYChart.Series series = new XYChart.Series();
//		    if(yNameList.size()>i) series.setName(yNameList.get(i));
			series.setName(String.valueOf(i));
		    for(int k=0;k<yList.size();k++)
		    	series.getData().add(new XYChart.Data(xDataList.get(k),yList.get(k)));
		    lineChart.getData().add(series);
		}  
		return lineChart;
	}
//	@Override
//	public void run() {
//		try {
//			System.err.println("aht");
//			start(new Stage());
//		} catch (Exception e) {
//			System.err.println("ah");
//			e.printStackTrace();
//		}		
//	}	
}
