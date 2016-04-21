package rocks.inspectit.server.diagnosis.engine.tag;

/**
 * Represents the state of a {@link Tag}.
 *
 * @author Claudio Waldvogel
 */
public enum TagState {
	/**
	 * A {@link Tag} was not used as input to other rules and thus did not act as predecessor for other {@link Tag}s.
	 */
	LEAF,

	/**
	 * The {@link Tag} was consumed by a rule and is predecessor for other {@link Tag}s.
	 */
	PARENT
}
