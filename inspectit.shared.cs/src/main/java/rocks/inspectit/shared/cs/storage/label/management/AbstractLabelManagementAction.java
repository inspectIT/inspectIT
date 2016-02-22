package rocks.inspectit.shared.cs.storage.label.management;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;

/**
 * Abstract class for all label management actions.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractLabelManagementAction implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3640398456749258696L;

	/**
	 * Label type.
	 */
	private final AbstractStorageLabelType<?> labelType;

	/**
	 * Label list.
	 */
	private final Collection<AbstractStorageLabel<?>> labelList;

	/**
	 * Constructor when action is for a label type.
	 * 
	 * @param labelType
	 *            Label types.
	 */
	public AbstractLabelManagementAction(AbstractStorageLabelType<?> labelType) {
		this.labelType = labelType;
		this.labelList = Collections.emptyList();
	}

	/**
	 * Constructor when action is for a label(s).
	 * 
	 * @param labelList
	 *            Collection of labels.
	 */
	public AbstractLabelManagementAction(Collection<AbstractStorageLabel<?>> labelList) {
		this.labelType = null; // NOPMD
		this.labelList = labelList;
	}

	/**
	 * Executes the action.
	 * 
	 * @param storageService
	 *            Storage service that can be used for action purposes.
	 * @throws BusinessException
	 *             If exception occurs during the executions.
	 */
	public abstract void execute(IStorageService storageService) throws BusinessException;

	/**
	 * @return Returns if this action is related to label type.
	 */
	public boolean isLabelTypeBasedAction() {
		return null != labelType;
	}

	/**
	 * @return Returns if this action is related to label(s).
	 */
	public boolean isLabelBasedAction() {
		return null != labelList && !labelList.isEmpty();
	}

	/**
	 * Gets {@link #labelType}.
	 * 
	 * @return {@link #labelType}
	 */
	public AbstractStorageLabelType<?> getLabelType() {
		return labelType;
	}

	/**
	 * Gets {@link #labelList}.
	 * 
	 * @return {@link #labelList}
	 */
	public Collection<AbstractStorageLabel<?>> getLabelList() {
		return labelList;
	}

}
