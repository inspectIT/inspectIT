package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.List;

/**
 * Base class for Http related inspectIT operations.
 * 
 * @author Stefan Siegl
 * 
 */
public abstract class InspectITHttpAggregationBase extends InspectITSamplerBase {

	/** query template. */
	protected HttpTimerData httpTemplate;
	/** platformId. */
	protected Long platformId;
	/** Result. */
	protected List<HttpTimerData> httpResult;

	@Override
	public Configuration[] getRequiredConfig() {
		return new Configuration[] { Configuration.PLATFORM_ID };
	}

	@Override
	public void setup() throws Exception {
		httpTemplate = new HttpTimerData();
		platformId = getValue(Configuration.PLATFORM_ID);
		httpTemplate.setPlatformIdent(platformId);
	}
}
