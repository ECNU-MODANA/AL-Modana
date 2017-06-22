package ecnu.modana.FmiDriver;

import java.util.ArrayList;

public class Controller extends ToolSlave{
	public Controller(){
		slaveId="Controller";
	}
	int n=5,heaterNumbers=3;
	int h[]=new int[n];
	int rh[]=new int[]{0,0,0,0,0}; //房间i使用哪个加热器
	int heater[]=new int[heaterNumbers];
	double power[]=new double[]{6,5,4};
	
	double[] off=new double[]{21, 21, 21, 21, 21};
	double[] on=new double[]{19, 19, 19, 19, 19};
	double[] get=new double[]{16, 17, 18, 17, 16};
	double[] low=new double[]{15, 16, 16, 16, 15};
	
	double[] Room=new double[]{7, 7, 7, 7, 7}; //input
	double[] humans=new double[]{0,0,0,0,0};
	double[] unCom=new double[]{0,0,0,0,0};
	double derAllUn=0,derEnergy=0;
	int needNum=0;
	int xzh=heaterNumbers;
	
	int[] sortUn=new int[5];
	@Override
	public String OpenModel(String modelPath) {
		slaveId="Controller";
		//h[0]=1;
		variableTypes=new ArrayList<>();
		for(int i=0;i<n;i++)
			variableTypes.add("Double");
		return "h1,h2,h3,h4,h5,derAllUn,room1,room2,room3,room4,room5,humansNum1,humansNum2,humansNum3,humansNum4,humansNum5";
	}

	@Override
	public double DoStep(double curTime, double stepSize) {
		int i,j;
		needNum=0;
		xzh=0;
		derAllUn=0;
		for(i=0;i<n;i++){
			if(Room[i]<low[i]&&humans[i]>0)
				unCom[i]=humans[i]*(low[i]-Room[i]);
			else unCom[i]=0;
			if(Room[i]<get[i]&&humans[i]>0) {
				needNum++;
			}
			else if(Room[i]>=on[i]&&h[i]>0){
				heater[rh[i]]=0;
				h[i]=0;
				xzh++;
				//unCom[i]=0;
			}
			derAllUn+=unCom[i];
		}
		for(i=0,derEnergy=0;i<3;i++)
			if(heater[i]>0) derEnergy+=5;//power[i];
		if(needNum==0) return stepSize;
		for(i=0;i<heaterNumbers;i++) {
			//if(heater[i]==0) xzh++;
			heater[i]=0;
		}
		for(i=0;i<n;i++){
			h[i]=0;
			rh[i]=0;
		}
		Sort(unCom);
		if(needNum>0){
			for(i=0;i<n;i++)
				if(Room[sortUn[i]]<get[sortUn[i]]&&humans[sortUn[i]]>0&&h[sortUn[i]]==0){
					for(j=0;j<heaterNumbers;j++)
						if(heater[j]==0){
							h[sortUn[i]]=1;
							rh[sortUn[i]]=j;
							heater[j]=sortUn[i];
							break;
						}
				}
		}
		for(i=0,derEnergy=0;i<3;i++)
			if(heater[i]>0) derEnergy+=5;//power[i];
		return stepSize;
	}

	@Override
	public String GetValues(String varNames) {
		int i;
//		String t="";
//		for(i=0;i<n-1;i++)
//			t+=humans[i]+",";
//		t+=humans[i];
//		System.out.println(t);
		if(null==varNames){
			String res="";
			for(i=0;i<n;i++) res+=h[i]+",";
			res+=derAllUn;
			return res;
		}
		if("h1".equals(varNames)) return h[0]+"";
		else if("h2".equals(varNames)) return h[1]+"";
		else if("h3".equals(varNames)) return h[2]+"";
		else if("h4".equals(varNames)) return h[3]+"";
		else if("h5".equals(varNames)) return h[4]+"";
		else if("derEnergy".equals(varNames)) return derEnergy+"";
		else return derAllUn+"";
	}
	private double[] tds=new double[n];
	private void Sort(double unCom[]){ //从大到小排序
		int i,t;
		for(i=0;i<n;i++) tds[i]=unCom[i];
		for(i=0;i<n;i++){
			t=i;
			for(int j=0;j<n;j++)
				if(tds[t]<tds[j]){
					t=j;
				}
			sortUn[i]=t;
			tds[t]=-9e30;
		}			
	}
	@Override
	public boolean SetValues(String varNames, String values) { 
		if(null==varNames){
			String[]tem=values.split(",");
			for(int i=0;i<tem.length;i++){
				Room[i]=Double.valueOf(tem[i]);
			}
			return true;
		}
		if("Room1".equals(varNames)) Room[0]=Double.valueOf(values);
		else if("Room2".equals(varNames)) Room[1]=Double.valueOf(values);
		else if("Room3".equals(varNames)) Room[2]=Double.valueOf(values);
		else if("Room4".equals(varNames)) Room[3]=Double.valueOf(values);
		else if("Room5".equals(varNames)) Room[4]=Double.valueOf(values);
		
		else if("humans1".equals(varNames)) humans[0]=Double.valueOf(values);
		else if("humans2".equals(varNames)) humans[1]=Double.valueOf(values);
		else if("humans3".equals(varNames)) humans[2]=Double.valueOf(values);
		else if("humans4".equals(varNames)) humans[3]=Double.valueOf(values);
		else if("humans5".equals(varNames)) humans[4]=Double.valueOf(values);
		return true;
	}
	@Override
	public boolean ConnectMaster(String host, int port) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void NewPath() {
		// TODO Auto-generated method stub
		
	}	
}
