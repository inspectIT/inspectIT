package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Details page for the {@link ExceptionSensorAssignment}.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionSensorAssignmentDetailsPage extends AbstractClassSensorAssignmentDetailsPage {

	/**
	 * {@link ExceptionSensorAssignment}.
	 */
	private ExceptionSensorAssignment assignment;

	/**
	 * Constructor.
	 * 
	 * @param masterBlockListener
	 *            listener to inform the master block on changes to the input
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public ExceptionSensorAssignmentDetailsPage(ISensorAssignmentUpdateListener masterBlockListener, boolean canEdit) {
		super(masterBlockListener, canEdit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		TableWrapLayout parentLayout = new TableWrapLayout();
		parentLayout.topMargin = 5;
		parentLayout.leftMargin = 5;
		parentLayout.rightMargin = 2;
		parentLayout.bottomMargin = 2;
		parentLayout.numColumns = 2;
		parentLayout.makeColumnsEqualWidth = true;
		parent.setLayout(parentLayout);

		FormToolkit toolkit = managedForm.getToolkit();

		// title
		FormText title = createTitle(parent, toolkit);
		TableWrapData twd = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		twd.colspan = 2;
		title.setLayoutData(twd);

		super.createContents(parent);

		// create help to correctly layout
		twd = new TableWrapData();
		twd.grabHorizontal = true;
		toolkit.createLabel(parent, "", SWT.NONE).setLayoutData(twd);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ExceptionSensorAssignment getInput() {
		return assignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setInput(ISelection selection) {
		if (!selection.isEmpty()) {
			assignment = (ExceptionSensorAssignment) ((IStructuredSelection) selection).getFirstElement();
		} else {
			assignment = null; // NOPMD
		}
	}

}
