package info.novatec.inspectit.cmr.usermanagement;

import java.io.Serializable;



/**
 * Representing a user in the system.
 * @author Joshua Hartmann
 *
 */
public class User {
	
	/**
	 * The unique name of the user.
	 */
	private String name;
	/**
	 * The hex string representation of the SHA256 Hash of the user password.
	 */
	private String password;
	/**
	 * The users email adress.
	 * Used to send a new password, if the current one is lost.
	 */
	private String email;
	/**
	 * The id of the role the user is set to.
	 */
	private long roleId;
	
	/**
	 * The default-constructor.
	 */
	public User() {
		
	}
	/**
	 * The constructor for a User object.
	 * @param name The name of the user
	 * @param password The hashed password of the user
	 * @param email The email of the user
	 * @param roleId The id of the role the user is attached to
	 */
	public User(String name, String password, String email, long roleId) {
		super();
		this.name = name;
		this.password = Permutation.hashString(password);
		this.email = email;
		this.roleId = roleId;
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
	 * Gets {@link #password}.
	 *   
	 * @return {@link #password}  
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * Gets {@link #email}.
	 *   
	 * @return {@link #email}  
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "User [name=" + name + ", hashed password [" + password + "] , email=" + email + ", userRoleId=" + roleId + "]";
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
	 * Sets {@link #password}.  
	 *   
	 * @param password  
	 *            New value for {@link #password}
	 */
	public void setPassword(String password) {
			this.password = Permutation.hashString(password);

	}
	/**  
	 * Sets {@link #email}.  
	 *   
	 * @param email  
	 *            New value for {@link #email}  
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * Gets {@link #roleId}.
	 *   
	 * @return {@link #roleId}  
	 */
	public long getRoleId() {
		return roleId;
	}
	/**  
	 * Sets {@link #roleId}.  
	 *   
	 * @param roleId  
	 *            New value for {@link #roleId}  
	 */
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	
	
}
