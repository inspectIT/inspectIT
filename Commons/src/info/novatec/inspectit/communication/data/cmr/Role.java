package info.novatec.inspectit.communication.data.cmr;

import java.io.Serializable;
import java.util.List;

/**
 * Stores the permissions asociated with the Role.
 * @author Joshua Hartmann
 * @author Andreas Herzog
 *
 */
public class Role implements Serializable {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2997485478449132744L;
	/**
	 * Contains all permissions this role has.
	 */
	private List<Permission> permissions;
	/**
	 * A short title to name the role.
	 */
	private String title;

	/**
	 * The id of the Role.
	 */
	private long id;

	/**
	 * Default constructor for Role.
	 */
	
	public Role() {
		
	}
	
	/**
	 * The constructor for a role.
	 * @param id The id of the role.
	 * @param permissions The permissions this role has.
	 * @param title The title for the role.
	 */
	public Role(long id, String title, List<Permission> permissions) {
		super();
		this.permissions = permissions;
		this.title = title;
		this.id = id;
	}
		
	/**
	 * Gets {@link #permissions}.
	 *   
	 * @return {@link #permissions}  
	 */
	public List<Permission> getPermissions() {
		return permissions;
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
	public long getId() {
		return id;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Role [permissions=" + getPermissions().toString() + ", title=" + title + ", id=" + id + "]";
	}
	/**  
	 * Sets {@link #permissions}.  
	 *   
	 * @param permissions  
	 *            New value for {@link #permissions}  
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
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
	
	
	
}
