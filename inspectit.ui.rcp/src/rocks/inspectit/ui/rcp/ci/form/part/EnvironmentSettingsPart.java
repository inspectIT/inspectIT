package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.strategy.IStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.ListSendingStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.SimpleBufferStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.SizeBufferStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.TimeSendingStrategyConfig;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Part for defining the environment general setting.
 * 
 * @author Ivan Senic
 * 
 */
public class EnvironmentSettingsPart extends SectionPart implements IPropertyListener {

	/**
	 * Display name of the size buffer strategy.
	 */
	private static final String SIZE_BUFFER_STRATEGY = "Size buffer";

	/**
	 * Display name of the simple buffer strategy.
	 */
	private static final String SIMPLE_BUFFER_STRATEGY = "Simple buffer";

	/**
	 * Display name of the list sending strategy.
	 */
	private static final String LIST_SENDING_STRATEGY = "List size strategy";

	/**
	 * Display name of the time sending strategy.
	 */
	private static final String TIME_SENDING_STRATEGY = "Time strategy";

	/**
	 * Form page.
	 */
	private FormPage formPage;

	/**
	 * Environment being edited.
	 */
	private Environment environment;

	/**
	 * {@link ControlDecoration} for displaying validation errors for sending strategy.
	 */
	private ValidationControlDecoration<Text> sendingValueDecoration;

	/**
	 * {@link ControlDecoration} for displaying validation errors for buffer strategy.
	 */
	private ValidationControlDecoration<Text> bufferValueDecoration;

	/**
	 * {@link Text} for sending strategy value.
	 */
	private Text sendingValue;

	/**
	 * {@link Text} for buffer strategy value.
	 */
	private Text bufferValue;

	/**
	 * {@link Combo} for choosing the sending strategy.
	 */
	private Combo sendingCombo;

	/**
	 * Combo for choosing buffer strategy.
	 */
	private Combo bufferCombo;

	/**
	 * Button for class loading delegation.
	 */
	private Button classDelegationButton;

	/**
	 * Default constructor.
	 * 
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public EnvironmentSettingsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createPart(getSection(), toolkit);

		// text and description on our own
		getSection().setText("Strategies");
		Label label = toolkit.createLabel(getSection(), "Define sending and buffer strategies");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(label);
	}

	/**
	 * Creates complete client.
	 * 
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createPart(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.horizontalSpacing = 10;
		mainComposite.setLayout(gridLayout);
		section.setClient(mainComposite);

		toolkit.createLabel(mainComposite, "Sending strategy:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		sendingCombo = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);

		sendingCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		toolkit.adapt(sendingCombo, false, false);
		sendingValue = toolkit.createText(mainComposite, "", SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = 50;
		sendingValue.setLayoutData(gd);
		createInfoLabel(
				mainComposite,
				toolkit,
				"The time strategy will cause the Agent to send its measurements after a specified interval in milliseconds.\nThe list size strategy will cause the Agent to send its measurements after a specified size of value objects is reached.");

		toolkit.createLabel(mainComposite, "Buffer strategy:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		bufferCombo = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);

		bufferCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		toolkit.adapt(bufferCombo, false, false);
		bufferValue = toolkit.createText(mainComposite, "", SWT.BORDER | SWT.RIGHT);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = 50;
		bufferValue.setLayoutData(gd);
		createInfoLabel(
				mainComposite,
				toolkit,
				"The simple version of a buffer is apparently no buffer at all. It contains exactly one element. This is useful if old data isn't necessary or maybe the memory of the application is very limited.\nThe Size buffer strategy needs specification of the size of this buffer. This buffer works as a FILO stack, so last added elements will be sent first (as they are more important), and old ones are thrown away if this buffer is full");

		// fill the boxes and values
		sendingCombo.add(TIME_SENDING_STRATEGY);
		sendingCombo.add(LIST_SENDING_STRATEGY);
		IStrategyConfig sendingStrategyConfig = environment.getSendingStrategyConfig();
		if (sendingStrategyConfig instanceof TimeSendingStrategyConfig) {
			sendingCombo.setData(TIME_SENDING_STRATEGY, sendingStrategyConfig);
			sendingCombo.setData(LIST_SENDING_STRATEGY, new ListSendingStrategyConfig());
			sendingCombo.select(0);
			sendingValue.setText(String.valueOf(((TimeSendingStrategyConfig) sendingStrategyConfig).getTime()));
		} else if (sendingStrategyConfig instanceof ListSendingStrategyConfig) {
			sendingCombo.setData(TIME_SENDING_STRATEGY, new TimeSendingStrategyConfig());
			sendingCombo.setData(LIST_SENDING_STRATEGY, sendingStrategyConfig);
			sendingCombo.select(1);
			sendingValue.setText(String.valueOf(((ListSendingStrategyConfig) sendingStrategyConfig).getListSize()));
		}

		bufferCombo.add(SIMPLE_BUFFER_STRATEGY);
		bufferCombo.add(SIZE_BUFFER_STRATEGY);
		IStrategyConfig bufferStrategyConfig = environment.getBufferStrategyConfig();
		if (bufferStrategyConfig instanceof SimpleBufferStrategyConfig) {
			bufferCombo.setData(SIMPLE_BUFFER_STRATEGY, bufferStrategyConfig);
			bufferCombo.setData(SIZE_BUFFER_STRATEGY, new SizeBufferStrategyConfig());
			bufferCombo.select(0);
			bufferValue.setEnabled(false);
		} else if (bufferStrategyConfig instanceof SizeBufferStrategyConfig) {
			bufferCombo.setData(SIMPLE_BUFFER_STRATEGY, new SimpleBufferStrategyConfig());
			bufferCombo.setData(SIZE_BUFFER_STRATEGY, bufferStrategyConfig);
			bufferCombo.select(1);
			bufferValue.setText(String.valueOf(((SizeBufferStrategyConfig) bufferStrategyConfig).getSize()));
		}

		// listeners
		sendingCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object data = sendingCombo.getData(sendingCombo.getItem(sendingCombo.getSelectionIndex()));
				if (data instanceof TimeSendingStrategyConfig) {
					sendingValue.setText(String.valueOf(((TimeSendingStrategyConfig) data).getTime()));
				} else if (data instanceof ListSendingStrategyConfig) {
					sendingValue.setText(String.valueOf(((ListSendingStrategyConfig) data).getListSize()));
				}
			}
		});
		// disable the buffer value field when simple strategy is on
		bufferCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object data = bufferCombo.getData(bufferCombo.getItem(bufferCombo.getSelectionIndex()));
				if (data instanceof SimpleBufferStrategyConfig) {
					bufferValue.setEnabled(false);
					bufferValue.setText("");
				} else if (data instanceof SizeBufferStrategyConfig) {
					bufferValue.setEnabled(true);
					bufferValue.setText(String.valueOf(((SizeBufferStrategyConfig) data).getSize()));
				}
			}
		});

		// validation boxes
		sendingValueDecoration = new ValidationControlDecoration<Text>(sendingValue, formPage.getManagedForm().getMessageManager()) {
			@Override
			protected boolean validate(Text control) {
				return validateUpdateSendingStrategy(false);
			}
		};
		sendingValueDecoration.registerListener(SWT.Modify);
		bufferValueDecoration = new ValidationControlDecoration<Text>(bufferValue, formPage.getManagedForm().getMessageManager()) {
			@Override
			protected boolean validate(Text control) {
				return validateUpdateBufferStrategy(false);
			}
		};
		bufferValueDecoration.registerListener(SWT.Modify);

		// class delegation
		toolkit.createLabel(mainComposite, "Class loading delegation:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		classDelegationButton = toolkit.createButton(mainComposite, "Active", SWT.CHECK);
		classDelegationButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		classDelegationButton.setSelection(environment.isClassLoadingDelegation());
		createInfoLabel(
				mainComposite,
				toolkit,
				"If activated all sub-classes of java.lang.ClassLoader will be instrumented so that loading of the inspectIT classes is delegated to the inspectIT class loader. Should only be changed to false in rare cases and is expert user level option.");

		// dirty listener
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!isDirty()) {
					markDirty();
				}
			}
		};
		sendingCombo.addListener(SWT.Selection, dirtyListener);
		bufferCombo.addListener(SWT.Selection, dirtyListener);
		sendingValue.addListener(SWT.Modify, dirtyListener);
		bufferValue.addListener(SWT.Modify, dirtyListener);
		classDelegationButton.addListener(SWT.Selection, dirtyListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			validateUpdateSendingStrategy(true);
			validateUpdateBufferStrategy(true);
			environment.setClassLoadingDelegation(classDelegationButton.getSelection());
			getManagedForm().dirtyStateChanged();
		}
	}

	/**
	 * Validates the sending strategy value and updates if needed.
	 * 
	 * @param update
	 *            If beside validation an update on the model object should be done.
	 * @return if control has valid value
	 */
	private boolean validateUpdateSendingStrategy(boolean update) {
		boolean valid = true;
		IStrategyConfig sendingStrategy = (IStrategyConfig) sendingCombo.getData(sendingCombo.getItem(sendingCombo.getSelectionIndex()));
		if (sendingStrategy instanceof TimeSendingStrategyConfig) {
			try {
				long time = Long.parseLong(sendingValue.getText());
				if (time <= 0) {
					showTimeSendingStrategyValidationMessage();
					valid = false;
				} else {
					if (update) {
						((TimeSendingStrategyConfig) sendingStrategy).setTime(time);
						environment.setSendingStrategyConfig(sendingStrategy);
					}
				}
			} catch (NumberFormatException exception) {
				showTimeSendingStrategyValidationMessage();
				valid = false;
			}
		} else if (sendingStrategy instanceof ListSendingStrategyConfig) {
			try {
				int size = Integer.parseInt(sendingValue.getText());
				if (size <= 0) {
					showListSendingStrategyValidationMessage();
					valid = false;
				} else {
					if (update) {
						((ListSendingStrategyConfig) sendingStrategy).setListSize(size);
						environment.setSendingStrategyConfig(sendingStrategy);
					}
				}
			} catch (NumberFormatException exception) {
				showListSendingStrategyValidationMessage();
				valid = false;
			}
		}

		return valid;
	}

	/**
	 * Validates the sending strategy value and updates if needed.
	 * 
	 * @param update
	 *            If beside validation an update on the model object should be done.
	 * @return if control has valid value
	 */
	private boolean validateUpdateBufferStrategy(boolean update) {
		boolean valid = true;
		IStrategyConfig bufferStrategy = (IStrategyConfig) bufferCombo.getData(bufferCombo.getItem(bufferCombo.getSelectionIndex()));
		if (bufferStrategy instanceof SizeBufferStrategyConfig) {
			try {
				int size = Integer.parseInt(bufferValue.getText());
				if (size <= 0) {
					showSizeBufferStrategyValidationMessage();
					valid = false;
				} else {
					if (update) {
						((SizeBufferStrategyConfig) bufferStrategy).setSize(size);
					}
				}
			} catch (NumberFormatException exception) {
				showSizeBufferStrategyValidationMessage();
				valid = false;
			}
		}
		if (update) {
			environment.setBufferStrategyConfig(bufferStrategy);
		}

		return valid;
	}

	/**
	 * Shows validation error message for time sending strategy.
	 */
	private void showTimeSendingStrategyValidationMessage() {
		sendingValueDecoration.setDescriptionText("Time sending strategy must define a positive number of milliseconds.");
	}

	/**
	 * Shows validation error message for list sending strategy.
	 */
	private void showListSendingStrategyValidationMessage() {
		sendingValueDecoration.setDescriptionText("List sending strategy must define a list size greater than zero.");
	}

	/**
	 * Shows validation error message for size buffer strategy.
	 */
	private void showSizeBufferStrategyValidationMessage() {
		bufferValueDecoration.setDescriptionText("Size buffer strategy must define a buffer size greater than zero.");
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 * 
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @param text
	 *            Information text.
	 */
	protected void createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
			environment = input.getEnvironment();
		}
	}

	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}
