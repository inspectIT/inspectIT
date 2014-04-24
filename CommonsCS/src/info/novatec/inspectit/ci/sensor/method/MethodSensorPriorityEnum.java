package info.novatec.inspectit.ci.sensor.method;

/**
 * Enumeration used for the priority system of the sensor types.
 * 
 * @author Ivan Senic
 * 
 */
// TODO remove this class when PriorityEnum is moved to Commons (server-side instrumentation ticket
// INSPECTIT-1919)
public enum MethodSensorPriorityEnum {

	/** The priority used by the invocation tracing system. */
	INVOC,

	/** Minimum priority. */
	MIN,

	/** Low priority. */
	LOW,

	/** Normal priority. */
	NORMAL,

	/** High priority. */
	HIGH,

	/** Maximum priority. */
	MAX;

}
