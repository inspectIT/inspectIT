package info.novatec.inspectit.rcp.formatter;

import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.WritingStatus;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.resource.CombinedIcon;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.DataTimeFrameLabelType;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;

import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

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
		ImageDescriptor combinedImageDescriptor = new CombinedIcon(descriptors, orientation);
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
}
