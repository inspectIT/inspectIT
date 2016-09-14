package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.shared.all.communication.data.cmr.CmrStatusData;
import rocks.inspectit.shared.all.version.InvalidVersionException;
import rocks.inspectit.shared.all.version.VersionService;
import rocks.inspectit.shared.cs.cmr.service.ICmrManagementService;

/**
 * Restful service provider for CMR information.
 *
 * @author Ivan Senic
 *
 */
@Controller
@RequestMapping(value = "/cmr")
public class CmrRestfulService {

	/**
	 * Reference to the existing {@link VersionService}.
	 */
	@Autowired
	private VersionService versionService;

	/**
	 * Reference to the existing {@link IVersioningService}.
	 */
	@Autowired
	private ICmrManagementService cmrManagementService;

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
	 * Returns CMR version.
	 * <p>
	 * <i> Example URL: /cmr/version</i>
	 *
	 * @return Returns CMR version.
	 * @throws IOException
	 *             If version information is not available.
	 * @throws InvalidVersionException
	 *             If version information is not available.
	 */
	@RequestMapping(method = GET, value = "version")
	@ResponseBody
	public String getVersion() throws IOException, InvalidVersionException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(versionService.getVersionAsString());
	}

	/**
	 * Returns CMR status information.
	 * <p>
	 * <i> Example URL: /cmr/status</i>
	 *
	 * @return Returns CMR status information.
	 */
	@RequestMapping(method = GET, value = "status")
	@ResponseBody
	public CmrStatusData getStatusData() {
		return cmrManagementService.getCmrStatusData();
	}

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
}
