package rocks.inspectit.shared.all.instrumentation.classcache;

/**
 * Interface for all types containing modifiers.
 *
 * @author Ivan Senic
 *
 */
public interface TypeWithModifiers {

	/**
	 * Returns modifiers for this type.
	 *
	 * @return Returns modifiers for this type.
	 * @see Modifiers
	 */
	int getModifiers();

	/**
	 * Sets modifiers for this type.
	 *
	 * @param modifiers
	 *            New value for modifiers
	 * @see Modifiers
	 */
	void setModifiers(int modifiers);
}
