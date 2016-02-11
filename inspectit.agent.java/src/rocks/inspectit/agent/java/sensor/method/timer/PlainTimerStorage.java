package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.valueobject.TimerRawVO;

import java.sql.Timestamp;
import java.util.List;

/**
 * Class which stores the data as they arrive without further processing. This will increase memory
 * usage by a high amount but should reduces CPU usage.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlainTimerStorage implements ITimerStorage {

	/**
	 * The raw value object.
	 */
	private TimerRawVO timerRawVO;

	/**
	 * Default constructor which initializes a {@link TimerRawVO} object.
	 * 
	 * @param timeStamp
	 *            The time stamp.
	 * @param platformIdent
	 *            The platform ID.
	 * @param sensorTypeIdent
	 *            The sensor type ID.
	 * @param methodIdent
	 *            The method ID.
	 * @param parameterContentData
	 *            The content of the parameter/fields.
	 * @param charting
	 *            If TimerData's charting should be set or not.
	 */
	public PlainTimerStorage(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData, boolean charting) {
		timerRawVO = new TimerRawVO(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData, charting);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addData(double time, double cpuTime) {
		if (cpuTime < 0) {
			timerRawVO.add(time);
		} else {
			timerRawVO.add(time, cpuTime);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData finalizeDataObject() {
		return timerRawVO;
	}

}
