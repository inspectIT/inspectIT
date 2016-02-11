package info.novatec.inspectit.rcp.ci.dialog;

import info.novatec.inspectit.ci.context.AbstractContextCapture;
import info.novatec.inspectit.ci.context.impl.FieldContextCapture;
import info.novatec.inspectit.ci.context.impl.ParameterContextCapture;
import info.novatec.inspectit.ci.context.impl.ReturnContextCapture;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.validation.IControlValidationListener;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for creating new {@link AbstractContextCapture}.
 * 
 * @author Ivan Senic
 * 
 */
public class CaptureContextDialog extends TitleAreaDialog implements IControlValidationListener {

	/**
	 * Context capture being created.
	 */
	private AbstractContextCapture contextCapture;

	/**
	 * All {@link ValidationControlDecoration}s.
	 */
	private List<ValidationControlDecoration<?>> validationControlDecorations = new ArrayList<>();

	/**
	 * Defined accessor paths.
	 */
	private List<String> paths = new ArrayList<>();

	/**
	 * Dialog OK button.
	 */
	private Button okButton;

	/**
	 * Radio for return context type selection.
	 */
	private Button returnButton;

	/**
	 * Radio for parameter context type selection.
	 */
	private Button parameterButton;

	/**
	 * Radio for field context type selection.
	 */
	private Button fieldButton;

	/**
	 * Text box for the name of the captured context.
	 */
	private Text nameText;

	/**
	 * Text box for displaying accessor paths.
	 */
	private Text accessorText;

	/**
	 * Button for adding new path.
	 */
	private Button addPath;

	/**
	 * Button for clearing all path.
	 */
	private Button clearPaths;

	/**
	 * Label for the index of parameter.
	 */
	private Label indexLabel;

	/**
	 * Text box for the index of parameter.
	 */
	private Text indexText;

	/**
	 * Label for the field name of field capture.
	 */
	private Label fieldLabel;

	/**
	 * Text box for the field name of field capture.
	 */
	private Text fieldText;

	/**
	 * Default constructor.
	 * 
	 * @param parentShell
	 *            Shell.
	 */
	public CaptureContextDialog(Shell parentShell) {
		this(parentShell, null);
	}

	/**
	 * Edit mode constructor. Data will be populated with the given {@link AbstractContextCapture}.
	 * 
	 * @param parentShell
	 *            Shell.
	 * @param contextCapture
	 *            context capture to edit
	 */
	public CaptureContextDialog(Shell parentShell, AbstractContextCapture contextCapture) {
		super(parentShell);
		this.contextCapture = contextCapture;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Context Capture");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("Add Context Capture");
		this.setMessage("Define type of the context capturing", IMessageProvider.INFORMATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(null != contextCapture);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (returnButton.getSelection()) {
				contextCapture = new ReturnContextCapture();
			} else if (parameterButton.getSelection()) {
				ParameterContextCapture capture = new ParameterContextCapture();
				capture.setIndex(Integer.parseInt(indexText.getText()));
				contextCapture = capture;
			} else if (fieldButton.getSelection()) {
				FieldContextCapture capture = new FieldContextCapture();
				capture.setFieldName(fieldText.getText());
				contextCapture = capture;
			}
			contextCapture.setDisplayName(nameText.getText());
			if (CollectionUtils.isNotEmpty(paths)) {
				contextCapture.setPaths(paths);
			}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(5, false);
		layout.horizontalSpacing = 10;
		main.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumWidth = 400;
		gd.minimumHeight = 250;
		main.setLayoutData(gd);

		Label typeLabel = new Label(main, SWT.NONE);
		typeLabel.setText("Catch type:");

		returnButton = new Button(main, SWT.RADIO);
		returnButton.setText("Return value");
		returnButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_RETURN));
		returnButton.setSelection(null == contextCapture || contextCapture instanceof ReturnContextCapture);
		returnButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		createInfoLabel(main, "Select for capturing the return value.");

		new Label(main, SWT.NONE);
		parameterButton = new Button(main, SWT.RADIO);
		parameterButton.setText("Parameter");
		parameterButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PARAMETER));
		parameterButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parameterButton.setSelection(contextCapture instanceof ParameterContextCapture);
		indexLabel = new Label(main, SWT.RIGHT);
		indexLabel.setText("Index:");
		indexLabel.setVisible(contextCapture instanceof ParameterContextCapture);
		indexLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		indexText = new Text(main, SWT.BORDER | SWT.RIGHT);
		indexText.setText("0");
		indexText.setVisible(contextCapture instanceof ParameterContextCapture);
		indexText.setEnabled(contextCapture instanceof ParameterContextCapture);
		indexText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final ValidationControlDecoration<Text> indexValidation = new ValidationControlDecoration<Text>(indexText, this) {
			@Override
			protected boolean validate(Text control) {
				try {
					return Integer.parseInt(control.getText()) >= 0;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		};
		indexValidation.setDescriptionText("Index must be a zero-positive index of parameter in a method.");
		indexValidation.registerListener(SWT.Modify);

		parameterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = parameterButton.getSelection();
				indexLabel.setVisible(selected);
				indexText.setVisible(selected);
				indexText.setEnabled(selected);
				indexText.setFocus();
				indexText.setSelection(0, indexText.getText().length());
				indexValidation.executeValidation();
			}
		});
		createInfoLabel(main, "Select for capturing the method parameter value. Specify the correct index of the parameter in the method.");

		new Label(main, SWT.NONE);
		fieldButton = new Button(main, SWT.RADIO);
		fieldButton.setText("Field");
		fieldButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_FIELD));
		fieldButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		fieldButton.setSelection(contextCapture instanceof FieldContextCapture);
		fieldLabel = new Label(main, SWT.RIGHT);
		fieldLabel.setText("Named:");
		fieldLabel.setVisible(contextCapture instanceof FieldContextCapture);
		fieldLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		fieldText = new Text(main, SWT.BORDER | SWT.RIGHT);
		fieldText.setText("myField");
		fieldText.setVisible(contextCapture instanceof FieldContextCapture);
		fieldText.setEnabled(contextCapture instanceof FieldContextCapture);
		fieldText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final ValidationControlDecoration<Text> fieldValidation = new ValidationControlDecoration<Text>(fieldText, null, this) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};
		fieldValidation.setDescriptionText("Name of the field to capture must be defined");
		fieldValidation.registerListener(SWT.Modify);
		fieldButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = fieldButton.getSelection();
				fieldLabel.setVisible(selected);
				fieldText.setVisible(selected);
				fieldText.setEnabled(selected);
				fieldText.setFocus();
				fieldText.setSelection(0, fieldText.getText().length());
				fieldValidation.executeValidation();
			}
		});
		createInfoLabel(main, "Select for capturing the field value on the object executing the method. Specify the correct name of the object field.");

		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText("Display name:");

		nameText = new Text(main, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		nameText.setFocus();

		ValidationControlDecoration<Text> nameValidation = new ValidationControlDecoration<Text>(nameText, null, this) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};
		nameValidation.setDescriptionText("Display name for this context capture must be defined");
		nameValidation.registerListener(SWT.Modify);
		createInfoLabel(main, "Display name defines how will the captured value be named.");

		Label accessorsLabel = new Label(main, SWT.NONE);
		accessorsLabel.setText("Accessor path:");

		accessorText = new Text(main, SWT.BORDER | SWT.READ_ONLY);
		accessorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		accessorText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				addPath.setFocus();
			}
		});
		addPath = new Button(main, SWT.PUSH);
		addPath.setText("Add Path");
		addPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addPath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog pathDialog = new InputDialog(getShell(), "Path to Follow", "Enter field name to navigate on the captured object:", "", null);
				if (pathDialog.open() == Dialog.OK) {
					String path = pathDialog.getValue();
					if (StringUtils.isNotBlank(path)) {
						paths.add(path);
						updateAccessorText();
					}
				}
			}
		});
		clearPaths = new Button(main, SWT.PUSH);
		clearPaths.setText("Clear All Paths");
		clearPaths.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		clearPaths.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				paths.clear();
				updateAccessorText();
			}
		});
		createInfoLabel(
				main,
				"Specify path to follow on the captured object. For example, if object of type Customer is captured, and this object has a field named 'id', then you can save the 'id' value by creating a path '-> id'. ");

		validationControlDecorations.add(indexValidation);
		validationControlDecorations.add(fieldValidation);
		validationControlDecorations.add(nameValidation);

		if (null != contextCapture) {
			nameText.setText(contextCapture.getDisplayName());
			if (CollectionUtils.isNotEmpty(contextCapture.getPaths())) {
				paths.addAll(contextCapture.getPaths());
				updateAccessorText();
			}
			if (contextCapture instanceof ParameterContextCapture) {
				indexText.setText(String.valueOf(((ParameterContextCapture) contextCapture).getIndex()));
			} else if (contextCapture instanceof FieldContextCapture) {
				fieldText.setText(((FieldContextCapture) contextCapture).getFieldName());
			}
		}

		return main;
	}

	/**
	 * Updates text in accessor text.
	 */
	private void updateAccessorText() {
		accessorText.setText("");
		StringBuilder builder = new StringBuilder();
		for (String path : paths) {
			builder.append(" -> ");
			builder.append(path);
		}
		accessorText.setText(builder.toString());
	}

	/**
	 * Gets {@link #contextCapture}.
	 * 
	 * @return {@link #contextCapture}
	 */
	public AbstractContextCapture getContextCapture() {
		return contextCapture;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		boolean allValid = true;
		for (ValidationControlDecoration<?> validation : validationControlDecorations) {
			if (!validation.isValid()) {
				allValid = false;
				break;
			}
		}

		if (null != okButton) {
			okButton.setEnabled(allValid);
		}
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 * 
	 * @param parent
	 *            Composite to create on.
	 * @param text
	 *            Information text.
	 */
	protected void createInfoLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

}
