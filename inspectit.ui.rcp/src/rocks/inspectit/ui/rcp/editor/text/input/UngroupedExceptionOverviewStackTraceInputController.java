package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Textual input controller for displaying the stack trace of a single {@link ExceptionSensorData}
 * object.
 * 
 * @author Ivan Senic
 * 
 */
public class UngroupedExceptionOverviewStackTraceInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.ungroupedexceptionoverviewstacktrace";

	/**
	 * Text box to display the stack trace.
	 */
	private Text stackTraceText;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		stackTraceText = toolkit.createText(parent, "", SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		stackTraceText.setEditable(false);
		stackTraceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
		if (data == null || data.isEmpty()) {
			stackTraceText.setText("");
		} else {
			Object input = data.get(0);
			if (input instanceof ExceptionSensorData) {
				stackTraceText.setText(((ExceptionSensorData) input).getStackTrace());
			}
		}
	}

}
