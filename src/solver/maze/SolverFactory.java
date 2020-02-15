package solver.maze;

import solver.input.MazeDescriptor;
import solver.util.SolverAction;

public class SolverFactory {
	
	public static AbstractSolver create(MazeDescriptor md, int threadNr, HighscoreHandler scoreHandler, SolverAction a) {
		switch(a) {
		case SOLVE:
		case SOLVE_ENDLESS: return createDefaultSolver(md, threadNr, scoreHandler, a);
		case RECURSE_DOUBLE: return createDoubleRecuse(md, threadNr, scoreHandler, a);
		case RECURSE_TRIPPLE: return createTrippleRecurse(md, threadNr, scoreHandler, a);
		default: throw new RuntimeException("Unknown action:_ " + a);
		}
	}

	private static TrippleRecurseSolver createTrippleRecurse(MazeDescriptor md, int threadNr, HighscoreHandler scoreHandler, SolverAction a) {
		SingleSolutionSolver sss = new SingleSolutionSolver(md.copy(), scoreHandler);
		return new TrippleRecurseSolver(md.copy(), threadNr, scoreHandler, false, sss.getSolution().getLength());
	}

	private static DoubleRecurseSolver createDoubleRecuse(MazeDescriptor md, int threadNr, HighscoreHandler scoreHandler, SolverAction a) {
		SingleSolutionSolver sss = new SingleSolutionSolver(md.copy(), scoreHandler);
		return new DoubleRecurseSolver(md.copy(), threadNr, scoreHandler, false, sss.getSolution().getLength());
	}

	private static DefaultSolver createDefaultSolver(MazeDescriptor md, int threadNr, HighscoreHandler scoreHandler, SolverAction a) {
		boolean endless = a == SolverAction.SOLVE_ENDLESS;
		return new DefaultSolver(md, threadNr, scoreHandler, endless);
	}

}
