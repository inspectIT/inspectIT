package rocks.inspectit.agent.java.sending.impl;

import java.util.List;
import java.util.Map;

import rocks.inspectit.agent.java.core.ListListener;
import rocks.inspectit.agent.java.sending.AbstractSendingStrategy;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * A simple implementation which checks the size of the list of the current value objects. If the
 * size of the list is greater than the defined one, {@link #sendNow()} is called.
 *
 * @author Patrice Bouillet
 *
 */
public class ListSizeStrategy extends AbstractSendingStrategy implements ListListener<List<DefaultData>> {

	/**
	 * Default size.
	 */
	private static final long DEFAULT_SIZE = 10L;

	/**
	 * The size.
	 */
	private long size = DEFAULT_SIZE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startStrategy() {
		getCoreService().addListListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		getCoreService().removeListListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contentChanged(int elementCount) {
		if (elementCount > size) {
			sendNow();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Map<String, String> settings) {
		this.size = Long.parseLong(settings.get("size"));
	}

}
