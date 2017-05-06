package ecnu.modana.FmiDriver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.text.rtf.RTFEditorKit;

public class HumansActivity extends ToolSlave{
	int n=5;
	class Behaviors{
		public double lamda,time;
		public int from,to;
		public int idx;
	}
	private double lamdas[]=new double[]{//0-4，到外面；5-9外面进入房间；10-21房间内流动
		0,0,0,0,0,
		0,0,0,0,0,
		0,0,0,0,0,
		0,0,0,0,0,0,0
	};

	private double maxHumans[]=new double[]{1e30,50,50,50,50,100};
	private double humanNums[];
	private Queue<Behaviors> operationQ ;
	private double startT=7,endT=22,startYr=5;
	private String mountRequest_1;
	private String mountRequest_2;
	@Override
	public String OpenModel(String modelPath){
		slaveId="HumansActivity";
		mountRequest_1=slaveId+"/0/"+"/"+slaveId+"/notActive";
		mountRequest_2=slaveId+"/"+startYr+"//"+slaveId+"/exchangeableActive";
		operationQ = new PriorityQueue<>(1, HumansComparator);
		//humanNums=new double[]{9e30,5,0,7,8,9};
		humanNums=new double[]{9e30,0,0,0,0,0};
		for(int i=0;i<22;i++){
			Behaviors behaviors=new Behaviors();
			behaviors.idx=i;
			behaviors.time=startYr;
			if(i<=4)
			{
				behaviors.lamda=2;
				behaviors.from=i+1;
				behaviors.to=0;
			}
			else if(i<=9){
				behaviors.lamda=3;
				behaviors.from=0;
				behaviors.to=i-4;
			}
			else behaviors.lamda=3;
			switch (i) {
			case 10:
				behaviors.from=1;
				behaviors.to=2;
				break;
			case 11:
				behaviors.from=1;
				behaviors.to=5;
				break;
			case 12:
				behaviors.from=2;
				behaviors.to=1;
				break;
			case 13:
				behaviors.from=2;
				behaviors.to=3;
				break;
			case 14:
				behaviors.from=2;
				behaviors.to=4;
				break;
			case 15:
				behaviors.from=3;
				behaviors.to=2;
				break;
			case 16:
				behaviors.from=3;
				behaviors.to=4;
				break;
			case 17:
				behaviors.from=4;
				behaviors.to=2;
				break;
			case 18:
				behaviors.from=4;
				behaviors.to=3;
				break;
			case 19:
				behaviors.from=4;
				behaviors.to=5;
				break;
			case 20:
				behaviors.from=5;
				behaviors.to=1;
				break;
			case 21:
				behaviors.from=5;
				behaviors.to=4;
				break;
			default:
				break;
			}
			//behaviors.time=zsfb(behaviors.lamda);
			operationQ.add(behaviors);
		}
		ValueQ(0,0,0,0);
		return "mountRequest_1,mountRequest_2,humanNums1,humanNums2,humanNums3,humanNums4,humansNum5";
	}
	private void Ini(double currentTime, double stepSize){
		double curTime=currentTime%24;
		if(curTime<=startYr&&(curTime+stepSize)>=startYr){
			int i;
			for(i=1;i<=n;i++) humanNums[i]=1e-30;
			mountRequest_1=slaveId+"/0/"+"/"+slaveId+"/notActive";
			mountRequest_2=slaveId+"/"+startT+"//"+slaveId+"/exchangeableActive";
			lamdas=new double[]{
					0,0,0,0,0,
					0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0
				};
			ValueQ(0,0,0,currentTime);
		}
		else if(curTime<=startT&&(curTime+stepSize)>=startT){ //start work
			lamdas=new double[]{
				5,5,5,5,5,
				17,14,14,14,40,
				4,4,4,4,4,4,4,4,4,4,6,6
			};
			ValueQ(2, 7, 4,currentTime);
		}
		else if((curTime<=11&&curTime+stepSize>=11)||(curTime<=17&&curTime+stepSize>=17)) //start eat
		{
			lamdas=new double[]{
					25,24,24,24,47,
					15,13,13,13,34,
					4,4,4,4,4,4,4,4,4,4,6,6
				};
				ValueQ(2, 7, 4,currentTime);
		}
		else if((curTime<=13&&curTime+stepSize>=13)||(curTime<=19&&curTime+stepSize>=19)) //end eat
		{
			lamdas=new double[]{
					12,10,10,10,18,
					18,15,15,15,34,
					4,4,4,4,4,4,4,4,4,4,7,7
				};
				ValueQ(2, 7, 4,currentTime);
		} 
		else if(curTime<=endT&&(curTime+stepSize)>=endT){ //start out
			lamdas=new double[]{
					14,15,15,15,28,
					5,3,3,3,8,
					1,1,1,1,1,1,1,1,1,1,2,2
				};
			ValueQ(15, 2, 0,currentTime);
		}
		else if(curTime<=endT+1&&(curTime+stepSize)>=endT+1){ //out
			lamdas=new double[]{
					48,50,50,50,99,
					0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0
				};
			ValueQ(150, 0, 0,currentTime);
		}
//		else if(curTime%24<=endT+2&&(curTime+stepSize)%24>=endT+2){
//			ValueQ(10000, 0, 0,curTime);
//		}
	}
	private void ValueQ(double out,double in,double hx,double curTime){
		Behaviors behaviors;
		ArrayList<Behaviors> tList=new ArrayList<>();
		while(operationQ.size()>0){
			behaviors=operationQ.remove();
			behaviors.lamda=lamdas[behaviors.idx];
//			if(behaviors.idx<5)
//				behaviors.lamda=out;
//			else if(behaviors.idx<10)
//				behaviors.lamda=in;
//			else behaviors.lamda=hx;
			if(behaviors.lamda>0)
				behaviors.time=zsfb(behaviors.lamda)+curTime;
			tList.add(behaviors);
		}
		for(Behaviors b : tList)
			operationQ.add(b);
	}
	@Override
	public double DoStep(double curTime, double stepSize){
		Ini(curTime,stepSize);
		Behaviors behaviors;
		if(curTime>=startYr) {
		 curTime+=1e-30;
		}
		//curTime+=stepSize;
		while(operationQ.size()>0){
			behaviors=operationQ.peek();
			if(behaviors.time>=curTime&&behaviors.time<=curTime+stepSize){
				if(behaviors.from!=0&&humanNums[behaviors.from]>=1) 
					humanNums[behaviors.from]=(int)(humanNums[behaviors.from]-1);
				if(behaviors.to!=0&&humanNums[behaviors.from]>=1&&humanNums[behaviors.to]<maxHumans[behaviors.to]
						&&curTime>=startT-stepSize) 
					humanNums[behaviors.to]=(int)(humanNums[behaviors.to]+1);			
				operationQ.remove();
//				if(curTime%24<=startT-stepSize||curTime%24>=endT+stepSize) 
//					behaviors.time=startT+(int)(curTime/24)*24;
//				else 
					behaviors.time=zsfb(behaviors.lamda)+behaviors.time;
				operationQ.add(behaviors);
			}
			else break;
		}
		double res=operationQ.peek().time-curTime-stepSize;
		if(curTime%24>=endT){
			int i;
			for(i=1;i<=n;i++)
				if(humanNums[i]>=1) break;
			if(i==n+1){
				mountRequest_1=slaveId+"/0/"+"/"+slaveId+"/notActive";
				mountRequest_2=slaveId+"/"+startYr+"//"+slaveId+"/exchangeableActive";
			}
		}
		if(res<stepSize) 
			res=stepSize;
		if(res>9e30)
			res= stepSize;
		return res;
		//return res-curTime;
	}
	@Override
	public String GetValues(String varNames){
		String res="";;
		if(null==varNames){
			res=mountRequest_1+","+mountRequest_2+","+humanNums[1];
			for(int i=2;i<=n;i++)
				res+=","+humanNums[i];
			return res;
		}
		if("humanNums1".equals(varNames)) return res+humanNums[1];
		else if("humanNums2".equals(varNames)) return res+humanNums[2];
		else if("humanNums3".equals(varNames)) return res+humanNums[3];
		else if("humanNums4".equals(varNames)) return res+humanNums[4];
		else if("humanNums5".equals(varNames)) return res+humanNums[5];
		else if("mountRequest_1".equals(varNames)) 
			return mountRequest_1;
		else if("mountRequest_2".equals(varNames)) 
			return mountRequest_2;
		return res;
	}
	@Override
	public boolean SetValues(String varNames,String values){
		if("mountRequest_1".equals(varNames))
			mountRequest_1=values;
		else if("mountRequest_2".equals(varNames))
			mountRequest_2=values;
		return true;
	}
	public static Comparator<Behaviors> HumansComparator = new Comparator<Behaviors>(){
		@Override
		public int compare(Behaviors o1, Behaviors o2) {
			return o1.time-o2.time>0?1:-1;
		}
	};
	private double zsfb(double lamda){
		double z = Math.random();
		double x = -(1 / lamda) * Math.log(z);
		return x;
	}
	@Override
	public boolean ConnectMaster(String host, int port) {
		return true;
	}
	@Override
	public void NewPath() {
		// TODO Auto-generated method stub
		
	}
}
