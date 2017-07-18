package rocks.inspectit.agent.java.core.disruptor;

/**
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
