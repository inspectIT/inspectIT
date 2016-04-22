package rocks.inspectit.server.spring.exporter;

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;

/**
 * Provides automatic remote export of services found inside a Spring context.
 *
 * <p>
 * When a bean is annotated with {@link Service} <em>and</em> implements an interface annotated with
 * {@link ServiceInterface}, the bean will be exported as a service with this interface. This is
 * done <em>after</em> the Spring context is refreshed or started so that we are sure that any
 * {@link BeanPostProcessor}s and AOP advise is applied.
 *
 * <p>
 * This exporter bean is registered in the spring context so that all lifecycles are passed and this
 * bean can be used as a reference candidate for other spring beans. The name is either being
 * specified statically in the annotation or dynamically via the name of the service bean +
 * "Exporter" at the end.
 *
 * <p>
 *
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="http://jira.springframework.org/browse/SPR-3926">Spring JIRA (SPR-3926)</a>. Original
 * authors are James Douglas and Henno Vermeulen.
 *
 * @author James Douglas
 * @author Henno Vermeulen
 * @author Patrice Bouillet
 */
@Component
public class RemotingExporter implements BeanFactoryPostProcessor {

	/**
	 * The logger for this class. Cannot be injected via Spring.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemotingExporter.class);

	/**
	 * The annotation defining a class being a service.
	 */
	private Class<? extends Annotation> serviceAnnotationType = Service.class;

	/**
	 * The annotation defining an interface being a service interface.
	 */
	private Class<? extends Annotation> serviceInterfaceAnnotationType = ServiceInterface.class;

	/**
	 * {@inheritDoc}
	 *
	 * We look for the service annotation on the bean class found in the {@link BeanDefinition}
	 * before any {@link BeanPostProcessor}s or AOP advice is applied.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		LOG.info("|-RemoteExporter: Processing Beans for remote export");

		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
			if (beanDef.isAbstract()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("|- RemoteExporter: Skipping abstract Bean '" + beanName + "'");
				}
				continue;
			}

			if (null == beanDef.getBeanClassName()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("|- RemoteExporter: Skipping Bean '" + beanName + "' which has no bean class defined");
				}
				continue;
			}

			Class<?> serviceInterface = null;
			Class<?> serviceClass;
			String beanClassName = beanDef.getBeanClassName();
			if (null == beanClassName) {
				// bean class name is null for beans defined via @Configuration
				continue;
			}
			try {
				serviceClass = Class.forName(beanClassName);
				serviceInterface = findServiceInterface(serviceClass);
			} catch (ClassNotFoundException e) {
				throw new BeanCreationException("class of bean " + beanName + " not found", e);
			}

			if (serviceInterface != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("|- RemoteExporter: Found service Bean with name '" + beanName + "' and interface " + serviceInterface);
				}

				// creating the bean definition so that the bean is registered in the spring
				// container
				Annotation annotation = AnnotationUtils.findAnnotation(serviceClass, serviceInterfaceAnnotationType);
				RootBeanDefinition definition;

				MutablePropertyValues values = new MutablePropertyValues();
				values.add("service", new RuntimeBeanReference(beanName));
				values.add("serviceInterface", new TypedStringValue(serviceInterface.getCanonicalName()));

				ServiceExporterType type = (ServiceExporterType) AnnotationUtils.getValue(annotation, "exporter");
				switch (type) {
				case RMI:
					definition = new RootBeanDefinition(KryoNetRmiServiceExporter.class);
					if (annotationPropertySet(annotation, "serviceId")) {
						int serviceId = (int) AnnotationUtils.getValue(annotation, "serviceId");
						values.add("serviceId", serviceId);
					}
					break;
				case HTTP:
					definition = new RootBeanDefinition(KryoHttpInvokerServiceExporter.class);
					break;
				default:
					throw new BeanCreationException("Could not create service exporter bean because exporter type is not handled: " + type);
				}

				definition.setPropertyValues(values);
				definition.setAutowireCandidate(true);

				BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
				registry.registerBeanDefinition(getNameForExporterBean(beanName, annotation), definition);

				if (LOG.isDebugEnabled()) {
					LOG.debug("|- RemoteExporter: Registered new Bean: " + beanName + "Exporter");
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("|- RemoteExporter: Skipping Bean because no remotable interface was found '" + beanName + "'");
				}
			}
		}
	}

	/**
	 * Checks for if an annotation property is set by overriding the default value.
	 *
	 * @param annotation
	 *            the annotation to check for.
	 * @param attributeName
	 *            the name of the attribute to compare the default and current value.
	 * @return if the annotation property has been set.
	 */
	private boolean annotationPropertySet(Annotation annotation, String attributeName) {
		Object defaultValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
		Object currentValue = AnnotationUtils.getValue(annotation, attributeName);
		return !currentValue.equals(defaultValue);
	}

	/**
	 * Creates the name of the exporter bean under which it will be exposed in the spring container.
	 *
	 * @param beanName
	 *            the original service bean name.
	 * @param annotation
	 *            the annotation reference used to extract the name definition - if any.
	 * @return the name of the exporter bean.
	 */
	private String getNameForExporterBean(String beanName, Annotation annotation) {
		String name = (String) AnnotationUtils.getValue(annotation, "name");
		if ((null != name) && !"".equals(name.trim())) {
			return name;
		} else {
			return beanName + "Exporter";
		}
	}

	/**
	 * Searches for a service interface on the passed class.
	 *
	 * @param serviceClass
	 *            the class to look for a service interface.
	 * @return returns the service interface.
	 */
	private Class<?> findServiceInterface(Class<?> serviceClass) {
		Class<?> serviceInterface = null;
		if (AnnotationUtils.isAnnotationDeclaredLocally(serviceAnnotationType, serviceClass)) {
			for (Class<?> interfaceClass : serviceClass.getInterfaces()) {
				if (AnnotationUtils.isAnnotationDeclaredLocally(serviceInterfaceAnnotationType, interfaceClass)) {
					serviceInterface = interfaceClass;
				}
			}
		}
		return serviceInterface;
	}

}
