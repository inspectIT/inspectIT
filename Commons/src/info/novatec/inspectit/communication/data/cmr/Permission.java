
package info.novatec.inspectit.communication.data.cmr;

import java.io.Serializable;
/**
 * Storage for a single permission.
 * 
 * @author Joshua Hartmann
 * @author Andreas Herzog
 * @author Lucca Hellriegel
 */
public class Permission implements Serializable {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5411425549495314822L;
	/**
	 * The id of the permission, used to identify which functionality it covers, must be unique.
	 */
	private long id;
	/**
	 * A short title for the permission.
	 */
	private String title;
	/**
	 * A more detailed description for the functionality the permission covers.
	 */
	private String description;
	
	/**
	 * Parameter for advanced permissions.
	 */
	private String parameter;

	/**
	 * Default constructor for permission.
	 */
	public Permission() {
		
	}
	
	/**
	 * The constructor for a permission.
	 * @param title The short title of the permission.
	 * @param description The more detailed description of the permission.
	 */
	public Permission(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		this.parameter = "No parameter given";
		this.id = 0;
	}
	
	/**
	 * The constructor for a permission with a parameter.
	 * @param title The short title of the permission.
	 * @param description The more detailed description of the permission.
	 * @param parameter for advanced permissions.
	 */
	public Permission(String title, String description, String parameter) {
		super();
		this.title = title;
		this.description = description;
		this.parameter = parameter;
		this.id = 0;
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
		return "Permission [id=" + id + ", title='" + title + "', description='" + description + "', parameter='" + parameter + "']";
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
	
	/**
	 * @return the parameter
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	
	
}
