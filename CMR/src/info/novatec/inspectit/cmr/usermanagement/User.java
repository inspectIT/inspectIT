package info.novatec.inspectit.cmr.usermanagement;

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
	 * The SHA256 Hash of the userpassword.
	 */
	private String hashedPassword;
	/**
	 * The users email adress.
	 * Used to send a new password, if the current one is lost.
	 */
	private String email;
	/**
	 * The unique id the user is identified with.
	 */
	private int id; //XXX not necessary?
	/**
	 * The users role the user is set to.
	 */
	private Role userRole;

	/**
	 * The constructor for a User object.
	 * @param name The name of the user
	 * @param hashedPassword The hashed password of the user
	 * @param email The email of the user
	 * @param id The id of the user
	 * @param userRole The role the user is attached to
	 */
	public User(String name, String hashedPassword, String email, int id, Role userRole) {
		super();
		this.name = name;
		this.hashedPassword = hashedPassword;
		this.email = email;
		this.id = id;
		this.userRole = userRole;
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
	 * Gets {@link #hashedPassword}.
	 *   
	 * @return {@link #hashedPassword}  
	 */
	public String getHashedPassword() {
		return hashedPassword;
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
	public int getId() {
		return id;
	}
	/**
	 * Gets {@link #userRole}.
	 *   
	 * @return {@link #userRole}  
	 */
	public Role getUserRole() {
		return userRole;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "User [name=" + name + ", hashedPassword=" + hashedPassword + ", email=" + email + ", id=" + id + ", userRole=" + userRole + "]";
	}
}
