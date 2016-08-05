package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.SystemSensorData;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void gather() {
		// The timestamp is set in the {@link CpuInformation#reset()} to avoid multiple renewal. It
		// will not be set on the first execution of {@link CpuInformation#gather()}, but shortly
		// before.
		float cpuUsage = this.getOsBean().retrieveCpuUsage();
		long cpuTime = this.getOsBean().getProcessCpuTime();

		this.cpuInformationData.incrementCount();
		this.cpuInformationData.updateProcessCpuTime(cpuTime);
		this.cpuInformationData.addCpuUsage(cpuUsage);

		if (cpuUsage < this.cpuInformationData.getMinCpuUsage()) {
			this.cpuInformationData.setMinCpuUsage(cpuUsage);
		}
		if (cpuUsage > this.cpuInformationData.getMaxCpuUsage()) {
			this.cpuInformationData.setMaxCpuUsage(cpuUsage);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SystemSensorData get() {
		CpuInformationData newCpuInformationData = new CpuInformationData();

		newCpuInformationData.setPlatformIdent(this.cpuInformationData.getPlatformIdent());
		newCpuInformationData.setSensorTypeIdent(this.cpuInformationData.getSensorTypeIdent());
		newCpuInformationData.setCount(this.cpuInformationData.getCount());

		newCpuInformationData.setProcessCpuTime(this.cpuInformationData.getProcessCpuTime());

		newCpuInformationData.setTotalCpuUsage(this.cpuInformationData.getTotalCpuUsage());
		newCpuInformationData.setMinCpuUsage(this.cpuInformationData.getMinCpuUsage());
		newCpuInformationData.setMaxCpuUsage(this.cpuInformationData.getMaxCpuUsage());

		newCpuInformationData.setTimeStamp(this.cpuInformationData.getTimeStamp());

		return newCpuInformationData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		this.cpuInformationData.setCount(0);

		this.cpuInformationData.setProcessCpuTime(0L);

		this.cpuInformationData.setTotalCpuUsage(0f);
		this.cpuInformationData.setMinCpuUsage(Float.MAX_VALUE);
		this.cpuInformationData.setMaxCpuUsage(0f);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.cpuInformationData.setTimeStamp(timestamp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SystemSensorData getSystemSensorData() {
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
