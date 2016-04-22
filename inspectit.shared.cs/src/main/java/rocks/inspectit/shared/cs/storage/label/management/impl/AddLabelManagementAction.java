package rocks.inspectit.shared.cs.storage.label.management.impl;

import java.util.Collection;
import java.util.Collections;

import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.label.management.AbstractLabelManagementAction;
import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;

/**
 * Action for the adding of data related to labels and label types.
 *
 * @author Ivan Senic
 *
 */
public class AddLabelManagementAction extends AbstractLabelManagementAction {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 4099978606572054690L;

	/**
	 * No-arg constructor. Only for the serialization and should not be used.
	 */
	public AddLabelManagementAction() {
		this(Collections.<AbstractStorageLabel<?>> emptyList());
	}

	/**
	 * Constructor when action is for a label type.
	 *
	 * @param labelType
	 *            Label types.
	 */
	public AddLabelManagementAction(AbstractStorageLabelType<?> labelType) {
		super(labelType);
	}

	/**
	 * Constructor when action is for a label(s).
	 *
	 * @param labelList
	 *            Collection of labels.
	 */
	public AddLabelManagementAction(Collection<AbstractStorageLabel<?>> labelList) {
		super(labelList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(IStorageService storageService) {
		if (isLabelTypeBasedAction()) {
			storageService.saveLabelType(this.getLabelType());
		} else if (isLabelBasedAction()) {
			for (AbstractStorageLabel<?> label : this.getLabelList()) {
				storageService.saveLabelToCmr(label);
			}
		}
	}

}
