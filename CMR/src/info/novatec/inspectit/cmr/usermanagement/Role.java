package info.novatec.inspectit.cmr.usermanagement;

import java.util.List;;
/**
 * Stores the permissions asociated with the Role.
 * @author Joshua Hartmann
 *
 */
public class Role {
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
	 * The constructor for a role.
	 * @param permissions The permissions this role has.
	 * @param title The title for the role.
	 */
	public Role(String title, List<Permission> permissions) {
		super();
		this.permissions = permissions;
		this.title = title;
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
		return "Role [permissions=" + permissions + ", title=" + title + ", id=" + id + "]";
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
