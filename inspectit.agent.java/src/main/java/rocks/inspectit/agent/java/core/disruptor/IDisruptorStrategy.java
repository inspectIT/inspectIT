package rocks.inspectit.agent.java.core.disruptor;

/**
 * Disruptor strategy holds all the options that are needed for intializing of the disruptor in the
 * core service. Currently this class provides:
 *
 * <ul>
 * <li>the size of the disruptor buffer
 * </ul>
 *
 * @author Ivan Senic
 *
 */
public interface IDisruptorStrategy {

	/**
	 * Returns the size of the buffer for storing monitoring data before sending.
	 *
	 * @return Returns the size of the buffer for storing monitoring data before sending.
	 */
	int getDataBufferSize();
}
