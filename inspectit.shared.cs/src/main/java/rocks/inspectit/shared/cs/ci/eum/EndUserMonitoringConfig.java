package rocks.inspectit.shared.cs.ci.eum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration class for setting up the End User Monitoring.
 *
 * @author David Monschein
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "end-user-monitoring-config")
public class EndUserMonitoringConfig {

	/**
	 * Attribute whether displays whether the EUM is enabled or not.
	 */
	@XmlAttribute(name = "eum-enabled", required = true)
	private boolean eumEnabled = false;

	/**
	 * The Base URL which gets instrumented to deliver the JS Agent.
	 */
	@XmlAttribute(name = "eum-script-base-url", required = true)
	private String scriptBaseUrl = "/";

	/**
	 * Each character of this String maps to a single module which is activated.
	 */
	@XmlAttribute(name = "active-modules", required = true)
	private String activeModules = "12";

	/**
	 * Gets {@link #eumEnabled}.
	 *
	 * @return {@link #eumEnabled}
	 */
	public boolean isEumEnabled() {
		return eumEnabled;
	}

	/**
	 * Sets {@link #eumEnabled}.
	 *
	 * @param eumEnabled
	 *            New value for {@link #eumEnabled}
	 */
	public void setEumEnabled(boolean eumEnabled) {
		this.eumEnabled = eumEnabled;
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
	 * @param activeModulesString
	 *            New value for {@link #activeModules}
	 */
	public void setActiveModules(String activeModulesString) {
		this.activeModules = activeModulesString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.activeModules == null) ? 0 : this.activeModules.hashCode());
		result = (prime * result) + (this.eumEnabled ? 1231 : 1237);
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
		EndUserMonitoringConfig other = (EndUserMonitoringConfig) obj;
		if (this.activeModules == null) {
			if (other.activeModules != null) {
				return false;
			}
		} else if (!this.activeModules.equals(other.activeModules)) {
			return false;
		}
		if (this.eumEnabled != other.eumEnabled) {
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