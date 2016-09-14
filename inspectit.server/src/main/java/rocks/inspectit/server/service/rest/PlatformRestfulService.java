package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * Restful service provider for agent information.
 *
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/data/agents")
public class PlatformRestfulService {

	/**
	 * Header information for swagger requests.
	 *
	 * @param response
	 *            Response information
	 */
	@ModelAttribute
	public void setVaryResponseHeader(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
	}

	/**
	 * Reference to the existing {@link IGlobalDataAccessService}.
	 */
	@Autowired
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Handling of all the exceptions happening in this controller.
	 *
	 * @param exception
	 *            Exception being thrown
	 * @return {@link ModelAndView}
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(Exception exception) {
		return new JsonError(exception).asModelAndView();
	}

	/**
	 * Returns information of all existing agents.
	 * <p>
	 * <i> Example URL: /data/agents</i>
	 * </p>
	 *
	 * @return a list of {@link PlatformIdent} with all existing agents.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public Set<PlatformIdent> getAll() {
		Map<PlatformIdent, AgentStatusData> agentsOverviewMap = globalDataAccessService.getAgentsOverview();
		return agentsOverviewMap.keySet();
	}

	/**
	 * Returns the status of a given agentId.
	 * <p>
	 * <i> Example URL: /data/agents/{agentId}</i>
	 * </p>
	 *
	 * @param agentId
	 *            AGENT ID bounded from path.
	 * @return the status of a given agentId.
	 * @throws BusinessException
	 *             If given ID of the agent is not valid.
	 */
	@RequestMapping(method = GET, value = "{agentId}")
	@ResponseBody
	public AgentStatusData getStatus(@PathVariable long agentId) throws BusinessException {
		AgentStatusData agentStatus = null;
		Map<PlatformIdent, AgentStatusData> agents = globalDataAccessService.getAgentsOverview();
		for (Entry<PlatformIdent, AgentStatusData> entry : agents.entrySet()) {
			if (entry.getKey().getId() == agentId) {
				agentStatus = globalDataAccessService.getAgentsOverview().get(entry.getKey());
			}
		}

		if (agentStatus == null) {
			throw new BusinessException("Agent with ID " + agentId + " does not exist!", AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST);
		}
		return agentStatus;
	}

	/**
	 * Returns list of instrumented methods of a given agentId.
	 * <p>
	 * <i> Example URL: /data/agents/{agentId}/methods</i>
	 * </p>
	 *
	 * @param agentId
	 *            AGENT ID bounded from path.
	 * @return a Set of {@link MethodIdent} of a given agentId.
	 * @throws BusinessException
	 *             If given ID of the agent is not valid.
	 */
	@RequestMapping(method = GET, value = "{agentId}/methods")
	@ResponseBody
	public Set<MethodIdent> getMethods(@PathVariable long agentId) throws BusinessException {
		return globalDataAccessService.getCompleteAgent(agentId).getMethodIdents();
	}

	/**
	 * Returns the complete sensors informations of an agent.
	 * <p>
	 * <i> Example URL: /data/agents/{agentId}/sensors</i>
	 * </p>
	 *
	 * @param agentId
	 *            AGENT ID bounded from path.
	 * @return a Set of {@link SensorTypeIdent} of the given agentId.
	 * @throws BusinessException
	 *             If given ID of the agent is not valid.
	 */
	@RequestMapping(method = GET, value = "{agentId}/sensors")
	@ResponseBody
	public Set<SensorTypeIdent> getSensors(@PathVariable long agentId) throws BusinessException {
		return globalDataAccessService.getCompleteAgent(agentId).getSensorTypeIdents();
	}

}
