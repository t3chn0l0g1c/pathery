package solver.threads;

import java.util.function.Consumer;

import solver.maze.AbstractSolver;
import solver.util.ThreadStatistics;

public class MonitoringThread extends Thread {

	private final WorkerThread[] threads;
	private final Consumer<ThreadStatistics> hasEndedNotifier;
	private final Consumer<ThreadStatistics> statisticsConsumer;
	
	public MonitoringThread(WorkerThread[] threads, Consumer<ThreadStatistics> hasEndedNotifier, Consumer<ThreadStatistics> con) {
		this.threads = threads;
		this.hasEndedNotifier = hasEndedNotifier;
		this.statisticsConsumer = con;
	}
	
	private boolean isTerminated() {
		for(WorkerThread l : threads) {
			if(l.getState()!=State.TERMINATED) {
				return false;
			}
		}
		return true;
	}
	
	private void updateStatistics(ThreadStatistics statistics) {
		statistics.reset();
		for (int i = 0; i < threads.length; i++) {
			AbstractSolver l = threads[i].getLevel();
			l.updateStatistics(statistics);
			if(threads[i].getState()!=State.TERMINATED) {
				statistics.addThreadAlive();
			}
		}
		statisticsConsumer.accept(statistics);
	}
	
	@Override
	public void run() {
		for(WorkerThread w : threads) {
			w.start();
		}
		long startTime = System.currentTimeMillis();
		long time = System.currentTimeMillis();
		ThreadStatistics statistics = new ThreadStatistics(startTime, threads.length);
		statisticsConsumer.accept(statistics);
		
		while (!isTerminated()) {
			if (System.currentTimeMillis() - time > 1000) {
				time = System.currentTimeMillis();
				updateStatistics(statistics);
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("loggingThread died.");
		
		updateStatistics(statistics);
		
		hasEndedNotifier.accept(statistics);
		
	}

	public void cancel() {
		for(WorkerThread w : threads) {
			w.cancel();
		}
	}
}
