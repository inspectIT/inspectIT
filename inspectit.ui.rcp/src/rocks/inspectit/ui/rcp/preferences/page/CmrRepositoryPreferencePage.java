package info.novatec.inspectit.rcp.preferences.page;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.dialog.AddCmrRepositoryDefinitionDialog;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.rcp.wizard.ManageLabelWizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for {@link CmrRepositoryDefinition} management.
 * <p>
 * <b>This class is not used at the moment. It's not confirmed that quality of the class in with the
 * standards, however, it can serve as an entry to future work regarding preferences.</b>
 * 
 * @author Ivan Senic
 * 
 */
public class CmrRepositoryPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, CmrRepositoryChangeListener {

	/**
	 * {@link CmrRepositoryManager}.
	 */
	private CmrRepositoryManager cmrRepositoryManager;

	/**
	 * Table with repositories.
	 */
	private TableViewer tableViewer;

	/**
	 * Add button.
	 */
	private Button addButton;

	/**
	 * Remove button.
	 */
	private Button removeButton;

	/**
	 * Refresh button.
	 */
	private Button refreshButton;

	/**
	 * Manage labels button.
	 */
	private Button manageLabelsButton;

	/**
	 * Input list to save changes until Apply or OK are fired.
	 */
	private Map<CmrRepositoryDefinition, OnlineStatus> inputList;

	/**
	 * Default constructor.
	 */
	public CmrRepositoryPreferencePage() {
	}

	/**
	 * Sec. constructor.
	 * 
	 * @param title
	 *            Title of preference page.
	 */
	public CmrRepositoryPreferencePage(String title) {
		super(title);
	}

	/**
	 * Third constructor.
	 * 
	 * @param title
	 *            Title of preference page.
	 * @param image
	 *            Image.
	 */
	public CmrRepositoryPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench) {
		cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
		cmrRepositoryManager.addCmrRepositoryChangeListener(this);
		inputList = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
			inputList.put(cmrRepositoryDefinition, cmrRepositoryDefinition.getOnlineStatus());
		}
		noDefaultAndApplyButton();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.INHERIT_DEFAULT);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);

		Label info = new Label(mainComposite, SWT.NONE);
		info.setText("Add, remove and manage repositories");
		GridData labelGridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		labelGridData.horizontalSpan = 2;
		info.setLayoutData(labelGridData);

		Table table = new Table(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns();
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(inputList.keySet());

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsState();
			}
		});

		Composite buttonComposite = new Composite(mainComposite, SWT.INHERIT_DEFAULT);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText("Add");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddCmrRepositoryDefinitionDialog dialog = new AddCmrRepositoryDefinitionDialog(getShell());
				dialog.open();
				if (dialog.getReturnCode() == Dialog.OK && null != dialog.getCmrRepositoryDefinition()) {
					inputList.put(dialog.getCmrRepositoryDefinition(), OnlineStatus.OFFLINE);
					cmrRepositoryManager.forceCmrRepositoryOnlineStatusUpdate(dialog.getCmrRepositoryDefinition());
					tableViewer.refresh();
				}
			}
		});

		removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
				for (Object selectedObject : selection.toArray()) {
					if (selectedObject instanceof CmrRepositoryDefinition) {
						inputList.remove((CmrRepositoryDefinition) selectedObject);
					}
				}
				tableViewer.refresh();
			}
		});

		refreshButton = new Button(buttonComposite, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (CmrRepositoryDefinition cmrRepositoryDefinition : inputList.keySet()) {
					cmrRepositoryManager.forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
				}
			}
		});

		manageLabelsButton = new Button(buttonComposite, SWT.PUSH);
		manageLabelsButton.setText("Manage Labels");
		manageLabelsButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		manageLabelsButton.setEnabled(false);
		manageLabelsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
				for (Object selectedObject : selection.toArray()) {
					if (selectedObject instanceof CmrRepositoryDefinition) {
						ManageLabelWizard mlw = new ManageLabelWizard((CmrRepositoryDefinition) selectedObject);
						WizardDialog wizardDialog = new WizardDialog(getShell(), mlw);
						wizardDialog.open();
					}
				}
			}
		});

		return mainComposite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performOk() {
		saveChanges();
		return super.performOk();
	}

	@Override
	public void dispose() {
		super.dispose();
		cmrRepositoryManager.removeCmrRepositoryChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Does nothing.
	 */
	public void repositoryAdded(CmrRepositoryDefinition repositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Does nothing.
	 */
	public void repositoryRemoved(CmrRepositoryDefinition repositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(final CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING && inputList.containsKey(repositoryDefinition)) {
			OnlineStatus oldRegisteredStatus = inputList.get(repositoryDefinition);
			if (!oldRegisteredStatus.equals(newStatus)) {
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						tableViewer.refresh(repositoryDefinition);
						updateButtonsState();
					}
				}, tableViewer.getTable(), removeButton, manageLabelsButton);
				inputList.put(repositoryDefinition, newStatus);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		SafeExecutor.asyncExec(new Runnable() {
			@Override
			public void run() {
				tableViewer.refresh(cmrRepositoryDefinition);
			}
		}, tableViewer.getTable());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
	}

	/**
	 * Updates the state of the remove and license info buttons depending on the current table
	 * selection.
	 */
	private void updateButtonsState() {
		StructuredSelection structuredSelection = (StructuredSelection) tableViewer.getSelection();
		if (structuredSelection.isEmpty()) {
			removeButton.setEnabled(false);
			manageLabelsButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
			if (structuredSelection.size() == 1 && ((CmrRepositoryDefinition) structuredSelection.getFirstElement()).getOnlineStatus() == OnlineStatus.ONLINE) {
				manageLabelsButton.setEnabled(true);
			} else {
				manageLabelsButton.setEnabled(false);
			}
		}
	}

	/**
	 * Save made changes.
	 */
	private void saveChanges() {
		// do nothing if no changes are there
		if (!isDirty()) {
			return;
		}

		// add all new
		for (CmrRepositoryDefinition cmrRepositoryDefinition : inputList.keySet()) {
			if (!cmrRepositoryManager.getCmrRepositoryDefinitions().contains(cmrRepositoryDefinition)) {
				cmrRepositoryManager.addCmrRepositoryDefinition(cmrRepositoryDefinition);
			}
		}

		// remove all deleted
		List<CmrRepositoryDefinition> removeList = new ArrayList<CmrRepositoryDefinition>();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
			if (!inputList.keySet().contains(cmrRepositoryDefinition)) {
				removeList.add(cmrRepositoryDefinition);
			}
		}
		if (!removeList.isEmpty()) {
			for (CmrRepositoryDefinition cmrRepositoryDefinition : removeList) {
				cmrRepositoryManager.removeCmrRepositoryDefinition(cmrRepositoryDefinition);
			}
		}

		// save to local preferences
		savePreferences();
	}

	/**
	 * Where there changes performed by user.
	 * 
	 * @return Where there changes performed by user.
	 */
	private boolean isDirty() {
		return !Objects.equals(inputList.keySet(), cmrRepositoryManager.getCmrRepositoryDefinitions());
	}

	/**
	 * Saves the changes to preference store.
	 */
	private void savePreferences() {
		PreferencesUtils.saveCmrRepositoryDefinitions(cmrRepositoryManager.getCmrRepositoryDefinitions(), false);
	}

	/**
	 * Creates columns for the table.
	 */
	private void createColumns() {
		TableViewerColumn onlineColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		onlineColumn.getColumn().setResizable(false);
		onlineColumn.getColumn().setWidth(24);
		onlineColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public Image getImage(Object element) {
				if (element instanceof CmrRepositoryDefinition) {
					if (((CmrRepositoryDefinition) element).getOnlineStatus() == OnlineStatus.ONLINE) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE_SMALL);
					} else if (((CmrRepositoryDefinition) element).getOnlineStatus() == OnlineStatus.OFFLINE) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE_SMALL);
					} else {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH_SMALL);
					}
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return null;
			}

		});

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setResizable(true);
		nameColumn.getColumn().setWidth(150);
		nameColumn.getColumn().setText("Name");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof CmrRepositoryDefinition) {
					return ((CmrRepositoryDefinition) element).getName();
				}
				return null;
			}
		});

		TableViewerColumn ipColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		ipColumn.getColumn().setResizable(true);
		ipColumn.getColumn().setWidth(120);
		ipColumn.getColumn().setText("IP Address");
		ipColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof CmrRepositoryDefinition) {
					return ((CmrRepositoryDefinition) element).getIp();
				}
				return null;
			}
		});

		TableViewerColumn portColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		portColumn.getColumn().setResizable(true);
		portColumn.getColumn().setWidth(50);
		portColumn.getColumn().setText("Port");
		portColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof CmrRepositoryDefinition) {
					return String.valueOf(((CmrRepositoryDefinition) element).getPort());
				}
				return null;
			}
		});

	}

}
