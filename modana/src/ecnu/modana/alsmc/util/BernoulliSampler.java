package ecnu.modana.alsmc.util;

import java.util.Random;

public class BernoulliSampler {
	
	Random random = null;
	int max = 100000;
	int threshold = 50000;
	
	/**
	 * constructor
	 * @param prob probability to simulate
	 * @param precision sampling precision of prob value
	 */
	public BernoulliSampler(double prob, double precision) {
		random = new Random();
		max = (int)(1 / precision);
		threshold = (int)(prob*max);
	}
	
	/**
	 * one Bernouli trial
	 * @return success or not
	 */
	public boolean nextTria() {
		if (random.nextInt(max) < threshold) {
			return true;
		} else {
			return false;
		}
	}

//	public static void main(String[] args) {
//		BernoulliSampler bs = new BernoulliSampler(0.3, 0.01);
//		for (int i = 0; i < 100; i++) {
//			System.out.println(bs.max + ", " +bs.threshold + ", " + bs.nextTria());
//		}
//	}
}
