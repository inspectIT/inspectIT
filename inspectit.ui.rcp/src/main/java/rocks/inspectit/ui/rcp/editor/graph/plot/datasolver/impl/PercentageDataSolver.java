package rocks.inspectit.ui.rcp.editor.graph.plot.datasolver.impl;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.Range;

import rocks.inspectit.ui.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;

/**
 * This class is used to plot data as percentage data.
 *
 * @author Marius Oehler
 *
 */
public class PercentageDataSolver extends AbstractPlotDataSolver {

	/**
	 * The number formatter used to label the axis.
	 */
	private static NumberFormat numberFormat = new NumberFormat() {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = 558623875022886643L;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			return toAppendTo.append((int) number).append("%");
		}
	};

	/**
	 * Package-private Constructor.
	 */
	PercentageDataSolver() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberAxis getAxis() {
		NumberAxis rangeAxis = new NumberAxis("Percentage");
		rangeAxis.setRange(new Range(0, 100), false, false);
		rangeAxis.setAutoRangeMinimumSize(50, false);
		rangeAxis.setTickUnit(new NumberTickUnit(10.0d, new DecimalFormat("0")));
		rangeAxis.setNumberFormatOverride(numberFormat);
		return rangeAxis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double valueConvert(double value) {
		return value * 100D;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String valueToHumanReadableImpl(double value) {
		return NumberFormatter.formatDoubleToPercent(value / 100D);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAggregatable() {
		return true;
	}
}