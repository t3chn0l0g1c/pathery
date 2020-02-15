package solver.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import solver.maze.Field;

public class CacheMap<T> {

	public static final int CACHE_MAP_SIZE = 1000010;
	public static final int SUBPATH_MAP_SIZE = 25;
	
	private final Map<ArrayWrapper, T> map = new HashMap<>(CACHE_MAP_SIZE, 1f);
	
	// TODO consider char/byte array for better performance (intrinsics on equals)
	// subCacheMap first
//	.
	public static class ArrayWrapper {
		final char[] array;
		final int hash;
		public ArrayWrapper(char[] array) {
			this.array = array;
			hash = Arrays.hashCode(array);
		}
		public ArrayWrapper(Field[] a) {
			this.array = new char[a.length];
			for(int i = 0; i<a.length; i++) {
				array[i] = (char) a[i].id;
			}
			hash = Arrays.hashCode(array);
		}
		@Override
		public int hashCode() {
			return hash;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ArrayWrapper) {
				ArrayWrapper o = (ArrayWrapper) obj;
				return hash == o.hash && Arrays.equals(array, o.array); 
			}
			return false;
		}
	}
	
	private final int mapSize;
	
	private final AtomicInteger size = new AtomicInteger();
//	private final AtomicInteger chunk = new AtomicInteger();
	
	private final ArrayWrapper[] buffer;
//	= new ArrayWrapper[MAP_SIZE];
	private final T defaultValue;
	
	private CacheMap(int size, T defaultValue) {
		this.mapSize = size;
		buffer = new ArrayWrapper[size];
		this.defaultValue = defaultValue;
	}
	
	public void put(ArrayWrapper key, T value) {
		int current = size.incrementAndGet();
		int idx = current%mapSize;
		if(buffer[idx]!=null) {
			map.remove(buffer[idx]);
		}
		buffer[idx] = key;
		map.put(key, value);
	}
	
	public void put(Field[] fa, T value) {
		put(new ArrayWrapper(fa), value);
	}
	
	public T get(ArrayWrapper key) {
		return map.getOrDefault(key, defaultValue);
	}
	
	public T get(Field[] fa) {
		return get(new ArrayWrapper(fa));
	}
	
	public static CacheMap<Integer> createCacheMap() {
		return new CacheMap<>(CACHE_MAP_SIZE, 0);
	}
	
	public static CacheMap<SubPath[]> createSubPathMap() {
		return new CacheMap<>(SUBPATH_MAP_SIZE, null);
	}
	
	// TODO use ArrayWrapper for int[] hash/equals
	// hold ArrayWrapper in redundant arrays (chunk size) and delete first chunk on size constraint
	
	// get/put/reset methods
	
	// TODO benchmark.
	// 2.2mio with thread based map (46% cached)
	// 
	
	
	
}
