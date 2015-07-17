package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.communication.data.ParameterContentData;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating storage objects for the Timer sensor according to the definition in the
 * configuration.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class TimerStorageFactory {

	/**
	 * The singleton of this class.
	 */
	private static TimerStorageFactory singleton;

	/**
	 * Raw data transmission mode.
	 */
	public static final int RAW_DATA_TRANSMISSION = 0;

	/**
	 * Aggregate the data before sending.
	 */
	public static final int AGGREGATE_BEFORE_SEND = 1;

	/**
	 * Optimized mode.
	 */
	public static final int OPTIMIZED = 2;

	/**
	 * The default mode.
	 */
	private int mode = OPTIMIZED;

	/**
	 * Constructor is private to prevent subclasses and new instances.
	 */
	private TimerStorageFactory() {
	}

	/**
	 * Multiple instances of a factory aren't needed, so return a singleton of this class.
	 * 
	 * @return The singleton.
	 */
	public static TimerStorageFactory getFactory() {
		if (null == singleton) {
			createTimerStorageFactory();
		}
		return singleton;
	}

	/**
	 * Creates singleton in synchronized method.
	 */
	private static synchronized void createTimerStorageFactory() {
		if (null == singleton) {
			singleton = new TimerStorageFactory();
		}
	}

	/**
	 * If given {@link Map} contains a key named <b>mode</b>, it is checked against the keywords
	 * <b>raw</b>, <b>aggregate</b> and <b>optimized</b>.
	 * 
	 * @param parameters
	 *            The parameters.
	 */
	public void setParameters(final Map<String, Object> parameters) {
		String mode = (String) parameters.get("mode");

		if (null != mode) {
			if ("raw".equals(mode)) {
				setMode(RAW_DATA_TRANSMISSION);
			} else if ("aggregate".equals(mode)) {
				setMode(AGGREGATE_BEFORE_SEND);
			} else if ("optimized".equals(mode)) {
				setMode(OPTIMIZED);
			}
		}
	}

	/**
	 * Sets the mode for this factory. It can be one of the following: <br>
	 * <b>RAW_DATA_TRANSMISSION</b> <br>
	 * <b>AGGREGATE_BEFORE_SEND</b> <br>
	 * <b>OPTIMIZED</b>
	 * 
	 * @param mode
	 *            The mode to set.
	 */
	public void setMode(final int mode) {
		this.mode = mode;
	}

	/**
	 * Returns a new implementation of the {@link ITimerStorage} interface. Depends on the current
	 * mode which is set through {@link #setMode(int)}.
	 * 
	 * @param timeStamp
	 *            The time stamp.
	 * @param platformIdent
	 *            The id of the current platform.
	 * @param sensorTypeIdent
	 *            The id of the sensor type.
	 * @param methodIdent
	 *            The id of the method.
	 * @param parameterContentData
	 *            The contents of some additional parameters. Can be <code>null</code>.
	 * @param charting
	 *            If TimerData's charting should be set or not.
	 * @return A new {@link ITimerStorage} implementation object.
	 */
	public ITimerStorage newStorage(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData, boolean charting) {
		switch (mode) {
		case RAW_DATA_TRANSMISSION:
			return new PlainTimerStorage(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData, charting);
		case AGGREGATE_BEFORE_SEND:
			return new AggregateTimerStorage(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData, charting);
		case OPTIMIZED:
			return new OptimizedTimerStorage(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData, charting);
		default:
			return new OptimizedTimerStorage(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData, charting);
		}
	}

}
