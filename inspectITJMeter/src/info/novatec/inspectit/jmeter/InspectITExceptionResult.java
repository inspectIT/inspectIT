package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.jmeter.data.ExceptionResult;
import info.novatec.inspectit.jmeter.data.ResultBase;

import java.util.List;

/**
 * Sampler that requests the exception view.
 * 
 * @author Stefan Siegl
 */
public class InspectITExceptionResult extends InspectITSamplerBase {

	/** search template. */
	private ExceptionSensorData exceptionData;
	/** platformId. */
	private Long platformId;
	/** sorting. */
	private ResultComparator<ExceptionSensorData> sorting;
	/** result. */
	private List<ExceptionSensorData> exceptions;

	public Configuration[] getRequiredConfig() {
		return new Configuration[] { Configuration.PLATFORM_ID, Configuration.EXCEPTION_SORT };
	}

	@Override
	public void setup() {
		exceptionData = new ExceptionSensorData();
		platformId = getValue(Configuration.PLATFORM_ID);
		exceptionData.setPlatformIdent(platformId);
		sorting = getValue(Configuration.EXCEPTION_SORT);
	}

	@Override
	public void run() {
		exceptions = repository.getExceptionDataAccessService().getUngroupedExceptionOverview(exceptionData, sorting);
	}

	@Override
	public ResultBase getResult() {
		return new ExceptionResult(platformId, exceptions.size());
	}
}
