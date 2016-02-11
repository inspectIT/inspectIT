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
	 * Represents the byte count of one megabyte.
	 */
	private static final double MEGABYTE_BYTE_COUNT = 1048576D;

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
	public double valueConvert(double value) {
		return value / MEGABYTE_BYTE_COUNT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String valueToHumanReadableImpl(double value) {
		return NumberFormatter.humanReadableByteCount((long) (value * MEGABYTE_BYTE_COUNT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAggregatable() {
		return true;
	}
}
