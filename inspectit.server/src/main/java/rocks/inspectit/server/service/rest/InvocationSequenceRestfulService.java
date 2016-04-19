package rocks.inspectit.server.service.rest;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	 * Provides overview of several invocation data.
	 *
	 * *
	 * <p>
	 * <i> Example URL: /data/invocations/overview</i>
	 * </p>
	 *
	 * @param platformId
	 *            Agent/Platform ID.
	 * @param fromDate
	 *            Begin of time period.
	 * @param toDate
	 *            End of time period.
	 * @param latestReadId
	 *            Latest read ID of the invocations, only invocations with equal or higher id are
	 *            submitted.
	 * @param limit
	 *            The limit/size of the results.
	 * @return a list of {@link InvocationSequenceData}.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "overview")
	@ResponseBody
	public List<InvocationSequenceData> getInvocationSequenceOverview(@RequestParam(value = "platformId", required = false, defaultValue = "0") Long platformId,
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date toDate,
			@RequestParam(value = "latestReadId", required = false, defaultValue = "0") Long latestReadId, @RequestParam(value = "limit", defaultValue = "100") int limit) {

		List<InvocationSequenceData> result = invocationDataAccessService.getInvocationSequenceOverview(platformId, limit, fromDate, toDate, latestReadId, null);

		return result;
	}

	/**
	 * Provides detail informations of an invocation sequence data.
	 *
	 * <p>
	 * <i> Example URL: /data/invocations/details?id=4611686018427388252</i>
	 * </p>
	 *
	 * @param id
	 *            Invocation sequence ID.
	 * @return detail information of an {@link InvocationSequenceData}.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "details")
	@ResponseBody
	public InvocationSequenceData getInvocationSequenceDetails(@RequestParam(value = "id", required = true) long id) {
		InvocationSequenceData template = new InvocationSequenceData();
		template.setId(id);
		InvocationSequenceData result = invocationDataAccessService.getInvocationSequenceDetail(template);

		return result;
	}
}
