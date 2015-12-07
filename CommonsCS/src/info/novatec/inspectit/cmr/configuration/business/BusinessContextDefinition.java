package info.novatec.inspectit.cmr.configuration.business;

import info.novatec.inspectit.cmr.configuration.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.BusinessContextErrorCodeEnum;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root element of the XML holding the business context configuration.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "business-context")
public class BusinessContextDefinition implements IBusinessContextDefinition {
	/**
	 *
	 */
	private static final long serialVersionUID = -6600206353286327382L;

	/**
	 * The name of the default application.
	 */
	private static final String UNKNOWN_APP = "Unknown Application";

	/**
	 * Application definition configurations.
	 */
	@XmlElementWrapper(name = "applications")
	@XmlElementRef(type = ApplicationDefinition.class)
	private final List<IApplicationDefinition> applicationDefinitions = new LinkedList<IApplicationDefinition>();

	/**
	 * Default application definition.
	 */
	@XmlElement(name = "default-application", type = ApplicationDefinition.class)
	private final IApplicationDefinition defaultApplicationDefinition = new ApplicationDefinition(ApplicationDefinition.DEFAULT_ID, UNKNOWN_APP, new MatchingRule(new BooleanExpression(true)));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IApplicationDefinition> getApplicationDefinitions() {
		List<IApplicationDefinition> allApplicationDefinitions = new LinkedList<IApplicationDefinition>(applicationDefinitions);
		allApplicationDefinitions.add(getDefaultApplicationDefinition());
		return Collections.unmodifiableList(allApplicationDefinitions);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		if (applicationDefinitions.contains(appDefinition)) {
			throw new TechnicalException("Adding application " + appDefinition.getApplicationName() + " with id " + appDefinition.getId() + ".", BusinessContextErrorCodeEnum.DUPLICATE_ITEM);
		} else {
			applicationDefinitions.add(appDefinition);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException {
		if (applicationDefinitions.contains(appDefinition)) {
			throw new TechnicalException("Adding application " + appDefinition.getApplicationName() + " with id " + appDefinition.getId() + ".", BusinessContextErrorCodeEnum.DUPLICATE_ITEM);
		} else if (insertBeforeIndex < 0 || insertBeforeIndex > applicationDefinitions.size()) {
			throw new TechnicalException("Adding application" + appDefinition.getApplicationName() + " with id " + appDefinition.getId() + " at index " + insertBeforeIndex + ".",
					BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		} else {
			applicationDefinitions.add(insertBeforeIndex, appDefinition);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateApplicationDefinition(IApplicationDefinition appDefinition) throws BusinessException {
		int index = applicationDefinitions.indexOf(appDefinition);
		if (index < 0) {
			throw new TechnicalException("Updating application " + appDefinition.getApplicationName() + " with id '" + appDefinition.getId() + "'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
		}
		applicationDefinitions.set(index, appDefinition);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean deleteApplicationDefinition(IApplicationDefinition appDefinition) {
		return applicationDefinitions.remove(appDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveApplicationDefinition(IApplicationDefinition appDefinition, int index) throws BusinessException {
		if (index < 0 || index >= applicationDefinitions.size()) {
			throw new TechnicalException("Moving application to index " + index + ".", BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		}
		int currentIndex = applicationDefinitions.indexOf(appDefinition);
		if (currentIndex < 0) {
			throw new TechnicalException("Moving application to index " + index + ".", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
		}

		IApplicationDefinition definitionToMove = applicationDefinitions.remove(currentIndex);

		applicationDefinitions.add(index, definitionToMove);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IApplicationDefinition getApplicationDefinition(long id) throws BusinessException {
		for (IApplicationDefinition appDef : applicationDefinitions) {
			if (appDef.getId() == id) {
				return appDef;
			}
		}
		throw new TechnicalException("Retrieve application with id '" + id + "'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IApplicationDefinition getDefaultApplicationDefinition() {

		return defaultApplicationDefinition;
	}
}
