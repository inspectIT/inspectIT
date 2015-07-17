package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * This matcher filers that classes and methods based on the annotation class name defined in the
 * {@link UnregisteredSensorConfig}. If the annotation supplied is targeting a Class, then all
 * methods that are provided by delegate matchers of a Class that has annotation will be
 * instrumented. Otherwise, if the Annotation is targeting the method, only methods that have this
 * annotation will be instrumented.
 * 
 * @author Ivan Senic
 * 
 */
public class AnnotationMatcher extends AbstractMatcher {

	/**
	 * The {@link IMatcher} delegator object to route the calls of all methods to.
	 */
	private IMatcher delegateMatcher;

	/**
	 * The inheritance checker used to check super-classes and interfaces.
	 */
	private IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * The only constructor which needs a reference to the {@link UnregisteredSensorConfig} instance
	 * of the corresponding configuration.
	 * 
	 * @param inheritanceAnalyzer
	 *            Inheritance analyzer.
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param unregisteredSensorConfig
	 *            The sensor configuration.
	 * @param delegateMatcher
	 *            The {@link IMatcher} delegator object to route the calls of all methods to.
	 * @see AbstractMatcher
	 */
	public AnnotationMatcher(IInheritanceAnalyzer inheritanceAnalyzer, IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig, IMatcher delegateMatcher) {
		super(classPoolAnalyzer, unregisteredSensorConfig);

		this.inheritanceAnalyzer = inheritanceAnalyzer;
		this.delegateMatcher = delegateMatcher;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		return delegateMatcher.compareClassName(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CtMethod> getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException {
		List<CtMethod> matchingMethods = delegateMatcher.getMatchingMethods(classLoader, className);

		boolean classHasAnnotation = checkClassForAnnotation(classLoader, className, unregisteredSensorConfig.getAnnotationClassName());

		if (!classHasAnnotation) {
			List<CtMethod> notMatchingMethods = null;
			for (CtMethod method : matchingMethods) {
				@SuppressWarnings("unchecked")
				List<AttributeInfo> methodAttributesList = method.getMethodInfo2().getAttributes();
				boolean methodHasAnnotation = checkForAnnotation(methodAttributesList, unregisteredSensorConfig.getAnnotationClassName());
				if (!methodHasAnnotation) {
					if (null == notMatchingMethods) {
						notMatchingMethods = new ArrayList<CtMethod>();
					}
					notMatchingMethods.add(method);
				}
			}
			if (null != notMatchingMethods) {
				try {
					matchingMethods.removeAll(notMatchingMethods);
				} catch (UnsupportedOperationException exception) {
					// list not supporting remove, do manually
					List<CtMethod> returnList = new ArrayList<CtMethod>();
					returnList.addAll(matchingMethods);
					returnList.removeAll(notMatchingMethods);
					return returnList;
				}
			}
		}

		return matchingMethods;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CtConstructor> getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		List<CtConstructor> matchingConstructors = delegateMatcher.getMatchingConstructors(classLoader, className);

		boolean classHasAnnotation = checkClassForAnnotation(classLoader, className, unregisteredSensorConfig.getAnnotationClassName());

		if (!classHasAnnotation) {
			List<CtConstructor> notMatchingConstructors = null;
			for (CtConstructor constructor : matchingConstructors) {
				@SuppressWarnings("unchecked")
				List<AttributeInfo> constructorAttributesList = constructor.getMethodInfo2().getAttributes();
				boolean constructorHasAnnotation = checkForAnnotation(constructorAttributesList, unregisteredSensorConfig.getAnnotationClassName());
				if (!constructorHasAnnotation) {
					if (null == notMatchingConstructors) {
						notMatchingConstructors = new ArrayList<CtConstructor>();
					}
					notMatchingConstructors.add(constructor);
				}
			}
			if (null != notMatchingConstructors) {
				try {
					matchingConstructors.removeAll(notMatchingConstructors);
				} catch (UnsupportedOperationException exception) {
					// list not supporting remove, do manually
					List<CtConstructor> returnList = new ArrayList<CtConstructor>();
					returnList.addAll(matchingConstructors);
					returnList.removeAll(notMatchingConstructors);
					return returnList;
				}
			}
		}

		return matchingConstructors;
	}

	/**
	 * Checks if the class has the annotation with the given annotation name. This method will also
	 * check all the superclass and interfaces.
	 * 
	 * @param classLoader
	 *            Class loader.
	 * @param className
	 *            Name of the class to check.
	 * @param annotationClassName
	 *            Annotation name.
	 * @return True if annotation if found, false otherwise.
	 * @throws NotFoundException
	 *             If type is not found.
	 */
	private boolean checkClassForAnnotation(ClassLoader classLoader, String className, String annotationClassName) throws NotFoundException {
		CtClass clazz = classPoolAnalyzer.getClassPool(classLoader).get(className);
		@SuppressWarnings("unchecked")
		List<AttributeInfo> classAttributesList = clazz.getClassFile2().getAttributes();
		if (checkForAnnotation(classAttributesList, annotationClassName)) {
			return true;
		}

		// check every super class
		try {
			Iterator<CtClass> iterator = inheritanceAnalyzer.getSuperclassIterator(classLoader, className);
			while (iterator.hasNext()) {
				@SuppressWarnings("unchecked")
				List<AttributeInfo> superClassAttributeList = iterator.next().getClassFile2().getAttributes();
				if (checkForAnnotation(superClassAttributeList, annotationClassName)) {
					return true;
				}
			}
		} catch (NotFoundException e) { // NOPMD NOCHK
			// ignore
		}

		// check every interface class
		try {
			Iterator<CtClass> iterator = inheritanceAnalyzer.getInterfaceIterator(classLoader, className);
			while (iterator.hasNext()) {
				@SuppressWarnings("unchecked")
				List<AttributeInfo> interfaceClassAttributeList = iterator.next().getClassFile2().getAttributes();
				if (checkForAnnotation(interfaceClassAttributeList, annotationClassName)) {
					return true;
				}
			}
		} catch (NotFoundException e) { // NOPMD NOCHK
			// ignore
		}

		return false;
	}

	/**
	 * Checks if in the list of {@link AttributeInfo} objects exists any
	 * {@link AnnotationsAttribute} object that has information existence of the wanted annotation.
	 * Note that the attribute list should be acquired by {@link ClassFile#getAttributes()},
	 * {@link MethodInfo#getAttributes()} or {@link FieldInfo#getAttributes()} methods.
	 * 
	 * @param attributesList
	 *            List of attributes.
	 * @param annotationClassName
	 *            Name of the annotation to find.
	 * @return True if annotation could be located, false otherwise.
	 */
	private boolean checkForAnnotation(List<AttributeInfo> attributesList, String annotationClassName) {
		for (int i = 0; i < attributesList.size(); i++) {
			AttributeInfo attributeInfo = (AttributeInfo) attributesList.get(i);
			if (attributeInfo instanceof AnnotationsAttribute) {
				AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) attributeInfo;
				Annotation[] annotations = annotationsAttribute.getAnnotations();
				for (int j = 0; j < annotations.length; j++) {
					if (annotations[j].getTypeName().equals(annotationClassName)) {
						return true;
					}
				}
				break;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List<? extends CtBehavior> methods) throws NotFoundException {
		delegateMatcher.checkParameters(methods);
	}

}
