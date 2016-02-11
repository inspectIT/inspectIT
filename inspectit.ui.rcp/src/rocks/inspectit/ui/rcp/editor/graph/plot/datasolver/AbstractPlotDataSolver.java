package info.novatec.inspectit.rcp.editor.graph.plot.datasolver;

import info.novatec.inspectit.rcp.InspectIT;

import org.eclipse.core.runtime.IStatus;
import org.jfree.chart.axis.NumberAxis;

/**
 * The implementation of this interface define how specific data will be plotted by providing a
 * specific Y-axis and the data transformation.
 * 
 * @author Marius Oehler
 *
 */
public abstract class AbstractPlotDataSolver {

	/**
	 * Specifies the Y-axis which is used in the plot.
	 * 
	 * @return Y-axis
	 */
	public abstract NumberAxis getAxis();

	/**
	 * Specifies a data transformation. The given {@link Double} value will be transformed into
	 * another value that is used in the plot.
	 * 
	 * @param value
	 *            original value
	 * @return transformed value
	 */
	public double valueConvert(double value) {
		return value;
	}

	/**
	 * Converts the value into a formatted human readable string. This implementation is wrapped by
	 * the {@link #valueToHumanReadable(String)} method.
	 * 
	 * @param value
	 *            value to convert
	 * @return formatted string or <code>NULL</code> if the given value is invalid for this data
	 *         solver
	 */
	protected abstract String valueToHumanReadableImpl(double value);

	/**
	 * Converts the value into a formatted human readable string. The input value has to be a valid
	 * value which is based on a value returned by the {@link #valueConvert(String)} method.
	 * 
	 * @param value
	 *            value to convert
	 * @return formatted string
	 */
	public String valueToHumanReadable(double value) {
		String formattedString = valueToHumanReadableImpl(value);
		if (formattedString == null) {
			InspectIT.getDefault().log(IStatus.INFO, "value " + value + " could not be formatted as a human readable string");
			return "incompatible data <" + value + ">";
		} else {
			return formattedString;
		}
	}

	/**
	 * Returns <code>true</code> if the values of this data solver can be aggregated in a reasonable
	 * way.
	 * 
	 * @return <code>true</code> if the values can be aggregated
	 */
	public abstract boolean isAggregatable();
}
