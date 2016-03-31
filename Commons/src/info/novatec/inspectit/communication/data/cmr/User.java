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
 * Representing a user in the system.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 *
 */

@Entity
@NamedQueries({ @NamedQuery(name = User.FIND_ALL, query = "SELECT u FROM User u"),
		@NamedQuery(name = User.FIND_BY_EMAIL, query = "SELECT u FROM User u WHERE u.email=:email"),
		@NamedQuery(name = User.FIND_BY_ROLE_ID, query = "SELECT u FROM User u WHERE u.roleId=:roleId") })
public class User implements Serializable {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2583270705967441921L;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "User.findAll";

	/**
	 * Constant for findByEmail query.
	 */
	public static final String FIND_BY_EMAIL = "User.findByEmail";

	/**
	 * Constant for findByEmail query.
	 */
	public static final String FIND_BY_ROLE_ID = "User.findByRole";

	/**
	 * The id of the User.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQUENCE")
	@SequenceGenerator(name = "USER_SEQUENCE", sequenceName = "USER_SEQUENCE")
	private Long id;

	/**
	 * The hex string representation of the SHA256 Hash of the user password.
	 */
	private String password;

	/**
	 * The users email address. Used to send a new password, if the current one
	 * is lost.
	 */

	@Column(unique = true)
	private String email;
	/**
	 * The id of the role the user is set to.
	 */
	private Long roleId;

	/**
	 * Indicating if this user is locked.
	 */
	private boolean locked;

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
	 * @param isLocked
	 *            boolean to see if user is locked by admin
	 */
	public User(String password, String email, long roleId, boolean isLocked) {
		this.password = password;
		this.email = email;
		this.roleId = roleId;
		this.locked = isLocked;
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
		return (locked ? "(LOCKED) " : "") + "User [email=" + email + ", hashed password [" + password + "], "
				+ "userRoleId=" + roleId + "]";
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
	public Long getRoleId() {
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

	/**
	 * Indicating if the user is locked or not.
	 * 
	 * @return True if the user is locked, false if not.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Allows to lock/unlock a user.
	 * 
	 * @param locked
	 *            indicating if this user should be locked or not.
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
