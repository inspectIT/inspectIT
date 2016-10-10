package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

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
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;

/**
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/bc/definition/app")
public class BusinessTransactionDefinitionRestfulService {

	/**
	 * Reference to the existing {@link IConfigurationInterfaceService}.
	 */
	@Autowired
	private IConfigurationInterfaceService configurationInterfaceService;

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
	 * Provides informations of business context definition.
	 *
	 * <p>
	 * <i> Example URL: /bc/definition/app</i>
	 * </p>
	 *
	 * @return a list of {@link ApplicationDefinition}.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public List<ApplicationDefinition> getApplicationDefinitions() {
		return configurationInterfaceService.getApplicationDefinitions();
	}

	/**
	 * Provides detail information of a business context definition (application).
	 *
	 * <p>
	 * <i> Example URL: /bc/definition/app/{id}</i>
	 * </p>
	 *
	 * @param id
	 *            Application ID.
	 *
	 * @return detail information of an {@link ApplicationDefinition}.
	 * @throws BusinessException
	 *             If given ID of the application is not valid.
	 */
	@RequestMapping(method = GET, value = "{id}")
	@ResponseBody
	public ApplicationDefinition getApplicationDefinition(@PathVariable int id) throws BusinessException {
		return configurationInterfaceService.getApplicationDefinition(id);
	}

	/**
	 * Provides information of all business transaction definitions of an application.
	 *
	 * <p>
	 * <i> Example URL: /bc/definition/app/{id}/btx</i>
	 * </p>
	 *
	 * @param id
	 *            Application ID.
	 *
	 * @return a list of {@link BusinessTransactionData}.
	 * @throws BusinessException
	 *             If given ID of the application is not valid.
	 */
	@RequestMapping(method = GET, value = "{id}/btx")
	@ResponseBody
	public List<BusinessTransactionDefinition> getBusinessTransactionDefinitions(@PathVariable int id) throws BusinessException {
		return configurationInterfaceService.getApplicationDefinition(id).getBusinessTransactionDefinitions();
	}

	/**
	 * Provides detail information of a business transaction definition and application.
	 *
	 * <p>
	 * <i> Example URL: /bc/definition/app/{id}/btx/{businessTxId}</i>
	 * </p>
	 *
	 * @param id
	 *            Application ID.
	 * @param businessTxId
	 *            Business transaction ID.
	 *
	 * @return detail information of a {@link BusinessTransactionData}.
	 * @throws BusinessException
	 *             If given ID of the application is not valid.
	 */
	@RequestMapping(method = GET, value = "{id}/btx/{businessTxId}")
	@ResponseBody
	public BusinessTransactionDefinition getBusinessTransactionDefinition(@PathVariable int id, @PathVariable int businessTxId) throws BusinessException {
		return configurationInterfaceService.getApplicationDefinition(id).getBusinessTransactionDefinition(businessTxId);
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
