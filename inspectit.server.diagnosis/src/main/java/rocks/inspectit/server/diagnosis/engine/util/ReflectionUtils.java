package rocks.inspectit.server.diagnosis.engine.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;

/**
 * Class contains utility methods to work with reflection API.
 *
 * @author Claudio Waldvogel
 */
public final class ReflectionUtils {

	/**
	 * Private constructor.
	 */
	private ReflectionUtils() {
		throw new UnsupportedOperationException("Must not be instantiated.");
	}

	/**
	 * Represents a callback while searching for annotations on a type. The <code>Visitor</code> is
	 * used to lookup a certain annotation on a type, utilize it, and return something.
	 *
	 * @param <A>
	 *            The annotation to by searched.
	 * @param <T>
	 *            The type where containing the annotation.
	 * @param <R>
	 *            The return type.
	 */
	public interface Visitor<A extends Annotation, T, R> {

		/**
		 * Callback method to process a detected annotation.
		 *
		 * @param annotation
		 *            The annotation type.
		 * @param type
		 *            The type which owns the annotation.
		 * @return Any object.
		 */
		R visit(A annotation, T type);
	}

	/**
	 * Tries to instantiate a class. Intended to avoid boiler plate try/catch code.
	 *
	 * @param clazz
	 *            The class to be instantiated.
	 * @param <T>
	 *            The type of the class
	 * @return A new instance of the the <code>class</code>.
	 */
	public static <T> T tryInstantiate(Class<? extends T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Failed to instantiate clazz.", e);
		}
	}

	/**
	 * Checks if a Class provides a public constructor without any arguments.
	 *
	 * @param clazz
	 *            The class to be checked
	 * @return true if constructor is present, false otherwise.
	 */
	public static boolean hasNoArgsConstructor(Class<?> clazz) {
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (Modifier.isPublic(constructor.getModifiers()) && constructor.getParameterTypes().length == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if given class is annotation with a certain annotation.
	 *
	 * @param clazz
	 *            The class to be checked.
	 * @param annotationClass
	 *            The annotation to be looked up.
	 * @param <T>
	 *            The type of annotation
	 * @return true if annotation is present, false otherwise.
	 */
	public static <T extends Annotation> T findAnnotation(Class<?> clazz, Class<T> annotationClass) {
		return AnnotationUtils.findAnnotation(clazz, annotationClass);
	}

	/**
	 * Visits all fields of a class which are annotated with a certain annotation. The method
	 * utilizes a {@link Visitor} to create the results. The following example shows how all fields,
	 * annotated with SessionVariable, are processed and wrapped in SessionVariableInjection
	 * objects.
	 * <p>
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	List<SessionVariableInjection> injections = ReflectionUtils.visitFieldsAnnotatedWith(SessionVariable.class, clazz, new Visitor<SessionVariable, Field, SessionVariableInjection>() {
	 * 		&#64;Override
	 * 		public SessionVariableInjection visit(SessionVariable annotation, Field field) {
	 * 			return new SessionVariableInjection(annotation.name(), annotation.optional(), field);
	 * 		}
	 * 	});
	 * }
	 * </pre>
	 *
	 * @param annotation
	 *            The type of the annotation.
	 * @param clazz
	 *            The class to be checked.
	 * @param visitor
	 *            The {@link Visitor} to process the annotated field.
	 * @param <A>
	 *            The Annotation type
	 * @param <R>
	 *            The result type of the {@link Visitor}
	 * @return List with elements of type <code>R</code> produced by the {@link Visitor}.
	 */
	public static <A extends Annotation, R> List<R> visitFieldsAnnotatedWith(Class<A> annotation, Class<?> clazz, Visitor<A, Field, R> visitor) {
		List<R> results = new ArrayList<>();
		// TODO should we support inheritance?
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation)) {
				R result = visitor.visit(field.getAnnotation(annotation), field);
				results.add(checkNotNull(result, "Visitor must not return null values!"));
			}
		}
		return results;
	}

	/**
	 * Visits all methods of a class which are annotated with a certain annotation. The method
	 * utilizes a {@link Visitor} to create the results. The following example shows how all
	 * methods, annotated with Action, are processed and wrapped in ActionMethod objects.
	 * <p>
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	List<ActionMethod> actionMethods = ReflectionUtils.visitMethodsAnnotatedWith(Action.class, clazz, new Visitor<Action, Method, ActionMethod>() {
	 * 		&#64;Override
	 * 		public ActionMethod visit(Action annotation, Method method) {
	 * 			return new ActionMethod(method, annotation.resultTag(), annotation.resultQuantity());
	 * 		}
	 * 	});
	 * }
	 * </pre>
	 *
	 * @param annotation
	 *            The type of the annotation.
	 * @param clazz
	 *            The class to be checked.
	 * @param visitor
	 *            The {@link Visitor} to process the annotated field.
	 * @param <A>
	 *            The Annotation type
	 * @param <R>
	 *            The result type of the {@link Visitor}
	 * @return List with elements of type <code>R</code> produced by the {@link Visitor}.
	 */
	public static <A extends Annotation, R> List<R> visitMethodsAnnotatedWith(Class<A> annotation, Class<?> clazz, Visitor<A, Method, R> visitor) {
		List<R> results = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				R result = visitor.visit(method.getAnnotation(annotation), method);
				results.add(checkNotNull(result, "Visitor must not return null values!"));
			}
		}
		return results;
	}
}
