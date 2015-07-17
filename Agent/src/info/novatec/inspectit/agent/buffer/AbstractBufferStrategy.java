package info.novatec.inspectit.agent.buffer;

import info.novatec.inspectit.agent.config.IConfigurationStorage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for all {@link IBufferStrategy} for correct initialization with Spring.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public abstract class AbstractBufferStrategy<E> implements InitializingBean, IBufferStrategy<E> {

	/**
	 * Configuration storage to read settings from.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		this.init(configurationStorage.getBufferStrategyConfig().getSettings());
	}
}
