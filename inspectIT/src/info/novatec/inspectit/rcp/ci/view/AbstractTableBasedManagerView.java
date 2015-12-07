package info.novatec.inspectit.rcp.ci.view;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;
import info.novatec.inspectit.rcp.view.IRefreshableView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Abstract Manager view using a {@link TableViewer} as body.
 *
 * @author Alexander Wert
 *
 */
public abstract class AbstractTableBasedManagerView implements CmrRepositoryChangeListener, IRefreshableView {

	/**
	 * {@link FormToolkit}.
	 */
	protected FormToolkit toolkit;

	/**
	 * {@link SashForm} displayed in the view as main composite.
	 */
	protected SashForm mainComposite;

	/**
	 * Table viewer in the form.
	 */
	protected TableViewer tableViewer;

	/**
	 * Main form in upper part of main composite.
	 */
	protected Form mainForm;

	/**
	 * Help composite for displaying messages.
	 */
	protected Composite messageComposite;

	/**
	 * {@link CmrRepositoryManager} needed for loading the CMRs.
	 */
	protected CmrRepositoryManager cmrRepositoryManager;

	/**
	 * Currently displayed {@link CmrRepositoryDefinition}.
	 */
	protected CmrRepositoryDefinition displayedCmrRepositoryDefinition;

	/**
	 * Cached statuses of CMR repository definitions.
	 */
	private final Map<CmrRepositoryDefinition, OnlineStatus> cachedOnlineStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * Adapter to publish the selection to the Site.
	 */
	private final SelectionProviderAdapter selectionProviderAdapter = new SelectionProviderAdapter();

	/**
	 * The {@link IWorkbenchPartSite} the view is showed in.
	 */
	private final IWorkbenchPartSite workbenchPartSite;

	/**
	 * Default Constructor.
	 *
	 * @param workbenchPartSite
	 *            The {@link IWorkbenchPartSite} the view is showed in.
	 */
	public AbstractTableBasedManagerView(IWorkbenchPartSite workbenchPartSite) {
		this.workbenchPartSite = workbenchPartSite;
		cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
		cmrRepositoryManager.addCmrRepositoryChangeListener(this);
		selectDisplayedCmrRepositoryDefinition();
	}

	/**
	 * Creates the controls of the view.
	 *
	 * @param parent
	 *            The parent composite.
	 * @param multiSelection
	 *            indicates whether the corresponding table should support multi-selection.
	 */
	public void createControls(Composite parent, boolean multiSelection) {
		createViewToolbar();

		toolkit = new FormToolkit(parent.getDisplay());

		mainComposite = new SashForm(parent, SWT.VERTICAL);
		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		mainComposite.setLayout(mainLayout);

		mainForm = toolkit.createForm(mainComposite);
		mainForm.getBody().setLayout(new GridLayout(1, true));
		mainForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createHeadClient(mainForm);
		toolkit.decorateFormHeading(mainForm);

		int borderStyle = toolkit.getBorderStyle();
		toolkit.setBorderStyle(SWT.NULL);

		int multiSelectionFlag = multiSelection ? SWT.MULTI : SWT.NONE;

		Table table = toolkit.createTable(mainForm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | multiSelectionFlag);

		toolkit.setBorderStyle(borderStyle);
		tableViewer = new TableViewer(table);

		// create tree content provider
		tableViewer.setContentProvider(getContentProvider());
		tableViewer.setLabelProvider(getLabelProvider());

		ViewerComparator comparator = getViewerComparator();
		if (null != comparator) {
			tableViewer.setComparator(comparator);
		}
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

		// double-click listener
		IDoubleClickListener doubleClickListener = getDoubleClickListener();
		if (null != doubleClickListener) {
			tableViewer.addDoubleClickListener(doubleClickListener);
		}

		// menu
		String menuId = getMenuId();
		if (null != menuId && null != workbenchPartSite) {
			MenuManager menuManager = new MenuManager();
			menuManager.setRemoveAllWhenShown(true);
			workbenchPartSite.registerContextMenu(menuId, menuManager, tableViewer);
			Control control = tableViewer.getControl();
			Menu menu = menuManager.createContextMenu(control);
			control.setMenu(menu);
		}

		// resizing listener
		mainComposite.addControlListener(new ControlAdapter() {
			private boolean verticaLayout;

			@Override
			public void controlResized(ControlEvent e) {
				int width = mainComposite.getBounds().width;
				int height = mainComposite.getBounds().height;

				if (width > height && verticaLayout) {
					verticaLayout = false;
					mainComposite.setOrientation(SWT.HORIZONTAL);
				} else if (width < height && !verticaLayout) {
					verticaLayout = true;
					mainComposite.setOrientation(SWT.VERTICAL);
				}

				mainComposite.layout();
			}
		});

		// update all
		updateFormTitle();
		updateFormMenuManager();
		updateFormBody();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection ss = (StructuredSelection) tableViewer.getSelection();
				if (matchesContentType(ss.getFirstElement())) {
					getSelectionProviderAdapter().setSelection(ss);
				} else {
					// setting selection to the CMR so that we can perform all the necessary
					// operations
					getSelectionProviderAdapter().setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
				}
			}
		});
		if (null != workbenchPartSite) {
			workbenchPartSite.setSelectionProvider(getSelectionProviderAdapter());
		}
		getSelectionProviderAdapter().setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));

	}

	/**
	 * Indicates whether the passed {@link Object} matches the content type of the corresponding
	 * {@link AbstractTableBasedManagerView} instance.
	 *
	 * @param object
	 *            {@link Object} instance to check
	 * @return <code>true</code>, if the passed {@link Object} matches the content type of the
	 *         corresponding {@link AbstractTableBasedManagerView} instance. Otherwise
	 *         <code>false</code>.
	 */
	protected abstract boolean matchesContentType(Object object);

	/**
	 * Create view tool-bar. Sub-classes can implement if needed.
	 */
	protected void createViewToolbar() {

	}

	/**
	 * Returns the {@link IContentProvider} to be used in the table viewer. The sub-classes can
	 * override if needed. Default implementation returns the array/collection provider.
	 *
	 * @return Returns the {@link IContentProvider} to be used in the table viewer.
	 */
	protected IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/**
	 * @return Return label provider for the {@link #tableViewer}.
	 */
	protected abstract IBaseLabelProvider getLabelProvider();

	/**
	 * Returns comparator to be used in the tree viewer. Default implementation returns a comparator
	 * based on the {@link Comparable} interface, which means no-comparator.
	 *
	 * Sub-class can override.
	 *
	 * @return Returns comparator to be used in the tree viewer.
	 */
	protected ViewerComparator getViewerComparator() {
		// just compare based on the comparable interface
		return new ViewerComparator() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof Comparable && e2 instanceof Comparable) {
					return ((Comparable) e1).compareTo(e2);
				}
				return 0;
			}
		};
	}

	/**
	 * @return Returns the {@link IDoubleClickListener} to be activated on the tree double click.
	 */
	protected IDoubleClickListener getDoubleClickListener() {
		return null;
	}

	/**
	 * @return Returns ID of the menu that should be hooked up to the table viewer.
	 */
	protected abstract String getMenuId();

	/**
	 * Create head client in the {@link #mainForm}. Sub-classes can implement if needed.
	 *
	 * @param form
	 *            form main form.
	 */
	protected void createHeadClient(Form form) {

	}

	/**
	 * Updates the content of the view.
	 */
	protected abstract void updateContent();

	/**
	 * Updates the form menu. Sub-classes can extend if needed.
	 */
	protected void updateFormMenuManager() {
		IMenuManager menuManager = mainForm.getMenuManager();
		menuManager.removeAll();

		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
			if (!Objects.equals(cmrRepositoryDefinition, displayedCmrRepositoryDefinition)) {
				menuManager.add(new SelectCmrAction(cmrRepositoryDefinition));
			}
		}
		menuManager.update(true);
		mainForm.getHead().layout();
	}

	/**
	 * Updates the form title. Sub-classes can extend if needed.
	 */
	protected void updateFormTitle() {
		if (null != displayedCmrRepositoryDefinition) {
			mainForm.setImage(ImageFormatter.getCmrRepositoryImage(displayedCmrRepositoryDefinition, true));
			mainForm.setText(displayedCmrRepositoryDefinition.getName());
			mainForm.setToolTipText(TextFormatter.getCmrRepositoryDescription(displayedCmrRepositoryDefinition));
			mainForm.setMessage(null);
		} else {
			mainForm.setImage(null);
			mainForm.setText("No repository exists");
			mainForm.setMessage("Repositories can be added from the Repository Manager", IMessageProvider.WARNING);
			mainForm.setToolTipText(null);
		}
	}

	/**
	 * Updates the table input and refreshes the table. Sub-classes can extend if needed.
	 */
	protected void updateFormBody() {
		clearFormBody();
		if (null == displayedCmrRepositoryDefinition) {
			displayMessage("No CMR repository present. Please add the CMR repository via 'Add CMR repository' action.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
		} else if (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			displayMessage("Selected CMR repository is currently unavailable.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
		} else {
			List<?> inputList = getTableInput();
			if (null == inputList || CollectionUtils.isEmpty(inputList)) {
				displayMessage("No items exists on selected CMR repository.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
			} else {
				createTableColumns(tableViewer);
				tableViewer.setLabelProvider(getLabelProvider());
				ViewerComparator comparator = getViewerComparator();
				if (null != comparator) {
					tableViewer.setComparator(getViewerComparator());
				}
				tableViewer.getTable().setHeaderVisible(true);
				tableViewer.getTable().setLinesVisible(true);
				tableViewer.getTable().setVisible(true);
				tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				tableViewer.setInput(inputList);
			}
		}
		mainForm.getBody().layout();
	}

	/**
	 *
	 * @return Returns the list of items to be used as input in the table.
	 */
	protected abstract List<?> getTableInput();

	/**
	 * Creates proper columns based on the selection.
	 *
	 * @param tableViewer
	 *            the {@link TableViewer} to build the columns for.
	 */
	protected abstract void createTableColumns(TableViewer tableViewer);

	/**
	 * Displays the message on the provided composite.
	 *
	 * @param text
	 *            Text of message.
	 * @param image
	 *            Image to show.
	 */
	protected void displayMessage(String text, Image image) {
		clearFormBody();
		if (null == messageComposite || messageComposite.isDisposed()) {
			messageComposite = toolkit.createComposite(mainForm.getBody());
		} else {
			for (Control c : messageComposite.getChildren()) {
				if (!c.isDisposed()) {
					c.dispose();
				}
			}
		}
		messageComposite.setLayout(new GridLayout(2, false));
		messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		toolkit.createLabel(messageComposite, null).setImage(image);
		toolkit.createLabel(messageComposite, text, SWT.WRAP).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		mainForm.getBody().layout();
	}

	/**
	 * Clears the look of the forms body.
	 */
	protected void clearFormBody() {
		if (messageComposite != null && !messageComposite.isDisposed()) {
			messageComposite.dispose();
		}
		for (TableColumn tableColumn : tableViewer.getTable().getColumns()) {
			tableColumn.dispose();
		}
		tableViewer.setInput(Collections.emptyList());
		tableViewer.getTable().setHeaderVisible(false);
		tableViewer.getTable().setLinesVisible(false);
		tableViewer.getTable().setVisible(false);
		tableViewer.getTable().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryOnlineStatusUpdated(final CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (Objects.equals(displayedCmrRepositoryDefinition, repositoryDefinition)) {
			OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
			if (newStatus == OnlineStatus.ONLINE) {
				if (null == cachedStatus || OnlineStatus.OFFLINE.equals(cachedStatus) || OnlineStatus.UNKNOWN.equals(cachedStatus)) {
					performUpdate(true);
				}
				cachedOnlineStatus.put(repositoryDefinition, newStatus);
			} else if (newStatus == OnlineStatus.OFFLINE) {
				if (null == cachedStatus || OnlineStatus.ONLINE.equals(cachedStatus)) {
					performUpdate(true);
				}
				cachedOnlineStatus.put(repositoryDefinition, newStatus);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
		cachedOnlineStatus.put(cmrRepositoryDefinition, cmrRepositoryDefinition.getOnlineStatus());
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateFormMenuManager();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryRemoved(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		cachedOnlineStatus.remove(cmrRepositoryDefinition);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateFormMenuManager();

				// if selected update
				if (Objects.equals(displayedCmrRepositoryDefinition, cmrRepositoryDefinition)) {
					selectDisplayedCmrRepositoryDefinition();
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateFormMenuManager();

				if (Objects.equals(displayedCmrRepositoryDefinition, cmrRepositoryDefinition)) {
					updateFormTitle();
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
	}

	/**
	 * Selects the displayed repository definition.
	 */
	private void selectDisplayedCmrRepositoryDefinition() {
		List<CmrRepositoryDefinition> repositories = cmrRepositoryManager.getCmrRepositoryDefinitions();
		if (CollectionUtils.isNotEmpty(repositories)) {
			// find first online
			for (CmrRepositoryDefinition repositoryDefinition : repositories) {
				if (repositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
					displayedCmrRepositoryDefinition = repositoryDefinition;
					performUpdate(true);
					getSelectionProviderAdapter().setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
					return;
				}
			}
			// if no online display first
			displayedCmrRepositoryDefinition = repositories.get(0);
			performUpdate(true);
			getSelectionProviderAdapter().setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
		}
	}

	/**
	 * Performs an update of the view.
	 *
	 * @param updateInput
	 *            Indicates whether the input of this view should be updated as well by accessing
	 *            the CMR.
	 */
	protected void performUpdate(final boolean updateInput) {
		performUpdate(updateInput, null);
	}

	/**
	 * Performs an update of the view.
	 *
	 * @param updateInput
	 *            Indicates whether the input of this view should be updated as well by accessing
	 *            the CMR. environments.
	 * @param selection
	 *            Initial selection in the table view. Can be null, in this case no selection is
	 *            applied.
	 */
	protected void performUpdate(final boolean updateInput, final ISelection selection) {
		if (updateInput) {
			updateViewContent(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							mainForm.setBusy(true);
							updateFormTitle();
							updateFormMenuManager();
							updateFormBody();
							if (null != selection) {
								tableViewer.setSelection(selection, true);
								tableViewer.getTable().setFocus();
							}
							mainForm.setBusy(false);
							mainForm.layout();
						}
					});
				}
			});
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					mainForm.setBusy(true);
					updateFormTitle();
					updateFormMenuManager();
					updateFormBody();
					if (null != selection) {
						tableViewer.setSelection(selection, true);
						tableViewer.getTable().setFocus();
					}
					mainForm.setBusy(false);
					mainForm.layout();
				}
			});
		}
	}

	/**
	 * Updates profiles and environment by communicating with the CMR.
	 *
	 * @param jobListener
	 *            the listener to the job completion, may be <code>null</code>
	 */
	private void updateViewContent(IJobChangeListener jobListener) {
		Job updateStorageListJob = new Job("Update data") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				updateContent();
				return Status.OK_STATUS;
			}
		};
		if (null != jobListener) {
			updateStorageListJob.addJobChangeListener(jobListener);
		}
		updateStorageListJob.schedule();
	}

	/**
	 * Informs that the editing repository for the configuration interface has been changed.
	 *
	 * @param cmrRepositoryDefinition
	 *            CmrRepositoryDefinition
	 */
	public void repositoryDefinitionChange(CmrRepositoryDefinition cmrRepositoryDefinition) {
		displayedCmrRepositoryDefinition = cmrRepositoryDefinition;
		performUpdate(true);
		getSelectionProviderAdapter().setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
	}

	/**
	 * Sets the focus to this view.
	 */
	public void setFocus() {
		if (tableViewer.getTable().isVisible()) {
			tableViewer.getTable().setFocus();
		} else {
			mainForm.setFocus();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		// check the status of CMR, if it's online do update, if it's offline just fire update CMR
		// online status job
		if (null != displayedCmrRepositoryDefinition && OnlineStatus.OFFLINE != displayedCmrRepositoryDefinition.getOnlineStatus()) {
			performUpdate(true);
		} else if (null != displayedCmrRepositoryDefinition) {
			InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(displayedCmrRepositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return true;
	}

	/**
	 * Disposes this view and cleans up.
	 */
	public void dispose() {
		cmrRepositoryManager.removeCmrRepositoryChangeListener(this);
	}

	/**
	 * Gets {@link #workbenchPartSite}.
	 *
	 * @return {@link #workbenchPartSite}
	 */
	public IWorkbenchPartSite getWorkbenchSite() {
		return workbenchPartSite;
	}

	/**
	 * Gets {@link #selectionProviderAdapter}.
	 *
	 * @return {@link #selectionProviderAdapter}
	 */
	public SelectionProviderAdapter getSelectionProviderAdapter() {
		return selectionProviderAdapter;
	}

	/**
	 * Action to select CMR from the form menu.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class SelectCmrAction extends Action {

		/**
		 * CMR repository to change to.
		 */
		private final CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * Default constructor.
		 *
		 * @param cmrRepositoryDefinition
		 *            {@link CmrRepositoryDefinition}
		 */
		SelectCmrAction(CmrRepositoryDefinition cmrRepositoryDefinition) {
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			setText(cmrRepositoryDefinition.getName());
			setImageDescriptor(ImageDescriptor.createFromImage(ImageFormatter.getCmrRepositoryImage(cmrRepositoryDefinition, true)));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			repositoryDefinitionChange(cmrRepositoryDefinition);
		}
	}
}
