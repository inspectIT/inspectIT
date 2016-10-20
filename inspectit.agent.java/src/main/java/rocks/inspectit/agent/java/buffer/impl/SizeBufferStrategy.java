package rocks.inspectit.agent.java.buffer.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import rocks.inspectit.agent.java.buffer.AbstractBufferStrategy;
import rocks.inspectit.agent.java.buffer.IBufferStrategy;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.spring.logger.Log;

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
	@Override
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
	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MethodSensorData> next() {
		synchronized (this) {
			return stack.removeLast();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Map<String, String> settings) {
		if (settings.containsKey("size")) {
			this.size = Integer.parseInt(settings.get("size"));
		}
	}

}
