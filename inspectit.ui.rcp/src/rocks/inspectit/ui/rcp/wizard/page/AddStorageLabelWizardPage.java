package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.label.edit.LabelValueEditingSupport;
import info.novatec.inspectit.rcp.storage.label.edit.LabelValueEditingSupport.LabelEditListener;
import info.novatec.inspectit.rcp.wizard.ManageLabelWizard;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.NumberStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

/**
 * Page for adding storage labels.
 * 
 * @author Ivan Senic
 * 
 */
public class AddStorageLabelWizardPage extends WizardPage {

	/**
	 * {@link StyledString} that has empty string.
	 */
	protected static final StyledString EMPTY_STYLED_STRING = new StyledString();

	/**
	 * Defines if the data needed to be entered on this wizard page is optional. If it is optional
	 * the page {@link #isPageComplete()} method will return true always, so that wizards can finish
	 * although no label was inserted.
	 */
	private boolean optional;

	/**
	 * Default message.
	 */
	private String defaultMessage = "Define the new label type and its value";

	/**
	 * {@link StorageData} to add label to.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition} where data is located.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * List of available labels types.
	 */
	private List<AbstractStorageLabelType<?>> labelTypeList;

	/**
	 * List of labels to add.
	 */
	private List<AbstractStorageLabel<?>> labelsToAdd = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * Widgets.
	 */
	private Combo labelTypeSelection;

	/**
	 * Table to display labels.
	 */
	private TableViewer labelsTableViewer;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * Remove label button.
	 */
	private Button removeLabelButton;

	/**
	 * Page complete listener.
	 */
	private Listener pageCompleteListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			setPageComplete(isPageComplete());
		}
	};

	/**
	 * Value column.
	 */
	private TableViewerColumn value;

	/**
	 * Default constructor. Use this constructor when storage data is known. Note that using the
	 * page this way, {@link #optional} value is set to true, thus wizard page would not allow to
	 * finish until label is correctly created.
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 */
	public AddStorageLabelWizardPage(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super("Add New Labels");
		this.setTitle("Add New Labels");
		defaultMessage += " for the storage '" + storageData.getName() + "'";
		this.setMessage(defaultMessage);
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.optional = false;
		labelTypeList = cmrRepositoryDefinition.getStorageService().getAllLabelTypes();
	}

	/**
	 * The constructor for usage when storage to label is not known in advance. Note that using this
	 * constructor will set {@link #optional} to true.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 */
	public AddStorageLabelWizardPage(CmrRepositoryDefinition cmrRepositoryDefinition) {
		super("Add New Labels");
		this.setTitle("Add New Labels");
		defaultMessage = "Optionally add one or more labels to the selected storage";
		this.setMessage(defaultMessage);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.optional = true;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(4, false));

		Label l = new Label(main, SWT.NONE);
		l.setText("Label type:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		labelTypeSelection = new Combo(main, SWT.READ_ONLY);
		labelTypeSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Button addButton = new Button(main, SWT.PUSH);
		addButton.setEnabled(false);
		addButton.setText("Add");
		addButton.setToolTipText("Add Label");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = labelTypeSelection.getSelectionIndex();
				if (-1 != index) {
					AbstractStorageLabelType<?> selectedLabelType = labelTypeList.get(index);

					if (selectedLabelType.isOnePerStorage() && isLabelTypePresent(selectedLabelType)) {
						setMessage("Selected label type is one-per-storage. New label value will overwrite the old one.", IMessageProvider.WARNING);
						Iterator<AbstractStorageLabel<?>> it = labelsToAdd.iterator();
						while (it.hasNext()) {
							if (Objects.equals(it.next().getStorageLabelType(), selectedLabelType)) {
								it.remove();
								break;
							}
						}
					} else {
						setMessage(defaultMessage);
					}

					if (selectedLabelType.getValueClass().equals(Boolean.class)) {
						BooleanStorageLabel booleanStorageLabel = new BooleanStorageLabel();
						booleanStorageLabel.setStorageLabelType((AbstractStorageLabelType<Boolean>) selectedLabelType);
						labelsToAdd.add(booleanStorageLabel);
					} else if (selectedLabelType.getValueClass().equals(Date.class)) {
						DateStorageLabel dateStorageLabel = new DateStorageLabel();
						dateStorageLabel.setStorageLabelType((AbstractStorageLabelType<Date>) selectedLabelType);
						labelsToAdd.add(dateStorageLabel);
					} else if (selectedLabelType.getValueClass().equals(Number.class)) {
						NumberStorageLabel numberStorageLabel = new NumberStorageLabel();
						numberStorageLabel.setStorageLabelType((AbstractStorageLabelType<Number>) selectedLabelType);
						labelsToAdd.add(numberStorageLabel);
					} else if (selectedLabelType.getValueClass().equals(String.class)) {
						StringStorageLabel stringStorageLabel = new StringStorageLabel();
						stringStorageLabel.setStorageLabelType((AbstractStorageLabelType<String>) selectedLabelType);
						labelsToAdd.add(stringStorageLabel);
					}
					labelsTableViewer.refresh();
					labelsTableViewer.editElement(labelsToAdd.get(labelsToAdd.size() - 1), 1);
				}
			}
		});

		labelTypeSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addButton.setEnabled(labelTypeSelection.getSelectionIndex() >= 0);
			}
		});

		Button manageLabelsButton = new Button(main, SWT.PUSH);
		manageLabelsButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_LABEL));
		manageLabelsButton.setToolTipText("Manage Labels on Repository");
		manageLabelsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		manageLabelsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ManageLabelWizard manageLabelWizard = new ManageLabelWizard(cmrRepositoryDefinition);
				WizardDialog wizardDialog = new WizardDialog(getShell(), manageLabelWizard);
				wizardDialog.open();
				if (wizardDialog.getReturnCode() == WizardDialog.OK) {
					updateLabelTypes();
				}
			}

		});

		// table
		createLabelTable(main);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		gd.minimumHeight = 100;
		labelsTableViewer.getTable().setLayoutData(gd);

		addButton.addListener(SWT.Selection, pageCompleteListener);
		labelsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				removeLabelButton.setEnabled(!labelsTableViewer.getSelection().isEmpty());
			}
		});

		labelsTableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					deleteSelectedlabels();
				}
			}
		});

		// empty help label
		gd = new GridData();
		gd.horizontalSpan = 2;
		new Label(main, SWT.NONE).setLayoutData(gd);

		removeLabelButton = new Button(main, SWT.PUSH);
		removeLabelButton.setText("Remove");
		removeLabelButton.setToolTipText("Remove Selected Labels");
		removeLabelButton.setEnabled(false);
		removeLabelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		removeLabelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelectedlabels();
			}

		});

		updateLabelTypes();
		setControl(main);
	}

	/**
	 * Updates the labels types available on the server.
	 * 
	 */
	private void updateLabelTypes() {
		Job updateLabelsTypes = new Job("Loading Label Types") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					labelTypeList = cmrRepositoryDefinition.getStorageService().getAllLabelTypes();
				} else {
					labelTypeList = Collections.emptyList();
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						labelTypeSelection.removeAll();
						for (AbstractStorageLabelType<?> labelType : labelTypeList) {
							labelTypeSelection.add(TextFormatter.getLabelName(labelType));
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		updateLabelsTypes.schedule();
	}

	/**
	 * Deletes selected labels.
	 */
	private void deleteSelectedlabels() {
		StructuredSelection structuredSelection = (StructuredSelection) labelsTableViewer.getSelection();
		if (!structuredSelection.isEmpty()) {
			for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
				labelsToAdd.remove(it.next());
			}
			labelsTableViewer.refresh();
			setPageComplete(isPageComplete());
		}
	}

	/**
	 * Creates the table for the labels.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	private void createLabelTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);

		labelsTableViewer = new TableViewer(table);

		TableViewerColumn type = new TableViewerColumn(labelsTableViewer, SWT.NONE);
		type.getColumn().setText("Label");
		type.getColumn().setMoveable(false);
		type.getColumn().setResizable(true);
		type.getColumn().setWidth(200);

		value = new TableViewerColumn(labelsTableViewer, SWT.NONE);
		value.getColumn().setText("Value");
		value.getColumn().setMoveable(false);
		value.getColumn().setResizable(true);
		value.getColumn().setWidth(100);
		LabelValueEditingSupport editingSupport = new LabelValueEditingSupport(labelsTableViewer, storageData, cmrRepositoryDefinition);
		editingSupport.addLabelEditListener(new LabelEditListener() {
			@Override
			public void preLabelValueChange(AbstractStorageLabel<?> label) {
			}

			@Override
			public void postLabelValueChange(AbstractStorageLabel<?> label) {
				setPageComplete(isPageComplete());
			}

		});
		value.setEditingSupport(editingSupport);

		labelsTableViewer.setContentProvider(new ArrayContentProvider());
		labelsTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabel) {
					AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
					switch (index) {
					case 0:
						return new StyledString(TextFormatter.getLabelName(label));
					case 1:
						if (null != label.getValue()) {
							return new StyledString(TextFormatter.getLabelValue(label, false));
						}
					default:
						break;
					}
				}
				return EMPTY_STYLED_STRING;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabel) {
					return ImageFormatter.getImageForLabel(((AbstractStorageLabel<?>) element).getStorageLabelType());
				}
				return null;
			}
		});

		labelsTableViewer.setInput(labelsToAdd);

	}

	/**
	 * Returns if the label type is present in the storage or in the list of labels to add.
	 * 
	 * @param selectedLabelType
	 *            Selected {@link AbstractStorageLabelType}.
	 * @return True if label type already exists.
	 */
	private boolean isLabelTypePresent(AbstractStorageLabelType<?> selectedLabelType) {
		if (storageData.isLabelPresent(selectedLabelType)) {
			return true;
		}
		for (AbstractStorageLabel<?> label : labelsToAdd) {
			if (ObjectUtils.equals(selectedLabelType, label.getStorageLabelType())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (optional) {
			return true;
		} else if (!labelsToAdd.isEmpty()) {
			for (AbstractStorageLabel<?> label : labelsToAdd) {
				if (null != label.getValue()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets {@link #labelsToAdd}.
	 * 
	 * @return {@link #labelsToAdd}
	 */
	public List<AbstractStorageLabel<?>> getLabelsToAdd() {
		Iterator<AbstractStorageLabel<?>> it = labelsToAdd.iterator();
		while (it.hasNext()) {
			if (null == it.next().getValue()) {
				it.remove();
			}
		}

		return labelsToAdd;
	}

	/**
	 * Sets {@link #storageData}.
	 * 
	 * @param storageData
	 *            New value for {@link #storageData}
	 */
	public void setStorageData(StorageData storageData) {
		this.storageData = storageData;
		// update the editing support
		LabelValueEditingSupport editingSupport = new LabelValueEditingSupport(labelsTableViewer, storageData, cmrRepositoryDefinition);
		editingSupport.addLabelEditListener(new LabelEditListener() {
			@Override
			public void preLabelValueChange(AbstractStorageLabel<?> label) {
			}

			@Override
			public void postLabelValueChange(AbstractStorageLabel<?> label) {
				setPageComplete(isPageComplete());
			}

		});
		value.setEditingSupport(editingSupport);
	}

}
