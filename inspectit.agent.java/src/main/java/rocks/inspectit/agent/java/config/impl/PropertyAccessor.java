package rocks.inspectit.agent.java.config.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.config.PropertyAccessException;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPath;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class is used to programmatically build the path to access a specific method parameter or a
 * field of a class.
 *
 * @author Patrice Bouillet
 * @author Stefan Siegl
 *
 */
@Component
public class PropertyAccessor implements IPropertyAccessor {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * Static null value for return value capturing in case the returned value was <code>null</code>
	 * .
	 */
	private static final String NULL_VALUE = "null";

	/**
	 * An array containing the names of all methods that might be called by the PropertyAccessor.
	 * Names should not include the brackets.
	 */
	private static final String[] ALLOWED_METHODS = new String[] { "size", "length" };

	/**
	 * {@inheritDoc}
	 */
	public String getPropertyContent(PropertyPathStart propertyPathStart, Object clazz, Object[] parameters, Object returnValue) throws PropertyAccessException {
		if (null == propertyPathStart) {
			throw new PropertyAccessException("Property path start cannot be null!");
		}

		if (null == propertyPathStart.getContentType()) {
			throw new PropertyAccessException("Content type is not defined.");
		}

		switch (propertyPathStart.getContentType()) {
		case FIELD:
			if (null == clazz) {
				throw new PropertyAccessException("Class reference cannot be null!");
			}
			return getPropertyContent(propertyPathStart.getPathToContinue(), clazz);
		case PARAM:
			if (null == parameters) {
				throw new PropertyAccessException("Parameter array reference cannot be null!");
			}

			if (propertyPathStart.getSignaturePosition() >= parameters.length) {
				throw new PropertyAccessException("Signature position out of range!");
			}

			return getPropertyContent(propertyPathStart.getPathToContinue(), parameters[propertyPathStart.getSignaturePosition()]);
		case RETURN:
			// we will not throw an exception here as the return value of a method can sometimes be
			// null. If we throw an exception, this will lead to the removal of the path and thus no
			// return value of this property accessor will be captured afterwards.
			if (null == returnValue) {
				return NULL_VALUE;
			} else {
				return getPropertyContent(propertyPathStart.getPathToContinue(), returnValue);
			}
		default:
			throw new PropertyAccessException("Missing handler for type " + propertyPathStart.getContentType());
		}
	}

	/**
	 * Checks whether or not the method may be called within the parameter storage algorithm.
	 *
	 * @param method
	 *            The method name to check for.
	 * @return <code>true</code> if the method is accepted.
	 */
	private boolean isAcceptedMethod(String method) {
		for (String allowed : ALLOWED_METHODS) {
			if (allowed.equals(method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Inner static recursive method to go along the given path.
	 *
	 * @see PropertyPath
	 *
	 * @param propertyPath
	 *            The path to follow.
	 * @param object
	 *            The object to analyze.
	 * @return The {@link String} representation of the field or parameter followed by the path.
	 * @throws PropertyAccessException
	 *             This exception is thrown whenever something unexpectedly happens while accessing
	 *             a property.
	 */
	private String getPropertyContent(PropertyPath propertyPath, Object object) throws PropertyAccessException {
		if (null == object) {
			return "null";
		}

		if (null == propertyPath) {
			// end of the path to follow, return the String representation of
			// the object
			return object.toString();
		}

		Class<?> c;
		if (object instanceof Class) {
			// This check is needed when a static class is passed to this
			// method.
			c = (Class<?>) object;
		} else {
			c = object.getClass();
		}

		// We need to differ between calls of methods and the navigation of
		// properties of an object. This differentiation is integrated to
		// force the user to add () to the method to be called, thus the
		// user is aware what he is doing and no unwanted method calls are
		// performed.
		if (propertyPath.isMethodCall()) {

			// strip the "()" from the path to find the method
			String methodName = propertyPath.getName().substring(0, propertyPath.getName().length() - 2);

			// check if this method may be called
			if (!isAcceptedMethod(methodName)) {
				throw new PropertyAccessException("Method " + methodName + " MAY not be called!");
			}

			// special handling for the length method of Array objects
			// Array objects do not inherit from the static Array class, thus
			// trying to retrieve the method by reflection is not possible
			if ("length".equals(methodName)) {
				if (object.getClass().isArray()) { // ensure that we are really
					// dealing with an array
					return getPropertyContent(propertyPath.getPathToContinue(), Integer.valueOf(Array.getLength(object)));
				} else {
					log.error("Trying to access the lenght() method for a non array type");
					throw new PropertyAccessException("Trying to access the length() method for a non array type");
				}
			}

			do {
				// we are iterating using getDeclaredMethods as this call will
				// also provide the default access and protected methods which
				// the
				// call to getMethods() will not
				Method[] methods = c.getDeclaredMethods();
				for (Method method : methods) {
					if (methodName.equals(method.getName())) {

						// We are only calling methods that do not take an
						// argument
						if (method.getParameterTypes().length != 0) {
							if (log.isDebugEnabled()) {
								log.debug("Skipping matching method " + method.getName() + " as it is not a no argument method");
							}
							continue;
						}

						try {
							Object result = method.invoke(object, (Object[]) null);
							return getPropertyContent(propertyPath.getPathToContinue(), result);
						} catch (IllegalArgumentException e) {
							log.error(e.getMessage());
							throw new PropertyAccessException("Illegal Argument Exception!", e);
						} catch (IllegalAccessException e) {
							log.error(e.getMessage());
							throw new PropertyAccessException("IllegalAccessException!", e);
						} catch (InvocationTargetException e) {
							log.error(e.getMessage());
							throw new PropertyAccessException("InvocationTargetException!", e);
						}

					}
				}

				c = c.getSuperclass();
			} while (c != Object.class);

		} else { // We are dealing with a property navigation and not an method
			// call
			do {
				Field[] fields = c.getDeclaredFields();
				for (Field field : fields) {
					if (propertyPath.getName().equals(field.getName())) {
						try {
							field.setAccessible(true);
							Object fieldObject = field.get(object);
							return getPropertyContent(propertyPath.getPathToContinue(), fieldObject);
						} catch (SecurityException e) {
							log.error(e.getMessage());
							throw new PropertyAccessException("Security Exception was thrown while accessing a field!", e);
						} catch (IllegalArgumentException e) {
							log.error(e.getMessage());
							throw new PropertyAccessException("Illegal Argument Exception!", e);
						} catch (IllegalAccessException e) {
							log.error(e.getMessage());
							throw new PropertyAccessException("Illegal Access Exception!", e);
						}
					}
				}

				c = c.getSuperclass();
			} while (c != Object.class);
		}

		throw new PropertyAccessException("Property or method " + propertyPath.getName() + " cannot be found in class " + object.getClass() + "!");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ParameterContentData> getParameterContentData(List<PropertyPathStart> propertyAccessorList, Object clazz, Object[] parameters, Object returnValue) {
		List<ParameterContentData> parameterContentData = new ArrayList<ParameterContentData>();
		for (PropertyPathStart start : propertyAccessorList) {
			try {
				String content = this.getPropertyContent(start, clazz, parameters, returnValue);
				ParameterContentData paramContentData = new ParameterContentData();
				paramContentData.setContent(content);
				paramContentData.setContentType(start.getContentType());
				paramContentData.setName(start.getName());
				paramContentData.setSignaturePosition(start.getSignaturePosition());
				parameterContentData.add(paramContentData);
			} catch (PropertyAccessException e) {
				if (log.isErrorEnabled()) {
					log.error("Cannot access the property: " + start + " for class " + clazz + ". Will be removed from the list to prevent further errors! (" + e.getMessage() + ")");
				}

				propertyAccessorList.remove(start);
				// iterator.remove(); // Unsupported exception. Iterator can't make changes, since
				// iterating over a snapshot.

			}

		}

		return parameterContentData;
	}

}
