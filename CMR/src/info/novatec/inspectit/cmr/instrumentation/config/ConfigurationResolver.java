package info.novatec.inspectit.cmr.instrumentation.config;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.FunctionalMethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.exclude.ExcludeRule;
import info.novatec.inspectit.ci.factory.FunctionalMethodSensorAssignmentFactory;
import info.novatec.inspectit.cmr.ci.ConfigurationInterfaceManager;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.pattern.IMatchPattern;
import info.novatec.inspectit.pattern.PatternFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component that can resolve the different CI configuration questions.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ConfigurationResolver {

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
	 * {@link FunctionalMethodSensorAssignmentFactory}.
	 */
	@Autowired
	private FunctionalMethodSensorAssignmentFactory functionalAssignmentFactory;

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
					appliers.add(methodSensorAssignment.getInstrumentationApplier(environment, registrationService));
				}

				// then all exception ones
				for (ExceptionSensorAssignment exceptionSensorAssignment : profile.getExceptionSensorAssignments()) {
					appliers.add(exceptionSensorAssignment.getInstrumentationApplier(environment, registrationService));
				}
			} catch (Exception e) {
				// on exception just exclude the profile
				continue;
			}
		}

		// collect functionals as well
		for (FunctionalMethodSensorAssignment functionalAssignment : functionalAssignmentFactory.getFunctionalAssignments(environment)) {
			appliers.add(functionalAssignment.getInstrumentationApplier(environment, registrationService));
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
				continue;
			}
		}
		return assignments;
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
