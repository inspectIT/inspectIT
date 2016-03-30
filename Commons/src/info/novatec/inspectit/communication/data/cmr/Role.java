package info.novatec.inspectit.communication.data.cmr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

/**
 * Stores the permissions asociated with the Role.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = Role.FIND_ALL, query = "SELECT r FROM Role r"),
		@NamedQuery(name = Role.FIND_BY_TITLE, query = "SELECT r FROM Role r WHERE r.title=:title"),
		@NamedQuery(name = Role.FIND_BY_ID, query = "SELECT r FROM Role r WHERE r.id=:id") })
public class Role implements Serializable {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2997485478449132744L;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "Role.findAll";

	/**
	 * Constant for findByEmail query.
	 */
	public static final String FIND_BY_TITLE = "Role.findByTitle";

	/**
	 * Constant for findByEmail query.
	 */
	public static final String FIND_BY_ID = "Role.findById";

	/**
	 * The id of the Role.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ROLE_SEQUENCE")
	@SequenceGenerator(name = "ROLE_SEQUENCE", sequenceName = "ROLE_SEQUENCE")
	private Long id;

	/**
	 * Contains all permissions this role has.
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	// Using a List<Permission> will lead to a error with Kryo and Hibernate
	// because Hibernate uses a PersistentBag
	private Set<Permission> permissions;

	/**
	 * A short title to name the role.
	 */
	@Column(unique = true, nullable = false)
	private String title;

	/**
	 * A more detailed description for the role.
	 */
	private String description;

	/**
	 * Default constructor for Role.
	 */
	public Role() {

	}

	/**
	 * The constructor for a role.
	 * 
	 * @param id
	 *            The id of the role. [id should no longer be hardcoded]
	 * @param permissions
	 *            The permissions this role has.
	 * @param title
	 *            The title for the role.
	 * @param description
	 *            Description of the role.
	 */
	public Role(long id, String title, List<Permission> permissions, String description) {
		super();
		this.permissions = new HashSet<Permission>(permissions);
		this.title = title;
		this.id = id;
		this.description = description;
	}

	/**
	 * The constructor for a role.
	 * 
	 * @param permissions
	 *            The permissions this role has.
	 * @param title
	 *            The title for the role.
	 * @param description
	 *            Description of the role.
	 */
	public Role(String title, List<Permission> permissions, String description) {
		super();
		this.permissions = new HashSet<Permission>(permissions);
		this.title = title;
		this.description = description;
	}

	/**
	 * Gets {@link #permissions}.
	 * 
	 * @return {@link #permissions}
	 */
	public List<Permission> getPermissions() {
		return new ArrayList<Permission>(permissions);
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
	 * Gets {@link #Id}.
	 * 
	 * @return {@link #Id}
	 */
	public Long getId() {
		return id;
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
		return "Role [permissions=" + getPermissions().toString() + ", title='" + title + "', description='"
				+ description + "', id=" + id + "]";
	}

	/**
	 * Sets {@link #permissions}.
	 * 
	 * @param permissions
	 *            New value for {@link #permissions}
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = new HashSet<Permission>(permissions);
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
}