package info.novatec.inspectit.agent.config;

import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPath;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.inspectit.communication.data.ParameterContentData;

import java.util.List;

/**
 * This interface defines methods to access the contents of the fields and method parameters of
 * classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IPropertyAccessor {

	/**
	 * Returns the content of the property. Either a field of a class will be accessed, a method
	 * parameter or the return value.
	 * 
	 * @see PropertyPathStart
	 * @see PropertyPath
	 * 
	 * @param propertyPathStart
	 *            This parameter defines the start of the path.
	 * @param clazz
	 *            The current instance or class object of the executed method.
	 * @param parameters
	 *            The method parameters (can be <code>null</code>).
	 * @param returnValue
	 *            The return value of the method.
	 * @return The {@link String} representation of the field or parameter followed by the path.
	 * @throws PropertyAccessException
	 *             This exception is thrown whenever something unexpectedly happens while accessing
	 *             a property.
	 */
	String getPropertyContent(PropertyPathStart propertyPathStart, Object clazz, Object[] parameters, Object returnValue) throws PropertyAccessException;

	/**
	 * Converts the list of property accessors {@link PropertyPathStart} into a list of
	 * {@link ParameterContentData}.
	 * 
	 * @param propertyAccessorList
	 *            The list of property accessors.
	 * @param clazz
	 *            The class object.
	 * @param parameters
	 *            The parameters.
	 * @param returnValue
	 *            The return value of the method.
	 * @return The list of {@link ParameterContentData}.
	 */
	List<ParameterContentData> getParameterContentData(List<PropertyPathStart> propertyAccessorList, Object clazz, Object[] parameters, Object returnValue);

}