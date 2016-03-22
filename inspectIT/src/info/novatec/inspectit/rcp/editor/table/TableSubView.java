package info.novatec.inspectit.rcp.editor.table;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.FormRootEditor;
import info.novatec.inspectit.rcp.editor.root.SubViewClassificationController.SubViewClassification;
import info.novatec.inspectit.rcp.editor.search.ISearchExecutor;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchCriteria;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchResult;
import info.novatec.inspectit.rcp.editor.search.helper.TableViewerSearchHelper;
import info.novatec.inspectit.rcp.editor.table.input.TableInputController;
import info.novatec.inspectit.rcp.editor.tooltip.ColumnAwareToolTipSupport;
import info.novatec.inspectit.rcp.editor.tooltip.IColumnToolTipProvider;
import info.novatec.inspectit.rcp.editor.viewers.CheckedDelegatingIndexLabelProvider;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.handlers.ShowHideColumnsHandler;
import info.novatec.inspectit.rcp.menu.ShowHideMenuManager;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Sub-view which is used to create a table.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TableSubView extends AbstractSubView implements ISearchExecutor {

	/**
	 * The referenced input controller.
	 */
	private final TableInputController tableInputController;

	/**
	 * The created table viewer.
	 */
	private TableViewer tableViewer;

	/**
	 * Defines if a job is currently already executing.
	 */
	private volatile boolean jobInSchedule = false;

	/**
	 * {@link TableViewerSearchHelper}.
	 */
	private TableViewerSearchHelper tableViewerSearchHelper;

	/**
	 * Default constructor which needs a tree input controller to create all the content etc.
	 * 
	 * @param tableInputController
	 *            The table input controller.
	 */
	public TableSubView(TableInputController tableInputController) {
		Assert.isNotNull(tableInputController);

		this.tableInputController = tableInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		tableInputController.setInputDefinition(getRootEditor().getInputDefinition());
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		// the style can not be SWT.VIRTUAL when the SWT.CHECK style is used
		// the check of the elements won't work because of the virtual loading
		int style = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
		if (tableInputController.isCheckStyle()) {
			style |= SWT.CHECK;
		} else {
			style |= SWT.VIRTUAL;
		}
		final Table table = toolkit.createTable(parent, style);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);

		if (tableInputController.isCheckStyle()) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(false);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setWidth(30);
			viewerColumn.getColumn().setText("Selected");
		}

		tableInputController.createColumns(tableViewer);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(tableInputController.getContentProvider());
		IBaseLabelProvider labelProvider = tableInputController.getLabelProvider();
		if (tableInputController.isCheckStyle() && labelProvider instanceof StyledCellIndexLabelProvider) {
			labelProvider = new CheckedDelegatingIndexLabelProvider((StyledCellIndexLabelProvider) labelProvider);
		}
		tableViewer.setLabelProvider(labelProvider);
		if (labelProvider instanceof IColumnToolTipProvider) {
			ColumnAwareToolTipSupport.enableFor(tableViewer);
		}
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				tableInputController.doubleClick(event);
			}
		});
		tableViewer.setComparator(tableInputController.getComparator());
		if (null != tableViewer.getComparator()) {
			TableColumn[] tableColumns = tableViewer.getTable().getColumns();
			for (TableColumn tableColumn : tableColumns) {
				tableColumn.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.refresh();
					}
				});
			}
		}

		// add show hide columns support
		MenuManager headerMenuManager = new ShowHideMenuManager(tableViewer, tableInputController.getClass());
		headerMenuManager.setRemoveAllWhenShown(false);

		// normal selection menu manager
		MenuManager selectionMenuManager = new MenuManager();
		selectionMenuManager.setRemoveAllWhenShown(true);
		getRootEditor().getSite().registerContextMenu(FormRootEditor.ID + ".tablesubview", selectionMenuManager, tableViewer);

		final Menu selectionMenu = selectionMenuManager.createContextMenu(table);
		final Menu headerMenu = headerMenuManager.createContextMenu(table);

		table.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point pt = Display.getDefault().map(null, table, new Point(event.x, event.y));
				Rectangle clientArea = table.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + table.getHeaderHeight());
				if (header) {
					table.setMenu(headerMenu);
				} else {
					table.setMenu(selectionMenu);
				}
			}
		});

		/**
		 * IMPORTANT: Only the menu set in the setMenu() will be disposed automatically.
		 */
		table.addListener(SWT.Dispose, new Listener() {
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

		Object input = tableInputController.getTableInput();
		tableViewer.setInput(input);

		ControlAdapter columnResizeListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (e.widget instanceof TableColumn) {
					TableColumn column = (TableColumn) e.widget;
					if (column.getWidth() > 0) {
						ShowHideColumnsHandler.registerNewColumnWidth(tableInputController.getClass(), column.getText(), column.getWidth());
					}
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {
				ShowHideColumnsHandler.setColumnOrder(tableInputController.getClass(), tableViewer.getTable().getColumnOrder());
			}
		};

		for (TableColumn column : table.getColumns()) {
			if (tableInputController.canAlterColumnWidth(column)) {
				Integer rememberedWidth = ShowHideColumnsHandler.getRememberedColumnWidth(tableInputController.getClass(), column.getText());
				boolean isColumnHidden = ShowHideColumnsHandler.isColumnHidden(tableInputController.getClass(), column.getText());

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
		int[] columnOrder = ShowHideColumnsHandler.getColumnOrder(tableInputController.getClass());
		if (null != columnOrder && columnOrder.length == table.getColumns().length) {
			table.setColumnOrder(columnOrder);
		} else if (null != columnOrder) {
			// if the order exists, but length is not same, then update with the default order
			ShowHideColumnsHandler.setColumnOrder(tableInputController.getClass(), table.getColumnOrder());
		}

		tableViewerSearchHelper = new TableViewerSearchHelper(tableViewer, tableInputController, getRootEditor().getInputDefinition().getRepositoryDefinition());
		// add listener for the check box style if active
		if (tableInputController.isCheckStyle()) {
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.detail == SWT.CHECK) {
						if (e.item instanceof TableItem) {
							TableItem item = (TableItem) e.item;
							tableInputController.objectChecked(item.getData(), item.getChecked());
						}
					}
				}
			});
		}
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
						tableInputController.doRefresh(monitor, getRootEditor());
						SafeExecutor.asyncExec(new Runnable() {
							public void run() {
								if (checkDisposed()) {
									return;
								}

								// refresh should only influence the master sub views
								if (tableInputController.getSubViewClassification() == SubViewClassification.MASTER) {
									Object input = tableInputController.getTableInput();
									tableViewer.setInput(input);
									if (tableViewer.getTable().isVisible()) {
										tableViewer.refresh();

										// if we use check style, set elements to the initial state
										if (tableInputController.isCheckStyle()) {
											for (TableItem tableItem : tableViewer.getTable().getItems()) {
												tableItem.setChecked(tableInputController.areItemsInitiallyChecked());
											}
										}
									}

								}
							}
						}, tableViewer.getTable());
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

		if (tableInputController.canOpenInput(data)) {
			tableViewer.setInput(data);
			tableViewer.refresh(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return tableViewer.getControl();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return tableViewer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return tableInputController.getPreferenceIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (checkDisposed()) {
			return;
		}

		if (PreferenceId.ITEMCOUNT.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			int limit = (Integer) preferenceMap.get(PreferenceId.ItemCount.COUNT_SELECTION_ID);
			tableInputController.setLimit(limit);
			this.doRefresh();
		}

		// we are suspending the table redraw while controller handles the event to disable any
		// updates on the table by controller
		tableViewer.getTable().setRedraw(false);
		tableInputController.preferenceEventFired(preferenceEvent);
		tableViewer.getTable().setRedraw(true);

		switch (preferenceEvent.getPreferenceId()) {
		case CLEAR_BUFFER:
			if (tableInputController.getPreferenceIds().contains(PreferenceId.CLEAR_BUFFER)) {
				tableViewer.refresh();
			}
			break;
		case TIME_RESOLUTION:
			if (tableInputController.getPreferenceIds().contains(PreferenceId.TIME_RESOLUTION)) {
				tableViewer.refresh();
			}
			break;
		case INVOCATION_SUBVIEW_MODE:
			if (tableInputController.getPreferenceIds().contains(PreferenceId.INVOCATION_SUBVIEW_MODE)) {
				tableViewer.refresh();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns the table input controller.
	 * 
	 * @return The table input controller.
	 */
	public TableInputController getTableInputController() {
		return tableInputController;
	}

	/**
	 * Return the names of all columns in the table. Not visible columns names will also be
	 * included. The order of the names will be same to the initial table column order, thus not
	 * reflecting the current state of the table if the columns were moved.
	 * 
	 * @return List of column names.
	 */
	public List<String> getColumnNames() {
		List<String> names = new ArrayList<String>();
		for (TableColumn column : tableViewer.getTable().getColumns()) {
			names.add(column.getText());
		}
		return names;
	}

	/**
	 * 
	 * @return The list of integers representing the column order in the table. Note that only
	 *         columns that are currently visible will be included in the list.
	 * @see Table#getColumnOrder()
	 */
	public List<Integer> getColumnOrder() {
		int[] order = tableViewer.getTable().getColumnOrder();
		List<Integer> orderWithoutHidden = new ArrayList<Integer>();
		for (int index : order) {
			if (tableViewer.getTable().getColumns()[index].getWidth() > 0) {
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
		if (Objects.equals(inputControllerClass, tableInputController.getClass())) {
			return this;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult executeSearch(SearchCriteria searchCriteria) {
		return tableViewerSearchHelper.executeSearch(searchCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult next() {
		return tableViewerSearchHelper.next();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult previous() {
		return tableViewerSearchHelper.previous();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearSearch() {
		tableViewerSearchHelper.clearSearch();
	}

	/**
	 * Returns true if the table in the sub-view is disposed. False otherwise.
	 * 
	 * @return Returns true if the table in the sub-view is disposed. False otherwise.
	 */
	private boolean checkDisposed() {
		return tableViewer.getTable().isDisposed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		tableInputController.dispose();
	}

}
