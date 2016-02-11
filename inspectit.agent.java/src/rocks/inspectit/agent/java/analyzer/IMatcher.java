package info.novatec.inspectit.agent.analyzer;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * This interface is used for all implementations of the configuration matching system. There are
 * different ones available because of the possibility to define sensors as superclasses or having a
 * wildcard expression in its string.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IMatcher {

	/**
	 * This method compares the class name. Returns <code>true</code> if the comparison is
	 * successful.
	 * 
	 * @param classLoader
	 *            The class loader of the given class.
	 * @param className
	 *            The name of the class to load.
	 * @return <code>true</code> if the passed class name matches the defined one.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException;

	/**
	 * Returns all the matching methods of the passed class.
	 * 
	 * @param classLoader
	 *            The class loader of the given class.
	 * @param className
	 *            The name of the class to load.
	 * @return A {@link List} containing all the {@link CtMethod} objects which matched successfully
	 *         to this implementation.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	List<CtMethod> getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException;

	/**
	 * Returns all the matching constructors of the passed class.
	 * 
	 * @param classLoader
	 *            The class loader of the given class.
	 * @param className
	 *            The name of the class to load.
	 * @return A {@link List} containing all the {@link CtConstructor} objects which matched
	 *         successfully to this implementation.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	List<CtConstructor> getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException;

	/**
	 * This method checks all the parameters of the given {@link List} of {@link CtBehavior} objects
	 * if they match the configuration. If one method object is found which has parameters not
	 * matching to the configuration, it is removed from the list.
	 * 
	 * @param methods
	 *            The {@link List} of {@link CtBehavior} objects to check.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	void checkParameters(List<? extends CtBehavior> methods) throws NotFoundException;

}
