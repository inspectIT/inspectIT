package info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl;

import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.RangeType;

/**
 * Default implementation of the {@link AbstractPlotDataSolver}. This implementation will be used by
 * default.
 * 
 * @author Marius Oehler
 *
 */
public class DefaultDataSolver extends AbstractPlotDataSolver {

	/**
	 * Package-private Constructor.
	 */
	DefaultDataSolver() {
	}

	@Override
	public NumberAxis getAxis() {
		NumberAxis rangeAxis = new NumberAxis("");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRange(true);
		return rangeAxis;
	}

	@Override
	protected double valueConverterImpl(String value) throws Exception {
		return Double.parseDouble(value);
	}

	@Override
	protected String valueToHumanReadableImpl(String value) throws Exception {
		return value;
	}
}