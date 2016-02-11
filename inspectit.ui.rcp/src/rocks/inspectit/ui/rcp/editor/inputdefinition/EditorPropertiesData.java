package info.novatec.inspectit.rcp.editor.inputdefinition;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Objects;

/**
 * Class that defines the editor properties like title, image, description.
 * 
 * @author Ivan Senic
 * 
 */
public class EditorPropertiesData {

	/**
	 * Enumeration that can be used to specify what should be used for editors part image and name.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum PartType {

		/**
		 * Value for group properties, can be used when specifying the part title and image.
		 */
		SENSOR,

		/**
		 * Value for view properties, can be used when specifying the part title and image.
		 */
		VIEW,

	}

	/**
	 * Flag for specifying what should be used as a part name. Defaults to
	 * {@link EditorPropertiesData#VIEW}.
	 */
	private PartType partNameFlag = PartType.VIEW;

	/**
	 * Flag for specifying what should be used as a part image. Defaults to
	 * {@link EditorPropertiesData#SENSOR}.
	 */
	private PartType partImageFlag = PartType.SENSOR;

	/**
	 * Group/sensor image.
	 */
	private Image sensorImage;

	/**
	 * Group/sensor name.
	 */
	private String sensorName = "";

	/**
	 * The image of the view.
	 */
	private Image viewImage;

	/**
	 * View name.
	 */
	private String viewName = "";

	/**
	 * @return Returns the part name. This {@link String} should be used for the editor tab.
	 */
	public String getPartName() {
		switch (partNameFlag) {
		case SENSOR:
			return sensorName;
		default:
			return viewName;
		}
	}

	/**
	 * Sets what will be used as part name.
	 * <p>
	 * Values acceptable are:<br>
	 * {@link #SENSOR} - Uses sensor name as the part name<br>
	 * {@link #VIEW} - Uses view name as the part name<br>
	 * {@link #INFO} - Uses info as the part name
	 * 
	 * @param partType
	 *            Flag to set for part name.
	 */
	public void setPartNameFlag(PartType partType) {
		partNameFlag = partType;
	}

	/**
	 * @return Returns the part image. This {@link Image} should be used for the editor tab.
	 */
	public Image getPartImage() {
		switch (partImageFlag) {
		case VIEW:
			return viewImage;
		default:
			return sensorImage;
		}
	}

	/**
	 * Sets what will be used as part image.
	 * <p>
	 * Values acceptable are:<br>
	 * {@link #SENSOR} - Uses sensor image as the part image<br>
	 * {@link #VIEW} - Uses view image as the part image<br>
	 * 
	 * @param partType
	 *            Flag to set for part image.
	 */
	public void setPartImageFlag(PartType partType) {
		partImageFlag = partType;
	}

	/**
	 * Gets {@link #partTooltip}.
	 * 
	 * @return {@link #partTooltip}
	 */
	public String getPartTooltip() {
		StringBuilder stringBuilder = new StringBuilder(sensorName);
		if (StringUtils.isNotEmpty(viewName)) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(" > ");
			}
			stringBuilder.append(viewName);
		}
		return stringBuilder.toString();
	}

	/**
	 * Gets {@link #sensorImage}.
	 * 
	 * @return {@link #sensorImage}
	 */
	public Image getSensorImage() {
		return sensorImage;
	}

	/**
	 * Sets {@link #sensorImage}.
	 * 
	 * @param image
	 *            New value for {@link #sensorImage}
	 */
	public void setSensorImage(Image image) {
		this.sensorImage = image;
	}

	/**
	 * Gets {@link #sensorName}.
	 * 
	 * @return {@link #sensorName}
	 */
	public String getSensorName() {
		return sensorName;
	}

	/**
	 * Sets {@link #sensorName}.
	 * 
	 * @param sensorName
	 *            New value for {@link #sensorName}
	 */
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	/**
	 * Gets {@link #viewImage}.
	 * 
	 * @return {@link #viewImage}
	 */
	public Image getViewImage() {
		return viewImage;
	}

	/**
	 * Sets {@link #viewImage}.
	 * 
	 * @param descriptionImage
	 *            New value for {@link #viewImage}
	 */
	public void setViewImage(Image descriptionImage) {
		this.viewImage = descriptionImage;
	}

	/**
	 * Gets {@link #viewName}.
	 * 
	 * @return {@link #viewName}
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Sets {@link #viewName}.
	 * 
	 * @param viewName
	 *            New value for {@link #viewName}
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(partImageFlag, partNameFlag, sensorImage, sensorName, viewImage, viewName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		EditorPropertiesData that = (EditorPropertiesData) object;
		return Objects.equal(this.partImageFlag, that.partImageFlag) && Objects.equal(this.partNameFlag, that.partNameFlag) && Objects.equal(this.sensorImage, that.sensorImage)
				&& Objects.equal(this.sensorName, that.sensorName) && Objects.equal(this.viewImage, that.viewImage) && Objects.equal(this.viewName, that.viewName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("group/sensor image", sensorImage).add("group/sensor", sensorName).add("viewName", viewImage).add("viewName", viewName).toString();
	}

}