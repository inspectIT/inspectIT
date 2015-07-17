package info.novatec.inspectit.agent.spring;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.ConfigurationStorage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * This class enables that {@link SpringConfiguration} processes the {@link ConfigurationStorage}
 * after it has been successfully initialized.
 * 
 * @author Ivan Senic
 * 
 */
@Component("strategyAndSensorConfiguration")
@DependsOn({ "configurationReader" })
public class StrategyAndSensorConfiguration implements InitializingBean {

	/**
	 * {@link IConfigurationStorage} holding the needed configuration details.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * {@link SpringConfiguration} to process the {@link IConfigurationStorage}.
	 */
	@Autowired
	private SpringConfiguration springConfiguration;

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		springConfiguration.registerComponents(configurationStorage);
	}

}
