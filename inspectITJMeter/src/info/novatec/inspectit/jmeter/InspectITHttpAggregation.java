package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.jmeter.data.HttpAggregatedResult;
import info.novatec.inspectit.jmeter.data.InspectITResultMarker;

/**
 * Sampler that requests the aggregated http view.
 * 
 * @author Stefan Siegl
 */
public class InspectITHttpAggregation extends InspectITHttpAggregationBase {

	@Override
	public void run() throws Throwable {
		httpResult = repository.getHttpTimerDataAccessService().getAggregatedTimerData(httpTemplate, false);
	}

	@Override
	public InspectITResultMarker getResult() {
		return new HttpAggregatedResult(platformId, httpResult.size());
	}
}
