package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.cmr.ci.ConfigurationInterfaceManager;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import info.novatec.inspectit.spring.logger.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link ICmrManagementService}.
 * 
 * @author Ivan Senic
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
			throw new TechnicalException("Create the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
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
			throw new TechnicalException("Update the agent mappings..", ConfigurationInterfaceErrorCodeEnum.INPUT_OUTPUT_OPERATION_FAILED, e);
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
