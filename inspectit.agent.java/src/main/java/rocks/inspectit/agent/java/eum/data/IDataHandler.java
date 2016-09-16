package rocks.inspectit.agent.java.eum.data;

/**
 * @author Jonas Kunz
 *
 */
public interface IDataHandler {
	/**
	 * Parses the incoming beacon and decides whether it is a session creation or a user action and
	 * then adds it to the session map or to the user action list.
	 *
	 * @param data
	 *            the beacon which should get parsed and processed
	 */
	void insertBeacon(String data);
}
