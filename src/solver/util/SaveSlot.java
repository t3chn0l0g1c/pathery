package solver.util;

import save.SaveGameManager.SAVE_SLOT;

public class SaveSlot {

	private final SAVE_SLOT slot;
	
	private int score;

	public SaveSlot(SAVE_SLOT slot) {
		this.slot = slot;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		return slot + " (" + score + ")";
	}

	public SAVE_SLOT getSlot() {
		return slot;
	}
	
}
