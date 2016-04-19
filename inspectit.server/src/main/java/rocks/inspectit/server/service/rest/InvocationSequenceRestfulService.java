/**
 *
 */
package rocks.inspectit.server.service.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.InvocationSequenceManagementErrorCodeEnum;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;

/**
 * @author Mario Mann
 *
 */
@Controller
@RequestMapping(value = "/invocations")
public class InvocationSequenceRestfulService {

	/**
	 * Reference to the existing {@link IInvocationDataAccessService}.
	 */
	@Autowired
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * Reference to the existing {@link IGlobalDataAccessService}.
	 */
	@Autowired
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Provides overview of several invocation data.
	 *
	 * @param id
	 *            Agent ID.
	 * @param fromDate
	 *            Begin of time period.
	 * @param toDate
	 *            End of time period.
	 * @param latestReadId
	 *            Latest read ID of the invocations, only invocations with higher id are submitted.
	 * @param limit
	 *            The limit/size of the results.
	 * @return Returns the list of invocation sequences.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "overview")
	@ResponseBody
	public List<InvocationSequenceData> getInvocationSequenceOverview(@RequestParam(value = "id", required = false) Long id, @RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate, @RequestParam(value = "latestReadId", required = false) Long latestReadId,
			@RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {

		Parameter parameter = new Parameter(id, fromDate, toDate, latestReadId);

		List<InvocationSequenceData> result = getInvocationSequence3(getInvocationSequence2(getInvocationSequence1(parameter), parameter), parameter);

		if (result.size() > limit) {
			result = result.subList(0, limit);
		}

		return result;
	}

	/**
	 * Provides detail informations of an invocation sequence data.
	 *
	 * @param id
	 *            Invocation sequence ID.
	 * @return InvocationSequenceData
	 * @throws BusinessException
	 *             If given ID of the invocation sequence is not valid.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "details")
	@ResponseBody
	public InvocationSequenceData getInvocationSequenceDetails(@RequestParam(value = "id", required = true) long id) throws BusinessException {
		InvocationSequenceData template = new InvocationSequenceData();
		template.setId(id);
		InvocationSequenceData result = invocationDataAccessService.getInvocationSequenceDetail(template);
		if (result == null) {
			throw new BusinessException("Invocation sequence with ID " + id + " does not exist!", InvocationSequenceManagementErrorCodeEnum.INVOCATION_SEQUENCE_DOES_NOT_EXIST);
		}

		return result;
	}

	/**
	 * Returns a list of {@link InvocationSequenceData}, only invocations with higher latest read ID
	 * are submitted, if specified.
	 *
	 * @param invocationSequences
	 *            List of invocation sequences.
	 * @param parameter
	 *            Parameters which were specified in the REST call.
	 * @return Returns the list of invocation sequences.
	 */
	private List<InvocationSequenceData> getInvocationSequence3(List<InvocationSequenceData> invocationSequences, Parameter parameter) {
		List<InvocationSequenceData> result = new ArrayList<>();
		if (parameter.latestReadId instanceof Long) {
			for (InvocationSequenceData invocationSequence : invocationSequences) {
				if (invocationSequence.getId() >= parameter.latestReadId) {
					result.add(invocationSequence);
				}
			}
		} else {
			result = invocationSequences;
		}

		return result;
	}

	/**
	 * Returns a list of {@link InvocationSequenceData}, only in a range of fromDate and toDate
	 * parameter, if specified.
	 *
	 * @param invocationSequences
	 *            List of invocation sequences.
	 * @param parameter
	 *            Parameters which were specified in the REST call.
	 * @return Returns the list of invocation sequences.
	 * @throws IllegalArgumentException
	 */
	private List<InvocationSequenceData> getInvocationSequence2(List<InvocationSequenceData> invocationSequences, Parameter parameter) {
		List<InvocationSequenceData> result = new ArrayList<>();
		if (parameter.fromDate != null && parameter.toDate != null) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date startDate = simpleDateFormat.parse(String.format("%s 00:00:00", parameter.fromDate));
				Date endDate = simpleDateFormat.parse(String.format("%s 23:59:59", parameter.toDate));
				for (InvocationSequenceData invocationSequence : invocationSequences) {
					if (invocationSequence.getTimeStamp().after(startDate) && invocationSequence.getTimeStamp().before(endDate)) {
						result.add(invocationSequence);
					}
				}
			} catch (ParseException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		} else {
			result = invocationSequences;
		}

		return result;
	}

	/**
	 * Returns a list of {@link InvocationSequenceData}, only invocations with the agent id are
	 * submitted, if specified.
	 *
	 * @param parameter
	 *            Parameters which were specified in the REST call.
	 * @return Returns the list of invocation sequences.
	 */
	private List<InvocationSequenceData> getInvocationSequence1(Parameter parameter) {
		List<InvocationSequenceData> result = new ArrayList<>();
		Map<PlatformIdent, AgentStatusData> agentsOverviewMap = globalDataAccessService.getAgentsOverview();

		if (parameter.id == null) {
			for (Map.Entry<PlatformIdent, AgentStatusData> agent : agentsOverviewMap.entrySet()) {
				result.addAll(invocationDataAccessService.getInvocationSequenceOverview(agent.getKey().getId(), null));
			}
		} else {
			result.addAll(invocationDataAccessService.getInvocationSequenceOverview(parameter.id, null));
		}

		return result;
	}

	/**
	 * @author Mario Mann
	 *
	 */
	private static class Parameter {
		/**
		 * Agent ID.
		 */
		final Long id;

		/**
		 * Begin of time period.
		 */
		final String fromDate;

		/**
		 * End of time period.
		 */
		final String toDate;

		/**
		 * Latest read ID of the invocations, only invocations with higher id are submitted.
		 */
		final Long latestReadId;

		/**
		 * @param id
		 *            Agent ID.
		 * @param fromDate
		 *            Begin of time period.
		 * @param toDate
		 *            End of time period.
		 * @param latestReadId
		 *            Latest read ID of the invocations, only invocations with higher id are
		 *            submitted.
		 */
		Parameter(Long id, String fromDate, String toDate, Long latestReadId) {
			this.id = id;
			this.fromDate = fromDate;
			this.toDate = toDate;
			this.latestReadId = latestReadId;
		}
	}
}
