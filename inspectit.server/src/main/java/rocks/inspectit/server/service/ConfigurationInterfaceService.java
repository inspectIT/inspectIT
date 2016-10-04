package rocks.inspectit.server.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.TechnicalException;
import rocks.inspectit.shared.all.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceImportData;
import rocks.inspectit.shared.cs.cmr.service.ICmrManagementService;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;

/**
 * Implementation of the {@link ICmrManagementService}.
 *
 * @author Ivan Senic
 * @author Marius Oehler
 *
 */
@Service
public class ConfigurationInterfaceService implements IConfigurationInterfaceService {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ConfigurationInterfaceManager}.
	 */
	@Autowired
	private ConfigurationInterfaceManager ciManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Profile> getAllProfiles() {
		return ciManager.getAllProfiles();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile getProfile(String id) throws BusinessException {
		return ciManager.getProfile(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile createProfile(Profile profile) throws BusinessException {
		try {
			return ciManager.createProfile(profile);
		} catch (JAXBException e) {
			throw new TechnicalException("Create the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Create the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile importProfile(Profile profile) throws BusinessException {
		try {
			return ciManager.importProfile(profile);
		} catch (JAXBException e) {
			throw new TechnicalException("Import the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Import the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile updateProfile(Profile profile) throws BusinessException {
		try {
			return ciManager.updateProfile(profile);
		} catch (JAXBException e) {
			throw new TechnicalException("Update the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteProfile(Profile profile) throws BusinessException {
		try {
			ciManager.deleteProfile(profile);
		} catch (IOException e) {
			throw new TechnicalException("Delete the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Environment> getAllEnvironments() {
		return ciManager.getAllEnvironments();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment getEnvironment(String id) throws BusinessException {
		return ciManager.getEnvironment(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment createEnvironment(Environment environment) throws BusinessException {
		try {
			return ciManager.createEnvironment(environment);
		} catch (JAXBException e) {
			throw new TechnicalException("Create the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Create the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment importEnvironment(Environment environment) throws BusinessException {
		try {
			return ciManager.importEnvironment(environment);
		} catch (JAXBException e) {
			throw new TechnicalException("Import the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Import the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment updateEnvironment(Environment environment) throws BusinessException {
		try {
			return ciManager.updateEnvironment(environment, true);
		} catch (JAXBException e) {
			throw new TechnicalException("Update the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEnvironment(Environment environment) throws BusinessException {
		try {
			ciManager.deleteEnvironment(environment);
		} catch (IOException e) {
			throw new TechnicalException("Delete the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentMappings getAgentMappings() {
		return ciManager.getAgentMappings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentMappings saveAgentMappings(AgentMappings agentMappings) throws BusinessException {
		try {
			return ciManager.saveAgentMappings(agentMappings, true);
		} catch (JAXBException e) {
			throw new TechnicalException("Update the agent mappings.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update the agent mappings.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getExportData(Collection<Environment> environments, Collection<Profile> profiles) throws BusinessException {
		try {
			return ciManager.getExportData(environments, profiles);
		} catch (JAXBException e) {
			throw new TechnicalException("Export the data.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Export the data.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConfigurationInterfaceImportData getImportData(byte[] importData) throws BusinessException {
		try {
			return ciManager.getImportData(importData);
		} catch (JAXBException | SAXException e) {
			throw new TechnicalException("Export the data.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Export the data.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized List<ApplicationDefinition> getApplicationDefinitions() {
		return ciManager.getBusinessconContextDefinition().getApplicationDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ApplicationDefinition getApplicationDefinition(int id) throws BusinessException {
		if (id == ApplicationDefinition.DEFAULT_ID) {
			return ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION;
		} else {
			return ciManager.getBusinessconContextDefinition().getApplicationDefinition(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationDefinition addApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException {
		return addApplicationDefinition(appDefinition, -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ApplicationDefinition addApplicationDefinition(ApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			ApplicationDefinition addedApplicationDefinition;
			if (insertBeforeIndex < 0) {
				addedApplicationDefinition = businessContextDefinition.addApplicationDefinition(appDefinition);
			} else {
				addedApplicationDefinition = businessContextDefinition.addApplicationDefinition(appDefinition, insertBeforeIndex);
			}
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
			return addedApplicationDefinition;
		} catch (JAXBException e) {
			throw new TechnicalException("Add the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED,
					e);
		} catch (IOException e) {
			throw new TechnicalException("Add the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void deleteApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			businessContextDefinition.deleteApplicationDefinition(appDefinition);
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
		} catch (JAXBException e) {
			throw new TechnicalException("Delete the application definition '" + appDefinition.getApplicationName() + "'.",
					ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Delete the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ApplicationDefinition moveApplicationDefinition(ApplicationDefinition appDefinition, int index) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			ApplicationDefinition movedAppDefinition = businessContextDefinition.moveApplicationDefinition(appDefinition, index);
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
			return movedAppDefinition;
		} catch (JAXBException e) {
			throw new TechnicalException("Move the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED,
					e);
		} catch (IOException e) {
			throw new TechnicalException("Move the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ApplicationDefinition updateApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			ApplicationDefinition updated = businessContextDefinition.updateApplicationDefinition(appDefinition);
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
			return updated;
		} catch (JAXBException e) {
			throw new TechnicalException("Update the application definition '" + appDefinition.getApplicationName() + "'.",
					ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AlertingDefinition> getAlertingDefinitions() {
		return ciManager.getAlertingDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlertingDefinition getAlertingDefinition(String id) throws BusinessException {
		return ciManager.getAlertingDefinition(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlertingDefinition createAlertingDefinition(AlertingDefinition alertingDefinition) throws BusinessException {
		try {
			return ciManager.createAlertingDefinition(alertingDefinition);
		} catch (JAXBException e) {
			throw new TechnicalException("Update the alerting definition.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update the alerting definition.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlertingDefinition updateAlertingDefinition(AlertingDefinition alertingDefinition) throws BusinessException {
		try {
			return ciManager.updateAlertingDefinition(alertingDefinition);
		} catch (JAXBException e) {
			throw new TechnicalException("Update the alerting definition.", ConfigurationInterfaceErrorCodeEnum.JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED, e);
		} catch (IOException e) {
			throw new TechnicalException("Update the alerting definition.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteAlertingDefinition(AlertingDefinition alertingDefinition) throws BusinessException {
		try {
			ciManager.deleteAlertingDefinition(alertingDefinition);
		} catch (IOException e) {
			throw new TechnicalException("Delete the alerting definition '" + alertingDefinition.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
		}
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 *
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Configuration Interface Service active...");
		}
	}
}
