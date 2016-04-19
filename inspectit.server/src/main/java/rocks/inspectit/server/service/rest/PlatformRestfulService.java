package rocks.inspectit.server.service.rest;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * Restful service provider for platform/agent information.
 *
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/platform")
public class PlatformRestfulService {

	/**
	 * Reference to the existing {@link IGlobalDataAccessService}.
	 */
	@Autowired
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Returns information of all existing agents.
	 * <p>
	 * <i> Example URL: /platform/all</i>
	 * </p>
	 *
	 * @return a list of {@link InvocationSequenceData} with all existing agents.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "all")
	@ResponseBody
	public Set<PlatformIdent> getAll() {
		Map<PlatformIdent, AgentStatusData> agentsOverviewMap = globalDataAccessService.getAgentsOverview();
		return agentsOverviewMap.keySet();
	}

	/**
	 * Returns the status of a given agentId.
	 * <p>
	 * <i> Example URL: /platform/status?id=1</i>
	 * </p>
	 *
	 * @param platformId
	 *            AGENT/PLATFORM ID bounded from path.
	 * @return the status of a given platformId.
	 * @throws BusinessException
	 *             If given ID of the agent/platform is not valid.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "status")
	@ResponseBody
	public AgentStatusData getStatus(@RequestParam(value = "platformId", required = true) long platformId) throws BusinessException {
		AgentStatusData agentStatus = null;
		Map<PlatformIdent, AgentStatusData> agents = globalDataAccessService.getAgentsOverview();
		for (Entry<PlatformIdent, AgentStatusData> entry : agents.entrySet()) {
			if (entry.getKey().getId() == platformId) {
				agentStatus = globalDataAccessService.getAgentsOverview().get(entry.getKey());
			}
		}

		if (agentStatus != null) {
			throw new BusinessException("Agent with ID " + platformId + " does not exist!", AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST);
		}
		return agentStatus;
	}

	/**
	 * Returns list of instrumented methods of a given agentId.
	 * <p>
	 * <i> Example URL: /platform/methods?id=1</i>
	 * </p>
	 *
	 * @param platformId
	 *            AGENT/PLATFORM ID bounded from path.
	 * @return a Set of {@link MethodIdent} of a given platformId.
	 * @throws BusinessException
	 *             If given ID of the agent/platform is not valid.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "methods")
	@ResponseBody
	public Set<MethodIdent> getMethods(@RequestParam(value = "platformId", required = true) long platformId) throws BusinessException {
		return globalDataAccessService.getCompleteAgent(platformId).getMethodIdents();
	}

	/**
	 * Returns the complete sensors informations of an agent.
	 * <p>
	 * <i> Example URL: /platform/sensors?id=1</i>
	 * </p>
	 *
	 * @param platformId
	 *            AGENT/PLATFORM ID bounded from path.
	 * @return a Set of {@link SensorTypeIdent} of the given agentId.
	 * @throws BusinessException
	 *             If given ID of the agent/platform is not valid.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "sensors")
	@ResponseBody
	public Set<SensorTypeIdent> getSensors(@RequestParam(value = "platformId", required = true) long platformId) throws BusinessException {
		return globalDataAccessService.getCompleteAgent(platformId).getSensorTypeIdents();
	}

}
