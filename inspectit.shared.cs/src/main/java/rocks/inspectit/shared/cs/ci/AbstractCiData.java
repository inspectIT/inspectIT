package rocks.inspectit.shared.cs.ci;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Abstract data POJO to contain shared properties of {@link Profile}s and {@link Environment}s.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractCiData {

	/**
	 * Id.
	 */
	@XmlAttribute(name = "id", required = true)
	private String id;

	/**
	 * Name.
	 */
	@XmlAttribute(name = "name", required = true)
	private String name;

	/**
	 * Description.
	 */
	@XmlAttribute(name = "description")
	private String description;

	/**
	 * Date created.
	 */
	@XmlAttribute(name = "created-on", required = true)
	private Date createdDate;

	/**
	 * Date updated.
	 */
	@XmlAttribute(name = "updated-on")
	private Date updatedDate;

	/**
	 * Date updated.
	 */
	@XmlAttribute(name = "imported-on")
	private Date importDate;

	/**
	 * Revision. Server for version control and updating control.
	 */
	@XmlAttribute(name = "revision")
	private Integer revision = Integer.valueOf(1);

	/**
	 * Default constructor.
	 */
	public AbstractCiData() {
	}

	/**
	 * Clone constructor.
	 *
	 * @param template
	 *            template for the new instance
	 */
	public AbstractCiData(AbstractCiData template) {
		if (template != null) {
			this.id = template.id;
			this.name = template.name;
			this.description = template.description;
			this.createdDate = template.createdDate;
			this.updatedDate = template.updatedDate;
			this.importDate = template.importDate;
			this.revision = template.revision;
		}
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
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
	 * Gets {@link #createdDate}.
	 *
	 * @return {@link #createdDate}
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets {@link #createdDate}.
	 *
	 * @param createdDate
	 *            New value for {@link #createdDate}
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Gets {@link #updatedDate}.
	 *
	 * @return {@link #updatedDate}
	 */
	public Date getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * Sets {@link #updatedDate}.
	 *
	 * @param updatedDate
	 *            New value for {@link #updatedDate}
	 */
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	/**
	 * Gets {@link #importDate}.
	 *
	 * @return {@link #importDate}
	 */
	public Date getImportDate() {
		return importDate;
	}

	/**
	 * Sets {@link #importDate}.
	 *
	 * @param importDate
	 *            New value for {@link #importDate}
	 */
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
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
		result = (prime * result) + ((this.createdDate == null) ? 0 : this.createdDate.hashCode());
		result = (prime * result) + ((this.description == null) ? 0 : this.description.hashCode());
		result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
		result = (prime * result) + ((this.importDate == null) ? 0 : this.importDate.hashCode());
		result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
		result = (prime * result) + ((this.revision == null) ? 0 : this.revision.hashCode());
		result = (prime * result) + ((this.updatedDate == null) ? 0 : this.updatedDate.hashCode());
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
		AbstractCiData other = (AbstractCiData) obj;
		if (this.createdDate == null) {
			if (other.createdDate != null) {
				return false;
			}
		} else if (!this.createdDate.equals(other.createdDate)) {
			return false;
		}
		if (this.description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!this.description.equals(other.description)) {
			return false;
		}
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		if (this.importDate == null) {
			if (other.importDate != null) {
				return false;
			}
		} else if (!this.importDate.equals(other.importDate)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!this.revision.equals(other.revision)) {
			return false;
		}
		if (this.updatedDate == null) {
			if (other.updatedDate != null) {
				return false;
			}
		} else if (!this.updatedDate.equals(other.updatedDate)) {
			return false;
		}
		return true;
	}

}
