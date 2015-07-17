package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.jmeter.data.AggregatedTimerResult;
import info.novatec.inspectit.jmeter.data.ResultBase;

import java.util.List;

/**
 * Sampler that requests the aggregated timer view.
 * 
 * @author Stefan Siegl
 */
public class InspectITAggregatedTimer extends InspectITSamplerBase {

	/** search template. */
	private TimerData timerData;
	/** platformId. */
	private Long platformId;
	/** result. */
	private List<TimerData> timers;

	public Configuration[] getRequiredConfig() {
		return new Configuration[] { Configuration.PLATFORM_ID };
	}

	@Override
	public void setup() {
		timerData = new TimerData();
		platformId = getValue(Configuration.PLATFORM_ID);
		timerData.setPlatformIdent(platformId);
	}

	@Override
	public void run() {
		timers = repository.getTimerDataAccessService().getAggregatedTimerData(timerData);
	}

	@Override
	public ResultBase getResult() {
		return new AggregatedTimerResult(platformId, timers.size());
	}
}
