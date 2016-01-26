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
import rocks.inspectit.server.instrumentation.config.applier.MethodSensorInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.SpecialInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.TimerMethodSensorInstrumentationApplier;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.PatternFactory;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.ISensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;
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
	 *            environment {@link Environment} to get appliers for.
	 * @return Returns all {@link IInstrumentationApplier}s contained in all profiles for the given
	 *         environment and all functional applier defined by environment..
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

				// first all method assignments
				for (MethodSensorAssignment methodSensorAssignment : profile.getMethodSensorAssignments()) {
					appliers.add(getInstrumentationApplier(methodSensorAssignment, environment));
				}

				// then all exception ones
				for (ExceptionSensorAssignment exceptionSensorAssignment : profile.getExceptionSensorAssignments()) {
					appliers.add(getInstrumentationApplier(exceptionSensorAssignment, environment));
				}
			} catch (BusinessException e) {
				// on exception just exclude the profile
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + profileId + " ignored during profile difference calculation due to the exception.", e);
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
			return new SpecialInstrumentationApplier((SpecialMethodSensorAssignment) sensorAssignment, environment);
		} else if (MethodSensorAssignment.class.isAssignableFrom(sensorAssigmentClass)) {
			return new MethodSensorInstrumentationApplier((MethodSensorAssignment) sensorAssignment, environment, registrationService);
		}
		throw new IllegalArgumentException("Instrumentation applier can be created. Assignment " + sensorAssignment + " is of unknow type.");
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

		Collection<ExcludeRule> assignments = new ArrayList<>();
		for (String profileId : environment.getProfileIds()) {
			try {
				Profile profile = configurationInterfaceManager.getProfile(profileId);
				// don't include inactive profiles
				if (!profile.isActive()) {
					continue;
				}

				assignments.addAll(profile.getExcludeRules());
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + profileId + " ignored during profile difference calculation due to the exception.", e);
				}
				continue;
			}
		}
		return assignments;
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
		stringBuilder.append("|-class loading delegation: " + environment.isClassLoadingDelegation() + "\n"); // NOPMD
		stringBuilder.append("|-enhanced exception sensor: " + environment.getExceptionSensorConfig().isEnhanced()); // NOPMD

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
		List<AgentMapping> mappings = new ArrayList<>(configurationInterfaceManager.getAgentMappings().getMappings());

		for (Iterator<AgentMapping> it = mappings.iterator(); it.hasNext();) {
			AgentMapping agentMapping = it.next();
			if (!agentMapping.isActive() || !matches(agentMapping, definedIPs, agentName)) {
				it.remove();
			}
		}

		if (CollectionUtils.isEmpty(mappings)) {
			throw new BusinessException("Determing an environment to use for the agent with name '" + agentName + "' and IP adress(es): " + definedIPs,
					ConfigurationInterfaceErrorCodeEnum.ENVIRONMENT_FOR_AGENT_NOT_FOUND);
		} else if (mappings.size() > 1) {
			throw new BusinessException("Determing an environment to use for the agent with name '" + agentName + "' and IP adress(es): " + definedIPs,
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
