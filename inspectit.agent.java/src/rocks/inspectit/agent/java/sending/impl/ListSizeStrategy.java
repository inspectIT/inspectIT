package info.novatec.inspectit.agent.sending.impl;

import info.novatec.inspectit.agent.core.ListListener;
import info.novatec.inspectit.agent.sending.AbstractSendingStrategy;
import info.novatec.inspectit.communication.DefaultData;

import java.util.List;
import java.util.Map;

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
	public void startStrategy() {
		getCoreService().addListListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		getCoreService().removeListListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contentChanged(List<List<DefaultData>> list) {
		if (list.size() > size) {
			sendNow();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, String> settings) {
		this.size = Long.parseLong((String) settings.get("size"));
	}

}
