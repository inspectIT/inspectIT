package rocks.inspectit.server.instrumentation.config.filter;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableAbstractInterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableAnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableInterfaceType;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.PatternFactory;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

/**
 * Filter that filters the class types based on the given assignment and vice versa.
 *
 * @see #matches(AbstractClassSensorAssignment, ImmutableClassType)
 * @author Ivan Senic
 *
 */
public class ClassSensorAssignmentFilter {

	/**
	 * Tests if the given {@link ImmutableClassType} matches the class sensor assignment. Same as calling {@link #matches(AbstractClassSensorAssignment, ImmutableClassType, true)}.
	 *
	 * @param classSensorAssignment
	 *            assignment.
	 * @param classType
	 *            classType
	 * @return <code>true</code> if class type matches the assignment.
	 */
	public boolean matches(AbstractClassSensorAssignment<?> classSensorAssignment, ImmutableClassType classType) {
		return matches(classSensorAssignment, classType, true);
	}

	/**
	 * Tests if the given {@link ImmutableClassType} matches the class sensor assignment. This
	 * method allows option for non-checking the annotation, which can be helpful with method sensor
	 * assignments as the annotation matching must be also done on the method level.
	 *
	 * @param classSensorAssignment
	 *            assignment.
	 * @param classType
	 *            classType
	 * @param checkAnnotation
	 *            if annotation matching has to be checked on the {@link ClassType}
	 * @return <code>true</code> if class type matches the assignment.
	 */
	public boolean matches(AbstractClassSensorAssignment<?> classSensorAssignment, ImmutableClassType classType, boolean checkAnnotation) {
		if (!matchesClassName(classSensorAssignment, classType)) {
			return false;
		}

		if (checkAnnotation && !matchesAnnotation(classSensorAssignment, classType)) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if the {@link AbstractClassSensorAssignment} matches the given
	 * {@link ImmutableClassType} in terms of name specified in the
	 * {@link AbstractClassSensorAssignment}.
	 *
	 * @param classSensorAssignment
	 *            Assignment defining the name.
	 * @param classType
	 *            Type to check.
	 * @return <code>true</code> if class/super-class/interface match the name pattern as specified
	 *         in the {@link AbstractClassSensorAssignment}.
	 */
	private boolean matchesClassName(AbstractClassSensorAssignment<?> classSensorAssignment, ImmutableClassType classType) {
		IMatchPattern pattern = PatternFactory.getPattern(classSensorAssignment.getClassName());

		if (classSensorAssignment.isSuperclass()) {
			// TODO INSPECTIT-2091
			// what about a change to include the class itself so when I specify super-class it
			// means this class and all sub-classes? if yes, change functional assignments to match

			// match any superclass
			for (ImmutableClassType superClassType : classType.getImmutableSuperClasses()) {
				if (checkClassAndSuperClassForName(superClassType, pattern)) {
					return true;
				}
			}
		} else if (classSensorAssignment.isInterf()) {
			// match any interface
			return checkClassAndSuperClassForInterface(classType, pattern);
		} else {
			// else match this class
			return pattern.match(classType.getFQN());
		}

		return false;
	}

	/**
	 * Check if the class type or any of its super-classes matches the given name pattern.
	 *
	 * @param classType
	 *            Type to check.
	 * @param namePattern
	 *            Pattern to test FQN with.
	 * @return <code>true</code> if class or any of the super-classes match the name pattern.
	 */
	private boolean checkClassAndSuperClassForName(ImmutableClassType classType, IMatchPattern namePattern) {
		if (namePattern.match(classType.getFQN())) {
			return true;
		}

		for (ImmutableClassType superClassType : classType.getImmutableSuperClasses()) {
			if (checkClassAndSuperClassForName(superClassType, namePattern)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if the interface type or any of its super-interfaces matches the given name pattern.
	 *
	 * @param interfaceType
	 *            Type to check.
	 * @param namePattern
	 *            Pattern to test FQN with.
	 * @return <code>true</code> if interface or any of the super-interface match the name pattern.
	 */
	private boolean checkInterfaceAndSuperInterfacesForName(ImmutableInterfaceType interfaceType, IMatchPattern namePattern) {
		if (namePattern.match(interfaceType.getFQN())) {
			return true;
		}

		for (ImmutableInterfaceType superInterfaceType : interfaceType.getImmutableSuperInterfaces()) {
			if (checkInterfaceAndSuperInterfacesForName(superInterfaceType, namePattern)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the {@link AbstractClassSensorAssignment} matches the given
	 * {@link ImmutableClassType} in terms of annotation specified in the
	 * {@link AbstractClassSensorAssignment}.
	 *
	 * @param classSensorAssignment
	 *            Assignment defining the annotations.
	 * @param classType
	 *            Type to check.
	 * @return <code>true</code> if class or any of it super-classes or realized interfaces are
	 *         implementing the specified annotation.
	 */
	protected boolean matchesAnnotation(AbstractClassSensorAssignment<?> classSensorAssignment, ImmutableClassType classType) {
		// only check if we have annotation set
		if (StringUtils.isEmpty(classSensorAssignment.getAnnotation())) {
			return true;
		}

		IMatchPattern pattern = PatternFactory.getPattern(classSensorAssignment.getAnnotation());

		// check class and super classes first
		if (checkClassAndSuperClassForAnnotation(classType, pattern)) {
			return true;
		}

		// then all interfaces.
		for (ImmutableAbstractInterfaceType interfaceType : classType.getImmutableRealizedInterfaces()) {
			if (interfaceType.isInterface() && checkInterfaceAndSuperInterfaceForAnnotation(interfaceType.castToInterface(), pattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given {@link ImmutableClassType} or any of it's super classes implement an
	 * interface that matches given annotation pattern.
	 *
	 * @param classType
	 *            Type to check.
	 * @param pattern
	 *            Pattern to test interface FQNs with.
	 * @return <code>true</code> if class or any super-classes implements interface that matches the
	 *         pattern.
	 */
	private boolean checkClassAndSuperClassForInterface(ImmutableClassType classType, IMatchPattern pattern) {
		// match any interface
		for (ImmutableAbstractInterfaceType interfaceType : classType.getImmutableRealizedInterfaces()) {
			if (interfaceType.isInterface() && checkInterfaceAndSuperInterfacesForName(interfaceType.castToInterface(), pattern)) {
				return true;
			}
		}

		for (ImmutableClassType superClassType : classType.getImmutableSuperClasses()) {
			if (checkClassAndSuperClassForInterface(superClassType, pattern)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given {@link ImmutableClassType} or any of it's super classes have an
	 * annotation that matches given annotation pattern.
	 *
	 * @param classType
	 *            Type to check.
	 * @param annotationPattern
	 *            Pattern to test annotation FQNs with.
	 * @return <code>true</code> if class or any super-classes have annotation that matches the
	 *         pattern.
	 */
	private boolean checkClassAndSuperClassForAnnotation(ImmutableClassType classType, IMatchPattern annotationPattern) {
		if (checkAnnotations(classType.getImmutableAnnotations(), annotationPattern)) {
			return true;
		}

		for (ImmutableClassType superClassType : classType.getImmutableSuperClasses()) {
			if (checkClassAndSuperClassForAnnotation(superClassType, annotationPattern)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given {@link ImmutableInterfaceType} or any of it's super interfaces have an
	 * annotation that matches given annotation pattern.
	 *
	 * @param interfaceType
	 *            Type to check.
	 * @param annotationPattern
	 *            Pattern to test annotation FQNs with.
	 * @return <code>true</code> if interface or any super-interfaces have annotation that matches
	 *         the pattern.
	 */
	private boolean checkInterfaceAndSuperInterfaceForAnnotation(ImmutableInterfaceType interfaceType, IMatchPattern annotationPattern) {
		if (checkAnnotations(interfaceType.getImmutableAnnotations(), annotationPattern)) {
			return true;
		}

		for (ImmutableInterfaceType superInterfaceType : interfaceType.getImmutableSuperInterfaces()) {
			if (checkInterfaceAndSuperInterfaceForAnnotation(superInterfaceType, annotationPattern)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if any of given {@link ImmutableAnnotationType}s matches the given pattern.
	 *
	 * @param annotations
	 *            Collection of annotations.
	 * @param pattern
	 *            Pattern to test annotation FQNs with.
	 * @return <code>true</code> if any of given annotations matches the pattern.
	 */
	protected boolean checkAnnotations(Collection<? extends ImmutableAnnotationType> annotations, IMatchPattern pattern) {
		for (ImmutableAnnotationType annotationType : annotations) {
			if (pattern.match(annotationType.getFQN())) {
				return true;
			}
		}
		return false;
	}
}
