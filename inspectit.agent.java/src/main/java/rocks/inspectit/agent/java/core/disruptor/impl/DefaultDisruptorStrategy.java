package rocks.inspectit.agent.java.core.disruptor.impl;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.disruptor.IDisruptorStrategy;

/**
 * Default strategy for configuring the disruptor. Holds the buffer size of disruptor.
 *
 * @author Ivan Senic
 *
 */
public class DefaultDisruptorStrategy implements IDisruptorStrategy {

	/**
	 * Configuration storage to read properties from.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * Size of the buffer.
	 */
	private int dataBufferSize;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDataBufferSize() {
		return dataBufferSize;
	}

	/**
	 * Reads settings from the {@link #configurationStorage}. Should be called only after
	 * initialized as bean.
	 *
	 * @throws Exception
	 *             If disruptor config can not be read from the {@link #configurationStorage} or
	 *             settings don't contain the <i>bufferSize</i> property.
	 */
	@PostConstruct
	protected void postConstruct() throws Exception {
		Map<String, String> settings = configurationStorage.getDisruptorStrategyConfig().getSettings();
		if (settings.containsKey("bufferSize")) {
			this.dataBufferSize = Integer.parseInt(settings.get("bufferSize"));
		} else {
			throw new BeanInitializationException("Disruptor strategy can not be initialized without the buffer size property.");
		}
	}

}
