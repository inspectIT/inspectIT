package info.novatec.inspectit.cmr.property;

import info.novatec.inspectit.cmr.jaxb.JAXBTransformator;
import info.novatec.inspectit.cmr.property.configuration.AbstractProperty;
import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.cmr.util.ShutdownService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;

/**
 * Properties manager bean that controls all properties specified in the configuration files and
 * provides the {@link Properties} object as a bean for the Spring context property-placeholder.
 * 
 * @author Ivan Senic
 * 
 */
@org.springframework.context.annotation.Configuration
public class PropertyManager {

	/**
	 * The logger of this class.
	 * <p>
	 * Must be declared manually since loading of the properties is done before beans
	 * post-processing.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PropertyManager.class);

	/**
	 * Name of the local properties bean that will be created.
	 */
	public static final String LOCAL_PROPERTIES_BEAN_NAME = "localPropertiesBean";

	/**
	 * Directory where configuration files are places.
	 */
	private static final String CONFIG_DIR = "config";

	/**
	 * Directory where configuration files are places.
	 */
	private static final String SCHEMA_DIR = CONFIG_DIR + File.separatorChar + "schema";

	/**
	 * Name of the schema file for configuration.
	 */
	private static final String CONFIGURATION_SCHEMA_FILE = "configurationSchema.xsd";

	/**
	 * Name of the schema file for configuration update.
	 */
	private static final String CONFIGURATION_UPDATE_SCHEMA_FILE = "configurationUpdateSchema.xsd";

	/**
	 * File name where default configuration is stored.
	 */
	private static final String DEFAULT_CONFIG_FILE = "default.xml";

	/**
	 * File name where the current configuration updates are stored.
	 */
	private static final String CONFIG_UPDATE_FILE = "configurationUpdates.xml";

	/**
	 * Default configuration.
	 */
	private Configuration configuration;

	/**
	 * Currently used configuration update.
	 */
	private ConfigurationUpdate configurationUpdate;

	/**
	 * {@link PropertyUpdateExecutor} that executes methods need to be executed after properties
	 * changes.
	 */
	@Autowired
	private PropertyUpdateExecutor propertyUpdateExecutor;

	/**
	 * Shutdown service for executing restarts.
	 */
	@Autowired
	private ShutdownService shutdownService;

	/**
	 * {@link JAXBTransformator}.
	 * <p>
	 * Can not auto-wire this component at the point of start-up, because it's a component. Thus,
	 * direct access.
	 */
	private JAXBTransformator transformator = new JAXBTransformator();

	/**
	 * Returns the currently existing {@link PropertySection} in the CMR configuration.
	 * 
	 * @return Returns the currently existing {@link PropertySection} in the CMR configuration.
	 */
	public Collection<PropertySection> getConfigurationPropertySections() {
		return configuration.getSections();
	}

	/**
	 * Returns the currently CMR configuration.
	 * 
	 * @return Returns the currently CMR configuration.
	 */
	Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Updates the current configuration of the CMR.
	 * 
	 * @param update
	 *            {@link ConfigurationUpdate} containing all {@link AbstractPropertyUpdate}s that
	 *            should be reflected in the current configuration of the CMR.
	 * @param executeRestart
	 *            Should restart be automatically executed after properties update.
	 * @throws Exception
	 *             If update is not valid
	 */
	public synchronized void updateConfiguration(ConfigurationUpdate update, boolean executeRestart) throws Exception {
		// first validate all changes
		// if property does not exist or can not be updated throw exception
		for (IPropertyUpdate<?> propertyUpdate : update.getPropertyUpdates()) {
			SingleProperty<Object> property = (SingleProperty<Object>) configuration.forLogicalName(propertyUpdate.getPropertyLogicalName());
			if (null == property) {
				throw new Exception("Property " + propertyUpdate.getPropertyLogicalName() + " can not be updated because the property does not exist in the current configuration.");
			} else if (!property.canUpdate(propertyUpdate)) {
				throw new Exception("Property " + propertyUpdate.getPropertyLogicalName() + " can not be updated because the property update value is not valid.");
			}
		}

		// if all valid update all
		List<SingleProperty<?>> updatedProperties = new ArrayList<>();
		for (IPropertyUpdate<?> propertyUpdate : update.getPropertyUpdates()) {
			SingleProperty<Object> property = (SingleProperty<Object>) configuration.forLogicalName(propertyUpdate.getPropertyLogicalName());
			if (propertyUpdate.isRestoreDefault()) {
				property.setToDefaultValue();
			} else {
				property.setValue(propertyUpdate.getUpdateValue());
			}
			updatedProperties.add(property);

			if (LOG.isInfoEnabled()) {
				LOG.info("Property '" + property.getName() + "' successfully updated, new value is " + property.getFormattedValue());
			}
		}

		// merge the update file
		// note that restore to default updates will also be part of the update
		if (null == configurationUpdate) {
			configurationUpdate = update;
		} else {
			configurationUpdate.merge(update, true);
		}

		// back up the old configuration file if it exists
		if (Files.exists(getConfigurationUpdatePath())) {
			String backupPathString = getConfigurationUpdatePath().toString() + "~" + System.currentTimeMillis() + ".backup";
			Path backupPath = Paths.get(backupPathString);
			try {
				Files.copy(getConfigurationUpdatePath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOG.warn("Could not back up the current configuration update", e);
			}
		}

		// flush the new configuration update
		try {
			transformator.marshall(getConfigurationUpdatePath(), configurationUpdate, Paths.get(CONFIG_DIR).relativize(getConfigurationUpdateSchemaPath()).toString());
		} catch (JAXBException | IOException e) {
			LOG.warn("Could not flush the new configuration update", e);
		}

		if (executeRestart) {
			// just restart, properties will be reloaded anyway
			shutdownService.restart();
		} else {
			// activate property update executor
			propertyUpdateExecutor.executePropertyUpdates(updatedProperties);
		}
	}

	/**
	 * Returns {@link Properties} containing key/value property pairs defined in CMR configuration.
	 * 
	 * @return Returns {@link Properties} containing key/value property pairs defined in CMR
	 *         configuration.
	 */
	@Bean(name = LOCAL_PROPERTIES_BEAN_NAME)
	protected synchronized Properties getProperties() {
		try {
			loadConfigurationAndUpdates();
		} catch (JAXBException | IOException | SAXException e) {
			LOG.warn("|-Default CMR configuration can not be loaded.", e);
			return new Properties();
		}

		// check if there is update file
		if (null != configurationUpdate) {
			LOG.info("|-Updates to the CMR Configuration found, applying the updates");

			List<IPropertyUpdate<?>> notValidList = new ArrayList<>();
			// if there is update file validate updates and set to the configuration

			for (IPropertyUpdate<?> propertyUpdate : configurationUpdate.getPropertyUpdates()) {
				SingleProperty<Object> property = (SingleProperty<Object>) configuration.forLogicalName(propertyUpdate.getPropertyLogicalName());

				// if property does not exist or can not be update add to not valid
				if (null == property || !property.canUpdate(propertyUpdate)) {
					notValidList.add(propertyUpdate);
					continue;
				}

				if (propertyUpdate.isRestoreDefault()) {
					property.setToDefaultValue();
				} else {
					property.setValue(propertyUpdate.getUpdateValue());
				}
			}

			// if not valid list is not empty
			// log all wrong properties and rewrite the configuration update
			if (CollectionUtils.isNotEmpty(notValidList)) {
				for (IPropertyUpdate<?> propertyUpdate : notValidList) {
					configurationUpdate.removePropertyUpdate(propertyUpdate);
					LOG.info("|-Update of the property " + propertyUpdate.getPropertyLogicalName()
							+ " can not be performed either because property does not exist in the default configuration or the update value is not valid");
				}
				try {
					transformator.marshall(getConfigurationUpdatePath(), configurationUpdate, Paths.get(CONFIG_DIR).relativize(getConfigurationUpdateSchemaPath()).toString());
				} catch (JAXBException | IOException e) {
					LOG.warn("|-CMR Configuration update can not be re-written", e);
				}
			}
		} else {
			LOG.info("|-No CMR Configuration updates found, continuing to use default configuration");
		}

		// validate configuration
		Map<AbstractProperty, PropertyValidation> validationMap = configuration.validate();

		// if we have some validation problems log them
		if (MapUtils.isNotEmpty(validationMap)) {
			for (Entry<AbstractProperty, PropertyValidation> entry : validationMap.entrySet()) {
				LOG.warn(entry.getValue().getMessage());
			}
		} else {
			LOG.info("|-CMR Configuration verified with no errors");
		}

		// create properties from correct ones
		Properties properties = new Properties();
		for (AbstractProperty property : configuration.getAllProperties()) {
			if (!validationMap.containsKey(property)) {
				property.register(properties);
			}
		}
		return properties;
	}

	/**
	 * This is a workaround for the problem of providing the {@link PropertyManager} to the bean
	 * factory for auto-wiring. The problem is that because Properties are provided via @Bean
	 * annotation, the {@link PropertyManager} itself will not be completely auto-wired with needed
	 * dependencies.
	 * 
	 * @return Object it self.
	 */
	@Bean
	protected PropertyManager getPropertyManager() {
		return this;
	}

	/**
	 * @return Returns path to the default configuration path.
	 */
	Path getDefaultConfigurationPath() {
		return Paths.get(CONFIG_DIR, DEFAULT_CONFIG_FILE);
	}

	/**
	 * @return Returns path to the current configuration update path.
	 */
	Path getConfigurationUpdatePath() {
		return Paths.get(CONFIG_DIR, CONFIG_UPDATE_FILE);
	}

	/**
	 * @return Returns path to the configuration XSD schema file.
	 */
	Path getConfigurationSchemaPath() {
		return Paths.get(SCHEMA_DIR, CONFIGURATION_SCHEMA_FILE);
	}

	/**
	 * @return Returns path to the configuration update XSD schema file.
	 */
	Path getConfigurationUpdateSchemaPath() {
		return Paths.get(SCHEMA_DIR, CONFIGURATION_UPDATE_SCHEMA_FILE);
	}

	/**
	 * Loads the default configuration if it is not already loaded. If successfully loaded
	 * configuration will be placed in the {@link #configuration} field.
	 * 
	 * 
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 * @throws SAXException
	 *             If {@link SAXException} occurs during schema parsing.
	 */
	void loadConfigurationAndUpdates() throws JAXBException, IOException, SAXException {
		LOG.info("|-Loading the default CMR configuration");
		configuration = transformator.unmarshall(getDefaultConfigurationPath(), getConfigurationSchemaPath(), Configuration.class);
		configurationUpdate = transformator.unmarshall(getConfigurationUpdatePath(), getConfigurationUpdateSchemaPath(), ConfigurationUpdate.class);
	}

}
