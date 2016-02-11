package info.novatec.inspectit.spring.logger;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Component to inject the {@link Log} to each bean that has a {@link Log} annotation.
 * 
 * @author Patrice Bouillet
 * 
 */
@Component
public class LoggerPostProcessor implements BeanPostProcessor {

	/**
	 * {@inheritDoc}
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object postProcessBeforeInitialization(final Object bean, String beanName) {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
			public void doWith(Field field) throws IllegalAccessException {
				if (field.getAnnotation(Log.class) != null) {
					Logger log = LoggerFactory.getLogger(bean.getClass());
					ReflectionUtils.makeAccessible(field);
					field.set(bean, log);
				}
			}
		});
		return bean;
	}

}