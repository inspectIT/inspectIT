package rocks.inspectit.server.diagnosis.service.data;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

/**
 * Helper class to for common methods in Diagnosis project.
 *
 * @author Isabel Vico Peinado
 *
 */
public final class DiagnosisHelper {

	/**
	 * Private constructor for the helper class.
	 */
	private DiagnosisHelper() {
	}

	/**
	 * Gets the exclusive duration of the invocation sequence data element, in case the element has
	 * not timer data or has not the exclusive time available it will returns 0.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return the exclusive duration of this invocation sequence data element or 0.
	 */
	public static double getExclusiveDuration(InvocationSequenceData data) {
		TimerData timerData;
		if (InvocationSequenceDataHelper.hasSQLData(data)) {
			timerData = data.getSqlStatementData();
		} else if (InvocationSequenceDataHelper.hasTimerData(data)) {
			timerData = data.getTimerData();
		} else {
			return 0.0;
		}

		if (timerData.isExclusiveTimeDataAvailable()) {
			return timerData.getExclusiveDuration();
		} else {
			return 0.0;
		}
	}

}
