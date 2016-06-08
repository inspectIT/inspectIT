package rocks.inspectit.ui.rcp.formatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;

import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ChartingMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.ci.context.AbstractContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.FieldContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.ParameterContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.ReturnContextCapture;
import rocks.inspectit.shared.cs.ci.profile.data.AbstractProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.ExcludeRulesProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.JmxDefinitionProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpUrlConnectionInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQConsumerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQListenerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;
import rocks.inspectit.shared.cs.communication.data.cmr.WritingStatus;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageData.StorageState;
import rocks.inspectit.shared.cs.storage.label.type.AbstractCustomStorageLabelType;
import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.AssigneeLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CreationDateLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.DataTimeFrameLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.ExploredByLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.RatingLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.StatusLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.UseCaseLabelType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;
import rocks.inspectit.ui.rcp.resource.CombinedIcon;

/**
 * The class provide image descriptors for the different elements.
 *
 * @author Ivan Senic
 *
 */
public final class ImageFormatter {

	/**
	 * List of icons that can be used for custom labels.
	 */
	public static final String[] LABEL_ICONS = new String[] { InspectITImages.IMG_HOME, InspectITImages.IMG_MESSAGE, InspectITImages.IMG_WARNING, InspectITImages.IMG_ACTIVITY,
			InspectITImages.IMG_CALENDAR, InspectITImages.IMG_ASSIGNEE_LABEL_ICON, InspectITImages.IMG_CHECKMARK, InspectITImages.IMG_CLASS, InspectITImages.IMG_PACKAGE,
			InspectITImages.IMG_METHOD_DEFAULT, InspectITImages.IMG_MEMORY_OVERVIEW, InspectITImages.IMG_CPU_OVERVIEW, InspectITImages.IMG_THREADS_OVERVIEW, InspectITImages.IMG_DATABASE,
			InspectITImages.IMG_DATE_LABEL_ICON, InspectITImages.IMG_USECASE_LABEL_ICON, InspectITImages.IMG_RATING_LABEL_ICON, InspectITImages.IMG_STATUS_LABEL_ICON, InspectITImages.IMG_SEARCH,
			InspectITImages.IMG_TIMER, InspectITImages.IMG_TOOL, InspectITImages.IMG_TRASH, InspectITImages.IMG_PROPERTIES, InspectITImages.IMG_TIME, InspectITImages.IMG_FONT,
			InspectITImages.IMG_INFORMATION, InspectITImages.IMG_FILTER, InspectITImages.IMG_HTTP_URL };

	/**
	 * Private constructor.
	 */
	private ImageFormatter() {
	}

	/**
	 * Returns the {@link Image} for the composite that represents a label.
	 *
	 * @param labelType
	 *            Label type.
	 * @return {@link Image} for Composite.
	 */
	public static Image getImageForLabel(AbstractStorageLabelType<?> labelType) {
		return InspectIT.getDefault().getImage(getImageKeyForLabel(labelType));
	}

	/**
	 * Returns the {@link ImageDescriptor} for the composite that represents a label.
	 *
	 * @param labelType
	 *            Label type.
	 * @return {@link ImageDescriptor} for {@link Composite}.
	 */
	public static ImageDescriptor getImageDescriptorForLabel(AbstractStorageLabelType<?> labelType) {
		return InspectIT.getDefault().getImageDescriptor(getImageKeyForLabel(labelType));
	}

	/**
	 * Returns the image key for the label type.
	 *
	 * @param labelType
	 *            Label type.
	 * @return String that represents the image key. Will never be <code>null</code>.
	 */
	private static String getImageKeyForLabel(AbstractStorageLabelType<?> labelType) {
		if (AssigneeLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_ASSIGNEE_LABEL_ICON;
		} else if (CreationDateLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_DATE_LABEL_ICON;
		} else if (ExploredByLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_MOUNTEDBY_LABEL_ICON;
		} else if (RatingLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_RATING_LABEL_ICON;
		} else if (StatusLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_STATUS_LABEL_ICON;
		} else if (UseCaseLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_USECASE_LABEL_ICON;
		} else if (DataTimeFrameLabelType.class.equals(labelType.getClass())) {
			return InspectITImages.IMG_TIMEFRAME;
		} else if (labelType instanceof AbstractCustomStorageLabelType) {
			AbstractCustomStorageLabelType<?> customLabelType = (AbstractCustomStorageLabelType<?>) labelType;
			if (null != customLabelType.getImageKey()) {
				// assure that the image key is registered in the image registry
				if (null != InspectIT.getDefault().getImage(customLabelType.getImageKey())) { // NOPMD
					return customLabelType.getImageKey();
				}
			}
			if (Boolean.class.equals(customLabelType.getValueClass())) {
				return InspectITImages.IMG_CHECKMARK;
			} else if (Date.class.equals(customLabelType.getValueClass())) {
				return InspectITImages.IMG_CALENDAR;
			} else if (Number.class.equals(customLabelType.getValueClass())) {
				return InspectITImages.IMG_NUMBER;
			} else if (String.class.equals(customLabelType.getValueClass())) {
				return InspectITImages.IMG_FONT;
			}
		}
		return InspectITImages.IMG_USER_LABEL_ICON;
	}

	/**
	 *
	 * @param storageData
	 *            {@link StorageData} to get picture for.
	 * @return Returns the {@link Image} for the storage, based on the
	 *         {@link StorageData.StorageState}.
	 */
	public static Image getImageForStorageLeaf(StorageData storageData) {
		if (storageData.getState() == StorageState.CREATED_NOT_OPENED) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE);
		} else if (storageData.getState() == StorageState.OPENED) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_OPENED);
		} else if (storageData.getState() == StorageState.RECORDING) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_RECORDING);
		} else if (storageData.getState() == StorageState.CLOSED) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_CLOSED);
		}
		return null;
	}

	/**
	 * Returns image based on the CMR repository status.
	 *
	 * @param selectedCmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param small
	 *            Should picture be small or big.
	 * @return Image.
	 */
	public static Image getCmrRepositoryImage(CmrRepositoryDefinition selectedCmrRepositoryDefinition, boolean small) {
		if (selectedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			if (small) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE_SMALL);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE);
			}
		} else if (selectedCmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			if (small) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE_SMALL);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE);
			}
		} else {
			if (small) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH_SMALL);
			} else {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH);
			}
		}
	}

	/**
	 * Returns image for the title box.
	 *
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}
	 * @return Image for the title box.
	 */
	public static Image getStorageRepositoryImage(StorageRepositoryDefinition storageRepositoryDefinition) {
		LocalStorageData localStorageData = storageRepositoryDefinition.getLocalStorageData();
		if (localStorageData.isFullyDownloaded()) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_DOWNLOADED);
		} else if (storageRepositoryDefinition.getCmrRepositoryDefinition().getOnlineStatus() != OnlineStatus.OFFLINE) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_AVAILABLE);
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_NOT_AVAILABLE);
		}
	}

	/**
	 * Returns image that represents the {@link WritingStatus} or null if the writing status passed
	 * is null.
	 *
	 * @param status
	 *            Image for {@link WritingStatus}.
	 * @return Returns image that represents the {@link WritingStatus} or null if the writing status
	 *         passed is null.
	 */
	public static Image getWritingStatusImage(WritingStatus status) {
		if (null == status) {
			return null;
		}
		switch (status) {
		case GOOD:
			return InspectIT.getDefault().getImage(InspectITImages.IMG_FLAG);
		case MEDIUM:
			return InspectIT.getDefault().getImage(InspectITImages.IMG_WARNING);
		case BAD:
			return InspectIT.getDefault().getImage(InspectITImages.IMG_ALERT);
		default:
			return null;
		}
	}

	/**
	 * Returns overlayed icon for editors with additional {@link ImageDescriptor} depending if the
	 * repository is CMR or Storage repository.
	 *
	 * @param original
	 *            Original icon.
	 * @param repositoryDefinition
	 *            Repository definition.
	 * @param resourceManager
	 *            Resource manager that image will be created with. It is responsibility of a caller
	 *            to provide {@link ResourceManager} for correct image disposing.
	 * @return Overlayed icon.
	 */
	public static Image getOverlayedEditorImage(Image original, RepositoryDefinition repositoryDefinition, ResourceManager resourceManager) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			return original;
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			ImageDescriptor overlayDescriptor = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STORAGE_OVERLAY);
			DecorationOverlayIcon icon = new DecorationOverlayIcon(original, new ImageDescriptor[] { null, null, null, overlayDescriptor, null });
			Image img = resourceManager.createImage(icon);
			return img;
		}
		return null;
	}

	/**
	 * Returns an overlayed icon of the passed main image using the passed images as overlays.
	 *
	 * @param main
	 *            main image to be overlayed
	 * @param resourceManager
	 *            Resource manager that image will be created with. It is responsibility of a caller
	 *            to provide {@link ResourceManager} for correct image disposing.
	 * @param scaleFactor
	 *            scale factor to be used to scale the overlay images. 1.0 means exactly the
	 *            original size, 0.5 half size, 2.0 double size.
	 * @param overlays
	 *            1 to 4 overlay images. 1: top-left, 2: top-right, 3: bottom-left, 4: button-right.
	 *            Any of the overlay positions can be null to not draw an overlay at that position.
	 * @return an overlayed image
	 */
	public static Image getOverlayedImage(Image main, ResourceManager resourceManager, double scaleFactor, Image... overlays) {
		ImageDescriptor[] descriptors = new ImageDescriptor[overlays.length];
		for (int i = 0; i < overlays.length; i++) {
			if (null != overlays[i]) {
				ImageData imageData = overlays[i].getImageData();
				imageData = imageData.scaledTo((int) (scaleFactor * imageData.width), (int) (scaleFactor * imageData.height));
				descriptors[i] = ImageDescriptor.createFromImageData(imageData);
			}
		}

		DecorationOverlayIcon icon = new DecorationOverlayIcon(main, descriptors);
		Image img = resourceManager.createImage(icon);
		return img;
	}

	/**
	 * Returns the combined image for given array of descriptors. Orientation can be vertical or
	 * horizontal.
	 *
	 * @param resourceManager
	 *            {@link ResourceManager}.
	 * @param orientation
	 *            SWT#Vertical or SWT#Horizontal. Descriptors will be passed in given order.
	 * @param descriptors
	 *            Array of descriptors.
	 *
	 * @return Combined {@link Image}.
	 */
	public static Image getCombinedImage(ResourceManager resourceManager, int orientation, ImageDescriptor... descriptors) {
		return getCombinedImage(resourceManager, orientation, 0, 0, descriptors);
	}

	/**
	 * Returns the combined image for given array of descriptors. Orientation can be vertical or
	 * horizontal.
	 *
	 * @param resourceManager
	 *            {@link ResourceManager}.
	 * @param orientation
	 *            SWT#Vertical or SWT#Horizontal. Descriptors will be passed in given order.
	 * @param minWidth
	 *            min width of image
	 * @param minHeight
	 *            min height of image
	 * @param descriptors
	 *            Array of descriptors.
	 *
	 * @return Combined {@link Image}.
	 */
	public static Image getCombinedImage(ResourceManager resourceManager, int orientation, int minWidth, int minHeight, ImageDescriptor... descriptors) {
		ImageDescriptor combinedImageDescriptor = new CombinedIcon(descriptors, orientation, minWidth, minHeight);
		Image img = resourceManager.createImage(combinedImageDescriptor);
		return img;
	}

	/**
	 * Returns the image for the agent based on the last data sent date.
	 *
	 * @param agentStatusData
	 *            {@link AgentStatusData} golding the information or null if it's not available.
	 *
	 * @return {@link Image}
	 */
	public static Image getAgentImage(AgentStatusData agentStatusData) {
		if (null != agentStatusData) {
			switch (agentStatusData.getAgentConnection()) {
			case CONNECTED:
				if (null != agentStatusData.getMillisSinceLastData()) {
					long millis = agentStatusData.getMillisSinceLastData().longValue();
					// at last one minute of not sending data to display as the non active
					if (millis > 60000) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NOT_SENDING);
					} else {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_ACTIVE);
					}
				} else {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NOT_SENDING);
				}
			case NO_KEEP_ALIVE:
				return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NO_KEEPALIVE);
			default:
				return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NOT_ACTIVE);
			}
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT_NOT_ACTIVE);
		}
	}

	/**
	 * Returns image for the {@link ISensorConfig}.
	 *
	 * @param sensorConfig
	 *            {@link ISensorConfig}
	 * @return Image or <code>null</code> if one can not be resolved for given sensor configuration.
	 */
	public static Image getSensorConfigImage(ISensorConfig sensorConfig) {
		return getSensorConfigImage(sensorConfig.getClass());
	}

	/**
	 * Returns image for the {@link ISensorConfig} class.
	 *
	 * @param sensorClass
	 *            {@link ISensorConfig} class.
	 * @return Image or <code>null</code> if one can not be resolved for given sensor configuration.
	 */
	public static Image getSensorConfigImage(Class<? extends ISensorConfig> sensorClass) {
		if (ObjectUtils.equals(sensorClass, ExceptionSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR);
		} else if (ObjectUtils.equals(sensorClass, ConnectionSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
		} else if (ObjectUtils.equals(sensorClass, HttpSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, InvocationSequenceSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_INVOCATION);
		} else if (ObjectUtils.equals(sensorClass, PreparedStatementParameterSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
		} else if (ObjectUtils.equals(sensorClass, PreparedStatementSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
		} else if (ObjectUtils.equals(sensorClass, StatementSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
		} else if (ObjectUtils.equals(sensorClass, TimerSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER);
		} else if (ObjectUtils.equals(sensorClass, Log4jLoggingSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_LOG);
		} else if (ObjectUtils.equals(sensorClass, ClassLoadingSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS_OVERVIEW);
		} else if (ObjectUtils.equals(sensorClass, CompilationSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_COMPILATION_OVERVIEW);
		} else if (ObjectUtils.equals(sensorClass, CpuSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_CPU_OVERVIEW);
		} else if (ObjectUtils.equals(sensorClass, MemorySensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_MEMORY_OVERVIEW);
		} else if (ObjectUtils.equals(sensorClass, RuntimeSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_VM_SUMMARY);
		} else if (ObjectUtils.equals(sensorClass, SystemSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW);
		} else if (ObjectUtils.equals(sensorClass, ThreadSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_THREADS_OVERVIEW);
		} else if (ObjectUtils.equals(sensorClass, JmxSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN);
		} else if (ObjectUtils.equals(sensorClass, RemoteApacheHttpClientV40InserterSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, RemoteHttpExtractorSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, RemoteHttpUrlConnectionInserterSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, RemoteJettyHttpClientV61InserterSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, RemoteMQConsumerExtractorSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, RemoteMQInserterSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		} else if (ObjectUtils.equals(sensorClass, RemoteMQListenerExtractorSensorConfig.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
		}
		return null;
	}

	/**
	 * Returns the image describing the method visibility of the {@link MethodSensorAssignment}.
	 *
	 * @param resourceManager
	 *            Resource manager to create image with.
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment} to create image for.
	 * @return Image
	 */
	public static Image getMethodVisibilityImage(ResourceManager resourceManager, MethodSensorAssignment methodSensorAssignment) {
		ImageDescriptor[] descriptors = new ImageDescriptor[4];
		if (methodSensorAssignment.isPublicModifier()) {
			descriptors[0] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_PUBLIC);
		} else {
			descriptors[0] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_PUBLIC_DISABLED);
		}

		if (methodSensorAssignment.isProtectedModifier()) {
			descriptors[1] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_PROTECTED);
		} else {
			descriptors[1] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_PROTECTED_DISABLED);
		}

		if (methodSensorAssignment.isDefaultModifier()) {
			descriptors[2] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_DEFAULT);
		} else {
			descriptors[2] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_DEFAULT_DISABLED);
		}

		if (methodSensorAssignment.isPrivateModifier()) {
			descriptors[3] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_PRIVATE);
		} else {
			descriptors[3] = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_METHOD_PRIVATE_DISABLED);
		}

		return getCombinedImage(resourceManager, SWT.HORIZONTAL, descriptors);
	}

	/**
	 * Returns the image describing the options of the {@link AbstractClassSensorAssignment}.
	 *
	 * @param resourceManager
	 *            Resource manager to create image with.
	 * @param assignment
	 *            {@link AbstractClassSensorAssignment} to create image for.
	 * @return Image
	 */
	public static Image getSensorAssignmentOptionsImage(ResourceManager resourceManager, AbstractClassSensorAssignment<?> assignment) {
		List<ImageDescriptor> descs = new ArrayList<>();

		if (StringUtils.isNotEmpty(assignment.getAnnotation())) {
			descs.add(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ANNOTATION));
		}

		if (assignment instanceof ChartingMethodSensorAssignment) {
			ChartingMethodSensorAssignment chartingAssignment = (ChartingMethodSensorAssignment) assignment;

			if (chartingAssignment.isCharting()) {
				descs.add(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_CHART_PIE));
			}
		}

		if (assignment instanceof TimerMethodSensorAssignment) {
			TimerMethodSensorAssignment timerAssignment = (TimerMethodSensorAssignment) assignment;

			if (timerAssignment.isStartsInvocation()) {
				descs.add(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_INVOCATION));
			}

			boolean paramsCapture = false, returnCapture = false, fieldsCapture = false;
			if (CollectionUtils.isNotEmpty(timerAssignment.getContextCaptures())) {
				for (AbstractContextCapture contextCapture : timerAssignment.getContextCaptures()) {
					paramsCapture |= contextCapture instanceof ParameterContextCapture;
					returnCapture |= contextCapture instanceof ReturnContextCapture;
					fieldsCapture |= contextCapture instanceof FieldContextCapture;
				}
			}

			if (paramsCapture) {
				descs.add(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_PARAMETER));
			}

			if (fieldsCapture) {
				descs.add(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_FIELD));
			}

			if (returnCapture) {
				descs.add(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_RETURN));
			}
		}

		if (CollectionUtils.isEmpty(descs)) {
			return null;
		} else {
			return getCombinedImage(resourceManager, SWT.HORIZONTAL, descs.toArray(new ImageDescriptor[descs.size()]));
		}
	}

	/**
	 * Returns environment image.
	 *
	 * @param environment
	 *            Environment to get image for.
	 * @return Returns environment image.
	 */
	public static Image getEnvironmentImage(Environment environment) {
		return InspectIT.getDefault().getImage(InspectITImages.IMG_BLOCK);
	}

	/**
	 * Returns profile image.
	 *
	 * @param profile
	 *            Profile to get image for.
	 * @return Returns profile image.
	 */
	public static Image getProfileImage(Profile profile) {
		if (profile.isCommonProfile()) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_ADDRESSBOOK_BLUE);
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_ADDRESSBOOK);
		}
	}

	/**
	 * Returns application definition image.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to get image for.
	 * @return Returns image for application definition.
	 */
	public static Image getApplicationDefinitionImage(ApplicationDefinition appDefinition) {
		if (appDefinition.getId() == ApplicationDefinition.DEFAULT_ID) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION_GREY);
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION);
		}
	}

	/**
	 * Returns business transaction definition image.
	 *
	 * @param businessTxDefinition
	 *            {@link BusinessTransactionDefinition} to get image for.
	 * @return Returns image for business transaction definition.
	 */
	public static Image getBusinessTransactionDefinitionImage(BusinessTransactionDefinition businessTxDefinition) {
		if (businessTxDefinition.getId() == BusinessTransactionDefinition.DEFAULT_ID) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_TRANSACTION_GREY);
		} else {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_TRANSACTION);
		}
	}

	/**
	 * Returns profile data image.
	 *
	 * @param profileData
	 *            Profile data to get image for.
	 * @return Returns profile data image.
	 */
	public static Image getProfileDataImage(AbstractProfileData<?> profileData) {
		if (profileData.isOfType(SensorAssignmentProfileData.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER);
		} else if (profileData.isOfType(ExcludeRulesProfileData.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS_EXCLUDE);
		} else if (profileData.isOfType(JmxDefinitionProfileData.class)) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN);
		}
		return null;
	}
}
