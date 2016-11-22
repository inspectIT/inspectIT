package rocks.inspectit.shared.all.communication.data.eum;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;

import rocks.inspectit.shared.all.communication.DefaultData;

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
public class Beacon extends DefaultData {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -1184284333074134927L;

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
	 * supports double values, which do not have enough precision for 64 bit itnegers.
	 */
	@JsonSerialize(include = Inclusion.NON_DEFAULT, using = ToStringSerializer.class)
	@JsonProperty
	private long sessionID;

	/**
	 * The tabID of the JS Agent which sent this beacon.. It is serialized as a string as JS only
	 * supports double values, which do not have enough precision for 64 bit itnegers.
	 */
	@JsonSerialize(include = Inclusion.NON_DEFAULT, using = ToStringSerializer.class)
	@JsonProperty
	private long tabID;


	/**
	 * The module string containing al lactive modules for the agent from which this beacon came
	 * from.
	 */
	@JsonProperty
	private String activeAgentModules;

	/**
	 * The contents of this beacon.
	 */
	@JsonSerialize(include = Inclusion.NON_EMPTY)
	@JsonProperty
	private List<AbstractEUMElement> data;

	/**
	 * Default constructor.
	 */
	public Beacon() {
		sessionID = REQUEST_NEW_SESSION_ID_MARKER;
		tabID = REQUEST_NEW_TAB_ID_MARKER;
		data = new ArrayList<AbstractEUMElement>();
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
	public Beacon(long sessionID, long tabID, String activeAgentModules, List<AbstractEUMElement> data) {
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
	public List<AbstractEUMElement> getData() {
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
	 * Assigns an ID to this beacon and all contained elements.
	 *
	 * @param sessionID
	 *            the sessionID to assign
	 * @param tabID
	 *            the tabID to assign
	 */
	public void assignIDs(long sessionID, long tabID) {
		this.sessionID = sessionID;
		this.tabID = tabID;
		for (AbstractEUMElement element : data) {
			element.setSessionID(sessionID);
			element.setTabID(tabID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj)) {
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
		if (this.tabID != other.tabID) { // NOPMD
			return false;
		}
		return true;
	}

}
