package rocks.inspectit.ui.rcp.editor.text.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.mutable.MutableDouble;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.formatter.ColorFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;

/**
 * The small summary below the SQL invocation overview.
 *
 * @author Ivan Senic
 *
 */
public class SqlInvocSummaryTextInputController extends AbstractTextInputController {

	/**
	 * Constant for the slowest80/20 color id.
	 */
	private static final String SLOWEST8020_COLOR = "slowest8020Color";

	/**
	 * Link for slowest 80% sqls.
	 */
	private static final String SLOWEST80_LINK = "slowest80Link";

	/**
	 * Link for slowest 20% sqls.
	 */
	private static final String SLOWEST20_LINK = "slowest20Link";

	/**
	 * Slowest 80/20 string.
	 */
	private static final String SLOWEST_80_20 = "Slowest 80%/20%:";

	/**
	 * SQLs duration in invocation string.
	 */
	private static final String SQLS_DURATION_IN_INVOCATION = "SQLs duration in invocation:";

	/**
	 * Total duration string.
	 */
	private static final String TOTAL_DURATION = "Total duration:";

	/**
	 * Total SQLs string.
	 */
	private static final String TOTAL_SQLS = "Total SQLs:";

	/**
	 * Reset link to be added to the slowest 80/20 count when selection is active.
	 */
	private static final String RESET_LINK = "<a href=\"reset\">[RESET]</a>";

	/**
	 * System {@link SWT#COLOR_DARK_GREEN} color.
	 */
	private static final RGB GREEN_RGB;

	/**
	 * System {@link SWT#COLOR_DARK_YELLOW} color.
	 */
	private static final RGB YELLOW_RGB;

	/**
	 * System {@link SWT#COLOR_RED} color.
	 */
	private static final RGB RED_RGB;

	/**
	 * Local resource manager for color creation.
	 */
	private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * Total count SQL field.
	 */
	private FormText totalSql;

	/**
	 * Total SQL duration field.
	 */
	private FormText totalDuration;

	/**
	 * Percentage in invocation.
	 */
	private FormText percentageOfDuration;

	/**
	 * Slowest 80%/20% count.
	 */
	private FormText slowestCount;

	/**
	 * HYperlink settings so that we can change the link color.
	 */
	private HyperlinkSettings slowestHyperlinkSettings;

	/**
	 * List that will be passed to display slowest 80%.
	 */
	private Collection<SqlStatementData> slowest80List = new ArrayList<>();

	/**
	 * List that will be passed to display slowest 20%.
	 */
	private Collection<SqlStatementData> slowest20List = new ArrayList<>();

	/**
	 * Keep the source invocations.
	 */
	private List<InvocationSequenceData> sourceInvocations;

	/**
	 * Content displayed in slowest 80/20 without the link.
	 */
	private String slowestContent;

	/**
	 * If reset link is displayed.
	 */
	private boolean resetDisplayed;

	static {
		Display display = Display.getDefault();
		GREEN_RGB = display.getSystemColor(SWT.COLOR_DARK_GREEN).getRGB();
		YELLOW_RGB = display.getSystemColor(SWT.COLOR_DARK_YELLOW).getRGB();
		RED_RGB = display.getSystemColor(SWT.COLOR_RED).getRGB();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		main = toolkit.createComposite(parent, SWT.BORDER);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout gl = new GridLayout(8, false);
		main.setLayout(gl);

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));

		totalSql = toolkit.createFormText(main, false);
		totalSql.setToolTipText("Total amount of SQL Statements executed in the invocation");
		totalSql.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TIME));

		totalDuration = toolkit.createFormText(main, false);
		totalDuration.setToolTipText("Duration sum of all SQL Statements executed in the invocation");
		totalDuration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INVOCATION));

		percentageOfDuration = toolkit.createFormText(main, false);
		percentageOfDuration.setToolTipText("Percentage of the time spent in the invocation on SQL Statements execution");
		percentageOfDuration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HELP));

		slowestCount = toolkit.createFormText(main, false);
		slowestCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		slowestCount.setToolTipText("Amount of slowest SQL Statements that take 80%/20% time of total SQL execution duration");

		// remove left and right margins from the parent
		Layout parentLayout = parent.getLayout();
		if (parentLayout instanceof GridLayout) {
			((GridLayout) parentLayout).marginWidth = 0;
			((GridLayout) parentLayout).marginHeight = 0;
		}

		setDefaultText();

		slowestHyperlinkSettings = new HyperlinkSettings(parent.getDisplay());
		slowestHyperlinkSettings.setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		slowestCount.setHyperlinkSettings(slowestHyperlinkSettings);
		slowestCount.addHyperlinkListener(getHyperlinkAdapter());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setDataInput(List<? extends Object> data) {
		if (CollectionUtils.isNotEmpty(data)) {
			Object defaultData = data.get(0);
			if (defaultData instanceof InvocationSequenceData) {
				updateRepresentation((List<InvocationSequenceData>) data);
			}
		} else {
			setDefaultText();
		}
	}

	/**
	 * Updates the representation of the text form.
	 *
	 * @param invocations
	 *            Invocations to display.
	 */
	@SuppressWarnings("unchecked")
	private void updateRepresentation(List<InvocationSequenceData> invocations) {
		sourceInvocations = invocations;
		resetDisplayed = false;

		MutableDouble duration = new MutableDouble(0d);
		List<SqlStatementData> sqlList = new ArrayList<>();
		InvocationSequenceDataHelper.collectSqlsInInvocations(invocations, sqlList, duration);
		double totalInvocationsDuration = 0d;
		for (InvocationSequenceData inv : invocations) {
			totalInvocationsDuration += inv.getDuration();
		}
		double percentage = (duration.toDouble() / totalInvocationsDuration) * 100;

		slowest80List.clear();
		int slowest80 = getSlowestSqlCount(duration.toDouble(), sqlList, 0.8d, slowest80List);
		int slowest20 = sqlList.size() - slowest80;
		slowest20List = CollectionUtils.subtract(sqlList, slowest80List);

		totalSql.setText("<form><p><b>" + TOTAL_SQLS + "</b> " + sqlList.size() + "</p></form>", true, false);
		totalDuration.setText("<form><p><b>" + TOTAL_DURATION + "</b> " + NumberFormatter.formatDouble(duration.doubleValue()) + " ms</p></form>", true, false);

		String formatedPercentage = NumberFormatter.formatDouble(percentage, 1);
		if (CollectionUtils.isNotEmpty(sqlList)) {
			Color durationInInvocationColor = ColorFormatter.getPerformanceColor(GREEN_RGB, YELLOW_RGB, RED_RGB, percentage, 20d, 80d, resourceManager);
			percentageOfDuration.setColor("durationInInvocationColor", durationInInvocationColor);
			percentageOfDuration.setText("<form><p><b>" + SQLS_DURATION_IN_INVOCATION + "</b> <span color=\"durationInInvocationColor\">" + formatedPercentage + "%</span></p></form>", true, false);
		} else {
			percentageOfDuration.setText("<form><p><b>" + SQLS_DURATION_IN_INVOCATION + "</b> " + formatedPercentage + "%</p></form>", true, false);
		}

		String slowest80String = getCountAndPercentage(slowest80, sqlList.size());
		String slowest20String = getCountAndPercentage(slowest20, sqlList.size());
		if (CollectionUtils.isNotEmpty(sqlList)) {
			double slowest80Percentage = ((double) slowest80 / sqlList.size()) * 100;
			if (Double.isNaN(slowest80Percentage)) {
				slowest80Percentage = 0;
			}
			Color color8020 = ColorFormatter.getPerformanceColor(GREEN_RGB, YELLOW_RGB, RED_RGB, slowest80Percentage, 70d, 10d, resourceManager);
			slowestCount.setColor(SLOWEST8020_COLOR, color8020);
			slowestHyperlinkSettings.setForeground(color8020);

			StringBuilder text = new StringBuilder("<b>" + SLOWEST_80_20 + "</b> ");
			if (slowest80 > 0) {
				text.append("<a href=\"" + SLOWEST80_LINK + "\">" + slowest80String + "</a>");
			} else {
				text.append("<span color=\"" + SLOWEST8020_COLOR + "\">" + slowest80String + "</span>");
			}
			text.append(" / ");
			if (slowest20 > 0) {
				text.append("<a href=\"" + SLOWEST20_LINK + "\">" + slowest20String + "</a>");
			} else {
				text.append("<span color=\"" + SLOWEST8020_COLOR + "\">" + slowest20String + "</span>");
			}
			slowestContent = text.toString();
		} else {
			slowestContent = "<b>" + SLOWEST_80_20 + "</b> " + slowest80String + " / " + slowest20String;

		}
		slowestCount.setText("<form><p>" + slowestContent + "</p></form>", true, false);

		main.layout();
	}

	/**
	 * Returns the {@link HyperlinkAdapter} to handle the Hyperlink clicks.
	 *
	 * @return Returns the {@link HyperlinkAdapter} to handle the Hyperlink clicks.
	 */
	private HyperlinkAdapter getHyperlinkAdapter() {
		return new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();
						IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
						if (SLOWEST80_LINK.equals(e.getHref())) {
							rootEditor.setDataInput(new ArrayList<>(slowest80List));
							showResetFor8020(true);
						} else if (SLOWEST20_LINK.equals(e.getHref())) {
							rootEditor.setDataInput(new ArrayList<>(slowest20List));
							showResetFor8020(true);
						} else {
							rootEditor.setDataInput(sourceInvocations);
							showResetFor8020(false);
						}
					}
				});
			}
		};
	}

	/**
	 * Define if reset button should be displayed in the 80/20 test.
	 *
	 * @param show
	 *            If <code>true</code> reset link will be show, otherwise hidden.
	 */
	private void showResetFor8020(boolean show) {
		if (show && !resetDisplayed) {
			resetDisplayed = true;
			slowestCount.setText("<form><p>" + slowestContent + " " + RESET_LINK + "</p></form>", true, false);
		} else if (!show && resetDisplayed) {
			resetDisplayed = false;
			slowestCount.setText("<form><p>" + slowestContent + "</p></form>", true, false);
		}
	}

	/**
	 * Sets default text that has no informations displayed.
	 */
	private void setDefaultText() {
		resetDisplayed = false;
		totalSql.setText("<form><p><b>" + TOTAL_SQLS + "</b></p></form>", true, false);
		totalDuration.setText("<form><p><b>" + TOTAL_DURATION + "</b></p></form>", true, false);
		percentageOfDuration.setText("<form><p><b>" + SQLS_DURATION_IN_INVOCATION + "</b></p></form>", true, false);
		slowestCount.setText("<form><p><b>" + SLOWEST_80_20 + "</b></p></form>", true, false);
	}

	/**
	 * Returns string representation of count and percentage.
	 *
	 * @param count
	 *            Count.
	 * @param totalCount
	 *            Total count.
	 * @return {@link String} representation.
	 */
	private String getCountAndPercentage(int count, int totalCount) {
		if (0 == totalCount) {
			return "0(0%)";
		}
		return count + "(" + NumberFormatter.formatDouble(((double) count / totalCount) * 100, 0) + "%)";
	}

	/**
	 * Calculates how much slowest SQL can fit into the given percentage of total duration.
	 *
	 * @param totalDuration
	 *            Total duration of all SQLs.
	 * @param sqlStatementDataList
	 *            List of SQL. Note that there is a side effect of list sorting.
	 * @param percentage
	 *            Wanted percentages to be calculated.
	 * @param resultList
	 *            List to add the resulting statements to.
	 * @return Return the count of SQL.
	 */
	private int getSlowestSqlCount(double totalDuration, List<SqlStatementData> sqlStatementDataList, double percentage, Collection<SqlStatementData> resultList) {
		// sort first
		Collections.sort(sqlStatementDataList, new Comparator<SqlStatementData>() {
			@Override
			public int compare(SqlStatementData o1, SqlStatementData o2) {
				return ObjectUtils.compare(o2.getDuration(), o1.getDuration());
			}
		});

		int result = 0;
		double currentDurationSum = 0;
		for (SqlStatementData sqlStatementData : sqlStatementDataList) {
			if ((currentDurationSum / totalDuration) < percentage) {
				result++;
				resultList.add(sqlStatementData);
			} else {
				break;
			}
			currentDurationSum += sqlStatementData.getDuration();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		resourceManager.dispose();
		super.dispose();
	}

}
