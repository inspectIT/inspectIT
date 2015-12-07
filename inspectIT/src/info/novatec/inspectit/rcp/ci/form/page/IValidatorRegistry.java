package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.rcp.validation.IControlValidationListener;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.Map;
import java.util.Set;

/**
 * Interface describing a control validator registry.
 *
 * @author Alexander Wert
 *
 */
public interface IValidatorRegistry extends IControlValidationListener {

	/**
	 * Returns a Map of {@link ValidatorKey} to {@link ValidationControlDecoration} instances
	 * registered at this registry.
	 *
	 * @return a Map of {@link ValidatorKey} to {@link ValidationControlDecoration} instances.
	 */
	Map<ValidatorKey, ValidationControlDecoration<?>> getValidationControlDecorators();

	/**
	 * Registers a {@link ValidationControlDecoration} instance.
	 *
	 * @param key
	 *            {@link ValidatorKey} for the {@link ValidationControlDecoration} instance to be
	 *            registered
	 * @param validator
	 *            {@link ValidationControlDecoration} instance to be registered
	 */
	void registerValidator(ValidatorKey key, ValidationControlDecoration<?> validator);

	/**
	 * Performs an initials check of the validation state for all registered
	 * {@link ValidationControlDecoration} instances.
	 */
	void performInitialValidation();

	/**
	 * Removes {@link ValidationControlDecoration} instances that are identified by the given set of
	 * {@link ValidatorKey} instances.
	 *
	 * @param keys
	 *            set of keys identifying {@link ValidationControlDecoration} instances to be
	 *            removed
	 */
	void unregisterValidators(Set<ValidatorKey> keys);
}
