
package info.novatec.inspectit.cmr.usermanagement;


import org.springframework.orm.hibernate3.support.HibernateDaoSupport;



/**
 * Storage for a single privilege.
 * @author Joshua Hartmann
 *  extends HibernateDaoSupport 
 */
public class Permission {

	/**
	 * The id of the privilege, used to identify which functionality it covers, must be unique.
	 */
	private long id;
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
	 * @param title The short title of the Permission.
	 * @param description The more detailed description of the privilege.
	 */
	public Permission(String title, String description) {
		super();
		this.title = title;
		this.description = description;
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
		return "Permission [id=" + id + ", title=" + title + ", description=" + description + "]";
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
