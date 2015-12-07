package info.novatec.inspectit.communication.data.cmr;

/**
 * Application data for class representing a recognized application.
 *
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
		this.setId(id);
		this.applicationDefinitionId = applicationDefinitionId;
		this.setName(name);
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
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(int id) {
		this.id = id;
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
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
		if (getId() != other.getId()) {
			return false;
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}
}
