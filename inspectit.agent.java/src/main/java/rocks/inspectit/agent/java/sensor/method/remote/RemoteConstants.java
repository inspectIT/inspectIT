package rocks.inspectit.agent.java.sensor.method.remote;

/**
 * Interface to contain constant value for webrequest.
 *
 * @author Thomas Kluge
 *
 */
public interface RemoteConstants {

	/**
	 * Name of the InspectIT http Header.
	 */
	String INSPECTIT_HTTP_HEADER = "inspectITHeader";

	/**
	 * Char to split inspectIt header into subparts. Each subpart has a different value with
	 * different informations.
	 */
	char CHAR_SPLIT_INSPECTIT_HEADER = ';';

	/**
	 * Number of subparts of the inspectIt header. After Split the inspectIt header by
	 * {@link #CHAR_SPLIT_INSPECTIT_HEADER} the resulting array contains this amount of elements.
	 */
	int INSPECTIT_HEADER_ELEMENT_COUNT = 2;

}
