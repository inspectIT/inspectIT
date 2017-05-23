package rocks.inspectit.shared.cs.ci.eum;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;

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
	 * The Base URL which gets instrumented to deliver the JS Agent. Must begin and end with a
	 * slash.
	 */
	@XmlAttribute(name = "eum-script-base-url", required = true)
	private String scriptBaseUrl = "/";

	/**
	 * Each character of this String maps to a single module which is activated.
	 */
	@XmlAttribute(name = "active-modules", required = true)
	private String activeModules = String.valueOf(JSAgentModule.BROWSERINFO_MODULE.getIdentifier()) + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier() + JSAgentModule.RESTIMINGS_MODULE.getIdentifier(); // NOPMD

	/**
	 * Defines a Threshold in milliseconds to make the traces generated at the browser more lean.
	 * Captured JS Function execution are only sent if (a) their execution duration is greather than
	 * or equa lto his threshold or (b) they triggered a request. This early filtering of unrelevant
	 * reduces the load on the client aswell as on the CMR.
	 */
	@XmlAttribute(name = "relevancyThreshold", required = true)
	private int relevancyThreshold = 10;

	/**
	 * Flag whether to allow the JS Agent to instrument listeners. Listener instrumentation can have
	 * a performance impact for sites which have a high rate of listener executions. This flag
	 * allows to globally disable the instrumentation of listeners while other data can still be
	 * captured. For example, this also includes listeners to AJAX states.
	 */
	@XmlAttribute(name = "listenerInstrumentationAllowed", required = true)
	private boolean listenerInstrumentationAllowed = true;

	/**
	 * Enables or disables the minification of the JS agent.
	 */
	@XmlAttribute(name = "agentMinificationEnabled", required = true)
	private boolean agentMinificationEnabled = true;

	/**
	 * When enabled, users sending a Do-Not-Track Header will not receive teh JS Agent.
	 */
	@XmlAttribute(name = "respectDNTHeader", required = true)
	private boolean respectDNTHeader = false;

	/**
	 * Contains a list of all {@link EumDomEventSelector} to use.
	 */
	@XmlElementRefs({ @XmlElementRef(type = EumDomEventSelector.class) })
	private Collection<EumDomEventSelector> eventSelectors = new ArrayList<>();
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
	public Collection<EumDomEventSelector> getEventSelectors() {
		return this.eventSelectors;
	}

	/**
	 * Sets {@link #eventSelectors}.
	 *
	 * @param eventSelectors
	 *            New value for {@link #eventSelectors}
	 */
	public void setEventSelectors(Collection<EumDomEventSelector> eventSelectors) {
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
		result = (prime * result) + (this.eumEnabled ? 1231 : 1237);
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
		EndUserMonitoringConfig other = (EndUserMonitoringConfig) obj;
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
		if (this.eumEnabled != other.eumEnabled) {
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