package ecnu.modana.alsmc.smc;

import java.util.ArrayList;

import ecnu.modana.alsmc.main.ExeUppaal;
import ecnu.modana.alsmc.main.State;
import ecnu.modana.alsmc.modelCheck.Check;
import ecnu.modana.alsmc.util.ThesisCaseStudy;
import ecnu.modana.alsmc.util.UserFile;

	public class SMCTest {
		
		public static ThesisCaseStudy tcs = new ThesisCaseStudy(20);
		
	public static void main(String[] args) {
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
	
	public static void bietTest() {
		BIETAlgorithm algorithm = new BIETAlgorithm(0, 0, 0.01, 0.95);
		do {
			/*ExeUppaal.generateNewTrace();
			ArrayList<State> sl = ExeUppaal.osim.stateList;*/
			ArrayList<State> sl = tcs.randomGenOneTrace();
			if (Check.checkTrace(sl, UserFile.properties)) {
				algorithm.xPlus1();
			}
			algorithm.nPlus1();
			if (algorithm.getN() % 100 == 0) {
				System.out.println(algorithm.getGamma()+", "+algorithm.getP()
				+", "+algorithm.getN()+","+algorithm.getX());
			}
		} while (!algorithm.run());
		System.out.println(algorithm.getP()+", "+algorithm.getN()+","+algorithm.getX());
		tcs.closeAll();
	}

}
