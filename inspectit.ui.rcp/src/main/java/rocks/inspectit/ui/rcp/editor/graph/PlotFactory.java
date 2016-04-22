package rocks.inspectit.ui.rcp.editor.graph;

import rocks.inspectit.ui.rcp.editor.graph.plot.DefaultClassesPlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.DefaultCpuPlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.DefaultMemoryPlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.DefaultThreadsPlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.HttpTimerPlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.JmxPlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.PlotController;
import rocks.inspectit.ui.rcp.editor.graph.plot.TimerPlotController;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;

/**
 * The factory for the plot creation.
 *
 * @author Patrice Bouillet
 *
 */
public final class PlotFactory {

	/**
	 * The private constructor.
	 */
	private PlotFactory() {
	}

	/**
	 * Creates and returns an instance of {@link PlotController}.
	 *
	 * @param sensorTypeEnum
	 *            The {@link SensorTypeEnum}.
	 * @return An instance of {@link PlotController}.
	 */
	public static PlotController createDefaultPlotController(SensorTypeEnum sensorTypeEnum) {
		switch (sensorTypeEnum) {
		case CHARTING_TIMER:
		case CHARTING_MULTI_TIMER:
			return new TimerPlotController();
		case CLASSLOADING_INFORMATION:
			return new DefaultClassesPlotController();
		case COMPILATION_INFORMATION:
			return null;
		case MEMORY_INFORMATION:
			return new DefaultMemoryPlotController();
		case CPU_INFORMATION:
			return new DefaultCpuPlotController();
		case RUNTIME_INFORMATION:
			return null;
		case SYSTEM_INFORMATION:
			return null;
		case THREAD_INFORMATION:
			return new DefaultThreadsPlotController();
		case CHARTING_HTTP_TIMER_SENSOR:
			return new HttpTimerPlotController();
		case CHARTING_JMX_SENSOR_DATA:
			return new JmxPlotController();
		default:
			return null;
		}
	}

	/**
	 * Returns an instance of {@link PlotController}.
	 *
	 * @param fqn
	 *            The fully-qualified-name.
	 * @return An instance of {@link PlotController}.
	 */
	public static PlotController createDefaultPlotController(String fqn) {
		return createDefaultPlotController(SensorTypeEnum.get(fqn));
	}

}
