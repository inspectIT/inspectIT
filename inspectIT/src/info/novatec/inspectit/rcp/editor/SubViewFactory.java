package info.novatec.inspectit.rcp.editor;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.composite.GridCompositeSubView;
import info.novatec.inspectit.rcp.editor.composite.SashCompositeSubView;
import info.novatec.inspectit.rcp.editor.composite.TabbedCompositeSubView;
import info.novatec.inspectit.rcp.editor.graph.GraphSubView;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
import info.novatec.inspectit.rcp.editor.table.input.AggregatedTimerSummaryInputController;
import info.novatec.inspectit.rcp.editor.table.input.ExceptionSensorInvocInputController;
import info.novatec.inspectit.rcp.editor.table.input.GroupedExceptionOverviewInputController;
import info.novatec.inspectit.rcp.editor.table.input.HttpTimerDataInputController;
import info.novatec.inspectit.rcp.editor.table.input.InvocOverviewInputController;
import info.novatec.inspectit.rcp.editor.table.input.MethodInvocInputController;
import info.novatec.inspectit.rcp.editor.table.input.MultiInvocDataInputController;
import info.novatec.inspectit.rcp.editor.table.input.NavigationInvocOverviewInputController;
import info.novatec.inspectit.rcp.editor.table.input.SqlParameterAggregationInputControler;
import info.novatec.inspectit.rcp.editor.table.input.TaggedHttpTimerDataInputController;
import info.novatec.inspectit.rcp.editor.table.input.TimerDataInputController;
import info.novatec.inspectit.rcp.editor.table.input.UngroupedExceptionOverviewInputController;
import info.novatec.inspectit.rcp.editor.text.TextSubView;
import info.novatec.inspectit.rcp.editor.text.input.ClassesInputController;
import info.novatec.inspectit.rcp.editor.text.input.CpuInputController;
import info.novatec.inspectit.rcp.editor.text.input.MemoryInputController;
import info.novatec.inspectit.rcp.editor.text.input.SqlInvocSummaryTextInputController;
import info.novatec.inspectit.rcp.editor.text.input.SqlStatementTextInputController;
import info.novatec.inspectit.rcp.editor.text.input.ThreadsInputController;
import info.novatec.inspectit.rcp.editor.text.input.UngroupedExceptionOverviewStackTraceInputController;
import info.novatec.inspectit.rcp.editor.text.input.VmSummaryInputController;
import info.novatec.inspectit.rcp.editor.tree.SteppingTreeSubView;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;
import info.novatec.inspectit.rcp.editor.tree.input.ExceptionMessagesTreeInputController;
import info.novatec.inspectit.rcp.editor.tree.input.ExceptionTreeInputController;
import info.novatec.inspectit.rcp.editor.tree.input.SqlInputController;
import info.novatec.inspectit.rcp.editor.tree.input.SqlInvocInputController;
import info.novatec.inspectit.rcp.editor.tree.input.SteppingInvocDetailInputController;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

/**
 * The factory for the creation of a {@link ISubView}.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public final class SubViewFactory {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SubViewFactory() {
	}

	/**
	 * Creates a default {@link ISubView} object based on the passed {@link SensorTypeEnum}.
	 * 
	 * @param sensorTypeEnum
	 *            The sensor type on which the default view controller is based on.
	 * @return An instance of a {@link ISubView}.
	 */
	public static ISubView createSubView(SensorTypeEnum sensorTypeEnum) {
		switch (sensorTypeEnum) {
		case AVERAGE_TIMER:
			// same as Timer
		case TIMER:
			SashCompositeSubView timerSashSubView = new SashCompositeSubView();
			timerSashSubView.addSubView(new TableSubView(new TimerDataInputController()));
			return timerSashSubView;
		case CHARTING_TIMER:
			GridCompositeSubView timerSubView = new GridCompositeSubView();
			timerSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			ISubView aggregatedTimerSummarySubView = new TableSubView(new AggregatedTimerSummaryInputController());
			timerSubView.addSubView(aggregatedTimerSummarySubView, new GridData(SWT.FILL, SWT.FILL, true, false));
			return timerSubView;
		case CHARTING_MULTI_TIMER:
			return new GraphSubView(SensorTypeEnum.CHARTING_MULTI_TIMER);
		case CLASSLOADING_INFORMATION:
			GridCompositeSubView classLoadingSubView = new GridCompositeSubView();
			classLoadingSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			classLoadingSubView.addSubView(new TextSubView(new ClassesInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return classLoadingSubView;
		case MEMORY_INFORMATION:
			GridCompositeSubView memorySubView = new GridCompositeSubView();
			memorySubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			memorySubView.addSubView(new TextSubView(new MemoryInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return memorySubView;
		case CPU_INFORMATION:
			GridCompositeSubView cpuSubView = new GridCompositeSubView();
			cpuSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			cpuSubView.addSubView(new TextSubView(new CpuInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return cpuSubView;
		case SYSTEM_INFORMATION:
			return new TextSubView(new VmSummaryInputController());
		case THREAD_INFORMATION:
			GridCompositeSubView threadSubView = new GridCompositeSubView();
			threadSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			threadSubView.addSubView(new TextSubView(new ThreadsInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return threadSubView;
		case INVOCATION_SEQUENCE:
			GridCompositeSubView sqlCombinedView = new GridCompositeSubView();
			ISubView invocSql = new TreeSubView(new SqlInvocInputController());
			ISubView invocSqlSummary = new TextSubView(new SqlInvocSummaryTextInputController());
			sqlCombinedView.addSubView(invocSql, new GridData(SWT.FILL, SWT.FILL, true, true));
			sqlCombinedView.addSubView(invocSqlSummary, new GridData(SWT.FILL, SWT.FILL, true, false));

			TabbedCompositeSubView invocTabbedSubView = new TabbedCompositeSubView();
			ISubView invocDetails = new SteppingTreeSubView(new SteppingInvocDetailInputController(false));
			ISubView invocMethods = new TableSubView(new MethodInvocInputController());
			ISubView invocExceptions = new TableSubView(new ExceptionSensorInvocInputController());
			invocTabbedSubView.addSubView(invocDetails, "Call Hierarchy", InspectIT.getDefault().getImage(InspectITImages.IMG_CALL_HIERARCHY));
			invocTabbedSubView.addSubView(sqlCombinedView, "SQL", InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));
			invocTabbedSubView.addSubView(invocMethods, "Methods", InspectIT.getDefault().getImage(InspectITImages.IMG_METHOD_PUBLIC));
			invocTabbedSubView.addSubView(invocExceptions, "Exceptions", InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR));

			SashCompositeSubView invocSubView = new SashCompositeSubView();
			ISubView invocOverview = new TableSubView(new InvocOverviewInputController());
			invocSubView.addSubView(invocOverview, 1);
			invocSubView.addSubView(invocTabbedSubView, 2);

			return invocSubView;
		case SQL:
			SashCompositeSubView sqlSashSubView = new SashCompositeSubView();
			sqlSashSubView.addSubView(new TreeSubView(new SqlInputController()), 10);
			sqlSashSubView.addSubView(new TableSubView(new SqlParameterAggregationInputControler()), 5);
			sqlSashSubView.addSubView(new TextSubView(new SqlStatementTextInputController()), 1);
			return sqlSashSubView;
		case EXCEPTION_SENSOR:
			SashCompositeSubView ungroupedExceptionSensorSubView = new SashCompositeSubView();
			ISubView ungroupedExceptionOverview = new TableSubView(new UngroupedExceptionOverviewInputController());
			TabbedCompositeSubView exceptionTreeTabbedSubView = new TabbedCompositeSubView();
			ISubView exceptionTree = new TreeSubView(new ExceptionTreeInputController());
			ISubView stackTraceInput = new TextSubView(new UngroupedExceptionOverviewStackTraceInputController());

			exceptionTreeTabbedSubView.addSubView(exceptionTree, "Exception Tree", InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_TREE));
			exceptionTreeTabbedSubView.addSubView(stackTraceInput, "Stack Trace", InspectIT.getDefault().getImage(InspectITImages.IMG_STACKTRACE));

			ungroupedExceptionSensorSubView.addSubView(ungroupedExceptionOverview, 1);
			ungroupedExceptionSensorSubView.addSubView(exceptionTreeTabbedSubView, 2);
			return ungroupedExceptionSensorSubView;
		case EXCEPTION_SENSOR_GROUPED:
			SashCompositeSubView groupedExceptionSensorSubView = new SashCompositeSubView();
			ISubView groupedExceptionOverview = new TableSubView(new GroupedExceptionOverviewInputController());
			ISubView exceptionMessagesTree = new TreeSubView(new ExceptionMessagesTreeInputController());

			groupedExceptionSensorSubView.addSubView(groupedExceptionOverview, 1);
			groupedExceptionSensorSubView.addSubView(exceptionMessagesTree, 2);
			return groupedExceptionSensorSubView;
		case NAVIGATION_INVOCATION:
			GridCompositeSubView sqlCombinedView1 = new GridCompositeSubView();
			ISubView invocSql1 = new TreeSubView(new SqlInvocInputController());
			ISubView invocSqlSummary1 = new TextSubView(new SqlInvocSummaryTextInputController());
			sqlCombinedView1.addSubView(invocSql1, new GridData(SWT.FILL, SWT.FILL, true, true));
			sqlCombinedView1.addSubView(invocSqlSummary1, new GridData(SWT.FILL, SWT.FILL, true, false));

			TabbedCompositeSubView invocTabbedSubView1 = new TabbedCompositeSubView();
			ISubView invocDetails1 = new SteppingTreeSubView(new SteppingInvocDetailInputController(true));
			ISubView invocMethods1 = new TableSubView(new MethodInvocInputController());
			ISubView invocExceptions1 = new TableSubView(new ExceptionSensorInvocInputController());
			invocTabbedSubView1.addSubView(invocDetails1, "Call Hierarchy", InspectIT.getDefault().getImage(InspectITImages.IMG_CALL_HIERARCHY));
			invocTabbedSubView1.addSubView(sqlCombinedView1, "SQL", InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));
			invocTabbedSubView1.addSubView(invocMethods1, "Methods", InspectIT.getDefault().getImage(InspectITImages.IMG_METHOD_PUBLIC));
			invocTabbedSubView1.addSubView(invocExceptions1, "Exceptions", InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR));

			SashCompositeSubView invocSubView1 = new SashCompositeSubView();
			ISubView invocOverview1 = new TableSubView(new NavigationInvocOverviewInputController());
			invocSubView1.addSubView(invocOverview1, 1);
			invocSubView1.addSubView(invocTabbedSubView1, 2);

			return invocSubView1;
		case MULTI_INVOC_DATA:
			SashCompositeSubView multiInvocSubView = new SashCompositeSubView();
			ISubView multiInvocOverview = new TableSubView(new MultiInvocDataInputController());
			TabbedCompositeSubView multiInvocTabbedSubView = new TabbedCompositeSubView();
			ISubView multiInvocSql = new TreeSubView(new SqlInvocInputController());
			ISubView multiInvocMethods = new TableSubView(new MethodInvocInputController());
			ISubView multiInvocExceptions = new TableSubView(new ExceptionSensorInvocInputController());

			multiInvocTabbedSubView.addSubView(multiInvocSql, "SQL", InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));
			multiInvocTabbedSubView.addSubView(multiInvocMethods, "Methods", InspectIT.getDefault().getImage(InspectITImages.IMG_METHOD_PUBLIC));
			multiInvocTabbedSubView.addSubView(multiInvocExceptions, "Exceptions", InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR));

			multiInvocSubView.addSubView(multiInvocOverview, 1);
			multiInvocSubView.addSubView(multiInvocTabbedSubView, 2);

			return multiInvocSubView;
		case HTTP_TIMER_SENSOR:
			SashCompositeSubView httpSashSubView = new SashCompositeSubView();
			httpSashSubView.addSubView(new TableSubView(new HttpTimerDataInputController()));
			return httpSashSubView;
		case TAGGED_HTTP_TIMER_SENSOR:
			SashCompositeSubView taggedHttpSashSubView = new SashCompositeSubView();
			taggedHttpSashSubView.addSubView(new TableSubView(new TaggedHttpTimerDataInputController()));
			return taggedHttpSashSubView;
		case CHARTING_HTTP_TIMER_SENSOR:
			return new GraphSubView(SensorTypeEnum.CHARTING_HTTP_TIMER_SENSOR);
		default:
			throw new IllegalArgumentException("Could not create sub-view. Not supported: " + sensorTypeEnum.toString());
		}
	}

	/**
	 * Returns an instance of {@link ISubView}.
	 * 
	 * @param fqn
	 *            the fully-qualified name.
	 * @return An instance of {@link ISubView}.
	 */
	public static ISubView createSubView(String fqn) {
		return createSubView(SensorTypeEnum.get(fqn));
	}

}
