package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Collection;

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
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;

/**
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/bc/app")
public class BusinessTransactionRestfulService {

	/**
	 * Reference to the existing {@link IBusinessContextManagementService}.
	 */
	@Autowired
	private IBusinessContextManagementService businessContextManagementService;

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
	 * Provides informations of business context instances.
	 *
	 * <p>
	 * <i> Example URL: /bc/app</i>
	 * </p>
	 *
	 * @return a list of {@link ApplicationData}.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public Collection<ApplicationData> getApplications() {
		return businessContextManagementService.getApplications();
	}

	/**
	 * Provides detail information of a business context instances (application).
	 *
	 * <p>
	 * <i> Example URL: /bc/app/{id}</i>
	 * </p>
	 *
	 * @param id
	 *            Application ID.
	 *
	 * @return detail information of an {@link ApplicationData}.
	 */
	@RequestMapping(method = GET, value = "{id}")
	@ResponseBody
	public ApplicationData getApplication(@PathVariable int id) {
		return businessContextManagementService.getApplicationForId(id);
	}

	/**
	 * Provides information of all business transactions of an application.
	 *
	 * <p>
	 * <i> Example URL: /bc/app/{id}/btx</i>
	 * </p>
	 *
	 * @param id
	 *            Application ID.
	 *
	 * @return a list of {@link BusinessTransactionData}.
	 */
	@RequestMapping(method = GET, value = "{id}/btx")
	@ResponseBody
	public Collection<BusinessTransactionData> getBusinessTransactions(@PathVariable int id) {
		return businessContextManagementService.getBusinessTransactions(id);
	}

	/**
	 * Provides detail information of a business transaction and application.
	 *
	 * <p>
	 * <i> Example URL: /bc/app/{id}/btx/{businessTxId}</i>
	 * </p>
	 *
	 * @param id
	 *            Application ID.
	 * @param businessTxId
	 *            Business transaction ID.
	 *
	 * @return detail information of a {@link BusinessTransactionData}.
	 */
	@RequestMapping(method = GET, value = "{id}/btx/{businessTxId}")
	@ResponseBody
	public BusinessTransactionData getBusinessTransaction(@PathVariable int id, @PathVariable int businessTxId) {
		return businessContextManagementService.getBusinessTransactionForId(id, businessTxId);
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
