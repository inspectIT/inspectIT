/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.shared.all.communication.data.eum.Beacon;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Class for processing beacons which the javascript agent sends back to the agent.
 *
 * @author David Monschein, Jonas kunz
 */

@Component
public class DataHandler implements IDataHandler {

	/**
	 * Logger.
	 */
	@Log
	private Logger log;

	/**
	 * Core service to which received data is passed.
	 */
	@Autowired
	private ICoreService coreService;

	/**
	 * The generator used to generate unique EUM ids.
	 */
	@Autowired
	private IdGenerator idGenerator;

	/**
	 * The mapper used for decoding JSON beacons.
	 */
	private ObjectMapper jsonMapper;

	/**
	 * Constructor.
	 */
	public DataHandler() {
		jsonMapper = new ObjectMapper();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String insertBeacon(String data) {
		if (data == null) {
			return "{}";
		}

		try {
			Beacon beacon = jsonMapper.readValue(data, Beacon.class);
			ObjectNode response = jsonMapper.getNodeFactory().objectNode();

			long sessionID = beacon.getSessionID();
			long tabID = beacon.getTabID();

			// assign new IDs if requested
			if (sessionID == Beacon.REQUEST_NEW_SESSION_ID_MARKER) {
				sessionID = idGenerator.generateSessionID();
				response.put("sessionID", String.valueOf(sessionID));
			}
			if (beacon.getTabID() == Beacon.REQUEST_NEW_TAB_ID_MARKER) {
				tabID = idGenerator.generateTabID();
				response.put("tabID", String.valueOf(tabID));
			}

			// even needed if the IDs were all known, as this also assigns the ids to all stored
			// AbstractEUMElements.
			beacon.assignIDs(sessionID, tabID);

			// send the received elements to the CMR
			if (!beacon.getData().isEmpty()) {
				coreService.addEUMData(beacon);
			}

			return jsonMapper.writeValueAsString(response);

		} catch (Exception e) {
			log.error("Error decoding beacon!", e);
			return "{}";
		}
	}


}
