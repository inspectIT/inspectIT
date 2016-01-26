package rocks.inspectit.server.instrumentation.config;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Configuration holder joins together all relative information needed for the instrumentation of
 * the specific class cache. The holder should be manager with the
 * {@link #update(Environment, long)} method. When this is called the holder performs needed
 * {@link #agentConfiguration} and {@link #instrumentationAppliers} updates.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ConfigurationHolder {

	/**
	 * Configuration creator.
	 */
	@Autowired
	private ConfigurationCreator configurationCreator;

	/**
	 * Configuration resolver.
	 */
	@Autowired
	private ConfigurationResolver configurationResolver;

	/**
	 * Environment for the configuration. Can be <code>null</code> if no environment is set.
	 */
	private Environment environment;

	/**
	 * Agent configuration used for the given environment. Can be <code>null</code> if no
	 * environment is set.
	 */
	private AgentConfig agentConfiguration;

	/**
	 * Cached instrumentation appliers for the current environment. Can be <code>null</code> if no
	 * environment is set.
	 */
	private Collection<IInstrumentationApplier> instrumentationAppliers;

	/**
	 * Returns if the configuration in this holder is properly initialized.
	 *
	 * @return Returns if the configuration in this holder is properly initialized.
	 */
	public boolean isInitialized() {
		return null != environment;
	}

	/**
	 * Updates the defined configuration in the holder with following tasks:<br>
	 * 1. Creates the new {@link #agentConfiguration} for given environment and platform id<br>
	 * 2. Resolves all {@link #instrumentationAppliers} for given environment<br>
	 * 3. sets the passes environment to the holder.
	 * <p>
	 * If <code>null</code> is passed then everything saved in the holder will be reset to
	 * <code>null</code> as well.
	 *
	 * @param environment
	 *            Environment to update the configuration and appliers with.
	 * @param platformId
	 *            Agent id needed for resolving configuration.
	 */
	public void update(Environment environment, long platformId) {
		if (null != environment) {
			this.environment = environment;
			this.agentConfiguration = configurationCreator.environmentToConfiguration(environment, platformId);
			this.instrumentationAppliers = configurationResolver.getInstrumentationAppliers(environment);
		} else {
			this.environment = null; // NOPMD
			this.agentConfiguration = null; // NOPMD
			this.instrumentationAppliers = null; // NOPMD
		}
	}

	/**
	 * Gets {@link #environment}.
	 *
	 * @return {@link #environment}
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Gets {@link #agentConfiguration}.
	 *
	 * @return {@link #agentConfiguration}
	 */
	public AgentConfig getAgentConfiguration() {
		return agentConfiguration;
	}

	/**
	 * Gets {@link #instrumentationAppliers}.
	 *
	 * @return {@link #instrumentationAppliers}
	 */
	public Collection<IInstrumentationApplier> getInstrumentationAppliers() {
		return instrumentationAppliers;
	}

}
