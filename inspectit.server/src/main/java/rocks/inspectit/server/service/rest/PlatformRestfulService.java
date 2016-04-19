/**
 *
 */
package rocks.inspectit.server.service.rest;

import java.util.Map;
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
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
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
	 * @return List<InvocationSequenceData>
	 */
	@RequestMapping(method = RequestMethod.GET, value = "all")
	@ResponseBody
	public Set<PlatformIdent> getAll() {
		Map<PlatformIdent, AgentStatusData> agentsOverviewMap = globalDataAccessService.getAgentsOverview();

		return agentsOverviewMap.keySet();
	}

	/**
	 * Returns the status of a given platformId.
	 * <p>
	 * <i> Example URL: /platform/status?id=1</i>
	 * </p>
	 *
	 * @param id
	 *            PLATFORMID bounded from path.
	 * @return String
	 * @throws BusinessException
	 *             If given ID of the agent is not valid.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "status")
	@ResponseBody
	public String getStatus(@RequestParam(value = "id", required = true) long id) throws BusinessException {
		PlatformIdent agent = globalDataAccessService.getCompleteAgent(id);
		return globalDataAccessService.getAgentsOverview().get(agent).getAgentConnection().toString();
	}

	/**
	 * Returns list of instrumented methods of a given platformId.
	 * <p>
	 * <i> Example URL: /platform/methods?id=1</i>
	 * </p>
	 *
	 * @param id
	 *            PLATFORMID bounded from path.
	 * @return Set<MethodIdent>
	 * @throws BusinessException
	 *             If given ID of the agent is not valid.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "methods")
	@ResponseBody
	public Set<MethodIdent> getMethods(@RequestParam(value = "id", required = true) long id) throws BusinessException {
		return globalDataAccessService.getCompleteAgent(id).getMethodIdents();
	}

	@RequestMapping(method = RequestMethod.GET, value = "sensors")
	@ResponseBody
	public Set<SensorTypeIdent> getSensors(@RequestParam(value = "id", required = true) long id) throws BusinessException {
		return globalDataAccessService.getCompleteAgent(id).getSensorTypeIdents();
	}

}
