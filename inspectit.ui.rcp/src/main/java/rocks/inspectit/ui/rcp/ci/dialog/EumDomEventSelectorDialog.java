package rocks.inspectit.ui.rcp.ci.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.shared.cs.ci.eum.EumDomEventSelector;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.validation.IControlValidationListener;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;

/**
 * Dialog for creating or updating {@link EumDomEventSelector}s.
 *
 * @author Jonas Kunz
 *
 */
public class EumDomEventSelectorDialog extends TitleAreaDialog implements IControlValidationListener {

	/**
	 * Pattern for validating the events-list field. <br>
	 * The events must be either the "*" as wildcard or a comma separated list.
	 */
	private static final Pattern EVENTS_PATTERN = Pattern.compile("([a-zA-Z]+(,[a-zA-Z]+)*)|\\*");

	/**
	 * Pattern for validating the attributes-list field. <br>
	 * The list must contain html or JS attributes, or special attributes with a dollar sign, like
	 * $label. In addition, a naming prefix maybe prefixed separated by a dot.
	 */
	private static final Pattern ATTRIBUTES_PATTERN = Pattern.compile("([a-zA-Z0-9]+\\.)?\\$?[a-zA-Z0-9\\-]+(,([a-zA-Z0-9]+\\.)?\\$?[a-zA-Z0-9\\-]+)*");

	/**
	 * Pattern for validating the attributes-list field. <br>
	 * The list must contiant html or JS attributes, or special attributes iwth a dollar sign, like
	 * $label.
	 */
	private static final Pattern ANCESTORS_PATTERN = Pattern.compile("(-1)|([0-9]+)");

	/**
	 * The selector currently being modified.
	 */
	private EumDomEventSelector selector;

	/**
	 * All {@link ValidationControlDecoration}s.
	 */
	private final List<ValidationControlDecoration<?>> validationControlDecorations = new ArrayList<>();

	/**
	 * OK button.
	 */
	private Button okButton;

	/**
	 * Holds the list of events to apply the selector on.
	 */
	private Text eventsText;

	/**
	 * Holds the CSS selector for selecting the elements to monitor.
	 */
	private Text selectorText;

	/**
	 * Holds the attributes to extract from the DOM element when an event is monitored.
	 */
	private Text attributesText;

	/**
	 * Holds the number of ancestor levels to check for this selector.
	 */
	private Text ancestorLevelsToCheckText;

	/**
	 * The checkbox for (un)setting the always-relevant flag.
	 */
	private Button alwaysRelevantButton;

	/**
	 * Default constructor.
	 *
	 * @param parentShell
	 *            Shell.
	 */
	public EumDomEventSelectorDialog(Shell parentShell) {
		this(parentShell, null);
	}

	/**
	 * Edit mode constructor. Data will be populated with the given {@link EumDomEventSelector}.
	 *
	 * @param parentShell
	 *            Shell.
	 * @param selector
	 *            context capture to edit
	 */
	public EumDomEventSelectorDialog(Shell parentShell, EumDomEventSelector selector) {
		super(parentShell);
		this.selector = selector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getTitle());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle(getTitle());
		this.setMessage("Define dom event selector", IMessageProvider.INFORMATION);
	}

	/**
	 * Defines dialog title.
	 *
	 * @return Title
	 */
	private String getTitle() {
		if (null != selector) {
			return "Edit DOM Event Selector for End User Monitoring";
		} else {
			return "Add DOM Event Selector for End User Monitoring";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(null != selector);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (null == selector) {
				selector = new EumDomEventSelector();
			}

			selector.setEventsList(eventsText.getText());
			selector.setSelector(selectorText.getText());
			selector.setAttributesToExtractList(attributesText.getText());
			selector.setAlwaysRelevant(alwaysRelevantButton.getSelection());
			selector.setAncestorLevelsToCheck(Integer.parseInt(ancestorLevelsToCheckText.getText()));
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 10;
		main.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gd);

		// events
		Label eventsLabel = new Label(main, SWT.NONE);
		eventsLabel.setText("Events:");

		eventsText = new Text(main, SWT.BORDER);
		eventsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ValidationControlDecoration<Text> eventsValidationDecoration = new ValidationControlDecoration<Text>(eventsText, this) {
			@Override
			protected boolean validate(Text control) {
				return EVENTS_PATTERN.matcher(eventsText.getText()).matches();
			}
		};
		eventsValidationDecoration.setDescriptionText("The events must be a comma-separated list of DOM-events without spaces or '*' as wildcard");
		eventsValidationDecoration.registerListener(SWT.Modify);
		validationControlDecorations.add(eventsValidationDecoration);
		createInfoLabel(main, "Comma-separated list of DOM-events (e.g. 'click') to listen for. Use '*' as wildcard to match any event.");

		// selector
		Label selectorLabel = new Label(main, SWT.NONE);
		selectorLabel.setText("CSS-Selector:");

		selectorText = new Text(main, SWT.BORDER);
		selectorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ValidationControlDecoration<Text> selectorValidationDecoration = new ValidationControlDecoration<Text>(selectorText, null, this) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(selectorText.getText());
			}
		};
		selectorValidationDecoration.setDescriptionText("The CSS-Selector must not be empty");
		selectorValidationDecoration.registerListener(SWT.Modify);
		validationControlDecorations.add(selectorValidationDecoration);

		createInfoLabel(main, "CSS-Selector for filtering on which DOM-elements the given events will be recorded.");

		// attributes to extract
		Label attributesLabel = new Label(main, SWT.NONE);
		attributesLabel.setText("Attributes to Extract:");

		attributesText = new Text(main, SWT.BORDER);
		attributesText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ValidationControlDecoration<Text> attributesValidationDecoration = new ValidationControlDecoration<Text>(attributesText, null, this) {
			@Override
			protected boolean validate(Text control) {
				return ATTRIBUTES_PATTERN.matcher(attributesText.getText()).matches();
			}
		};
		attributesValidationDecoration.setDescriptionText("The field must contain a comma separated list of attributes without spaces");
		attributesValidationDecoration.registerListener(SWT.Modify);
		validationControlDecorations.add(attributesValidationDecoration);

		createInfoLabel(main, "Comma separated list of HTML attributes (e.g. 'id,href').\n When a matching event of the specified ones occurs on an element matching the selector,"
				+ " these attributes will be read from DOM element and stored in the trace.\n This allows for an easy identification of the element. A name prefix maybe added with a dot as separator.");

		// ancestor levels to check
		Label ancestorLevelsLabel = new Label(main, SWT.NONE);
		ancestorLevelsLabel.setText("Levels of ancestors to check:");
		ancestorLevelsToCheckText = new Text(main, SWT.BORDER);
		ancestorLevelsToCheckText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createInfoLabel(main,
				"When an event occurs, the CSS matching starts at the most inner element in the dom tree where the event occured. \n"
						+ "This options how many levels of ancestors of this element are checked until a match is found.\n"
						+ "A value of 0 means no ancestors are considered, 1 means only the direct parent ischecked, ...\n"
						+ "The special value '-1' means that all ancestors up to the root are checked until a match is found.");
		ValidationControlDecoration<Text> ancestorLevelsValidationDecoration = new ValidationControlDecoration<Text>(ancestorLevelsToCheckText, null, this) {
			@Override
			protected boolean validate(Text control) {
				return ANCESTORS_PATTERN.matcher(ancestorLevelsToCheckText.getText()).matches();
			}
		};
		ancestorLevelsValidationDecoration.setDescriptionText("This field must contain a numeric value which is either -1 or zero and greater.");
		ancestorLevelsValidationDecoration.registerListener(SWT.Modify);
		validationControlDecorations.add(ancestorLevelsValidationDecoration);

		// always-relevant
		alwaysRelevantButton = new Button(main, SWT.CHECK);
		alwaysRelevantButton.setSelection(false);
		alwaysRelevantButton.setText("Always Relevant");
		alwaysRelevantButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		// always relevant together with wildcard event are not allowed
		ValidationControlDecoration<Text> wildcardEventValidationDecoration = new ValidationControlDecoration<Text>(eventsText, this) {
			@Override
			protected boolean validate(Text control) {
				return !alwaysRelevantButton.getSelection() || !"*".equals(eventsText.getText());
			}
		};
		wildcardEventValidationDecoration.setDescriptionText("When specifying the selector as always relevant a concrete list of events must be given instead of the '*' wildcard");
		wildcardEventValidationDecoration.registerListener(alwaysRelevantButton, SWT.Selection);
		wildcardEventValidationDecoration.registerListener(eventsText, SWT.Modify);
		validationControlDecorations.add(wildcardEventValidationDecoration);

		// consider bubbling


		if (null != selector) {
			eventsText.setText(selector.getEventsList());
			selectorText.setText(selector.getSelector());
			attributesText.setText(selector.getAttributesToExtractList());
			alwaysRelevantButton.setSelection(selector.isAlwaysRelevant());
			ancestorLevelsToCheckText.setText(String.valueOf(selector.getAncestorLevelsToCheck()));
		}

		return main;
	}

	/**
	 * Gets {@link #selector}.
	 *
	 * @return {@link #selector}
	 */
	public EumDomEventSelector getSelector() {
		return this.selector;
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
