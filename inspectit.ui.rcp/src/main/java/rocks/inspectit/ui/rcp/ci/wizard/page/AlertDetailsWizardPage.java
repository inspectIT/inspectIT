package rocks.inspectit.ui.rcp.ci.wizard.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.all.util.StringUtils;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;

/**
 * Wizard Page for the definition of the alerting details.
 *
 * @author Alexander Wert
 *
 */
public class AlertDetailsWizardPage extends WizardPage {
	/**
	 * Description text for the email addresses input field.
	 */
	private static final String EMAIL_INFO_TEXT = "Specify email addresses to which notifications about an alert shall be sent.\n" + "Enter one address per line in the text box.";

	/**
	 * Description text for the time range input field.
	 */
	private static final String TIMERANGE_INFO_TEXT = "Specify the interval in minutes for checking this rule.\n\n" + "Note:\nIf this rule applies to a duration metric\n"
			+ "(e.g. business transaction or method response times),\n" + "the interval value should be of magnitutes higher (10x to 100x)\n" + "than the typical response times of your metric!\n"
			+ "Otherwise, the alerting engine may miss violations that take too long.";
	/**
	 * Description text for the threshold input fields.
	 */
	private static final String THRESHOLD_INFO_TEXT = "Specify the alerting threshold.\n" + "The unit of the threshold is determined by the previous\n"
			+ "selection of the metric (measurement + field).\n" + "Specify with the checkbox whether the given threshold is a lower or upper threshold.";

	/**
	 * Title of the wizard page.
	 */
	private static final String TITLE = "Alert Threshold";

	/**
	 * Default message of the wizard page.
	 */

	private static final String DEFAULT_MESSAGE = "Define the threshold and check interval for the new alert definition.";

	/**
	 * Default value for the time range / check interval.
	 */
	private static final int DEFAULT_TIMERANGE = 5;

	/**
	 * Number of layout columns in the main composite of this page.
	 */
	private static final int NUM_LAYOUT_COLUMNS = 4;

	/**
	 * Initial threshold value (used for editing mode).
	 */
	private Double initialThreshold;

	/**
	 * Initial indicator whether the threshold is used as lower threshold.
	 */
	private boolean initialLowerThreshold;

	/**
	 * Initial time range value (used for editing mode).
	 */
	private int initialTimerange;

	/**
	 * Initial list of e-mail addresses (used for editing mode).
	 */
	private List<String> initialsEmails;

	/**
	 * Input field for the threshold value.
	 */
	private Text thresholdBox;

	/**
	 * Input Spinner for the time range value in minutes.
	 */
	private Spinner timerangeSpinner;

	/**
	 * Input field for email addresses.
	 */
	private StyledText emailsBox;

	/**
	 * Checkbox for the selection of the threshold type (lower vs. upper).
	 */
	private Button lowerThresholdCheckBox;

	/**
	 * Default Constructor.
	 *
	 * To be used for creation mode.
	 */
	public AlertDetailsWizardPage() {
		this(null, false, DEFAULT_TIMERANGE, null);
	}

	/**
	 * Constructor.
	 *
	 * To be used for editing mode.
	 *
	 * @param initialThreshold
	 *            Initial threshold value (used for editing mode).
	 * @param initialLowerThreshold
	 *            Initial indicator whether the threshold is used as lower threshold.
	 * @param initialTimerange
	 *            Initial time range value (used for editing mode).
	 * @param initialsEmails
	 *            Initial list of e-mail addresses (used for editing mode).
	 */
	public AlertDetailsWizardPage(Double initialThreshold, boolean initialLowerThreshold, int initialTimerange, List<String> initialsEmails) {
		super(TITLE);
		setTitle(TITLE);
		setMessage(DEFAULT_MESSAGE);
		this.initialThreshold = initialThreshold;
		this.initialTimerange = initialTimerange;
		this.initialsEmails = initialsEmails;
		this.initialLowerThreshold = initialLowerThreshold;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		// create main composite
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(NUM_LAYOUT_COLUMNS, false));

		// create threshold controls
		Label thresholdLabel = new Label(main, SWT.LEFT);
		thresholdLabel.setText("Alert threshold:");
		thresholdLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		thresholdBox = new Text(main, SWT.BORDER);
		thresholdBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// create threshold type controls
		lowerThresholdCheckBox = new Button(main, SWT.CHECK);
		lowerThresholdCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, NUM_LAYOUT_COLUMNS - 3, 1));
		lowerThresholdCheckBox.setText("Use as lower threshold");
		Label infoLabelThreshold = new Label(main, SWT.RIGHT);
		infoLabelThreshold.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelThreshold.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelThreshold.setToolTipText(THRESHOLD_INFO_TEXT);

		// create time range controls
		Label timerangeLabel = new Label(main, SWT.LEFT);
		timerangeLabel.setText("Check interval [min]:");
		timerangeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		timerangeSpinner = new Spinner(main, SWT.BORDER);
		timerangeSpinner.setMinimum(1);
		timerangeSpinner.setMaximum(Integer.MAX_VALUE);
		timerangeSpinner.setIncrement(1);
		timerangeSpinner.setPageIncrement(10);
		timerangeSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, NUM_LAYOUT_COLUMNS - 2, 1));
		Label infoLabelTimerange = new Label(main, SWT.RIGHT);
		infoLabelTimerange.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabelTimerange.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabelTimerange.setToolTipText(TIMERANGE_INFO_TEXT);

		// create email addresses controls
		Label emailsLabel = new Label(main, SWT.LEFT);
		emailsLabel.setText("Send alerts to the following e-mail addresses:");
		emailsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, NUM_LAYOUT_COLUMNS - 1, 1));
		Label infoLabel = new Label(main, SWT.RIGHT);
		infoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(EMAIL_INFO_TEXT);
		emailsBox = new StyledText(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		emailsBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, NUM_LAYOUT_COLUMNS, 1));

		setupListeners();
		initContents();
		setControl(main);
	}

	/**
	 * Sets the message based on the page contents.
	 */
	protected void setPageMessage() {
		if (thresholdBox.getText().isEmpty()) {
			setMessage("Threshold must not be empty!", ERROR);
			return;
		}

		if (timerangeSpinner.getText().isEmpty()) {
			setMessage("Check interval must not be empty!", ERROR);
			return;
		}
		Pair<Integer, String> emailsErrorMessage = checkEmailText();
		if (null != emailsErrorMessage) {
			setMessage("The email address '" + emailsErrorMessage.getSecond() + "' in line " + emailsErrorMessage.getFirst() + " is not valid!", ERROR);
			return;
		}
		setMessage(DEFAULT_MESSAGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		return !thresholdBox.getText().isEmpty() && !timerangeSpinner.getText().isEmpty() && (checkEmailText() == null);
	}

	/**
	 * Sets up control listeners.
	 */
	private void setupListeners() {
		thresholdBox.addVerifyListener(new VerifyListener() {
			// verifies whether input is a valid number
			@Override
			public void verifyText(VerifyEvent event) {
				Text text = (Text) event.getSource();

				final String previousText = text.getText();
				String newText = previousText.substring(0, event.start) + event.text + previousText.substring(event.end);

				if (!newText.isEmpty() && !NumberUtils.isNumber(newText)) {
					event.doit = false;
				}
			}
		});

		emailsBox.addLineStyleListener(new LineStyleListener() {
			@Override
			public void lineGetStyle(LineStyleEvent event) {
				StyleRange range = new StyleRange();
				range.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
				int maxNumLine = emailsBox.getLineCount();
				int bulletLength = Integer.toString(maxNumLine).length();
				// right padding
				int widthBullet = ((bulletLength + 1) * emailsBox.getLineHeight()) / 2;
				range.metrics = new GlyphMetrics(0, 0, widthBullet);
				event.bullet = new Bullet(ST.BULLET_TEXT, range);
				event.bullet.text = String.format("%" + bulletLength + "s", emailsBox.getLineAtOffset(event.lineOffset) + 1);
			}
		});
		emailsBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// redraw line numbers
				emailsBox.redraw();
			}
		});

		Listener pageCompletionListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				setPageMessage();
			}
		};

		thresholdBox.addListener(SWT.Modify, pageCompletionListener);
		timerangeSpinner.addListener(SWT.Modify, pageCompletionListener);
		emailsBox.addListener(SWT.Modify, pageCompletionListener);
	}

	/**
	 * Initializes the contents of all fields if there are initial values.
	 */
	private void initContents() {
		if (null != initialThreshold) {
			thresholdBox.setText(String.valueOf(initialThreshold));
		}

		lowerThresholdCheckBox.setSelection(initialLowerThreshold);
		timerangeSpinner.setSelection(initialTimerange);

		if (null != initialsEmails) {
			String emailsText = "";
			for (String email : initialsEmails) {
				if (!emailsText.isEmpty()) {
					emailsText += System.getProperty("line.separator");
				}
				emailsText += email;
			}
			emailsBox.setText(emailsText);
		}
	}

	/**
	 * Validates the content of the email addresses input field checking whether the e-mails have a
	 * correct syntax.
	 *
	 * @return Returns a integer-string pair indicating the line number and e-mail address that is
	 *         not correct. If all e-mail addresses have a correct syntax, then this method returns
	 *         <code>null</code>.
	 */
	private Pair<Integer, String> checkEmailText() {
		String emailText = emailsBox.getText();
		if (emailText.isEmpty()) {
			return null;
		}
		String[] emails = emailText.split(System.getProperty("line.separator"));
		for (int i = 0; i < emails.length; i++) {
			String address = emails[i].trim();
			if (!address.isEmpty() && !StringUtils.isValidEmailAddress(address)) {
				return new Pair<Integer, String>(i + 1, address);
			}
		}

		return null;
	}

	/**
	 * Returns the specified threshold value.
	 *
	 * @return Returns the specified threshold value.
	 */
	public double getThreshold() {
		return Double.parseDouble(thresholdBox.getText());
	}

	/**
	 * Returns the specified threshold type.
	 *
	 * @return Returns the specified threshold type.
	 */
	public ThresholdType getThresholdType() {
		return lowerThresholdCheckBox.getSelection() ? ThresholdType.LOWER_THRESHOLD : ThresholdType.UPPER_THRESHOLD;
	}

	/**
	 * Returns the specified time range value.
	 *
	 * @return Returns the specified time range value.
	 */
	public int getTimerange() {
		return Integer.parseInt(timerangeSpinner.getText());
	}

	/**
	 * Returns the specified email addresses.
	 *
	 * @return Returns the specified email addresses.
	 */
	public List<String> getEmailAddresses() {
		String[] array = emailsBox.getText().split(System.getProperty("line.separator"));
		List<String> emailAddresses = new ArrayList<>();
		for (String element : array) {
			String address = element.trim();
			if (!address.isEmpty()) {
				emailAddresses.add(element.trim());
			}
		}
		return emailAddresses;
	}
}
