package rocks.inspectit.ui.rcp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.formatter.SensorTypeAvailabilityEnum;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * The manager is used to create a tree model currently used by the {@link ServerView}.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenh�fner
 * @author Stefan Siegl
 * @author Alfred Krauss
 */
public class TreeModelManager {

	/**
	 * The repository definition used by this tree.
	 */
	private final RepositoryDefinition repositoryDefinition;

	/**
	 * Platform ident.
	 */
	private final PlatformIdent platformIdent;

	/**
	 * If inactive instrumentation should be hidden.
	 */
	private final boolean hideInactiveInstrumentations;

	/**
	 * Every tree model manager needs a reference to a {@link RepositoryDefinition} which reflects a
	 * CMR.
	 *
	 * @param repositoryDefinition
	 *            The definition of the repository / CMR.
	 * @param platformIdent
	 *            {@link PlatformIdent} to create tree for.
	 * @param hideInactiveInstrumentations
	 *            If inactive instrumentation should be hidden.
	 */
	public TreeModelManager(RepositoryDefinition repositoryDefinition, PlatformIdent platformIdent, boolean hideInactiveInstrumentations) {
		Assert.isNotNull(repositoryDefinition);

		this.repositoryDefinition = repositoryDefinition;
		this.platformIdent = platformIdent;
		this.hideInactiveInstrumentations = hideInactiveInstrumentations;
	}

	/**
	 * Returns the root elements of this model.
	 *
	 * @return The root elements.
	 */
	public Object[] getRootElements() {
		List<Component> components = new ArrayList<>();
		if (null != platformIdent) {
			// Add all sub-trees to this Agent
			components.add(getInstrumentedMethodsTree(platformIdent, repositoryDefinition));
			components.add(getInvocationSequenceTree(platformIdent, repositoryDefinition));
			components.add(getSqlTree(platformIdent, repositoryDefinition));
			components.add(getTimerTree(platformIdent, repositoryDefinition));
			components.add(getHttpTimerTree(platformIdent, repositoryDefinition));
			components.add(getRemoteCallTree(platformIdent, repositoryDefinition));
			components.add(getJmxSensorTree(platformIdent, repositoryDefinition));
			components.add(getExceptionSensorTree(platformIdent, repositoryDefinition));
			components.add(getSystemOverviewTree(platformIdent, repositoryDefinition));
		}
		return components.toArray();
	}

	/**
	 * Creates the deferred sub-tree for the JMX data.
	 *
	 * @param platformIdent
	 *            The platform ID used to create the sub-tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return a list containing the root and all children representing the monitored JMX Beans.
	 */
	private Component getJmxSensorTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite jmxDataComposite = new Composite();
		jmxDataComposite.setName("JMX Data");
		jmxDataComposite.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN));
		jmxDataComposite.setTooltip("With these views, the JMX (Java Management Extension) data objects can be analyzed.");

		boolean sensorTypeAvailable = false;

		for (SensorTypeIdent sensorTypeIdent : platformIdent.getSensorTypeIdents()) {
			if (sensorTypeIdent instanceof JmxSensorTypeIdent) {

				sensorTypeAvailable = true;

				Component showAll = new Leaf();
				showAll.setName("Show All");
				showAll.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.JMX_SENSOR_DATA);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorImage(SensorTypeEnum.JMX_SENSOR_DATA.getImage());
				editorPropertiesData.setSensorName("JMX Data");
				editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));
				editorPropertiesData.setViewName("Show All");
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(sensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				showAll.setInputDefinition(inputDefinition);

				DeferredJmxBrowserComposite browser = new DeferredJmxBrowserComposite();
				browser.setPlatformIdent(platformIdent);
				browser.setRepositoryDefinition(repositoryDefinition);
				browser.setName("Browser");
				browser.setSensorTypeIdent(sensorTypeIdent);

				jmxDataComposite.addChild(showAll);
				jmxDataComposite.addChild(browser);
			}
		}

		if (!sensorTypeAvailable) {
			jmxDataComposite.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ITEM_NA_GREY));
			jmxDataComposite.setTooltip(SensorTypeAvailabilityEnum.JMX_SENSOR_NA.getMessage());
		}

		return jmxDataComposite;
	}

	/**
	 * Creates the deferred sub-tree for instrumented methods.
	 *
	 * @param platformIdent
	 *            The platform ID used to create the sub-tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return a list containing the root and all children representing the instrumented methods in
	 *         the target VM.
	 */
	protected Component getInstrumentedMethodsTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		DeferredBrowserComposite instrumentedMethods = new DeferredBrowserComposite();
		instrumentedMethods.setName("Instrumentation Browser");
		instrumentedMethods.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INSTRUMENTATION_BROWSER));
		instrumentedMethods.setPlatformIdent(platformIdent);
		instrumentedMethods.setRepositoryDefinition(definition);
		instrumentedMethods.setHideInactiveInstrumentations(hideInactiveInstrumentations);
		instrumentedMethods.setTooltip("In this tree, you can see all methods that were being instrumented since the first launch of the Agent. "
				+ "It does not necessarily mean that these methods are currently instrumented and gathering data.");

		return instrumentedMethods;
	}

	/**
	 * Returns the invocation sequence tree.
	 *
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The invocation sequence tree.
	 */
	protected Component getInvocationSequenceTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite invocationSequence = new Composite();
		invocationSequence.setName("Invocation Sequences");
		invocationSequence.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INVOCATION));
		invocationSequence.setTooltip("Invocation Sequences are recorded call trees of the application. Only the starting points (which are defined "
				+ "via the invocation sequence sensor in the agent configuration) are shown in the browser tree.");

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.INVOCATION_SEQUENCE);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.INVOCATION_SEQUENCE.getImage());
		editorPropertiesData.setSensorName("Invocation Sequences");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));
		editorPropertiesData.setViewName("Show All");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		FilteredDeferredBrowserComposite browser = new FilteredDeferredBrowserComposite(SensorTypeEnum.INVOCATION_SEQUENCE);
		browser.setPlatformIdent(platformIdent);
		browser.setRepositoryDefinition(repositoryDefinition);
		browser.setName("Browser");
		browser.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INSTRUMENTATION_BROWSER));
		browser.setTooltip("Only the starting points of invocation sequences (which are defined via the invocation sequence sensor in the agent configuration) are shown in this tree.");
		browser.setHideInactiveInstrumentations(hideInactiveInstrumentations);

		invocationSequence.addChild(showAll);
		invocationSequence.addChild(browser);

		return invocationSequence;
	}

	/**
	 *
	 * Returns the SQL tree.
	 *
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The sql tree.
	 */
	private Component getSqlTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite invocationSequence = new Composite();
		invocationSequence.setName("SQL Statements");
		invocationSequence.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));
		invocationSequence.setTooltip("All recorded SQL statements can be analyzed here.");

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.SQL);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorName("SQL Statements");
		editorPropertiesData.setSensorImage(SensorTypeEnum.SQL.getImage());
		editorPropertiesData.setViewName("Show All");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		invocationSequence.addChild(showAll);

		return invocationSequence;
	}

	/**
	 * Creates the sub-tree for the platform sensors.
	 *
	 * @param platformIdent
	 *            The platform ident.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getSystemOverviewTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite systemOverview = new Composite();
		systemOverview.setName("System Overview");
		systemOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW));

		Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
		List<PlatformSensorTypeIdent> platformSensorTypeIdentList = new ArrayList<>();

		// get all platform sensor types
		for (SensorTypeIdent sensorTypeIdent : sensorTypeIdents) {
			if (sensorTypeIdent instanceof PlatformSensorTypeIdent) {
				PlatformSensorTypeIdent platformSensorTypeIdent = (PlatformSensorTypeIdent) sensorTypeIdent;
				platformSensorTypeIdentList.add(platformSensorTypeIdent);
			}
		}

		// sort the platform sensor types
		Collections.sort(platformSensorTypeIdentList, new Comparator<PlatformSensorTypeIdent>() {
			@Override
			public int compare(PlatformSensorTypeIdent one, PlatformSensorTypeIdent two) {
				return one.getFullyQualifiedClassName().compareTo(two.getFullyQualifiedClassName());
			}
		});

		// add the tree elements
		systemOverview.addChild(getPlatformSensorClassesLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorCpuLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorMemoryLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorThreadLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorVMSummaryLeaf(platformIdent, platformSensorTypeIdentList, definition));

		// sort the tree elements
		Collections.sort(systemOverview.getChildren(), new Comparator<Component>() {
			@Override
			public int compare(Component componentOne, Component componentTwo) {
				return componentOne.getName().compareTo(componentTwo.getName());
			}
		});

		return systemOverview;
	}

	/**
	 * Creates the cpu leaf.
	 *
	 * @param platformIdent
	 *            The platform ident object.
	 *
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorCpuLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component cpuOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		cpuOverview.setName("CPU");
		cpuOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CPU_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.CPU_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.CPU_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW));
				editorPropertiesData.setSensorName("System Overview");
				editorPropertiesData.setViewImage(SensorTypeEnum.CPU_INFORMATION.getImage());
				editorPropertiesData.setViewName("CPU Information");
				editorPropertiesData.setPartImageFlag(PartType.VIEW);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				cpuOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			cpuOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ITEM_NA_GREY));
			cpuOverview.setTooltip(SensorTypeAvailabilityEnum.CPU_INF_NA.getMessage());
		}

		return cpuOverview;
	}

	/**
	 * Creates the platform sensor classes leaf.
	 *
	 * @param platformIdent
	 *            The platform ident object.
	 *
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorClassesLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component classesOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		classesOverview.setName("Classes");
		classesOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.CLASSLOADING_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.CLASSLOADING_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW));
				editorPropertiesData.setSensorName("System Overview");
				editorPropertiesData.setViewImage(SensorTypeEnum.CLASSLOADING_INFORMATION.getImage());
				editorPropertiesData.setViewName("Class Loading Information");
				editorPropertiesData.setPartImageFlag(PartType.VIEW);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				classesOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			classesOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ITEM_NA_GREY));
			classesOverview.setTooltip(SensorTypeAvailabilityEnum.CLASS_INF_NA.getMessage());
		}

		return classesOverview;
	}

	/**
	 * Creates the platform sensor memory leaf.
	 *
	 * @param platformIdent
	 *            The platform ident object.
	 *
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorMemoryLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component memoryOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		memoryOverview.setName("Memory");
		memoryOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_MEMORY_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.MEMORY_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;
				List<PlatformSensorTypeIdent> platformSensorTypeIdentList = new ArrayList<>();
				// add sensor types to local list
				platformSensorTypeIdentList.add(platformSensorTypeIdent);
				for (PlatformSensorTypeIdent platformSensorTypeIdent2 : platformSensorTypeIdents) {
					if (platformSensorTypeIdent2.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.SYSTEM_INFORMATION.getFqn())) {
						platformSensorTypeIdentList.add(platformSensorTypeIdent2);
					}
				}

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.MEMORY_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW));
				editorPropertiesData.setSensorName("System Overview");
				editorPropertiesData.setViewImage(SensorTypeEnum.MEMORY_INFORMATION.getImage());
				editorPropertiesData.setViewName("Memory Information");
				editorPropertiesData.setPartImageFlag(PartType.VIEW);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				memoryOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			memoryOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ITEM_NA_GREY));
			memoryOverview.setTooltip(SensorTypeAvailabilityEnum.MEMORY_INF_NA.getMessage());
		}

		return memoryOverview;
	}

	/**
	 * Creates the platform sensor thread leaf.
	 *
	 * @param platformIdent
	 *            The platform ident object.
	 *
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorThreadLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component threadsOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		threadsOverview.setName("Threads");
		threadsOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_THREADS_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.THREAD_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.THREAD_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW));
				editorPropertiesData.setSensorName("System Overview");
				editorPropertiesData.setViewImage(SensorTypeEnum.THREAD_INFORMATION.getImage());
				editorPropertiesData.setViewName("Thread Information");
				editorPropertiesData.setPartImageFlag(PartType.VIEW);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				threadsOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			threadsOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ITEM_NA_GREY));
			threadsOverview.setTooltip(SensorTypeAvailabilityEnum.THREAD_INF_NA.getMessage());
		}

		return threadsOverview;
	}

	/**
	 * Creates the platform sensor VM Summary leaf.
	 *
	 * @param platformIdent
	 *            The platform ident object.
	 *
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorVMSummaryLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component vmSummary = new Leaf();
		boolean sensorTypeAvailable = false;
		vmSummary.setName("VM Summary");
		vmSummary.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_VM_SUMMARY));

		if (!platformSensorTypeIdents.isEmpty()) {
			sensorTypeAvailable = true;

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(definition);
			inputDefinition.setId(SensorTypeEnum.SYSTEM_INFORMATION);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			editorPropertiesData.setSensorImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW));
			editorPropertiesData.setSensorName("System Overview");
			editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_VM_SUMMARY));
			editorPropertiesData.setViewName("VM Summary");
			editorPropertiesData.setPartImageFlag(PartType.VIEW);
			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(platformIdent.getId());

			inputDefinition.setIdDefinition(idDefinition);
			vmSummary.setInputDefinition(inputDefinition);
		}

		if (!sensorTypeAvailable) {
			vmSummary.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ITEM_NA_GREY));
			vmSummary.setTooltip(SensorTypeAvailabilityEnum.SENSOR_NA.getMessage());
		}

		return vmSummary;
	}

	/**
	 * Returns the exception sensor tree.
	 *
	 * @param platformIdent
	 *            The {@link PlatformIdent} object used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The exception sensor tree.
	 */
	private Component getExceptionSensorTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite exceptionSensor = new Composite();
		exceptionSensor.setName("Exceptions");
		exceptionSensor.setTooltip("All recorded exceptions can be analyzed here.");

		exceptionSensor.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR));
		exceptionSensor.addChild(getUngroupedExceptionOverview(platformIdent, definition));
		exceptionSensor.addChild(getGroupedExceptionOverview(platformIdent, definition));

		return exceptionSensor;
	}

	/**
	 * Returns the ungrouped Exception Overview.
	 *
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The Exception Tree.
	 */
	private Component getUngroupedExceptionOverview(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Component ungroupedExceptionOverview = new Leaf();
		ungroupedExceptionOverview.setName("Show All");
		ungroupedExceptionOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));

		InputDefinition ungroupedExceptionOverviewInputDefinition = new InputDefinition();
		ungroupedExceptionOverviewInputDefinition.setRepositoryDefinition(definition);
		ungroupedExceptionOverviewInputDefinition.setId(SensorTypeEnum.EXCEPTION_SENSOR);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.EXCEPTION_SENSOR.getImage());
		editorPropertiesData.setSensorName("Exceptions");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));
		editorPropertiesData.setViewName("Show All");
		ungroupedExceptionOverviewInputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		ungroupedExceptionOverviewInputDefinition.setIdDefinition(idDefinition);
		ungroupedExceptionOverview.setInputDefinition(ungroupedExceptionOverviewInputDefinition);

		return ungroupedExceptionOverview;
	}

	/**
	 * Returns the grouped Exception Overview.
	 *
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The Exception Sensor overview.
	 */
	private Component getGroupedExceptionOverview(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Component groupedExceptionOverview = new Leaf();
		groupedExceptionOverview.setName("Grouped");
		groupedExceptionOverview.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_FILTER));

		InputDefinition groupedExceptionOverviewInputDefinition = new InputDefinition();
		groupedExceptionOverviewInputDefinition.setRepositoryDefinition(definition);
		groupedExceptionOverviewInputDefinition.setId(SensorTypeEnum.EXCEPTION_SENSOR_GROUPED);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.EXCEPTION_SENSOR_GROUPED.getImage());
		editorPropertiesData.setSensorName("Exceptions");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_FILTER));
		editorPropertiesData.setViewName("Grouped");
		groupedExceptionOverviewInputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());
		groupedExceptionOverviewInputDefinition.setIdDefinition(idDefinition);
		groupedExceptionOverview.setInputDefinition(groupedExceptionOverviewInputDefinition);

		return groupedExceptionOverview;
	}

	/**
	 * Returns the Timer data tree.
	 *
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The timer data tree.
	 */
	private Component getTimerTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite timerDataComposite = new Composite();
		timerDataComposite.setName("Timer Data");
		timerDataComposite.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER));
		timerDataComposite.setTooltip("With these views, the timer data objects can be analyzed to identify e.g. long running methods.");

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.TIMER);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.TIMER.getImage());
		editorPropertiesData.setSensorName("Timer Data");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));
		editorPropertiesData.setViewName("Show All");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		FilteredDeferredBrowserComposite browser = new FilteredDeferredBrowserComposite(SensorTypeEnum.TIMER);
		browser.setPlatformIdent(platformIdent);
		browser.setRepositoryDefinition(repositoryDefinition);
		browser.setName("Browser");
		browser.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INSTRUMENTATION_BROWSER));
		browser.setHideInactiveInstrumentations(hideInactiveInstrumentations);

		timerDataComposite.addChild(showAll);
		timerDataComposite.addChild(browser);

		return timerDataComposite;
	}

	/**
	 * Returns the Http Timer data tree.
	 *
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The timer data tree.
	 */
	private Component getHttpTimerTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite timerDataComposite = new Composite();
		timerDataComposite.setName("Http Timer Data");
		timerDataComposite.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP));

		Component urlAggregationView = new Leaf();
		urlAggregationView.setName("URI Aggregation");
		urlAggregationView.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP_URL));
		urlAggregationView.setTooltip("Aggregates all http requests that are currently in the buffer based on its URI");

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.HTTP_TIMER_SENSOR);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.HTTP_TIMER_SENSOR.getImage());
		editorPropertiesData.setSensorName("Http Timer Data");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP_URL));
		editorPropertiesData.setViewName("URI Aggregation");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());
		for (SensorTypeIdent sensorTypeIdent : platformIdent.getSensorTypeIdents()) {
			if (ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.HTTP_TIMER_SENSOR.getFqn())) {
				idDefinition.setSensorTypeId(sensorTypeIdent.getId());
				break;
			}
		}

		inputDefinition.setIdDefinition(idDefinition);
		urlAggregationView.setInputDefinition(inputDefinition);

		timerDataComposite.addChild(urlAggregationView);

		Component taggedView = new Leaf();
		taggedView.setName("Use Case Aggregation");
		taggedView.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP_TAGGED));
		taggedView.setTooltip(
				"Aggregates all http request that are currently in the buffer based on a the concrete value of the inspectIT Tag Header (called \"" + HttpTimerData.INSPECTIT_TAGGING_HEADER + "\")");

		inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.TAGGED_HTTP_TIMER_SENSOR);

		editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.TAGGED_HTTP_TIMER_SENSOR.getImage());
		editorPropertiesData.setSensorName("Http Timer Data");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP_TAGGED));
		editorPropertiesData.setViewName("Use Case Aggregation");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		taggedView.setInputDefinition(inputDefinition);

		timerDataComposite.addChild(taggedView);

		return timerDataComposite;
	}

	/**
	 * Returns the Remote Call data tree.
	 *
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The Remote Call data tree.
	 */
	private Component getRemoteCallTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite remoteCallDataComposite = new Composite();
		remoteCallDataComposite.setName("Remote Call Data");
		remoteCallDataComposite.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP));

		Component remoteDataView = new Leaf();
		remoteDataView.setName("Show All");
		remoteDataView.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP_URL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.REMOTE_HTTP_CALL_RESPONSE);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.REMOTE_HTTP_CALL_RESPONSE.getImage());
		editorPropertiesData.setSensorName("Remote Call Data");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP_TAGGED));
		editorPropertiesData.setViewName("Show All");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());
		for (SensorTypeIdent sensorTypeIdent : platformIdent.getSensorTypeIdents()) {
			if (ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_HTTP_CALL_RESPONSE.getFqn())
					|| ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_MQ_CONSUMER_RESPONSE.getFqn())
					|| ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_MQ_LISTENER_RESPONSE.getFqn())
					|| ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_CALL_REQUEST_APACHE_HTTPCLIENT_V40.getFqn())
					|| ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_CALL_REQUEST_HTTPURLCONNECTION.getFqn())
					|| ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_CALL_REQUEST_JETTY_HTTPCONNECTION.getFqn())
					|| ObjectUtils.equals(sensorTypeIdent.getFullyQualifiedClassName(), SensorTypeEnum.REMOTE_CALL_REQUEST_MQ.getFqn())) {
				idDefinition.setSensorTypeId(sensorTypeIdent.getId());
				break;
			}
		}

		inputDefinition.setIdDefinition(idDefinition);
		remoteDataView.setInputDefinition(inputDefinition);

		remoteCallDataComposite.addChild(remoteDataView);

		return remoteCallDataComposite;
	}
}
