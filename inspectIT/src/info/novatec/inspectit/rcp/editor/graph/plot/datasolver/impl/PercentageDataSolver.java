package info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl;

import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;

import java.text.DecimalFormat;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.Range;

/**
 * This class is used to plot data as percentage data.
 * 
 * @author Marius Oehler
 *
 */
public class PercentageDataSolver extends AbstractPlotDataSolver {

	/**
	 * Package-private Constructor.
	 */
	PercentageDataSolver() {
	}

	@Override
	public NumberAxis getAxis() {
		NumberAxis rangeAxis = new NumberAxis("Percentage");
		rangeAxis.setRange(new Range(0, 100), true, false);
		rangeAxis.setAutoRangeMinimumSize(50, false);
		rangeAxis.setTickUnit(new NumberTickUnit(10.0d, new DecimalFormat("0")));
		return rangeAxis;
	}

	@Override
	protected double valueConverterImpl(String value) throws Exception {
		return Double.parseDouble(value) * 100D;
	}

	@Override
	protected String valueToHumanReadableImpl(String value) throws Exception {
		return NumberFormatter.formatDoubleToPercent(Double.parseDouble(value));
	}
}
