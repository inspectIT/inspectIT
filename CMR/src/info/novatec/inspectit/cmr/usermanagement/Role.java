package info.novatec.inspectit.cmr.usermanagement;

import java.util.List;
/**
 * Stores the privileges asociated with the Role.
 * @author Joshua Hartmann
 *
 */
public class Role {
	/**
	 * Contains all privileges this role has.
	 */
	private List<Privilege> privileges;
	/**
	 * A short title to name the role.
	 */
	private String title;

	/**
	 * The id of the Role.
	 */
	private int id;


	/**
	 * The constructor for a role.
	 * @param privileges The privileges this role has.
	 * @param title The title for the role.
	 * @param id The id of the role.
	 */
	public Role(int id, String title, List<Privilege> privileges) {
		super();
		this.privileges = privileges;
		this.title = title;
		this.id = id;
	}
	/**
	 * Gets {@link #privileges}.
	 *   
	 * @return {@link #privileges}  
	 */
	public List<Privilege> getPrivileges() {
		return privileges;
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
	public int getId() {
		return id;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Role [privileges=" + privileges + ", title=" + title + ", id=" + id + "]";
	}
}
