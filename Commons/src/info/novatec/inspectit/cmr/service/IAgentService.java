package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;

import java.util.List;
import java.util.Map;

/**
 * Interface for agent communication with the CMR.
 *
 * @author Ivan Senic
 *
 */
@ServiceInterface(exporter = ServiceExporterType.RMI, serviceId = 4)
public interface IAgentService {

	/**
	 * Registers the agent with the CMR. The CMR will answer with the {@link AgentConfiguration}
	 * containing all necessary information for the agent initialization.
	 *
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent. Can be <code>null</code>.
	 * @param version
	 *            The version the agent is currently running with.
	 * @return {@link AgentConfiguration}.
	 * @throws BusinessException
	 *             When no environment can be located for the agent based on the mapping settings.
	 */
	AgentConfiguration register(List<String> definedIPs, String agentName, String version) throws BusinessException;

	/**
	 * Unregisters the platform in the CMR by sending the agent name and the network interfaces
	 * defined by the machine.
	 *
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            Name of the Agent.
	 * @throws BusinessException
	 *             If un-registration fails.
	 */
	void unregister(List<String> definedIPs, String agentName) throws BusinessException;

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
	 * @return Instrumentation result containing method instrumentation configs or <code>null</code>
	 *         if nothing was instrumented.
	 * @throws BusinessException
	 *             If agent with specified id does not exist.
	 */
	InstrumentationResult analyzeAndInstrument(long platformIdent, String hash, Type type) throws BusinessException;

	/**
	 * Informs the CMR that the methods have been instrumented on the agent.
	 *
	 * @param methodToSensorMap
	 *            map containing method id as key and applied sensor IDs
	 */
	void instrumentationApplied(Map<Long, long[]> methodToSensorMap);

}
