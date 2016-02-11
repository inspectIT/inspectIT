package info.novatec.inspectit.ci;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Mapping to define which agent should be connected to which {@link Environment}.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "agent-mapping")
public class AgentMapping {

	/**
	 * If mapping is active.
	 */
	@XmlAttribute(name = "active", required = true)
	private boolean active = true;

	/**
	 * Agent name or pattern.
	 */
	@XmlAttribute(name = "agent-name", required = true)
	private String agentName;

	/**
	 * IP address or pattern.
	 */
	@XmlAttribute(name = "ip-address", required = true)
	private String ipAddress;

	/**
	 * Optional description.
	 */
	@XmlAttribute(name = "description")
	private String description;

	/**
	 * Id of the referenced environment.
	 */
	@XmlAttribute(name = "environment-id")
	private String environmentId;

	/**
	 * No-args constructor.
	 */
	public AgentMapping() {
		this("inspectIT", "localhost");
	}

	/**
	 * Default constructor.
	 * 
	 * @param agentName
	 *            Agent name or pattern.
	 * @param ipAddress
	 *            IP address or pattern.
	 */
	public AgentMapping(String agentName, String ipAddress) {
		this.agentName = agentName;
		this.ipAddress = ipAddress;
	}

	/**
	 * Gets {@link #active}.
	 * 
	 * @return {@link #active}
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets {@link #active}.
	 * 
	 * @param active
	 *            New value for {@link #active}
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets {@link #agentName}.
	 * 
	 * @return {@link #agentName}
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * Sets {@link #agentName}.
	 * 
	 * @param agentName
	 *            New value for {@link #agentName}
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	/**
	 * Gets {@link #ipAddress}.
	 * 
	 * @return {@link #ipAddress}
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets {@link #ipAddress}.
	 * 
	 * @param ipAddress
	 *            New value for {@link #ipAddress}
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Gets {@link #description}.
	 * 
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets {@link #description}.
	 * 
	 * @param description
	 *            New value for {@link #description}
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets {@link #environmentId}.
	 * 
	 * @return {@link #environmentId}
	 */
	public String getEnvironmentId() {
		return environmentId;
	}

	/**
	 * Sets {@link #environmentId}.
	 * 
	 * @param environmentId
	 *            New value for {@link #environmentId}
	 */
	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((agentName == null) ? 0 : agentName.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((environmentId == null) ? 0 : environmentId.hashCode());
		result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
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
		AgentMapping other = (AgentMapping) obj;
		if (active != other.active) {
			return false;
		}
		if (agentName == null) {
			if (other.agentName != null) {
				return false;
			}
		} else if (!agentName.equals(other.agentName)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (environmentId == null) {
			if (other.environmentId != null) {
				return false;
			}
		} else if (!environmentId.equals(other.environmentId)) {
			return false;
		}
		if (ipAddress == null) {
			if (other.ipAddress != null) {
				return false;
			}
		} else if (!ipAddress.equals(other.ipAddress)) {
			return false;
		}
		return true;
	}

}
