package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Details page for the {@link ExceptionSensorAssignment}.
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
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public ExceptionSensorAssignmentDetailsPage(boolean canEdit) {
		super(canEdit);
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

		// create buttons on the end
		Label helpLabel = toolkit.createLabel(parent, "", SWT.NONE);
		twd = new TableWrapData();
		twd.grabHorizontal = true;
		helpLabel.setLayoutData(twd);

		Control okControl = super.createOkButton(parent);
		twd = new TableWrapData(TableWrapData.RIGHT);
		okControl.setLayoutData(twd);
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
	public void selectionChanged(IFormPart part, ISelection selection) {
		if (!selection.isEmpty()) {
			assignment = (ExceptionSensorAssignment) ((IStructuredSelection) selection).getFirstElement();
		} else {
			assignment = null; // NOPMD
		}
		update();
	}

}
