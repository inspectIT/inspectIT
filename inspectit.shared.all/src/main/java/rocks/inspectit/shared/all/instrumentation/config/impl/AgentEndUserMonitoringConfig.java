package rocks.inspectit.shared.all.instrumentation.config.impl;

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
	 * Default constructor.
	 */
	public AgentEndUserMonitoringConfig() {
	}

	/**
	 * @param isEnabled
	 *            true, if end user monitoring is enabled
	 * @param scriptBaseUrl
	 *            the base url to place the script under and to sent the beacons to
	 * @param activeModules
	 *            the active modules, see {@link JSAgentModule}.
	 */
	public AgentEndUserMonitoringConfig(boolean isEnabled, String scriptBaseUrl, String activeModules) {
		this.enabled = isEnabled;
		this.scriptBaseUrl = scriptBaseUrl;
		this.activeModules = activeModules;
	}

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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.activeModules == null) ? 0 : this.activeModules.hashCode());
		result = (prime * result) + (this.enabled ? 1231 : 1237);
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
		if (this.enabled != other.enabled) {
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