package rocks.inspectit.shared.all.communication.data.cmr;

import java.io.Serializable;

/**
 * Class that holds agent status data.
 *
 * @author Ivan Senic
 *
 */
public class AgentStatusData implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -822865263748943014L;

	/**
	 * Enumeration that defines the connection status.
	 *
	 * @author Ivan Senic
	 *
	 */
	public enum AgentConnection {

		/**
		 * Agent was never connected.
		 */
		NEVER_CONNECTED,

		/**
		 * Agent is connected at the moment.
		 */
		CONNECTED,

		/**
		 * Agent is not sending keep-alive messages.
		 */
		NO_KEEP_ALIVE,

		/**
		 * Agent is disconnected.
		 */
		DISCONNECTED;

	}

	/**
	 * Enumeration that defines the instrumentation status.
	 *
	 * @author Marius Oehler
	 *
	 */
	public enum InstrumentationStatus {
		/**
		 * The agent has the latest instrumentation. This is default.
		 */
		UP_TO_DATE,

		/**
		 * The agent has not the latest instrumentation.
		 */
		PENDING,

		/**
		 * The class cache of the CMR is empty. Dynamic instrumentation does not work.
		 */
		NO_CLASS_CACHE_AVAILABLE;
	}

	/**
	 * {@link AgentConnection}.
	 */
	private AgentConnection agentConnection;

	/**
	 * Time in milliseconds when last data was sent.
	 */
	private long lastDataSendTimestamp;

	/**
	 * Time in milliseconds when the last keep-alive was received.
	 */
	private long lastKeepAliveTimestamp;

	/**
	 * Time in milliseconds when the agent was connecting.
	 */
	private long connectionTimestamp;

	/**
	 * Current CMR time.
	 */
	private long serverTimestamp;

	/**
	 * The time of the agent's last instrumentation update.
	 */
	private long pendingSinceTime;

	/**
	 * The current {@link InstrumentationStatus}.
	 */
	private InstrumentationStatus instrumentationStatus;

	/**
	 * No-arg constructor. Calling {@link #AgentStatusData(AgentConnection)} using
	 * {@link AgentConnection#NEVER_CONNECTED}.
	 */
	public AgentStatusData() {
		this(AgentConnection.NEVER_CONNECTED);
	}

	/**
	 * Default constructor.
	 *
	 * @param agentConnection
	 *            {@link AgentConnection}.
	 */
	public AgentStatusData(AgentConnection agentConnection) {
		this.agentConnection = agentConnection;
		this.instrumentationStatus = InstrumentationStatus.UP_TO_DATE;
	}

	/**
	 * Returns the information about how much milliseconds passed since last data sending for the
	 * agent.
	 * <p>
	 * This information can be obtained only if valid information is stored in
	 * {@link #lastDataSendTimestamp} and {@link #serverTimestamp}.
	 *
	 * @return Milliseconds or <code>null</code>.
	 */
	public Long getMillisSinceLastData() {
		if ((0 < lastDataSendTimestamp) && (lastDataSendTimestamp <= serverTimestamp)) {
			return serverTimestamp - lastDataSendTimestamp;
		} else {
			return null;
		}
	}

	/**
	 * Gets {@link #agentConnection}.
	 *
	 * @return {@link #agentConnection}
	 */
	public AgentConnection getAgentConnection() {
		return agentConnection;
	}

	/**
	 * Sets {@link #agentConnection}.
	 *
	 * @param agentConnection
	 *            New value for {@link #agentConnection}
	 */
	public void setAgentConnection(AgentConnection agentConnection) {
		this.agentConnection = agentConnection;
	}

	/**
	 * Sets {@link #lastDataSendTimestamp}.
	 *
	 * @param lastDataSendTimestamp
	 *            New value for {@link #lastDataSendTimestamp}
	 */
	public void setLastDataSendTimestamp(long lastDataSendTimestamp) {
		this.lastDataSendTimestamp = lastDataSendTimestamp;
	}

	/**
	 * Sets {@link #serverTimestamp}.
	 *
	 * @param serverTimestamp
	 *            New value for {@link #serverTimestamp}
	 */
	public void setServerTimestamp(long serverTimestamp) {
		this.serverTimestamp = serverTimestamp;
	}

	/**
	 * Gets {@link #lastKeepAliveTimestamp}.
	 *
	 * @return {@link #lastKeepAliveTimestamp}
	 */
	public long getLastKeepAliveTimestamp() {
		return lastKeepAliveTimestamp;
	}

	/**
	 * Sets {@link #lastKeepAliveTimestamp}.
	 *
	 * @param lastKeepAliveTimestamp
	 *            New value for {@link #lastKeepAliveTimestamp}
	 */
	public void setLastKeepAliveTimestamp(long lastKeepAliveTimestamp) {
		this.lastKeepAliveTimestamp = lastKeepAliveTimestamp;
	}

	/**
	 * Gets {@link #connectionTimestamp}.
	 *
	 * @return {@link #connectionTimestamp}
	 */
	public long getConnectionTimestamp() {
		return connectionTimestamp;
	}

	/**
	 * Sets {@link #connectionTimestamp}.
	 *
	 * @param connectionTimestamp
	 *            New value for {@link #connectionTimestamp}
	 */
	public void setConnectionTimestamp(long connectionTimestamp) {
		this.connectionTimestamp = connectionTimestamp;
	}

	/**
	 * Gets {@link #pendingSinceTime}.
	 *
	 * @return {@link #pendingSinceTime}
	 */
	public long getLastInstrumentationUpate() {
		return this.pendingSinceTime;
	}

	/**
	 * Sets {@link #pendingSinceTime}.
	 *
	 * @param pendingSinceTime
	 *            New value for {@link #pendingSinceTime}
	 */
	public void setPendingSinceTime(long pendingSinceTime) {
		this.pendingSinceTime = pendingSinceTime;
	}

	/**
	 * Gets {@link #instrumentationStatus}.
	 *
	 * @return {@link #instrumentationStatus}
	 */
	public InstrumentationStatus getInstrumentationStatus() {
		return this.instrumentationStatus;
	}

	/**
	 * Sets {@link #instrumentationStatus}.
	 *
	 * @param instrumentationStatus
	 *            New value for {@link #instrumentationStatus}
	 */
	public void setInstrumentationStatus(InstrumentationStatus instrumentationStatus) {
		this.instrumentationStatus = instrumentationStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.agentConnection == null) ? 0 : this.agentConnection.hashCode());
		result = (prime * result) + (int) (this.connectionTimestamp ^ (this.connectionTimestamp >>> 32));
		result = (prime * result) + ((this.instrumentationStatus == null) ? 0 : this.instrumentationStatus.hashCode());
		result = (prime * result) + (int) (this.lastDataSendTimestamp ^ (this.lastDataSendTimestamp >>> 32));
		result = (prime * result) + (int) (this.lastKeepAliveTimestamp ^ (this.lastKeepAliveTimestamp >>> 32));
		result = (prime * result) + (int) (this.pendingSinceTime ^ (this.pendingSinceTime >>> 32));
		result = (prime * result) + (int) (this.serverTimestamp ^ (this.serverTimestamp >>> 32));
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
		AgentStatusData other = (AgentStatusData) obj;
		if (this.agentConnection != other.agentConnection) {
			return false;
		}
		if (this.connectionTimestamp != other.connectionTimestamp) {
			return false;
		}
		if (this.instrumentationStatus != other.instrumentationStatus) {
			return false;
		}
		if (this.lastDataSendTimestamp != other.lastDataSendTimestamp) {
			return false;
		}
		if (this.lastKeepAliveTimestamp != other.lastKeepAliveTimestamp) {
			return false;
		}
		if (this.pendingSinceTime != other.pendingSinceTime) {
			return false;
		}
		if (this.serverTimestamp != other.serverTimestamp) {
			return false;
		}
		return true;
	}

}
