package info.novatec.inspectit.rcp.editor.inputdefinition;

import info.novatec.inspectit.rcp.editor.inputdefinition.extra.IInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory.InputDefinitionExtraMarker;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import com.google.common.base.Objects;

/**
 * This class is used as the input definition for all editors in the application. Nearly all
 * parameters are optional in here. The actual processing is done in the respective editor which
 * accesses these fields.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InputDefinition {

	/**
	 * This class holds the definition for the IDs used to identify the correct data objects on the
	 * CMR.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public static final class IdDefinition {

		/**
		 * If an ID is not in use ({@link #platformId}, {@link #sensorTypeId}, {@link #methodId}) it
		 * is set to this value to indicate this.
		 */
		public static final long ID_NOT_USED = 0;

		/**
		 * The ID of the platform for the view. Default is {@link ID_NOT_USED}.
		 */
		private long platformId = ID_NOT_USED;

		/**
		 * The ID of the sensor type for the view. Default is {@link ID_NOT_USED}.
		 */
		private long sensorTypeId = ID_NOT_USED;

		/**
		 * The ID of the method for the view. Default is {@link ID_NOT_USED}.
		 */
		private long methodId = ID_NOT_USED;

		/**
		 * The ID of the JMX sensor definition for the view. Default is {@link ID_NOT_USED}.
		 */
		private long jmxDefinitionId = ID_NOT_USED;

		/**
		 * Gets {@link #platformId}.
		 * 
		 * @return {@link #platformId}
		 */
		public long getPlatformId() {
			return platformId;
		}

		/**
		 * Sets {@link #platformId}.
		 * 
		 * @param platformId
		 *            New value for {@link #platformId}
		 */
		public void setPlatformId(long platformId) {
			this.platformId = platformId;
		}

		/**
		 * Gets {@link #sensorTypeId}.
		 * 
		 * @return {@link #sensorTypeId}
		 */
		public long getSensorTypeId() {
			return sensorTypeId;
		}

		/**
		 * Sets {@link #sensorTypeId}.
		 * 
		 * @param sensorTypeId
		 *            New value for {@link #sensorTypeId}
		 */
		public void setSensorTypeId(long sensorTypeId) {
			this.sensorTypeId = sensorTypeId;
		}

		/**
		 * Gets {@link #methodId}.
		 * 
		 * @return {@link #methodId}
		 */
		public long getMethodId() {
			return methodId;
		}

		/**
		 * Sets {@link #methodId}.
		 * 
		 * @param methodId
		 *            New value for {@link #methodId}
		 */
		public void setMethodId(long methodId) {
			this.methodId = methodId;
		}

		/**
		 * Gets {@link #jmxDefinitionId}.
		 * 
		 * @return {@link #jmxDefinitionId}
		 */
		public long getJmxDefinitionId() {
			return jmxDefinitionId;
		}

		/**
		 * Sets {@link #jmxDefinitionId}.
		 * 
		 * @param jmxDefinitionId
		 *            New value for {@link #jmxDefinitionId}
		 */
		public void setJmxDefinitionId(long jmxDefinitionId) {
			this.jmxDefinitionId = jmxDefinitionId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(platformId, sensorTypeId, methodId, jmxDefinitionId);
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
			IdDefinition that = (IdDefinition) object;
			return Objects.equal(this.platformId, that.platformId) && Objects.equal(this.sensorTypeId, that.sensorTypeId) && Objects.equal(this.methodId, that.methodId)
					&& Objects.equal(this.jmxDefinitionId, that.jmxDefinitionId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return Objects.toStringHelper(this).add("platformId", platformId).add("sensorTypeId", sensorTypeId).add("methodId", methodId).add("jmxDefinitionId", jmxDefinitionId).toString();
		}

	}

	/**
	 * If it is not necessary that every subview has its own {@link IdDefinition} object, then this
	 * ID can be used as the key for the map.
	 */
	public static final String GLOBAL_ID = "inspectit.subview.global";

	/**
	 * The update rate of the automatic update mechanism.
	 */
	private long updateRate = PreferencesUtils.getLongValue(PreferencesConstants.REFRESH_RATE);

	/**
	 * If the view should be updated automatically. Default is <code>false</code> .
	 */
	private boolean automaticUpdate = false;

	/**
	 * The repository definition for this view to access the data.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * The idMappings are used for the subviews / controllers to retrieve their specific
	 * {@link IdDefinition} object. The key of the map is one of the IDs defined in the controller
	 * classes.
	 */
	private Map<String, List<IdDefinition>> idMappings = new HashMap<String, List<IdDefinition>>();

	/**
	 * The ID of the view.
	 */
	private SensorTypeEnum id;

	/**
	 * Editor data that define the title of the editor, description, icon and similar properties.
	 */
	private EditorPropertiesData editorPropertiesData = new EditorPropertiesData();

	/**
	 * Map for holding the input definition extras.
	 */
	private Map<InputDefinitionExtraMarker<? extends IInputDefinitionExtra>, IInputDefinitionExtra> inputDefintionExtras = new HashMap<InputDefinitionExtraMarker<? extends IInputDefinitionExtra>, IInputDefinitionExtra>();

	/**
	 * Gets {@link #repositoryDefinition}.
	 * 
	 * @return {@link #repositoryDefinition}
	 */
	public RepositoryDefinition getRepositoryDefinition() {
		return repositoryDefinition;
	}

	/**
	 * Sets {@link #repositoryDefinition}.
	 * 
	 * @param repositoryDefinition
	 *            New value for {@link #repositoryDefinition}
	 */
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		Assert.isNotNull(repositoryDefinition);

		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * Gets {@link #updateRate}.
	 * 
	 * @return {@link #updateRate}
	 */
	public long getUpdateRate() {
		return updateRate;
	}

	/**
	 * Sets {@link #updateRate}.
	 * 
	 * @param updateRate
	 *            New value for {@link #updateRate}
	 */
	public void setUpdateRate(long updateRate) {
		this.updateRate = updateRate;
	}

	/**
	 * Gets {@link #automaticUpdate}.
	 * 
	 * @return {@link #automaticUpdate}
	 */
	public boolean isAutomaticUpdate() {
		return automaticUpdate;
	}

	/**
	 * Sets {@link #automaticUpdate}.
	 * 
	 * @param automaticUpdate
	 *            New value for {@link #automaticUpdate}
	 */
	public void setAutomaticUpdate(boolean automaticUpdate) {
		this.automaticUpdate = automaticUpdate;
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public SensorTypeEnum getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(SensorTypeEnum id) {
		this.id = id;
	}

	/**
	 * Gets {@link #editorPropertiesData}.
	 * 
	 * @return {@link #editorPropertiesData}
	 */
	public EditorPropertiesData getEditorPropertiesData() {
		return editorPropertiesData;
	}

	/**
	 * Sets {@link #editorPropertiesData}.
	 * 
	 * @param editorPropertiesData
	 *            New value for {@link #editorPropertiesData}
	 */
	public void setEditorPropertiesData(EditorPropertiesData editorPropertiesData) {
		this.editorPropertiesData = editorPropertiesData;
	}

	/**
	 * This is a convenience method to just define one {@link IdDefinition} object for this input
	 * definition. If one is already defined, a {@link RuntimeException} is thrown.
	 * 
	 * @param idDefinition
	 *            The ID definition to set.
	 */
	public void setIdDefinition(IdDefinition idDefinition) {
		if (idMappings.containsKey(GLOBAL_ID)) {
			throw new RuntimeException("Already defined an input definition!");
		}

		idMappings.put(GLOBAL_ID, new ArrayList<IdDefinition>());
		idMappings.get(GLOBAL_ID).add(idDefinition);
	}

	/**
	 * This is the counterpart method to {@link #setIdDefinition(IdDefinition)}. This method can be
	 * called as often as needed in contrary to {@link #getAndRemoveIdDefinition(String)}.
	 * 
	 * @return The single ID definition.
	 */
	public IdDefinition getIdDefinition() {
		if (!idMappings.containsKey(GLOBAL_ID)) {
			throw new RuntimeException("No unique id definition is set!");
		}

		return idMappings.get(GLOBAL_ID).get(0);
	}

	/**
	 * Appends the {@link IdDefinition} to the end of the list for the given ID.
	 * 
	 * @param id
	 *            The ID of this {@link IdDefinition} object.
	 * @param idDefinition
	 *            The {@link IdDefinition} object.
	 */
	public void addIdMapping(String id, IdDefinition idDefinition) {
		if (!idMappings.containsKey(id)) {
			idMappings.put(id, new ArrayList<IdDefinition>());
		}

		idMappings.get(id).add(idDefinition);
	}

	/**
	 * This method retrieves the id definition for the specific id. Important here is that the id
	 * definition will be removed from the list. This is necessary so that the same subviews in one
	 * editor could get different id definitions.
	 * <p>
	 * The order of the adding and retrieving of the subviews is important to visualize the correct
	 * graphs/tables etc.
	 * 
	 * @param id
	 *            The ID.
	 * @return The {@link IdDefinition} object.
	 */
	public IdDefinition getAndRemoveIdDefinition(String id) {
		if (!idMappings.containsKey(id)) {
			throw new RuntimeException("Key not found for ID definitions: " + id);
		}
		IdDefinition idDefinition = idMappings.get(id).remove(0);
		if (idMappings.get(id).isEmpty()) {
			idMappings.remove(id);
		}
		return idDefinition;
	}

	/**
	 * Adds the input definition extra.
	 * 
	 * @param extraMarker
	 *            {@link InputDefinitionExtraMarker}.
	 * @param extra
	 *            Extra to add.
	 * @param <E>
	 *            Type of extra.
	 */
	public <E extends IInputDefinitionExtra> void addInputDefinitonExtra(InputDefinitionExtraMarker<E> extraMarker, E extra) {
		inputDefintionExtras.put(extraMarker, extra);
	}

	/**
	 * Returns the input definition extra.
	 * 
	 * @param extraMarker
	 *            Marker that defines the type of extra.
	 * @param <E>
	 *            Type of extra.
	 * @return Returns the input definition extra.
	 */
	@SuppressWarnings("unchecked")
	public <E extends IInputDefinitionExtra> E getInputDefinitionExtra(InputDefinitionExtraMarker<E> extraMarker) {
		return (E) inputDefintionExtras.get(extraMarker);
	}

	/**
	 * Returns if the {@link InputDefinition} contains the extra defined by the marker.
	 * 
	 * @param extraMarker
	 *            Marker.
	 * @return True if input definition has the extra marker.
	 */
	public boolean hasInputDefinitionExtra(InputDefinitionExtraMarker<?> extraMarker) {
		return inputDefintionExtras.containsKey(extraMarker);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(updateRate, automaticUpdate, repositoryDefinition, idMappings, id, editorPropertiesData, inputDefintionExtras);
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
		InputDefinition that = (InputDefinition) object;
		return Objects.equal(this.updateRate, that.updateRate) && Objects.equal(this.automaticUpdate, that.automaticUpdate) && Objects.equal(this.repositoryDefinition, that.repositoryDefinition)
				&& Objects.equal(this.idMappings, that.idMappings) && Objects.equal(this.id, that.id) && Objects.equal(this.editorPropertiesData, that.editorPropertiesData)
				&& Objects.equal(this.inputDefintionExtras, that.inputDefintionExtras);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("updateRate", updateRate).add("automaticUpdate", automaticUpdate).add("repositoryDefinition", repositoryDefinition).add("idMappings", idMappings)
				.add("id", id).add("editorPropertiesData", editorPropertiesData).add("inputDefintionExtras", inputDefintionExtras).toString();
	}

}
