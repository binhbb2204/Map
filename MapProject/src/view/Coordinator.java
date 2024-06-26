package view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import javax.swing.Timer;

import component.PanelError;
import glasspanepopup.GlassPanePopup;
import matrix.AStarCostEvaluator;
import matrix.MatrixNode;
import model.DFSMazeAlgorithm;
import model.Model_Error;
import model.XAStarPathAlgorithm;
import model.XMatrix;

public class Coordinator {
	private component.PanelError Error = new PanelError(); 
    Map<Object, PropertyChangeSupport> supports = new HashMap<Object, PropertyChangeSupport>();


    public void addPropertyChangeListener(Object source, PropertyChangeListener listener) {
		PropertyChangeSupport support = supports.get(source);
		if (support == null) {
			support = new PropertyChangeSupport(source);
			supports.put(source, support);
		}
		support.addPropertyChangeListener(listener);
	}

    public void removePropertyChangeListener(Object source, PropertyChangeListener listener) {
		PropertyChangeSupport support = supports.get(source);
		if (support != null) {
			support.removePropertyChangeListener(listener);
		}
	}

    protected void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
		PropertyChangeSupport support = supports.get(source);
		if (support != null) {
			support.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

    static class Pack {

		Canvas canvas;
		XAStarPathAlgorithm algorithm;
		AStarCostEvaluator evaluator;
		DFSMazeAlgorithm dfsMazeAlgorithm;
		Timer mazeTimer;

	}

	Collection<Pack> packs = new ArrayList<Pack>();

	ControlPanel controlPanel;

	PropertyChangeListener startRequestedListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			for (Pack pack : packs) {
				if (pack.algorithm.isSearching()) {
					return;
				}
				if (pack.canvas.getMatrix().getEnd() == null) {
					//JOptionPane.showMessageDialog(AWTUtil.findFrame(controlPanel), "Destination not set.");
					GlassPanePopup.showPopup(Error);
					Error.setData(new Model_Error("Destination is not set yet."));
					return;
					
				}
			}
			final MatrixNode start = ((Canvas)evt.getSource()).getMatrix().getStart();
			for (Pack pack : packs) {
				final Canvas canvas = pack.canvas;
				final XAStarPathAlgorithm algorithm = pack.algorithm;
				final AStarCostEvaluator evaluator = pack.evaluator;
				final XMatrix matrix = canvas.getMatrix();
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						firePropertyChange(canvas, AppConstant.SearchStarted, null, null);
						canvas.setEditable(false);
						evaluator.setEnabled(true);
						matrix.setStart(matrix.getValue(start.getRow(), start.getCol()));
						matrix.reset();
						matrix.evaluateHeuristic();
						
						algorithm.searchPath(matrix.getStart(), matrix.getEnd());
		
						long duration = algorithm.getRuntime();
						canvas.repaint();
						canvas.setEditable(true);
						firePropertyChange(canvas, AppConstant.SearchCompleted, null, null);
						firePropertyChange(canvas, AppConstant.SearchTime, null, duration);
					}
				};
				Thread thread = new Thread(runnable);
				thread.start();
			}
		}
	};

	PropertyChangeListener matrixEditedListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Canvas src = (Canvas)evt.getSource();
			for (Pack pack : packs) {
				if (pack.canvas == src) {
					continue;
				}
				if (copyMatrix(src.getMatrix(), pack.canvas.getMatrix())) {
					pack.canvas.repaint();
				}
			}
		}
	};

	boolean copyMatrix(XMatrix src, XMatrix dest) {
		int srcRows = src.getRow();
		int srcCols = src.getColumn();
		int destRows = dest.getRow();
		int destCols = dest.getColumn();
		if (srcRows != destRows || srcCols != destCols) {
			System.err.println("matrix size not match");
			return false;
		}
		for (int i = 0; i < srcRows; i++) {
			for (int j = 0; j < srcCols; j++) {
				MatrixNode srcNode = src.getValue(i, j);
				MatrixNode destNode = dest.getValue(i, j);
				destNode.setEnabled(srcNode.isEnabled());
			}
		}
		MatrixNode srcStart = src.getStart();
		if (srcStart != null) {
			dest.setStart(dest.getValue(srcStart.getRow(), srcStart.getCol()));
		}
		else {
			dest.setStart(null);
		}
		MatrixNode srcEnd = src.getEnd();
		if (srcEnd != null) {
			dest.setEnd(dest.getValue(srcEnd.getRow(), srcEnd.getCol()));
		}
		else {
			dest.setEnd(null);
		}
		dest.buildGraph();
		return true;
	}

	//to create random disabled blocks when generating a map
	void generateMatrix(XMatrix matrix, int obstaclePercent) {
		int rows = matrix.getRow();
		int cols = matrix.getColumn();
		matrix.setStart(null);
		matrix.setEnd(null);
		Random random = new Random();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix.getValue(i, j).setEnabled(random.nextInt(100) >= obstaclePercent);
			}
		}
	}
	
	private void stopMazeTimers() {
        for (Pack pack : packs) {
            if (pack.mazeTimer != null) {
                pack.mazeTimer.stop();
                pack.mazeTimer = null;
            }
			pack.canvas.getMatrix().reset();
            pack.canvas.repaint();
			pack.canvas.validate();
        }
    }

    private void generateMazeStepByStep(XMatrix matrix, Pack pack) {
		stopMazeTimers();
		matrix.reset();
		int interval = controlPanel.getParameters().getAnimationMs();
		Timer timer = new Timer(interval, null); 
		pack.mazeTimer = timer;
		matrix.setStart(null);
		matrix.setEnd(null);
		timer.addActionListener(e -> {
			if (!pack.dfsMazeAlgorithm.generateMazeStep()) {
				timer.stop();
				GlassPanePopup.showPopup(Error);
				Error.setData(new Model_Error("Maze generation is complete."));
			}
			for (Pack p : packs) {
				if (p != pack) {
					copyMatrix(matrix, p.canvas.getMatrix());
					p.canvas.repaint();
				}
			}
			pack.canvas.repaint();
		});
		timer.start();	
	}    
	PropertyChangeListener controlPanelListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (AppConstant.CellSizeChanged.equals(evt.getPropertyName())) {
				for (Pack pack : packs) {
					pack.canvas.updateMatrixIfDimensionChanged();
				}
			}
			else if (AppConstant.ClearMapRequested.equals(evt.getPropertyName()) ) {
				stopMazeTimers();
				for (Pack pack : packs) {
					stopMazeTimers();
					generateMatrix(pack.canvas.getMatrix(), 0);
					pack.canvas.repaint();
				}
			}
			else if (AppConstant.GenerateMapRequested.equals(evt.getPropertyName())) {
				stopMazeTimers();
				XMatrix src = null;
				AppConstant.Painter currentPainter = controlPanel.getParameters().getPainter();
				for (Pack pack : packs) {
					
					if (src == null) {
						src = pack.canvas.getMatrix();
						if (currentPainter == AppConstant.Painter.DESTINATION) {
							// Add logic for DESTINATION mode here
							generateMatrix(src, controlPanel.getParameters().getObstaclePercent());
							// Logic for setting destination
						} else if (currentPainter == AppConstant.Painter.DFS_MAZE) {
							// Add logic for DFS_MAZE mode here
							pack.dfsMazeAlgorithm.initializeMaze(src);
                            generateMazeStepByStep(src, pack);
						}
						pack.canvas.repaint();
					}
					else if (copyMatrix(src, pack.canvas.getMatrix())) {
						pack.canvas.repaint();
					}
				}
			}
			else if (AppConstant.StopRequested.equals(evt.getPropertyName())) {
				stopMazeTimers();
				for (Pack pack : packs) {
					stopMazeTimers();
					pack.evaluator.setEnabled(false);
				}
			}
			else if (AppConstant.ModeChanged.equals(evt.getPropertyName())) {
				switch (controlPanel.getParameters().getMode()) {
				case MAP_EDITING_MODE:
					for (Pack pack : packs) {
						stopMazeTimers();
						pack.canvas.getMatrix().reset();
						pack.canvas.repaint();
					}
					break;
				default:
					break;
				}
			}
		}
	};

	public void add(Canvas canvas, XAStarPathAlgorithm algorithm, AStarCostEvaluator evaluator) {
		Pack pack = new Pack();
		pack.canvas = canvas;
		pack.algorithm = algorithm;
		pack.evaluator = evaluator;
		pack.dfsMazeAlgorithm = new DFSMazeAlgorithm();
		canvas.addPropertyChangeListener(AppConstant.StartRequested, startRequestedListener);
		canvas.addPropertyChangeListener(AppConstant.MapEdited, matrixEditedListener);
		packs.add(pack);
	}

	public void setControlPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
		this.controlPanel.addPropertyChangeListener(controlPanelListener);
	}
	
}
