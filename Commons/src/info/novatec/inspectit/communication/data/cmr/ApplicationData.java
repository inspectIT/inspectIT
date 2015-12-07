/**
 *
 */
package info.novatec.inspectit.communication.data.cmr;

/**
 * @author Alexander Wert
 *
 */
public class ApplicationData {

	/**
	 * Application identifier.
	 */
	private int id;

	/**
	 * Application name.
	 */
	private String name;

	/**
	 * Identifier of corresponding application definition.
	 */
	private int applicationDefinitionId;

	/**
	 * Default Constructor.
	 */
	public ApplicationData() {

	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            application identifier.
	 * @param applicationDefinitionId
	 *            identifier of corresponding application definition.
	 * @param name
	 *            application name.
	 */
	public ApplicationData(int id, int applicationDefinitionId, String name) {
		super();
		this.id = id;
		this.applicationDefinitionId = applicationDefinitionId;
		this.name = name;
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
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ApplicationData other = (ApplicationData) obj;
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets {@link #applicationDefinitionId}.
	 *
	 * @return {@link #applicationDefinitionId}
	 */
	public int getApplicationDefinitionId() {
		return applicationDefinitionId;
	}

	/**
	 * Sets {@link #applicationDefinitionId}.
	 *
	 * @param applicationDefinitionId
	 *            New value for {@link #applicationDefinitionId}
	 */
	public void setApplicationDefinitionId(int applicationDefinitionId) {
		this.applicationDefinitionId = applicationDefinitionId;
	}

}
