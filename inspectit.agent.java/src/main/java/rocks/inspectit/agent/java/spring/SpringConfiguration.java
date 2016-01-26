package rocks.inspectit.agent.java.spring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.shared.all.instrumentation.config.impl.AbstractSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.StrategyConfig;
import rocks.inspectit.shared.all.kryonet.Client;
import rocks.inspectit.shared.all.kryonet.ExtendedSerializationImpl;
import rocks.inspectit.shared.all.kryonet.IExtendedSerialization;

/**
 * Post process configuration storage to define buffer and sending strategy beans.
 *
 * @author Ivan Senic
 *
 */
@Configuration
@ComponentScan("rocks.inspectit")
public class SpringConfiguration implements BeanDefinitionRegistryPostProcessor {

	/**
	 * Registry to add bean definitions to.
	 */
	private BeanDefinitionRegistry registry;

	/**
	 * Bean factory to force initialization of manually defined beans.
	 */
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * {@inheritDoc}
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry = registry;
	}

	/**
	 * Returns {@link PropertyPlaceholderConfigurer} for the Agent.
	 *
	 * @return Returns {@link PropertyPlaceholderConfigurer} for the Agent.
	 */
	@Bean
	public static PropertyPlaceholderConfigurer properties() {
		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
		ClassPathResource[] resources = new ClassPathResource[] { new ClassPathResource("/config/bytebufferpool.properties"), new ClassPathResource("/config/instrumentation.properties") };
		ppc.setLocations(resources);
		ppc.setIgnoreUnresolvablePlaceholders(true);
		return ppc;
	}

	/**
	 * @return Returns socketReadExecutorService
	 */
	@Bean(name = "socketReadExecutorService")
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public ExecutorService getSocketReadExecutorService() {
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("inspectit-socket-read-executor-service-thread-%d").setDaemon(true).build();
		return Executors.newFixedThreadPool(1, threadFactory);
	}

	/**
	 * @return Returns coreServiceExecutorService
	 */
	@Bean(name = "coreServiceExecutorService")
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public ScheduledExecutorService getCoreServiceExecutorService() {
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("inspectit-core-service-executor-service-thread-%d").setDaemon(true).build();
		return Executors.newScheduledThreadPool(1, threadFactory);
	}

	/**
	 * Creates the client bean.
	 *
	 * @param prototypesProvider
	 *            {@link PrototypesProvider} (autowired)
	 * @return Created bean
	 */
	@Bean(name = "kryonet-client")
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	@Autowired
	public Client getClient(PrototypesProvider prototypesProvider) {
		IExtendedSerialization serialization = new ExtendedSerializationImpl(prototypesProvider);
		return new Client(serialization, prototypesProvider);
	}

	/**
	 * Registers components needed by the configuration to the Spring container.
	 *
	 * @param configurationStorage
	 *            {@link IConfigurationStorage} with the settings.
	 * @throws Exception
	 *             If exception occurs during the registration.
	 */
	public void registerComponents(IConfigurationStorage configurationStorage) throws Exception {
		// buffer strategy
		String className = configurationStorage.getBufferStrategyConfig().getClazzName();
		String beanName = "bufferStrategy[" + className + "]";
		registerBeanDefinitionAndInitialize(beanName, className);

		// sending strategies
		StrategyConfig sendingStrategyConfig = configurationStorage.getSendingStrategyConfig();
		className = sendingStrategyConfig.getClazzName();
		beanName = "sendingStrategy[" + className + "]";
		registerBeanDefinitionAndInitialize(beanName, className);

		// platform sensor types
		for (AbstractSensorTypeConfig platformSensorTypeConfig : configurationStorage.getPlatformSensorTypes()) {
			className = platformSensorTypeConfig.getClassName();
			beanName = "platformSensorType[" + className + "]";
			registerBeanDefinitionAndInitialize(beanName, className);
		}

		// jmx sensor types
		for (JmxSensorTypeConfig jmxSensorTypeConfig : configurationStorage.getJmxSensorTypes()) {
			className = jmxSensorTypeConfig.getClassName();
			beanName = "jmxSensorType[" + className + "]";
			registerBeanDefinitionAndInitialize(beanName, className);
		}

		// method sensor types
		for (AbstractSensorTypeConfig methodSensorTypeConfig : configurationStorage.getMethodSensorTypes()) {
			className = methodSensorTypeConfig.getClassName();
			beanName = "methodSensorType[" + className + "]";
			registerBeanDefinitionAndInitialize(beanName, className);
		}

	}

	/**
	 * Creates bean definition for the given class name, registers the definition in the registry
	 * and immediately invokes the initialization of the bean.
	 * <p>
	 * <i>This is the only way to initialize the bean definitions that no other component has
	 * dependency to, since we add the definitions in the moment when the lookup has been finished
	 * and bean creation has started.</i>
	 *
	 * @param beanName
	 *            Name of the bean to register.
	 * @param className
	 *            Class name of the bean.
	 * @throws ClassNotFoundException
	 *             If class can not be founded.
	 */
	private void registerBeanDefinitionAndInitialize(String beanName, String className) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(className);
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(clazz);
		definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
		definition.setAutowireCandidate(true);
		registry.registerBeanDefinition(beanName, definition);
		beanFactory.getBean(beanName, clazz);
	}
}
