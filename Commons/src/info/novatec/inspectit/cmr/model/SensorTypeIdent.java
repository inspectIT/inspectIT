package info.novatec.inspectit.cmr.model;

import java.io.Serializable;

/**
 * The Sensor Type Ident class is the abstract base class for the {@link MethodSensorTypeIdent} and
 * {@link PlatformSensorTypeIdent} classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class SensorTypeIdent implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8196924255255396992L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * The many-to-many association to the {@link PlatformIdent} objects.
	 */
	private PlatformIdent platformIdent;

	/**
	 * The fully qualified class name of the sensor type.
	 */
	private String fullyQualifiedClassName;

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
	 * Gets {@link #platformIdent}.
	 * 
	 * @return {@link #platformIdent}
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * Sets {@link #platformIdent}.
	 * 
	 * @param platformIdent
	 *            New value for {@link #platformIdent}
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #fullyQualifiedClassName}.
	 * 
	 * @return {@link #fullyQualifiedClassName}
	 */
	public String getFullyQualifiedClassName() {
		return fullyQualifiedClassName;
	}

	/**
	 * Sets {@link #fullyQualifiedClassName}.
	 * 
	 * @param fullyQualifiedClassName
	 *            New value for {@link #fullyQualifiedClassName}
	 */
	public void setFullyQualifiedClassName(String fullyQualifiedClassName) {
		this.fullyQualifiedClassName = fullyQualifiedClassName;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedClassName == null) ? 0 : fullyQualifiedClassName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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
		SensorTypeIdent other = (SensorTypeIdent) obj;
		if (fullyQualifiedClassName == null) {
			if (other.fullyQualifiedClassName != null) {
				return false;
			}
		} else if (!fullyQualifiedClassName.equals(other.fullyQualifiedClassName)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
