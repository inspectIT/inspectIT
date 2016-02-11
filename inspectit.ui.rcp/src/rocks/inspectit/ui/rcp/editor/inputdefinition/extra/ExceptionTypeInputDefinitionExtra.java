package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * Input definition extra for displaying the concrete exception type.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionTypeInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * The detailed name of the {@link Throwable} object.
	 */
	private String throwableType;

	/**
	 * Gets {@link #throwableType}.
	 * 
	 * @return {@link #throwableType}
	 */
	public String getThrowableType() {
		return throwableType;
	}

	/**
	 * Sets {@link #throwableType}.
	 * 
	 * @param throwableType
	 *            New value for {@link #throwableType}
	 */
	public void setThrowableType(String throwableType) {
		this.throwableType = throwableType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(throwableType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		ExceptionTypeInputDefinitionExtra that = (ExceptionTypeInputDefinitionExtra) object;
		return Objects.equal(this.throwableType, that.throwableType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("throwableType", throwableType).toString();
	}

}
