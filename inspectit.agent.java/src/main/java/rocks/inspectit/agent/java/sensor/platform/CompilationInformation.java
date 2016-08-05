package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;

/**
 * This class provides dynamic information about the compilation system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class CompilationInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private CompilationInformationData compilationInformationData = new CompilationInformationData();

	/**
	 * The {@link RuntimeInfoProvider} used to retrieve information from the compilation system.
	 */
	private RuntimeInfoProvider runtimeBean;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void gather() {
		// The timestamp is set in the {@link CompilationInformation#reset()} to avoid multiple
		// renewal. It will not be set on the first execution of
		// {@link CompilationInformation#gather()}, but shortly before.
		long totalCompilationTime = this.getRuntimeBean().getTotalCompilationTime();

		this.compilationInformationData.incrementCount();
		this.compilationInformationData.addTotalCompilationTime(totalCompilationTime);

		if (totalCompilationTime < this.compilationInformationData.getMinTotalCompilationTime()) {
			this.compilationInformationData.setMinTotalCompilationTime(totalCompilationTime);
		} else if (totalCompilationTime > this.compilationInformationData.getMaxTotalCompilationTime()) {
			this.compilationInformationData.setMaxTotalCompilationTime(totalCompilationTime);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SystemSensorData get() {
		CompilationInformationData newCompilationInformationData = new CompilationInformationData();

		newCompilationInformationData.setPlatformIdent(this.compilationInformationData.getPlatformIdent());
		newCompilationInformationData.setSensorTypeIdent(this.compilationInformationData.getSensorTypeIdent());
		newCompilationInformationData.setCount(this.compilationInformationData.getCount());

		newCompilationInformationData.setTotalTotalCompilationTime(this.compilationInformationData.getTotalTotalCompilationTime());
		newCompilationInformationData.setMaxTotalCompilationTime(this.compilationInformationData.getMaxTotalCompilationTime());
		newCompilationInformationData.setMinTotalCompilationTime(this.compilationInformationData.getMinTotalCompilationTime());

		newCompilationInformationData.setTimeStamp(this.compilationInformationData.getTimeStamp());

		return this.compilationInformationData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		this.compilationInformationData.setCount(0);

		this.compilationInformationData.setTotalTotalCompilationTime(0L);
		this.compilationInformationData.setMinTotalCompilationTime(Long.MAX_VALUE);
		this.compilationInformationData.setMaxTotalCompilationTime(0L);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.compilationInformationData.setTimeStamp(timestamp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SystemSensorData getSystemSensorData() {
		return this.compilationInformationData;
	}

	/**
	 * Gets the {@link RuntimeInfoProvider}. The getter method is provided for better testability.
	 *
	 * @return {@link RuntimeInfoProvider}.
	 */
	private RuntimeInfoProvider getRuntimeBean() {
		if (this.runtimeBean == null) {
			this.runtimeBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getRuntimeInfoProvider();
		}
		return this.runtimeBean;
	}
}
