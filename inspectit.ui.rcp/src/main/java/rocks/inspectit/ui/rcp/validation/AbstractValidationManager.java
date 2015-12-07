package rocks.inspectit.ui.rcp.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class encapsulating common functionality of validation managers.
 *
 * @author Alexander Wert
 * @param <K>
 *            the type of the validation key.
 */
public abstract class AbstractValidationManager<K> {

	/**
	 * Multi-map of keys to corresponding validation states. This Map holds ONLY INVALID state.
	 * Valid states are immediately removed from this map.
	 */
	private final Map<K, Set<ValidationState>> validationErrorStates = new IdentityHashMap<>();

	/**
	 * Processes a change on the validation state.
	 *
	 * @param key
	 *            The key for which the validation state has changed.
	 * @param newState
	 *            the new validation state.
	 */
	public void validationStateChanged(K key, ValidationState newState) {
		Set<ValidationState> states = validationErrorStates.get(key);
		if (newState.isValid()) {
			if (null == states) {
				states = Collections.emptySet();
			}
			boolean removed = states.remove(newState);
			if (removed) {
				if (states.isEmpty()) {
					hideMessage(key);
				} else {
					showMessage(key, states);
				}

				notifyUpstream(key, states);
			}
		} else {
			if (null == states) {
				states = new HashSet<ValidationState>();
				validationErrorStates.put(key, states);
			}
			// replace state instance
			states.remove(newState);
			states.add(newState);
			showMessage(key, states);
			notifyUpstream(key, states);
		}
	}

	/**
	 * Processes a removal of a validation element.
	 *
	 * @param key
	 *            the key for which the validation state has been removed
	 * @param validationStateId
	 *            the identifier of the validated element
	 */
	public void validationStateRemoved(K key, Object validationStateId) {
		validationStateChanged(key, new ValidationState(validationStateId, true, ""));
	}

	/**
	 * Removes all validation states for the given key.
	 *
	 * @param key
	 *            the key for the validation states must be removed
	 */
	public void validationStatesRemoved(K key) {
		hideMessage(key);
		validationErrorStates.remove(key);
		notifyUpstream(key, Collections.<ValidationState> emptySet());
	}

	/**
	 * Removes all validation states.
	 */
	public void allValidationStatesRemoved() {
		Set<K> keysToRemove = new HashSet<>(validationErrorStates.keySet());
		for (K key : keysToRemove) {
			validationStatesRemoved(key);
		}
	}

	/**
	 * Notifies an upstream validation manager if there is any.
	 *
	 * @param key
	 *            the key of the validation state
	 * @param states
	 *            the validation states for the given key
	 */
	protected abstract void notifyUpstream(K key, Set<ValidationState> states);

	/**
	 * Shows validation message for the given key and states.
	 *
	 * @param key
	 *            the key for which to show the validation message
	 * @param states
	 *            the states providing the detailed validation information
	 */
	protected abstract void showMessage(K key, Set<ValidationState> states);

	/**
	 * Hides the validation message for the given key.
	 *
	 * @param key
	 *            the key to hide the validation message for
	 */
	protected abstract void hideMessage(K key);

	/**
	 * Returns the set of validation states for the given key.
	 *
	 * @param key
	 *            the key to retrieve the validation states for.
	 * @return Returns the set of validation states for the given key.
	 */
	protected Set<ValidationState> getValidationErrorStates(K key) {
		return validationErrorStates.get(key);
	}

}
