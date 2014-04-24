package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.cmr.ci.ConfigurationInterfaceManager;
import info.novatec.inspectit.spring.logger.Log;

import java.util.Collection;

import javax.annotation.PostConstruct;

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
	public Collection<Profile> getAllProfiles() {
		return ciManager.getAllProfiles();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile getProfile(String id) throws Exception {
		return ciManager.getProfile(id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile createProfile(Profile profile) throws Exception {
		return ciManager.createProfile(profile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Profile updateProfile(Profile profile) throws Exception {
		return ciManager.updateProfile(profile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteProfile(Profile profile) throws Exception {
		ciManager.deleteProfile(profile);
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
	public Environment getEnvironment(String id) throws Exception {
		return ciManager.getEnvironment(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment createEnvironment(Environment environment) throws Exception {
		return ciManager.createEnvironment(environment);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment updateEnvironment(Environment environment) throws Exception {
		return ciManager.updateEnvironment(environment, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEnvironment(Environment environment) throws Exception {
		ciManager.deleteEnvironment(environment);
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
	public AgentMappings setAgentMappings(AgentMappings agentMappings) throws Exception {
		return ciManager.setAgentMappings(agentMappings, true);
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
