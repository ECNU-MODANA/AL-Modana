package ecnu.modana.alsmc.smc;

import ecnu.modana.FmiDriver.StepRestrictionMasterAlgorithm;
import ecnu.modana.alsmc.main.ExeUppaal;
import ecnu.modana.alsmc.main.State;
import ecnu.modana.alsmc.modelCheck.Check;
import ecnu.modana.util.FileHandler;

import java.util.ArrayList;

	public class SMCTest {
		
//		public static ThesisCaseStudy tcs = new ThesisCaseStudy(20);
		
	public static void main(String[] args) throws Exception{
//        NativeLibrary.getInstance("G:\\TMP\\FMUFile4557899740853020759.tmp\\binaries\\win32\\smartBuilding_rooB.dll");
        FileHandler.delFolder("G:/TMP2");
	    bietTest();
//		bhtTest();
		//apmcTest();
	//	sprtTest();
		//ciTest();
	//	aciTest();
	}
	public static void aciTest() {
		ACIAlgorithm algorithm = new ACIAlgorithm(0, 0, 0.01, 0.1);
		do {
			//ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;
			if (Check.checkTrace(sl, "P=?[F T0>31 & T1<4 ]")) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			System.out.println(algorithm.getP()+", "+algorithm.getN()+", "+algorithm.getX());
		} while (!algorithm.run());
		System.out.println(algorithm.getP()+", "+algorithm.getN()+", "+algorithm.getX());
	}
	
	public static void apmcTest() {
		APMCAlgorithm algorithm = new APMCAlgorithm(0, 0, 0.02, 0.05);
		do {
			//ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;
			if (Check.checkTrace(sl, "P=?[F T0>31 & T1<4 ]")) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			System.out.println(algorithm.getMaxN()+", "+algorithm.getN()
					+", "+algorithm.getX() +", "+algorithm.getP());
		} while (!algorithm.run());
		System.out.println(algorithm.getN() +", "+algorithm.getX() +", "+algorithm.getP());
	}
	
	public static void ciTest() {
		CIAlgorithm algorithm = new CIAlgorithm(0, 0, 0.01, 0.1);
		do {
			//ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;
			if (Check.checkTrace(sl, "P=?[F T0>31 & T1<4 ]")) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			System.out.println(algorithm.getP() +", "+algorithm.getN()+", "+algorithm.getX());
		} while (!algorithm.run());
		System.out.println(algorithm.getP() +", "+algorithm.getN()+", "+algorithm.getX());
	}
	
	
	public static void sprtTest() {
		SPRTAlgorithm algorithm = new SPRTAlgorithm(0.3, 0.05, 0.05, 0.01);
		do {
			//ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;
			if (Check.checkTrace(sl, "P=?[F T0>31 & T1<4 ]")) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			System.out.println(algorithm.getGama()+", "+
				algorithm.getT1()+", "+algorithm.getT2()+", n="+algorithm.getN());
		} while (!algorithm.run());
		System.out.println("result = " + algorithm.getH0() +", n = " + algorithm.getN());
	}
	
	public static void bhtTest() {
		BHTAlgorithm algorithm = new BHTAlgorithm(0.3, 10000);
		do {
			//ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;
			if (Check.checkTrace(sl, "P=?[F T0>31 & T1<4 ]")) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			System.out.println(algorithm.getGamma()+", "+algorithm.getT()+", n = "+algorithm.getN());
		} while (!algorithm.run());
		System.out.println("result = " + algorithm.getH0() +", n = " + algorithm.getN());
	}
	
	public static void bietTest() throws Exception{
		Long start = System.currentTimeMillis();
		BIETAlgorithm algorithm = new BIETAlgorithm(0, 0, 0.001, 0.99);

		do {
			/*ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;*/
					StepRestrictionMasterAlgorithm srma = new StepRestrictionMasterAlgorithm(false,false,false);
//			StepRevisionMasterAlgorithm srma = new StepRevisionMasterAlgorithm(false,false,false);
			ArrayList<State> sl = (ArrayList<State>) srma.simulateOne("E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomA.fmu,E:\\fmusdk\\fmu20\\fmu\\cs\\smartBuilding_roomB.fmu", 48,0.05,
                false, ',', null);

			if (Check.checkTrace(sl, "P=?[ F energyRoomA>12 ]")) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			if (algorithm.getN() % 100 == 0) {
				System.out.println(algorithm.getGamma()+", "+algorithm.getP()
				+", "+algorithm.getN()+","+algorithm.getX());
			}
			System.out.println(algorithm.getP()+", "+algorithm.getN()+","+algorithm.getX());
			srma = null;
			sl = null;

		} while (!algorithm.run());
		System.out.println(algorithm.getP()+", "+algorithm.getN()+","+algorithm.getX()+"total time consumption:"+(System.currentTimeMillis()-start));
	}

//0.043478260869565216, 21,0total time consumption:41742
//0.043478260869565216, 21,0total time consumption:30688
//check P=?[ F energyHeaterA>12 ] 0.05 0.9
//		0.8549618320610687, 129,111total time consumption:251972 step revision try1
//		0.8467153284671532, 135,115total time consumption:268171 step revision try2
//      0.8322147651006712, 147,123total time consumption:200873 step restriction try1
//		0.8176100628930818, 157,129total time consumption:218541 step restriction try2
//		0.8507462686567164, 132,113total time consumption:180422 step restriction try3

//		0.8958333333333334, 94,85total time consumption:1033163 with 0.001 step restriction
// check P=?[ F energyRoomA>12 ] 0.01 0.95
//0.8439897698209718, 5081,4289total time consumption:6656831 step restriction
		//0.8365566932119833, 5272,4411total time consumption:7482690 step revision
//		0.01   0.99
//		0.8364236766685973, 132940,111194


	}
