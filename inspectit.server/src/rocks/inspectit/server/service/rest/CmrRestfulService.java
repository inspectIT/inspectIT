package info.novatec.inspectit.cmr.service.rest;

import info.novatec.inspectit.cmr.service.ICmrManagementService;
import info.novatec.inspectit.cmr.service.rest.error.JsonError;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.version.VersionService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
	 */
	@RequestMapping(method = RequestMethod.GET, value = "version")
	@ResponseBody
	public String getVersion() throws IOException {
		return versionService.getVersionAsString();
	}

	/**
	 * Returns CMR status information.
	 * <p>
	 * <i> Example URL: /cmr/status-data</i>
	 * 
	 * @return Returns CMR status information.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "status-data")
	@ResponseBody
	public CmrStatusData getStatusData() {
		return cmrManagementService.getCmrStatusData();
	}

}
