package rocks.inspectit.shared.all.communication.data.cmr;

/**
 * Data object representing a recognized business transaction of an application.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionData {
	/**
	 * Business transaction identifier.
	 */
	private int id;

	/**
	 * Identifier of corresponding business transaction definition.
	 */
	private int businessTransactionDefinitionId;

	/**
	 * {@link ApplicationData} instance this business transaction belongs to.
	 */
	private ApplicationData application;

	/**
	 * Name of the business transaction.
	 */
	private String name;

	/**
	 * Default Constructor.
	 */
	public BusinessTransactionData() {
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            business transaction identifier.
	 * @param businessTransactionDefinitionId
	 *            identifier of corresponding business transaction definition.
	 * @param application
	 *            {@link ApplicationData} instance this business transaction belongs to.
	 * @param name
	 *            name of the business transaction.
	 */
	public BusinessTransactionData(int id, int businessTransactionDefinitionId, ApplicationData application, String name) {
		this.businessTransactionDefinitionId = businessTransactionDefinitionId;
		this.id = id;
		this.application = application;
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
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets {@link #applicationId}.
	 *
	 * @return {@link #applicationId}
	 */
	public ApplicationData getApplication() {
		return application;
	}

	/**
	 * Sets {@link #application}.
	 *
	 * @param application
	 *            New value for {@link #application}
	 */
	public void setApplication(ApplicationData application) {
		this.application = application;
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
	 * Gets {@link #businessTransactionDefinitionId}.
	 *
	 * @return {@link #businessTransactionDefinitionId}
	 */
	public int getBusinessTransactionDefinitionId() {
		return businessTransactionDefinitionId;
	}

	/**
	 * Sets {@link #businessTransactionDefinitionId}.
	 *
	 * @param businessTransactionDefinitionId
	 *            New value for {@link #businessTransactionDefinitionId}
	 */
	public void setBusinessTransactionDefinitionId(int businessTransactionDefinitionId) {
		this.businessTransactionDefinitionId = businessTransactionDefinitionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.application == null) ? 0 : this.application.hashCode());
		result = (prime * result) + this.businessTransactionDefinitionId;
		result = (prime * result) + this.id;
		result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
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
		BusinessTransactionData other = (BusinessTransactionData) obj;
		if (this.application == null) {
			if (other.application != null) {
				return false;
			}
		} else if (!this.application.equals(other.application)) {
			return false;
		}
		if (this.businessTransactionDefinitionId != other.businessTransactionDefinitionId) {
			return false;
		}
		if (this.id != other.id) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
