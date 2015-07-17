package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.spring.logger.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The default implementation of the {@link IInheritanceAnalyzer} interface.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
@Component
public class InheritanceAnalyzer implements IInheritanceAnalyzer {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * Set of logged classes for which the interfaces can not be found in
	 * {@link #addInterfaceExtends(List, CtClass)}.
	 */
	private Set<String> loggedClassesSet = new HashSet<String>();

	/**
	 * The class pool analyzer is used by the {@link #getSuperclassIterator(ClassLoader, String)}
	 * and {@link #getInterfaceIterator(ClassLoader, String)} methods to access the right class.
	 */
	private final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * The default constructor accepting one parameter.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 */
	@Autowired
	public InheritanceAnalyzer(IClassPoolAnalyzer classPoolAnalyzer) {
		this.classPoolAnalyzer = classPoolAnalyzer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<CtClass> getSuperclassIterator(ClassLoader classLoader, String className) throws NotFoundException {
		// retrieve the correct class pool
		ClassPool classPool = classPoolAnalyzer.getClassPool(classLoader);
		CtClass clazz = classPool.get(className);
		return new SuperclassIterator(clazz);
	}

	/**
	 * Iterator implementation to iterate over all superclasses of a class.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static class SuperclassIterator implements Iterator<CtClass> {

		/**
		 * The current super class.
		 */
		private CtClass superClass;

		/**
		 * The iterator has to be initialized with a {@link CtClass} object where all super classes
		 * are taken from.
		 * 
		 * @param clazz
		 *            The root class.
		 * @throws NotFoundException
		 *             This exception is thrown if a class is not found from within Javassist.
		 */
		public SuperclassIterator(CtClass clazz) throws NotFoundException {
			superClass = clazz;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			try {
				return superClass.getSuperclass() != null;
			} catch (NotFoundException e) {
				return false;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public CtClass next() {
			try {
				superClass = superClass.getSuperclass();
			} catch (NotFoundException e) {
				throw new NoSuchElementException(e.getMessage()); // NOPMD
			}

			if (null == superClass) {
				throw new NoSuchElementException();
			}

			return superClass;
		}

		/**
		 * {@inheritDoc}
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<CtClass> getInterfaceIterator(ClassLoader classLoader, String className) throws NotFoundException {
		// retrieve the correct class pool
		ClassPool classPool = classPoolAnalyzer.getClassPool(classLoader);
		CtClass ctClass = classPool.get(className);

		List<CtClass> interfaces = new ArrayList<CtClass>();
		while (null != ctClass) {
			addInterfaceExtends(interfaces, ctClass);
			ctClass = ctClass.getSuperclass();
		}

		return interfaces.iterator();
	}

	/**
	 * Adds all interfaces to this list, including the one which are extended by other interfaces.
	 * 
	 * @param interfaces
	 *            The list to add the interfaces to.
	 * @param ctClass
	 *            The class to get the interfaces from.
	 * @throws NotFoundException
	 *             This exception is thrown if a class is not found from within Javassist.
	 */
	private void addInterfaceExtends(List<CtClass> interfaces, CtClass ctClass) throws NotFoundException {
		String[] ifs = null;
		try {
			ifs = ctClass.getClassFile2().getInterfaces();
		} catch (Exception e) {
			String className = ctClass.getName();
			if (loggedClassesSet.add(className)) {
				log.warn("Not possible to load interfaces for class " + className + ".");
			}
		}
		if (null != ifs) {
			int num = ifs.length;
			CtClass[] ctClasses = new CtClass[num];
			for (int i = 0; i < num; ++i) {
				try {
					ctClasses[i] = ctClass.getClassPool().get(ifs[i]);
				} catch (NotFoundException e) { // NOPMD NOCHK
					// ignore
				}
			}

			for (int i = 0; i < ctClasses.length; i++) {
				if (null != ctClasses[i]) {
					CtClass interfaceCtClass = ctClasses[i];
					interfaces.add(interfaceCtClass);
					addInterfaceExtends(interfaces, interfaceCtClass);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInterface(String className, ClassLoader classLoader) throws NotFoundException {
		// retrieve the correct class pool
		ClassPool classPool = classPoolAnalyzer.getClassPool(classLoader);

		CtClass actClass = classPool.get(className);
		return actClass.isInterface();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean implementsInterface(String className, ClassLoader classLoader, String interfaceName) {
		try {
			for (Iterator<CtClass> interfaceIterator = getInterfaceIterator(classLoader, className); interfaceIterator.hasNext();) {
				CtClass ctInterface = interfaceIterator.next();
				String name = ctInterface.getName();
				if (name.equalsIgnoreCase(interfaceName)) {
					return true;
				}
			}
		} catch (NotFoundException e) {
			return false;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean subclassOf(String className, String superClassName, ClassPool classPool) {
		try {
			CtClass superClass = classPool.get(superClassName);
			CtClass clazz = classPool.get(className);

			if (clazz.subclassOf(superClass)) {
				return true;
			}
		} catch (NotFoundException e) {
			return false;
		}
		return false;
	}
}
