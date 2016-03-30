package info.novatec.inspectit.communication.data.cmr;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

/**
 * Storage for a single permission.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 */
@Entity
@NamedQueries({ @NamedQuery(name = Permission.FIND_ALL, query = "SELECT p FROM Permission p"),
		@NamedQuery(name = Permission.FIND_BY_TITLE, query = "SELECT p FROM Permission p WHERE p.title=:title"),
		@NamedQuery(name = Permission.FIND_BY_ID, query = "SELECT p FROM Permission p WHERE p.id=:id") })
public class Permission implements Comparable<Permission>, Serializable {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5411425549495314822L;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "Permission.findAll";

	/**
	 * Constant for findByEmail query.
	 */
	public static final String FIND_BY_TITLE = "Permission.findByTitle";

	/**
	 * Constant for findByEmail query.
	 */
	public static final String FIND_BY_ID = "Permission.findById";

	/**
	 * The id of the permission, used to identify which functionality it covers,
	 * must be unique.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERMISSION_SEQUENCE")
	@SequenceGenerator(name = "PERMISSION_SEQUENCE", sequenceName = "PERMISSION_SEQUENCE")
	private Long id;

	/**
	 * A short title for the permission.
	 */
	@Column(unique = true, nullable = false)
	private String title;

	/**
	 * A more detailed description for the functionality the permission covers.
	 */
	private String description;

	/**
	 * Parameter for advanced permissions.
	 */
	private String parameter;

	/**
	 * Default constructor for permission.
	 */
	public Permission() {

	}

	/**
	 * The constructor for a permission.
	 * 
	 * @param title
	 *            The short title of the permission.
	 * @param description
	 *            The more detailed description of the permission.
	 */
	public Permission(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		this.parameter = "No parameter given";
	}

	/**
	 * The constructor for a permission with a parameter.
	 * 
	 * @param title
	 *            The short title of the permission.
	 * @param description
	 *            The more detailed description of the permission.
	 * @param parameter
	 *            for advanced permissions.
	 */
	public Permission(String title, String description, String parameter) {
		super();
		this.title = title;
		this.description = description;
		this.parameter = parameter;
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Gets {@link #title}.
	 * 
	 * @return {@link #title}
	 */
	public String getTitle() {
		return title;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Permission [id=" + id + ", title='" + title + "', description='" + description + "', parameter='"
				+ parameter + "']";
	}

	/**
	 * Sets {@link #title}.
	 * 
	 * @param title
	 *            New value for {@link #title}
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
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
	 * @return the parameter
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * @param parameter
	 *            the parameter to set
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	/**
	 * Comparison based on the id of the permissions.
	 * 
	 * @param o
	 *            the other permission.
	 * @return 0 if both ids are null, -1 if only this id is null, otherwise this.getId().compareTo(o.getId())
	 */
	public int compareTo(Permission o) {		
		if (getId() == null && o.getId() == null) {
			if (o.getId() == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			return getId().compareTo(o.getId());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}
	
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
		Permission other = (Permission) obj;
		if (title == null) {
			if (other.getTitle() != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (parameter == null) {
			if (other.parameter != null) {
				return false;
			}
		} else if (!parameter.equals(other.parameter)) {
			return false;
		}
		return true;
	}
}
