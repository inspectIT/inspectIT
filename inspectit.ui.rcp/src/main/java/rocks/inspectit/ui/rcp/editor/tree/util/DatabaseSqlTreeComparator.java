package rocks.inspectit.ui.rcp.editor.tree.util;

import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Objects;

import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.ui.rcp.editor.tree.TreeViewerComparator;
import rocks.inspectit.ui.rcp.util.data.DatabaseInfoHelper;

/**
 * Special comparator to avoid the comparison of {@link DatabaseInfoHelper}s.
 * 
 * @author Ivan Senic
 * 
 */
public class DatabaseSqlTreeComparator extends TreeViewerComparator<SqlStatementData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 instanceof SqlStatementData && o2 instanceof SqlStatementData) {
			DatabaseInfoHelper d1 = new DatabaseInfoHelper((SqlStatementData) o1);
			DatabaseInfoHelper d2 = new DatabaseInfoHelper((SqlStatementData) o2);
			if (Objects.equal(d1, d2)) {
				return super.compare(viewer, o1, o2);
			} else {
				return this.compareDatabaseInfoHelpers(d1, d2);
			}
		} else if (o1 instanceof DatabaseInfoHelper && o2 instanceof DatabaseInfoHelper) {
			return this.compareDatabaseInfoHelpers((DatabaseInfoHelper) o1, (DatabaseInfoHelper) o2);
		}
		return 0;
	}

	/**
	 * @param d1
	 *            {@link DatabaseInfoHelper}
	 * @param d2
	 *            {@link DatabaseInfoHelper}
	 * @return Returns result of the comparison of the database urls.
	 */
	private int compareDatabaseInfoHelpers(DatabaseInfoHelper d1, DatabaseInfoHelper d2) {
		return d1.getDatabaseUrl().compareToIgnoreCase(d2.getDatabaseUrl());
	}

}