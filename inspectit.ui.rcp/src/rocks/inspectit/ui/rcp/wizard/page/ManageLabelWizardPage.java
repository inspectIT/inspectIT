package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.BooleanStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.DateStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.NumberStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.StringStorageLabelComposite;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.management.AbstractLabelManagementAction;
import info.novatec.inspectit.storage.label.management.impl.AddLabelManagementAction;
import info.novatec.inspectit.storage.label.management.impl.RemoveLabelManagementAction;
import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomBooleanLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomNumberLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomStringLabelType;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * Manage label page.
 * 
 * @author Ivan Senic
 * 
 */
public class ManageLabelWizardPage extends WizardPage {

	/**
	 * Default message.
	 */
	private static final String DEFAULT_MESSAGE = "Add and remove labels that can be used later for labeling storages";

	/**
	 * Empty style string.
	 */
	private static final StyledString EMPTY_STYLED_STRING = new StyledString();

	/**
	 * Available remote label list.
	 */
	private List<AbstractStorageLabel<?>> labelList = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * Available remote label type list.
	 */
	private List<AbstractStorageLabelType<?>> labelTypeList = new ArrayList<AbstractStorageLabelType<?>>();

	/**
	 * Table viewer for labels.
	 */
	private TableViewer labelsTableViewer;

	/**
	 * Remove label button.
	 */
	private Button removeLabels;

	/**
	 * Add label button.
	 */
	private Button createLabel;

	/**
	 * {@link TableViewer} for label types.
	 */
	private TableViewer labelTypeTableViewer;

	/**
	 * Create label type button.
	 */
	private Button createLabelType;

	/**
	 * Remove label types button.
	 */
	private Button removeLabelType;

	/**
	 * List of labels in storages.
	 */
	private Set<AbstractStorageLabel<?>> labelsInStorages = new HashSet<AbstractStorageLabel<?>>();

	/**
	 * List of actions that need to be executed at the end of wizard.
	 */
	private List<AbstractLabelManagementAction> managementActions = new LinkedList<AbstractLabelManagementAction>();

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            CMR to manage labels for.
	 */
	public ManageLabelWizardPage(CmrRepositoryDefinition cmrRepositoryDefinition) {
		super("Manage Labels");
		this.setTitle("Manage Labels");
		this.setMessage(DEFAULT_MESSAGE);
		if (null != cmrRepositoryDefinition) {
			this.setMessage("Label management for repository '" + cmrRepositoryDefinition.getName() + "' (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
		}
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		// label type - upper composite
		createLabelTypeTable(sashForm);
		// labels - lower composite
		createLabelTable(sashForm);

		sashForm.setWeights(new int[] { 1, 1 });
		setControl(sashForm);
	}

	/**
	 * Creates the table for the label types.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	private void createLabelTypeTable(Composite parent) {
		Composite upperComposite = new Composite(parent, SWT.NONE);
		upperComposite.setLayout(new GridLayout(2, false));

		Label labelTypeInfo = new Label(upperComposite, SWT.NONE);
		labelTypeInfo.setText("Existing label types");
		labelTypeInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		Table labelTypeTable = new Table(upperComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION);
		labelTypeTable.setHeaderVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		gd.heightHint = 150;
		labelTypeTable.setLayoutData(gd);

		TableColumn column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setMoveable(false);
		column.setResizable(false);
		column.setWidth(25);

		column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setText("Name");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(200);

		column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setText("Value type");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(100);

		column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setText("One per storage");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(80);

		labelTypeTableViewer = new TableViewer(labelTypeTable);
		labelTypeTableViewer.setContentProvider(new ArrayContentProvider());
		labelTypeTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabelType) {
					AbstractStorageLabelType<?> labelType = (AbstractStorageLabelType<?>) element;
					switch (index) {
					case 1:
						return new StyledString(TextFormatter.getLabelName(labelType));
					case 2:
						return new StyledString(TextFormatter.getLabelValueType(labelType));
					case 3:
						if (labelType.isOnePerStorage()) {
							return new StyledString("Yes");
						} else {
							return new StyledString("No");
						}
					default:
						break;
					}
				}
				return EMPTY_STYLED_STRING;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabelType) {
					if (isLabelTypeExistsInStorage((AbstractStorageLabelType<?>) element, labelsInStorages)) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE);
					}
				} else if (index == 1 && element instanceof AbstractStorageLabelType) {
					return ImageFormatter.getImageForLabel((AbstractStorageLabelType<?>) element);
				}
				return null;
			}
		});
		labelTypeTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				manageLabelTypeSlection();
			}
		});
		labelTypeTableViewer.setInput(labelTypeList);

		createLabelType = new Button(upperComposite, SWT.PUSH);
		createLabelType.setText("Add");
		createLabelType.setToolTipText("Create New Label Type");
		createLabelType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		createLabelType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateLabelTypeDialog createDialog = new CreateLabelTypeDialog(getShell());
				createDialog.open();
				if (createDialog.getReturnCode() == Dialog.OK) {
					AbstractStorageLabelType<?> createdType = createDialog.getCreatedLabelType();
					AddLabelManagementAction addLabelManagementAction = new AddLabelManagementAction(createdType);
					managementActions.add(addLabelManagementAction);
					labelTypeList.add(createdType);
					labelTypeTableViewer.refresh();
				}
			}
		});

		removeLabelType = new Button(upperComposite, SWT.PUSH);
		removeLabelType.setText("Remove");
		removeLabelType.setToolTipText("Remove Label Type");
		removeLabelType.setEnabled(false);
		removeLabelType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeLabelType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractStorageLabelType<?> typeToRemove = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
				boolean removeFromStorage = false;
				if (isLabelTypeExistsInStorage(typeToRemove, labelsInStorages)) {
					MessageDialog messageDialog = new MessageDialog(getShell(), "Remove Label Type", null, "Should all labels of selected type be removed also from storages where they are used?",
							MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
					if (messageDialog.open() == 0) {
						removeFromStorage = true;
					}
				}
				RemoveLabelManagementAction removeLabelManagementAction = new RemoveLabelManagementAction(typeToRemove, removeFromStorage);
				managementActions.add(removeLabelManagementAction);
				labelTypeList.remove(typeToRemove);
				Iterator<AbstractStorageLabel<?>> it = labelList.iterator();
				while (it.hasNext()) {
					if (ObjectUtils.equals(typeToRemove, it.next().getStorageLabelType())) {
						it.remove();
					}
				}
				labelTypeTableViewer.refresh();
			}
		});

		Job loadDataJob = new Job("Loading Labels Data") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				labelsInStorages.addAll(cmrRepositoryDefinition.getStorageService().getAllLabelsInStorages());
				labelTypeList.addAll(cmrRepositoryDefinition.getStorageService().getAllLabelTypes());
				labelList.addAll(cmrRepositoryDefinition.getStorageService().getAllLabels());

				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						labelsTableViewer.refresh();
						labelTypeTableViewer.refresh();
					}
				}, labelsTableViewer.getTable(), labelTypeTableViewer.getTable());

				return Status.OK_STATUS;
			}
		};
		loadDataJob.schedule();
	}

	/**
	 * Creates the table for the labels.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	private void createLabelTable(Composite parent) {
		Composite lowerComposite = new Composite(parent, SWT.NONE);
		lowerComposite.setLayout(new GridLayout(2, false));

		Table table = new Table(lowerComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		gd.heightHint = 150;
		table.setLayoutData(gd);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setMoveable(false);
		column.setResizable(false);
		column.setWidth(25);

		column = new TableColumn(table, SWT.NONE);
		column.setText("Label");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(200);

		column = new TableColumn(table, SWT.NONE);
		column.setText("Value");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(100);

		labelsTableViewer = new TableViewer(table);
		labelsTableViewer.setContentProvider(new ArrayContentProvider());
		labelsTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabel) {
					AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
					switch (index) {
					case 1:
						return new StyledString(TextFormatter.getLabelName(label));
					case 2:
						return new StyledString(TextFormatter.getLabelValue(label, false));
					default:
						break;
					}
				}
				return EMPTY_STYLED_STRING;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabel) {
					if (isLabelExistsInStorage((AbstractStorageLabel<?>) element, labelsInStorages)) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE);
					}
				}
				if (index == 1 && element instanceof AbstractStorageLabel) {
					return ImageFormatter.getImageForLabel(((AbstractStorageLabel<?>) element).getStorageLabelType());
				}
				return null;
			}
		});
		labelsTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof AbstractStorageLabel && e2 instanceof AbstractStorageLabel) {
					return ((AbstractStorageLabel<?>) e1).compareTo((AbstractStorageLabel<?>) e2);
				}
				return super.compare(viewer, e1, e2);
			}
		});
		labelsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (labelsTableViewer.getSelection().isEmpty()) {
					removeLabels.setEnabled(false);
				} else {
					removeLabels.setEnabled(true);
				}
			}
		});

		createLabel = new Button(lowerComposite, SWT.PUSH);
		createLabel.setText("Add");
		createLabel.setToolTipText("Create New Label");
		createLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		createLabel.setEnabled(false);
		createLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractStorageLabelType<?> suggestedLabelType = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
				CreateLabelDialog createLabelDialog = new CreateLabelDialog(getShell(), labelTypeList, suggestedLabelType);
				createLabelDialog.open();
				if (createLabelDialog.getReturnCode() == Dialog.OK) {
					AbstractStorageLabel<?> createdLabel = createLabelDialog.getCreatedLabel();
					List<AbstractStorageLabel<?>> createdList = new ArrayList<AbstractStorageLabel<?>>(1);
					createdList.add(createdLabel);
					AddLabelManagementAction addLabelManagementAction = new AddLabelManagementAction(createdList);
					managementActions.add(addLabelManagementAction);
					labelList.add(createdLabel);
					manageLabelTypeSlection();
				}
			}
		});

		removeLabels = new Button(lowerComposite, SWT.PUSH);
		removeLabels.setText("Remove");
		removeLabels.setToolTipText("Remove Label(s)");
		removeLabels.setEnabled(false);
		removeLabels.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeLabels.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<AbstractStorageLabel<?>> labelsToRemove = ((StructuredSelection) labelsTableViewer.getSelection()).toList();
				boolean inStorage = false;
				for (AbstractStorageLabel<?> label : labelsToRemove) {
					if (isLabelExistsInStorage(label, labelsInStorages)) {
						inStorage = true;
						break;
					}
				}

				boolean removeFromStorage = false;
				if (inStorage) {
					MessageDialog messageDialog = new MessageDialog(getShell(), "Remove Label(s)", null, "Should all selected labels be removed also from storages where they are used?",
							MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
					if (messageDialog.open() == 0) {
						removeFromStorage = true;
						labelsInStorages.removeAll(labelsToRemove);
					}
				}
				RemoveLabelManagementAction removeLabelManagementAction = new RemoveLabelManagementAction(labelsToRemove, removeFromStorage);
				managementActions.add(removeLabelManagementAction);
				labelList.removeAll(labelsToRemove);
				manageLabelTypeSlection();
				labelTypeTableViewer.refresh(true);
			}
		});

		FormText storageInfo = new FormText(lowerComposite, SWT.NONE);
		storageInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		storageInfo.setImage("storage", InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE));
		storageInfo.setText("<form><p>The icon <img href=\"storage\"/> denotes that label type or label is currently used in one or more storages.</p></form>", true, false);
	}

	/**
	 * Returns if any storage in collection of storages does contain at least one label that is of
	 * the given label type.
	 * 
	 * @param labelType
	 *            Label type to search for.
	 * @param labelsInStorages
	 *            Storages to check.
	 * @return True if at least one label of given type exists in one of given storages.
	 */
	private boolean isLabelTypeExistsInStorage(AbstractStorageLabelType<?> labelType, Set<AbstractStorageLabel<?>> labelsInStorages) {
		for (AbstractStorageLabel<?> label : labelsInStorages) {
			if (ObjectUtils.equals(label.getStorageLabelType(), labelType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if any storage in collection of storages does contain given label.
	 * 
	 * @param label
	 *            Label to search for.
	 * @param labelsInStorages
	 *            Storages to check.
	 * @return True if label exists in one of given storages.
	 */
	private boolean isLabelExistsInStorage(AbstractStorageLabel<?> label, Set<AbstractStorageLabel<?>> labelsInStorages) {
		for (AbstractStorageLabel<?> labelInStorage : labelsInStorages) {
			if (ObjectUtils.equals(label, labelInStorage)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Manages the label type selection.
	 */
	private void manageLabelTypeSlection() {
		if (!labelTypeTableViewer.getSelection().isEmpty()) {
			AbstractStorageLabelType<?> labelType = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
			List<AbstractStorageLabel<?>> inputForLabelTable = new ArrayList<AbstractStorageLabel<?>>();
			for (AbstractStorageLabel<?> label : labelList) {
				if (ObjectUtils.equals(label.getStorageLabelType(), labelType)) {
					inputForLabelTable.add(label);
				}
			}
			if (labelType.isValueReusable()) {
				createLabel.setEnabled(true);
				labelsTableViewer.getTable().setEnabled(true);
			} else {
				createLabel.setEnabled(false);
				labelsTableViewer.getTable().setEnabled(false);
			}
			removeLabelType.setEnabled(AbstractCustomStorageLabelType.class.isAssignableFrom(labelType.getClass()));
			labelsTableViewer.setInput(inputForLabelTable);
			labelsTableViewer.refresh();
		} else {
			removeLabelType.setEnabled(false);
			createLabel.setEnabled(true);
			labelsTableViewer.getTable().setEnabled(true);
			labelsTableViewer.setInput(null);
			labelsTableViewer.refresh();
		}
	}

	/**
	 * Gets {@link #shouldRefreshStorages}.
	 * 
	 * @return {@link #shouldRefreshStorages}
	 */
	public boolean isShouldRefreshStorages() {
		return !managementActions.isEmpty();
	}

	/**
	 * Gets {@link #managementActions}.
	 * 
	 * @return {@link #managementActions}
	 */
	public List<AbstractLabelManagementAction> getManagementActions() {
		return managementActions;
	}

	/**
	 * Create label type dialog.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class CreateLabelTypeDialog extends TitleAreaDialog {

		/**
		 * Available type list.
		 */
		private final AbstractCustomStorageLabelType<?>[] availableTypes = new AbstractCustomStorageLabelType<?>[] { new CustomBooleanLabelType(), new CustomDateLabelType(),
				new CustomNumberLabelType(), new CustomStringLabelType() };

		/**
		 * Reference to the created label type.
		 */
		private AbstractStorageLabelType<?> createdLabelType;

		/**
		 * Keys of the images that can be used in custom labels.
		 */
		private final String[] imageKeys;

		/**
		 * Index of the selected image key.
		 */
		private int selectedImageKeyIndex = -1;

		/** Image buttons. */
		private Button[] imageButtons;
		/** Ok Button. */
		private Button okButton;
		/** The main window. */
		private Composite main;
		/** the name. */
		private Text name;
		/** Selection of the value type. */
		private Combo valueTypeSelection;
		/** yes button. */
		private Button yesButton;
		/** no button. */
		private Button noButton;

		/**
		 * Default constructor.
		 * 
		 * @param parentShell
		 *            Shell
		 */
		public CreateLabelTypeDialog(Shell parentShell) {
			super(parentShell);
			this.imageKeys = ImageFormatter.LABEL_ICONS;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Create Label Type");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void create() {
			super.create();
			this.setTitle("Create Label Type");
			this.setMessage("Define properties for the new label type", IMessageProvider.INFORMATION);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				createdLabelType = ensureLabelType();
			}
			super.buttonPressed(buttonId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			main = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumWidth = 350;
			main.setLayoutData(gd);
			main.setLayout(new GridLayout(3, false));

			new Label(main, SWT.NONE).setText("Name:");
			name = new Text(main, SWT.BORDER);
			name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			new Label(main, SWT.NONE).setText("Value type:");
			valueTypeSelection = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
			valueTypeSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			for (AbstractStorageLabelType<?> labelType : availableTypes) {
				valueTypeSelection.add(TextFormatter.getLabelValueType(labelType));
			}

			new Label(main, SWT.NONE).setText("One per storage:");
			yesButton = new Button(main, SWT.RADIO);
			yesButton.setText("Yes");
			yesButton.setSelection(true);

			noButton = new Button(main, SWT.RADIO);
			noButton.setText("No");

			Label l = new Label(main, SWT.NONE);
			l.setText("Icon:");
			l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			final ScrolledComposite scrolledComposite = new ScrolledComposite(main, SWT.V_SCROLL | SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
			gd.widthHint = 1;
			gd.heightHint = 120;
			scrolledComposite.setLayoutData(gd);

			final Composite iconComposite = new Composite(scrolledComposite, SWT.NONE);
			iconComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

			// create buttons for selecting images
			SelectionAdapter buttonListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					for (int i = 0; i < imageButtons.length; i++) {
						if (Objects.equals(imageButtons[i], e.widget)) {
							if (selectedImageKeyIndex == i) {
								// de-selection occurred
								selectedImageKeyIndex = -1;
							} else {
								selectedImageKeyIndex = i;
							}
						} else {
							imageButtons[i].setSelection(false);
						}
					}
				}
			};
			imageButtons = new Button[imageKeys.length];
			for (int i = 0; i < imageKeys.length; i++) {
				Button button = new Button(iconComposite, SWT.TOGGLE);
				button.setImage(InspectIT.getDefault().getImage(imageKeys[i]));
				button.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				button.addSelectionListener(buttonListener);
				imageButtons[i] = button;
			}

			RowLayout layout = new RowLayout(SWT.HORIZONTAL);
			layout.wrap = true;
			layout.fill = true;
			layout.justify = true;
			layout.spacing = 7;
			iconComposite.setLayout(layout);
			scrolledComposite.setContent(iconComposite);
			scrolledComposite.setExpandVertical(true);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					Rectangle r = scrolledComposite.getClientArea();
					scrolledComposite.setMinSize(iconComposite.computeSize(r.width, SWT.DEFAULT));
				}
			});

			final Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(isInputValid());
				}

			};
			name.addListener(SWT.Modify, listener);
			valueTypeSelection.addListener(SWT.Selection, listener);
			yesButton.addListener(SWT.Selection, listener);
			noButton.addListener(SWT.Selection, listener);

			name.forceFocus();
			return main;
		}

		/**
		 * @return Returns if the input of the dialog is valid.
		 */
		private boolean isInputValid() {
			if (name.getText().trim().isEmpty()) {
				return false;
			}
			if (valueTypeSelection.getSelectionIndex() == -1) {
				return false;
			}
			return true;
		}

		/**
		 * Ensures that the label type is correctly created.
		 * 
		 * @return {@link AbstractStorageLabelType}
		 */
		private AbstractStorageLabelType<?> ensureLabelType() {
			AbstractCustomStorageLabelType<?> labelType = availableTypes[valueTypeSelection.getSelectionIndex()];
			labelType.setName(name.getText().trim());
			labelType.setOnePerStorage(yesButton.getSelection());
			if (selectedImageKeyIndex >= 0 && selectedImageKeyIndex < imageKeys.length) {
				labelType.setImageKey(imageKeys[selectedImageKeyIndex]);
			}
			return labelType;
		}

		/**
		 * Gets {@link #createdLabelType}.
		 * 
		 * @return {@link #createdLabelType}
		 */
		public AbstractStorageLabelType<?> getCreatedLabelType() {
			return createdLabelType;
		}

	}

	/**
	 * Create label dialog.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class CreateLabelDialog extends TitleAreaDialog {

		/**
		 * List of label types that can be created.
		 */
		private List<AbstractStorageLabelType<?>> labelTypes;

		/**
		 * Suggested label type that will be initially selected.
		 */
		private AbstractStorageLabelType<?> suggestedType;

		/**
		 * Label type selection Combo.
		 */
		private Combo typeSelection;

		/**
		 * Storage label composite.
		 */
		private AbstractStorageLabelComposite storageLabelComposite;

		/**
		 * OK Button.
		 */
		private Button okButton;

		/**
		 * Main composite.
		 */
		private Composite main;

		/**
		 * Created label reference.
		 */
		private AbstractStorageLabel<?> createdLabel;

		/**
		 * Default constructor.
		 * 
		 * @param parentShell
		 *            Shell.
		 * @param labelTypes
		 *            List of labels that can be created.
		 * @param suggestedType
		 *            Suggested type that will initially be selected in the combo box.
		 */
		public CreateLabelDialog(Shell parentShell, List<AbstractStorageLabelType<?>> labelTypes, AbstractStorageLabelType<?> suggestedType) {
			super(parentShell);
			this.labelTypes = labelTypes;
			this.suggestedType = suggestedType;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Create Label");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void create() {
			super.create();
			this.setTitle("Create Label");
			this.setMessage("Selected wanted label type and define label value", IMessageProvider.INFORMATION);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(isInputValid());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				createdLabel = storageLabelComposite.getStorageLabel();
			}
			super.buttonPressed(buttonId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			main = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumWidth = 350;
			main.setLayoutData(gd);
			main.setLayout(new GridLayout(2, false));

			new Label(main, SWT.NONE).setText("Label type:");
			typeSelection = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
			int index = -1;
			int i = 0;
			for (AbstractStorageLabelType<?> labelType : labelTypes) {
				typeSelection.add(TextFormatter.getLabelName(labelType));
				if (ObjectUtils.equals(labelType, suggestedType)) {
					index = i;
				}
				i++;
			}
			typeSelection.select(index);
			typeSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			typeSelection.setEnabled(false);

			final Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(isInputValid());
				}
			};
			typeSelection.addListener(SWT.Selection, listener);
			typeSelection.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateStorageLabelComposite();
				}
			});

			new Label(main, SWT.NONE).setText("Label value:");
			updateStorageLabelComposite();

			typeSelection.forceFocus();
			return main;
		}

		/**
		 * 
		 * @return Returns created label.
		 */
		public AbstractStorageLabel<?> getCreatedLabel() {
			return createdLabel;
		}

		/**
		 * Updates the storage label composite.
		 */
		@SuppressWarnings("unchecked")
		private void updateStorageLabelComposite() {
			if (null != storageLabelComposite && !storageLabelComposite.isDisposed()) {
				storageLabelComposite.dispose();
			}

			AbstractStorageLabelType<?> selectedLabelType = labelTypes.get(typeSelection.getSelectionIndex());
			if (selectedLabelType.getValueClass().equals(Boolean.class)) {
				storageLabelComposite = new BooleanStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Boolean>) selectedLabelType, false);
			} else if (selectedLabelType.getValueClass().equals(Date.class)) {
				storageLabelComposite = new DateStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Date>) selectedLabelType, false);
			} else if (selectedLabelType.getValueClass().equals(Number.class)) {
				storageLabelComposite = new NumberStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Number>) selectedLabelType, false);
			} else if (selectedLabelType.getValueClass().equals(String.class)) {
				storageLabelComposite = new StringStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<String>) selectedLabelType, false);
			}

			final Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(isInputValid());
				}
			};
			storageLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			storageLabelComposite.addListener(listener);
			main.layout();
		}

		/**
		 * 
		 * @return If dialog input is valid.
		 */
		private boolean isInputValid() {
			if (typeSelection.getSelectionIndex() == -1) {
				return false;
			}
			if (null == storageLabelComposite || !storageLabelComposite.isInputValid()) {
				return false;
			}
			return true;
		}
	}

}
