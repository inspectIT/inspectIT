package info.novatec.inspectit.indexing.restriction;

/**
 * Abstract class for all index query restriction classes.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractIndexQueryRestriction implements IIndexQueryRestriction {

	/**
	 * Field name.
	 */
	private String fieldName;

	/**
	 * Default constructor.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 */
	public AbstractIndexQueryRestriction(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getQualifiedMethodName() {
		if (null == fieldName) {
			return "";
		}
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		return methodName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		AbstractIndexQueryRestriction other = (AbstractIndexQueryRestriction) obj;
		if (fieldName == null) {
			if (other.fieldName != null) {
				return false;
			}
		} else if (!fieldName.equals(other.fieldName)) {
			return false;
		}
		return true;
	}

}
