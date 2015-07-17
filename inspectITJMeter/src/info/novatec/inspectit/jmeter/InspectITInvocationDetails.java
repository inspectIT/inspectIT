package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;
import info.novatec.inspectit.jmeter.data.InvocationDetailResult;

/**
 * Sampler to get the invocation details.
 * 
 * @author Stefan Siegl.
 */
public class InspectITInvocationDetails extends InspectITSamplerBase {

	/** Search template. */
	private InvocationSequenceData template;
	/** Result. */
	private InvocationSequenceData result;

	@Override
	public Configuration[] getRequiredConfig() {
		return new Configuration[] { Configuration.INVOC_ID };
	}

	@Override
	public void run() {
		result = repository.getInvocationDataAccessService().getInvocationSequenceDetail(template);
	}

	@Override
	public InspectITResultMarker getResult() {
		return new InvocationDetailResult(result);
	}

	@Override
	public void setup() {
		template = new InvocationSequenceData();
		Long invocId = getValue(Configuration.INVOC_ID);
		template.setId(invocId);
	}
}
