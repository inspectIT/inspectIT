package info.novatec.inspectit.rcp.storage.label.edit;

import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.BooleanStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.DateStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.NumberStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.StringStorageLabelComposite;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Editing support for the values of the table.
 * 
 * @author Ivan Senic
 * 
 */
public class LabelValueEditingSupport extends EditingSupport {

	/**
	 * {@link StorageData} needed for updating.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition} needed for updating.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Main cell editor.
	 */
	private ComboBoxCellEditor generalCellEditor;

	/**
	 * Cell editor for the boolean valued labels.
	 */
	private ComboBoxCellEditor booleanCellEditor;

	/**
	 * Table that is being edited.
	 */
	private Table table;

	/**
	 * Suggestion label list.
	 */
	private List<AbstractStorageLabel<?>> suggestionLabelList = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * Label that is being edited.
	 */
	private AbstractStorageLabel<?> editingLabel;

	/**
	 * List of listeners.
	 */
	private List<LabelEditListener> labelEditListeners = new ArrayList<LabelEditListener>();

	/**
	 * Default constructor.
	 * 
	 * @param viewer
	 *            Viewer.
	 * @param storageData
	 *            {@link StorageData}.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 */
	public LabelValueEditingSupport(TableViewer viewer, StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(viewer);
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		table = viewer.getTable();
		generalCellEditor = new ComboBoxCellEditor(table, new String[0], SWT.READ_ONLY);
		booleanCellEditor = new ComboBoxCellEditor(table, new String[] { "Yes", "No" }, SWT.READ_ONLY);

		int activationStyle = ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION | ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION
				| ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION;
		generalCellEditor.setActivationStyle(activationStyle);
		booleanCellEditor.setActivationStyle(activationStyle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CellEditor getCellEditor(Object element) {
		AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
		if (label instanceof BooleanStorageLabel) {
			return booleanCellEditor;
		} else {
			List<String> items = new ArrayList<String>();
			items.add("[create new value]");
			if (!Objects.equals(label, editingLabel)) {
				// if we still update the same label we don't need to reload everything
				if (label.getStorageLabelType().isValueReusable() && cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					suggestionLabelList.clear();
					suggestionLabelList.addAll(cmrRepositoryDefinition.getStorageService().getLabelSuggestions(label.getStorageLabelType()));
					if (!suggestionLabelList.isEmpty() && null != storageData) {
						suggestionLabelList.removeAll(storageData.getLabelList());
					}
				}
			}

			if (!suggestionLabelList.isEmpty()) {
				Collections.sort(suggestionLabelList);
				for (AbstractStorageLabel<?> existingLabel : suggestionLabelList) {
					items.add(TextFormatter.getLabelValue(existingLabel, false));
				}
			}

			generalCellEditor.setItems(items.toArray(new String[items.size()]));
			return generalCellEditor;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean canEdit(Object element) {
		AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
		return label.getStorageLabelType().isEditable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getValue(Object element) {
		AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
		editingLabel = label;
		if (null != label.getValue()) {
			if (label instanceof BooleanStorageLabel) {
				if (((BooleanStorageLabel) label).getValue().booleanValue()) {
					return 0;
				} else {
					return 1;
				}
			} else {
				String value = TextFormatter.getLabelValue(label, false);
				int i = 1;
				for (AbstractStorageLabel<?> suggestionLabel : suggestionLabelList) {
					if (Objects.equals(value, TextFormatter.getLabelValue(suggestionLabel, false))) {
						return i;
					}
					i++;
				}
			}
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(Object element, Object value) {
		int index = ((Integer) value).intValue();
		if (index >= 0) {
			AbstractStorageLabel<Object> label = (AbstractStorageLabel<Object>) element;
			if (label.getStorageLabelType().getValueClass().equals(Boolean.class)) {
				Boolean newValue = Boolean.valueOf(0 == index);
				changeValue(label, newValue);
			} else {
				if (index == 0) {
					// create new
					CreateValueDialog createValueDialog = new CreateValueDialog(table.getShell(), label.getStorageLabelType());
					createValueDialog.open();
					if (createValueDialog.getReturnCode() == Dialog.OK) {
						AbstractStorageLabel<?> createdLabel = createValueDialog.getCreatedLabel();
						changeValue(label, createdLabel.getValue());
						if (!suggestionLabelList.contains(createdLabel)) {
							suggestionLabelList.add(createdLabel);
						}
					}

				} else if (index > 0) {
					AbstractStorageLabel<?> suggestionLabel = suggestionLabelList.get(index - 1);
					Object labelValue = suggestionLabel.getValue();
					changeValue(label, labelValue);
				}

			}
			getViewer().refresh();
		}
	}

	/**
	 * Reforms the value change and informs the listeners.
	 * 
	 * @param label
	 *            Label to have value changed.
	 * @param newValue
	 *            New value to set.
	 */
	private void changeValue(AbstractStorageLabel<Object> label, Object newValue) {
		if (!Objects.equals(label.getValue(), newValue)) {
			synchronized (labelEditListeners) {
				for (LabelEditListener listener : labelEditListeners) {
					listener.preLabelValueChange(label);
				}
			}

			label.setValue(newValue);

			synchronized (labelEditListeners) {
				for (LabelEditListener listener : labelEditListeners) {
					listener.postLabelValueChange(label);
				}
			}
		}
	}

	/**
	 * Adds a {@link LabelEditListener}.
	 * 
	 * @param listener
	 *            Listener to add.
	 */
	public void addLabelEditListener(LabelEditListener listener) {
		synchronized (labelEditListeners) {
			if (!labelEditListeners.contains(listener)) {
				labelEditListeners.add(listener);
			}
		}
	}

	/**
	 * Removes a {@link LabelEditListener}.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	public void removeLabelEditListener(LabelEditListener listener) {
		synchronized (labelEditListeners) {
			labelEditListeners.remove(listener);
		}
	}

	/**
	 * Listener interface that will be called prior to and after label value editing.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public interface LabelEditListener {

		/**
		 * Called before the label is changed.
		 * 
		 * @param label
		 *            {@link AbstractStorageLabel}.
		 */
		void preLabelValueChange(AbstractStorageLabel<?> label);

		/**
		 * Called after the label is changed.
		 * 
		 * @param label
		 *            {@link AbstractStorageLabel}.
		 */
		void postLabelValueChange(AbstractStorageLabel<?> label);

	}

	/**
	 * Dialog for creating new values.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class CreateValueDialog extends Dialog {

		/**
		 * Label type.
		 */
		private AbstractStorageLabelType<?> labelType;

		/**
		 * Label to hold the created value.
		 */
		private AbstractStorageLabel<?> label;

		/**
		 * Composite for definition.
		 */
		private AbstractStorageLabelComposite storageLabelComposite;

		/**
		 * OK button.
		 */
		private Button okButton;

		/**
		 * Default constructor.
		 * 
		 * @param parentShell
		 *            Shell.
		 * @param labelType
		 *            Type of label.
		 */
		protected CreateValueDialog(Shell parentShell, AbstractStorageLabelType<?> labelType) {
			super(parentShell);
			this.labelType = labelType;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Create New Label Value");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(storageLabelComposite.isInputValid());
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(Composite parent) {
			if (labelType.getValueClass().equals(Boolean.class)) {
				storageLabelComposite = new BooleanStorageLabelComposite(parent, SWT.NONE, (AbstractStorageLabelType<Boolean>) labelType);
			} else if (labelType.getValueClass().equals(Date.class)) {
				storageLabelComposite = new DateStorageLabelComposite(parent, SWT.NONE, (AbstractStorageLabelType<Date>) labelType);
			} else if (labelType.getValueClass().equals(Number.class)) {
				storageLabelComposite = new NumberStorageLabelComposite(parent, SWT.NONE, (AbstractStorageLabelType<Number>) labelType);
			} else if (labelType.getValueClass().equals(String.class)) {
				storageLabelComposite = new StringStorageLabelComposite(parent, SWT.NONE, (AbstractStorageLabelType<String>) labelType);
			}

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.minimumWidth = 300;
			storageLabelComposite.setLayoutData(gd);
			storageLabelComposite.addListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(storageLabelComposite.isInputValid());
				}
			});
			return storageLabelComposite;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (Dialog.OK == buttonId) {
				label = storageLabelComposite.getStorageLabel();
			}
			super.buttonPressed(buttonId);
		}

		/**
		 * Gets {@link #label}.
		 * 
		 * @return {@link #label}
		 */
		public AbstractStorageLabel<?> getCreatedLabel() {
			return label;
		}

	}
}
