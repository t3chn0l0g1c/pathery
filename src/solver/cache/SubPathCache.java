package solver.cache;

import java.util.ArrayList;
import java.util.List;

public class SubPathCache {

	// TODO
	/**
	 * Gives static ordered access from 1* field to subPath[]
	 * array with IDs of [1st start (according to maze.get(type), all WPs (each)]
	 */
//	.
	
	// TODO order ascending for binary search?
	private final int[] startingAt;
	private final SubPath[][] results;
	
	private SubPathCache(int[] startingAt, SubPath[][] results) {
		this.startingAt = startingAt;
		this.results = results;
	}

	public SubPath[] get(int fieldId) {
		for(int i = 0; i<startingAt.length; i++) {
			if(fieldId==startingAt[i]) {
				return results[i];
			}
		}
		return null;
	}
	
	public static class SubPathCacheBuilder {
		
		private static class SubPathPair {
			final int id;
			final SubPath[] result;
			public SubPathPair(int id, SubPath[] result) {
				this.id = id;
				this.result = result;
			}
			
		}

		private final List<SubPathPair> list = new ArrayList<>(32);
		
		public void put(int id, SubPath[] result) {
			list.add(new SubPathPair(id, result));
		}
		
		public SubPathCache toCache() {
			int[] s = new int[list.size()];
			SubPath[][] r = new SubPath[list.size()][];
			for(int i = 0; i<list.size(); i++) {
				SubPathPair p = list.get(i);
				s[i] = p.id;
				r[i] = p.result;
			}
			return new SubPathCache(s, r);
		}
	}
}
