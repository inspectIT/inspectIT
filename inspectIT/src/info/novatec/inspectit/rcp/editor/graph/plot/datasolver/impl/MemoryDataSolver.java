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
	protected double valueConverterImpl(String value) throws Exception {
		return Double.parseDouble(value) / 1048576;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String valueToHumanReadableImpl(String value) throws Exception {
		return NumberFormatter.humanReadableByteCount((long) Double.parseDouble(value));
	}

}
