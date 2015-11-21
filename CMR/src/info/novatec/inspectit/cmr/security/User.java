package info.novatec.inspectit.cmr.security;

import java.io.Serializable;

/**
 * Representing a user in the system.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 *
 */
public class User implements Serializable {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2583270705967441921L;
	/**
	 * The hex string representation of the SHA256 Hash of the user password.
	 */
	private String password;
	/**
	 * The users email address. Used to send a new password, if the current one is lost.
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
	 * 
	 * @param password
	 *            The <b>hashed</b> password of the user
	 * @param email
	 *            The email of the user
	 * @param roleId
	 *            The id of the role the user is attached to
	 */
	public User(String password, String email, long roleId) {
		this.password = password;
		this.email = email;
		this.roleId = roleId;
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
		return "User [email=" + email + ", hashed password [" + password + "], " + "userRoleId=" + roleId + "]";
	}

	/**
	 * Sets {@link #password}.
	 * 
	 * @param password
	 *            New <b>hashed</b> value for {@link #password}
	 */
	public void setPassword(String password) {
		this.password = password;

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
