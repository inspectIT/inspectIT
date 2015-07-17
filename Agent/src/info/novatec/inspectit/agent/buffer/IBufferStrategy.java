package info.novatec.inspectit.agent.buffer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A BufferStrategy is used to define the behavior of the value objects once a connection problem
 * appears.
 * 
 * @param <E>
 *            The element contained in the list.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IBufferStrategy<E> extends Iterator<List<E>> {

	/**
	 * Adds a list of measurements.
	 * 
	 * @param measurements
	 *            The measurements to add.
	 */
	void addMeasurements(List<E> measurements);

	/**
	 * Initializes the buffer strategy with the given {@link Map}.
	 * 
	 * @param settings
	 *            The settings as a {@link Map}.
	 */
	void init(Map<String, String> settings);

}
