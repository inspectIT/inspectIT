package rocks.inspectit.shared.cs.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Abstract class that holds the schema version information. Classes should extend this class if
 * they want to pass over the schema version information.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractSchemaVersionAware implements ISchemaVersionAware {

	/**
	 * Schema version.
	 */
	@XmlAttribute(name = "schemaVersion", required = true)
	@JsonIgnore
	private int schemaVersion;

	/**
	 * Gets {@link #schemaVersion}.
	 *
	 * @return {@link #schemaVersion}
	 */
	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	/**
	 * Sets {@link #schemaVersion}.
	 *
	 * @param schemaVersion
	 *            New value for {@link #schemaVersion}
	 */
	@Override
	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.schemaVersion;
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
		AbstractSchemaVersionAware other = (AbstractSchemaVersionAware) obj;
		if (this.schemaVersion != other.schemaVersion) {
			return false;
		}
		return true;
	}

}
