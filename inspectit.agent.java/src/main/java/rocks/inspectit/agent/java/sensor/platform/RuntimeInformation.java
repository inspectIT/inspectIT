package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.PlatformSensorData;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;

/**
 * This class provides dynamic information about the runtime of the Virtual Machine through MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class RuntimeInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private RuntimeInformationData runtimeInformationData = new RuntimeInformationData();

	/** The {@link RuntimeInfoProvider} used to retrieve information from the runtime. */
	private RuntimeInfoProvider runtimeBean;

	/** {@inheritDoc} */
	public void gather() {
		/**
		 * The timestamp is set in the {@link RuntimeInformation#reset()} to avoid multiple renewal.
		 * It will not be set on the first execution of {@link RuntimeInformation#gather()}, but
		 * shortly before.
		 */
		long uptime = this.getRuntimeBean().getUptime();

		this.runtimeInformationData.incrementCount();
		this.runtimeInformationData.addUptime(uptime);

		if (uptime < this.runtimeInformationData.getMinUptime()) {
			this.runtimeInformationData.setMinUptime(uptime);
		} else if (uptime > this.runtimeInformationData.getMaxUptime()) {
			this.runtimeInformationData.setMaxUptime(uptime);
		}
	}

	/** {@inheritDoc} */
	public PlatformSensorData get() {
		RuntimeInformationData newRuntimeInformationData = new RuntimeInformationData();

		newRuntimeInformationData.setPlatformIdent(this.runtimeInformationData.getPlatformIdent());
		newRuntimeInformationData.setSensorTypeIdent(this.runtimeInformationData.getSensorTypeIdent());
		newRuntimeInformationData.setCount(this.runtimeInformationData.getCount());
		newRuntimeInformationData.setTotalUptime(this.runtimeInformationData.getTotalUptime());
		newRuntimeInformationData.setMinUptime(this.runtimeInformationData.getMinUptime());
		newRuntimeInformationData.setMaxUptime(this.runtimeInformationData.getMaxUptime());
		newRuntimeInformationData.setTimeStamp(this.runtimeInformationData.getTimeStamp());

		return this.runtimeInformationData;
	}

	/** {@inheritDoc} */
	public void reset() {
		this.runtimeInformationData.setCount(0);

		this.runtimeInformationData.setTotalUptime(0L);
		this.runtimeInformationData.setMinUptime(Long.MAX_VALUE);
		this.runtimeInformationData.setMaxUptime(0L);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.runtimeInformationData.setTimeStamp(timestamp);
	}

	/** {@inheritDoc} */
	@Override
	protected PlatformSensorData getPlatformSensorData() {
		return this.runtimeInformationData;
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
