package rocks.inspectit.server.property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;

/**
 * This class executes method annotated with {@link PropertyUpdate} annotation.
 *
 * @author Ivan Senic
 *
 */
@Component
public class PropertyUpdateExecutor implements BeanPostProcessor, BeanFactoryAware {

	/**
	 * The logger of this class.
	 * <p>
	 * Must be declared manually because of the post processor attribute of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PropertyUpdateExecutor.class);

	/**
	 * {@link ConfigurableListableBeanFactory}.
	 */
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * List of all collected {@link PropertyUpdateFieldInfo} objects.
	 */
	private List<PropertyUpdateFieldInfo> fieldInfoList = new ArrayList<>();

	/**
	 * List of all collected {@link PropertyUpdateMethodInfo} objects.
	 */
	private List<PropertyUpdateMethodInfo> methodInfoList = new ArrayList<>();

	/**
	 * Executes the methods that declare the {@link PropertyUpdate} annotations if the list of
	 * updated properties names matches the ones specified in the annotation.
	 *
	 * @param properties
	 *            List of updated properties.
	 */
	public void executePropertyUpdates(List<SingleProperty<?>> properties) {
		// first update all fields
		for (SingleProperty<?> singleProperty : properties) {
			for (PropertyUpdateFieldInfo fieldInfo : fieldInfoList) {
				if (fieldInfo.isPropertyMatching(singleProperty)) {
					Object value = singleProperty.getValue();
					if (!fieldInfo.getField().getType().equals(value.getClass())) {
						// if classes are not matching try with spring type converter
						value = beanFactory.getTypeConverter().convertIfNecessary(value, fieldInfo.getField().getType());
					}
					ReflectionUtils.setField(fieldInfo.getField(), fieldInfo.getTarget(), value);

					if (LOG.isDebugEnabled()) {
						LOG.debug("Updated field " + fieldInfo.getField().getName() + " on object " + fieldInfo.getTarget() + " with value " + value
								+ ". The field was updated because of the updated property " + fieldInfo.getProperty());
					}
				}
			}
		}

		// then execute all update methods
		Set<PropertyUpdateMethodInfo> methodsToExecute = new HashSet<>();
		for (PropertyUpdateMethodInfo methodInfo : methodInfoList) {
			if (!methodsToExecute.contains(methodInfo) && methodInfo.arePropertiesMatching(properties)) {
				methodsToExecute.add(methodInfo);
			}
		}

		if (CollectionUtils.isNotEmpty(methodsToExecute)) {
			for (PropertyUpdateMethodInfo methodInfo : methodsToExecute) {
				ReflectionUtils.invokeMethod(methodInfo.getMethod(), methodInfo.getTarget());

				if (LOG.isDebugEnabled()) {
					LOG.debug("Invoked the method " + methodInfo.getMethod().toGenericString() + " on target object " + methodInfo.getTarget()
					+ ". The method was invoked cause it defines the following properties of which at least one was updated: " + Arrays.toString(methodInfo.getProperties()));
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		Object realBean = null;
		try {
			realBean = getTargetObject(bean);
		} catch (Exception e) {
			LOG.warn("Unable to get the real bean object.", e);
			return bean;
		}

		// if we don't have the real object return
		if (null == realBean) {
			return bean;
		}

		final Object realBeanFinal = realBean;
		// process methods for @PropertyUpdate
		ReflectionUtils.doWithMethods(realBean.getClass(), new MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				// make sure only no-arg methods with annotation are added
				if (method.isAnnotationPresent(PropertyUpdate.class) && ArrayUtils.isEmpty(method.getParameterTypes())) {
					PropertyUpdate propertyUpdate = method.getAnnotation(PropertyUpdate.class);
					if (ArrayUtils.isNotEmpty(propertyUpdate.properties())) {
						ReflectionUtils.makeAccessible(method);
						PropertyUpdateMethodInfo methodInfo = new PropertyUpdateMethodInfo(realBeanFinal, method, propertyUpdate.properties());
						methodInfoList.add(methodInfo);
					}
				}
			}
		});

		// process fields for @Value
		ReflectionUtils.doWithFields(realBean.getClass(), new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (field.isAnnotationPresent(Value.class)) {
					// we must skip final fields
					if (Modifier.isFinal(field.getModifiers())) {
						LOG.warn("Field " + field.getName() + " of bean " + beanName
								+ " defines @Value annotation, although it's declared as final. This field can not be updated if its property value is changed.");
						return;
					}

					Value value = field.getAnnotation(Value.class);
					String placeholder = value.value();
					int startChar = placeholder.indexOf('{');
					int endChar = placeholder.indexOf('}');
					String property;
					if ((startChar > 0) && (endChar > startChar)) {
						property = placeholder.substring(startChar + 1, endChar);
					} else {
						property = placeholder;
					}
					ReflectionUtils.makeAccessible(field);
					PropertyUpdateFieldInfo fieldInfo = new PropertyUpdateFieldInfo(realBeanFinal, field, property);
					fieldInfoList.add(fieldInfo);
				}
			}
		});

		// always return original bean
		return bean;
	}

	/**
	 * Checks if the given bean is proxy and if so tries to get the target object. Otherwise returns
	 * the original bean.
	 *
	 * @param bean
	 *            bean
	 * @return Target object of a bean if it's a proxy or bean itself.
	 * @throws Exception
	 *             passing exception
	 */
	private Object getTargetObject(Object bean) throws Exception {
		if (AopUtils.isJdkDynamicProxy(bean)) {
			return ((Advised) bean).getTargetSource().getTarget();
		} else {
			return bean;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException("PropertyUpdateExecutor requires a ConfigurableListableBeanFactory");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	/**
	 * Class that combines all needed information for one field that needs to be updated when
	 * certain property is changed.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static final class PropertyUpdateFieldInfo {

		/**
		 * Target object.
		 */
		private final Object target;

		/**
		 * Field that needs to be updated.
		 */
		private final Field field;

		/**
		 * Property name.
		 */
		private final String property;

		/**
		 * @param target
		 *            Target object.
		 * @param field
		 *            Field that needs to be updated.
		 * @param property
		 *            Property name.
		 */
		public PropertyUpdateFieldInfo(Object target, Field field, String property) {
			if (null == target) {
				throw new IllegalArgumentException("Target object can not be null.");
			}
			if (null == field) {
				throw new IllegalArgumentException("Field to update can not be null.");
			}
			if (null == property) {
				throw new IllegalArgumentException("Property name can not be null.");
			}
			this.target = target;
			this.field = field;
			this.property = property;
		}

		/**
		 * Gets {@link #target}.
		 *
		 * @return {@link #target}
		 */
		public Object getTarget() {
			return target;
		}

		/**
		 * Gets {@link #field}.
		 *
		 * @return {@link #field}
		 */
		public Field getField() {
			return field;
		}

		/**
		 * Gets {@link #property}.
		 *
		 * @return {@link #property}
		 */
		public String getProperty() {
			return property;
		}

		/**
		 * Returns true if the name of the property field is bounded to is matching the logical name
		 * of the update property.
		 *
		 * @param updatedProperty
		 *            {@link SingleProperty}.
		 * @return Returns true if the name of the property field is bounded to is matching the
		 *         logical name of the update property.
		 */
		public boolean isPropertyMatching(SingleProperty<?> updatedProperty) {
			return property.equals(updatedProperty.getLogicalName());
		}
	}

	/**
	 * Class that combines all needed information for one method that needs to be executed when
	 * certain properties are changed.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static final class PropertyUpdateMethodInfo {

		/**
		 * Target object.
		 */
		private final Object target;

		/**
		 * Method that should be executed.
		 */
		private final Method method;

		/**
		 * List of properties to react upon change.
		 */
		private final String[] properties;

		/**
		 * Default constructor.
		 *
		 * @param target
		 *            Target object.
		 * @param method
		 *            Method that should be executed.
		 * @param properties
		 *            List of properties to react upon change.
		 */
		public PropertyUpdateMethodInfo(Object target, Method method, String[] properties) {
			if (null == target) {
				throw new IllegalArgumentException("Target object can not be null.");
			}
			if (null == method) {
				throw new IllegalArgumentException("Method to invoke can not be null.");
			}
			if (ArrayUtils.isEmpty(properties)) {
				throw new IllegalArgumentException("Property array can not be empty.");
			}
			this.target = target;
			this.method = method;
			this.properties = properties;
		}

		/**
		 * Gets {@link #target}.
		 *
		 * @return {@link #target}
		 */
		public Object getTarget() {
			return target;
		}

		/**
		 * Gets {@link #method}.
		 *
		 * @return {@link #method}
		 */
		public Method getMethod() {
			return method;
		}

		/**
		 * Gets {@link #properties}.
		 *
		 * @return {@link #properties}
		 */
		public String[] getProperties() {
			return properties;
		}

		/**
		 * Returns true if given list of properties are matching any property name in this
		 * {@link PropertyUpdateMethodInfo} object.
		 *
		 * @param updatedProperties
		 *            Updated properties.
		 * @return Returns true if given list of are matching any property name in this
		 *         {@link PropertyUpdateMethodInfo} object.
		 */
		public boolean arePropertiesMatching(List<SingleProperty<?>> updatedProperties) {
			if (CollectionUtils.isNotEmpty(updatedProperties)) {
				for (SingleProperty<?> property : updatedProperties) {
					if (ArrayUtils.contains(properties, property.getLogicalName())) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((method == null) ? 0 : method.hashCode());
			result = (prime * result) + ((target == null) ? 0 : System.identityHashCode(target));
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			PropertyUpdateMethodInfo other = (PropertyUpdateMethodInfo) obj;
			if (method == null) {
				if (other.method != null) {
					return false;
				}
			} else if (!method.equals(other.method)) {
				return false;
			}
			if (target == null) {
				if (other.target != null) {
					return false;
				}
			} else if (System.identityHashCode(target) != System.identityHashCode(other.target)) {
				return false;
			}
			return true;
		}
	}

}
