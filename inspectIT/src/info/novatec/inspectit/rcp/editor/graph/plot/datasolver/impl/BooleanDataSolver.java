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
	 * Package-private Constructor.
	 */
	BooleanDataSolver() {
	}

	/**
	 * The number formatter used to label the axis.
	 */
	private static NumberFormat numberFormat = new NumberFormat() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 558623875022886643L;

		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			return null;
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			return null;
		}

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

	@Override
	public NumberAxis getAxis() {
		NumberAxis rangeAxis = new NumberAxis("Boolean");
		rangeAxis.setRange(new Range(-0.5, 1.5), true, false);
		rangeAxis.setTickUnit(new NumberTickUnit(1d, new DecimalFormat("0")));
		rangeAxis.setNumberFormatOverride(numberFormat);
		return rangeAxis;
	}

	@Override
	protected double valueConverterImpl(String value) throws Exception {
		return Boolean.parseBoolean(value) ? 1 : 0;
	}

	@Override
	protected String valueToHumanReadableImpl(String value) throws Exception {
		if ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase())) {
			return String.valueOf(Boolean.parseBoolean(value));
		} else {
			throw new Exception();
		}
	}

}
