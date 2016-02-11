package info.novatec.inspectit.agent.buffer.impl;

import info.novatec.inspectit.agent.buffer.AbstractBufferStrategy;
import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * This implementation will hold all list of measurements for the given size. It works as a FILO
 * stack.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SizeBufferStrategy extends AbstractBufferStrategy<MethodSensorData> implements IBufferStrategy<MethodSensorData> {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The default count if none is specified.
	 */
	private static final int DEFAULT_COUNT = 60;

	/**
	 * The linked list containing the FILO stack.
	 */
	private LinkedList<List<MethodSensorData>> stack; // NOPMD

	/**
	 * The stack size.
	 */
	private int size;

	/**
	 * Delegates to the second constructor with the default count.
	 */
	public SizeBufferStrategy() {
		this(DEFAULT_COUNT);
	}

	/**
	 * The second constructor where one can specify the actual count or stack size.
	 * 
	 * @param size
	 *            The stack size.
	 */
	public SizeBufferStrategy(int size) {
		this.size = size;
		stack = new LinkedList<List<MethodSensorData>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMeasurements(List<MethodSensorData> measurements) {
		if (null == measurements) {
			throw new IllegalArgumentException("Measurements cannot be null!");
		}

		synchronized (this) {
			// as we can only add one element at the time, we only have to delete
			// the oldest element.
			if (stack.size() >= size) {
				// if the measurements stack size is reached, this buffer strategy will simply drop
				// the old ones, because we can not let the data pile up if the sending of the data
				// is not fast enough
				stack.removeFirst();
				log.info("Possible data loss due to the excessive data creation on the Agent!");
			}

			stack.addLast(measurements);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MethodSensorData> next() {
		synchronized (this) {
			return stack.removeLast();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, String> settings) {
		if (settings.containsKey("size")) {
			this.size = Integer.parseInt((String) settings.get("size"));
		}
	}

}
