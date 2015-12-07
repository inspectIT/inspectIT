package info.novatec.inspectit.ci;

import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.enumeration.BusinessContextErrorCodeEnum;
import info.novatec.inspectit.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
public class BusinessContextDefinition {
	/**
	 * The name of the default application.
	 */
	public static final String UNKNOWN_APP = "Unknown Application";

	/**
	 * Default application definition that matches any data. Therefore {@link BooleanExpression}
	 * with true is used.
	 */
	private static final ApplicationDefinition DEFAULT_APPLICATION_DEFINITION = new ApplicationDefinition(ApplicationDefinition.DEFAULT_ID, UNKNOWN_APP, new BooleanExpression(true, true));

	/**
	 * Application definition configurations.
	 */
	@XmlElementWrapper(name = "applications")
	@XmlElementRef
	private final List<ApplicationDefinition> applicationDefinitions = new LinkedList<ApplicationDefinition>();

	/**
	 * Revision. Server for version control and updating control.
	 */
	@XmlAttribute(name = "revision")
	private Integer revision = 1;

	/**
	 * Returns an unmodifiable list of all {@link ApplicationDefinition} instances known in this
	 * business context.
	 *
	 * @return unmodifiable list of all {@link ApplicationDefinition} instances known in this
	 *         business context
	 */
	public List<ApplicationDefinition> getApplicationDefinitions() {
		List<ApplicationDefinition> allApplicationDefinitions = new LinkedList<ApplicationDefinition>(applicationDefinitions);
		allApplicationDefinitions.add(getDefaultApplicationDefinition());
		return Collections.unmodifiableList(allApplicationDefinitions);
	}

	/**
	 * Retrieves the {@link ApplicationDefinition} with the given identifier.
	 *
	 * @param id
	 *            unique id identifying the application definition to retrieve
	 * @return Return the {@link ApplicationDefinition} with the given id, or null if no application
	 *         definition with the passed id could be found.
	 * @throws BusinessException
	 *             if an application with the given id does not exist.
	 */
	public ApplicationDefinition getApplicationDefinition(int id) throws BusinessException {
		if (id == ApplicationDefinition.DEFAULT_ID) {
			return getDefaultApplicationDefinition();
		}

		for (ApplicationDefinition appDef : applicationDefinitions) {
			if (appDef.getId() == id) {
				return appDef;
			}
		}

		throw new BusinessException("Retrieve application with id '" + id + "'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
	}

	/**
	 * Returns the default {@link ApplicationDefinition}.
	 *
	 * @return Returns the default {@link ApplicationDefinition}.
	 */
	public ApplicationDefinition getDefaultApplicationDefinition() {
		return DEFAULT_APPLICATION_DEFINITION;
	}

	/**
	 * Adds application definition to the business context.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} instance to add
	 * @return added {@link ApplicationDefinition} instance
	 * @throws BusinessException
	 *             if application cannot be added.
	 */
	public ApplicationDefinition addApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException {
		return addApplicationDefinition(appDefinition, applicationDefinitions.size());
	}

	/**
	 * Adds application definition to the business context. Inserts it to the list before the
	 * element with the passed index.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @return added {@link ApplicationDefinition} instance
	 * @throws BusinessException
	 *             if application cannot be added.
	 */
	public ApplicationDefinition addApplicationDefinition(ApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException {
		if (appDefinition == null) {
			throw new BusinessException("Adding application 'null'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
		} else if (applicationDefinitions.contains(appDefinition)) {
			throw new BusinessException("Adding application " + appDefinition.getApplicationName() + " with id " + appDefinition.getId() + ".", BusinessContextErrorCodeEnum.DUPLICATE_ITEM);
		} else if (insertBeforeIndex < 0 || insertBeforeIndex > applicationDefinitions.size()) {
			throw new BusinessException("Adding application" + appDefinition.getApplicationName() + " with id " + appDefinition.getId() + " at index " + insertBeforeIndex + ".",
					BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		} else {
			applicationDefinitions.add(insertBeforeIndex, appDefinition);
			return appDefinition;
		}
	}

	/**
	 * Updates the given {@link ApplicationDefinition}.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to update
	 *
	 * @throws BusinessException
	 *             If update fails.
	 * @return the updated {@link ApplicationDefinition} instance.
	 */
	public ApplicationDefinition updateApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException {
		if (appDefinition == null) {
			throw new BusinessException("Updating application 'null'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
		}
		int index = applicationDefinitions.indexOf(appDefinition);
		if (index < 0) {
			throw new BusinessException("Updating application " + appDefinition.getApplicationName() + " with id '" + appDefinition.getId() + "'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
		}
		ApplicationDefinition currentApplicationDefinition = applicationDefinitions.get(index);

		appDefinition.setRevision(appDefinition.getRevision() + 1);
		if (currentApplicationDefinition != appDefinition && currentApplicationDefinition.getRevision() + 1 != appDefinition.getRevision()) { // NOPMD
			throw new BusinessException("Update of the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.REVISION_CHECK_FAILED);
		}

		applicationDefinitions.set(index, appDefinition);

		return appDefinition;
	}

	/**
	 * Deletes the {@link ApplicationDefinition} from the business context.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to delete
	 *
	 * @return Returns true if the business context contained the application
	 */
	public boolean deleteApplicationDefinition(ApplicationDefinition appDefinition) {
		return applicationDefinitions.remove(appDefinition);
	}

	/**
	 * Moves the {@link ApplicationDefinition} to a different position specified by the index
	 * parameter.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to move
	 * @param index
	 *            position to move the {@link ApplicationDefinition} to
	 * @return moved {@link ApplicationDefinition} instance
	 * @throws BusinessException
	 *             If moving fails.
	 */
	public ApplicationDefinition moveApplicationDefinition(ApplicationDefinition appDefinition, int index) throws BusinessException {
		if (appDefinition == null) {
			throw new BusinessException("Moving application 'null'.", BusinessContextErrorCodeEnum.UNKNOWN_APPLICATION);
		} else if (index < 0 || index >= applicationDefinitions.size()) {
			throw new BusinessException("Moving application to index " + index + ".", BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		}
		int currentIndex = applicationDefinitions.indexOf(appDefinition);
		if (currentIndex < 0) {
			throw new BusinessException("Moving application to index " + index + ".", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
		}

		if (index != currentIndex) {
			ApplicationDefinition definitionToMove = applicationDefinitions.remove(currentIndex);
			if (definitionToMove != appDefinition && definitionToMove.getRevision() != appDefinition.getRevision()) { // NOPMD
				throw new BusinessException("Moving the application definition '" + appDefinition.getApplicationName() + "'.", ConfigurationInterfaceErrorCodeEnum.REVISION_CHECK_FAILED);
			}
			definitionToMove.setRevision(definitionToMove.getRevision() + 1);
			applicationDefinitions.add(index, definitionToMove);
			return definitionToMove;
		} else {
			return appDefinition;
		}

	}

	/**
	 * Gets {@link #revision}.
	 *
	 * @return {@link #revision}
	 */
	public Integer getRevision() {
		return revision;
	}

	/**
	 * Sets {@link #revision}.
	 *
	 * @param revision
	 *            New value for {@link #revision}
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationDefinitions == null) ? 0 : applicationDefinitions.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BusinessContextDefinition other = (BusinessContextDefinition) obj;
		if (applicationDefinitions == null) {
			if (other.applicationDefinitions != null) {
				return false;
			}
		} else if (!applicationDefinitions.equals(other.applicationDefinitions)) {
			return false;
		}
		if (revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!revision.equals(other.revision)) {
			return false;
		}
		return true;
	}
}
