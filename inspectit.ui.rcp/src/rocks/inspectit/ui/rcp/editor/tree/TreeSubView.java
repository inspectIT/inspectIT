package info.novatec.inspectit.rcp.editor.tree;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.FormRootEditor;
import info.novatec.inspectit.rcp.editor.root.SubViewClassificationController.SubViewClassification;
import info.novatec.inspectit.rcp.editor.search.ISearchExecutor;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchCriteria;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchResult;
import info.novatec.inspectit.rcp.editor.search.helper.DeferredTreeViewerSearchHelper;
import info.novatec.inspectit.rcp.editor.tooltip.ColumnAwareToolTipSupport;
import info.novatec.inspectit.rcp.editor.tooltip.IColumnToolTipProvider;
import info.novatec.inspectit.rcp.editor.tree.input.TreeInputController;
import info.novatec.inspectit.rcp.handlers.ShowHideColumnsHandler;
import info.novatec.inspectit.rcp.menu.ShowHideMenuManager;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Sub-view which is used to create a tree.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TreeSubView extends AbstractSubView implements ISearchExecutor {

	/**
	 * The referenced input controller.
	 */
	private final TreeInputController treeInputController;

	/**
	 * The created tree viewer.
	 */
	private TreeViewer treeViewer;

	/**
	 * Defines if a job is currently already executing.
	 */
	private volatile boolean jobInSchedule = false;

	/**
	 * {@link DeferredTreeViewerSearchHelper}.
	 */
	private DeferredTreeViewerSearchHelper searchHelper;

	/**
	 * Default constructor which needs a tree input controller to create all the content etc.
	 * 
	 * @param treeInputController
	 *            The tree input controller.
	 */
	public TreeSubView(TreeInputController treeInputController) {
		Assert.isNotNull(treeInputController);

		this.treeInputController = treeInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		treeInputController.setInputDefinition(getRootEditor().getInputDefinition());
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		final Tree tree = toolkit.createTree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setHeaderVisible(true);

		treeViewer = new DeferredTreeViewer(tree);
		treeInputController.createColumns(treeViewer);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(treeInputController.getContentProvider());
		IBaseLabelProvider labelProvider = treeInputController.getLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		if (labelProvider instanceof IColumnToolTipProvider) {
			ColumnAwareToolTipSupport.enableFor(treeViewer);
		}
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				treeInputController.doubleClick(event);
				TreeSelection selection = (TreeSelection) event.getSelection();
				TreePath path = selection.getPaths()[0];
				if (null != path) {
					boolean expanded = treeViewer.getExpandedState(path);
					if (expanded) {
						treeViewer.collapseToLevel(path, 1);
					} else {
						treeViewer.expandToLevel(path, 1);
					}
				}
			}
		});
		treeViewer.setComparator(treeInputController.getComparator());
		if (null != treeViewer.getComparator()) {
			TreeColumn[] treeColumns = treeViewer.getTree().getColumns();
			for (TreeColumn treeColumn : treeColumns) {
				treeColumn.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						treeViewer.refresh();
					}
				});
			}
		}
		if (ArrayUtils.isNotEmpty(treeInputController.getFilters())) {
			treeViewer.setFilters(treeInputController.getFilters());
		}

		// add show hide columns support
		MenuManager headerMenuManager = new ShowHideMenuManager(treeViewer, treeInputController.getClass());
		headerMenuManager.setRemoveAllWhenShown(false);

		// normal selection menu manager
		MenuManager selectionMenuManager = new MenuManager();
		selectionMenuManager.setRemoveAllWhenShown(true);
		getRootEditor().getSite().registerContextMenu(FormRootEditor.ID + ".treesubview", selectionMenuManager, treeViewer);

		final Menu selectionMenu = selectionMenuManager.createContextMenu(tree);
		final Menu headerMenu = headerMenuManager.createContextMenu(tree);

		tree.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point pt = Display.getDefault().map(null, tree, new Point(event.x, event.y));
				Rectangle clientArea = tree.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + tree.getHeaderHeight());
				if (header) {
					tree.setMenu(headerMenu);
				} else {
					tree.setMenu(selectionMenu);
				}
			}
		});

		/**
		 * IMPORTANT: Only the menu set in the setMenu() will be disposed automatically.
		 */
		tree.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!headerMenu.isDisposed()) {
					headerMenu.dispose();
				}
				if (!selectionMenu.isDisposed()) {
					selectionMenu.dispose();
				}
			}
		});

		Object input = treeInputController.getTreeInput();
		treeViewer.setInput(input);

		ControlAdapter columnResizeListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (e.widget instanceof TreeColumn) {
					TreeColumn column = (TreeColumn) e.widget;
					if (column.getWidth() > 0) {
						ShowHideColumnsHandler.registerNewColumnWidth(treeInputController.getClass(), column.getText(), column.getWidth());
					}
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {
				ShowHideColumnsHandler.setColumnOrder(treeInputController.getClass(), treeViewer.getTree().getColumnOrder());
			}
		};

		for (TreeColumn column : tree.getColumns()) {
			if (treeInputController.canAlterColumnWidth(column)) {
				Integer rememberedWidth = ShowHideColumnsHandler.getRememberedColumnWidth(treeInputController.getClass(), column.getText());
				boolean isColumnHidden = ShowHideColumnsHandler.isColumnHidden(treeInputController.getClass(), column.getText());

				if (rememberedWidth != null && !isColumnHidden) {
					column.setWidth(rememberedWidth.intValue());
					column.setResizable(true);
				} else if (isColumnHidden) {
					column.setWidth(0);
					column.setResizable(false);
				}
			}

			column.addControlListener(columnResizeListener);
		}

		// update the order of columns if the order was defined for the class, and no new columns
		// were added
		int[] columnOrder = ShowHideColumnsHandler.getColumnOrder(treeInputController.getClass());
		if (null != columnOrder && columnOrder.length == tree.getColumns().length) {
			tree.setColumnOrder(columnOrder);
		} else if (null != columnOrder) {
			// if the order exists, but length is not same, then update with the default order
			ShowHideColumnsHandler.setColumnOrder(treeInputController.getClass(), tree.getColumnOrder());
		}

		// create search helper
		searchHelper = new DeferredTreeViewerSearchHelper((DeferredTreeViewer) treeViewer, treeInputController, getRootEditor().getInputDefinition().getRepositoryDefinition());
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		if (!jobInSchedule) {
			jobInSchedule = true;

			Job job = new Job(getDataLoadingJobName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						treeInputController.doRefresh(monitor, getRootEditor());
						SafeExecutor.asyncExec(new Runnable() {
							public void run() {
								if (checkDisposed()) {
									return;
								}

								// refresh should only influence the master sub views
								if (treeInputController.getSubViewClassification() == SubViewClassification.MASTER) {
									Object input = treeInputController.getTreeInput();
									treeViewer.setInput(input);
									if (treeViewer.getTree().isVisible()) {
										treeViewer.refresh();
										treeViewer.expandToLevel(treeInputController.getExpandLevel());
									}
								}
							}
						}, treeViewer.getTree());
					} catch (Throwable throwable) { // NOPMD
						throw new RuntimeException("Unknown exception occurred trying to refresh the view.", throwable);
					} finally {
						jobInSchedule = false;
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		if (checkDisposed()) {
			return;
		}

		if (treeInputController.canOpenInput(data)) {
			treeViewer.setInput(data);
			treeViewer.expandToLevel(treeInputController.getExpandLevel());
			// i will comment this out because tree viewer is not refreshing if it is not visible,
			// meaning when i clear buffer only tree views that are visible are cleared.
			// if (treeViewer.getControl().isVisible()) {
			treeViewer.refresh();
			// }
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return treeViewer.getControl();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return treeViewer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return treeInputController.getPreferenceIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (checkDisposed()) {
			return;
		}

		treeInputController.preferenceEventFired(preferenceEvent);
		switch (preferenceEvent.getPreferenceId()) {
		case FILTERDATATYPE:
		case INVOCFILTEREXCLUSIVETIME:
		case INVOCFILTERTOTALTIME:
			// we have to re-apply the filter if there is one
			if (ArrayUtils.isNotEmpty(treeInputController.getFilters())) {
				treeViewer.setFilters(treeInputController.getFilters());
			}
			break;
		case CLEAR_BUFFER:
			if (treeInputController.getPreferenceIds().contains(PreferenceId.CLEAR_BUFFER)) {
				treeViewer.refresh();
				treeViewer.expandToLevel(treeInputController.getExpandLevel());
			}
			break;
		case TIME_RESOLUTION:
			if (treeInputController.getPreferenceIds().contains(PreferenceId.TIME_RESOLUTION)) {
				treeViewer.refresh();
				treeViewer.expandToLevel(treeInputController.getExpandLevel());
			}
			break;
		case INVOCATION_SUBVIEW_MODE:
			if (treeInputController.getPreferenceIds().contains(PreferenceId.INVOCATION_SUBVIEW_MODE)) {
				treeViewer.refresh();
				treeViewer.expandToLevel(treeInputController.getExpandLevel());
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns the tree viewer.
	 * 
	 * @return The tree viewer.
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * Returns the tree input controller.
	 * 
	 * @return The tree input controller.
	 */
	public TreeInputController getTreeInputController() {
		return treeInputController;
	}

	/**
	 * Return the names of all columns in the tree. Not visible columns names will also be included.
	 * The order of the names will be same to the initial tree column order, thus not reflecting the
	 * current state of the table if the columns were moved.
	 * 
	 * @return List of column names.
	 */
	public List<String> getColumnNames() {
		List<String> names = new ArrayList<String>();
		for (TreeColumn column : treeViewer.getTree().getColumns()) {
			names.add(column.getText());
		}
		return names;
	}

	/**
	 * 
	 * @return The list of integers representing the column order in the tree. Note that only
	 *         columns that are currently visible will be included in the list.
	 * @see org.eclipse.swt.widgets.Table#getColumnOrder()
	 */
	public List<Integer> getColumnOrder() {
		int[] order = treeViewer.getTree().getColumnOrder();
		List<Integer> orderWithoutHidden = new ArrayList<Integer>();
		for (int index : order) {
			if (treeViewer.getTree().getColumns()[index].getWidth() > 0) {
				orderWithoutHidden.add(index);
			}
		}
		return orderWithoutHidden;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISubView getSubViewWithInputController(Class<?> inputControllerClass) {
		if (Objects.equals(inputControllerClass, treeInputController.getClass())) {
			return this;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult executeSearch(SearchCriteria searchCriteria) {
		return searchHelper.executeSearch(searchCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult next() {
		return searchHelper.next();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult previous() {
		return searchHelper.previous();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearSearch() {
		searchHelper.clearSearch();
	}

	/**
	 * Returns true if the tree in the sub-view is disposed. False otherwise.
	 * 
	 * @return Returns true if the tree in the sub-view is disposed. False otherwise.
	 */
	private boolean checkDisposed() {
		return treeViewer.getTree().isDisposed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		treeInputController.dispose();
	}

}
