package solver.maze;

import java.util.ArrayList;
import java.util.List;

import solver.input.MazeDescriptor;
import solver.util.Coordinate;
import solver.util.FieldCollection.FieldArrayList;
import solver.util.Solution;
import solver.util.Solution.Wall;
import solver.util.Type;

public class SingleSolutionSolver extends AbstractSolver {

	public SingleSolutionSolver(MazeDescriptor md, HighscoreHandler sh) {
		super(md, sh);
	}
	
	public final Solution getSolution() {
//		List<Field> list = new ArrayList<Field>();
		FieldArrayList fal = new FieldArrayList();
		final int length = retrievePath(false, fal);
		List<Field> list = fal.getList();
		if(list.isEmpty() || list.get(list.size()-1).getType()!=Type.END) {
			return null;
		}
		List<Coordinate> coords = new ArrayList<Coordinate>(list.size());
		for(Field f : list) {
			for(int i = 0; i<field.length; i++) {
				for(int j = 0; j<field[i].length; j++) {
					if(field[i][j]==f) {
						coords.add(new Coordinate(j, i));
						break;
					}
				}
			}
		}
		List<Wall> walls = collectWallValues(length);
		List<Wall> whatIf = new ArrayList<Wall>();
		for(int i = 0; i<field.length; i++) {
			for(int j = 0; j<field[i].length; j++) {
				if(field[i][j].getType() == Type.NORMAL) {
					field[i][j].setType(Type.BLOCKED_BY_USER, maze);
					field[i][j].precedessor = Field.pred_constant;
					int whatIfLength = test(null, null);
					field[i][j].setType(Type.NORMAL, maze);
					field[i][j].precedessor = 0;
					if(whatIfLength>-1 && whatIfLength!=length) {
						whatIf.add(new Wall(j, i, whatIfLength-length));
					}
				}
			}
		}
		return new Solution(coords, length, walls, whatIf);
	}

	private final List<Wall> collectWallValues(int length) {
		List<Wall> result = new ArrayList<Wall>();
		for(int i = 0; i<field.length; i++) {
			for(int j = 0; j<field[i].length; j++) {
				if(field[i][j].getType()==Type.BLOCKED_BY_USER) {
					field[i][j].setType(Type.NORMAL, maze);
					field[i][j].precedessor = 0;
					int nl = test(null, null);
					result.add(new Wall(j, i, length-nl));
					field[i][j].setType(Type.BLOCKED_BY_USER, maze);
					field[i][j].precedessor = Field.pred_constant;
				}
			}
		}
		return result;
	}
	
	@Override
	public void doStuff() {
		// nothin
	}

	
}
