package rocks.inspectit.server.instrumentation.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.StrategyConfig;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.PatternFactory;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;
import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.strategy.IStrategyConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Configuration creator is responsible for creating the {@link AgentConfig} from the
 * {@link Environment}.
 *
 * @author Ivan Senic
 */
@Component
public class ConfigurationCreator {

	/**
	 * Registration service used.
	 */
	@Autowired
	private IRegistrationService registrationService;

	/**
	 * {@link ConfigurationResolver}.
	 */
	@Autowired
	private ConfigurationResolver configurationResolver;

	/**
	 * Returns proper configuration for the agent with the correctly set IDs for the agent and
	 * sensors.
	 *
	 * @param environment
	 *            Defined {@link Environment} for the agent keep all configuration properties.
	 * @param platformId
	 *            Id of the agent to create configuration for.
	 * @return {@link AgentConfig}.
	 */
	public AgentConfig environmentToConfiguration(Environment environment, long platformId) {
		AgentConfig agentConfiguration = new AgentConfig();
		agentConfiguration.setPlatformId(platformId);

		// then all platform sensors
		if (CollectionUtils.isNotEmpty(environment.getPlatformSensorConfigs())) {
			Collection<PlatformSensorTypeConfig> platformSensorTypeConfigs = new ArrayList<>();
			for (IPlatformSensorConfig platformSensorConfig : environment.getPlatformSensorConfigs()) {
				if (platformSensorConfig.isActive()) {
					platformSensorTypeConfigs.add(getPlatformSensorTypeConfig(platformId, platformSensorConfig));
				}
			}
			agentConfiguration.setPlatformSensorTypeConfigs(platformSensorTypeConfigs);
		} else {
			agentConfiguration.setPlatformSensorTypeConfigs(Collections.<PlatformSensorTypeConfig> emptyList());
		}

		// then all method sensors
		if (CollectionUtils.isNotEmpty(environment.getMethodSensorConfigs())) {
			Collection<MethodSensorTypeConfig> methodSensorTypeConfigs = new ArrayList<>();
			for (IMethodSensorConfig methodSensorConfig : environment.getMethodSensorConfigs()) {
				methodSensorTypeConfigs.add(getMethodSensorTypeConfig(platformId, methodSensorConfig));
			}
			agentConfiguration.setMethodSensorTypeConfigs(methodSensorTypeConfigs);
		} else {
			agentConfiguration.setMethodSensorTypeConfigs(Collections.<MethodSensorTypeConfig> emptyList());
		}

		IExceptionSensorConfig exceptionSensorConfig = environment.getExceptionSensorConfig();
		if (null != exceptionSensorConfig) {
			agentConfiguration.setExceptionSensorTypeConfig(getExceptionSensorTypeConfig(platformId, exceptionSensorConfig));
		}

		// buffer strategy
		IStrategyConfig bufferStrategyConfig = environment.getBufferStrategyConfig();
		agentConfiguration.setBufferStrategyConfig(new StrategyConfig(bufferStrategyConfig.getClassName(), bufferStrategyConfig.getSettings()));

		// sending strategy
		IStrategyConfig sendingStrategyConfig = environment.getSendingStrategyConfig();
		agentConfiguration.setSendingStrategyConfig(new StrategyConfig(sendingStrategyConfig.getClassName(), sendingStrategyConfig.getSettings()));

		// exclude classes
		Collection<ExcludeRule> excludeRules = configurationResolver.getAllExcludeRules(environment);
		if (CollectionUtils.isNotEmpty(excludeRules)) {
			Collection<IMatchPattern> excludeClassesPatterns = new ArrayList<>();
			for (ExcludeRule excludeRule : excludeRules) {
				excludeClassesPatterns.add(PatternFactory.getPattern(excludeRule.getClassName()));
			}
			agentConfiguration.setExcludeClassesPatterns(excludeClassesPatterns);
		} else {
			agentConfiguration.setExcludeClassesPatterns(Collections.<IMatchPattern> emptyList());
		}

		// set configuration info
		agentConfiguration.setConfigurationInfo(configurationResolver.getConfigurationInfo(environment));

		return agentConfiguration;
	}

	/**
	 * Creates the agent based {@link PlatformSensorTypeConfig} with correctly registered ID.
	 *
	 * @param platformId
	 *            ID of the agent.
	 * @param platformSensorConfig
	 *            {@link IPlatformSensorConfig} defined in the {@link Environment}.
	 * @return {@link PlatformSensorTypeConfig}.
	 */
	private PlatformSensorTypeConfig getPlatformSensorTypeConfig(long platformId, IPlatformSensorConfig platformSensorConfig) {
		long id = registrationService.registerPlatformSensorTypeIdent(platformId, platformSensorConfig.getClassName());

		PlatformSensorTypeConfig platformSensorTypeConfig = new PlatformSensorTypeConfig();
		platformSensorTypeConfig.setId(id);
		platformSensorTypeConfig.setClassName(platformSensorConfig.getClassName());
		platformSensorTypeConfig.setParameters(platformSensorConfig.getParameters());

		return platformSensorTypeConfig;
	}

	/**
	 * Creates the agent based {@link MethodSensorTypeConfig} with correctly registered ID.
	 *
	 * @param platformId
	 *            ID of the agent.
	 * @param methodSensorConfig
	 *            {@link IMethodSensorConfig} defined in the {@link Environment}.
	 * @return {@link MethodSensorTypeConfig}.
	 */
	private MethodSensorTypeConfig getMethodSensorTypeConfig(long platformId, IMethodSensorConfig methodSensorConfig) {
		long id = registrationService.registerMethodSensorTypeIdent(platformId, methodSensorConfig.getClassName(), methodSensorConfig.getParameters());

		MethodSensorTypeConfig methodSensorTypeConfig = new MethodSensorTypeConfig();
		methodSensorTypeConfig.setId(id);
		methodSensorTypeConfig.setClassName(methodSensorConfig.getClassName());
		methodSensorTypeConfig.setParameters(methodSensorConfig.getParameters());
		methodSensorTypeConfig.setName(methodSensorConfig.getName());
		methodSensorTypeConfig.setPriority(methodSensorConfig.getPriority());

		return methodSensorTypeConfig;
	}

	/**
	 * Creates the agent based {@link ExceptionSensorTypeConfig} with correctly registered ID.
	 *
	 * @param platformId
	 *            ID of the agent.
	 * @param exceptionSensorConfig
	 *            {@link IExceptionSensorConfig} defined in the {@link Environment}.
	 * @return {@link ExceptionSensorTypeConfig}.
	 */
	private ExceptionSensorTypeConfig getExceptionSensorTypeConfig(long platformId, IExceptionSensorConfig exceptionSensorConfig) {
		long id = registrationService.registerMethodSensorTypeIdent(platformId, exceptionSensorConfig.getClassName(), exceptionSensorConfig.getParameters());

		ExceptionSensorTypeConfig exceptionSensorTypeConfig = new ExceptionSensorTypeConfig();
		exceptionSensorTypeConfig.setId(id);
		exceptionSensorTypeConfig.setClassName(exceptionSensorConfig.getClassName());
		exceptionSensorTypeConfig.setParameters(exceptionSensorConfig.getParameters());
		exceptionSensorTypeConfig.setName(exceptionSensorConfig.getName());
		exceptionSensorTypeConfig.setEnhanced(exceptionSensorConfig.isEnhanced());

		return exceptionSensorTypeConfig;
	}

}
