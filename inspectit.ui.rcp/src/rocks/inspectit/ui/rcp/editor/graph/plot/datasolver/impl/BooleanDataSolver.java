package info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl;

import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.Range;

/**
 * This class is used to plot boolean data.
 * 
 * @author Marius Oehler
 *
 */
public class BooleanDataSolver extends AbstractPlotDataSolver {

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
			if (number == 0D) {
				toAppendTo.append("False");
			} else if (number == 1D) {
				toAppendTo.append("True");
			}
			return toAppendTo;
		}
	};

	/**
	 * Package-private Constructor.
	 */
	BooleanDataSolver() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberAxis getAxis() {
		NumberAxis rangeAxis = new NumberAxis("Boolean");
		rangeAxis.setRange(new Range(-0.5, 1.5), true, false);
		rangeAxis.setTickUnit(new NumberTickUnit(1d, new DecimalFormat("0")));
		rangeAxis.setNumberFormatOverride(numberFormat);
		return rangeAxis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String valueToHumanReadableImpl(double value) {
		if (value == 0) {
			return "False";
		} else if (value == 1) {
			return "True";
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAggregatable() {
		return false;
	}
}
