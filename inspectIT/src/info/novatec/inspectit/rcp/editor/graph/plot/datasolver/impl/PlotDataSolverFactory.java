package info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl;

import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.PlotDataSolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to access the implementation of the {@link AbstractPlotDataSolver}.
 * 
 * @author Marius Oehler
 *
 */
public final class PlotDataSolverFactory {

	/**
	 * Holds the singleton instance of each {@link AbstractPlotDataSolver} implementation.
	 */
	private Map<PlotDataSolver, AbstractPlotDataSolver> dataSolverMap;

	/**
	 * Hidden default constructor.
	 */
	private PlotDataSolverFactory() {
		dataSolverMap = new HashMap<>();
	}

	/**
	 * Returns a {@link AbstractPlotDataSolver} implementation of the given {@link EPlotDataSolver}.
	 * There is only one instance of each dataSolver which is stored in the {@link #dataSolverMap}.
	 * If there is no instance of the desired implementation, a new one is created and stored in the
	 * map.
	 * 
	 * @param dataSolver
	 *            desired {@link AbstractPlotDataSolver}
	 * @return {@link AbstractPlotDataSolver} implementation
	 */
	private AbstractPlotDataSolver getDataSolverInstance(PlotDataSolver dataSolver) {
		if (!dataSolverMap.containsKey(dataSolver)) {
			try {
				AbstractPlotDataSolver plotDataSolver = dataSolver.getDataSolverImplementation().newInstance();
				dataSolverMap.put(dataSolver, plotDataSolver);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return dataSolverMap.get(dataSolver);
	}

	/**
	 * Singleton instance.
	 */
	private static PlotDataSolverFactory singleton;

	/**
	 * Returns the singleton. If there is no instance of this class, it will be created.
	 * 
	 * @return singleton of this class.
	 */
	private static PlotDataSolverFactory instance() {
		if (null == singleton) {
			createSingleton();
		}
		return singleton;
	}

	/**
	 * Creates the {@link PlotDataSolverFactory} singleton.
	 */
	private static synchronized void createSingleton() {
		if (null == singleton) {
			singleton = new PlotDataSolverFactory();
		}
	}

	/**
	 * Returns an implementation of {@link AbstractPlotDataSolver} which is represented by the given
	 * {@link EPlotDataSolver} enum.
	 * 
	 * @param dataSolver
	 *            desired {@link AbstractPlotDataSolver}
	 * @return {@link AbstractPlotDataSolver} implementation
	 */
	public static AbstractPlotDataSolver getDataSolver(PlotDataSolver dataSolver) {
		return instance().getDataSolverInstance(dataSolver);
	}

	/**
	 * Returns the default implementation of the {@link AbstractPlotDataSolver} interface. Equals to
	 * a call of the {@link #getDataSolver(EPlotDataSolver)} method with parameter
	 * {@link EPlotDataSolver#DEFAULT}.
	 * 
	 * @return default {@link AbstractPlotDataSolver} implementation
	 */
	public static AbstractPlotDataSolver getDefaultDataSolver() {
		return getDataSolver(PlotDataSolver.DEFAULT);
	}
}
