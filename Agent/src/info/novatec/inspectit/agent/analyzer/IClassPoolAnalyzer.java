package info.novatec.inspectit.agent.analyzer;

import javassist.ClassPool;
import javassist.CtConstructor;
import javassist.CtMethod;

/**
 * This interface defines methods to help with the usage of the javassist class pool.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IClassPoolAnalyzer {

	/**
	 * Returns all the methods of a class as an array of {@link CtMethod} objects.
	 * 
	 * @param classLoader
	 *            The class loader of the given class name to successfully search for the class.
	 * @param className
	 *            The name of the class.
	 * @return The array of {@link CtMethod} objects of the passed class name.
	 */
	CtMethod[] getMethodsForClassName(final ClassLoader classLoader, final String className);

	/**
	 * Returns all the constructors of a class as an array of {@link CtConstructor} objects.
	 * 
	 * @param classLoader
	 *            The class loader of the given class name to successfully search for the class.
	 * @param className
	 *            The name of the class.
	 * @return The array of {@link CtConstructor} objects of the passed class name.
	 */
	CtConstructor[] getConstructorsForClassName(final ClassLoader classLoader, final String className);

	/**
	 * Copy the hierarchy from the given classloader and build new classpool objects.
	 * 
	 * @param classLoader
	 *            The class loader.
	 * @return The ClassPool referring to this class loader.
	 */
	ClassPool addClassLoader(final ClassLoader classLoader);

	/**
	 * Returns the {@link ClassPool} which is responsible for the given class loader.
	 * 
	 * @param classLoader
	 *            The class loader.
	 * @return The ClassPool referring to this class loader.
	 */
	ClassPool getClassPool(final ClassLoader classLoader);

}