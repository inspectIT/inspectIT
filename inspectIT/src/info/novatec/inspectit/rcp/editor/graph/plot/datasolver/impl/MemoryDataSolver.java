package info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl;

import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.RangeType;

/**
 * This class is used to plot data as memory data.
 * 
 * @author Marius Oehler
 *
 */
public class MemoryDataSolver extends AbstractPlotDataSolver {

	/**
	 * Package-private Constructor.
	 */
	MemoryDataSolver() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberAxis getAxis() {
		NumberAxis rangeAxis = new NumberAxis("Memory (MB)");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setLowerBound(0);
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRange(true);
		return rangeAxis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double valueConvertImpl(String value) throws Exception {
		return Double.parseDouble(value) / 1048576D;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String valueToHumanReadableImpl(double value) {
		return NumberFormatter.humanReadableByteCount((long) (value * 1048576D));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAggregatable() {
		return true;
	}
}
