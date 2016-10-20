package rocks.inspectit.shared.all.cmr.model;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.codehaus.jackson.annotate.JsonIgnore;

import rocks.inspectit.shared.all.jpa.MapStringConverter;

/**
 * The Method Sensor Type Ident class is used to store the sensor types which are used for methods
 * and basically called when the respective method is called.
 *
 * @author Patrice Bouillet
 *
 */
@Entity
@DiscriminatorValue("MSTI")
@NamedQueries({ @NamedQuery(name = MethodSensorTypeIdent.FIND_ALL, query = "SELECT ms FROM MethodSensorTypeIdent ms"),
		@NamedQuery(name = MethodSensorTypeIdent.FIND_ID_BY_CLASS_AND_PLATFORM_ID, query = "SELECT ms.id FROM MethodSensorTypeIdent ms JOIN ms.platformIdent p WHERE p.id=:platformIdent AND ms.fullyQualifiedClassName=:fullyQualifiedClassName"),
		@NamedQuery(name = MethodSensorTypeIdent.UPDATE_PARAMETERS, query = "UPDATE MethodSensorTypeIdent SET settings=:parameters WHERE id=:id") })
public class MethodSensorTypeIdent extends SensorTypeIdent {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8933452676894686230L;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "MethodSensorTypeIdent.findAll";

	/**
	 * Constant for findIdByClassAndPlatformId query. *
	 * <p>
	 * Parameters in the query:
	 * <ul>
	 * <li>platformIdent
	 * <li>fullyQualifiedClassName
	 * </ul>
	 */
	public static final String FIND_ID_BY_CLASS_AND_PLATFORM_ID = "MethodSensorTypeIdent.findIdByClassAndPlatformId";

	/**
	 * Constant for updateParameters query. *
	 * <p>
	 * Parameters in the query:
	 * <ul>
	 * <li>id
	 * <li>parameters
	 * </ul>
	 */
	public static final String UPDATE_PARAMETERS = "MethodSensorTypeIdent.updateParameters";

	/**
	 * Settings of the sensor on the agent.
	 */
	@Convert(converter = MapStringConverter.class)
	@Column(length = 2000)
	private Map<String, Object> settings;

	/**
	 * Gets {@link #settings}.
	 *
	 * @return {@link #settings}
	 */
	@JsonIgnore
	public Map<String, Object> getSettings() {
		return settings;
	}

	/**
	 * Sets {@link #settings}.
	 *
	 * @param settings
	 *            New value for {@link #settings}
	 */
	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}

}
