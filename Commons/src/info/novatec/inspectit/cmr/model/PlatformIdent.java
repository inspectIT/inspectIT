package info.novatec.inspectit.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Platform Ident class is used to store the unique information of an Agent, so every Agent in
 * different JVMs on the same target server receives a different one.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlatformIdent implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 8501768676196666426L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * The timestamp which shows when this information was created on the CMR.
	 */
	private Timestamp timeStamp;

	/**
	 * The many-to-many association to the {@link SensorTypeIdent} objects.
	 */
	private Set<SensorTypeIdent> sensorTypeIdents = new HashSet<SensorTypeIdent>(0);

	/**
	 * The one-to-many association to the {@link MethodIdent} objects.
	 */
	private Set<MethodIdent> methodIdents = new HashSet<MethodIdent>(0);

	/**
	 * The one-to-many association to the {@link JmxDefinitionDataIdent} objects.
	 */
	private Set<JmxDefinitionDataIdent> jmxDefinitionDataIdents = new HashSet<JmxDefinitionDataIdent>(0);

	/**
	 * The list of ip's of the target system (including v4 and v6).
	 */
	private List<String> definedIPs;

	/**
	 * The self-defined name of the inspectIT Agent.
	 */
	private String agentName = "Agent";

	/**
	 * the current version of the agent.
	 */
	private String version = "n/a";

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #timeStamp}.
	 * 
	 * @return {@link #timeStamp}
	 */
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets {@link #timeStamp}.
	 * 
	 * @param timeStamp
	 *            New value for {@link #timeStamp}
	 */
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets {@link #sensorTypeIdents}.
	 * 
	 * @return {@link #sensorTypeIdents}
	 */
	public Set<SensorTypeIdent> getSensorTypeIdents() {
		return sensorTypeIdents;
	}

	/**
	 * Sets {@link #sensorTypeIdents}.
	 * 
	 * @param sensorTypeIdents
	 *            New value for {@link #sensorTypeIdents}
	 */
	public void setSensorTypeIdents(Set<SensorTypeIdent> sensorTypeIdents) {
		this.sensorTypeIdents = sensorTypeIdents;
	}

	/**
	 * Gets {@link #methodIdents}.
	 * 
	 * @return {@link #methodIdents}
	 */
	public Set<MethodIdent> getMethodIdents() {
		return methodIdents;
	}

	/**
	 * Sets {@link #methodIdents}.
	 * 
	 * @param methodIdents
	 *            New value for {@link #methodIdents}
	 */
	public void setMethodIdents(Set<MethodIdent> methodIdents) {
		this.methodIdents = methodIdents;
	}

	/**
	 * Gets {@link #definedIPs}.
	 * 
	 * @return {@link #definedIPs}
	 */
	public List<String> getDefinedIPs() {
		return definedIPs;
	}

	/**
	 * Sets {@link #definedIPs}.
	 * 
	 * @param definedIPs
	 *            New value for {@link #definedIPs}
	 */
	public void setDefinedIPs(List<String> definedIPs) {
		this.definedIPs = definedIPs;
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
	 * Gets {@link #version}.
	 * 
	 * @return {@link #version}
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets {@link #version}.
	 * 
	 * @param version
	 *            New value for {@link #version}
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Gets {@link #jmxDefinitionDataIdents}.
	 * 
	 * @return {@link #jmxDefinitionDataIdents}
	 */
	public Set<JmxDefinitionDataIdent> getJmxDefinitionDataIdents() {
		return jmxDefinitionDataIdents;
	}

	/**
	 * Sets {@link #jmxDefinitionDataIdents}.
	 * 
	 * @param jmxDefinitionDataIdents
	 *            New value for {@link #jmxDefinitionDataIdents}
	 */
	public void setJmxDefinitionDataIdents(Set<JmxDefinitionDataIdent> jmxDefinitionDataIdents) {
		this.jmxDefinitionDataIdents = jmxDefinitionDataIdents;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentName == null) ? 0 : agentName.hashCode());
		result = prime * result + ((definedIPs == null) ? 0 : definedIPs.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		PlatformIdent other = (PlatformIdent) obj;
		if (agentName == null) {
			if (other.agentName != null) {
				return false;
			}
		} else if (!agentName.equals(other.agentName)) {
			return false;
		}
		if (definedIPs == null) {
			if (other.definedIPs != null) {
				return false;
			}
		} else if (!definedIPs.equals(other.definedIPs)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
