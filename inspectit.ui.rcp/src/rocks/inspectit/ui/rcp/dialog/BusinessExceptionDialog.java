package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.IErrorCode;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Dialog for displaying {@link BusinessException}s.
 * 
 * @author Ivan Senic
 * 
 */
public class BusinessExceptionDialog extends TitleAreaDialog {

	/**
	 * Dialog title.
	 */
	private String dialogTitle = "Business exception occurred";

	/**
	 * Exception being shown.
	 */
	private BusinessException exception;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Parent shell.
	 * @param exception
	 *            Exception being shown.
	 */
	public BusinessExceptionDialog(Shell parentShell, BusinessException exception) {
		super(parentShell);
		Assert.isNotNull(exception, "Exception to show in the dialog must not be null.");
		this.exception = exception;

		if (exception instanceof TechnicalException) {
			dialogTitle = "Technical exception occurred";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(dialogTitle);

		this.setTitleImage(InspectIT.getDefault().getImage(InspectITImages.IMG_WIZBAN_ERROR));
		if (null != exception.getErrorCode()) {
			this.setMessage(exception.getErrorCode().getName());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true).setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Composite main = toolkit.createComposite(parent);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 400;
		gd.heightHint = 200;
		main.setLayoutData(gd);

		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.topMargin = 0;
		tableWrapLayout.leftMargin = 0;
		tableWrapLayout.bottomMargin = 0;
		tableWrapLayout.rightMargin = 0;
		main.setLayout(tableWrapLayout);

		// create separator for better visualization
		Composite separator = toolkit.createCompositeSeparator(main);
		TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB);
		tableWrapData.heightHint = 2;
		separator.setLayoutData(tableWrapData);

		// then goes content
		Composite content = toolkit.createComposite(main);
		content.setLayout(new TableWrapLayout());
		content.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// main message
		FormText mainMessageFormText = toolkit.createFormText(content, false);
		StringBuilder mainMessage = new StringBuilder("<form><p>");
		if (null != exception.getActionPerformed()) {
			mainMessage.append("An exception occurred while executing the action <span color=\"headingColor\">" + exception.getActionPerformed() + "</span><br/><br/>");
		}
		if (null != exception.getServiceMethodSignature()) {
			mainMessage.append("The exception occurred while invoking the service method <span color=\"headingColor\">" + exception.getServiceMethodSignature() + "</span><br/><br/>");
		}
		mainMessage.append("Following error code was reported:</p></form>");
		mainMessageFormText.setText(mainMessage.toString(), true, false);
		mainMessageFormText.setLayoutData(new TableWrapData(TableWrapData.FILL));
		mainMessageFormText.setColor("headingColor", toolkit.getColors().getColor(IFormColors.TITLE));

		IErrorCode errorCode = exception.getErrorCode();

		// title
		Composite titleComposite = toolkit.createComposite(content);
		titleComposite.setLayout(new GridLayout(1, true));
		titleComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		FormText titleFormText = toolkit.createFormText(titleComposite, false);
		titleFormText.setText("<form><p><span color=\"headingColor\">" + errorCode.getName() + "</span></p></form>", true, false);
		titleFormText.setColor("headingColor", getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		titleFormText.setFont(JFaceResources.getFont(JFaceResources.HEADER_FONT));
		gd = new GridData(SWT.CENTER, SWT.FILL, true, false);
		titleFormText.setLayoutData(gd);

		// description part
		FormText descriptionFormText = toolkit.createFormText(content, false);
		StringBuilder descriptionMessage = new StringBuilder("<form><p>");

		// description
		descriptionMessage.append("<span color=\"headingColor\" font=\"headingFont\">Description</span><br/>");
		if (null != errorCode.getDescription()) {
			descriptionMessage.append(errorCode.getDescription());
			descriptionMessage.append("<br/><br/>");
		} else {
			descriptionMessage.append("-<br/><br/>");
		}

		// causes
		descriptionMessage.append("<span color=\"headingColor\" font=\"headingFont\">Possible causes</span><br/>");
		if (null != errorCode.getPossibleCause()) {
			String[] causes = errorCode.getPossibleCause().split("\n");
			for (String cause : causes) {
				descriptionMessage.append(cause);
				descriptionMessage.append("<br/>");
			}
			descriptionMessage.append("<br/>");
		} else {
			descriptionMessage.append("-<br/><br/>");
		}

		// solutions
		descriptionMessage.append("<span color=\"headingColor\" font=\"headingFont\">Possible solutions</span><br/>");
		if (null != errorCode.getPossibleSolution()) {
			String[] solutions = errorCode.getPossibleSolution().split("\n");
			for (String solution : solutions) {
				descriptionMessage.append(solution);
				descriptionMessage.append("<br/>");
			}
			descriptionMessage.append("<br/>");
		} else {
			descriptionMessage.append("-<br/><br/>");
		}

		// end
		descriptionMessage.append("</p></form>");

		descriptionFormText.setColor("headingColor", getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		descriptionFormText.setFont("headingFont", JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		descriptionFormText.setText(descriptionMessage.toString(), true, false);
		descriptionFormText.setLayoutData(new TableWrapData(TableWrapData.FILL));

		return main;
	}
}
