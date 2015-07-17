package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;
import info.novatec.inspectit.jmeter.data.InvocationOverviewResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Sampler to get the invocation sequence overview.
 * 
 * @author Stefan Siegl
 */
public class InspectITInvocationOverview extends InspectITSamplerBase {

	/** platformId. */
	private Long platformIdent;
	/** Amount of invocation sequences to get. */
	private Integer count;
	/** Integrate the invocation IDs in the result. */
	private Boolean detailedInvocationOutput;
	/** result. */
	List<InvocationSequenceData> invocs;
	/** sorting. */
	private ResultComparator<InvocationSequenceData> sorting;

	@Override
	public Configuration[] getRequiredConfig() {
		return new Configuration[] { Configuration.PLATFORM_ID, Configuration.INVOC_COUNT, Configuration.INVOCATION_OVERVIEW_SORT, Configuration.OUTPUT };
	}

	@Override
	public void setup() {
		platformIdent = getValue(Configuration.PLATFORM_ID);
		count = getValue(Configuration.INVOC_COUNT);
		detailedInvocationOutput = getValue(Configuration.OUTPUT);
		sorting = getValue(Configuration.INVOCATION_OVERVIEW_SORT);
	}

	@Override
	public void run() {
		invocs = repository.getInvocationDataAccessService().getInvocationSequenceOverview(platformIdent, count, sorting);
	}

	@Override
	public InspectITResultMarker getResult() {
		List<Long> invocationIds = null;
		if (detailedInvocationOutput) {
			invocationIds = new ArrayList<Long>(count);
			for (InvocationSequenceData invocationSequenceData : invocs) {
				invocationIds.add(invocationSequenceData.getId());
			}
		}
		return new InvocationOverviewResult(platformIdent, count, invocationIds);
	}

}
