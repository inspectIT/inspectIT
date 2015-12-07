/**
 *
 */
package info.novatec.inspectit.communication.data.cmr;

/**
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
		super();
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
	 * Gets {@link #applicationId}.
	 *
	 * @return {@link #applicationId}
	 */
	public ApplicationData getApplication() {
		return application;
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
		result = prime * result + ((application == null) ? 0 : application.hashCode());
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
		BusinessTransactionData other = (BusinessTransactionData) obj;
		if (application == null) {
			if (other.application != null) {
				return false;
			}
		} else if (!application.equals(other.application)) {
			return false;
		}
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
	 * Gets {@link #businessTransactionDefinitionId}.
	 *
	 * @return {@link #businessTransactionDefinitionId}
	 */
	public int getBusinessTransactiondefinitionId() {
		return businessTransactionDefinitionId;
	}

	/**
	 * Sets {@link #businessTransactionDefinitionId}.
	 *
	 * @param businessTransactiondefinitionId
	 *            New value for {@link #businessTransactionDefinitionId}
	 */
	public void setBusinessTransactiondefinitionId(int businessTransactiondefinitionId) {
		this.businessTransactionDefinitionId = businessTransactiondefinitionId;
	}

}
