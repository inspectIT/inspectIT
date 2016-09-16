package rocks.inspectit.shared.all.communication.data.eum;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Base class for all EUM data objects. Used to identify classes for the
 * {@link rocks.inspectit.server.influx.builder.EUMPointBuilder}
 *
 * @author Jonas Kunz
 *
 */
public abstract class AbstractEUMData extends DefaultData {

	// TODO: Implement the Sizeable interface correctly.

	/**
	 * generated serial version ID.
	 */
	private static final long serialVersionUID = -8862698613017062040L;

	/**
	 * An id for the User session which is unique in combination with the platformID.
	 */
	private String sessionId;

	/**
	 * ID-based constructor.
	 *
	 * @param id
	 *            the session id
	 */
	public AbstractEUMData(String id) {
		sessionId = id;
	}

	/**
	 * Default constructor, leaving the sessionID as null.
	 *            the session id
	 */
	public AbstractEUMData() {
	}

	/**
	 * Gets {@link #sessionID}.
	 *
	 * @return {@link #sessionID}
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Sets {@link #sessionID}.
	 *
	 * @param sessionId
	 *            New value for {@link #sessionID}
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.sessionId == null) ? 0 : this.sessionId.hashCode());
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
		AbstractEUMData other = (AbstractEUMData) obj;
		if (this.sessionId == null) {
			if (other.sessionId != null) {
				return false;
			}
		} else if (!this.sessionId.equals(other.sessionId)) {
			return false;
		}
		return true;
	}

}
