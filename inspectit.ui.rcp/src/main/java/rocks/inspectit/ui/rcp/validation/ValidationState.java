package rocks.inspectit.ui.rcp.validation;

/**
 * The validation state describes an abstract result of an validation process of the contents of
 * model or UI elements.
 *
 * @author Alexander Wert
 *
 */
public class ValidationState {

	/**
	 * Identifier of the element this validation state has been created for.
	 */
	private final Object id;

	/**
	 * Validation state.
	 */
	private boolean valid;

	/**
	 * Error message in the case that validation state is false.
	 */
	private String message;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            Identifier of the element this validation state has been created for.
	 * @param valid
	 *            Validation state.
	 * @param message
	 *            Error message in the case that validation state is false.
	 */
	public ValidationState(Object id, boolean valid, String message) {
		this.id = id;
		this.valid = valid;
		this.message = message;
	}

	/**
	 * Gets {@link #valid}.
	 *
	 * @return {@link #valid}
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets {@link #valid}.
	 *
	 * @param valid
	 *            New value for {@link #valid}
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Gets {@link #message}.
	 *
	 * @return {@link #message}
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets {@link #message}.
	 *
	 * @param message
	 *            New value for {@link #message}
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
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
		ValidationState other = (ValidationState) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
