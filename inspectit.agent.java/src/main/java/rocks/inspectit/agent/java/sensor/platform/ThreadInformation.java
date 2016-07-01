package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.ThreadInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.PlatformSensorData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;

/**
 * This class provides dynamic information about the thread system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class ThreadInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private ThreadInformationData threadInformationData = new ThreadInformationData();

	/** The {@link ThreadInfoProvider} used to retrieve information from the thread system. */
	private ThreadInfoProvider threadBean;

	/** {@inheritDoc} */
	public void gather() {
		/**
		 * The timestamp is set in the {@link ThreadInformation#reset()} to avoid multiple renewal.
		 * It will not be set on the first execution of {@link ThreadInformation#gather()}, but
		 * shortly before.
		 */
		int daemonThreadCount = this.getThreadBean().getDaemonThreadCount();
		int peakThreadCount = this.getThreadBean().getPeakThreadCount();
		int threadCount = this.getThreadBean().getThreadCount();
		long totalStartedThreadCount = this.getThreadBean().getTotalStartedThreadCount();

		this.threadInformationData.incrementCount();
		this.threadInformationData.addDaemonThreadCount(daemonThreadCount);
		this.threadInformationData.addPeakThreadCount(peakThreadCount);
		this.threadInformationData.addThreadCount(threadCount);
		this.threadInformationData.addTotalStartedThreadCount(totalStartedThreadCount);

		if (daemonThreadCount < this.threadInformationData.getMinDaemonThreadCount()) {
			this.threadInformationData.setMinDaemonThreadCount(daemonThreadCount);
		} else if (daemonThreadCount > this.threadInformationData.getMaxDaemonThreadCount()) {
			this.threadInformationData.setMaxDaemonThreadCount(daemonThreadCount);
		}

		if (peakThreadCount < this.threadInformationData.getMinPeakThreadCount()) {
			this.threadInformationData.setMinPeakThreadCount(peakThreadCount);
		} else if (peakThreadCount > this.threadInformationData.getMaxPeakThreadCount()) {
			this.threadInformationData.setMaxPeakThreadCount(peakThreadCount);
		}

		if (threadCount < this.threadInformationData.getMinThreadCount()) {
			this.threadInformationData.setMinThreadCount(threadCount);
		} else if (threadCount > this.threadInformationData.getMaxThreadCount()) {
			this.threadInformationData.setMaxThreadCount(threadCount);
		}

		if (totalStartedThreadCount < this.threadInformationData.getMinTotalStartedThreadCount()) {
			this.threadInformationData.setMinTotalStartedThreadCount(totalStartedThreadCount);
		} else if (totalStartedThreadCount > this.threadInformationData.getMaxTotalStartedThreadCount()) {
			this.threadInformationData.setMaxTotalStartedThreadCount(totalStartedThreadCount);
		}
	}

	/** {@inheritDoc} */
	public PlatformSensorData get() {
		ThreadInformationData newThreadInformationData = new ThreadInformationData();
		newThreadInformationData.setPlatformIdent(this.threadInformationData.getPlatformIdent());
		newThreadInformationData.setSensorTypeIdent(this.threadInformationData.getSensorTypeIdent());
		newThreadInformationData.setCount(this.threadInformationData.getCount());
		newThreadInformationData.setTotalDaemonThreadCount(this.threadInformationData.getTotalDaemonThreadCount());
		newThreadInformationData.setMinDaemonThreadCount(this.threadInformationData.getMinDaemonThreadCount());
		newThreadInformationData.setMaxDaemonThreadCount(this.threadInformationData.getMaxDaemonThreadCount());
		newThreadInformationData.setTotalPeakThreadCount(this.threadInformationData.getTotalPeakThreadCount());
		newThreadInformationData.setMinPeakThreadCount(this.threadInformationData.getMinPeakThreadCount());
		newThreadInformationData.setMaxPeakThreadCount(this.threadInformationData.getMaxPeakThreadCount());
		newThreadInformationData.setTotalThreadCount(this.threadInformationData.getTotalThreadCount());
		newThreadInformationData.setMinThreadCount(this.threadInformationData.getMinThreadCount());
		newThreadInformationData.setMaxThreadCount(this.threadInformationData.getMaxThreadCount());
		newThreadInformationData.setTotalTotalStartedThreadCount(this.threadInformationData.getTotalTotalStartedThreadCount());
		newThreadInformationData.setMinTotalStartedThreadCount(this.threadInformationData.getMinTotalStartedThreadCount());
		newThreadInformationData.setMaxTotalStartedThreadCount(this.threadInformationData.getMaxTotalStartedThreadCount());
		newThreadInformationData.setTimeStamp(this.threadInformationData.getTimeStamp());

		return newThreadInformationData;
	}

	/** {@inheritDoc} */
	public void reset() {
		this.threadInformationData.setCount(0);

		this.threadInformationData.setTotalDaemonThreadCount(0);
		this.threadInformationData.setMinDaemonThreadCount(Integer.MAX_VALUE);
		this.threadInformationData.setMaxDaemonThreadCount(0);

		this.threadInformationData.setTotalPeakThreadCount(0);
		this.threadInformationData.setMinPeakThreadCount(Integer.MAX_VALUE);
		this.threadInformationData.setMaxPeakThreadCount(0);

		this.threadInformationData.setTotalThreadCount(0);
		this.threadInformationData.setMinThreadCount(Integer.MAX_VALUE);
		this.threadInformationData.setMaxThreadCount(0);

		this.threadInformationData.setTotalTotalStartedThreadCount(0L);
		this.threadInformationData.setMinTotalStartedThreadCount(Integer.MAX_VALUE);
		this.threadInformationData.setMaxTotalStartedThreadCount(0);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.threadInformationData.setTimeStamp(timestamp);
	}

	/** {@inheritDoc} */
	@Override
	protected PlatformSensorData getPlatformSensorData() {
		return this.threadInformationData;
	}

	/**
	 * Gets the {@link ThreadInfoProvider}. The getter method is provided for better testability.
	 *
	 * @return {@link ThreadInfoProvider}.
	 */
	private ThreadInfoProvider getThreadBean() {
		if (this.threadBean == null) {
			this.threadBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getThreadInfoProvider();
		}
		return this.threadBean;
	}
}
