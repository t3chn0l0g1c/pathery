package solver.maze;

import java.util.ArrayList;
import java.util.List;

import save.SaveGameManager;
import save.SaveGameManager.SAVE_SLOT;
import solver.util.Coordinate;
import solver.util.DRCoords;
import solver.util.Type;

public class HighscoreHandler {

	private final String id;
	private int highScore = 0;
	private List<Coordinate> current_best_solution = null;

	private final boolean writeBest;
	private final SAVE_SLOT saveSlot;
	private final int threadCount;

	private List<List<Coordinate>> chunks = new ArrayList<>();
	
	public HighscoreHandler(String id, boolean writeBest, SAVE_SLOT saveSlot, int threadCount) {
		this.writeBest = writeBest;
		this.id = id;
		this.saveSlot = saveSlot;
		this.threadCount = threadCount;
	}

	public int getHighScore() {
		return highScore;
	}

	public final synchronized List<Coordinate> getCurrentBest() {
		if (current_best_solution == null) {
			return null;
		}
		return new ArrayList<Coordinate>(current_best_solution);
	}

	public synchronized DRCoords getDoubleRecurseChunk() {
		if(chunks.isEmpty()) {
			return null;
		}
		return new DRCoords(current_best_solution, chunks.remove(0));
	}

	public synchronized boolean checkLongestPathSyncronized(final int path, final AbstractSolver l, final long startTime) {
		if (path > highScore) {
			highScore = path;
			current_best_solution = l.getMaze().createCoords();
			chunkify();
			if (writeBest) {
				SaveGameManager.save(l.getMd().getSite(), id, saveSlot, path, current_best_solution);
			}
			System.out.println("\nNEW MAX: " + path + " (found after " + (System.currentTimeMillis() - startTime) + "ms)");

			l.print();
			System.out.println("Blocks used: " + l.getMaze().getFieldsForType(Type.BLOCKED_BY_USER).length);
			return true;
		}
		return false;
	}
	
	private void chunkify() {
		int chunkSize = Math.max(current_best_solution.size()/threadCount, 1);
		List<List<Coordinate>> chunks = new ArrayList<>();
		int i = 0;
		List<Coordinate> temp = new ArrayList<Coordinate>();
		for(Coordinate c : current_best_solution) {
			temp.add(c);
			i++;
			if(i==chunkSize) {
				chunks.add(temp);
				temp = new ArrayList<Coordinate>();
				i = 0;
			}
		}
		if(!temp.isEmpty()) {
			chunks.add(temp);
		}
		this.chunks = chunks;
	}

}
