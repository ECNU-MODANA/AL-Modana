package ecnu.modana.FmiDriver;
import java.math.BigDecimal;

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
		return "h,v";
	}

	@Override
	public void NewPath() {
		h=4;
		v=0;
	}

	@Override
	public double DoStep(double curTime, double stepSize) {
//		if(curTime>=3){
//			curTime+=1e-20;
//		}
		//if(h==0) v=v*c; //防止回滚后。。。
		double t=QXSW(h+(v+v+g*stepSize)*stepSize/2,5);
		if(t<0){ //落地
			double td=Math.sqrt(Math.abs(v*v-2*g*h));
			//double td1=(-1*v+td)/g;
			double td2=(-1*v-td)/g;
			v=v*c;
			h=0;
			if(td2==0) return stepSize;
			return td2;
		}
		else if(v>0&&v+g*stepSize<0){//v向上时==0，即v由正变成负数
			double td=Math.abs(v/g);
			h=h-g*td*td/2;
			v=0;
			return td;
		}
		else{
			v=v+g*stepSize;
			h=t;
			if(h==0) 
				v=v*c;
		}
		return stepSize;
	}

	@Override
	public String GetValues(String varNames) {
		if(null==varNames){
			return String.valueOf(h)+","+String.valueOf(v);
		}
		else if("h".equals(varNames)) return String.valueOf(h);
		else if("v".equals(varNames)) return String.valueOf(v);
		return "";
	}

	@Override
	public boolean SetValues(String varNames, String values) {
		if(null==varNames){
			String[]tem=values.split(",");
			int i=0;
			//for(int i=0;i<tem.length;i++){
				h=Double.valueOf(tem[i++]);
				v=Double.valueOf(tem[i]);
			//}
			return true;
		}
		else if("h".equals(varNames)) h=Double.valueOf(values);
		else if("v".equals(varNames)) v=Double.valueOf(values);
		return true;
	}
	public double Predict(double time,double stepSize){
		double t=QXSW(h+(v+v+g*stepSize)*stepSize/2,5);
		if(t<0){ //落地
			double td=Math.sqrt(Math.abs(v*v-2*g*h));
			//double td1=(-1*v+td)/g;
			double td2=(-1*v-td)/g;
			if(td2==0) return stepSize;
			return td2;
		}
		else if(v>0&&v+g*stepSize<0){//v向上时==0，即v由正变成负数
			double td=Math.abs(v/g);
			return td;
		}
	   return stepSize;
	}
	private double QXSW(double f,int ws){
		BigDecimal   b   =   new   BigDecimal(f);  
		return b.setScale(2,  ws).doubleValue();  
	}
}
