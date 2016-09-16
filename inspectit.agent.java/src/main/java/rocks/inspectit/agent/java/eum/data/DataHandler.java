/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.shared.all.communication.data.EUMData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.ClickAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.Request;
import rocks.inspectit.shared.all.communication.data.eum.RequestType;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserAction;
import rocks.inspectit.shared.all.communication.data.eum.UserSession;

/**
 * Class for processing beacons which the javascript agent sends back to the agent.
 *
 * @author David Monschein
 */

// TODO if data not in our format don't accept it
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
	// __________________________ //

	/**
	 * Maps the sessionIds to the UserSession objects.
	 */
	private final Map<String, UserSession> sessionMap;

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
	 * Creates a new instance which handles sessions and user actions.
	 *
	 */
	public DataHandler() {
		this.sessionMap = new HashMap<String, UserSession>();
	}

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
			return;
		}

		if (jsonObj != null) {
			if (jsonObj.has(JSON_TYPE_ATTRIBUTE)) {
				String type = jsonObj.get(JSON_TYPE_ATTRIBUTE).asText();
				if (type.equals(JSON_TYPE_SESSION)) {
					createSession(jsonObj);
				} else if (type.equals(JSON_TYPE_ACTION)) {
					createUserAction(jsonObj);
				}
			} else if (jsonObj.isArray()) {
				// multiple entries
				for (JsonNode action : jsonObj) {
					if (action.has(JSON_TYPE_ATTRIBUTE)) {
						String innerType = action.get(JSON_TYPE_ATTRIBUTE).asText();
						if (innerType.equals(JSON_TYPE_ACTION)) {
							createUserAction(action);
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
	private void createSession(JsonNode obj) {
		try {
			UserSession newSession = jsonMapper.readValue(obj, UserSession.class);
			if (!sessionMap.containsKey(newSession.getSessionId())) {
				sessionMap.put(newSession.getSessionId(), newSession);
			}
		} catch (JsonParseException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Creates a new user action from a json object which represents a user action.
	 *
	 * @param obj
	 *            json object representing a user action
	 */
	private void createUserAction(JsonNode obj) {
		if (obj.has(JSON_SESSIONID_ATTRIBUTE) && obj.has(JSON_ACTION_CONTENTS) && obj.has(JSON_ACTION_SPECTYPE) && obj.has(JSON_BASEURL_ATTRIBUTE)) {
			String specType = obj.get(JSON_ACTION_SPECTYPE).asText();
			String sessionId = obj.get(JSON_SESSIONID_ATTRIBUTE).asText();
			String baseUrl = obj.get(JSON_BASEURL_ATTRIBUTE).asText();

			if (!sessionMap.containsKey(sessionId)) {
				createEmptySession(sessionId);
			}

			if (obj.get(JSON_ACTION_CONTENTS).isArray() && sessionMap.containsKey(sessionId)) {
				UserSession userSession = sessionMap.get(sessionId);
				UserAction parsedAction = null;
				if (specType.equals(JSON_ACTION_TYPE_PAGELOAD)) {
					parsedAction = parsePageLoadAction(obj.get(JSON_ACTION_CONTENTS));
				} else if (specType.equals(JSON_ACTION_TYPE_CLICK)) {
					parsedAction = parseClickAction(obj.get(JSON_ACTION_CONTENTS));
				}

				if (parsedAction != null) {
					parsedAction.setUserSession(userSession);
					parsedAction.setBaseUrl(baseUrl);
					sendAction(parsedAction);
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
				if (childRequest.getRequestType() == RequestType.PAGELOAD) {
					// no instanceof :)
					rootAction.setPageLoadRequest((PageLoadRequest) childRequest);
				} else {
					rootAction.addRequest(childRequest);
				}
			} catch (IOException e) {
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
					return null;
				}
				root = false;
			} else {
				if (rootAction != null) {
					try {
						Request childRequest = jsonMapper.readValue(reqOrAction, Request.class);
						rootAction.addRequest(childRequest);
					} catch (IOException e) {
						return null;
					}
				}
			}
		}
		return rootAction;
	}

	/**
	 * Creates a session without browser informations. Needed if we want to assign data to a session
	 * but there is none on server side otherwise we would lose data.
	 *
	 * @param id
	 *            the id of the session which should get created
	 */
	private void createEmptySession(String id) {
		UserSession r = new UserSession();
		r.setSessionId(id);
		r.setBrowser("unknown");
		r.setDevice("unknown");
		r.setLanguage("unknown");
		sessionMap.put(id, r);
	}

	/**
	 * Generates an EUM data object and sends it to the cmr.
	 *
	 * @param action
	 *            the action which should be transfered to the cmr.
	 */
	private void sendAction(UserAction action) {
		// transform to EUM data and add
		EUMData data = new EUMData();
		data.setBaseUrl(action.getBaseUrl());
		data.setUserSession(action.getUserSession());

		for (Request req : action.getChildRequests()) {
			if (req != null) {
				switch (req.getRequestType()) {
				case AJAX:
					data.addAjaxRequest((AjaxRequest) req);
					break;
				case PAGELOAD:
					data.addPageLoadRequest((PageLoadRequest) req);
					break;
				case RESOURCELOAD:
					data.addResourceLoadRequest((ResourceLoadRequest) req);
					break;
				default:
					break;
				}
			}
		}

		this.coreService.addEumData(data);
	}

}
