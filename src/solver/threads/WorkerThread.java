package solver.threads;

import solver.input.MazeDescriptor;
import solver.maze.AbstractSolver;
import solver.maze.HighscoreHandler;
import solver.maze.SolverFactory;
import solver.util.CanceledException;
import solver.util.SolverAction;

public class WorkerThread extends Thread{

	
	private final AbstractSolver level;
	
	
	public WorkerThread(AbstractSolver level) {
		this.level = level;
	}

	@Override
	public void run() {
		long time = System.currentTimeMillis();
		try {
			level.doStuff();
		} catch(CanceledException e) {
			// ignore
		}
		long time2 = System.currentTimeMillis() - time;
		System.out.println("Took " + time2 + "ms");
	}
	
	public AbstractSolver getLevel() {
		return level;
	}
	
	public void cancel() {
		level.cancel();
	}
	
	public static WorkerThread[] createWorkers(MazeDescriptor md, HighscoreHandler highscoreHandler, int threadCount, SolverAction a) {
		WorkerThread[] result = new WorkerThread[threadCount];
		for(int i = 0; i<result.length; i++) {
			result[i] = new WorkerThread(SolverFactory.create(md, i, highscoreHandler, a));
		}
		result[0].level.printPermutationCount();
		return result;
	}
	
}
