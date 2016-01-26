package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Set;

/**
 * A class cache model element of a type that can have annotation, which only provides immutable
 * access.
 *
 * @author Stefan Siegl
 * @author Ivan Senic
 */
public interface ImmutableTypeWithAnnotations {

	/**
	 * Returns immutable annotations as an unmodifiableSet.
	 *
	 * @return Returns immutable annotations as an unmodifiableSet.
	 */
	Set<? extends ImmutableAnnotationType> getImmutableAnnotations();

	/**
	 * Return if type is {@link ImmutableType}. If <code>true</code> {@link #castToType()} can be
	 * used safely.
	 *
	 * @return Return <code>true</code> if type is {@link ImmutableType}.
	 */
	boolean isType();

	/**
	 * Cast to the {@link ImmutableType}. Not that this cast works only if {@link #isType()} returns
	 * <code>true</code>.
	 *
	 * @return {@link ImmutableType}
	 */
	ImmutableType castToType();

	/**
	 * Return if type is {@link ImmutableMethodType}. If <code>true</code>
	 * {@link #castToMethodType()} can be used safely.
	 *
	 * @return Return <code>true</code> if type is {@link ImmutableMethodType}.
	 */
	boolean isMethodType();

	/**
	 * Cast to the {@link ImmutableMethodType}. Not that this cast works only if
	 * {@link #isMethodType()} returns <code>true</code>.
	 *
	 * @return {@link ImmutableMethodType}
	 */
	ImmutableMethodType castToMethodType();

}
