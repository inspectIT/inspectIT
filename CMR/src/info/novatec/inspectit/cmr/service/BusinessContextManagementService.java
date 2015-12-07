package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.businesscontext.BusinessContextManager;
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
	private BusinessContextManager businessContextManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBusinessContextDefinition getBusinessContextDefinition() {
		return businessContextManager.getBusinessconContextDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
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
		return businessContextManager.getBusinessconContextDefinition().getApplicationDefinitions();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws BusinessException
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		businessContextManager.getBusinessconContextDefinition().addApplicationDefinition(appDefinition);
		businessContextManager.updateBusinessContextDefinition();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException {
		businessContextManager.getBusinessconContextDefinition().addApplicationDefinition(appDefinition, insertBeforeIndex);
		businessContextManager.updateBusinessContextDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		businessContextManager.getBusinessconContextDefinition().deleteApplicationDefinition(appDefinition);
		businessContextManager.updateBusinessContextDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveApplicationDefinition(IApplicationDefinition appDefinition, int index) throws BusinessException {
		businessContextManager.getBusinessconContextDefinition().moveApplicationDefinition(appDefinition, index);
		businessContextManager.updateBusinessContextDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		businessContextManager.getBusinessconContextDefinition().updateApplicationDefinition(appDefinition);
		businessContextManager.updateBusinessContextDefinition();

	}
}
