package ecnu.modana.FmiDriver;

public class BouncingBallPlug extends ToolSlave{
	
	private double h=4,g=-9.8,v=0,c=-0.8;
	@Override
	public boolean ConnectMaster(String host, int port) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String OpenModel(String modelPath) {
		// TODO Auto-generated method stub
		return "h";
	}

	@Override
	public void NewPath() {
		// TODO Auto-generated method stub
		h=4;
		v=0;
	}

	@Override
	public double DoStep(double curTime, double stepSize) {
		if(h-g*stepSize*stepSize/2<=0){
			return x;
		}
		else{
			v=v+g*stepSize;
			h=h-g*stepSize*stepSize/2;
		}
		return stepSize;
	}

	@Override
	public String GetValues(String varNames) {
		if(null==varNames){
			return String.valueOf(h);
		}
		else if("h".equals(varNames)) return String.valueOf(h);
		return "";
	}

	@Override
	public boolean SetValues(String varNames, String values) {
		if(null==varNames){
			String[]tem=values.split(",");
			for(int i=0;i<tem.length;i++){
				h=Double.valueOf(tem[i]);
			}
			return true;
		}
		else if("h".equals(varNames)) h=Double.valueOf(values);
		return true;
	}

}
