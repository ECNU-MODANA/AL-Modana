package plugin.test;
import static java.lang.Math.pow;

import java.math.BigDecimal;
import java.math.MathContext;
public class Test {
public static void main(String[] args) {
	try {
		System.out.println("oe test");
//		BigDecimal bd1 = new BigDecimal("3464656776868432998434".toCharArray(),2,15);
//		BigDecimal bigDecimal = new BigDecimal("1688".toCharArray(),4,3);
//		new MathContext(10).getPrecision();
//		BigDecimal bigDecimal = new  
//		bigDecimal = bigDecimal.pow(80).;
//		bd1=bd1.pow(697);
//		System.out.println(bd1.pow(699));
//		for (int i = 0; i < 697; i++) {
//		System.out.println(i+","+bigDecimal.pow(i));
//		}
		MathContext mc = new MathContext(10);
		BigDecimal bigDecimal ;
		bigDecimal = new BigDecimal(0.000144545787897, mc);
		bigDecimal = bigDecimal.pow(699);
		System.out.println(bigDecimal.round(mc));
//		System.out.println(bigDecimal);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
