package info.novatec.inspectit.storage.label.management.impl;

import info.novatec.inspectit.cmr.service.IStorageService;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.management.AbstractLabelManagementAction;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action for the removing of data related to labels and label types.
 * 
 * @author Ivan Senic
 * 
 */
public class RemoveLabelManagementAction extends AbstractLabelManagementAction {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5553041046576560998L;

	/**
	 * Should action also perform remove data from storages.
	 */
	private boolean removeFromStorageAlso;

	/**
	 * No-arg constructor. Only for the serialization and should not be used.
	 */
	public RemoveLabelManagementAction() {
		this(Collections.<AbstractStorageLabel<?>> emptyList(), false);
	}

	/**
	 * Constructor when action is for a label type.
	 * 
	 * @param labelType
	 *            Label types.
	 * @param removeFromStorageAlso
	 *            Should all labels of provided type also be removed from storages.
	 */
	public RemoveLabelManagementAction(AbstractStorageLabelType<?> labelType, boolean removeFromStorageAlso) {
		super(labelType);
		this.removeFromStorageAlso = removeFromStorageAlso;
	}

	/**
	 * Constructor when action is for a label(s).
	 * 
	 * @param labelList
	 *            Collection of labels.
	 * @param removeFromStorageAlso
	 *            Should the labels also be removed from storages.
	 */
	public RemoveLabelManagementAction(Collection<AbstractStorageLabel<?>> labelList, boolean removeFromStorageAlso) {
		super(labelList);
		this.removeFromStorageAlso = removeFromStorageAlso;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(IStorageService storageService) throws StorageException {
		if (isLabelTypeBasedAction()) {
			List<?> returnList = storageService.getLabelSuggestions(this.getLabelType());
			List<AbstractStorageLabel<?>> labels = (List<AbstractStorageLabel<?>>) returnList;
			storageService.removeLabelsFromCmr(labels, this.isRemoveFromStorageAlso());
			storageService.removeLabelType(this.getLabelType());
		} else if (isLabelBasedAction()) {
			Collection<AbstractStorageLabel<?>> labels = this.getLabelList();
			storageService.removeLabelsFromCmr(labels, this.isRemoveFromStorageAlso());
		}
	}

	/**
	 * Gets {@link #removeFromStorageAlso}.
	 * 
	 * @return {@link #removeFromStorageAlso}
	 */
	public boolean isRemoveFromStorageAlso() {
		return removeFromStorageAlso;
	}

	/**
	 * Sets {@link #removeFromStorageAlso}.
	 * 
	 * @param removeFromStorageAlso
	 *            New value for {@link #removeFromStorageAlso}
	 */
	public void setRemoveFromStorageAlso(boolean removeFromStorageAlso) {
		this.removeFromStorageAlso = removeFromStorageAlso;
	}

}
