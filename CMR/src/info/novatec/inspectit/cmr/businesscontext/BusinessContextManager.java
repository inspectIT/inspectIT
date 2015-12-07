package info.novatec.inspectit.cmr.businesscontext;

import info.novatec.inspectit.cmr.configuration.business.BusinessContextDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessContextDefinition;
import info.novatec.inspectit.cmr.jaxb.JAXBTransformator;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.BusinessContextErrorCodeEnum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 * Component responsible for managing the business context definitions.
 * 
 * @author Alexander Wert
 *
 */
@Component
public class BusinessContextManager {

	/**
	 * The logger of this class.
	 * <p>
	 * Must be declared manually since loading of the properties is done before beans
	 * post-processing.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(BusinessContextManager.class);

	/**
	 * Directory where configuration files are placed.
	 */
	private static final String CONFIG_DIR = "config";

	/**
	 * Directory where configuration files are places.
	 */
	private static final String SCHEMA_DIR = CONFIG_DIR + File.separatorChar + "schema";

	/**
	 * Name of the schema file for configuration.
	 */
	private static final String BUSINESS_CONTEXT_SCHEMA_FILE = "businessContextSchema.xsd";

	/**
	 * File name where default configuration is stored.
	 */
	private static final String BUSINESS_CONTEXT_CONFIG_FILE = "businessContext.xml";

	/**
	 * Business context definition.
	 */
	private IBusinessContextDefinition businessContextDefinition;

	/**
	 * {@link JAXBTransformator}.
	 * <p>
	 * Can not auto-wire this component at the point of start-up, because it's a component. Thus,
	 * direct access.
	 */
	private JAXBTransformator transformator = new JAXBTransformator();

	public IBusinessContextDefinition getBusinessconContextDefinition() {
		return businessContextDefinition;
	}

	private Path getBusinessContextConfigurationPath() {
		return Paths.get(CONFIG_DIR, BUSINESS_CONTEXT_CONFIG_FILE);
	}

	/**
	 * @return Returns path to the configuration XSD schema file.
	 */
	private Path getBusinessContextSchemaPath() {
		return Paths.get(SCHEMA_DIR, BUSINESS_CONTEXT_SCHEMA_FILE);
	}

	/**
	 * Loads the business context definition if it is not already loaded. If successfully loaded
	 * definition will be placed in the {@link #businessContextDefinition} field.
	 * 
	 * 
	 * @throws BusinessException
	 *             If loading business context fails.
	 */
	@PostConstruct
	public synchronized void loadBusinessContextDefinition() throws BusinessException {
		LOG.info("|-Loading the business context definition");
		try {
			businessContextDefinition = transformator.unmarshall(getBusinessContextConfigurationPath(), getBusinessContextSchemaPath(), BusinessContextDefinition.class);
		} catch (JAXBException | SAXException e) {
			throw new TechnicalException("Load business context.", BusinessContextErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Load business context.", BusinessContextErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
		if (null == businessContextDefinition) {
			businessContextDefinition = new BusinessContextDefinition();
			try {
				storeToFile();
			} catch (JAXBException e) {
				throw new TechnicalException("Load business context.", BusinessContextErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
			} catch (IOException e) {
				throw new TechnicalException("Load business context.", BusinessContextErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
			}
		}
	}

	/**
	 * Updates and stores new definition of the business context.
	 * 
	 * @param businessContextDefinition
	 *            New {@link IBusinessContextDefinition} to use.
	 * @throws BusinessException
	 *             If updating business context fails.
	 */
	public synchronized void updateBusinessContextDefinition(IBusinessContextDefinition businessContextDefinition) throws BusinessException {
		try {
			this.businessContextDefinition = businessContextDefinition;
			storeToFile();
		} catch (JAXBException e) {
			throw new TechnicalException("Update business context.", BusinessContextErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update business context.", BusinessContextErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}
	
	/**
	 * Updates the definition of the business context.
	 * 
	 * @throws BusinessException
	 *             If updating business context fails.
	 */
	public synchronized void updateBusinessContextDefinition() throws BusinessException {
		try {
			storeToFile();
		} catch (JAXBException e) {
			throw new TechnicalException("Update business context.", BusinessContextErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update business context.", BusinessContextErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * Stores the current state of the {@link #businessContextDefinition} to file.
	 * 
	 * @throws JAXBException
	 *             if marshalling fails
	 * @throws IOException
	 *             if writing to file fails
	 */
	private void storeToFile() throws JAXBException, IOException {
		transformator.marshall(getBusinessContextConfigurationPath(), businessContextDefinition, null);
	}

}
