package save;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import solver.util.Coordinate;

public class SaveGameManager {

	
	public enum SAVE_SLOT {
		TEMP,
		SAVE_1,
		SAVE_2,
		SAVE_3,
		SAVE_4,
		SAVE_5;
	}
	
	private static final String ROOT = "src/data/";
	private static final String FILE_EXTENSION = ".data";
	
	private static String getFileName(String site, String id, SAVE_SLOT saveSlot) {
		return ROOT + site + "_" + id + "_" + saveSlot + FILE_EXTENSION;
	}
	
	
	public static void save(String site, String id, SAVE_SLOT slot, int score, List<Coordinate> walls) {
		save(new SaveFile(site, id, score, walls), slot);
	}
	
	public static void save(SaveFile file, SAVE_SLOT slot) {
		try {
			File f = new File(getFileName(file.getSite(), file.getId(), slot));
			Files.write(f.toPath(), file.getBytes());
			System.out.println("Saved as "+ f.getName());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static SaveFile load(String site, String id, SAVE_SLOT saveSlot) {
		try {
			File f = new File(getFileName(site, id, saveSlot));
			if(!f.exists()) {
				return null;
			}
			List<String> lines = Files.readAllLines(f.toPath());
			System.out.println("Loaded " + f.getName());
			return SaveFile.readFromFile(site, id, lines);
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
