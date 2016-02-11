package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * The abstract class of the {@link TreeInputController} interface to provide some standard methods.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public abstract class AbstractTreeInputController implements TreeInputController {

	/**
	 * Map of the enumeration keys and {@link TreeViewerColumn}s. Subclasses can use utility methods
	 * to bound columns for later use.
	 */
	private Map<Enum<?>, TreeViewerColumn> treeViewerColumnMap = new HashMap<Enum<?>, TreeViewerColumn>();

	/**
	 * The input definition.
	 */
	private InputDefinition inputDefinition;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		Assert.isNotNull(inputDefinition);

		this.inputDefinition = inputDefinition;
	}

	/**
	 * Returns the input definition.
	 * 
	 * @return The input definition.
	 */
	protected InputDefinition getInputDefinition() {
		Assert.isNotNull(inputDefinition);

		return inputDefinition;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public boolean canOpenInput(List<? extends DefaultData> data) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void createColumns(TreeViewer treeViewer) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void doubleClick(DoubleClickEvent event) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public ViewerComparator getComparator() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public IContentProvider getContentProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public ViewerFilter[] getFilters() {
		return new ViewerFilter[0];
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public IBaseLabelProvider getLabelProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return an empty set by default, sub-classes may override.
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public String getReadableString(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public Object getTreeInput() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <b>2</b> by default, sub-classes may override.
	 */
	public int getExpandLevel() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getObjectsToSearch(Object treeInput) {
		if (treeInput instanceof Object[]) {
			return (Object[]) treeInput;
		}
		if (treeInput instanceof Collection) {
			return ((Collection<?>) treeInput).toArray();
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default controller sets the sub view to be master.
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.MASTER;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns true, classes may override.
	 */
	@Override
	public boolean canAlterColumnWidth(TreeColumn treeColumn) {
		return true;
	}

	/**
	 * Maps a column with the enumeration key. The implementing classes should map each column they
	 * create to the enum that represents that column. Later on the column can be retrieved with the
	 * enum key if needed.
	 * 
	 * @param key
	 *            Enumeration that represents the column.
	 * @param column
	 *            Created column to be mapped.
	 */
	public void mapTreeViewerColumn(Enum<?> key, TreeViewerColumn column) {
		treeViewerColumnMap.put(key, column);
	}

	/**
	 * Returns the column that has been mapped with the given enum key. Enum should represent the
	 * wanted column.
	 * 
	 * @param key
	 *            Enumeration that represents the column.
	 * @return Returns the column that has been mapped with the given enum key or <code>null</code>
	 *         if no mapping has been done.
	 */
	public TreeViewerColumn getMappedTreeViewerColumn(Enum<?> key) {
		return treeViewerColumnMap.get(key);
	}

}
