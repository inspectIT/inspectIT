package info.novatec.inspectit.rcp.editor.graph.plot.datasolver;

import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.BooleanDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.ClampedPercentageDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.DefaultDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.MemoryDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.PercentageDataSolver;

/**
 * Enumeration of the implemented {@link AbstractPlotDataSolver}.
 * 
 * @author Marius Oehler
 *
 */
public enum PlotDataSolver {

	/**
	 * This value represents the default data solver.
	 */
	DEFAULT(DefaultDataSolver.class, "Default"),

	/**
	 * This value represents the data solver for percentage values.
	 */
	PERCENTAGE(PercentageDataSolver.class, "Percentage values"),

	/**
	 * This value represents the data solver for percentage values between 0 and 100 percent.
	 */
	CLAMPED_PERCENTAGE(ClampedPercentageDataSolver.class, "Percentage values (Clamped)"),

	/**
	 * This value represents the data solver for memory values.
	 */
	MEMORY(MemoryDataSolver.class, "Memory"),

	/**
	 * This value represents the data solver for boolean values.
	 */
	BOOLEAN(BooleanDataSolver.class, "Boolean values");

	/**
	 * The concrete implementation.
	 */
	private Class<? extends AbstractPlotDataSolver> solverImplementation;

	/**
	 * The title of this data solver.
	 */
	private String title;

	/**
	 * Enumeration constructor.
	 * 
	 * @param solverImplementation
	 *            the concrete implementation for this type of {@link AbstractPlotDataSolver}.
	 * @param title
	 *            the title of the implemented data solver
	 */
	private PlotDataSolver(Class<? extends AbstractPlotDataSolver> solverImplementation, String title) {
		this.solverImplementation = solverImplementation;
		this.title = title;
	}

	/**
	 * Returns the {@link #solverImplementation}.
	 * 
	 * @return the class of the implementation
	 */
	public Class<? extends AbstractPlotDataSolver> getDataSolverImplementation() {
		return solverImplementation;
	}

	/**
	 * Returns the {@link #title}.
	 * 
	 * @return the title of the data solver
	 */
	public String getTitle() {
		return title;
	}
}
