package rocks.inspectit.shared.cs.storage.recording;

/**
 * Enum that describes three state recording can be in.
 * 
 * @author Ivan Senic
 */
public enum RecordingState {

	/**
	 * Recording is scheduled.
	 */
	SCHEDULED,

	/**
	 * Recording is active.
	 */
	ON,

	/**
	 * Recording is not active.
	 */
	OFF;
}