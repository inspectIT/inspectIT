package info.novatec.inspectit.rcp.validation;

/**
 * Interface for listeners on the validation control decoration.
 * 
 * @author Ivan Senic
 * 
 */
public interface IControlValidationListener {

	/**
	 * State changed.
	 * 
	 * @param valid
	 *            If current state is valid.
	 * @param validationControlDecoration
	 *            {@link ValidationControlDecoration} firing the event.
	 */
	void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration);
}
