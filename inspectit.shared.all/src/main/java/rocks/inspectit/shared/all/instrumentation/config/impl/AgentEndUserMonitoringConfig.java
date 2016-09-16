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
		enabled = false;
		scriptBaseUrl = "/";
		activeModules = "";
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
		super();
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

}