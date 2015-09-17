package info.novatec.inspectit.cmr.usermanagement;

import java.security.NoSuchAlgorithmException;


/**
 * Representing a user in the system.
 * @author Joshua Hartmann
 *
 */
public class User {
	
	
	/**
	 * The name of the user.
	 */
	private String name;
	/**
	 * The SHA256 Hash of the user password.
	 */
	private byte[] password;
	/**
	 * The users email address.
	 * The SHA256 Hash of the userpassword.
	 */
	/**
	 * The users email adress.
	 * Used to send a new password, if the current one is lost.
	 */
	private String email;
	/**
	 * The unique id the user is identified with.
	 */
	private long id;
	/**
	 * The id of the role the user is set to.
	 */
	private long roleId;

	/**
	 * The constructor for a User object.
	 * @param name The name of the user
	 * @param password The hashed password of the user
	 * @param email The email of the user
	 * @param roleId The id of the role the user is attached to
	 * @throws NoSuchAlgorithmException 
	 */
	public User(String name, String password, String email, long roleId) throws NoSuchAlgorithmException {
		super();
		this.name = name;
		this.password = Permutation.hash(password);
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
	public byte[] getPassword() {
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
	 * Gets {@link #id}.
	 *   
	 * @return {@link #id}  
	 */
	public long getId() {
		return id;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "User [name=" + name + ", hashedPassword is hidden , email=" + email + ", id=" + id + ", userRole=" + roleId + "]";

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
	 * @throws NoSuchAlgorithmException 
	 */
	public void setPassword(String password) throws NoSuchAlgorithmException {
			this.password = Permutation.hash(password);

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
	 * Sets {@link #id}.  
	 *   
	 * @param id  
	 *            New value for {@link #id}  
	 */
	public void setId(long id) {
		this.id = id;
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
