package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.List;

/**
 * @author Jonas Kunz
 *
 */
public class AgentEndUserMonitoringConfig {

	/**
	 * switch to disable EUM entirely.
	 */
	private boolean enabled;

	/**
	 * The base url under which we will locate the JS Agent script and we will receive the beacons.
	 * Must begin and end with a slash.
	 */
	private String scriptBaseUrl;

	/**
	 * A string containing the identifiers of all enabled JS agent modules, see
	 * {@link JSAgentModule}.
	 */
	private String activeModules;

	/**
	 * Defines a Threshold in milliseconds to make the traces generated at the browser more lean.
	 * Captured JS Function execution are only sent if (a) their execution duration is greather than
	 * or equa lto his threshold or (b) they triggered a request. This early filtering of unrelevant
	 * reduces the load on the client aswell as on the CMR.
	 */
	private int relevancyThreshold;

	/**
	 * Flag whether to allow the JS Agent to instrument listeners. Listener instrumentation can have
	 * a performance impact for sites which have a high rate of listener executions. This flag
	 * allows to globally disable the instrumentation of listeners while other data can still be
	 * captured. For example, this also includes listeners to AJAX states.
	 */
	private boolean listenerInstrumentationAllowed;

	/**
	 * Enables or disables the minification of the JS agent.
	 */
	private boolean agentMinificationEnabled;

	/**
	 * If enabled, users sending a Do-Not-Track Header will not be monitored.
	 */
	private boolean respectDNTHeader;

	/**
	 * Holds all active {@link AgentEumDomEventSelector}s.
	 */
	private List<AgentEumDomEventSelector> eventSelectors;

	/**
	 * Gets {@link #isEnabled}.
	 *
	 * @return {@link #isEnabled}
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets {@link #isEnabled}.
	 *
	 * @param isEnabled
	 *            New value for {@link #isEnabled}
	 */
	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}

	/**
	 * Gets {@link #scriptBaseUrl}.
	 *
	 * @return {@link #scriptBaseUrl}
	 */
	public String getScriptBaseUrl() {
		return scriptBaseUrl;
	}

	/**
	 * Sets {@link #scriptBaseUrl}.
	 *
	 * @param scriptBaseUrl
	 *            New value for {@link #scriptBaseUrl}
	 */
	public void setScriptBaseUrl(String scriptBaseUrl) {
		this.scriptBaseUrl = scriptBaseUrl;
	}

	/**
	 * Gets {@link #activeModules}.
	 *
	 * @return {@link #activeModules}
	 */
	public String getActiveModules() {
		return this.activeModules;
	}

	/**
	 * Sets {@link #activeModules}.
	 *
	 * @param activeModules
	 *            New value for {@link #activeModules}
	 */
	public void setActiveModules(String activeModules) {
		this.activeModules = activeModules;
	}

	/**
	 * Gets {@link #relevancyThreshold}.
	 *
	 * @return {@link #relevancyThreshold}
	 */
	public int getRelevancyThreshold() {
		return this.relevancyThreshold;
	}

	/**
	 * Sets {@link #relevancyThreshold}.
	 *
	 * @param relevancyThreshold
	 *            New value for {@link #relevancyThreshold}
	 */
	public void setRelevancyThreshold(int relevancyThreshold) {
		this.relevancyThreshold = relevancyThreshold;
	}

	/**
	 * Gets {@link #listenerInstrumentationAllowed}.
	 *
	 * @return {@link #listenerInstrumentationAllowed}
	 */
	public boolean isListenerInstrumentationAllowed() {
		return this.listenerInstrumentationAllowed;
	}

	/**
	 * Sets {@link #listenerInstrumentationAllowed}.
	 *
	 * @param listenerInstrumentationAllowed
	 *            New value for {@link #listenerInstrumentationAllowed}
	 */
	public void setListenerInstrumentationAllowed(boolean listenerInstrumentationAllowed) {
		this.listenerInstrumentationAllowed = listenerInstrumentationAllowed;
	}

	/**
	 * Gets {@link #agentMinificationEnabled}.
	 *
	 * @return {@link #agentMinificationEnabled}
	 */
	public boolean isAgentMinificationEnabled() {
		return this.agentMinificationEnabled;
	}

	/**
	 * Sets {@link #agentMinificationEnabled}.
	 *
	 * @param agentMinificationEnabled
	 *            New value for {@link #agentMinificationEnabled}
	 */
	public void setAgentMinificationEnabled(boolean agentMinificationEnabled) {
		this.agentMinificationEnabled = agentMinificationEnabled;
	}

	/**
	 * Gets {@link #respectDNTHeader}.
	 *
	 * @return {@link #respectDNTHeader}
	 */
	public boolean isRespectDNTHeader() {
		return this.respectDNTHeader;
	}

	/**
	 * Sets {@link #respectDNTHeader}.
	 *
	 * @param respectDNTHeader
	 *            New value for {@link #respectDNTHeader}
	 */
	public void setRespectDNTHeader(boolean respectDNTHeader) {
		this.respectDNTHeader = respectDNTHeader;
	}

	/**
	 * Gets {@link #eventSelectors}.
	 *
	 * @return {@link #eventSelectors}
	 */
	public List<AgentEumDomEventSelector> getEventSelectors() {
		return this.eventSelectors;
	}

	/**
	 * Sets {@link #eventSelectors}.
	 *
	 * @param eventSelectors
	 *            New value for {@link #eventSelectors}
	 */
	public void setEventSelectors(List<AgentEumDomEventSelector> eventSelectors) {
		this.eventSelectors = eventSelectors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.activeModules == null) ? 0 : this.activeModules.hashCode());
		result = (prime * result) + (this.agentMinificationEnabled ? 1231 : 1237);
		result = (prime * result) + (this.enabled ? 1231 : 1237);
		result = (prime * result) + ((this.eventSelectors == null) ? 0 : this.eventSelectors.hashCode());
		result = (prime * result) + (this.listenerInstrumentationAllowed ? 1231 : 1237);
		result = (prime * result) + this.relevancyThreshold;
		result = (prime * result) + (this.respectDNTHeader ? 1231 : 1237);
		result = (prime * result) + ((this.scriptBaseUrl == null) ? 0 : this.scriptBaseUrl.hashCode());
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
		AgentEndUserMonitoringConfig other = (AgentEndUserMonitoringConfig) obj;
		if (this.activeModules == null) {
			if (other.activeModules != null) {
				return false;
			}
		} else if (!this.activeModules.equals(other.activeModules)) {
			return false;
		}
		if (this.agentMinificationEnabled != other.agentMinificationEnabled) {
			return false;
		}
		if (this.enabled != other.enabled) {
			return false;
		}
		if (this.eventSelectors == null) {
			if (other.eventSelectors != null) {
				return false;
			}
		} else if (!this.eventSelectors.equals(other.eventSelectors)) {
			return false;
		}
		if (this.listenerInstrumentationAllowed != other.listenerInstrumentationAllowed) {
			return false;
		}
		if (this.relevancyThreshold != other.relevancyThreshold) {
			return false;
		}
		if (this.respectDNTHeader != other.respectDNTHeader) {
			return false;
		}
		if (this.scriptBaseUrl == null) {
			if (other.scriptBaseUrl != null) {
				return false;
			}
		} else if (!this.scriptBaseUrl.equals(other.scriptBaseUrl)) {
			return false;
		}
		return true;
	}

}