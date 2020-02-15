package solver.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import save.SaveFile;
import save.SaveGameManager;
import save.SaveGameManager.SAVE_SLOT;
import solver.input.MazeDescriptor;
import solver.maze.HighscoreHandler;
import solver.threads.MonitoringThread;
import solver.threads.WorkerThread;
import solver.util.Coordinate;
import solver.util.SaveSlot;
import solver.util.Solution;
import solver.util.SolverAction;
import solver.util.ThreadStatistics;
import solver.util.Util;

public class MazePanel extends JPanel {
	
	private class SolveListener implements ActionListener {
		boolean active = false;
		final JButton solve;
		final JComboBox<SolverAction> actions;
		
		public SolveListener(JButton solve, JComboBox<SolverAction> actions) {
			this.solve = solve;
			this.actions = actions;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(active==false) {
				start((SolverAction)actions.getSelectedItem());
				solve.setText("Cancel");
			} else {
				cancel();
				solve.setText("Solve");
			}
			active = !active;
		}
		
		void reset() {
			solve.setText("Solve");
			active = false;
		}
	}

	private MazeCanvas canvas;
	private final MazeDescriptor md;
	private Solution currentSolution = null;
	private JLabel score;
	private JLabel solverScore;
	private JComboBox<SaveSlot> saveSlot;
	private SolveListener solveListener;

	private JFormattedTextField walls;
	private JFormattedTextField threadCount;

	private final SaveSlot[] saveSlots = new SaveSlot[SAVE_SLOT.values().length];

	private static MonitoringThread currentMonitoringThread = null;

	public MazePanel(final MazeDescriptor md) {
		this.md = md;

		createCanvas();

		JPanel sidePanel = new JPanel(new GridLayout(0, 1));

		JPanel saveLoadPanel = createSaveLoadPanel();
		sidePanel.add(saveLoadPanel);

		JPanel solvePanel = createSolvePanel();
		sidePanel.add(solvePanel);

		JPanel statisticsPanel = createStatisticsPanel();
		sidePanel.add(statisticsPanel);

		add(sidePanel);
	}

	private JLabel elapsedTime;
	private JLabel timeTilCurrentBest;
	private JLabel aliveThreads;
	private JLabel totalTests;
	private JLabel testsPerSec;
	private JLabel rawTestsPerSecPerThread;
	private JLabel saved;
	private JLabel savedPercent;
	private JLabel cached;
	private JLabel cachedPercent;
	private JLabel avgReUse;

	private void updateStatistics(ThreadStatistics s) {
		updateNumberLabel(elapsedTime, s.getElapsedTimeInSeconds(), "s");
		updateNumberLabel(timeTilCurrentBest, s.getCurrentBestDuration(), "ms");
		updateNumberLabel(aliveThreads, s.getThreadsAlive(), "");
		updateNumberLabel(totalTests, s.getTests(), "");
		updateNumberLabel(testsPerSec, s.getTotalTestsPerSecond(), "");
		updateNumberLabel(rawTestsPerSecPerThread,
				s.getActualTestsPerThreadPerSecond(), "");
		updateNumberLabel(saved, s.getSaved(), "");
		updateNumberLabel(savedPercent, s.getSavedPercent(), "");
		updateNumberLabel(cached, s.getCached(), "");
		updateNumberLabel(cachedPercent, s.getCachedPercent(), "");
		updateNumberLabel(avgReUse, s.getAverageReUseCount(), "");
		solverScore.setText("" + s.getBestSolution());

		saveSlots[SAVE_SLOT.TEMP.ordinal()].setScore(s.getBestSolution());
		saveSlot.repaint();

		setColorOfScoreLabel();
		// always updates TEMP saveSlot!
	}

	private void updateNumberLabel(JLabel label, long number, String suffix) {
		String value = String.valueOf(number);
		for (int i = value.length() - 3; i > 0; i -= 3) {
			value = value.substring(0, i) + "." + value.substring(i);
		}
		label.setText(value + suffix);
	}

	private JPanel createStatisticsPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2));

		elapsedTime = createAndAddStatisticsLabel("Elapsed Time:", panel);
		timeTilCurrentBest = createAndAddStatisticsLabel("Best found after:",
				panel);
		aliveThreads = createAndAddStatisticsLabel("Alive Threads:", panel);
		totalTests = createAndAddStatisticsLabel("Total Tests:", panel);
		testsPerSec = createAndAddStatisticsLabel("Tests/s:", panel);
		rawTestsPerSecPerThread = createAndAddStatisticsLabel(
				"Raw Tests/Thread/s:", panel);
		saved = createAndAddStatisticsLabel("Saved:", panel);
		savedPercent = createAndAddStatisticsLabel("Saved %:", panel);
		cached = createAndAddStatisticsLabel("Cached:", panel);
		cachedPercent = createAndAddStatisticsLabel("Cached %:", panel);
		avgReUse = createAndAddStatisticsLabel("Avg ReUse:", panel);

		return panel;
	}

	private static JLabel createAndAddStatisticsLabel(String labelText,
			JPanel panel) {
		JLabel label = new JLabel(labelText);
		panel.add(label);

		JLabel result = new JLabel("0");
		result.setSize(75, 20);
		panel.add(result);
		return result;
	}

	private void setColorOfScoreLabel() {
		// light red: 255 100 50
		// light green: 100 255 100
		Color c = new Color(238, 238, 238);
		try {
			int s = Integer.valueOf(score.getText());
			int ss = Integer.valueOf(solverScore.getText());
			if (ss > s) {
				c = new Color(100, 255, 100);
			} else if (s > ss) {
				c = new Color(255, 100, 50);
			}
		} catch (Exception e) {
			// meh
		}
		solverScore.setBackground(c);
	}

	private JPanel createSaveLoadPanel() {

		// TODO gridLayout!
		JPanel saveLoadPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.RELATIVE;

		JLabel s = new JLabel("Score: ");
		saveLoadPanel.add(s, gbc);

		score = new JLabel("0");
		score.setSize(75, 20);
		score.setPreferredSize(score.getSize());

		gbc.gridx = 1;
		saveLoadPanel.add(score, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		JLabel ss = new JLabel("SolverScore: ");
		saveLoadPanel.add(ss, gbc);

		gbc.gridx = 1;
		solverScore = new JLabel("0");
		solverScore.setOpaque(true);
		solverScore.setSize(75, 20);
		solverScore.setPreferredSize(solverScore.getSize());

		saveLoadPanel.add(solverScore, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		for (SAVE_SLOT slot : SAVE_SLOT.values()) {
			SaveSlot currentSlot = new SaveSlot(slot);
			SaveFile sf = SaveGameManager.load(md.getSite(), md.getId(), slot);
			if (sf != null) {
				currentSlot.setScore(sf.getScore());
			}
			saveSlots[slot.ordinal()] = currentSlot;
		}

		saveSlot = new JComboBox<>(saveSlots);
		// saveSlot.addActionListener((e)-> load());
		saveSlot.setSize(200, 20);

		saveLoadPanel.add(saveSlot, gbc);

		gbc.gridy = 3;
		gbc.gridwidth = GridBagConstraints.RELATIVE;

		JButton save = new JButton("Save");
		save.addActionListener((e) -> save());

		saveLoadPanel.add(save, gbc);

		gbc.gridx = 1;

		JButton load = new JButton("Load");
		load.addActionListener((e) -> load());
		saveLoadPanel.add(load, gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;

		JButton screenShot = new JButton("ScreenShot");
		screenShot.addActionListener((e) -> screenshot());
		saveLoadPanel.add(screenShot, gbc);

		return saveLoadPanel;
	}

	private void setWallsOnMD(String wallString) {
		try {
			if (!Util.isNullOrEmpty(wallString)) {
				md.setWallCount(Integer.valueOf(wallString));
			}
		} catch (Exception e) {
			System.out.println("No valid number: " + wallString);
		}
	}

	private void checkWallCountColor() {
		String t = walls.getText();
		Color c = Color.WHITE;
		if (Util.isNullOrEmpty(t)) {
			c = new Color(255, 100, 100);
		}
		try {
			int i = Integer.valueOf(t);
			if (i <= 0 || i > md.getHeight() * md.getWidth()) {
				c = new Color(255, 100, 100);
			}
		} catch (Exception e) {
			//
		}
		walls.setBackground(c);
	}

	private JPanel createSolvePanel() {
		JPanel solvePanel = new JPanel(new GridLayout(0, 2));

		solvePanel.setPreferredSize(new Dimension(255, 110));
		solvePanel.setMaximumSize(solvePanel.getPreferredSize());
		solvePanel.setMinimumSize(solvePanel.getPreferredSize());

		JComboBox<SolverAction> actions = new JComboBox<SolverAction>(SolverAction.values());
		
		solvePanel.add(actions);
		
		JButton solve = new JButton("Solve");
		solveListener = new SolveListener(solve, actions);
		solve.addActionListener(solveListener);
		solvePanel.add(solve);

		JLabel wallLabel = new JLabel("Walls");
		solvePanel.add(wallLabel);

		NumberFormatter nfWalls = new NumberFormatter(
				NumberFormat.getIntegerInstance());
		nfWalls.setValueClass(Integer.class);
		nfWalls.setAllowsInvalid(false); // this is the key!!
		nfWalls.setMinimum(0);
		// nfWalls.setMaximum(md.getWidth()*md.getHeight());

		walls = new JFormattedTextField(nfWalls);
		walls.setText(String.valueOf(md.getWalls()));
		checkWallCountColor();
		walls.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		walls.setSize(100, 20);
		walls.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				setWallsOnMD(walls.getText());
				checkWallCountColor();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setWallsOnMD(walls.getText());
				checkWallCountColor();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setWallsOnMD(walls.getText());
				checkWallCountColor();
			}
		});
		walls.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				checkWallCountColor();
				if (e.getButton() == MouseEvent.BUTTON3) {
					md.resetModifiedWallCount();
					walls.setText(String.valueOf(md.getWalls()));
				}
			};
		});
		solvePanel.add(walls);

		JLabel threadsLabel = new JLabel("Threads");
		solvePanel.add(threadsLabel);

		NumberFormatter nfThreads = new NumberFormatter(
				NumberFormat.getIntegerInstance());
		nfThreads.setValueClass(Integer.class);
		nfThreads.setAllowsInvalid(false); // this is the key!!
		nfThreads.setMinimum(0);
		nfThreads.setMaximum(10000);

		threadCount = new JFormattedTextField(nfThreads);
		threadCount.setText(String.valueOf(Runtime.getRuntime()
				.availableProcessors()-1));
		threadCount.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		threadCount.setSize(100, 20);

		solvePanel.add(threadCount);

		JButton reset = new JButton("Reset");
		reset.addActionListener((e) -> reset());
		solvePanel.add(reset);

		return solvePanel;
	}

	private void reset() {
		md.reset();
		updateSolution();
		System.out.println("Reset.");
		setColorOfScoreLabel();
	}

	synchronized void start(SolverAction a) {
		System.out.println("Starting " + a);
		int threadCount = threads();
		WorkerThread[] threads = WorkerThread.createWorkers(md,
				new HighscoreHandler(md.getId(), true, SAVE_SLOT.TEMP, threadCount),
				threadCount, a);
		MonitoringThread mt = new MonitoringThread(threads, this::hasEnded,
				this::updateStatistics);
		registerMonitoringThread(mt, this);
		mt.start();
	}

	public static synchronized void registerMonitoringThread(MonitoringThread mt, MazePanel mp) {
		if (currentMonitoringThread != null) {
			mp.cancel();
		}
		currentMonitoringThread = mt;
	}

	public synchronized void resetMonitoringThread() {
		currentMonitoringThread = null;
		solveListener.reset();
	}

	void cancel() {
		if (currentMonitoringThread != null) {
			currentMonitoringThread.cancel();
			resetMonitoringThread();
//			CacheHolder.reset();
		}
	}

	public synchronized void hasEnded(ThreadStatistics finalStatistics) {

		updateStatistics(finalStatistics);

		cancel();
	}

	private int threads() {
		return Integer.valueOf(threadCount.getText().replace(".", ""));
	}

	private void createCanvas() {
		canvas = new MazeCanvas(md);

		add(canvas);
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int x = e.getX() / MazeCanvas.SIZE;
				int y = e.getY() / MazeCanvas.SIZE;
				if (e.getButton() == MouseEvent.BUTTON3) {
					md.changeType(x, y);
					canvas.updateSolution();
				} else {
					if (md.clickedOnTile(x, y)) {
						updateSolution();
					}
				}
			}
		});

	}

	void updateSolution() {
		canvas.updateSolution();
		String scoreString = "0";
		if (canvas.getSolution() != null) {
			scoreString = String.valueOf(canvas.getSolution().getLength());
		}
		score.setText(scoreString);

		setColorOfScoreLabel();
	}

	// private void updateSaveSlotText(SAVE_SLOT slot) {
	// SaveFile save = SaveGameManager.load(md.getSite(), md.getId(), slot);
	// if(save!=null) {
	// saveSlots[slot.ordinal()].setScore(save.getScore());
	// }
	// saveSlot.repaint();
	// }
	//

	private void load() {
		SaveSlot slot = (SaveSlot) saveSlot.getSelectedItem();
		SaveFile save = SaveGameManager.load(md.getSite(), md.getId(),
				slot.getSlot());
		if (save == null) {
			return;
		}
		md.reset();
		for (Coordinate c : save.getWalls()) {
			md.clickedOnTile(c.x, c.y);
		}
		walls.setText(String.valueOf(save.getWalls().size()));
		updateSolution();

		setColorOfScoreLabel();
	}

	private void save() {
		SaveSlot slot = (SaveSlot) saveSlot.getSelectedItem();
		Solution s = canvas.getSolution();
		List<Coordinate> coords = new ArrayList<Coordinate>();
		coords.addAll(s.getWalls());
		SaveFile sf = new SaveFile(md.getSite(), md.getId(), s.getLength(),
				coords);
		SaveGameManager.save(sf, slot.getSlot());
		saveSlots[slot.getSlot().ordinal()].setScore(sf.getScore());
		saveSlot.repaint();
	}

	void screenshot() {
		canvas.updateSolution();
		currentSolution = canvas.getSolution();
		int length = currentSolution.getLength();
		BufferedImage buf = new BufferedImage(canvas.getWidth(),
				canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		canvas.paint(buf.getGraphics());
		try {
			String fileName = MazePanel.this.md.getSite() + "_"
					+ MazePanel.this.md.getId() + "_" + length + ".png";
			File f = new File("src\\screenshots\\" + fileName);
			ImageIO.write(buf, "png", f);
			System.out.println("screenshot saved as " + fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
