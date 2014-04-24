package info.novatec.inspectit.rcp.ci.view;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.editor.EnvironmentEditor;
import info.novatec.inspectit.rcp.ci.form.editor.ProfileEditor;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.ci.listener.IEnvironmentChangeListener;
import info.novatec.inspectit.rcp.ci.listener.IProfileChangeListener;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ci.EnvironmentLeaf;
import info.novatec.inspectit.rcp.model.ci.ProfileLeaf;
import info.novatec.inspectit.rcp.provider.IEnvironmentProvider;
import info.novatec.inspectit.rcp.provider.IProfileProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;
import info.novatec.inspectit.rcp.view.IRefreshableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * View displaying {@link Profile}s and {@link Environment}s.
 * 
 * @author Ivan Senic
 * 
 */
public class ConfigurationInterfaceManagerView extends ViewPart implements IRefreshableView, CmrRepositoryChangeListener, IProfileChangeListener, IEnvironmentChangeListener { // NOPMD

	/**
	 * View id.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.ci.view.ciManager";

	/**
	 * Menu to be bounded.
	 */
	private static final String MENU_ID = "info.novatec.inspectit.rcp.view.configurationInterfaceManager.table";

	/** Logic */

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
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedOnlineStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/** Widgets */

	/**
	 * {@link FormToolkit}.
	 */
	protected FormToolkit toolkit;

	/**
	 * {@link SashForm} displayed in the view as main composite.
	 */
	protected SashForm mainComposite;

	/**
	 * Main form in upper part of main composite.
	 */
	protected Form mainForm;

	/**
	 * Tree viewer in the form.
	 */
	protected TableViewer tableViewer;

	/**
	 * Help composite for displaying messages.
	 */
	protected Composite messageComposite;

	/**
	 * Adapter to publish the selection to the Site.
	 */
	private SelectionProviderAdapter selectionProviderAdapter = new SelectionProviderAdapter();

	/**
	 * Button for environment selection.
	 */
	private Button environmentSelection;

	/**
	 * Button for profile selection.
	 */
	private Button profileSelection;

	/**
	 * Input list of profiles.
	 */
	private List<IProfileProvider> profileProviders = new ArrayList<>(0);

	/**
	 * Input list of environments.
	 */
	private List<IEnvironmentProvider> environmentProviders = new ArrayList<>(0);

	/**
	 * Default constructor.
	 */
	public ConfigurationInterfaceManagerView() {
		cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
		cmrRepositoryManager.addCmrRepositoryChangeListener(this);
		selectDisplayedCmrRepositoryDefinition();

		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addProfileChangeListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addEnvironmentChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
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
		createHeadClient();
		toolkit.decorateFormHeading(mainForm);

		int borderStyle = toolkit.getBorderStyle();
		toolkit.setBorderStyle(SWT.NULL);
		Table table = toolkit.createTable(mainForm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
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
		IDoubleClickListener doubleClickListener = getTreeDoubleClickListener();
		if (null != doubleClickListener) {
			tableViewer.addDoubleClickListener(doubleClickListener);
		}

		// menu
		String menuId = getMenuId();
		if (null != menuId) {
			MenuManager menuManager = new MenuManager();
			menuManager.setRemoveAllWhenShown(true);
			getSite().registerContextMenu(menuId, menuManager, tableViewer);
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
				if (ss.getFirstElement() instanceof IProfileProvider || ss.getFirstElement() instanceof IEnvironmentProvider) {
					selectionProviderAdapter.setSelection(ss);
				} else {
					// setting selection to the CMR so that we can perform all the necessary
					// operations
					selectionProviderAdapter.setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
				}
			}
		});
		getSite().setSelectionProvider(selectionProviderAdapter);
		selectionProviderAdapter.setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
	}

	/**
	 * Create view tool-bar. Sub-classes can implement if needed.
	 */
	protected void createViewToolbar() {
	}

	/**
	 * Create head client in the {@link #mainForm}. Sub-classes can implement if needed.
	 */
	protected void createHeadClient() {
		Composite headClient = new Composite(mainForm.getHead(), SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		headClient.setLayout(gl);

		new Label(headClient, SWT.NONE).setText("Show:");

		environmentSelection = new Button(headClient, SWT.RADIO);
		environmentSelection.setText("Environments");
		environmentSelection.setSelection(true);

		profileSelection = new Button(headClient, SWT.RADIO);
		profileSelection.setText("Profiles");

		environmentSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performUpdate(false);
			}
		});

		mainForm.setHeadClient(headClient);
	}

	/**
	 * Creates proper columns based on the selection.
	 */
	private void createTableColumns() {
		if (isShowEnvironments()) {
			// columns when environments are displayed
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Environment");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Environment name.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Profiles");
			viewerColumn.getColumn().setWidth(80);
			viewerColumn.getColumn().setToolTipText("Number of linked profiles in the Environment.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Description");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Environment description.");

		} else if (isShowProfiles()) {
			// columns when profiles are displayed
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Profile");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Profile name.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Updated");
			viewerColumn.getColumn().setWidth(80);
			viewerColumn.getColumn().setToolTipText("The date and time when the profile was last time updated.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Active");
			viewerColumn.getColumn().setWidth(55);
			viewerColumn.getColumn().setToolTipText(
					"If profile is active or not, note that deactivated profile will not be considered during the instrumentation even if it's a part of an Environment.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Default");
			viewerColumn.getColumn().setWidth(55);
			viewerColumn.getColumn().setToolTipText("If profile is default or not, note that default profile can not be deleted or edited.");

			viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText("Description");
			viewerColumn.getColumn().setWidth(150);
			viewerColumn.getColumn().setToolTipText("Profile description.");
		}
		tableViewer.setLabelProvider(getLabelProvider());
		ViewerComparator comparator = getViewerComparator();
		if (null != comparator) {
			tableViewer.setComparator(getViewerComparator());
		}
	}

	/**
	 * @return Return label provider for the {@link #tableViewer}.
	 */
	protected IBaseLabelProvider getLabelProvider() {
		if (isShowEnvironments()) {
			return new EnvironmentLabelProvider();
		} else if (isShowProfiles()) {
			return new ProfileLabelProvider();
		}
		return null;
	}

	/**
	 * @return Returns the {@link IDoubleClickListener} to be activated on the tree double click.
	 */
	protected IDoubleClickListener getTreeDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty()) {
					Object selected = ((StructuredSelection) selection).getFirstElement();
					if (selected instanceof IProfileProvider) {
						// open profile job
						IProfileProvider profileProvider = (IProfileProvider) selected;
						new OpenProfileJob(profileProvider.getCmrRepositoryDefinition(), profileProvider.getProfile().getId(), getSite().getPage()).schedule();
					} else if (selected instanceof IEnvironmentProvider) {
						IEnvironmentProvider environmentProvider = (IEnvironmentProvider) selected;
						new OpenEnvironmentJob(environmentProvider.getCmrRepositoryDefinition(), environmentProvider.getEnvironment().getId(), getSite().getPage()).schedule();
					}
				}
			}
		};
	}

	/**
	 * Returns the {@link ITreeContentProvider} to be used in the tree viewer. The sub-classes can
	 * override if needed. Default implementation returns the array/collection provider.
	 * 
	 * @return Returns the {@link ITreeContentProvider} to be used in the tree viewer.
	 */
	protected IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/**
	 * Returns comparator to be used in the tree viewer. Default implementation returns
	 * <code>null</code>, which means no-comparator. Sub-class can override.
	 * 
	 * @return Returns comparator to be used in the tree viewer.
	 */
	protected ViewerComparator getViewerComparator() {
		// TODO for now just compare based on the comparable interface
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
	 * Updates the tree input and refreshes the tree.
	 */
	protected void updateFormBody() {
		clearFormBody();
		if (null == displayedCmrRepositoryDefinition) {
			displayMessage("No CMR repository present. Please add the CMR repository via 'Add CMR repository' action.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
		} else if (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			displayMessage("Selected CMR repository is currently unavailable.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
		} else if (isShowEnvironments()) {
			List<?> inputList = environmentProviders;
			if (CollectionUtils.isEmpty(inputList)) {
				displayMessage("No environment exists on selected CMR repository.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
			} else {
				createTableColumns();
				tableViewer.getTable().setHeaderVisible(true);
				tableViewer.getTable().setLinesVisible(true);
				tableViewer.getTable().setVisible(true);
				tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				tableViewer.setInput(inputList);
			}
		} else if (isShowProfiles()) {
			List<?> inputList = profileProviders;
			if (CollectionUtils.isEmpty(inputList)) {
				displayMessage("No profile exists on selected CMR repository.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
			} else {
				createTableColumns();
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
	 * {@inheritDoc}
	 */
	@Override
	public void profileAdded(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		ProfileLeaf profileLeaf = new ProfileLeaf(profile, repositoryDefinition);
		profileProviders.add(profileLeaf);
		showProfiles(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileEdited(Profile profile, boolean onlyProperties) {
		for (Iterator<IProfileProvider> it = profileProviders.iterator(); it.hasNext();) {
			IProfileProvider provider = it.next();
			if (Objects.equals(profile.getId(), provider.getProfile().getId())) {
				it.remove();
				profileProviders.add(new ProfileLeaf(profile, provider.getCmrRepositoryDefinition()));

				performUpdate(false);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileDeleted(Profile profile) {
		for (Iterator<IProfileProvider> it = profileProviders.iterator(); it.hasNext();) {
			IProfileProvider provider = it.next();
			if (Objects.equals(profile.getId(), provider.getProfile().getId())) {
				it.remove();

				performUpdate(false);
				break;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentAdded(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		EnvironmentLeaf environmentLeaf = new EnvironmentLeaf(environment, repositoryDefinition);
		environmentProviders.add(environmentLeaf);
		showEnvironments(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentEdited(Environment environment) {
		for (Iterator<IEnvironmentProvider> it = environmentProviders.iterator(); it.hasNext();) {
			IEnvironmentProvider provider = it.next();
			if (Objects.equals(environment.getId(), provider.getEnvironment().getId())) {
				it.remove();
				environmentProviders.add(new EnvironmentLeaf(environment, provider.getCmrRepositoryDefinition()));

				performUpdate(false);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentDeleted(Environment environment) {
		for (Iterator<IEnvironmentProvider> it = environmentProviders.iterator(); it.hasNext();) {
			IEnvironmentProvider provider = it.next();
			if (Objects.equals(environment.getId(), provider.getEnvironment().getId())) {
				it.remove();

				performUpdate(false);
				break;
			}
		}
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
					selectionProviderAdapter.setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
					return;
				}
			}
			// if no online display first
			displayedCmrRepositoryDefinition = repositories.get(0);
			performUpdate(true);
			selectionProviderAdapter.setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
		}
	}

	/**
	 * Informs that the editing repository for the configuration interface has been changed.
	 * 
	 * @param cmrRepositoryDefinition
	 *            CmrRepositoryDefinition
	 */
	public void ciRepositoryDefintionChange(CmrRepositoryDefinition cmrRepositoryDefinition) {
		displayedCmrRepositoryDefinition = cmrRepositoryDefinition;
		performUpdate(true);
		selectionProviderAdapter.setSelection(new StructuredSelection(displayedCmrRepositoryDefinition));
	}

	/**
	 * Performs update.
	 * 
	 * @param updateInput
	 *            If the update should go to the CMRs for an updated list of profiles and
	 *            environments.
	 */
	private void performUpdate(final boolean updateInput) {
		if (updateInput) {
			updateProfilesAndEnvironments(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							mainForm.setBusy(true);
							updateFormTitle();
							updateFormMenuManager();
							updateFormBody();
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
	private void updateProfilesAndEnvironments(IJobChangeListener jobListener) {
		Job updateStorageListJob = new Job("Update data") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				profileProviders.clear();
				environmentProviders.clear();
				if (displayedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
					// profiles
					profileProviders = new ArrayList<>();
					Collection<Profile> profiles = displayedCmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
					for (Profile profile : profiles) {
						profileProviders.add(new ProfileLeaf(profile, displayedCmrRepositoryDefinition));
					}

					// environment
					environmentProviders = new ArrayList<>();
					Collection<Environment> environments = displayedCmrRepositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();
					for (Environment environment : environments) {
						environmentProviders.add(new EnvironmentLeaf(environment, displayedCmrRepositoryDefinition));
					}
				}
				return Status.OK_STATUS;
			}
		};
		if (null != jobListener) {
			updateStorageListJob.addJobChangeListener(jobListener);
		}
		updateStorageListJob.schedule();
	}

	/**
	 * @return Are environments displayed.
	 */
	private boolean isShowEnvironments() {
		return environmentSelection.getSelection();
	}

	/**
	 * @return Are profiles displayed.
	 */
	private boolean isShowProfiles() {
		return profileSelection.getSelection();
	}

	/**
	 * Makes the view show the environments.
	 * 
	 * @param update
	 *            If update of the input should be made.
	 */
	public void showEnvironments(boolean update) {
		if (!isShowEnvironments()) {
			environmentSelection.setSelection(true);
			profileSelection.setSelection(false);
		}

		performUpdate(update);
	}

	/**
	 * Makes the view show the profiles.
	 * 
	 * @param update
	 *            If update of the input should be made.
	 */
	public void showProfiles(boolean update) {
		if (!isShowProfiles()) {
			profileSelection.setSelection(true);
			environmentSelection.setSelection(false);
		}

		performUpdate(update);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
		performUpdate(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return true;
	}

	/**
	 * @return Returns ID of the menu that should be hooked up to the tree viewer.
	 */
	protected String getMenuId() {
		return MENU_ID;
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		cmrRepositoryManager.removeCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeProfileChangeListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
		super.dispose();
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
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * Default constructor.
		 * 
		 * @param cmrRepositoryDefinition
		 *            {@link CmrRepositoryDefinition}
		 */
		public SelectCmrAction(CmrRepositoryDefinition cmrRepositoryDefinition) {
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			setText(cmrRepositoryDefinition.getName());
			setImageDescriptor(ImageDescriptor.createFromImage(ImageFormatter.getCmrRepositoryImage(cmrRepositoryDefinition, true)));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			ciRepositoryDefintionChange(cmrRepositoryDefinition);
		}
	}

	/**
	 * Profile label provider.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class ProfileLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Empty.
		 */
		private static final StyledString EMPTY = new StyledString();

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			if (element instanceof IProfileProvider) {
				Profile profile = ((IProfileProvider) element).getProfile();
				switch (index) {
				case 0:
					return new StyledString(profile.getName());
				case 1:
					if (null != profile.getUpdatedDate()) {
						return new StyledString(NumberFormatter.formatTime(profile.getUpdatedDate()));
					} else {
						return new StyledString(NumberFormatter.formatTime(profile.getCreatedDate()));
					}
				case 4:
					return TextFormatter.emptyStyledStringIfNull(TextFormatter.clearLineBreaks(profile.getDescription()));
				default:
					return EMPTY;
				}
			}
			return EMPTY;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			if (element instanceof IProfileProvider) {
				Profile profile = ((IProfileProvider) element).getProfile();
				switch (index) {
				case 0:
					return ImageFormatter.getProfileImage(profile);
				case 2:
					return profile.isActive() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : null; // NOPMD
				case 3:
					return profile.isDefaultProfile() ? InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK) : null; // NOPMD
				default:
					return super.getColumnImage(element, index);
				}
			}
			return super.getColumnImage(element, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Color getForeground(Object element, int index) {
			if (element instanceof IProfileProvider) {
				Profile profile = ((IProfileProvider) element).getProfile();
				if (profile.isCommonProfile()) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
				}
			}
			return super.getForeground(element, index);
		}
	}

	/**
	 * Environment label provider.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class EnvironmentLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Empty.
		 */
		private static final StyledString EMPTY = new StyledString();

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			if (element instanceof IEnvironmentProvider) {
				Environment environment = ((IEnvironmentProvider) element).getEnvironment();
				switch (index) {
				case 0:
					return new StyledString(environment.getName());
				case 1:
					if (CollectionUtils.isEmpty(environment.getProfileIds())) {
						return new StyledString(String.valueOf(0));
					} else {
						return new StyledString(String.valueOf(environment.getProfileIds().size()));
					}
				case 2:
					return TextFormatter.emptyStyledStringIfNull(TextFormatter.clearLineBreaks(environment.getDescription()));
				default:
					return EMPTY;
				}
			}
			return EMPTY;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			if (element instanceof IEnvironmentProvider) {
				switch (index) {
				case 0:
					return ImageFormatter.getEnvironmentImage(((IEnvironmentProvider) element).getEnvironment());
				default:
					return super.getColumnImage(element, index);
				}
			}
			return super.getColumnImage(element, index);
		}
	}

	/**
	 * Job for loading of the profile from the CMR and opening the profile editor.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class OpenProfileJob extends Job {

		/**
		 * {@link CmrRepositoryDefinition}.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * Profile id.
		 */
		private String profileId;

		/**
		 * Page to open editor in.
		 */
		private IWorkbenchPage page;

		/**
		 * @param cmrRepositoryDefinition
		 *            {@link CmrRepositoryDefinition}
		 * @param profileId
		 *            Profile id.
		 * @param page
		 *            Page to open editor in.
		 */
		public OpenProfileJob(CmrRepositoryDefinition cmrRepositoryDefinition, String profileId, IWorkbenchPage page) {
			super("Loading profile..");
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			this.profileId = profileId;
			this.page = page;
			setUser(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
				return Status.CANCEL_STATUS;
			}
			
			try {
				Profile profile = cmrRepositoryDefinition.getConfigurationInterfaceService().getProfile(profileId);
				final ProfileEditorInput profileEditorInput = new ProfileEditorInput(profile, cmrRepositoryDefinition);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							page.openEditor(profileEditorInput, ProfileEditor.ID, true);
						} catch (PartInitException e) {
							InspectIT.getDefault().createErrorDialog("Exception occurred opening the Profile editor.", e, -1);
						}
					}
				});
				return Status.OK_STATUS;
			} catch (final Exception e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createErrorDialog("Exception occurred loading the profile from the CMR.", e, -1);
					}
				});
				return Status.CANCEL_STATUS;
			}
		}

	}
	
	/**
	 * Job for loading of the environment from the CMR and opening the environment editor.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class OpenEnvironmentJob extends Job {

		/**
		 * {@link CmrRepositoryDefinition}.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * Environment id.
		 */
		private String environmentId;

		/**
		 * Page to open editor in.
		 */
		private IWorkbenchPage page;

		/**
		 * @param cmrRepositoryDefinition
		 *            {@link CmrRepositoryDefinition}
		 * @param environmentId
		 *            Profile id.
		 * @param page
		 *            Page to open editor in.
		 */
		public OpenEnvironmentJob(CmrRepositoryDefinition cmrRepositoryDefinition, String environmentId, IWorkbenchPage page) {
			super("Loading environment..");
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			this.environmentId = environmentId;
			this.page = page;
			setUser(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
				return Status.CANCEL_STATUS;
			}
			
			try {
				Environment environment = cmrRepositoryDefinition.getConfigurationInterfaceService().getEnvironment(environmentId);
				Collection<Profile> profiles = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
				final EnvironmentEditorInput environmentEditorInput = new EnvironmentEditorInput(environment, profiles, cmrRepositoryDefinition);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							page.openEditor(environmentEditorInput, EnvironmentEditor.ID, true);
						} catch (PartInitException e) {
							InspectIT.getDefault().createErrorDialog("Exception occurred opening the Environment editor.", e, -1);
						}
					}
				});
				return Status.OK_STATUS;
			} catch (final Exception e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createErrorDialog("Exception occurred loading the environment from the CMR.", e, -1);
					}
				});
				return Status.CANCEL_STATUS;
			}
		}

	}

}
