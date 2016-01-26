package rocks.inspectit.server.instrumentation.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableAnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableInterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableMethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableTypeWithAnnotations;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableTypeWithMethods;
import rocks.inspectit.shared.all.pattern.WildcardMatchPattern;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;

/**
 * This class helps in getting the class types that fit the class sensor assignment.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ClassCacheSearchNarrower {

	/**
	 * Helps in the search for the {@link ImmutableClassType} that fit the given
	 * {@link AbstractClassSensorAssignment}.
	 * <p>
	 * Search order is following:
	 * <p>
	 * 1. If direct class/interface/super-class name is given, then by name <br>
	 * 2. If annotation is given, then by annotation<br>
	 * 3. If nothing of above, then by the wild card name search.
	 *
	 * @param classCache
	 *            {@link ClassCache} to look in.
	 * @param classSensorAssignment
	 *            {@link AbstractClassSensorAssignment}
	 * @return All initialized {@link ImmutableClassType} that might match given
	 *         {@link AbstractClassSensorAssignment}. Note that this is only narrow process, full
	 *         check must be performed on the returned results.
	 */
	public Collection<? extends ImmutableClassType> narrowByClassSensorAssignment(ClassCache classCache, AbstractClassSensorAssignment<?> classSensorAssignment) {
		if (!WildcardMatchPattern.isPattern(classSensorAssignment.getClassName())) {
			// if we don't have a pattern that just load
			return narrowByNameSearch(classCache, classSensorAssignment.getClassName(), classSensorAssignment.isInterf(), classSensorAssignment.isSuperclass());
		}

		if (null != classSensorAssignment.getAnnotation()) {
			return narrowByAnnotationSearch(classCache, classSensorAssignment.getAnnotation());
		}

		// if nothing works then we have wild-card search in name
		return narrowByNameSearch(classCache, classSensorAssignment.getClassName(), classSensorAssignment.isInterf(), classSensorAssignment.isSuperclass());
	}

	/**
	 * Search by the class name defined in the {@link AbstractClassSensorAssignment} and includes
	 * the interface/super-class options as well.
	 *
	 * @param classCache
	 *            {@link ClassCache} to look in.
	 * @param className
	 *            Class name to search for
	 * @param isInterface
	 *            if class name is related to interface
	 * @param isSuperClass
	 *            if class name is related to superclass
	 * @return All initialized {@link ImmutableClassType} that might match given
	 *         {@link MethodSensorAssignment}. Note that this is only narrow process, full check
	 *         must be performed on the returned results.
	 */
	private Collection<? extends ImmutableClassType> narrowByNameSearch(ClassCache classCache, String className, boolean isInterface, boolean isSuperClass) {
		if (isInterface) {
			// if definition is for interface, load all matching interfaces
			Collection<? extends ImmutableInterfaceType> interfaceTypes = classCache.getLookupService().findInterfaceTypesByPattern(className, false);

			if (CollectionUtils.isEmpty(interfaceTypes)) {
				return Collections.emptyList();
			}

			// then load initialized realizing classes from all interfaces
			Collection<ImmutableClassType> results = new HashSet<>();
			for (ImmutableInterfaceType interfaceType : interfaceTypes) {
				collectClassesFromInterfaceAndSubInterfaces(results, interfaceType);
			}
			return results;
		} else if (isSuperClass) {
			// if definition is for superclass, load all matching classes
			Collection<? extends ImmutableClassType> superClassTypes = classCache.getLookupService().findClassTypesByPattern(className, false);

			if (CollectionUtils.isEmpty(superClassTypes)) {
				return Collections.emptyList();
			}

			// then load initialized sub-classes from all super types
			Collection<ImmutableClassType> results = new HashSet<>();
			for (ImmutableClassType superClassType : superClassTypes) {
				collectClassesFromSubClasses(results, superClassType);
			}
			return results;
		} else {
			return classCache.getLookupService().findClassTypesByPattern(className, true);
		}
	}

	/**
	 * Search by the annotation defined in the assignment.
	 *
	 * @param classCache
	 *            {@link ClassCache} to look in.
	 * @param annotation
	 *            Annotation FQN
	 * @return All initialized {@link ImmutableClassType} that might match given annotation. Note
	 *         that this is only narrow process, full check must be performed on the returned
	 *         results.
	 */
	private Collection<? extends ImmutableClassType> narrowByAnnotationSearch(ClassCache classCache, String annotation) {
		if (null == annotation) {
			return Collections.emptyList();
		}

		Collection<? extends ImmutableAnnotationType> annotationTypes = classCache.getLookupService().findAnnotationTypesByPattern(annotation, false);

		if (CollectionUtils.isEmpty(annotationTypes)) {
			return Collections.emptyList();
		}

		// then load initialized sub-classes from all super types
		Collection<ImmutableClassType> results = new HashSet<>();
		for (ImmutableAnnotationType annotationType : annotationTypes) {
			for (ImmutableTypeWithAnnotations typeWithAnnotations : annotationType.getImmutableAnnotatedTypes()) {

				// processing if it's type
				if (typeWithAnnotations.isType()) {
					ImmutableType immutableType = typeWithAnnotations.castToType();

					// if we have a type then take this class and all sub classes
					// if it's interface then all implementing classes
					if (immutableType.isClass()) {
						ImmutableClassType immutableClassType = immutableType.castToClass();
						if (immutableClassType.isInitialized()) {
							results.add(immutableClassType);
						}
						collectClassesFromSubClasses(results, immutableClassType);
					} else if (immutableType.isInterface()) {
						ImmutableInterfaceType immutableInterfaceType = immutableType.castToInterface();
						collectClassesFromInterfaceAndSubInterfaces(results, immutableInterfaceType);
					}
				}

				// processing if it's method
				if (typeWithAnnotations.isMethodType()) {
					ImmutableMethodType immutableMethodType = typeWithAnnotations.castToMethodType();
					ImmutableTypeWithMethods classOrInterfaceType = immutableMethodType.getImmutableClassOrInterfaceType();

					if (classOrInterfaceType.isClass() && classOrInterfaceType.isInitialized()) {
						results.add(classOrInterfaceType.castToClass());
					}
				}
			}
		}

		return results;
	}

	/**
	 * Collects all realizing classes that implement given interface or any of its sub-interfaces
	 * and adds them to the given results list. This method is recursive.
	 *
	 * @param results
	 *            List to store classes to.
	 * @param interfaceType
	 *            Type to check.
	 */
	private void collectClassesFromInterfaceAndSubInterfaces(Collection<ImmutableClassType> results, ImmutableInterfaceType interfaceType) {
		for (ImmutableClassType classType : interfaceType.getImmutableRealizingClasses()) {
			if (classType.isInitialized()) {
				results.add(classType);
			}
			collectClassesFromSubClasses(results, classType);
		}

		for (ImmutableInterfaceType superInterfaceType : interfaceType.getImmutableSubInterfaces()) {
			collectClassesFromInterfaceAndSubInterfaces(results, superInterfaceType);
		}
	}

	/**
	 * Collects all realizing classes that are sub-class of given class type or any of its
	 * sub-classes and adds them to the given results list. This method is recursive.
	 *
	 * @param results
	 *            List to store classes to.
	 * @param classType
	 *            Type to check.
	 */
	private void collectClassesFromSubClasses(Collection<ImmutableClassType> results, ImmutableClassType classType) {
		for (ImmutableClassType subClassType : classType.getImmutableSubClasses()) {
			if (subClassType.isInitialized()) {
				results.add(subClassType);
			}
			collectClassesFromSubClasses(results, subClassType);
		}
	}

}
