package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;

/**
 * This class provides dynamic information about the class loading system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class ClassLoadingInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private ClassLoadingInformationData classLoadingInformationData = new ClassLoadingInformationData();

	/**
	 * The {@link RuntimeInfoProvider} used to retrieve information from the class loading system.
	 */
	private RuntimeInfoProvider runtimeBean;

	/**
	 * {@inheritDoc}
	 */
	public void gather() {

		// The timestamp is set in the {@link ClassLoadingInformation#reset()} to avoid multiple
		// renewal. It will not be set on the first execution of
		// {@link ClassLoadingInformation#gather()}, but shortly before.
		int loadedClassCount = this.getRuntimeBean().getLoadedClassCount();
		long totalLoadedClassCount = this.getRuntimeBean().getTotalLoadedClassCount();
		long unloadedClassCount = this.getRuntimeBean().getUnloadedClassCount();

		this.classLoadingInformationData.incrementCount();
		this.classLoadingInformationData.addLoadedClassCount(loadedClassCount);
		this.classLoadingInformationData.addTotalLoadedClassCount(totalLoadedClassCount);
		this.classLoadingInformationData.addUnloadedClassCount(unloadedClassCount);

		if (loadedClassCount < this.classLoadingInformationData.getMinLoadedClassCount()) {
			this.classLoadingInformationData.setMinLoadedClassCount(loadedClassCount);
		} else if (loadedClassCount > this.classLoadingInformationData.getMaxLoadedClassCount()) {
			this.classLoadingInformationData.setMaxLoadedClassCount(loadedClassCount);
		}

		if (totalLoadedClassCount < this.classLoadingInformationData.getMinTotalLoadedClassCount()) {
			this.classLoadingInformationData.setMinTotalLoadedClassCount(totalLoadedClassCount);
		} else if (totalLoadedClassCount > this.classLoadingInformationData.getMaxTotalLoadedClassCount()) {
			this.classLoadingInformationData.setMaxTotalLoadedClassCount(totalLoadedClassCount);
		}

		if (unloadedClassCount < this.classLoadingInformationData.getMinUnloadedClassCount()) {
			this.classLoadingInformationData.setMinUnloadedClassCount(unloadedClassCount);
		} else if (unloadedClassCount > this.classLoadingInformationData.getMaxUnloadedClassCount()) {
			this.classLoadingInformationData.setMaxUnloadedClassCount(unloadedClassCount);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData get() {
		ClassLoadingInformationData newClassLoadingInformationData = new ClassLoadingInformationData();

		newClassLoadingInformationData.setPlatformIdent(this.classLoadingInformationData.getPlatformIdent());
		newClassLoadingInformationData.setSensorTypeIdent(this.classLoadingInformationData.getSensorTypeIdent());
		newClassLoadingInformationData.setCount(this.classLoadingInformationData.getCount());
		newClassLoadingInformationData.setTotalLoadedClassCount(this.classLoadingInformationData.getTotalLoadedClassCount());
		newClassLoadingInformationData.setMinLoadedClassCount(this.classLoadingInformationData.getMinLoadedClassCount());
		newClassLoadingInformationData.setMaxLoadedClassCount(this.classLoadingInformationData.getMaxLoadedClassCount());
		newClassLoadingInformationData.setTotalTotalLoadedClassCount(this.classLoadingInformationData.getTotalTotalLoadedClassCount());
		newClassLoadingInformationData.setMinTotalLoadedClassCount(this.classLoadingInformationData.getMinTotalLoadedClassCount());
		newClassLoadingInformationData.setMaxTotalLoadedClassCount(this.classLoadingInformationData.getMaxTotalLoadedClassCount());
		newClassLoadingInformationData.setTotalUnloadedClassCount(this.classLoadingInformationData.getTotalUnloadedClassCount());
		newClassLoadingInformationData.setMinUnloadedClassCount(this.classLoadingInformationData.getMinUnloadedClassCount());
		newClassLoadingInformationData.setMaxUnloadedClassCount(this.classLoadingInformationData.getMaxUnloadedClassCount());
		newClassLoadingInformationData.setTimeStamp(this.classLoadingInformationData.getTimeStamp());

		return newClassLoadingInformationData;
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		this.classLoadingInformationData.setCount(0);

		this.classLoadingInformationData.setTotalLoadedClassCount(0);
		this.classLoadingInformationData.setMinLoadedClassCount(Integer.MAX_VALUE);
		this.classLoadingInformationData.setMaxLoadedClassCount(0);

		this.classLoadingInformationData.setTotalTotalLoadedClassCount(0L);
		this.classLoadingInformationData.setMinTotalLoadedClassCount(Long.MAX_VALUE);
		this.classLoadingInformationData.setMaxTotalLoadedClassCount(0L);

		this.classLoadingInformationData.setTotalUnloadedClassCount(0L);
		this.classLoadingInformationData.setMinUnloadedClassCount(Long.MAX_VALUE);
		this.classLoadingInformationData.setMaxUnloadedClassCount(0L);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.classLoadingInformationData.setTimeStamp(timestamp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SystemSensorData getSystemSensorData() {
		return this.classLoadingInformationData;
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
