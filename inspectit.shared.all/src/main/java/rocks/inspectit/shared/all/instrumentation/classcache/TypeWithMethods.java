package rocks.inspectit.shared.all.instrumentation.classcache;


import java.util.Set;

/**
 * A type that can have methods.
 * 
 * @author Stefan Siegl
 */
public interface TypeWithMethods extends ImmutableTypeWithMethods {
	
	/**
	 * Adds a method that this type contains and ensures that the back-reference on the referred
	 * entity is set as well.
	 * 
	 * @param type
	 *            the method that is defined in this type.
	 */
	void addMethod(MethodType type);

	/**
	 * Adds a method that this type contains WITHOUT setting the back-reference. Please be aware
	 * that this method should only be called internally as this might mess up the bi-directional
	 * structure.
	 * 
	 * @param type
	 *            the method that is defined in this type.
	 */
	void addMethodNoBidirectionalUpdate(MethodType type);

	/**
	 * Gets {@link #methods} as an unmodifiableSet. If you want to add something to the list, use
	 * the provided adders, as they ensure that the bi-directional links are created.
	 * 
	 * @return {@link #methods}
	 */
	Set<MethodType> getMethods();
}
