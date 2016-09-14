package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;

/**
 * Restful service provider for detail {@link InvocationSequenceData} information.
 *
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/data/invocations")
public class InvocationSequenceRestfulService {

	/**
	 * Reference to the existing {@link IInvocationDataAccessService}.
	 */
	@Autowired
	private IInvocationDataAccessService invocationDataAccessService;

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
	 * Provides overview of several invocation data.
	 *
	 * *
	 * <p>
	 * <i> Example URL: /data/invocations</i>
	 * </p>
	 *
	 * @param agentId
	 *            Agent ID.
	 * @param fromDate
	 *            Begin of time period.
	 * @param toDate
	 *            End of time period.
	 * @param latestReadId
	 *            Latest read ID of the invocations, only invocations with higher id are submitted.
	 * @param limit
	 *            The limit/size of the results.
	 * @return a list of {@link InvocationSequenceData}.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public List<InvocationSequenceData> getInvocationSequenceOverview(@RequestParam(value = "agentId", required = false, defaultValue = "0") Long agentId,
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Date fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Date toDate,
			@RequestParam(value = "latestReadId", required = false, defaultValue = "0") Long latestReadId, @RequestParam(value = "limit", defaultValue = "100") int limit) {

		List<InvocationSequenceData> result = invocationDataAccessService.getInvocationSequenceOverview(agentId, limit, fromDate, toDate, latestReadId + 1, null);

		return result;
	}

	/**
	 * Provides detail informations of an invocation sequence data.
	 *
	 * <p>
	 * <i> Example URL: /data/invocations/{id}</i>
	 * </p>
	 *
	 * @param id
	 *            Invocation sequence ID.
	 * @return detail information of an {@link InvocationSequenceData}.
	 */
	@RequestMapping(method = GET, value = "{id}")
	@ResponseBody
	public InvocationSequenceData getInvocationSequenceDetails(@PathVariable long id) {
		InvocationSequenceData template = new InvocationSequenceData();
		template.setId(id);
		InvocationSequenceData result = invocationDataAccessService.getInvocationSequenceDetail(template);

		return result;
	}
}
