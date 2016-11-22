package rocks.inspectit.shared.all.communication.data.eum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Definition of the format of beacons sent by the EUM JS Agent.
 *
 * The beacon header stores the sessionID and tabID, which is not stored for each element explictly.
 * Therefore, after receiving a beacon, these IDs have to be assigned based on the beacon header to
 * the elements contained within the beacon.
 *
 * The JS Agent can signalize the server that it does not have an ID yet by setting the value of the
 * corresponding ID to {@link Beacon#REQUEST_NEW_TAB_ID_MARKER} or
 * {@link Beacon#REQUEST_NEW_SESSION_ID_MARKER}. The server is then responsible for assigning a new
 * ID and sending it as a response to the beacon.
 *
 * E.g. when the JS Agent sends a beacon with the tab ID of "-1", the server generates a new tab ID
 * which is assigned to all data sent with the beacon and responds with "{ tabID : [newID] }". This
 * id must then be reused by the JS agent for the other beacons.
 *
 * @author Jonas Kunz
 *
 */
public class Beacon {

	/**
	 * Special ID for marking that the JS Agent requires a new session ID.
	 */
	public static final long REQUEST_NEW_SESSION_ID_MARKER = -1;

	/**
	 * Special ID for marking that the JS Agent requires a new tab ID.
	 */
	public static final long REQUEST_NEW_TAB_ID_MARKER = -1;

	/**
	 * The sessionID of the JS Agent which sent this beacon. It is serialized as a string as JS only
	 * supports double values, which do not have enough precision for 64 bit integers.
	 */
	@JsonDeserialize(using = HexLongDeserializer.class)
	@JsonInclude(value = Include.NON_DEFAULT)
	@JsonProperty
	private long sessionID;

	/**
	 * The tabID of the JS Agent which sent this beacon. It is serialized as a string as JS only
	 * supports double values, which do not have enough precision for 64 bit integers.
	 * A browser tab is identified by having a running JavaScript interpreter.
	 */
	@JsonDeserialize(using = HexLongDeserializer.class)
	@JsonInclude(value = Include.NON_DEFAULT)
	@JsonProperty
	private long tabID;

	/**
	 * The module string containing all active modules for the agent from which this beacon came
	 * from.
	 */
	@JsonProperty
	private String activeAgentModules;

	/**
	 * The contents of this beacon.
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonProperty
	private List<EUMBeaconElement> data;

	/**
	 * Default constructor.
	 */
	public Beacon() {
		sessionID = REQUEST_NEW_SESSION_ID_MARKER;
		tabID = REQUEST_NEW_TAB_ID_MARKER;
		data = new ArrayList<EUMBeaconElement>();
	}

	/**
	 * Constructor primarly used for JUnit tests.
	 *
	 * @param sessionID
	 *            the sessionID
	 * @param tabID
	 *            the tabID
	 * @param activeAgentModules
	 *            the active modules
	 * @param data
	 *            the data
	 */
	public Beacon(long sessionID, long tabID, String activeAgentModules, List<EUMBeaconElement> data) {
		super();
		this.sessionID = sessionID;
		this.tabID = tabID;
		this.activeAgentModules = activeAgentModules;
		this.data = data;
	}

	/**
	 * Gets {@link #sessionID}.
	 *
	 * @return {@link #sessionID}
	 */
	public long getSessionID() {
		return this.sessionID;
	}

	/**
	 * Gets {@link #tabID}.
	 *
	 * @return {@link #tabID}
	 */
	public long getTabID() {
		return this.tabID;
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public List<EUMBeaconElement> getData() {
		return this.data;
	}

	/**
	 * Gets {@link #activeAgentModules}.
	 *
	 * @return {@link #activeAgentModules}
	 */
	public String getActiveAgentModules() {
		return this.activeAgentModules;
	}

	/**
	 * Finalises the deserialization of all elements and optionally assigns a new session or tab ID.
	 *
	 * @param sessionID
	 *            the sessionID to assign
	 * @param tabID
	 *            the tabID to assign
	 */
	public void deserializationComplete(long sessionID, long tabID) {
		this.sessionID = sessionID;
		this.tabID = tabID;
		for (EUMBeaconElement element : data) {
			element.deserializationComplete(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.activeAgentModules == null) ? 0 : this.activeAgentModules.hashCode());
		result = (prime * result) + ((this.data == null) ? 0 : this.data.hashCode());
		result = (prime * result) + (int) (this.sessionID ^ (this.sessionID >>> 32));
		result = (prime * result) + (int) (this.tabID ^ (this.tabID >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Beacon other = (Beacon) obj;
		if (this.activeAgentModules == null) {
			if (other.activeAgentModules != null) {
				return false;
			}
		} else if (!this.activeAgentModules.equals(other.activeAgentModules)) {
			return false;
		}
		if (this.data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!this.data.equals(other.data)) {
			return false;
		}
		if (this.sessionID != other.sessionID) {
			return false;
		}
		if (this.tabID != other.tabID) {
			return false;
		}
		return true;
	}

}

/**
 * Deserializer for deserializing long values encoded as Hex String.
 *
 * @author Jonas Kunz
 *
 */
class HexLongDeserializer extends StdDeserializer<Long> {

	/**
	 * serial version UID.
	 */
	private static final long serialVersionUID = 2005182201383653484L;

	/**
	 * Default Constructor.
	 */
	HexLongDeserializer() {
		super(Long.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String content = p.readValueAs(String.class);
		try {
			return Long.parseLong(content, 16);
		} catch (NumberFormatException e) {
			throw new JsonParseException(p, e.getMessage(), e); // NOPMD
		}
	}
}
