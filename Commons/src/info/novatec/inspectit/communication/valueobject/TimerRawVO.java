package info.novatec.inspectit.communication.valueobject;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This value object is used to store the raw time measurements from the executed methods.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerRawVO extends MethodSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 6176694438175985136L;

	/**
	 * The list which is holding one or more data container for the raw data. If the current data
	 * container is full a new one is created and added to the list.
	 */
	private List<TimerRawContainer> data = new ArrayList<TimerRawContainer>();

	/**
	 * The current data container which is filled with raw data.
	 */
	private TimerRawContainer container;

	/**
	 * If TimerData's charting should be set or not.
	 */
	private boolean charting;

	/**
	 * The constructor creates the TimerRawVO object.
	 * 
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The platform ident.
	 * @param sensorTypeIdent
	 *            The sensor type ident.
	 * @param methodIdent
	 *            The method ident.
	 * @param parameterContentData
	 *            The parameter content data. Can be <code>null</code>.
	 * @param charting
	 *            If TimerData's charting should be set or not.
	 */
	public TimerRawVO(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData, boolean charting) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);

		container = new TimerRawContainer();
		this.charting = charting;
		data.add(container);
	}

	/**
	 * Adds a new time value to the current data container.
	 * 
	 * @param time
	 *            The time value.
	 */
	public void add(double time) {
		createContainerIfNecessary();
		container.add(time);
	}

	/**
	 * Adds a new time and cpu time value to the current data container.
	 * 
	 * @param time
	 *            The time value.
	 * @param cpuTime
	 *            The cpu time value.
	 */
	public void add(double time, double cpuTime) {
		createContainerIfNecessary();
		container.add(time, cpuTime);
	}

	/**
	 * Creates a new container if it is necessary.
	 */
	private void createContainerIfNecessary() {
		if (container.isFull()) {
			container = new TimerRawContainer();
			data.add(container);
		}
	}

	/**
	 * Returns the list with {@link TimerRawContainer} objects.
	 * 
	 * @return The list with {@link TimerRawContainer} objects.
	 */
	public List<TimerRawContainer> getData() {
		return Collections.unmodifiableList(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData finalizeData() {
		TimerData timerData = new TimerData(getTimeStamp(), getPlatformIdent(), getSensorTypeIdent(), getMethodIdent());
		timerData.setParameterContentData(getParameterContentData());
		timerData.setCharting(charting);

		double[] values;
		double value;

		double min = Double.MAX_VALUE;
		double max = 0.0d;
		int count = 0;
		double duration = 0.0d;

		double cpuMin = Double.MAX_VALUE;
		double cpuMax = 0.0d;
		double cpuDuration = 0.0d;

		for (TimerRawContainer container : data) {
			values = container.getData();
			for (int j = 0; j < container.getCount(); j++) {
				value = values[j];
				duration += value;
				if (value < min) {
					min = value;
				}
				if (value > max) {
					max = value;
				}
			}
			count += container.getCount();

			values = container.getCpuData();
			if (null != values) {
				for (int j = 0; j < container.getCount(); j++) {
					value = values[j];
					cpuDuration += value;
					if (value < cpuMin) {
						cpuMin = value;
					}
					if (value > cpuMax) {
						cpuMax = value;
					}
				}
			}
		}

		timerData.calculateMin(min);
		timerData.calculateMax(max);
		timerData.setCount(count);
		timerData.setDuration(duration);
		// TODO compute the variance
		timerData.setVariance(-1);

		if (Double.MAX_VALUE != cpuMin) {
			timerData.calculateCpuMin(cpuMin);
			timerData.calculateCpuMax(cpuMax);
			timerData.setCpuDuration(cpuDuration);
		}

		return timerData;
	}

	/**
	 * Container to store the raw data.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public static class TimerRawContainer implements Serializable {

		/**
		 * The serial version uid for this class.
		 */
		private static final long serialVersionUID = 7225384620786119269L;

		/**
		 * The max amount this container is saving until it is full and no data can be accepted
		 * anymore.
		 */
		private static final int MAX_SIZE = 50;

		/**
		 * Initializes a new data array with the specified MAX_SIZE.
		 */
		private double[] data = new double[MAX_SIZE];

		/**
		 * Initializes a new cpu data array with the specified MAX_SIZE.
		 */
		private double[] cpuData = new double[MAX_SIZE];

		/**
		 * The current position/count.
		 */
		private int count = 0;

		/**
		 * Adds a new time value to the current data container.
		 * 
		 * @param time
		 *            The time value.
		 */
		public void add(double time) {
			data[count] = time;
			count++;

			if (null != cpuData) {
				cpuData = null; // NOPMD
			}
		}

		/**
		 * Adds the given time and cpu time to this timer.
		 * 
		 * @param time
		 *            the elapsed time.
		 * @param cpuTime
		 *            the cpu time.
		 */
		public void add(double time, double cpuTime) {
			data[count] = time;
			cpuData[count] = cpuTime;
			count++;
		}

		/**
		 * Returns if this container is full.
		 * 
		 * @return If this container is full.
		 */
		public boolean isFull() {
			return count == MAX_SIZE - 1;
		}

		/**
		 * Returns the current count.
		 * 
		 * @return The current count.
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Return the double data array containing the measurements.
		 * 
		 * @return The double data array.
		 */
		public double[] getData() {
			return data;
		}

		/**
		 * Return the double cpu data array containing the measurements.
		 * 
		 * @return The double cpu data array.
		 */
		public double[] getCpuData() {
			return cpuData;
		}

		/**
		 * Returns a hash code value for the array.
		 * 
		 * @param array
		 *            the array to create a hash code value for
		 * @return a hash code value for the array
		 */
		private static int hashCode(double[] array) {
			int prime = 31;
			if (array == null) {
				return 0;
			}
			int result = 1;
			for (int index = 0; index < array.length; index++) {
				long temp = Double.doubleToLongBits(array[index]);
				result = prime * result + (int) (temp ^ (temp >>> 32));
			}
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + count;
			result = prime * result + TimerRawContainer.hashCode(cpuData);
			result = prime * result + TimerRawContainer.hashCode(data);
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TimerRawContainer other = (TimerRawContainer) obj;
			if (count != other.count) {
				return false;
			}
			if (!Arrays.equals(cpuData, other.cpuData)) {
				return false;
			}
			if (!Arrays.equals(data, other.data)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TimerRawVO other = (TimerRawVO) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
