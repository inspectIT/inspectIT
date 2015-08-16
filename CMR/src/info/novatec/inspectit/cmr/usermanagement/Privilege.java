package info.novatec.inspectit.cmr.usermanagement;
/**
 * Storage for a single privilege.
 * @author Joshua Hartmann
 *
 */
public class Privilege {
	/**
	 * The id of the privilege, used to identify which functionality it covers, must be unique.
	 */
	private int id;
	/**
	 * A short title for the privilege.
	 */
	private String title;
	/**
	 * A more detailed description for the functionality the privilege covers.
	 */
	private String description;



	/**
	 * The constructor for a privilege.
	 * @param id The id for the role, which identifies it.
	 * @param title The short title of the Privilege.
	 * @param description The more detailed description of the privilege.
	 */
	public Privilege(int id, String title, String description) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
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
		return "Privilege [id=" + id + ", title=" + title + ", description=" + description + "]";
	}
}
