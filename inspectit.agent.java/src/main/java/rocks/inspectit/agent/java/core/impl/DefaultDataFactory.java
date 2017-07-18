package rocks.inspectit.agent.java.core.impl;

import com.lmax.disruptor.EventFactory;

/**
 * {@link EventFactory} that generates {@link DefaultDataWrapper}s.
 *
 * @author Matthias Huber
 *
 */
public class DefaultDataFactory implements EventFactory<DefaultDataWrapper> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultDataWrapper newInstance() {
		return new DefaultDataWrapper();
	}

}
