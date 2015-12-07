package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.ci.ConfigurationInterfaceManager;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessContextDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.spring.logger.Log;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Cached access and management service to the business context definition.
 *
 * @author Alexander Wert
 *
 */
@Service
public class BusinessContextManagementService implements IBusinessContextManagementService, InitializingBean {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * {@link BusinessContextManager}.
	 */
	@Autowired
	private ConfigurationInterfaceManager ciManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBusinessContextDefinition getBusinessContextDefinition() {
		return ciManager.getBusinessconContextDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Business Context Management Service active...");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IApplicationDefinition> getApplicationDefinitions() {
		return ciManager.getBusinessconContextDefinition().getApplicationDefinitions();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws BusinessException
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		IBusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
		businessContextDefinition.addApplicationDefinition(appDefinition);
		ciManager.updateBusinessContextDefinition(businessContextDefinition);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException {
		IBusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
		businessContextDefinition.addApplicationDefinition(appDefinition, insertBeforeIndex);
		ciManager.updateBusinessContextDefinition(businessContextDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		IBusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
		businessContextDefinition.deleteApplicationDefinition(appDefinition);
		ciManager.updateBusinessContextDefinition(businessContextDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveApplicationDefinition(IApplicationDefinition appDefinition, int index) throws BusinessException {
		IBusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
		businessContextDefinition.moveApplicationDefinition(appDefinition, index);
		ciManager.updateBusinessContextDefinition(businessContextDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		IBusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
		businessContextDefinition.updateApplicationDefinition(appDefinition);
		ciManager.updateBusinessContextDefinition(businessContextDefinition);
	}
}
