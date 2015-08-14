package info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl;

import org.jfree.chart.axis.NumberAxis;

/**
 * This class is used to plot data as percentage data between 0 and 100 percent.
 * 
 * @author Marius Oehler
 *
 */
public class ClampedPercentageDataSolver extends PercentageDataSolver {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double valueConvert(double value) {
		return Math.max(0D, Math.min(1D, value)) * 100D;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public NumberAxis getAxis() {
		NumberAxis axis = super.getAxis();
		axis.setAutoRange(false);
		return axis;
	}
}
