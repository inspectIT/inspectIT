package info.novatec.inspectit.rcp.view.impl;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.filter.FilterComposite;
import info.novatec.inspectit.rcp.form.StorageDataPropertyForm;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.handlers.CloseAndShowStorageHandler;
import info.novatec.inspectit.rcp.handlers.ShowRepositoryHandler;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.GroupedLabelsComposite;
import info.novatec.inspectit.rcp.model.storage.LocalStorageLeaf;
import info.novatec.inspectit.rcp.model.storage.LocalStorageTreeModelManager;
import info.novatec.inspectit.rcp.model.storage.StorageLeaf;
import info.novatec.inspectit.rcp.model.storage.StorageTreeModelManager;
import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.rcp.storage.listener.StorageChangeListener;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.rcp.view.IRefreshableView;
import info.novatec.inspectit.rcp.view.tree.StorageManagerTreeContentProvider;
import info.novatec.inspectit.rcp.view.tree.StorageManagerTreeLabelProvider;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.util.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * 
 * @author Ivan Senic
 * 
 */
public class StorageManagerView extends ViewPart implements CmrRepositoryChangeListener, StorageChangeListener, IRefreshableView { // NOPMD

	/**
	 * View id.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.view.storageManager";

	/**
	 * Menu id.
	 */
	public static final String MENU_ID = "info.novatec.inspectit.rcp.view.storageManager.storageTree";

	/**
	 * {@link CmrRepositoryManager}.
	 */
	private CmrRepositoryManager cmrRepositoryManager;

	/**
	 * {@link InspectITStorageManager}.
	 */
	private InspectITStorageManager storageManager;

	/**
	 * Map of storages and their repositories.
	 */
	private Map<StorageData, CmrRepositoryDefinition> storageRepositoryMap = new ConcurrentHashMap<StorageData, CmrRepositoryDefinition>();

	/**
	 * Cashed statuses of CMR repository definitions.
	 */
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedOnlineStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * Set of downloaded storages.
	 */
	private Set<LocalStorageData> downloadedStorages = Collections.newSetFromMap(new ConcurrentHashMap<LocalStorageData, Boolean>());

	/**
	 * Toolkit for decorations.
	 */
	private FormToolkit toolkit;

	/**
	 * Main form.
	 */
	private Form mainForm;

	/**
	 * Tree Viewer.
	 */
	private TreeViewer treeViewer;

	/**
	 * Filter for the tree.
	 */
	private TreeFilter treeFilter = new TreeFilter();

	/**
	 * Composite for message displaying.
	 */
	private Composite cmrMessageComposite;

	/**
	 * Label type that storages are ordered by.
	 */
	private AbstractStorageLabelType<?> orderingLabelType = null;

	/**
	 * Menu manager for filter repositories actions. Needed because it must be updated when the
	 * storages are added/removed.
	 */
	private MenuManager filterByRepositoryMenu;

	/**
	 * Menu manager for grouping storage by label. Needed because it must be updated when the
	 * storages are added/removed.
	 */
	private MenuManager groupByLabelMenu;

	/**
	 * Menu manager for filtering the storages based on the state.
	 */
	private MenuManager filterByStateMenu;

	/**
	 * Storage property form.
	 */
	private StorageDataPropertyForm storagePropertyForm;

	/**
	 * Last selected leaf.
	 */
	private StorageLeaf lastSelectedLeaf = null;

	/**
	 * Last selected local storage leaf.
	 */
	private LocalStorageLeaf lastSelectedLocalStorageLeaf = null;

	/**
	 * Boolean for layout of view.
	 */
	private boolean verticaLayout = true;

	/**
	 * Views main composite.
	 */
	private SashForm mainComposite;

	/**
	 * Upper composite where filter box and storage tree is located.
	 */
	private Composite upperComposite;

	/**
	 * Filter storages composite that will be displayed at top of view.
	 */
	private FilterStorageComposite filterStorageComposite;

	/**
	 * Selection button for showing the remove storages.
	 */
	private Button remoteStorageSelection;

	/**
	 * Selection button for showing the local storages.
	 */
	private Button localStorageSelection;

	/**
	 * Default constructor.
	 */
	public StorageManagerView() {
		cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
		cmrRepositoryManager.addCmrRepositoryChangeListener(this);
		storageManager = InspectIT.getDefault().getInspectITStorageManager();
		storageManager.addStorageChangeListener(this);
		updateStorageList(null);
		updateDownloadedStorages();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		createViewToolbar();

		mainComposite = new SashForm(parent, SWT.VERTICAL);
		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		mainComposite.setLayout(mainLayout);

		mainForm = toolkit.createForm(mainComposite);
		mainForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout lauout = new GridLayout(1, true);
		lauout.marginWidth = 0;
		lauout.marginHeight = 0;
		mainForm.getBody().setLayout(lauout);
		toolkit.decorateFormHeading(mainForm);
		createHeadClient();

		upperComposite = toolkit.createComposite(mainForm.getBody());
		lauout = new GridLayout(1, true);
		lauout.marginHeight = 0;
		upperComposite.setLayout(lauout);
		upperComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		int borderStyle = toolkit.getBorderStyle();
		toolkit.setBorderStyle(SWT.NULL);
		Tree tree = toolkit.createTree(upperComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		toolkit.setBorderStyle(borderStyle);
		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new StorageManagerTreeContentProvider());
		treeViewer.setLabelProvider(new StorageManagerTreeLabelProvider());
		// treeViewer.setComparator(new ServerViewComparator());
		treeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof GroupedLabelsComposite && e2 instanceof GroupedLabelsComposite) {
					return ObjectUtils.compare((GroupedLabelsComposite) e1, (GroupedLabelsComposite) e2);
				} else if (e1 instanceof Component && e2 instanceof Component) {
					return ((Component) e1).getName().compareToIgnoreCase(((Component) e2).getName());
				}
				return super.compare(viewer, e1, e2);
			}
		});
		treeViewer.addFilter(treeFilter);
		treeViewer.addFilter(filterStorageComposite.getFilter());
		treeViewer.getTree().setVisible(false);
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		storagePropertyForm = new StorageDataPropertyForm(mainComposite);
		storagePropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.addSelectionChangedListener(storagePropertyForm);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
				if (structuredSelection.getFirstElement() instanceof StorageLeaf) {
					lastSelectedLeaf = (StorageLeaf) structuredSelection.getFirstElement();
				} else if (structuredSelection.getFirstElement() instanceof LocalStorageLeaf) {
					lastSelectedLocalStorageLeaf = (LocalStorageLeaf) structuredSelection.getFirstElement();
				}
			}
		});

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateViewToolbar();
			}
		});

		treeViewer.addDoubleClickListener(new DoubleClickListener());

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(MENU_ID, menuManager, treeViewer);
		Control control = treeViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);

		mainComposite.addControlListener(new ControlAdapter() {
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

		updateFormBody();
		updateViewToolbar();

		mainComposite.setWeights(new int[] { 2, 3 });
		getSite().setSelectionProvider(treeViewer);
	}

	/**
	 * Creates the head client for form.
	 */
	private void createHeadClient() {
		Composite headClient = new Composite(mainForm.getHead(), SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		headClient.setLayout(gl);

		new Label(headClient, SWT.NONE).setText("Show available:");

		remoteStorageSelection = new Button(headClient, SWT.RADIO);
		remoteStorageSelection.setText("Online");
		remoteStorageSelection.setSelection(true);

		localStorageSelection = new Button(headClient, SWT.RADIO);
		localStorageSelection.setText("Local");

		remoteStorageSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateViewToolbar();
				updateFormBody();
			}
		});

		// filter composite
		filterStorageComposite = new FilterStorageComposite(headClient, SWT.NONE);
		filterStorageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		mainForm.setHeadClient(headClient);
	}

	/**
	 * Creates the view tool-bar.
	 */
	private void createViewToolbar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(new ShowPropertiesAction());

		MenuAction filterMenuAction = new MenuAction();
		filterMenuAction.setText("Group and Filter");
		filterMenuAction.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_FILTER));

		groupByLabelMenu = new MenuManager("Group Storages By");
		filterMenuAction.addContributionItem(groupByLabelMenu);

		filterByRepositoryMenu = new MenuManager("Filter By Repository");
		filterMenuAction.addContributionItem(filterByRepositoryMenu);

		filterByStateMenu = new MenuManager("Filter By Storage State");
		filterByStateMenu.add(new FilterStatesAction("Writable", StorageState.OPENED));
		filterByStateMenu.add(new FilterStatesAction("Recording", StorageState.RECORDING));
		filterByStateMenu.add(new FilterStatesAction("Readable", StorageState.CLOSED));
		filterMenuAction.addContributionItem(filterByStateMenu);

		toolBarManager.add(filterMenuAction);
		toolBarManager.add(new Separator());

	}

	/**
	 * Updates the storage list for all {@link CmrRepositoryDefinition}.
	 * 
	 * @param jobListener
	 *            the listener.
	 */
	private void updateStorageList(IJobChangeListener jobListener) {
		Job updateStorageListJob = new Job("Update Storages") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				storageRepositoryMap.clear();
				for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
					boolean canUpdate = false;
					if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
						canUpdate = true;
					} else {
						OnlineStatus cachedStatus = cachedOnlineStatus.get(cmrRepositoryDefinition);
						if (OnlineStatus.ONLINE.equals(cachedStatus)) {
							canUpdate = true;
						}
					}
					if (canUpdate) {
						try {
							List<StorageData> storages = cmrRepositoryDefinition.getStorageService().getExistingStorages();
							for (StorageData storage : storages) {
								storageRepositoryMap.put(storage, cmrRepositoryDefinition);
							}
						} catch (Exception e) {
							continue;
						}
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
	 * Updates the storage list only for provided {@link CmrRepositoryDefinition}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param removeOnly
	 *            If set to true, no storages will be loaded from the CMR.
	 * @param jobListener
	 *            the job listener.
	 */
	private void updateStorageList(final CmrRepositoryDefinition cmrRepositoryDefinition, final boolean removeOnly, IJobChangeListener jobListener) {
		Job updateStorageListJob = new Job("Updating Storages") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				while (storageRepositoryMap.values().remove(cmrRepositoryDefinition)) {
					continue;
				}
				if (!removeOnly) {
					boolean canUpdate = false;
					if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
						canUpdate = true;
					} else {
						OnlineStatus cachedStatus = cachedOnlineStatus.get(cmrRepositoryDefinition);
						if (OnlineStatus.ONLINE.equals(cachedStatus)) {
							canUpdate = true;
						}
					}
					if (canUpdate) {
						List<StorageData> storages = cmrRepositoryDefinition.getStorageService().getExistingStorages();
						for (StorageData storage : storages) {
							storageRepositoryMap.put(storage, cmrRepositoryDefinition);
						}
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
	 * Updates the list of downloaded storages.
	 */
	private void updateDownloadedStorages() {
		downloadedStorages.clear();
		downloadedStorages.addAll(InspectIT.getDefault().getInspectITStorageManager().getDownloadedStorages());
	}

	/**
	 * Updates the form body.
	 */
	private void updateFormBody() {
		clearFormBody();
		if (remoteStorageSelection.getSelection()) {
			if (!storageRepositoryMap.isEmpty()) {
				treeViewer.getTree().setVisible(true);
				treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				treeViewer.setInput(new StorageTreeModelManager(storageRepositoryMap, orderingLabelType));
				treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
				if (null != lastSelectedLeaf && storageRepositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
					StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
					treeViewer.setSelection(ss, true);
				}
				filterStorageComposite.setEnabled(true);
			} else {
				displayMessage("No storage information available on currently available CMR repositories.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
				filterStorageComposite.setEnabled(false);
			}
		} else {
			if (!downloadedStorages.isEmpty()) {
				treeViewer.getTree().setVisible(true);
				treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				treeViewer.setInput(new LocalStorageTreeModelManager(downloadedStorages, orderingLabelType));
				treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
				if (null != lastSelectedLocalStorageLeaf && downloadedStorages.contains(lastSelectedLocalStorageLeaf.getLocalStorageData())) {
					StructuredSelection ss = new StructuredSelection(lastSelectedLocalStorageLeaf);
					treeViewer.setSelection(ss, true);
				}
				filterStorageComposite.setEnabled(true);
			} else {
				displayMessage("No downloaded storage is available on the local machine.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
				filterStorageComposite.setEnabled(false);
			}
		}
		upperComposite.layout();
	}

	/**
	 * Clears the look of the forms body.
	 */
	private void clearFormBody() {
		if (cmrMessageComposite != null && !cmrMessageComposite.isDisposed()) {
			cmrMessageComposite.dispose();
		}
		treeViewer.setInput(Collections.emptyList());
		treeViewer.getTree().setVisible(false);
		treeViewer.getTree().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}

	/**
	 * Displays the message on the provided composite.
	 * 
	 * @param text
	 *            Text of message.
	 * @param image
	 *            Image to show.
	 */
	private void displayMessage(String text, Image image) {
		if (null == cmrMessageComposite || cmrMessageComposite.isDisposed()) {
			cmrMessageComposite = toolkit.createComposite(upperComposite);
		} else {
			for (Control c : cmrMessageComposite.getChildren()) {
				if (!c.isDisposed()) {
					c.dispose();
				}
			}
		}
		cmrMessageComposite.setLayout(new GridLayout(2, false));
		cmrMessageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		toolkit.createLabel(cmrMessageComposite, null).setImage(image);
		toolkit.createLabel(cmrMessageComposite, text, SWT.WRAP).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
	}

	/**
	 * Updates the view tool-bar.
	 */
	private void updateViewToolbar() {
		boolean remoteStoragesShown = remoteStorageSelection.getSelection();

		// filter by repository visible only when remote storages are displayed
		filterByRepositoryMenu.removeAll();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
			filterByRepositoryMenu.add(new FilterRepositoriesAction(cmrRepositoryDefinition));
		}
		filterByRepositoryMenu.getParent().update(false);
		filterByRepositoryMenu.setVisible(remoteStoragesShown);

		// filter by state is not visible with downloaded storages displayed
		filterByStateMenu.setVisible(remoteStoragesShown);

		// group by label
		Set<AbstractStorageLabelType<?>> availableLabelTypes = new HashSet<AbstractStorageLabelType<?>>();
		if (remoteStoragesShown) {
			for (StorageData storageData : storageRepositoryMap.keySet()) {
				for (AbstractStorageLabel<?> label : storageData.getLabelList()) {
					availableLabelTypes.add(label.getStorageLabelType());
				}
			}
		} else {
			for (LocalStorageData localStorageData : downloadedStorages) {
				for (AbstractStorageLabel<?> label : localStorageData.getLabelList()) {
					availableLabelTypes.add(label.getStorageLabelType());
				}
			}
		}

		groupByLabelMenu.removeAll();
		if (remoteStoragesShown) {
			groupByLabelMenu.add(new LabelOrderAction("CMR Repository", InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_SERVER_ONLINE_SMALL), null, null == orderingLabelType));
		} else {
			groupByLabelMenu.add(new LabelOrderAction("None", InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STORAGE_DOWNLOADED), null, null == orderingLabelType));
		}
		for (AbstractStorageLabelType<?> labelType : availableLabelTypes) {
			if (labelType.isGroupingEnabled()) {
				groupByLabelMenu.add(new LabelOrderAction(TextFormatter.getLabelName(labelType), ImageFormatter.getImageDescriptorForLabel(labelType), labelType, ObjectUtils.equals(labelType,
						orderingLabelType)));
			}
		}

	}

	/**
	 * Performs update.
	 * 
	 * @param updateStorageList
	 *            If the update should go to the CMRs for an updated storage list.
	 */
	private void performUpdate(final boolean updateStorageList) {
		updateDownloadedStorages();
		if (updateStorageList) {
			updateStorageList(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					SafeExecutor.asyncExec(new Runnable() {
						@Override
						public void run() {
							mainForm.setBusy(true);
							updateFormBody();
							updateViewToolbar();
							mainForm.setBusy(false);
							mainForm.layout();
						}
					}, mainForm);
				}
			});
		} else {
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					mainForm.setBusy(true);
					updateFormBody();
					updateViewToolbar();
					mainForm.setBusy(false);
					mainForm.layout();
				}
			}, mainForm);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		if (treeViewer.getTree().isVisible()) {
			treeViewer.getTree().setFocus();
		} else {
			mainForm.setFocus();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus == OnlineStatus.ONLINE) {
			OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
			if (null == cachedStatus || OnlineStatus.OFFLINE.equals(cachedStatus) || OnlineStatus.UNKNOWN.equals(cachedStatus)) {
				updateStorageList(repositoryDefinition, false, new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						SafeExecutor.asyncExec(new Runnable() {
							@Override
							public void run() {
								updateFormBody();
							}
						}, mainForm);
					}
				});

			}
			cachedOnlineStatus.put(repositoryDefinition, newStatus);
		} else if (newStatus == OnlineStatus.OFFLINE) {
			OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
			if (null == cachedStatus || OnlineStatus.ONLINE.equals(cachedStatus)) {
				updateStorageList(repositoryDefinition, true, new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						SafeExecutor.asyncExec(new Runnable() {
							@Override
							public void run() {
								updateFormBody();
							}
						}, mainForm);
					}
				});

			}
			cachedOnlineStatus.put(repositoryDefinition, newStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
		cachedOnlineStatus.put(cmrRepositoryDefinition, cmrRepositoryDefinition.getOnlineStatus());
		updateStorageList(cmrRepositoryDefinition, false, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				performUpdate(false);
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
		cachedOnlineStatus.remove(cmrRepositoryDefinition);
		updateStorageList(cmrRepositoryDefinition, true, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				performUpdate(false);
			}

		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
		SafeExecutor.asyncExec(new Runnable() {

			@Override
			public void run() {
				updateFormBody();
				updateViewToolbar();
			}
		}, mainForm);
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
	public void refresh() {
		performUpdate(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return !storageRepositoryMap.isEmpty() || !cmrRepositoryManager.getCmrRepositoryDefinitions().isEmpty();
	}

	/**
	 * Refreshes the view, only by refreshing the storages on the given repository.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository to update storages for.
	 */
	public void refresh(CmrRepositoryDefinition cmrRepositoryDefinition) {
		updateStorageList(cmrRepositoryDefinition, false, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				performUpdate(false);
			}

		});
	}

	/**
	 * Show or hides properties.
	 * 
	 * @param show
	 *            Should properties be shown.
	 */
	public void setShowProperties(boolean show) {
		if (show) {
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if (!selection.isEmpty()) {
				if (selection.getFirstElement() instanceof StorageLeaf) {
					StorageLeaf storageLeaf = ((StorageLeaf) selection.getFirstElement());
					storagePropertyForm = new StorageDataPropertyForm(mainComposite, storageLeaf);
					storagePropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				} else if (selection.getFirstElement() instanceof LocalStorageLeaf) {
					IStorageData storageData = ((LocalStorageLeaf) selection.getFirstElement()).getLocalStorageData();
					storagePropertyForm = new StorageDataPropertyForm(mainComposite, null, storageData);
					storagePropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				} else {
					storagePropertyForm = new StorageDataPropertyForm(mainComposite);
					storagePropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				}
			}

			treeViewer.addSelectionChangedListener(storagePropertyForm);
			mainComposite.setWeights(new int[] { 2, 3 });
			mainComposite.layout();
		} else {
			if (null != storagePropertyForm && !storagePropertyForm.isDisposed()) {
				treeViewer.removeSelectionChangedListener(storagePropertyForm);
				storagePropertyForm.dispose();
				storagePropertyForm = null; // NOPMD
			}
			mainComposite.setWeights(new int[] { 1 });
			mainComposite.layout();
		}
	}

	/**
	 * Performs update of the view, without getting data from CMR.
	 */
	public void refreshWithoutCmrCall() {
		performUpdate(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageDataUpdated(IStorageData storageData) {
		CmrRepositoryDefinition repositoryToUpdate = storageRepositoryMap.get(storageData);
		if (null != repositoryToUpdate) {
			refresh(repositoryToUpdate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageRemotelyDeleted(IStorageData storageData) {
		for (Iterator<Entry<StorageData, CmrRepositoryDefinition>> it = storageRepositoryMap.entrySet().iterator(); it.hasNext();) {
			if (Objects.equals(it.next().getKey().getId(), storageData.getId())) {
				it.remove();
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (remoteStorageSelection.getSelection()) {
							refreshWithoutCmrCall();
						}
					}
				}, remoteStorageSelection);

				break;
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storageLocallyDeleted(IStorageData storageData) {
		for (Iterator<LocalStorageData> it = downloadedStorages.iterator(); it.hasNext();) {
			if (Objects.equals(it.next().getId(), storageData.getId())) {
				it.remove();
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (localStorageSelection.getSelection()) {
							refreshWithoutCmrCall();
						}
					}
				}, localStorageSelection);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		cmrRepositoryManager.removeCmrRepositoryChangeListener(this);
		storageManager.removeStorageChangeListener(this);
		super.dispose();
	}

	/**
	 * Filter for the tree.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class TreeFilter extends ViewerFilter {

		/**
		 * Set of excluded repositories.
		 */
		private Set<CmrRepositoryDefinition> filteredRespositories = new HashSet<CmrRepositoryDefinition>();

		/**
		 * Set of excluded states.
		 */
		private Set<StorageState> filteredStates = new HashSet<StorageState>();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof StorageLeaf) {
				StorageLeaf storageLeaf = (StorageLeaf) element;
				if (filteredRespositories.contains(storageLeaf.getCmrRepositoryDefinition())) {
					return false;
				}
				if (filteredStates.contains(storageLeaf.getStorageData().getState())) {
					return false;
				}
			}
			return true;
		}

		/**
		 * @return the filteredRespositories
		 */
		public Set<CmrRepositoryDefinition> getFilteredRespositories() {
			return filteredRespositories;
		}

		/**
		 * @return the filteredStates
		 */
		public Set<StorageState> getFilteredStates() {
			return filteredStates;
		}

	}

	/**
	 * Action for selecting the grouping of storages.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class LabelOrderAction extends Action {

		/**
		 * Label type to group.
		 */
		private AbstractStorageLabelType<?> labelType;

		/**
		 * Constructor.
		 * 
		 * @param name
		 *            Name of action.
		 * @param imgDescriptor
		 *            {@link ImageDescriptor}.
		 * @param labelType
		 *            Label type to represent. Null for default settings.
		 * @param isChecked
		 *            Should be checked.
		 */
		public LabelOrderAction(String name, ImageDescriptor imgDescriptor, AbstractStorageLabelType<?> labelType, boolean isChecked) {
			super(name, Action.AS_RADIO_BUTTON);
			this.labelType = labelType;
			setChecked(isChecked);
			setImageDescriptor(imgDescriptor);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				orderingLabelType = labelType;
				updateFormBody();
			}
		}
	}

	/**
	 * Filter by storage repository action.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class FilterRepositoriesAction extends Action {

		/**
		 * Cmr to exclude/include.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * @param cmrRepositoryDefinition
		 *            Cmr to exclude/include.
		 */
		public FilterRepositoriesAction(CmrRepositoryDefinition cmrRepositoryDefinition) {
			super();
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			setText(cmrRepositoryDefinition.getName());
			setChecked(!treeFilter.getFilteredRespositories().contains(cmrRepositoryDefinition));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				treeFilter.getFilteredRespositories().remove(cmrRepositoryDefinition);
			} else {
				treeFilter.getFilteredRespositories().add(cmrRepositoryDefinition);
			}
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRepositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
		}

	}

	/**
	 * Filter by storage state action.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class FilterStatesAction extends Action {

		/**
		 * Storage state to exclude/include.
		 */
		private StorageState state;

		/**
		 * 
		 * @param text
		 *            Action text.
		 * @param state
		 *            Storage state to exclude/include.
		 */
		public FilterStatesAction(String text, StorageState state) {
			super();
			this.state = state;
			setText(text);
			setChecked(!treeFilter.getFilteredStates().contains(state));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				treeFilter.getFilteredStates().remove(state);
			} else {
				treeFilter.getFilteredStates().add(state);
			}
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRepositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
		}

	}

	/**
	 * Action for show hide properties.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ShowPropertiesAction extends Action {

		/**
		 * Default constructor.
		 */
		public ShowPropertiesAction() {
			super(null, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_PROPERTIES));
			setChecked(true);
			setToolTipText("Hide Properties");
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			if (isChecked()) {
				setShowProperties(true);
				setToolTipText("Hide Properties");
			} else {
				setShowProperties(false);
				setToolTipText("Show Properties");
			}
		};
	}

	/**
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private final class FilterStorageComposite extends FilterComposite {

		/**
		 * String to be filtered.
		 */
		private String filterString = "";

		/**
		 * Filter.
		 */
		private ViewerFilter filter = new ViewerFilter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Objects.equals("", filterString)) {
					return true;
				} else {
					if (element instanceof IStorageDataProvider) {
						return select(((IStorageDataProvider) element).getStorageData());
					} else if (element instanceof ILocalStorageDataProvider) {
						return select(((ILocalStorageDataProvider) element).getLocalStorageData());
					}
					return true;
				}
			}

			/**
			 * Does a filter select on {@link StorageData}.
			 * 
			 * @param storageData
			 *            {@link IStorageData}
			 * @return True if data in {@link IStorageData} fits the filter string.
			 */
			private boolean select(IStorageData storageData) {
				if (StringUtils.containsIgnoreCase(storageData.getName(), filterString)) {
					return true;
				}
				if (StringUtils.containsIgnoreCase(storageData.getDescription(), filterString)) {
					return true;
				}
				for (AbstractStorageLabel<?> label : storageData.getLabelList()) {
					if (StringUtils.containsIgnoreCase(TextFormatter.getLabelValue(label, false), filterString)) {
						return true;
					}
				}

				if (storageData instanceof StorageData) {
					if (StringUtils.containsIgnoreCase(((StorageData) storageData).getState().toString(), filterString)) {
						return true;
					}
				}

				return false;
			}

		};

		/**
		 * Default constructor.
		 * 
		 * @param parent
		 *            A widget which will be the parent of the new instance (cannot be null).
		 * @param style
		 *            The style of widget to construct.
		 * @see Composite#Composite(Composite, int)
		 */
		public FilterStorageComposite(Composite parent, int style) {
			super(parent, style, "Filter storages");
			((GridLayout) getLayout()).marginWidth = 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void executeCancel() {
			this.filterString = "";
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (remoteStorageSelection.getSelection()) {
				if (null != lastSelectedLeaf && storageRepositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
					StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
					treeViewer.setSelection(ss, true);
				}
			} else {
				if (null != lastSelectedLocalStorageLeaf && downloadedStorages.contains(lastSelectedLocalStorageLeaf.getLocalStorageData())) {
					StructuredSelection ss = new StructuredSelection(lastSelectedLocalStorageLeaf);
					treeViewer.setSelection(ss, true);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void executeFilter(String filterString) {
			this.filterString = filterString;
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (remoteStorageSelection.getSelection()) {
				if (null != lastSelectedLeaf && storageRepositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
					StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
					treeViewer.setSelection(ss, true);
				}
			} else {
				if (null != lastSelectedLocalStorageLeaf && downloadedStorages.contains(lastSelectedLocalStorageLeaf.getLocalStorageData())) {
					StructuredSelection ss = new StructuredSelection(lastSelectedLocalStorageLeaf);
					treeViewer.setSelection(ss, true);
				}
			}
		}

		/**
		 * Gets {@link #filter}.
		 * 
		 * @return {@link #filter}
		 */
		public ViewerFilter getFilter() {
			return filter;
		}

	}

	/**
	 * Double click listener, that opens the data explorer.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class DoubleClickListener implements IDoubleClickListener {

		/**
		 * {@inheritDoc}
		 */
		public void doubleClick(final DoubleClickEvent event) {
			UIJob openDataExplorerJob = new UIJob("Opening Data Explorer..") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					process();
					return Status.OK_STATUS;
				}
			};
			openDataExplorerJob.setUser(true);
			openDataExplorerJob.schedule();
		}

		/**
		 * Processes the double-click.
		 */
		private void process() {
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if (selection.getFirstElement() instanceof IStorageDataProvider) {
				showStorage((IStorageDataProvider) selection.getFirstElement(), InspectIT.getDefault().getInspectITStorageManager());
			} else if (selection.getFirstElement() instanceof ILocalStorageDataProvider) {
				showStorage((ILocalStorageDataProvider) selection.getFirstElement(), InspectIT.getDefault().getInspectITStorageManager());
			} else {
				TreeSelection treeSelection = (TreeSelection) selection;
				TreePath path = treeSelection.getPaths()[0];
				if (null != path) {
					boolean expanded = treeViewer.getExpandedState(path);
					if (expanded) {
						treeViewer.collapseToLevel(path, 1);
					} else {
						treeViewer.expandToLevel(path, 1);
					}
				}
			}
		}

		/**
		 * Executes show repository command.
		 * 
		 * @param repositoryDefinition
		 *            Repository to open.
		 */
		private void executeShowRepositoryCommand(RepositoryDefinition repositoryDefinition) {
			try {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(ShowRepositoryHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
				context.addVariable(ShowRepositoryHandler.REPOSITORY_DEFINITION, repositoryDefinition);

				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Shows storage from the {@link IStorageDataProvider}.
		 * 
		 * @param storageDataProvider
		 *            {@link IStorageDataProvider}
		 * @param storageManager
		 *            {@link InspectITStorageManager}
		 */
		private void showStorage(IStorageDataProvider storageDataProvider, final InspectITStorageManager storageManager) {
			final StorageData storageData = storageDataProvider.getStorageData();
			final CmrRepositoryDefinition cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();
			try {
				if (storageManager.isStorageMounted(storageData)) {
					// if we already have all data needed, get the repository definition and show it
					LocalStorageData localStorageData = storageManager.getLocalDataForStorage(storageData);
					RepositoryDefinition repositoryDefinition = storageManager.getStorageRepositoryDefinition(localStorageData);
					executeShowRepositoryCommand(repositoryDefinition);
				} else if (storageData.getState() == StorageState.CLOSED) {
					// if it is closed, mount it first
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								SubMonitor subMonitor = SubMonitor.convert(monitor);
								storageManager.mountStorage(storageData, cmrRepositoryDefinition, subMonitor);
								monitor.done();
							} catch (Exception e) {
								throw new InvocationTargetException(e);
							}
						}
					});
					LocalStorageData localStorageData = storageManager.getLocalDataForStorage(storageData);
					RepositoryDefinition repositoryDefinition = storageManager.getStorageRepositoryDefinition(localStorageData);
					executeShowRepositoryCommand(repositoryDefinition);
				} else if (storageData.getState() == StorageState.OPENED) {
					// if it's in writable state offer user to finalize it and explore it
					String dialogMessage = "Storages that are in writable mode can not be explored. Do you want to finalize selected storage first and then open it?";
					MessageDialog dialog = new MessageDialog(getSite().getShell(), "Opening Writable Storage", null, dialogMessage, MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
					if (0 == dialog.open()) {
						treeViewer.setSelection(treeViewer.getSelection());
						IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
						ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

						Command command = commandService.getCommand(CloseAndShowStorageHandler.COMMAND);
						ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
						IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
						context.addVariable(CloseAndShowStorageHandler.STORAGE_DATA_PROVIDER, storageDataProvider);
						context.addVariable(ISources.ACTIVE_SITE_NAME, getSite());
						try {
							command.executeWithChecks(executionEvent);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					return;
				} else if (storageData.getState() == StorageState.RECORDING) {
					// if it is used for recording, just show message
					InspectIT.getDefault().createInfoDialog("Selected storage is currently used for recording, it can not be explored.", -1);
				}
			} catch (InvocationTargetException | InterruptedException e) { // NOPMD
				InspectIT.getDefault().createErrorDialog("Exception occurred trying to mount the storage", e, -1);
			} catch (SerializationException e) {
				String msg = "Data in the remote storage " + storageData + " can not be read with this version of inspectIT.";
				if (null != storageData.getCmrVersion()) {
					msg += " Version of the CMR where storage was created is " + storageData.getCmrVersion() + ".";
				} else {
					msg += " Version of the CMR where storage was created is unknown";
				}
				InspectIT.getDefault().createErrorDialog(msg, e, -1);
			} catch (BusinessException | IOException e) {
				InspectIT.getDefault().createErrorDialog("Exception occurred trying to display the remote storage", e, -1);
			}
		}

		/**
		 * Shows storage from the {@link ILocalStorageDataProvider}.
		 * 
		 * @param localStorageDataProvider
		 *            {@link ILocalStorageDataProvider}
		 * @param storageManager
		 *            {@link InspectITStorageManager}
		 */
		private void showStorage(ILocalStorageDataProvider localStorageDataProvider, InspectITStorageManager storageManager) {
			LocalStorageData localStorageData = localStorageDataProvider.getLocalStorageData();
			try {
				if (localStorageData.isFullyDownloaded()) {
					StorageRepositoryDefinition storageRepositoryDefinition;
					storageRepositoryDefinition = storageManager.getStorageRepositoryDefinition(localStorageData);
					executeShowRepositoryCommand(storageRepositoryDefinition);
				}
			} catch (SerializationException e) {
				String msg = "Data in the remote storage " + localStorageData + " can not be read with this version of inspectIT.";
				if (null != localStorageData.getCmrVersion()) {
					msg += " Version of the CMR where storage was created is " + localStorageData.getCmrVersion() + ".";
				} else {
					msg += " Version of the CMR where storage was created is unknown";
				}
				InspectIT.getDefault().createErrorDialog(msg, e, -1);
			} catch (BusinessException | IOException e) {
				InspectIT.getDefault().createErrorDialog("Exception occurred trying to display the downloaded storage", e, -1);
			}
		}
	}

}
