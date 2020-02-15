package solver.util;

public class ThreadStatistics {

	
	private final long startTime;
	private final int threadCount;
	private long tests;
	private int threadsAlive;
	private long saved;
	private long cached;
	private long reUsedBlocks;
	private long reUseCount;
	private int bestSolution;
	private long currentBestFoundAt = -1;
	
	public ThreadStatistics(long startTime, int threadCount) {
		this.startTime = startTime;
		this.threadCount = threadCount;
	}
	
	public long getStartTime() {
		return startTime;
	}
	public long getTests() {
		return tests;
	}
	public void addTests(long tests) {
		this.tests += tests;
	}
	public int getThreadsAlive() {
		return threadsAlive;
	}
	public void addThreadAlive() {
		this.threadsAlive++;
	}
	public long getSaved() {
		return saved;
	}
	public void addSaved(long saved) {
		this.saved += saved;
	}
	public long getCached() {
		return cached;
	}
	public void addCached(long cached) {
		this.cached += cached;
	}
	
	public long getElapsedTimeInSeconds() {
		return (System.currentTimeMillis()-startTime)/1000L;
	}
	
	public long getSavedPercent() {
		if(tests==0) {
			return 0;
		}
		return saved * 100L /tests;
	}
	
	public long getCachedPercent() {
		if(tests==0) {
			return 0;
		}
		return cached * 100L /tests;
	}
	
	public long getActualTestsPerThreadPerSecond() {
		long t = tests-cached;
		long l = getElapsedTimeInSeconds();
		if(l==0) {
			return 0;
		}
		return (t / l) / threadCount;
	}
	
	public int getThreadCount() {
		return threadCount;
	}
	
	public long getTotalTestsPerSecond() {
		long l = getElapsedTimeInSeconds();
		if(l==0) {
			return 0;
		}
		return tests / l;
	}
	public long getReUsedBlocks() {
		return reUsedBlocks;
	}
	public void addReUsedBlocks(long reUsedBlocks) {
		this.reUsedBlocks += reUsedBlocks;
	}
	public long getReUseCount() {
		return reUseCount;
	}
	public void addReUseCount(long reUseCount) {
		this.reUseCount += reUseCount;
	}
	
	public long getAverageReUseCount() {
		if(reUseCount==0) {
			return 0;
		}
		return reUsedBlocks / reUseCount;
	}

	public int getBestSolution() {
		return bestSolution;
	}
	
	public long getCurrentBestDuration() {
		if(currentBestFoundAt<0) {
			return 0;
		}
		return currentBestFoundAt - startTime;
	}
	
	public void setBestSolution(int newBest, long foundAtTimeStamp) {
		if(newBest>bestSolution) {
			bestSolution = newBest;
			currentBestFoundAt = foundAtTimeStamp;
		}
	}
	
	public void reset() {
		tests = 0;
		threadsAlive = 0;
		saved = 0;
		cached = 0;
		reUsedBlocks = 0;
		reUseCount = 0;
		bestSolution = 0;
		currentBestFoundAt = 0;
	}
	
}
