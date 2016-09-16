/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.communication.data.eum.ClickAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.Request;
import rocks.inspectit.shared.all.communication.data.eum.RequestType;
import rocks.inspectit.shared.all.communication.data.eum.UserAction;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Class for processing beacons which the javascript agent sends back to the agent.
 *
 * @author David Monschein
 */

@Component
public class DataHandler implements IDataHandler {

	// JSON OBJ CONFIG CONSTANTS (STRUCTURE OF THE JSON OBJ)
	/**
	 * Json Attribute name for the base url.
	 */
	private static final String JSON_BASEURL_ATTRIBUTE = "baseUrl";

	/**
	 * Json Attribute name for the session id.
	 */
	private static final String JSON_SESSIONID_ATTRIBUTE = "sessionId";

	/**
	 * Json Attribute name for the type of a beacon.
	 */
	private static final String JSON_TYPE_ATTRIBUTE = "type";

	/**
	 * Json Attribute value for session creation.
	 */
	private static final String JSON_TYPE_SESSION = "userSession";

	/**
	 * Json Attribute value for action creation.
	 */
	private static final String JSON_TYPE_ACTION = "userAction";

	/**
	 * Json Attribute name which stores the requests which belong to an action.
	 */
	private static final String JSON_ACTION_CONTENTS = "contents";

	/**
	 * Json attribute name which indicates what type of user action the beacon contains.
	 */
	private static final String JSON_ACTION_SPECTYPE = "specialType";

	/**
	 * Json Attribute value for a pageload action.
	 */
	private static final String JSON_ACTION_TYPE_PAGELOAD = "pageLoad";

	/**
	 * Json attribute value for a click action.
	 */
	private static final String JSON_ACTION_TYPE_CLICK = "click";

	/**
	 * Needed for parsing json beacon data.
	 */
	private final ObjectMapper jsonMapper = new ObjectMapper();

	/**
	 * Core service for sending eum data.
	 */
	@Autowired
	private ICoreService coreService;

	/**
	 * platform manager, required to receive the platform ID.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Logger for error printing.
	 */
	@Log
	private Logger log;

	/**
	 * Parses the incoming beacon and decides whether it is a session creation or a user action and
	 * then adds it to the session map or to the user action list.
	 *
	 * @param data
	 *            the beacon which should get parsed and processed
	 */
	@Override
	public void insertBeacon(String data) {
		if (data == null) {
			return;
		}
		// either a useraction or sessioncreation
		JsonNode jsonObj = null;
		try {
			jsonObj = jsonMapper.readTree(data);
		} catch (IOException e) {
			log.warn("Received unparseable EUM beacon!", e);
		}

		if (jsonObj != null) {
			if (jsonObj.has(JSON_TYPE_ATTRIBUTE)) {
				String type = jsonObj.get(JSON_TYPE_ATTRIBUTE).asText();
				if (type.equals(JSON_TYPE_SESSION)) {
					createAndSendSessionInfo(jsonObj);
				} else if (type.equals(JSON_TYPE_ACTION)) {
					createAndSendUserAction(jsonObj);
				}
			} else if (jsonObj.isArray()) {
				// multiple entries
				for (JsonNode action : jsonObj) {
					if (action.has(JSON_TYPE_ATTRIBUTE)) {
						String innerType = action.get(JSON_TYPE_ATTRIBUTE).asText();
						if (innerType.equals(JSON_TYPE_ACTION)) {
							createAndSendUserAction(action);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a session from a json object using jackson.
	 *
	 * @param obj
	 *            the json object representing the user session.
	 */
	private void createAndSendSessionInfo(JsonNode obj) {
		try {
			UserSessionInfo newSession = jsonMapper.readValue(obj, UserSessionInfo.class);
			newSession.setPlatformIdent(getPlatformId());
			coreService.addEUMData(newSession);
		} catch (IOException e) {
			log.warn("Received unparseable EUM beacon!", e);
		}
	}

	/**
	 * Creates a new user action from a json object which represents a user action.
	 *
	 * @param obj
	 *            json object representing a user action
	 */
	private void createAndSendUserAction(JsonNode obj) {
		if (obj.has(JSON_SESSIONID_ATTRIBUTE) && obj.has(JSON_ACTION_CONTENTS) && obj.has(JSON_ACTION_SPECTYPE) && obj.has(JSON_BASEURL_ATTRIBUTE)) {
			String specType = obj.get(JSON_ACTION_SPECTYPE).asText();
			String sessionId = obj.get(JSON_SESSIONID_ATTRIBUTE).asText();
			String baseUrl = obj.get(JSON_BASEURL_ATTRIBUTE).asText();

			if (obj.get(JSON_ACTION_CONTENTS).isArray()) {
				UserAction parsedAction = null;
				if (specType.equals(JSON_ACTION_TYPE_PAGELOAD)) {
					parsedAction = parsePageLoadAction(obj.get(JSON_ACTION_CONTENTS));
				} else if (specType.equals(JSON_ACTION_TYPE_CLICK)) {
					parsedAction = parseClickAction(obj.get(JSON_ACTION_CONTENTS));
				}

				if (parsedAction != null) {
					parsedAction.setSessionId(sessionId);
					parsedAction.setPlatformIdent(getPlatformId());
					for (Request req : parsedAction.getChildRequests()) {
						req.setSessionId(sessionId);
						req.setPlatformIdent(getPlatformId());
					}
					parsedAction.setBaseUrl(baseUrl);
					coreService.addEUMData(parsedAction);
				}
			}
		}
	}

	/**
	 * Creates a page load action from a json array which contains all belonging requests. (at least
	 * one pageloadrequest)
	 *
	 * @param contentArray
	 *            the array which contains all belonging requests as json objects
	 * @return the parsed pageloadaction
	 */
	private UserAction parsePageLoadAction(JsonNode contentArray) {
		PageLoadAction rootAction = new PageLoadAction();
		for (JsonNode req : contentArray) {
			try {
				Request childRequest = jsonMapper.readValue(req, Request.class);
				childRequest.setTimeStamp(new Timestamp(new Date().getTime()));
				if (childRequest.getRequestType() == RequestType.PAGELOAD) {
					// no instanceof :)
					rootAction.setPageLoadRequest((PageLoadRequest) childRequest);
				} else {
					rootAction.addRequest(childRequest);
				}
			} catch (IOException e) {
				log.warn("Received unparseable EUM beacon!", e);
				return null;
			}
		}
		return rootAction;
	}

	/**
	 * Creates a click action from a json array which contains all belonging requests.
	 *
	 * @param contentArray
	 *            json array which contains all belonging requests as json objects.
	 * @return the parsed click action.
	 */
	private UserAction parseClickAction(JsonNode contentArray) {
		boolean root = true;
		ClickAction rootAction = null;
		for (JsonNode reqOrAction : contentArray) {
			if (root) {
				try {
					rootAction = jsonMapper.readValue(reqOrAction, ClickAction.class);
				} catch (IOException e) {
					log.warn("Received unparseable EUM beacon!", e);
					return null;
				}
				root = false;
			} else {
				if (rootAction != null) {
					try {
						Request childRequest = jsonMapper.readValue(reqOrAction, Request.class);
						childRequest.setTimeStamp(new Timestamp(new Date().getTime()));
						rootAction.addRequest(childRequest);
					} catch (IOException e) {
						log.warn("Received unparseable EUM beacon!", e);
						return null;
					}
				}
			}
		}
		return rootAction;
	}

	/**
	 * @return the platform ID of the current agent, or 0 if it is not available
	 */
	private long getPlatformId() {
		try {
			return platformManager.getPlatformId();
		} catch (IdNotAvailableException e) {
			return 0;
		}
	}

}
