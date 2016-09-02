package rocks.inspectit.server.instrumentation.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.server.instrumentation.config.applier.ExceptionSensorInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.JmxMonitoringApplier;
import rocks.inspectit.server.instrumentation.config.applier.MethodSensorInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.SpecialInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.TimerMethodSensorInstrumentationApplier;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.PatternFactory;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.ISensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.eum.EndUserMonitoringConfig;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;
import rocks.inspectit.shared.cs.ci.profile.data.AbstractProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.ExcludeRulesProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.JmxDefinitionProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Component that can resolve the different CI configuration points.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ConfigurationResolver {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ConfigurationInterfaceManager}.
	 */
	@Autowired
	private ConfigurationInterfaceManager configurationInterfaceManager;

	/**
	 * {@link IRegistrationService} needed for the instrumentation appliers.
	 */
	@Autowired
	private IRegistrationService registrationService;

	/**
	 * {@link SpecialMethodSensorAssignmentFactory}.
	 */
	@Autowired
	private SpecialMethodSensorAssignmentFactory specialAssignmentFactory;

	/**
	 * Returns all instrumentation appliers for one environment.
	 *
	 * @param environment
	 *            {@link Environment} to get appliers for.
	 * @return Returns all {@link IInstrumentationApplier}s contained in all profiles for the given
	 *         environment and all functional applier defined by environment.
	 */
	public Collection<IInstrumentationApplier> getInstrumentationAppliers(Environment environment) {
		if (null == environment) {
			return Collections.emptyList();
		}

		Collection<IInstrumentationApplier> appliers = new ArrayList<>();
		for (String profileId : environment.getProfileIds()) {
			try {
				Profile profile = configurationInterfaceManager.getProfile(profileId);
				// don't include inactive profiles
				if (!profile.isActive()) {
					continue;
				}

				// all assignments
				AbstractProfileData<?> profileData = profile.getProfileData();
				if (profileData.isOfType(SensorAssignmentProfileData.class)) {
					List<? extends AbstractClassSensorAssignment<?>> assignments = profileData.getData(SensorAssignmentProfileData.class);
					if (CollectionUtils.isNotEmpty(assignments)) {
						for (AbstractClassSensorAssignment<?> assignment : assignments) {
							appliers.add(getInstrumentationApplier(assignment, environment));
						}
					}
				}
			} catch (BusinessException e) {
				// on exception just exclude the profile
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + profileId + " ignored during collecting method sensor assignments due to the exception.", e);
				}
				continue;
			}
		}

		// collect functionals as well
		for (SpecialMethodSensorAssignment functionalAssignment : specialAssignmentFactory.getSpecialAssignments(environment)) {
			appliers.add(getInstrumentationApplier(functionalAssignment, environment));
		}

		return appliers;
	}

	/**
	 * Returns the {@link IInstrumentationApplier} for the given sensor assignment and the
	 * {@link Environment} it's being used in.
	 *
	 * @param sensorAssignment
	 *            {@link ISensorAssignment}
	 * @param environment
	 *            Environment being used.
	 * @return {@link IInstrumentationApplier}
	 * @throws IllegalArgumentException
	 *             If supplied assignment is not of known type.
	 */
	public IInstrumentationApplier getInstrumentationApplier(ISensorAssignment<?> sensorAssignment, Environment environment) throws IllegalArgumentException {
		// switch based on the class
		Class<?> sensorAssigmentClass = sensorAssignment.getClass();
		if (TimerMethodSensorAssignment.class.isAssignableFrom(sensorAssigmentClass)) {
			return new TimerMethodSensorInstrumentationApplier((TimerMethodSensorAssignment) sensorAssignment, environment, registrationService);
		} else if (ExceptionSensorAssignment.class.isAssignableFrom(sensorAssigmentClass)) {
			return new ExceptionSensorInstrumentationApplier((ExceptionSensorAssignment) sensorAssignment, environment, registrationService);
		} else if (SpecialMethodSensorAssignment.class.isAssignableFrom(sensorAssigmentClass)) {
			return new SpecialInstrumentationApplier((SpecialMethodSensorAssignment) sensorAssignment, environment, registrationService);
		} else if (MethodSensorAssignment.class.isAssignableFrom(sensorAssigmentClass)) {
			return new MethodSensorInstrumentationApplier((MethodSensorAssignment) sensorAssignment, environment, registrationService);
		}
		throw new IllegalArgumentException("Instrumentation applier can be created. Assignment " + sensorAssignment + " is of unknow type.");
	}

	/**
	 * Returns all JMX monitoring appliers for one environment.
	 *
	 * @param environment
	 *            {@link Environment} to get appliers for.
	 * @return Returns all {@link JmxMonitoringApplier}s contained in all profiles for the given
	 *         environment.
	 */
	public Collection<JmxMonitoringApplier> getJmxMonitoringAppliers(Environment environment) {
		if (null == environment) {
			return Collections.emptyList();
		}

		Collection<JmxMonitoringApplier> appliers = new ArrayList<>();
		for (String profileId : environment.getProfileIds()) {
			try {
				Profile profile = configurationInterfaceManager.getProfile(profileId);
				// don't include inactive profiles
				if (!profile.isActive()) {
					continue;
				}

				// all assignments
				AbstractProfileData<?> profileData = profile.getProfileData();
				if (profileData.isOfType(JmxDefinitionProfileData.class)) {
					List<JmxBeanSensorAssignment> assignments = profileData.getData(JmxDefinitionProfileData.class);
					if (CollectionUtils.isNotEmpty(assignments)) {
						for (JmxBeanSensorAssignment assignment : assignments) {
							appliers.add(new JmxMonitoringApplier(assignment, environment, registrationService));
						}
					}
				}
			} catch (BusinessException e) {
				// on exception just exclude the profile
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + profileId + " ignored during collecting JMX sensor assignments due to the exception.", e);
				}
				continue;
			}
		}

		return appliers;
	}

	/**
	 * Returns all {@link ExcludeRule}s contained in all profiles for the given environment.
	 *
	 * @param environment
	 *            {@link Environment} to get rules for.
	 * @return Returns all {@link ExcludeRule}s contained in all profiles for the given environment.
	 */
	public Collection<ExcludeRule> getAllExcludeRules(Environment environment) {
		if (null == environment) {
			return Collections.emptyList();
		}

		Collection<ExcludeRule> rules = new ArrayList<>();
		for (String profileId : environment.getProfileIds()) {
			try {
				Profile profile = configurationInterfaceManager.getProfile(profileId);
				// don't include inactive profiles
				if (!profile.isActive()) {
					continue;
				}

				AbstractProfileData<?> profileData = profile.getProfileData();
				if (profileData.isOfType(ExcludeRulesProfileData.class)) {
					List<ExcludeRule> data = profileData.getData(ExcludeRulesProfileData.class);
					if (CollectionUtils.isNotEmpty(data)) {
						rules.addAll(data);
					}
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + profileId + " ignored during profile difference calculation due to the exception.", e);
				}
				continue;
			}
		}
		return rules;
	}

	/**
	 * Returns the configuration info based on the given {@link Environment}.
	 *
	 * @param environment
	 *            {@link Environment}.
	 * @return Configuration info
	 */
	public String getConfigurationInfo(Environment environment) {
		if (null == environment) {
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Assigned environment: " + environment.getName() + "\n"); // NOPMD

		// all active profiles
		stringBuilder.append("Active profiles:\n");
		for (String profileId : environment.getProfileIds()) {
			try {
				Profile profile = configurationInterfaceManager.getProfile(profileId);
				// don't include inactive profiles
				if (!profile.isActive()) {
					continue;
				}

				stringBuilder.append("|-" + profile.getName() + "\n");
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + profileId + " ignored during configuration info creation due to the exception.", e);
				}
				continue;
			}
		}

		// some options
		stringBuilder.append("Options:\n"); // NOPMD
		stringBuilder.append("|-data buffer size: " + environment.getDataBufferSize() + "\n"); // NOPMD
		stringBuilder.append("|-class loading delegation: " + environment.isClassLoadingDelegation() + "\n"); // NOPMD
		stringBuilder.append("|-enhanced exception sensor: " + environment.getExceptionSensorConfig().isEnhanced() + "\n"); // NOPMD
		stringBuilder.append("|-retransformation strategy: " + environment.getRetransformationStrategy().toString() + "\n"); // NOPMD

		EndUserMonitoringConfig eumConfig = environment.getEumConfig();
		stringBuilder.append("|-end user monitoring: " + eumConfig.isEumEnabled()); // NOPMD
		if (eumConfig.isEumEnabled()) {
			stringBuilder.append("\n||-EUM beacon URL: " + eumConfig.getScriptBaseUrl() + JSAgentModule.BEACON_SUB_PATH); // NOPMD
			stringBuilder.append(
					"\n||-EUM JS agent URL: " + eumConfig.getScriptBaseUrl() + JSAgentModule.JAVASCRIPT_URL_PREFIX + JSAgentModule.JS_AGENT_REVISION + "_" + eumConfig.getActiveModules() + ".js"); // NOPMD
		}

		return stringBuilder.toString();
	}

	/**
	 * Tries to locate one {@link Environment} for the given agent name and IPs. If only one
	 * {@link Environment} fits the agent by current mappings this one will be returned. Otherwise
	 * an exception will be raised.
	 *
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent. Can be <code>null</code>.
	 * @return {@link Environment}.
	 * @throws BusinessException
	 *             Throws {@link Exception} if there is no matching environment for the agent or if
	 *             there is more than one valid environment for the agent.
	 */
	public Environment getEnvironmentForAgent(List<String> definedIPs, String agentName) throws BusinessException {
		AgentMappings agentMappings = configurationInterfaceManager.getAgentMappings();

		if (CollectionUtils.isEmpty(agentMappings.getMappings())) {
			throw new BusinessException("Determine an environment to use for the agent with name '" + agentName + "' and IP adress(es): " + definedIPs,
					ConfigurationInterfaceErrorCodeEnum.NO_MAPPING_DEFINED);
		}

		List<AgentMapping> mappings = new ArrayList<>(agentMappings.getMappings());

		for (Iterator<AgentMapping> it = mappings.iterator(); it.hasNext();) {
			AgentMapping agentMapping = it.next();
			if (!agentMapping.isActive() || !matches(agentMapping, definedIPs, agentName)) {
				it.remove();
			}
		}

		if (CollectionUtils.isEmpty(mappings)) {
			throw new BusinessException("Determine an environment to use for the agent with name '" + agentName + "' and IP adress(es): " + definedIPs,
					ConfigurationInterfaceErrorCodeEnum.ENVIRONMENT_FOR_AGENT_NOT_FOUND);
		} else if (mappings.size() > 1) {
			throw new BusinessException("Determine an environment to use for the agent with name '" + agentName + "' and IP adress(es): " + definedIPs,
					ConfigurationInterfaceErrorCodeEnum.MORE_THAN_ONE_ENVIRONMENT_FOR_AGENT_FOUND);
		} else {
			String environmentId = mappings.get(0).getEnvironmentId();
			return configurationInterfaceManager.getEnvironment(environmentId);
		}
	}

	/**
	 * Checks if the specified {@link AgentMapping} is matching the agent name and IPs.
	 *
	 * @param agentMapping
	 *            {@link AgentMapping} to check.
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent.
	 * @return <code>true</code> if the name and any of the IP addresses match the defined
	 *         {@link AgentMapping}
	 */
	private boolean matches(AgentMapping agentMapping, List<String> definedIPs, String agentName) {
		// first match name
		String definedName = agentMapping.getAgentName();
		IMatchPattern namePattern = PatternFactory.getPattern(definedName);
		if (!namePattern.match(agentName)) {
			return false;
		}

		String definedIps = agentMapping.getIpAddress();
		// then all IPs, if any matches return true
		IMatchPattern ipPattern = PatternFactory.getPattern(definedIps);
		for (String ip : definedIPs) {
			if (ipPattern.match(ip)) {
				return true;
			}
		}
		return false;
	}
}
