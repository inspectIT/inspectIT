package rocks.inspectit.server.instrumentation.config.job;

import java.util.Collection;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Job that is executed when a different environment is mapped to the agent as a result of the
 * mapping update in the CI.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class EnvironmentMappingUpdateJob extends AbstractConfigurationChangeJob {

	/**
	 * New environment connected to the agent. Should be <code>null</code> to denote that new
	 * mapping has no matching environment for the agent.
	 */
	private Environment environment;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// first remove all existing instrumentation points
		getClassCache().getInstrumentationService().removeInstrumentationPoints();

		// then update configuration holder
		ConfigurationHolder configurationHolder = getConfigurationHolder();
		configurationHolder.update(environment, getAgentId());

		// if we are initialized analyze the complete class cache
		if (configurationHolder.isInitialized()) {
			// then add instrumentation points
			Collection<IInstrumentationApplier> instrumentationAppliers = configurationHolder.getInstrumentationAppliers();
			getClassCache().getInstrumentationService().addInstrumentationPoints(getAgentConfiguration(), instrumentationAppliers);
		}
	}

	/**
	 * Sets {@link #environment}.
	 *
	 * @param environment
	 *            New value for {@link #environment}
	 */
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

}
