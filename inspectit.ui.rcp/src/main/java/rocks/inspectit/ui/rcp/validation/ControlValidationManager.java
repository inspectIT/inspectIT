package rocks.inspectit.ui.rcp.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import rocks.inspectit.ui.rcp.ci.form.part.business.DynamicNameExtractionPart;

/**
 * Validation manager responsible for delegating validation control changes to the upstream
 * validation manager.
 *
 * @author Alexander Wert
 * @param <T>
 *            Type of the control id
 */
public class ControlValidationManager<T> implements IControlValidationListener {

	/**
	 * A map of {@link ValidationControlDecoration} instances identified by their
	 * {@link ValidatorKey}.
	 */
	private final Map<ValidationControlDecoration<?>, T> controlValidators = new HashMap<>();

	/**
	 * Upstream validation manager to be notified on changes.
	 */
	private final AbstractValidationManager<String> upstreamValidationManager;

	/**
	 * Constructor.
	 *
	 * @param upstreamValidationManager
	 *            Upstream validation manager to be notified on changes.
	 */
	public ControlValidationManager(AbstractValidationManager<String> upstreamValidationManager) {
		this.upstreamValidationManager = upstreamValidationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		upstreamValidationManager.validationStateChanged(DynamicNameExtractionPart.TITLE,
				new ValidationState(controlValidators.get(validationControlDecoration), valid, validationControlDecoration.getDescriptionText()));
	}

	/**
	 * Remove validation control with the given key.
	 *
	 * @param validationKey
	 *            key of the validation control to be removed.
	 */
	public void validationRemoved(String validationKey) {
		upstreamValidationManager.validationStateRemoved(DynamicNameExtractionPart.TITLE, validationKey);
		ValidationControlDecoration<?> toRemove = null;
		for (Entry<ValidationControlDecoration<?>, T> entry : controlValidators.entrySet()) {
			if (entry.getValue().equals(validationKey)) {
				toRemove = entry.getKey();
				break;
			}
		}
		controlValidators.remove(toRemove);
	}

	/**
	 * Removes all {@link ValidationControlDecoration} instances.
	 */
	public void reset() {
		controlValidators.clear();
	}

	/**
	 * Adds an {@link ValidationControlDecoration} instance.
	 *
	 * @param validator
	 *            {@link ValidationControlDecoration} instance to add
	 * @param controlId
	 *            the identifier of the control.
	 */
	public void addValidator(ValidationControlDecoration<?> validator, T controlId) {
		controlValidators.put(validator, controlId);
		validationStateChanged(validator.isValid(), validator);
	}
}
