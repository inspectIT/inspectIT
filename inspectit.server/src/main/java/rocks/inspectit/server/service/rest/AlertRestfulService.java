package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.shared.cs.cmr.service.IAlertService;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Restful service provider for alert information.
 *
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/alerts")
public class AlertRestfulService {

	/**
	 * Reference to the existing {@link IAlertService}.
	 */
	@Autowired
	private IAlertService alertService;

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
	 * Returns information of all existing alerts.
	 * <p>
	 * <i> Example URL: /alerts</i>
	 * </p>
	 *
	 * @return a list of {@link Alert} with all existing alerts.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public List<Alert> getAll() {
		return alertService.getAlerts();
	}

	/**
	 * Returns information of all open alerts.
	 * <p>
	 * <i> Example URL: /alerts/open</i>
	 * </p>
	 *
	 * @return a list of {@link Alert} with all open alerts.
	 */
	@RequestMapping(method = GET, value = "open")
	@ResponseBody
	public List<Alert> getOpenAlert() {
		return alertService.getOpenAlerts();
	}

	/**
	 * Returns information of all closed alerts.
	 * <p>
	 * <i> Example URL: /alerts/closed</i>
	 * </p>
	 *
	 * @return a list of {@link Alert} with all closed alerts.
	 */
	@RequestMapping(method = GET, value = "closed")
	@ResponseBody
	public List<Alert> getClosedAlert() {
		return alertService.getClosedAlerts();
	}

	/**
	 * Returns the alert of a given alertId.
	 * <p>
	 * <i> Example URL: /alerts/{alertId}</i>
	 * </p>
	 *
	 * @param alertId
	 *            Alert ID bounded from path.
	 * @return the alert of a given alertId.
	 */
	@RequestMapping(method = GET, value = "{alertId}")
	@ResponseBody
	public Alert getAlert(@PathVariable String alertId) {
		return alertService.getAlert(alertId);
	}
}
