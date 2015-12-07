package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.ci.BusinessContextDefinition;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.cmr.ci.ConfigurationInterfaceManager;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import info.novatec.inspectit.spring.logger.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

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
	 * Set of {@link ApplicationData} instances representing identified applications.
	 */
	private final Set<ApplicationData> applications = new HashSet<>();

	/**
	 * Set of {@link BusinessTransactionData} instances representing identified business
	 * transactions.
	 */
	private final Set<BusinessTransactionData> businessTransactions = new HashSet<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BusinessContextDefinition getBusinessContextDefinition() {
		return ciManager.getBusinessconContextDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ApplicationDefinition> getApplicationDefinitions() {
		return ciManager.getBusinessconContextDefinition().getApplicationDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationDefinition getApplicationDefinition(int id) throws BusinessException {
		if (id == ApplicationDefinition.DEFAULT_ID) {
			return ciManager.getBusinessconContextDefinition().getDefaultApplicationDefinition();
		} else {
			return ciManager.getBusinessconContextDefinition().getApplicationDefinition(id);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws BusinessException
	 */
	@Override
	public synchronized void addApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			businessContextDefinition.addApplicationDefinition(appDefinition);
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
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
	public synchronized void addApplicationDefinition(ApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			businessContextDefinition.addApplicationDefinition(appDefinition, insertBeforeIndex);
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
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
	public synchronized void moveApplicationDefinition(ApplicationDefinition appDefinition, int index) throws BusinessException {
		try {
			BusinessContextDefinition businessContextDefinition = ciManager.getBusinessconContextDefinition();
			businessContextDefinition.moveApplicationDefinition(appDefinition, index);
			ciManager.updateBusinessContextDefinition(businessContextDefinition);
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
	public Collection<ApplicationData> getApplications() {
		return applications;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<BusinessTransactionData> getBusinessTransactions() {
		return businessTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<BusinessTransactionData> getBusinessTransactions(int applicationId) {
		List<BusinessTransactionData> resultList = new ArrayList<>();
		for (BusinessTransactionData businessTx : businessTransactions) {
			if (businessTx.getApplication().getId() == applicationId) {
				resultList.add(businessTx);
			}
		}
		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerBusinessTransaction(BusinessTransactionData businessTransaction) {
		businessTransactions.add(businessTransaction);
		applications.add(businessTransaction.getApplication());
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
}
