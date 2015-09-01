package info.novatec.inspectit.rcp.editor.graph.plot.datasolver;

import org.jfree.chart.axis.NumberAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of this interface define how specific data will be plotted by providing a
 * specific Y-axis and the data transformation.
 * 
 * @author Marius Oehler
 *
 */
public abstract class AbstractPlotDataSolver {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPlotDataSolver.class);

	/**
	 * Specifies the Y-axis which is used in the plot.
	 * 
	 * @return Y-axis
	 */
	public abstract NumberAxis getAxis();

	/**
	 * Specifies a data transformation. The given {@link String} value will be converted into a
	 * {@link Double} value that is used in the plot. This method is wrapped by the
	 * {@link #valueConverter(String)} method.
	 * 
	 * @param value
	 *            original value
	 * @return transformed value
	 * @throws Exception
	 *             exception will be thrown if the conversion has failed
	 */
	protected abstract double valueConverterImpl(String value) throws Exception;

	/**
	 * Specifies a data transformation. The given {@link String} value will be converted into a
	 * {@link Double} value that is used in the plot. info(
	 * 
	 * @param value
	 *            original value
	 * @return transformed value
	 */
	public double valueConverter(String value) {
		try {
			return valueConverterImpl(value);
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("value {} could not be converted", value);
			}
			return 0;
		}
	}

	/**
	 * Converts the value into a formatted human readable string. This implementation is wrapped by
	 * the {@link #valueToHumanReadable(String)} method.
	 * 
	 * @param value
	 *            value to convert
	 * @return formatted string
	 * @throws Exception
	 *             exception will be thrown if the conversion has failed
	 */
	protected abstract String valueToHumanReadableImpl(String value) throws Exception;

	/**
	 * Converts the value into a formatted human readable string.
	 * 
	 * @param value
	 *            value to convert
	 * @return formatted string
	 */
	public String valueToHumanReadable(String value) {
		try {
			return valueToHumanReadableImpl(value);
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("value {} could not be formatted as a human readable string", value);
			}
			return "incompatible data <" + value + ">";
		}
	}

}
