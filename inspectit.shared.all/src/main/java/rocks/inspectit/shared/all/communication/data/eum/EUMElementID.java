package rocks.inspectit.shared.all.communication.data.eum;

import java.io.Serializable;

/**
 * ID object used for globally identifying EUM elements uniquely. The ID consists of three
 * sub-parts:
 * <ul>
 * <li>The session ID, identifying a user session.</li>
 * <li>The tab ID, uniquely identifying a tab (or more precisely, a javascript interpreter instance)
 * within a session.</li>
 * <li>The local ID, uniquely identifying a element within a tab.</li>
 * </ul>
 *
 * @author Jonas Kunz
 *
 */
public class EUMElementID implements Serializable {

	/**
	 * Serialization UID.
	 */
	private static final long serialVersionUID = -7396170537157406276L;

	/**
	 * The session ID, uniquely identifying the user session..
	 */
	private long sessionID;

	/**
	 * The tab ID, uniquely identifying the tab (or more precisely, the javascript interpreter
	 * instance) within the session. Can be zero if the element is tab independent, e.g.
	 * {@link UserSessionInfo}.
	 */
	private long tabID;

	/**
	 * The local ID, uniquely identifying an individual element within a tab. Can be zero if the
	 * element is not required to be referenceable, e.g. a {@link UserSessionInfo}.
	 */
	private long localID;

	/**
	 * Default Constructor, zeroes all IDs.
	 *
	 */
	public EUMElementID() {
		this.sessionID = 0;
		this.tabID = 0;
		this.localID = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.localID ^ (this.localID >>> 32));
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
		EUMElementID other = (EUMElementID) obj;
		if (this.localID != other.localID) {
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

	/**
	 * Gets {@link #sessionID}.
	 *
	 * @return {@link #sessionID}
	 */
	public long getSessionID() {
		return this.sessionID;
	}

	/**
	 * Sets {@link #sessionID}.
	 *
	 * @param sessionID
	 *            New value for {@link #sessionID}
	 */
	public void setSessionID(long sessionID) {
		this.sessionID = sessionID;
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
	 * Sets {@link #tabID}.
	 *
	 * @param tabID
	 *            New value for {@link #tabID}
	 */
	public void setTabID(long tabID) {
		this.tabID = tabID;
	}

	/**
	 * Gets {@link #elementID}.
	 *
	 * @return {@link #elementID}
	 */
	public long getLocalID() {
		return this.localID;
	}

	/**
	 * Sets {@link #elementID}.
	 *
	 * @param localID
	 *            New value for {@link #elementID}
	 */
	public void setLocalID(long localID) {
		this.localID = localID;
	}

}
