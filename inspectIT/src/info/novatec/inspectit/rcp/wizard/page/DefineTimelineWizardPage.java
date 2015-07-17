package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.processor.impl.TimeFrameDataProcessor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page where user can limit the time frame for the storage write/record. The time frame can
 * be in past or future, depending on the {@link #timeStyle} value.
 * 
 * @author Ivan Senic
 * 
 */
public class DefineTimelineWizardPage extends WizardPage {

	/**
	 * Time style value for the selecting future time.
	 */
	public static final int FUTURE = 1;

	/**
	 * Time style value for the selecting past time.
	 */
	public static final int PAST = 2;

	/**
	 * Style that enables the selection of both dates.
	 */
	public static final int BOTH_DATES = 4;

	/**
	 * Map of the time characters associated to the multiplier of a second.
	 */
	private static final Map<Character, Integer> PERIOD_MULTIPLIERS_MAP = new HashMap<Character, Integer>(4);

	static {
		PERIOD_MULTIPLIERS_MAP.put('m', 1);
		PERIOD_MULTIPLIERS_MAP.put('h', 60);
		PERIOD_MULTIPLIERS_MAP.put('d', 60 * 24);
		PERIOD_MULTIPLIERS_MAP.put('w', 60 * 24 * 7);
	}

	/**
	 * Time style. The style is defining if the page will display the future selection or past time.
	 * 
	 * @see DefineTimelineWizardPage#FUTURE
	 * @see DefineTimelineWizardPage#PAST
	 */
	private int timeStyle;

	/** button for use time frame dividing. */
	private Button useTimeframe;
	/** button to the define the period. */
	private Button definePeriod;
	/** button to define the date. */
	private Button defineDate;
	/** the primary date. */
	private CDateTime cdtPrimary;
	/** the secondary date. */
	private CDateTime cdtSecondary;
	/** the period. */
	private Text periodAmount;
	/** the group. */
	private Group group;
	/** the main composite. */
	private Composite main;
	/** the composite holding the period. */
	private Composite periodComposite;
	/** the composite holding the dates. */
	private Composite cdtComposite;

	/**
	 * Listener for the page completeness.
	 */
	private Listener pageCompleteListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			setPageComplete(isPageComplete());
		}
	};

	/**
	 * Default message of view.
	 */
	private String defaultMessage;

	/**
	 * Default constructor.
	 * 
	 * @param pageName
	 *            Page name.
	 * @param defaultMessage
	 *            Default message displayed to the user on the page.
	 * @param timeStyle
	 *            Time style. The style is defining if the page will display the future selection or
	 *            past time.
	 * @see DefineTimelineWizardPage#FUTURE
	 * @see DefineTimelineWizardPage#PAST
	 */
	public DefineTimelineWizardPage(String pageName, String defaultMessage, int timeStyle) {
		super(pageName);
		setTitle(pageName);
		setMessage(defaultMessage);
		this.timeStyle = timeStyle;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		useTimeframe = new Button(main, SWT.CHECK);
		useTimeframe.setText("Use timeframe limiting");
		useTimeframe.setSelection(false);

		group = new Group(main, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(1, false));

		definePeriod = new Button(group, SWT.RADIO);
		definePeriod.setText("Enter wanted time period");
		definePeriod.setSelection(true);

		defineDate = new Button(group, SWT.RADIO);
		defineDate.setText("Selected exact date");
		if ((timeStyle & BOTH_DATES) != 0) {
			defineDate.setText(defineDate.getText() + "s");
		}
		defineDate.setSelection(false);

		useTimeframe.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (useTimeframe.getSelection()) {
					enableControlAndChildren(group, true);
				} else {
					enableControlAndChildren(group, false);
				}
			}
		});

		SelectionAdapter changeWayListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePage();
			}
		};

		defineDate.addSelectionListener(changeWayListener);
		definePeriod.addSelectionListener(changeWayListener);

		useTimeframe.addListener(SWT.Selection, pageCompleteListener);
		defineDate.addListener(SWT.Selection, pageCompleteListener);
		definePeriod.addListener(SWT.Selection, pageCompleteListener);

		updatePage();

		enableControlAndChildren(group, false);

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (useTimeframe.getSelection()) {
			if (definePeriod.getSelection()) {
				Date date = getDateFromPeriodComposite();
				if (null == date) {
					setMessage("Time period has to be entered correctly", ERROR);
					return false;
				}
			} else {
				if (null == this.getFromDate()) {
					setMessage("Please enter starting date & time", ERROR);
					return false;
				} else if (null == this.getToDate()) {
					setMessage("Please enter ending date & time", ERROR);
					return false;
				} else if (this.getFromDate().after(this.getToDate())) {
					if ((timeStyle & BOTH_DATES) != 0) {
						setMessage("Start date must be before end date", ERROR);
					} else {
						setMessage("Entered date must be a future date", ERROR);
					}
					return false;
				}
			}
		}
		setMessage(defaultMessage);
		return true;
	}

	/**
	 * @return If the time frame is used.
	 */
	public boolean isTimerframeUsed() {
		return useTimeframe.getSelection();
	}

	/**
	 * Returns the properly initialized {@link TimeFrameDataProcessor}.
	 * 
	 * @param chainedProcessors
	 *            Processors that need to be chained to {@link TimeFrameDataProcessor}.
	 * @return {@link TimeFrameDataProcessor}
	 * @see {AbstractChainedDataProcessor}
	 */
	public TimeFrameDataProcessor getTimeFrameDataProcessor(Collection<AbstractDataProcessor> chainedProcessors) {
		List<AbstractDataProcessor> normalProcessors = new ArrayList<AbstractDataProcessor>(chainedProcessors);
		Date fromDate = getFromDate();
		Date toDate = getToDate();
		TimeFrameDataProcessor timeFrameDataProcessor = new TimeFrameDataProcessor(fromDate, toDate, normalProcessors);
		return timeFrameDataProcessor;
	}

	/**
	 * @return Returns from date. Note that if time style is future, here the current date will be
	 *         returned.
	 */
	public Date getFromDate() {
		if ((timeStyle & FUTURE) != 0) {
			if ((timeStyle & BOTH_DATES) != 0) {
				return (null != cdtPrimary) ? cdtPrimary.getSelection() : null; // NOPMD
			} else {
				return new Date();
			}
		} else {
			if (definePeriod.getSelection()) {
				return getDateFromPeriodComposite();
			} else {
				return (null != cdtPrimary) ? cdtPrimary.getSelection() : null; // NOPMD
			}
		}
	}

	/**
	 * @return Returns from date. Note that if time style is past, here the current date will be
	 *         returned.
	 */
	public Date getToDate() {
		if ((timeStyle & FUTURE) != 0) {
			if (definePeriod.getSelection()) {
				return getDateFromPeriodComposite();
			} else if ((timeStyle & BOTH_DATES) != 0) {
				return (null != cdtSecondary) ? cdtSecondary.getSelection() : null; // NOPMD
			} else {
				return (null != cdtPrimary) ? cdtPrimary.getSelection() : null; // NOPMD
			}
		} else {
			if ((timeStyle & BOTH_DATES) != 0) {
				return (null != cdtSecondary) ? cdtSecondary.getSelection() : null; // NOPMD
			} else {
				return new Date();
			}
		}
	}

	/**
	 * Enables or disables composite and all its children.
	 * 
	 * @param composite
	 *            Composite to enable.
	 * @param enabled
	 *            True for enabling, false for disabling.
	 */
	private void enableControlAndChildren(Composite composite, boolean enabled) {
		composite.setEnabled(enabled);
		for (Control child : composite.getChildren()) {
			child.setEnabled(enabled);
			if (child instanceof Composite) {
				enableControlAndChildren((Composite) child, enabled);
			}
		}
	}

	/**
	 * Updates the widgets on the page based on the selection.
	 */
	private void updatePage() {
		if (null != cdtComposite && !cdtComposite.isDisposed()) {
			cdtComposite.dispose();
		}
		if (null != periodComposite && !periodComposite.isDisposed()) {
			periodComposite.dispose();
		}

		if (definePeriod.getSelection()) {
			periodComposite = new Composite(group, SWT.NONE);
			periodComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			periodComposite.setLayout(new GridLayout(3, false));

			Label text = new Label(periodComposite, SWT.NONE);

			periodAmount = new Text(periodComposite, SWT.SINGLE | SWT.BORDER);
			periodAmount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			periodAmount.addListener(SWT.Modify, pageCompleteListener);
			periodAmount.setFocus();

			new Label(periodComposite, SWT.NONE).setText("(ex. 2w 4d 12h 30m)");

			if ((timeStyle & FUTURE) != 0) {
				text.setText("Next:");
			} else {
				text.setText("Previous:");
			}
		} else {
			cdtComposite = new Composite(group, SWT.NONE);
			cdtComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			cdtComposite.setLayout(new GridLayout(1, true));

			cdtPrimary = new CDateTime(cdtComposite, CDT.BORDER | CDT.DROP_DOWN | CDT.TAB_FIELDS);
			cdtPrimary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			cdtPrimary.addListener(SWT.Modify, pageCompleteListener);

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if ((timeStyle & FUTURE) != 0 && (timeStyle & BOTH_DATES) == 0) {
				cdtPrimary.setPattern("'Till\t\t' EEEE, MMMM d YYYY '@' h:mm a");
				calendar.add(Calendar.HOUR, 1);
			} else {
				cdtPrimary.setPattern("'From\t' EEEE, MMMM d YYYY '@' h:mm a");
			}
			cdtPrimary.setSelection(calendar.getTime());

			if ((timeStyle & BOTH_DATES) != 0) {
				cdtSecondary = new CDateTime(cdtComposite, CDT.BORDER | CDT.DROP_DOWN | CDT.TAB_FIELDS);
				cdtSecondary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				calendar.add(Calendar.HOUR, 1);
				cdtSecondary.setSelection(calendar.getTime());
				cdtSecondary.addListener(SWT.Modify, pageCompleteListener);
				cdtSecondary.setPattern("'Till\t\t' EEEE, MMMM d YYYY '@' h:mm a");
			}
		}

		group.layout();
		main.layout();
	}

	/**
	 * @return Gets date from the date composite.
	 */
	private Date getDateFromPeriodComposite() {
		try {
			Calendar cal = Calendar.getInstance();
			String periodString = periodAmount.getText().trim();
			StringTokenizer tokenizer = new StringTokenizer(periodString, " ");
			int perAmountInMinutes = 0;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				Character timeChar = token.charAt(token.length() - 1);
				Integer timeMultiplayer = PERIOD_MULTIPLIERS_MAP.get(timeChar);
				if (null != timeMultiplayer) {
					try {
						int value = Integer.parseInt(token.substring(0, token.length() - 1));
						if (value > 0) {
							perAmountInMinutes += value * timeMultiplayer.intValue();
						}
					} catch (Exception e) {
						continue;
					}
				}
			}

			if (perAmountInMinutes > 0) {
				if ((timeStyle & FUTURE) != 0) {
					cal.add(Calendar.MINUTE, perAmountInMinutes);
				} else {
					cal.add(Calendar.MINUTE, -(perAmountInMinutes));
				}
				return cal.getTime();
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
}
