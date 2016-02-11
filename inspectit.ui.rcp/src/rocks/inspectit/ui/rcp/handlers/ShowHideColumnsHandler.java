package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This class is a handler for show/hide of columns, but also a cache for saving the state of the
 * columns.
 * 
 * @author Ivan Senic
 * 
 */
public class ShowHideColumnsHandler extends AbstractHandler {

	/**
	 * Command ID.
	 */
	public static final String COMMAND_ID = "info.novatec.inspectit.rcp.commands.showHideColumn";

	/**
	 * Column parameter.
	 */
	public static final String COLUMN_PARAM = "info.novatec.inspectit.rcp.commands.showHideColumn.Column";

	/**
	 * Visible parameter.
	 */
	public static final String VISIBLE_PARAM = "info.novatec.inspectit.rcp.commands.showHideColumn.Visible";

	/**
	 * Controller class parameter.
	 */
	public static final String CONTROLLER_CLASS_PARAM = "info.novatec.inspectit.rcp.commands.showHideColumn.ControllerClass";

	/**
	 * Map for saving columns size.
	 */
	private static Map<Integer, Integer> columnSizeCache;

	/**
	 * Set for saving columns visibility. All columns that are in this cache are not visible.
	 */
	private static Set<Integer> hiddenColumnsCache;

	/**
	 * Column order map.
	 */
	private static Map<Integer, int[]> columnOrderCache;

	static {
		startUp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the input definition out of the context
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		Item column = (Item) context.getVariable(COLUMN_PARAM);
		Boolean visible = (Boolean) context.getVariable(VISIBLE_PARAM);
		Class<?> inputControllerClass = (Class<?>) context.getVariable(CONTROLLER_CLASS_PARAM);

		showHideColumn(column, column.getText(), visible.booleanValue(), inputControllerClass);
		return null;
	}

	/**
	 * Shows or hides the {@link TreeColumn} or {@link TableColumn}.
	 * 
	 * @param column
	 *            Column.
	 * @param columnName
	 *            name of the column.
	 * @param showColumn
	 *            Should column be shown or not.
	 * @param controllerClass
	 *            Controller class.
	 */
	private void showHideColumn(Item column, String columnName, boolean showColumn, Class<?> controllerClass) {
		int columnHash = getColumnHash(controllerClass, columnName);
		if (showColumn) {
			// update cache data
			Integer width = columnSizeCache.get(columnHash);
			hiddenColumnsCache.remove(columnHash);

			// change appearance
			if (width != null) {
				setColumnWidth(column, width.intValue());
			} else {
				setColumnWidth(column, 100);
			}
			setColumnResizable(column, true);
		} else {
			// update cache data
			int width = getColumnWidth(column);
			hiddenColumnsCache.add(columnHash);
			columnSizeCache.put(columnHash, width);

			// change appearance
			setColumnWidth(column, 0);
			setColumnResizable(column, false);
		}
	}

	/**
	 * Sets the with of {@link TreeColumn} or {@link TableColumn}.
	 * 
	 * @param column
	 *            Column
	 * @param width
	 *            Width
	 */
	private void setColumnWidth(Item column, int width) {
		if (column instanceof TableColumn) {
			((TableColumn) column).setWidth(width);
		} else if (column instanceof TreeColumn) {
			((TreeColumn) column).setWidth(width);
		}
	}

	/**
	 * Gets the width of {@link TreeColumn} or {@link TableColumn}.
	 * 
	 * @param column
	 *            Column.
	 * @return Width of column, or -1 if provided {@link Item} object is not of type
	 *         {@link TreeColumn} or {@link TableColumn}.
	 */
	private int getColumnWidth(Item column) {
		if (column instanceof TableColumn) {
			return ((TableColumn) column).getWidth();
		} else if (column instanceof TreeColumn) {
			return ((TreeColumn) column).getWidth();
		}
		return -1;
	}

	/**
	 * Sets the {@link TreeColumn} or {@link TableColumn} resizable.
	 * 
	 * @param column
	 *            Column
	 * @param resizable
	 *            Resizable or not
	 */
	private void setColumnResizable(Item column, boolean resizable) {
		if (column instanceof TableColumn) {
			((TableColumn) column).setResizable(resizable);
		} else if (column instanceof TreeColumn) {
			((TreeColumn) column).setResizable(resizable);
		}
	}

	/**
	 * Returns if the cache has any knowledge of the column's width.
	 * 
	 * @param controllerClass
	 *            {@link info.novatec.inspectit.rcp.editor.table.input.TableInputController} class
	 *            where this column is defined.
	 * @param columnName
	 *            Column name.
	 * @return Size of columns width or <code>null</code> if it is unknown.
	 */
	public static Integer getRememberedColumnWidth(Class<?> controllerClass, String columnName) {
		int hash = getColumnHash(controllerClass, columnName);
		return columnSizeCache.get(hash);
	}

	/**
	 * Returns if the cache has any knowledge if the column is hidden.
	 * 
	 * @param controllerClass
	 *            {@link info.novatec.inspectit.rcp.editor.table.input.TableInputController} class
	 *            where this column is defined.
	 * @param columnName
	 *            Column name.
	 * @return True if column should be hidden.
	 */
	public static boolean isColumnHidden(Class<?> controllerClass, String columnName) {
		return hiddenColumnsCache.contains(getColumnHash(controllerClass, columnName));
	}

	/**
	 * Saves the column order for the controller class.
	 * 
	 * @param controllerClass
	 *            Controller class.
	 * @param order
	 *            Array that describes the order of the columns.
	 */
	public static void setColumnOrder(Class<?> controllerClass, int[] order) {
		Integer key = Integer.valueOf(controllerClass.getName().hashCode());
		columnOrderCache.remove(key);
		columnOrderCache.put(key, order);
	}

	/**
	 * Gets the column order for the controller class.
	 * 
	 * @param controllerClass
	 *            Controller class.
	 * @return Array that describes the order of the columns or null if the order was never saved
	 *         for the controller class.
	 */
	public static int[] getColumnOrder(Class<?> controllerClass) {
		Integer key = Integer.valueOf(controllerClass.getName().hashCode());
		return columnOrderCache.get(key);
	}

	/**
	 * Registers new column width to be saved for further use. Only positive column widths will be
	 * saved.
	 * 
	 * @param controllerClass
	 *            {@link info.novatec.inspectit.rcp.editor.table.input.TableInputController} class
	 *            where this column is defined.
	 * @param columnName
	 *            Column name.
	 * @param width
	 *            New width
	 */
	public static void registerNewColumnWidth(Class<?> controllerClass, String columnName, int width) {
		// only register positive values, thus keep track of the column size
		if (width > 0) {
			int hash = getColumnHash(controllerClass, columnName);
			columnSizeCache.put(hash, width);
		}
	}

	/**
	 * Creates hash code for column by its name and the
	 * {@link info.novatec.inspectit.rcp.editor.table.input.TableInputController} class it is
	 * located.
	 * 
	 * @param controllerClass
	 *            {@link info.novatec.inspectit.rcp.editor.table.input.TableInputController} class
	 *            where this column is defined.
	 * @param columnName
	 *            Column name.
	 * @return Hash code for caching.
	 */
	private static int getColumnHash(Class<?> controllerClass, String columnName) {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((controllerClass.getName() == null) ? 0 : controllerClass.getName().hashCode());
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		return result;
	}

	/**
	 * Loads preferences for columns size/visibility.
	 */
	private static synchronized void startUp() {
		columnSizeCache = new HashMap<Integer, Integer>();
		PreferencesUtils.loadPrimitiveMap(PreferencesConstants.TABLE_COLUMN_SIZE_CACHE, columnSizeCache, Integer.class, Integer.class);

		hiddenColumnsCache = new HashSet<Integer>();
		PreferencesUtils.loadPrimitiveCollection(PreferencesConstants.HIDDEN_TABLE_COLUMN_CACHE, hiddenColumnsCache, Integer.class);

		columnOrderCache = PreferencesUtils.getObject(PreferencesConstants.TABLE_COLUMN_ORDER_CACHE);
		if (null == columnOrderCache) {
			columnOrderCache = new HashMap<Integer, int[]>();
		}

		// shut down hook to save the data when closing UI
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				saveChanges();
			}
		});

	}

	/**
	 * Saves the preferences about columns size/visibility.
	 */
	private static void saveChanges() {
		PreferencesUtils.saveObject(PreferencesConstants.TABLE_COLUMN_SIZE_CACHE, columnSizeCache, false);
		PreferencesUtils.saveObject(PreferencesConstants.HIDDEN_TABLE_COLUMN_CACHE, hiddenColumnsCache, false);
		PreferencesUtils.saveObject(PreferencesConstants.TABLE_COLUMN_ORDER_CACHE, columnOrderCache, false);
	}

}
