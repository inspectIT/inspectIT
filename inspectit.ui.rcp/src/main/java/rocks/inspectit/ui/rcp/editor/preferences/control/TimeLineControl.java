package rocks.inspectit.ui.rcp.editor.preferences.control;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.ui.rcp.editor.preferences.IPreferenceGroup;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferencePanel;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;

/**
 * The time line control for the views that has set of links for fast setting of the time-frame, as
 * well as two date boxes for setting the time-frame directly.
 *
 * @author Ivan Senic
 *
 */
public class TimeLineControl extends AbstractPreferenceControl implements IPreferenceControl, PreferenceEventCallback {

	/**
	 * From date widget.
	 */
	private CDateTime fromDateTime;

	/**
	 * To date widget.
	 */
	private CDateTime toDateTime;

	/**
	 * Old from date.
	 */
	private Date oldToDate;

	/**
	 * Old to date.
	 */
	private Date oldFromDate;

	/**
	 * Main composite in the preference control.
	 */
	private Composite mainComposite;

	/**
	 * Default constructor.
	 *
	 * @param preferencePanel
	 *            Preference panel.
	 */
	public TimeLineControl(IPreferencePanel preferencePanel) {
		super(preferencePanel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PreferenceId getControlGroupId() {
		return PreferenceId.TIMELINE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Composite createControls(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Time Range");
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		mainComposite = toolkit.createComposite(section);
		mainComposite.setLayout(new GridLayout(1, false));
		section.setClient(mainComposite);

		Composite linksComposite = toolkit.createComposite(mainComposite);
		linksComposite.setLayout(new GridLayout(14, false));
		linksComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		toolkit.createLabel(linksComposite, "Last: ");
		createTimeHyperlink(linksComposite, toolkit, "15 minutes", 15 * 60 * 1000L);
		toolkit.createLabel(linksComposite, " | ");
		createTimeHyperlink(linksComposite, toolkit, "1 hour", 60 * 60 * 1000L);
		toolkit.createLabel(linksComposite, " | ");
		createTimeHyperlink(linksComposite, toolkit, "6 hours", 6 * 60 * 60 * 1000L);
		toolkit.createLabel(linksComposite, " | ");
		createTimeHyperlink(linksComposite, toolkit, "12 hours", 12 * 60 * 60 * 1000L);
		toolkit.createLabel(linksComposite, " | ");
		createTimeHyperlink(linksComposite, toolkit, "1 day", 24 * 60 * 60 * 1000L);
		toolkit.createLabel(linksComposite, " | ");
		createTimeHyperlink(linksComposite, toolkit, "7 days", 7 * 24 * 60 * 60 * 1000L);
		toolkit.createLabel(linksComposite, " | ");
		createTimeHyperlink(linksComposite, toolkit, "30 days", 30 * 24 * 60 * 60 * 1000L);

		Composite timeComposite = toolkit.createComposite(mainComposite);
		timeComposite.setLayout(new GridLayout(4, false));
		timeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Date toDate = new Date();
		Date fromDate = new Date(toDate.getTime() - PreferenceId.TimeLine.TIMELINE_DEFAULT);

		toolkit.createLabel(timeComposite, "From: ");
		fromDateTime = new CDateTime(timeComposite, CDT.BORDER | CDT.DROP_DOWN);
		fromDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fromDateTime.setFormat(CDT.DATE_SHORT | CDT.TIME_SHORT);
		fromDateTime.setSelection(fromDate);

		toolkit.createLabel(timeComposite, "To: ");
		toDateTime = new CDateTime(timeComposite, CDT.BORDER | CDT.DROP_DOWN);
		toDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		toDateTime.setFormat(CDT.DATE_SHORT | CDT.TIME_SHORT);
		toDateTime.setSelection(toDate);

		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPreferencePanel().update();
			}
		};
		fromDateTime.addSelectionListener(selectionListener);
		toDateTime.addSelectionListener(selectionListener);

		getPreferencePanel().registerCallback(this);

		return mainComposite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<IPreferenceGroup, Object> eventFired() {
		Map<IPreferenceGroup, Object> preferenceControlMap = new HashMap<>();
		Date toDate = toDateTime.getSelection();
		Date fromDate = fromDateTime.getSelection();

		if ((null == oldToDate) || (oldToDate.getTime() != toDate.getTime())) {
			preferenceControlMap.put(PreferenceId.TimeLine.TO_DATE_ID, toDate);
			oldToDate = new Date(toDate.getTime());
		}
		if ((null == oldFromDate) || (oldFromDate.getTime() != fromDate.getTime())) {
			preferenceControlMap.put(PreferenceId.TimeLine.FROM_DATE_ID, fromDate);
			oldFromDate = new Date(fromDate.getTime());
		}

		return preferenceControlMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		getPreferencePanel().removeCallback(this);
	}

	/**
	 * Creates {@link Hyperlink} that when clicked sets the last specified time to the timeframe
	 * control.
	 *
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param text
	 *            Text on the {@link Hyperlink}.
	 * @param time
	 *            Wanted time frame to set on click.
	 * @return Created {@link Hyperlink}.
	 */
	private Hyperlink createTimeHyperlink(Composite parent, FormToolkit toolkit, String text, final long time) {
		Hyperlink hyperlink = toolkit.createHyperlink(parent, text, SWT.NONE);
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				Date toDate = new Date();
				Date fromDate = new Date(toDate.getTime() - time);
				toDateTime.setSelection(toDate);
				fromDateTime.setSelection(fromDate);
				getPreferencePanel().update();
			}
		});
		return hyperlink;
	}

	/**
	 * Sets control enabled or not.
	 *
	 * @param enabled
	 *            If control is enabled or not.
	 */
	private void setEnabled(boolean enabled) {
		fromDateTime.setEnabled(enabled);
		toDateTime.setEnabled(enabled);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If live is set on we disable this preference.
	 */
	@Override
	public void eventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.LIVEMODE.equals(preferenceEvent.getPreferenceId())) {
			Boolean liveOn = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
			if ((null != liveOn) && liveOn.booleanValue()) {
				setEnabled(false);
			} else {
				setEnabled(true);
			}
		} else if (PreferenceId.TIMELINE.equals(preferenceEvent.getPreferenceId())) {
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDateTime.setSelection((Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.FROM_DATE_ID));
			}
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDateTime.setSelection((Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.TO_DATE_ID));
			}
		}
	}
}
