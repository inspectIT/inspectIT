package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.jmeter.data.HttpUsecaseResult;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;

/**
 * Sampler that requests the usecase aggregated http view.
 * 
 * @author Stefan Siegl
 */
public class InspectITHttpUsecaseAggregation extends InspectITHttpAggregationBase {

	@Override
	public void run() throws Throwable {
		httpResult = repository.getHttpTimerDataAccessService().getTaggedAggregatedTimerData(httpTemplate, false);
	}

	@Override
	public InspectITResultMarker getResult() {
		return new HttpUsecaseResult(platformId, httpResult.size());
	}
}
