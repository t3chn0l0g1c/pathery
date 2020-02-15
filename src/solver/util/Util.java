package solver.util;

import java.util.List;
import java.util.Random;

public class Util {

	
	public static <T> T getRandom(List<T> list, Random random) {
		return list.get(random.nextInt(list.size()));
	}
	
	
	public static boolean isNullOrEmpty(String s) {
		return s==null || s.isEmpty();
	}
	
	
}
