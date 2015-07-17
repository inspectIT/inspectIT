package info.novatec.inspectit.agent.analyzer;

import java.util.Iterator;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * The inheritance analyzer is a used to identify classes which have to be instrumented by inspectIT
 * but aren't directly specified in the configuration file but through their superclass.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IInheritanceAnalyzer {

	/**
	 * Returns an iterator over all superclasses of a class.
	 * 
	 * @param classLoader
	 *            The class loader of the class.
	 * @param className
	 *            The name of the class.
	 * @return An {@link Iterator} of all superclasses.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	Iterator<CtClass> getSuperclassIterator(ClassLoader classLoader, String className) throws NotFoundException;

	/**
	 * Returns an iterator over all implemented interfaces of a class.
	 * 
	 * @param classLoader
	 *            The class loader of the class.
	 * @param className
	 *            The name of the class.
	 * @return An {@link Iterator} of all interfaces.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	Iterator<CtClass> getInterfaceIterator(ClassLoader classLoader, String className) throws NotFoundException;

	/**
	 * Returns <code>true</code> if the class name defines an interface.
	 * 
	 * @param className
	 *            The name of the class.
	 * @param classLoader
	 *            The class loader of the class.
	 * @return true if given class is an interface.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	boolean isInterface(String className, ClassLoader classLoader) throws NotFoundException;

	/**
	 * Returns <code>true</code> if the passed class implements the interface.
	 * 
	 * @param className
	 *            The name of the class.
	 * @param classLoader
	 *            The class loader of the class.
	 * @param interfaceName
	 *            The name of the interface.
	 * @return <code>true</code> if given class implements the interface.
	 */
	boolean implementsInterface(String className, ClassLoader classLoader, String interfaceName);

	/**
	 * Checks whether the passed className is a direct or indirect subclass of superClassName.
	 * 
	 * @param className
	 *            The fully qualified name of the class to check for.
	 * @param superClassName
	 *            The fully qualified class name of the superclass.
	 * @param classPool
	 *            The class pool where to search for.
	 * 
	 * @return Whether the passed className is a direct or indirect subclass of superClassName.
	 */
	boolean subclassOf(String className, String superClassName, ClassPool classPool);
}