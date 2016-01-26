package rocks.inspectit.server.instrumentation.config.filter;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableMethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableTypeWithMethods;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.Modifiers;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.PatternFactory;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;

/**
 * Filter that filters the class types and method type based on the given assignment and vice versa.
 *
 * @see #filter(MethodSensorAssignment, ClassType)
 * @see #matches(MethodSensorAssignment, MethodType)
 *
 * @author Ivan Senic
 *
 */
public class MethodSensorAssignmentFilter extends ClassSensorAssignmentFilter {

	/**
	 * Checks if the {@link MethodType} matches the the {@link MethodSensorAssignment}.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment}.
	 * @param methodType
	 *            method type to check
	 * @return <code>true</code> if the method matches the {@link MethodSensorAssignment}
	 */
	public boolean matches(MethodSensorAssignment methodSensorAssignment, ImmutableMethodType methodType) {
		if (!matchesMethodName(methodSensorAssignment, methodType)) {
			return false;
		}

		if (!matchesParameters(methodSensorAssignment, methodType)) {
			return false;
		}

		if (!matchesModifiers(methodSensorAssignment, methodType)) {
			return false;
		}

		if (!matchesAnnotation(methodSensorAssignment, methodType)) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if the method name matches the {@link MethodSensorAssignment}.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment}.
	 * @param methodType
	 *            method type to check
	 * @return <code>true</code> if the method name matches the {@link MethodSensorAssignment}
	 */
	private boolean matchesMethodName(MethodSensorAssignment methodSensorAssignment, ImmutableMethodType methodType) {
		if (methodSensorAssignment.isConstructor()) {
			return MethodType.Character.CONSTRUCTOR.equals(methodType.getMethodCharacter());
		} else {
			// don't instrument constructors when we have a method assignment
			if (!MethodType.Character.METHOD.equals(methodType.getMethodCharacter())) {
				return false;
			}

			IMatchPattern pattern = PatternFactory.getPattern(methodSensorAssignment.getMethodName());
			return pattern.match(methodType.getName());
		}
	}

	/**
	 * Checks if the modifiers matches the {@link MethodSensorAssignment}.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment}.
	 * @param methodType
	 *            method type to check
	 * @return <code>true</code> if the method modifiers matches the {@link MethodSensorAssignment}
	 */
	private boolean matchesModifiers(MethodSensorAssignment methodSensorAssignment, ImmutableMethodType methodType) {
		if (Modifiers.isPublic(methodType.getModifiers())) {
			return methodSensorAssignment.isPublicModifier();
		} else if (Modifiers.isProtected(methodType.getModifiers())) {
			return methodSensorAssignment.isProtectedModifier();
		} else if (Modifiers.isPrivate(methodType.getModifiers())) {
			return methodSensorAssignment.isPrivateModifier();
		} else {
			return methodSensorAssignment.isDefaultModifier();
		}
	}

	/**
	 * Checks if the parameters matches the {@link MethodSensorAssignment}.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment}.
	 * @param methodType
	 *            method type to check
	 * @return <code>true</code> if the method parameters matches the {@link MethodSensorAssignment}
	 */
	private boolean matchesParameters(MethodSensorAssignment methodSensorAssignment, ImmutableMethodType methodType) {
		if (null == methodSensorAssignment.getParameters()) {
			return true;
		}

		if (methodSensorAssignment.getParameters().size() == methodType.getParameters().size()) {
			int size = methodSensorAssignment.getParameters().size();

			for (int i = 0; i < size; i++) {
				String parameterPattern = methodSensorAssignment.getParameters().get(i);
				IMatchPattern pattern = PatternFactory.getPattern(parameterPattern);

				if (!pattern.match(methodType.getParameters().get(i))) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks if the annotation matches the {@link MethodSensorAssignment}.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment}.
	 * @param methodType
	 *            method type to check
	 * @return <code>true</code> if the method parameters matches the {@link MethodSensorAssignment}
	 */
	private boolean matchesAnnotation(MethodSensorAssignment methodSensorAssignment, ImmutableMethodType methodType) {
		// first check if annotation is defined at all
		if (StringUtils.isEmpty(methodSensorAssignment.getAnnotation())) {
			return true;
		}

		ImmutableTypeWithMethods type = methodType.getImmutableClassOrInterfaceType();
		// Safety check for class type
		// it has to be class type
		if (!type.isClass()) {
			return false;
		}

		// check if class matches, if yes, then all methods match as well
		boolean classMatches = super.matchesAnnotation(methodSensorAssignment, type.castToClass());
		if (classMatches) {
			return true;
		}

		IMatchPattern pattern = PatternFactory.getPattern(methodSensorAssignment.getAnnotation());
		// if not check if the method has annotation
		return checkAnnotations(methodType.getImmutableAnnotations(), pattern);
	}

}
