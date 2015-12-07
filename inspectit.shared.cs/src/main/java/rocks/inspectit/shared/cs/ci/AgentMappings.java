package rocks.inspectit.shared.cs.ci;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Utility class that combines all {@link AgentMappings} for easier marshalling.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "agent-mappings")
public class AgentMappings {

	/**
	 * {@link AgentMapping}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = AgentMapping.class) })
	private Collection<AgentMapping> mappings = new ArrayList<>(0);

	/**
	 * Revision. Server for version control and updating control.
	 */
	@XmlAttribute(name = "revision")
	private Integer revision = 1;

	/**
	 * No-arg constructor.
	 */
	public AgentMappings() {
	}

	/**
	 * @param mappings
	 *            {@link AgentMapping}s.
	 */
	public AgentMappings(Collection<AgentMapping> mappings) {
		this.mappings = mappings;
	}

	/**
	 * Gets {@link #mappings}.
	 *
	 * @return {@link #mappings}
	 */
	public Collection<AgentMapping> getMappings() {
		return mappings;
	}

	/**
	 * Sets {@link #mappings}.
	 *
	 * @param mappings
	 *            New value for {@link #mappings}
	 */
	public void setMappings(Collection<AgentMapping> mappings) {
		this.mappings = mappings;
	}

	/**
	 * Gets {@link #revision}.
	 *
	 * @return {@link #revision}
	 */
	public int getRevision() {
		return revision.intValue();
	}

	/**
	 * Sets {@link #revision}.
	 *
	 * @param revision
	 *            New value for {@link #revision}
	 */
	public void setRevision(int revision) {
		this.revision = Integer.valueOf(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((mappings == null) ? 0 : mappings.hashCode());
		result = (prime * result) + revision;
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
		AgentMappings other = (AgentMappings) obj;
		if (mappings == null) {
			if (other.mappings != null) {
				return false;
			}
		} else if (!mappings.equals(other.mappings)) {
			return false;
		}
		if (getRevision() != other.getRevision()) {
			return false;
		}
		return true;
	}

}
