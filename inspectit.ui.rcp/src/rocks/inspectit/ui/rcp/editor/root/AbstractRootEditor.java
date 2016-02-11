package info.novatec.inspectit.rcp.editor.root;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.SubViewFactory;
import info.novatec.inspectit.rcp.editor.composite.AbstractCompositeSubView;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.listener.StorageChangeListener;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * The abstract root editor is the base class of all editors (or more specific the views).
 * Currently, the only root editor existing is the {@link FormRootEditor}. An editor is used here,
 * and not a view, because more than one editor can be opened more easily. Plus the
 * {@link IEditorInput} interface makes it easy to define the input for the views.
 * <p>
 * If the same editor is already opened (thus the editorinput is the same as one that is already
 * opened), the view is switched to existing one.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractRootEditor extends EditorPart implements IRootEditor, IInputDefinitionProvider, CmrRepositoryChangeListener, StorageChangeListener {

	/**
	 * The inner class for the update timer which just calls the
	 * {@link AbstractRootEditor#refresh()} method in the {@link Display} thread.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class UpdateTimerTask extends TimerTask {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						doRefresh();
					} catch (Exception e) {
						InspectIT.getDefault().createErrorDialog("The update mechanism on the view failed!", e, -1);
					}
				}
			});
		}
	}

	/**
	 * The update timer used for the automatic update of the contained views.
	 */
	private Timer updateTimer;

	/**
	 * The {@link IPreferencePanel}.
	 */
	private IPreferencePanel preferencePanel;

	/**
	 * The contained subview.
	 */
	private ISubView subView;

	/**
	 * The active subview.
	 */
	private ISubView activeSubView;

	/**
	 * The selection change listener, initialized lazily; <code>null</code> if not yet created.
	 */
	private ISelectionChangedListener selectionChangedListener = null;

	/**
	 * The post selection changed listener.
	 */
	private ISelectionChangedListener postSelectionChangedListener = null;

	/**
	 * The selection object. Needed for comparison with the new one.
	 */
	private ISelection selection;

	/**
	 * Denotes if the active {@link ISubView} is currently maximized.
	 */
	private boolean isMaximizedMode = false;

	/**
	 * Resource manager.
	 */
	private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		subView.doRefresh();
	}

	/**
	 * Returns the input definition for this view.
	 * 
	 * @return The input definition.
	 */
	public InputDefinition getInputDefinition() {
		InputDefinition inputDefinition = (InputDefinition) getEditorInput().getAdapter(InputDefinition.class);
		Assert.isNotNull(inputDefinition);
		return inputDefinition;
	}

	/**
	 * Starts the update timer.
	 */
	protected void startUpdateTimer() {
		if (null == updateTimer) {
			updateTimer = new Timer();
			updateTimer.schedule(new UpdateTimerTask(), 0L, getInputDefinition().getUpdateRate());
		}
	}

	/**
	 * Stops the update timer.
	 */
	protected void stopUpdateTimer() {
		if (null != updateTimer) {
			updateTimer.cancel();
			updateTimer = null; // NOPMD
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSaveAs() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IEditorSite editorSite, IEditorInput editorInput) throws PartInitException {
		// check for valid input
		if (!(editorInput instanceof RootEditorInput)) {
			throw new PartInitException("Invalid Input: Must be RootEditorInput");
		}

		// set site and input
		setSite(editorSite);
		setInput(editorInput);
		setTitleImage(ImageFormatter.getOverlayedEditorImage(getInputDefinition().getEditorPropertiesData().getPartImage(), getInputDefinition().getRepositoryDefinition(), resourceManager));

		this.subView = SubViewFactory.createSubView(getInputDefinition().getId());
		this.subView.setRootEditor(this);
		this.subView.init();
		editorSite.setSelectionProvider(new MultiSubViewSelectionProvider(this));

		InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().addStorageChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void createPartControl(Composite parent) {
		setPartName(getInputDefinition().getEditorPropertiesData().getPartName());
		setTitleToolTip(getInputDefinition().getEditorPropertiesData().getPartTooltip());

		// fill the view with content.
		createView(parent);

		// start the update timer if it is requested.
		if (getInputDefinition().isAutomaticUpdate()) {
			startUpdateTimer();
		} else {
			// do an update one time
			Timer timer = new Timer();
			timer.schedule(new UpdateTimerTask(), 0L);
		}

		if (null != preferencePanel) {
			PreferenceEventCallback callback = new PreferenceEventCallback() {
				/**
				 * {@inheritDoc}
				 */
				public void eventFired(PreferenceEvent preferenceEvent) {
					if (PreferenceId.LIVEMODE.equals(preferenceEvent.getPreferenceId())) {
						boolean isLiveMode = false;
						if (null != updateTimer) {
							stopUpdateTimer();
							isLiveMode = true;
						}
						if (preferenceEvent.getPreferenceMap().containsKey(LiveMode.REFRESH_RATE)) {
							long refresh = (Long) preferenceEvent.getPreferenceMap().get(LiveMode.REFRESH_RATE);
							getInputDefinition().setUpdateRate(refresh);
						}

						if (preferenceEvent.getPreferenceMap().containsKey(LiveMode.BUTTON_LIVE_ID)) {
							isLiveMode = (Boolean) preferenceEvent.getPreferenceMap().get(LiveMode.BUTTON_LIVE_ID);
						}

						if (isLiveMode) {
							startUpdateTimer();
						} else {
							stopUpdateTimer();
						}
					} else if (PreferenceId.UPDATE.equals(preferenceEvent.getPreferenceId())) {
						doRefresh();
					}
					getSubView().preferenceEventFired(preferenceEvent);
				}
			};
			preferencePanel.registerCallback(callback);
		}
	}

	/**
	 * Create the view in the subclasses.
	 * 
	 * @param parent
	 *            The parent composite.
	 */
	protected abstract void createView(Composite parent);

	/**
	 * Sets the preference panel. Throws {@link NullPointerException} if
	 * <code>preferencePanel</code> is set to <code>null</code>.
	 * 
	 * @param preferencePanel
	 *            The preference panel.
	 */
	protected void setPreferencePanel(IPreferencePanel preferencePanel) {
		Assert.isNotNull(preferencePanel);

		this.preferencePanel = preferencePanel;
	}

	/**
	 * {@inheritDoc}
	 */
	public IPreferencePanel getPreferencePanel() {
		return preferencePanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		// can never be true
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// can never be true, nothing to save.
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		if (null != getActiveSubView() && null != getActiveSubView().getControl()) {
			getActiveSubView().getControl().setFocus();
		} else if (null != subView && null != subView.getControl()) {
			subView.getControl().setFocus();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ISubView getSubView() {
		return subView;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setActiveSubView(ISubView subView) {
		Assert.isNotNull(subView);

		if (!ObjectUtils.equals(activeSubView, subView)) {
			this.activeSubView = subView;

			ISelectionProvider selectionProvider = activeSubView.getSelectionProvider();
			if (selectionProvider != null) {
				ISelectionProvider outerProvider = getSite().getSelectionProvider();
				if (outerProvider instanceof MultiSubViewSelectionProvider) {
					ISelection newSelection = selectionProvider.getSelection();
					MultiSubViewSelectionProvider provider = (MultiSubViewSelectionProvider) outerProvider;

					if (ObjectUtils.equals(selection, newSelection)) {
						// The selection object didn't change, but the selection area did, thus we
						// have to simulate a little bit. Problem is due to TreeSelection#equals
						// passing equal comparison to the parent class (which means that a
						// treeselection and a structuredselection are equal if they refer to the
						// same selection). See bug at
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=135837
						SelectionChangedEvent event = new SelectionChangedEvent(selectionProvider, StructuredSelection.EMPTY);
						provider.fireSelectionChanged(event);
						provider.firePostSelectionChanged(event);
					}
					// now the real event
					selection = newSelection;
					SelectionChangedEvent event = new SelectionChangedEvent(selectionProvider, selection);
					provider.fireSelectionChanged(event);
					provider.firePostSelectionChanged(event);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ISubView getActiveSubView() {
		return activeSubView;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		subView.setDataInput(data);
	}

	/**
	 * Returns the selection changed listener which listens to the nested editor's selection
	 * changes, and calls <code>handleSelectionChanged</code> .
	 * 
	 * @return the selection changed listener
	 */
	public ISelectionChangedListener getSelectionChangedListener() {
		if (selectionChangedListener == null) {
			selectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					AbstractRootEditor.this.handleSelectionChanged(event);
				}
			};
		}
		return selectionChangedListener;
	}

	/**
	 * Returns the post selection change listener which listens to the nested editor's selection
	 * changes.
	 * 
	 * @return the post selection change listener.
	 */
	public ISelectionChangedListener getPostSelectionChangedListener() {
		if (postSelectionChangedListener == null) {
			postSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					AbstractRootEditor.this.handlePostSelectionChanged(event);
				}
			};
		}
		return postSelectionChangedListener;
	}

	/**
	 * Handles a selection changed event from the nested sub view. The default implementation gets
	 * the selection provider from the site, and calls <code>fireSelectionChanged</code> on it (only
	 * if it is an instance of <code>MultiSubViewSelectionProvider</code>), passing a new event
	 * object.
	 * 
	 * @param event
	 *            The event.
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
		ISelectionProvider provider = getSite().getSelectionProvider();
		if (provider instanceof MultiSubViewSelectionProvider) {
			SelectionChangedEvent newEvent = new SelectionChangedEvent(provider, event.getSelection());
			MultiSubViewSelectionProvider prov = (MultiSubViewSelectionProvider) provider;

			prov.fireSelectionChanged(newEvent);
		}
	}

	/**
	 * Handles a post selection changed even from the active sub view.
	 * 
	 * @param event
	 *            The event
	 */
	protected void handlePostSelectionChanged(SelectionChangedEvent event) {
		ISelectionProvider provider = getSite().getSelectionProvider();
		if (provider instanceof MultiSubViewSelectionProvider) {
			SelectionChangedEvent newEvent = new SelectionChangedEvent(provider, event.getSelection());
			MultiSubViewSelectionProvider prov = (MultiSubViewSelectionProvider) provider;

			prov.firePostSelectionChanged(newEvent);
		}
	}

	/**
	 * Sets the current selection object.
	 * 
	 * @param selection
	 *            the selection object to set.
	 */
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	/**
	 * Returns if the currently active sub-view can be maximized. Note that only if a
	 * {@link AbstractCompositeSubView} is a main sub-view of the editor this action is possible. In
	 * addition to that, number of sub-view has to be larger of 1 and currently no view has to be
	 * maximized.
	 * <p>
	 * If {@link #activeSubView} is null, this method will still return true if all the conditions
	 * above are fulfilled. Note that in this case, maximize action will maximize the first view.
	 * 
	 * @return True if the maximize active sub-view action can be executed.
	 */
	public boolean canMaximizeActiveSubView() {
		if (isMaximizedMode) {
			return false;
		} else {
			if (subView instanceof AbstractCompositeSubView) {
				AbstractCompositeSubView compositeSubView = (AbstractCompositeSubView) subView;
				List<ISubView> allViews = compositeSubView.getSubViews();
				if (null != allViews && allViews.size() > 1) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Returns if the active sub view is currently maximized and thus can be minimized.
	 * 
	 * @return Returns if the active sub view is currently maximized and thus can be minimized.
	 */
	public boolean canMinimizeActiveSubView() {
		return isMaximizedMode;
	}

	/**
	 * Maximizes the active sub-view if that is possible.
	 */
	public void maximizeActiveSubView() {
		if (canMaximizeActiveSubView()) {
			maximizeSubView((AbstractCompositeSubView) subView, activeSubView);
			isMaximizedMode = true;
			ISelectionProvider selectionProvider = getSite().getSelectionProvider();
			selectionProvider.setSelection(StructuredSelection.EMPTY);
		}
	}

	/**
	 * Recursively maximizes all needed sub-views until the active one is maximized.
	 * 
	 * @param compositeSubView
	 *            {@link AbstractCompositeSubView} to start from.
	 * @param view
	 *            View to maximize.
	 * @return True if the composite sub view did the maximization.
	 */
	private boolean maximizeSubView(AbstractCompositeSubView compositeSubView, ISubView view) {
		if (null == view) {
			compositeSubView.maximizeSubView(null);
			return true;
		}

		if (compositeSubView.getSubViews().contains(view)) {
			compositeSubView.maximizeSubView(view);
			return true;
		} else {
			for (ISubView viewInComposite : compositeSubView.getSubViews()) {
				if (viewInComposite instanceof AbstractCompositeSubView) {
					boolean maximized = maximizeSubView((AbstractCompositeSubView) viewInComposite, view);
					if (maximized) {
						compositeSubView.maximizeSubView(viewInComposite);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Minimizes the active sub-view if that is possible.
	 */
	public void minimizeActiveSubView() {
		if (canMinimizeActiveSubView()) {
			restoreMaximization((AbstractCompositeSubView) subView);
			isMaximizedMode = false;
			ISelectionProvider selectionProvider = getSite().getSelectionProvider();
			selectionProvider.setSelection(StructuredSelection.EMPTY);
		}
	}

	/**
	 * Recursively restores the maximized mode.
	 * 
	 * @param compositeSubView
	 *            {@link AbstractCompositeSubView} to start from.
	 */
	private void restoreMaximization(AbstractCompositeSubView compositeSubView) {
		compositeSubView.restoreMaximization();
		for (ISubView viewInComposite : compositeSubView.getSubViews()) {
			if (viewInComposite instanceof AbstractCompositeSubView) {
				restoreMaximization((AbstractCompositeSubView) viewInComposite);
			}
		}
	}

	/**
	 * Returns if the editor had the active sub-view maximized.
	 * 
	 * @return Returns if the editor had the active sub-view maximized.
	 */
	public boolean isActiveViewMaximized() {
		return isMaximizedMode;
	}

	/**
	 * Updates the name of the editor.
	 * 
	 * @param name
	 *            New name.
	 */
	public void updateEditorName(String name) {
		if (StringUtils.isNotEmpty(name)) {
			setPartName(name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
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
		if (ObjectUtils.equals(cmrRepositoryDefinition, getInputDefinition().getRepositoryDefinition())) {
			close();
		} else if (getInputDefinition().getRepositoryDefinition() instanceof StorageRepositoryDefinition) {
			// close also if storage is displayed from the repository that is removed
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) getInputDefinition().getRepositoryDefinition();
			if (ObjectUtils.equals(cmrRepositoryDefinition, storageRepositoryDefinition.getCmrRepositoryDefinition()) && !storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded()) {
				close();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
		if (ObjectUtils.equals(cmrRepositoryDefinition, getInputDefinition().getRepositoryDefinition())) {
			if (agent.getId() == getInputDefinition().getIdDefinition().getPlatformId()) {
				close();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageDataUpdated(IStorageData storageData) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageRemotelyDeleted(IStorageData storageData) {
		RepositoryDefinition repositoryDefinition = getInputDefinition().getRepositoryDefinition();
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			LocalStorageData localStorageData = ((StorageRepositoryDefinition) repositoryDefinition).getLocalStorageData();
			if (!localStorageData.isFullyDownloaded() && ObjectUtils.equals(localStorageData.getId(), storageData.getId())) {
				close();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageLocallyDeleted(IStorageData storageData) {
		RepositoryDefinition repositoryDefinition = getInputDefinition().getRepositoryDefinition();
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			LocalStorageData localStorageData = ((StorageRepositoryDefinition) repositoryDefinition).getLocalStorageData();
			if (ObjectUtils.equals(localStorageData.getId(), storageData.getId())) {
				if (!InspectIT.getDefault().getInspectITStorageManager().getMountedAvailableStorages().contains(storageData)) {
					// close only if the remote one is also not available
					close();
				}
			}
		}
	}

	/**
	 * Closes the editor.
	 */
	protected void close() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				getEditorSite().getPage().closeEditor(AbstractRootEditor.this, false);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getCmrRepositoryManager().removeCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().removeStorageChangeListener(this);

		// stop the timer if it is active
		stopUpdateTimer();

		// dispose the local resource manager
		resourceManager.dispose();

		super.dispose();

		// dispose the preference panel
		if (null != preferencePanel) {
			preferencePanel.dispose();
		}

		if (null != subView) {
			subView.dispose();
		}
	}
}
