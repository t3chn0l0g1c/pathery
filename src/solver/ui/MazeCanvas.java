package solver.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.List;

import solver.input.MazeDescriptor;
import solver.maze.SingleSolutionSolver;
import solver.util.Coordinate;
import solver.util.ImageUtil;
import solver.util.Solution;
import solver.util.Solution.Direction;
import solver.util.Solution.Wall;

public class MazeCanvas extends Canvas {

	private final MazeDescriptor m;
	private VolatileImage buffer;
	private Solution solution;
	
	public static final int SIZE = 35;
	
	public MazeCanvas(MazeDescriptor mazeDescriptor) {
		this.m = mazeDescriptor;
		setSize(m.getWidth()*SIZE, m.getHeight()*SIZE);
		setMinimumSize(new Dimension(m.getWidth()*SIZE, m.getHeight()*SIZE));
	}

	public MazeDescriptor getMazeDescriptor() {
		return m;
	}

	@Override
	public void paint(Graphics g) {
		if(buffer==null) {
			buffer = createVolatileImage(getWidth(), getHeight());
			updateGraphics();
		}
		super.paint(g);
		g.drawImage(buffer, 0, 0, null);
	}

	
	protected void updateGraphics() {
		Graphics g = buffer.getGraphics();
		for(int x = 0; x<m.getWidth(); x++) {
			for(int y = 0; y<m.getHeight(); y++) {
				BufferedImage img = ImageUtil.getImageForType(m.getTypeFor(x, y));
				g.drawImage(img, x*SIZE, y*SIZE, null);
				// DEBUG IDs
//				int id = x + y*m.getWidth()+1;
//				g.drawString(""+id, x*SIZE, y*SIZE+12);
			}
		}
		g.setColor(Color.lightGray);
		for(int x = 0; x<m.getWidth(); x++) {
			g.drawLine(x*SIZE, 0, x*SIZE, getHeight());
			for(int y = 0; y<m.getHeight(); y++) {
				g.drawLine(0, y*SIZE, getWidth(), y*SIZE);
			}
		}
		if(solution!=null) {
			List<Coordinate> fields = solution.getFields();
			g.setColor(Color.red);
			for(int i = 1; i<fields.size(); i++) {
				Coordinate c1 = fields.get(i-1);
				Coordinate c2 = fields.get(i);
				Direction d = c1.getDirection(c2);
				if(d!=Direction.NOT_CONNECTED) {
					g.drawLine(c1.x*SIZE + 17, c1.y*SIZE + 17, c2.x*SIZE+17, c2.y*SIZE+17);
				}
			}
			List<Wall> walls = solution.getWalls();
			for(Wall w : walls) {
				g.drawString(String.valueOf(w.getValue()), w.x*SIZE + 10, w.y*SIZE+20);
			}
			List<Wall> whatIf = solution.getWhatIf();
			for(Wall w : whatIf) {
				Color c = Color.green;
				if(w.getValue()<0) {
					c = Color.red;
				}
				g.setColor(c);
				g.drawString(String.valueOf(w.getValue()), w.x*SIZE + 10, w.y*SIZE+20);
			}
		}
	}

	public void updateSolution() {
		solution = requestSolution();
		updateGraphics();
		repaint();
	}
	

	protected Solution requestSolution() {
		return new SingleSolutionSolver(m.copy(), null).getSolution();
	}

	public Solution getSolution() {
		return solution;
	}
}
