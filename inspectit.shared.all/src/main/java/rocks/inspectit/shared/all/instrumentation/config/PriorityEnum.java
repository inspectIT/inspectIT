package rocks.inspectit.shared.all.instrumentation.config;

/**
 * Enumeration used for the priority system of the sensor types.
 * 
 * @author Patrice Bouillet
 * 
 */
public enum PriorityEnum {

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
