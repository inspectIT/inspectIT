package info.novatec.inspectit.rcp.view.impl;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.tree.DeferredTreeViewer;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.TreeModelManager;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager.UpdateRepositoryJob;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.listener.StorageChangeListener;
import info.novatec.inspectit.rcp.util.SafeExecutor;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;
import info.novatec.inspectit.rcp.view.IRefreshableView;
import info.novatec.inspectit.rcp.view.listener.TreeViewDoubleClickListener;
import info.novatec.inspectit.rcp.view.tree.TreeContentProvider;
import info.novatec.inspectit.rcp.view.tree.TreeLabelProvider;
import info.novatec.inspectit.rcp.view.tree.TreeViewerComparator;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Objects;

/**
 * Data explorer view show one Agent from a given {@link RepositoryDefinition}. Other agents can be
 * selected via view menu.
 * 
 * @author Ivan Senic
 * 
 */
public class DataExplorerView extends ViewPart implements CmrRepositoryChangeListener, StorageChangeListener, IRefreshableView {

	/**
	 * ID of the refresh contribution item needed for setting the visibility.
	 */
	private static final String REFRESH_CONTRIBUTION_ITEM = "info.novatec.inspectit.rcp.view.dataExplorer.refresh";

	/**
	 * ID of the refresh contribution item needed for setting the visibility.
	 */
	private static final String CLEAR_BUFFER_CONTRIBUTION_ITEM = "info.novatec.inspectit.rcp.view.dataExplorer.clearBuffer";

	/**
	 * ID of this view.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.view.dataExplorer";

	/**
	 * Displayed repository.
	 */
	private RepositoryDefinition displayedRepositoryDefinition;

	/**
	 * Displayed agent.
	 */
	private PlatformIdent displayedAgent;

	/**
	 * Available agents for displaying.
	 */
	private List<? extends PlatformIdent> availableAgents;

	/**
	 * Cashed statuses of CMR repository definitions.
	 */
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedOnlineStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * Listener for tree double clicks.
	 */
	private final TreeViewDoubleClickListener treeViewDoubleClickListener = new TreeViewDoubleClickListener();

	/**
	 * Toolkit used for the view components.
	 */
	private FormToolkit toolkit;

	/**
	 * Main form for display of the repository.
	 */
	private Form mainForm;

	/**
	 * Tree in the form for the agents representation.
	 */
	private DeferredTreeViewer treeViewer;

	/**
	 * Composite used for message displaying.
	 */
	private Composite messageComposite;

	/**
	 * Collapse action.
	 */
	private CollapseAction collapseAction;

	/**
	 * Adapter to publish the selection to the Site.
	 */
	private SelectionProviderAdapter selectionProviderAdapter = new SelectionProviderAdapter();

	/**
	 * Combo where agents are displayed.
	 */
	private Combo agentsCombo;

	/**
	 * Toolbar manager for the view.
	 */
	private IToolBarManager toolBarManager;

	/**
	 * Map of the cached expanded objects in the agent tree per agent/repository combination. Key
	 * for this map is the combined hash code that can be obtained by calling method
	 * {@link #getHashCodeForAgentRepository(PlatformIdent, RepositoryDefinition)}.
	 */
	private Map<Integer, List<Object>> expandedElementsPerAgent = new ConcurrentHashMap<Integer, List<Object>>();

	/**
	 * If the inactive instrumentations should be hidden.
	 */
	private boolean hideInactiveInstrumentations = true;

	/**
	 * Default constructor.
	 */
	public DataExplorerView() {
		InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().addStorageChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		createViewToolbar();

		toolkit = new FormToolkit(parent.getDisplay());
		mainForm = toolkit.createForm(parent);
		mainForm.getBody().setLayout(new GridLayout(1, true));
		createHeadClient();
		toolkit.decorateFormHeading(mainForm);

		int borderStyle = toolkit.getBorderStyle();
		toolkit.setBorderStyle(SWT.NULL);
		Tree tree = toolkit.createTree(mainForm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
		toolkit.setBorderStyle(borderStyle);
		treeViewer = new DeferredTreeViewer(tree);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setComparator(new TreeViewerComparator());
		treeViewer.addDoubleClickListener(treeViewDoubleClickListener);
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		updateFormTitle();
		updateFormBody();
		updateAgentsCombo();

		RepositoryDefinition lastSelectedRepositoryDefinition = PreferencesUtils.getObject(PreferencesConstants.LAST_SELECTED_REPOSITORY);
		if (null != lastSelectedRepositoryDefinition) {
			showRepository(lastSelectedRepositoryDefinition, null);
			if (CollectionUtils.isNotEmpty(availableAgents)) {
				long lastSelectedAgentId = PreferencesUtils.getLongValue(PreferencesConstants.LAST_SELECTED_AGENT);
				for (PlatformIdent platformIdent : availableAgents) {
					if (platformIdent.getId().longValue() == lastSelectedAgentId) {
						selectAgentForDisplay(platformIdent);
						performUpdate();
						break;
					}
				}
			}
		}

		getSite().setSelectionProvider(selectionProviderAdapter);
	}

	/**
	 * Show the given repository on the view. If the selected agent is not provided, the arbitrary
	 * agent will be shown.
	 * 
	 * @param repositoryDefinition
	 *            Repository definition to display.
	 * @param agent
	 *            Agent to select. Can be null. If the repository does not
	 */
	public void showRepository(final RepositoryDefinition repositoryDefinition, final PlatformIdent agent) {
		SafeExecutor.syncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(true);
				if (null != displayedAgent && null != displayedRepositoryDefinition) {
					cacheExpandedObjects(displayedAgent, displayedRepositoryDefinition);
				}
				updateFormTitle();
				agentsCombo.removeAll();
				displayMessage("Loading agents for repository " + repositoryDefinition.getName(), Display.getDefault().getSystemImage(SWT.ICON_WORKING));
			}
		}, mainForm, agentsCombo);
		displayedRepositoryDefinition = repositoryDefinition;

		PreferencesUtils.saveObject(PreferencesConstants.LAST_SELECTED_REPOSITORY, displayedRepositoryDefinition, false);
		updateAvailableAgents(repositoryDefinition, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				selectAgentForDisplay(agent);

				StructuredSelection ss = new StructuredSelection(repositoryDefinition);
				selectionProviderAdapter.setSelection(ss);

				performUpdate();
			}
		});

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(false);
			}
		});
	}

	/**
	 * Selects the provided agent for display, if it is in the {@link #availableAgents} list. If
	 * not, a arbitrary agent will be selected if any is available.
	 * 
	 * @param agent
	 *            Hint for agent selection.
	 */
	private void selectAgentForDisplay(PlatformIdent agent) {
		SafeExecutor.syncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(true);
				displayMessage("Loading agent tree..", Display.getDefault().getSystemImage(SWT.ICON_WORKING));
			}
		}, mainForm);
		try {
			if (null != agent && CollectionUtils.isNotEmpty(availableAgents) && availableAgents.contains(agent)) {
				displayedAgent = displayedRepositoryDefinition.getGlobalDataAccessService().getCompleteAgent(agent.getId());
				PreferencesUtils.saveLongValue(PreferencesConstants.LAST_SELECTED_AGENT, agent.getId().longValue(), false);
			} else if (CollectionUtils.isNotEmpty(availableAgents)) {
				agent = availableAgents.iterator().next();
				displayedAgent = displayedRepositoryDefinition.getGlobalDataAccessService().getCompleteAgent(agent.getId());
			} else {
				displayedAgent = null; // NOPMD
			}
		} catch (BusinessException e) {
			InspectIT.getDefault().createErrorDialog("Exception occurred trying to load the agent tree for the agent " + agent.getAgentName() + ".", e, -1);
			displayedAgent = null; // NOPMD
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(false);
			}
		});
	}

	/**
	 * Caches the current expanded objects in the tree viewer with the given platform
	 * ident/repository combination. Note that this method will filter out the elements given by
	 * {@link org.eclipse.jface.viewers.TreeViewer#getExpandedElements()}, so that only the last
	 * expanded element in the tree is saved.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent} to cache elements for.
	 * @param repositoryDefinition
	 *            Repository that platform is belonging to.
	 */
	private void cacheExpandedObjects(PlatformIdent platformIdent, RepositoryDefinition repositoryDefinition) {
		Object[] allExpanded = treeViewer.getExpandedElements();
		if (allExpanded.length > 0) {
			Set<Object> parents = new HashSet<Object>();
			for (Object expanded : allExpanded) {
				Object parent = ((ITreeContentProvider) treeViewer.getContentProvider()).getParent(expanded);
				while (parent != null) {
					parents.add(parent);
					parent = ((ITreeContentProvider) treeViewer.getContentProvider()).getParent(parent);
				}
			}
			List<Object> expandedList = new ArrayList<Object>(Arrays.asList(allExpanded));
			expandedList.removeAll(parents);
			expandedElementsPerAgent.put(getHashCodeForAgentRepository(platformIdent, repositoryDefinition), expandedList);
		} else {
			expandedElementsPerAgent.put(getHashCodeForAgentRepository(platformIdent, repositoryDefinition), Collections.emptyList());
		}
	}

	/**
	 * Returns the hash code combination for {@link PlatformIdent} and {@link RepositoryDefinition}.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent}
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}
	 * @return The hash code as int.
	 */
	private int getHashCodeForAgentRepository(PlatformIdent platformIdent, RepositoryDefinition repositoryDefinition) {
		return Objects.hashCode(platformIdent, repositoryDefinition);
	}

	/**
	 * Updates the list of available agents.
	 * 
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}.
	 * @param jobListener
	 *            the listener.
	 */
	private void updateAvailableAgents(final RepositoryDefinition repositoryDefinition, IJobChangeListener jobListener) {
		Job updateAvailableAgentsJob = new Job("Updating Available Agents") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (repositoryDefinition instanceof CmrRepositoryDefinition) {
					CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;
					if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
						availableAgents = new ArrayList<PlatformIdent>(cmrRepositoryDefinition.getGlobalDataAccessService().getAgentsOverview().keySet());
					} else {
						availableAgents = null; // NOPMD
					}
				} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
					StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
					if (storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded() || storageRepositoryDefinition.getCmrRepositoryDefinition().getOnlineStatus() != OnlineStatus.OFFLINE) {
						availableAgents = new ArrayList<PlatformIdent>(storageRepositoryDefinition.getGlobalDataAccessService().getAgentsOverview().keySet());
					} else {
						availableAgents = null; // NOPMD
					}
				} else {
					availableAgents = null; // NOPMD
				}
				if (CollectionUtils.isNotEmpty(availableAgents)) {
					Collections.sort(availableAgents, new Comparator<PlatformIdent>() {
						@Override
						public int compare(PlatformIdent o1, PlatformIdent o2) {
							return ObjectUtils.compare(o1.getAgentName(), o2.getAgentName());
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		if (null != jobListener) {
			updateAvailableAgentsJob.addJobChangeListener(jobListener);
		}
		updateAvailableAgentsJob.schedule();
	}

	/**
	 * Creates view toolbar.
	 */
	private void createViewToolbar() {
		toolBarManager = getViewSite().getActionBars().getToolBarManager();

		ShowHideInactiveInstrumentationsAction showHideInactiveInstrumentationsAction = new ShowHideInactiveInstrumentationsAction();
		toolBarManager.add(showHideInactiveInstrumentationsAction);

		collapseAction = new CollapseAction();
		toolBarManager.add(collapseAction);
	}

	/**
	 * Creates the head client that holds the agents in combo box.
	 */
	private void createHeadClient() {
		Composite headClient = new Composite(mainForm.getHead(), SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		headClient.setLayout(gl);

		Label agentImg = new Label(headClient, SWT.NONE);
		agentImg.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT));

		agentsCombo = new Combo(headClient, SWT.READ_ONLY | SWT.BORDER | SWT.DROP_DOWN);
		agentsCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		agentsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selected = agentsCombo.getSelectionIndex();
				if (selected < availableAgents.size()) {
					PlatformIdent platformIdent = availableAgents.get(selected);
					if (!ObjectUtils.equals(displayedAgent, platformIdent)) {
						if (null != displayedAgent && null != displayedRepositoryDefinition) {
							cacheExpandedObjects(displayedAgent, displayedRepositoryDefinition);
						}
						selectAgentForDisplay(platformIdent);
						performUpdate();
					}
				}
			}
		});

		mainForm.setHeadClient(headClient);
	}

	/**
	 * Updates the combo menu with agents.
	 */
	private void updateAgentsCombo() {
		agentsCombo.removeAll();
		if (null != availableAgents && !availableAgents.isEmpty()) {
			agentsCombo.setEnabled(true);
			int i = 0;
			int selectedIndex = -1;
			for (PlatformIdent platformIdent : availableAgents) {
				agentsCombo.add(TextFormatter.getAgentDescription(platformIdent));
				if (ObjectUtils.equals(platformIdent, displayedAgent)) {
					selectedIndex = i;
				}
				i++;
			}
			if (-1 != selectedIndex) {
				agentsCombo.select(selectedIndex);
			}
		} else {
			agentsCombo.setEnabled(false);
		}
		mainForm.getHead().layout();
	}

	/**
	 * Updates the form title.
	 */
	private void updateFormTitle() {
		if (null != displayedRepositoryDefinition) {
			if (displayedRepositoryDefinition instanceof CmrRepositoryDefinition) {
				CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) displayedRepositoryDefinition;
				mainForm.setImage(ImageFormatter.getCmrRepositoryImage(cmrRepositoryDefinition, true));
				mainForm.setText(cmrRepositoryDefinition.getName());
				mainForm.setToolTipText(TextFormatter.getCmrRepositoryDescription(cmrRepositoryDefinition));
			} else if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
				StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
				mainForm.setImage(ImageFormatter.getStorageRepositoryImage(storageRepositoryDefinition));
				mainForm.setText(storageRepositoryDefinition.getName());
				mainForm.setToolTipText(getStorageDescirption(storageRepositoryDefinition));
			}
			mainForm.setMessage(null);
		} else {
			mainForm.setImage(null);
			mainForm.setText("No repository loaded");
			mainForm.setMessage("Repositories can be loaded from Repository or Storage Manager", IMessageProvider.WARNING);
			mainForm.setToolTipText(null);
		}
	}

	/**
	 * Updates the tree input and refreshes the tree.
	 */

	private void updateFormBody() {
		clearFormBody();
		if (null != displayedRepositoryDefinition && null != displayedAgent) {
			TreeModelManager treeModelManager = null;
			treeModelManager = new TreeModelManager(displayedRepositoryDefinition, displayedAgent, hideInactiveInstrumentations);
			if (null != treeModelManager && null != displayedAgent) {
				treeViewer.setInput(treeModelManager);
				treeViewer.getTree().setVisible(true);
				treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			} else {
				displayMessage("Repository is currently unavailable.", Display.getDefault().getSystemImage(SWT.ICON_ERROR));
			}
		} else if (null != displayedRepositoryDefinition && null == displayedAgent) {
			if (null == availableAgents) {
				displayMessage("No agent could be loaded on selected repository.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
			} else {
				displayMessage("This repository is empty.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
			}
		}

		mainForm.getBody().layout();
	}

	/**
	 * Updates view tool-bar.
	 */
	private void updateViewToolbar() {
		collapseAction.updateEnabledState();
		toolBarManager.find(REFRESH_CONTRIBUTION_ITEM).setVisible(displayedRepositoryDefinition instanceof CmrRepositoryDefinition);
		toolBarManager.find(CLEAR_BUFFER_CONTRIBUTION_ITEM).setVisible(
				displayedRepositoryDefinition instanceof CmrRepositoryDefinition && !OnlineStatus.OFFLINE.equals(((CmrRepositoryDefinition) displayedRepositoryDefinition).getOnlineStatus()));
		toolBarManager.update(true);
	}

	/**
	 * Clears the look of the form.
	 */
	private void clearFormBody() {
		if (messageComposite != null && !messageComposite.isDisposed()) {
			messageComposite.dispose();
		}
		treeViewer.setInput(null);
		treeViewer.getTree().setVisible(false);
		treeViewer.getTree().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}

	/**
	 * Updates the form.
	 */
	public void performUpdate() {
		SafeExecutor.asyncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(true);
				updateFormTitle();
				updateFormBody();
				updateAgentsCombo();
				updateViewToolbar();
				if (null != displayedAgent) {
					List<Object> expandedObjects = expandedElementsPerAgent.get(getHashCodeForAgentRepository(displayedAgent, displayedRepositoryDefinition));
					if (null != expandedObjects) {
						for (Object object : expandedObjects) {
							treeViewer.expandObject(object, 1);
						}
					}
				}
				mainForm.setBusy(false);
			}
		}, mainForm, agentsCombo, treeViewer.getTree());
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
	public void refresh() {
		if (displayedRepositoryDefinition instanceof CmrRepositoryDefinition) {
			if (null != displayedAgent) {
				cacheExpandedObjects(displayedAgent, displayedRepositoryDefinition);
			}
			final UpdateRepositoryJob job = InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate((CmrRepositoryDefinition) displayedRepositoryDefinition);
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					updateAvailableAgents(displayedRepositoryDefinition, new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (null != availableAgents && !availableAgents.isEmpty() && null != displayedAgent) {
								boolean found = false;
								for (PlatformIdent platformIdent : availableAgents) {
									if (platformIdent.getId().longValue() == displayedAgent.getId()) {
										selectAgentForDisplay(platformIdent);
										found = true;
										break;
									}
								}
								if (!found) {
									selectAgentForDisplay(availableAgents.get(0));
								}
							} else if (null != availableAgents && !availableAgents.isEmpty() && null == displayedAgent) {
								selectAgentForDisplay(availableAgents.get(0));
							} else {
								selectAgentForDisplay(null);
							}
							performUpdate();
						}
					});
					job.removeJobChangeListener(this);
				}
			});
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
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING) {
			boolean shouldUpdate = ObjectUtils.equals(displayedRepositoryDefinition, repositoryDefinition);
			if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
				shouldUpdate |= ObjectUtils.equals(((StorageRepositoryDefinition) displayedRepositoryDefinition).getCmrRepositoryDefinition(), repositoryDefinition);
			}
			if (shouldUpdate) {
				OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
				if (cachedStatus == OnlineStatus.OFFLINE && newStatus == OnlineStatus.ONLINE) {
					updateAvailableAgents(displayedRepositoryDefinition, new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							SafeExecutor.asyncExec(new Runnable() {
								@Override
								public void run() {
									mainForm.setBusy(true);
									updateFormTitle();
									updateFormBody();
									updateAgentsCombo();
									updateViewToolbar();
									mainForm.setBusy(false);
								}
							}, mainForm, agentsCombo);
						}
					});
				} else if (cachedStatus == OnlineStatus.ONLINE && newStatus == OnlineStatus.OFFLINE) {
					SafeExecutor.asyncExec(new Runnable() {
						@Override
						public void run() {
							mainForm.setBusy(true);
							updateFormTitle();
							updateAgentsCombo();
							updateViewToolbar();
							mainForm.setBusy(false);
						}
					}, mainForm, agentsCombo);
				}
			}
			cachedOnlineStatus.put(repositoryDefinition, newStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (ObjectUtils.equals(cmrRepositoryDefinition, displayedRepositoryDefinition)) {
			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					mainForm.setBusy(true);
					updateFormTitle();
					mainForm.setBusy(false);
				}
			}, mainForm);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (ObjectUtils.equals(cmrRepositoryDefinition, displayedRepositoryDefinition)) {
			displayedRepositoryDefinition = null; // NOPMD
			displayedAgent = null; // NOPMD
			performUpdate();
		} else if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (ObjectUtils.equals(cmrRepositoryDefinition, storageRepositoryDefinition.getCmrRepositoryDefinition()) && !storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded()) {
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						agentsCombo.removeAll();
						agentsCombo.setEnabled(false);
						displayMessage("CMR Repository for selected storage was removed.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
					}
				}, agentsCombo);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
		if (ObjectUtils.equals(cmrRepositoryDefinition, displayedRepositoryDefinition)) {
			availableAgents.remove(agent);
			if (ObjectUtils.equals(agent, displayedAgent)) {
				selectAgentForDisplay(null);
			}

			performUpdate();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageDataUpdated(IStorageData storageData) {
		if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (ObjectUtils.equals(storageData.getId(), storageRepositoryDefinition.getLocalStorageData().getId())) {
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						updateFormTitle();
					}
				}, mainForm);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageRemotelyDeleted(IStorageData storageData) {
		if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (!storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded() && ObjectUtils.equals(storageData.getId(), storageRepositoryDefinition.getLocalStorageData().getId())) {
				SafeExecutor.asyncExec(new Runnable() {
					@Override
					public void run() {
						agentsCombo.removeAll();
						agentsCombo.setEnabled(false);
						displayMessage("Selected storage was remotely deleted and is not available anymore.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
					}
				}, agentsCombo);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageLocallyDeleted(IStorageData storageData) {
		if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (ObjectUtils.equals(storageData.getId(), storageRepositoryDefinition.getLocalStorageData().getId())) {
				if (InspectIT.getDefault().getInspectITStorageManager().getMountedAvailableStorages().contains(storageData)) {
					// if remote one is available, just update
					performUpdate();
				} else {
					// if it is not available on the CMR, remove everything
					SafeExecutor.asyncExec(new Runnable() {
						@Override
						public void run() {
							agentsCombo.removeAll();
							agentsCombo.setEnabled(false);
							displayMessage("Selected storage was locally deleted and is not available anymore.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
						}
					}, agentsCombo);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getCmrRepositoryManager().removeCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().removeStorageChangeListener(this);
		super.dispose();
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
	 * Returns storage description for title box.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}
	 * @return Description for title box.
	 */
	private String getStorageDescirption(StorageRepositoryDefinition storageRepositoryDefinition) {
		LocalStorageData localStorageData = storageRepositoryDefinition.getLocalStorageData();
		if (localStorageData.isFullyDownloaded()) {
			return "Storage Repository - Accessible offline";
		} else {
			return "Storage Repository - Accessible via CMR repository";
		}
	}

	/**
	 * Action that collapses all agents.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CollapseAction extends Action {

		/**
		 * Default constructor.
		 */
		public CollapseAction() {
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_COLLAPSE));
			setToolTipText("Collapse All");
			updateEnabledState();
		}

		/**
		 * Updates the enabled state of action based on the currently selected
		 * {@link CmrRepositoryDefinition}.
		 */
		public final void updateEnabledState() {
			if (null != treeViewer && treeViewer.getInput() != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			treeViewer.setExpandedElements(new Object[0]);
			treeViewer.refresh();
		}

	}

	/**
	 * Class for handling the showing / hiding of the inactive instrumentations.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ShowHideInactiveInstrumentationsAction extends Action {

		/**
		 * Default constructor.
		 */
		public ShowHideInactiveInstrumentationsAction() {
			super(null, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_INSTRUMENTATION_BROWSER_INACTIVE));
			setChecked(!hideInactiveInstrumentations);
			updateToolTipText();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			hideInactiveInstrumentations = !isChecked(); // NOPMD
			// Bug in PMD reporting inverting of boolean
			updateToolTipText();
			if (null != displayedAgent && null != displayedRepositoryDefinition) {
				cacheExpandedObjects(displayedAgent, displayedRepositoryDefinition);
			}
			performUpdate();
		}

		/**
		 * Updates tool-tip text based on the current state.
		 */
		private void updateToolTipText() {
			if (!isChecked()) {
				setToolTipText("Show inactive instrumentations");
			} else {
				setToolTipText("Hide inactive instrumentations");
			}
		}
	}

}
