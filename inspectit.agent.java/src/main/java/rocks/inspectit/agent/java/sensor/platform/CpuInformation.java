package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.PlatformSensorData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;

/**
 * This class provides dynamic information about the underlying operating system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class CpuInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private CpuInformationData cpuInformationData = new CpuInformationData();

	/**
	 * The {@link OperatingSystemInfoProvider} used to retrieve information from the operating
	 * system.
	 */
	private OperatingSystemInfoProvider osBean;

	/** {@inheritDoc} */
	public void gather() {
		/**
		 * The timestamp is set in the {@link CpuInformation#reset()} to avoid multiple renewal. It
		 * will not be set on the first execution of {@link CpuInformation#gather()}, but shortly
		 * before.
		 */
		float cpuUsage = this.getOsBean().retrieveCpuUsage();

		this.cpuInformationData.incrementCount();
		this.cpuInformationData.updateProcessCpuTime(this.getOsBean().getProcessCpuTime());
		this.cpuInformationData.addCpuUsage(cpuUsage);

		if (cpuUsage < this.cpuInformationData.getMinCpuUsage()) {
			this.cpuInformationData.setMinCpuUsage(cpuUsage);
		} else if (cpuUsage > this.cpuInformationData.getMaxCpuUsage()) {
			this.cpuInformationData.setMaxCpuUsage(cpuUsage);
		}
	}

	/** {@inheritDoc} */
	public PlatformSensorData get() {
		CpuInformationData newCpuInformationData = new CpuInformationData();

		newCpuInformationData.setPlatformIdent(this.cpuInformationData.getPlatformIdent());
		newCpuInformationData.setSensorTypeIdent(this.cpuInformationData.getSensorTypeIdent());
		newCpuInformationData.setCount(this.cpuInformationData.getCount());
		newCpuInformationData.setProcessCpuTime(this.cpuInformationData.getProcessCpuTime());
		newCpuInformationData.setMinCpuUsage(this.cpuInformationData.getMinCpuUsage());
		newCpuInformationData.setMaxCpuUsage(this.cpuInformationData.getMaxCpuUsage());
		newCpuInformationData.setTimeStamp(this.cpuInformationData.getTimeStamp());

		return newCpuInformationData;
	}

	/** {@inheritDoc} */
	public void reset() {
		this.cpuInformationData.setCount(0);
		this.cpuInformationData.setProcessCpuTime(0L);
		this.cpuInformationData.setMinCpuUsage(Float.MAX_VALUE);
		this.cpuInformationData.setMaxCpuUsage(0.0f);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.cpuInformationData.setTimeStamp(timestamp);
	}

	/** {@inheritDoc} */
	@Override
	protected PlatformSensorData getPlatformSensorData() {
		return this.cpuInformationData;
	}

	/**
	 * Gets the {@link OperatingSystemInfoProvider}. The getter method is provided for better
	 * testability.
	 *
	 * @return {@link OperatingSystemInfoProvider}.
	 */
	private OperatingSystemInfoProvider getOsBean() {
		if (this.osBean == null) {
			this.osBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getOperatingSystemInfoProvider();
		}
		return this.osBean;
	}
}
