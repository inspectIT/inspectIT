package rocks.inspectit.shared.all.cmr.service;

import java.util.List;
import java.util.Map;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;

/**
 * Interface for agent communication with the CMR.
 *
 * @author Ivan Senic
 *
 */
@ServiceInterface(exporter = ServiceExporterType.RMI, serviceId = 4)
public interface IAgentService {

	/**
	 * Registers the agent with the CMR. The CMR will answer with the {@link AgentConfig}
	 * containing all necessary information for the agent initialization.
	 *
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent.
	 * @param version
	 *            The version the agent is currently running with.
	 * @return {@link AgentConfig}.
	 * @throws BusinessException
	 *             When no environment can be located for the agent based on the mapping settings.
	 */
	AgentConfig register(List<String> definedIPs, String agentName, String version) throws BusinessException;

	/**
	 * Unregisters the platform in the CMR by sending the agent id.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 *
	 * @throws BusinessException
	 *             If un-registration fails.
	 */
	void unregister(long platformIdent) throws BusinessException;

	/**
	 * Analyzes the given type and adds instrumentation points if necessary, returning the
	 * instrumentation result so that instrumentation can be performed on the agent.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @param hash
	 *            Class hash code.
	 * @param type
	 *            Parsed {@link Type} representing class being loaded on the agent.
	 * @return Instrumentation definition containing method instrumentation configs or <code>null</code>
	 *         if nothing should be instrumented.
	 * @throws BusinessException
	 *             If agent with specified id does not exist.
	 */
	InstrumentationDefinition analyze(long platformIdent, String hash, Type type) throws BusinessException;

	/**
	 * Informs the CMR that the methods have been instrumented on the agent.
	 *
	 * @param methodToSensorMap
	 *            map containing method id as key and applied sensor IDs
	 */
	void instrumentationApplied(Map<Long, long[]> methodToSensorMap);

}
