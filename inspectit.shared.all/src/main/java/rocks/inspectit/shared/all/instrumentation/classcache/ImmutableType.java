package rocks.inspectit.shared.all.instrumentation.classcache;

import java.util.Set;

/**
 * A class cache model element of the generic type that only provides immutable access. This
 * interface is meaningful to use if it does not matter what kind of element of the class cache is
 * returned.
 *
 * In addition this interface allows to "cast" the underlying type to the correct immutable
 * interface representation. Please note that this is the approach to safeguard the internals of the
 * class cache "against us".
 *
 * For sure everyone could just cast this or do an "instance of" check and cast it to the concrete
 * type. This is NOT an allowed operation (except from within the class cache or its services). We
 * choose this approach as the alternative would be to clone elements returned from the class cache.
 * As we are only using the information internally we deem it OK to just pass the type with an
 * immutable interface.
 *
 * @author Stefan Siegl
 */
public interface ImmutableType extends ImmutableTypeWithAnnotations {

	/**
	 * returns if this type is an annotation.
	 *
	 * @return if this type is an annotation.
	 */
	boolean isAnnotation();

	/**
	 * returns if this type is an interface.
	 *
	 * @return if this type is an interface.
	 */
	boolean isInterface();

	/**
	 * returns if this type is a class.
	 *
	 * @return if this type is a class.
	 */
	boolean isClass();

	/**
	 * casts this to an immutable class interface.
	 *
	 * @return this type cast to the immutable interface.
	 */
	ImmutableClassType castToClass();

	/**
	 * casts this to an immutable annotation type.
	 *
	 * @return this type cast to the annotation type.
	 */
	ImmutableAnnotationType castToAnnotation();

	/**
	 * casts this to an immutable interface type.
	 *
	 * @return this type cast to the interface type.
	 */
	ImmutableInterfaceType castToInterface();

	/**
	 * Returns the fully qualified name of the type.
	 *
	 * @return the fully qualified name of the type.
	 */
	String getFQN();

	/**
	 * Returns information if this type is initialized. Type is considered initialized when complete
	 * byte-code has be analyzed and all fields (method, classes, interfaces, etc) have been set.
	 * The not initialized types are the one which were recognized by analyzing some other class,
	 * thus only FQN is known.
	 *
	 * @return Returns information if this type is initialized.
	 */
	boolean isInitialized();

	/**
	 * returns whether this type has this hash code.
	 *
	 * @param hash
	 *            the given hash code.
	 * @return if this type has this hash code.
	 */
	boolean containsHash(String hash);

	/**
	 * Returns all registered hashes for the given type.
	 *
	 * @return Returns all registered hashes for the given type.
	 */
	Set<String> getHashes();
}
