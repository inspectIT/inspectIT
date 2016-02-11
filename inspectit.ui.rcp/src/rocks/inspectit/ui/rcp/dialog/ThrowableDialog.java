package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.exception.RemoteException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.handlers.OpenUrlHandler.ExceptionSupportHandler;

import java.util.Objects;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * The dialog for any kind of unexpected messages.
 * 
 * @author Ivan Senic
 * 
 */
public class ThrowableDialog extends TitleAreaDialog {

	/**
	 * Dialog title.
	 */
	private static final String DIALOG_TITLE = "Error occurred";

	/**
	 * Main message to show.
	 */
	private String message;

	/**
	 * Throwable being show.
	 */
	private Throwable throwable;

	/**
	 * Details composite, where details will be displayed on request.
	 */
	private Composite detailsComposite;

	/**
	 * {@link FormToolkit}.
	 */
	private FormToolkit toolkit;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Parent shell.
	 * @param message
	 *            Main message to show. If one is not supplied, message from throwable will be
	 *            displayed.
	 * @param throwable
	 *            Throwable being show.
	 */
	public ThrowableDialog(Shell parentShell, String message, Throwable throwable) {
		super(parentShell);
		Assert.isNotNull(throwable, "Throwable to show in the dialog must not be null.");
		this.throwable = throwable;
		this.message = message;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(DIALOG_TITLE);
		this.setTitleImage(InspectIT.getDefault().getImage(InspectITImages.IMG_WIZBAN_ERROR));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DIALOG_TITLE);

		newShell.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (areDetailsDisplayed()) {
					((GridData) detailsComposite.getLayoutData()).widthHint = getShell().getSize().x;
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
		createButton(parent, IDialogConstants.CLIENT_ID, "Send Error Report", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true).setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

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
		FormText mainFormText = toolkit.createFormText(content, false);

		StringBuilder sb = new StringBuilder("<form><p>");
		if (throwable instanceof RemoteException) {
			String serviceMethod = ((RemoteException) throwable).getServiceMethodSignature();
			sb.append("The error occurred while invoking the service method <span color=\"info\">" + serviceMethod + "</span>.");
		} else {
			sb.append("The unexpected error occurred in the <span color=\"info\">inspectIT User interface</span>.");
		}

		// message
		sb.append("<br/><br/><span color=\"header\" font=\"header\">Error Message</span><br/>");
		if (null != message) {
			sb.append(message);
		} else if (null != throwable.getMessage()) {
			sb.append(throwable.getMessage());
		} else {
			sb.append('-');
		}

		// error report
		sb.append("<br/><br/><span color=\"header\" font=\"header\">Send Error Report</span><br/>Please send us the error report to help fix the problem and improve this software.<br/><br/><a href=\"errorReport\">What data does the error report contain?</a></p></form>");

		mainFormText.setText(sb.toString(), true, false);
		mainFormText.setColor("info", toolkit.getColors().getColor(IFormColors.TITLE));
		mainFormText.setColor("header", getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		mainFormText.setFont("header", JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		mainFormText.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showErrorReportDescription();
			}
		});

		return main;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);

		if (buttonId == IDialogConstants.DETAILS_ID) {
			Composite buttonBar = (Composite) getButtonBar();
			Button detailsButton = getButton(buttonId);

			// details here
			if (areDetailsDisplayed()) {
				// if displayed just remove them
				detailsComposite.dispose();
				detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);

			} else {
				// if not displayed create complete
				createDetails(buttonBar);
				detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
			}
			fixShellSize();
		} else if (buttonId == IDialogConstants.CLIENT_ID) {
			try {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(ExceptionSupportHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
				context.addVariable(ExceptionSupportHandler.INPUT, throwable);

				command.executeWithChecks(executionEvent);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	/**
	 * @return If details are currently displayed.
	 */
	private boolean areDetailsDisplayed() {
		return null != detailsComposite && !detailsComposite.isDisposed();
	}

	/**
	 * manual fixing of shell size when details are shown.
	 */
	private void fixShellSize() {
		int yDelta = getShell().getSize().y - getContents().getSize().y;
		((Composite) getContents()).layout(true, true);
		Point size = getShell().getSize();
		getShell().setSize(size.x, getContents().computeSize(size.x, SWT.DEFAULT).y + yDelta);
	}

	/**
	 * Creates details.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	private void createDetails(Composite parent) {
		if (!areDetailsDisplayed()) {
			detailsComposite = toolkit.createComposite(parent);
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 2;
			detailsComposite.setLayout(layout);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, ((GridLayout) parent.getLayout()).numColumns, 1);
			gd.widthHint = getShell().getSize().x;
			detailsComposite.setLayoutData(gd);

			Label additonalInfo = toolkit.createLabel(detailsComposite, "Additional Information", SWT.NONE);
			additonalInfo.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
			additonalInfo.setFont(JFaceResources.getFont(JFaceResources.HEADER_FONT));
			TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
			twd.colspan = 2;
			additonalInfo.setLayoutData(twd);

			Label label = toolkit.createLabel(detailsComposite, "Exception:", SWT.NONE);
			label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

			Label exceptionType = toolkit.createLabel(detailsComposite, "", SWT.WRAP);
			exceptionType.setLayoutData(new TableWrapData(TableWrapData.FILL));

			label = toolkit.createLabel(detailsComposite, "Message:", SWT.NONE);
			label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

			Label exceptionMessage = toolkit.createLabel(detailsComposite, "", SWT.WRAP);
			exceptionMessage.setLayoutData(new TableWrapData(TableWrapData.FILL));

			label = toolkit.createLabel(detailsComposite, "Cause:", SWT.NONE);
			label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

			Label exceptionCause = toolkit.createLabel(detailsComposite, "", SWT.WRAP);
			exceptionCause.setLayoutData(new TableWrapData(TableWrapData.FILL));

			label = toolkit.createLabel(detailsComposite, "Stack trace:", SWT.WRAP);
			label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

			Text stackTrace = toolkit.createText(detailsComposite, "", SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
			twd = new TableWrapData(TableWrapData.FILL);
			twd.heightHint = 150;
			twd.maxWidth = 0;
			stackTrace.setLayoutData(twd);

			// special for Remote Exception
			if (throwable instanceof RemoteException) {
				exceptionType.setText(((RemoteException) throwable).getOriginalExceptionClass());
			} else {
				exceptionType.setText(throwable.getClass().getName());
			}
			if (null != throwable.getMessage()) {
				exceptionMessage.setText(throwable.getMessage());
			} else {
				exceptionMessage.setText("-");
			}
			if (null != throwable.getCause() && !Objects.equals(throwable, throwable.getCause())) {
				exceptionCause.setText(throwable.getCause().getClass().getName() + ": " + throwable.getCause().getMessage());
			} else {
				exceptionCause.setText("-");
			}
			stackTrace.setText(ExceptionUtils.getFullStackTrace(throwable));
		}
	}

	/**
	 * Shows the description box.
	 */
	private void showErrorReportDescription() {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		PopupDialog popupDialog = new PopupDialog(getShell(), shellStyle, true, false, false, false, false, "What data does the error report contain?", "What data does the error report contain?") {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				Text text = toolkit.createText(parent, null, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = 3;
				gd.verticalIndent = 3;
				text.setLayoutData(gd);
				text.setText("The error report to send to the inspectIT Team contains the following information:\n\n - inspectIT version you are using\n - Your operating system information and used Java version\n - Exception type, message and stack trace\n\nThe sending of the error report is only available via Send-To mail option. After clicking to the 'Send Error Report' button your e-mail client will open with new predefined e-mail message to send to support.inspectit@novatec-gmbh.de. The e-mail used to send the error report will be kept private.\n\nWe thank you in advance for helping us to fix this problem and improve our software.");
				return composite;
			}

			@Override
			protected Point getInitialLocation(Point initialSize) {
				// show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			@Override
			protected Point getInitialSize() {
				return new Point(600, 300);
			}
		};
		popupDialog.open();

	}
}
