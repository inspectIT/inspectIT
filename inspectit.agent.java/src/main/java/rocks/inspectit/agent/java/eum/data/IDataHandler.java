package rocks.inspectit.agent.java.eum.data;

/**
 * @author Jonas Kunz
 *
 */
public interface IDataHandler {
	/**
	 * Parses the incoming beacon. If the beacon requests for a new session and or tab ID, these IDs
	 * are assigned and packed into the response.
	 *
	 * @param data
	 *            the beacon which should get parsed and processed
	 * @return the response string to send back to the client.
	 *
	 */
	String insertBeacon(String data);
}
