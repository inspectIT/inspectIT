package info.novatec.inspectit.cmr.model;

import info.novatec.inspectit.jpa.MapStringConverter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * The Method Sensor Type Ident class is used to store the sensor types which are used for methods
 * and basically called when the respective method is called.
 * 
 * @author Patrice Bouillet
 * 
 */
@Entity
@DiscriminatorValue("MSTI")
@NamedQueries({
		@NamedQuery(name = MethodSensorTypeIdent.FIND_ALL, query = "SELECT ms FROM MethodSensorTypeIdent ms"),
		@NamedQuery(name = MethodSensorTypeIdent.FIND_BY_CLASS_AND_PLATFORM_ID, query = "SELECT ms FROM MethodSensorTypeIdent ms JOIN ms.platformIdent p WHERE p.id=:platformIdent AND ms.fullyQualifiedClassName=:fullyQualifiedClassName") })
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
	 * Constant for findByClassAndPlatformId query.
	 */
	public static final String FIND_BY_CLASS_AND_PLATFORM_ID = "MethodSensorTypeIdent.findByClassAndPlatformId";

	/**
	 * The one-to-many association to the {@link MethodIdentToSensorType} objects.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "methodSensorTypeIdent", cascade = CascadeType.ALL)
	private Set<MethodIdentToSensorType> methodIdentToSensorTypes = new HashSet<MethodIdentToSensorType>(0);

	/**
	 * Settings of the sensor on the agent.
	 */
	@Convert(converter = MapStringConverter.class)
	@Column(length = 2000)
	private Map<String, Object> settings;

	/**
	 * Gets {@link #methodIdentToSensorTypes}.
	 * 
	 * @return {@link #methodIdentToSensorTypes}
	 */
	public Set<MethodIdentToSensorType> getMethodIdentToSensorTypes() {
		return methodIdentToSensorTypes;
	}

	/**
	 * Sets {@link #methodIdentToSensorTypes}.
	 * 
	 * @param methodIdentToSensorTypes
	 *            New value for {@link #methodIdentToSensorTypes}
	 */
	public void setMethodIdentToSensorTypes(Set<MethodIdentToSensorType> methodIdentToSensorTypes) {
		this.methodIdentToSensorTypes = methodIdentToSensorTypes;
	}

	/**
	 * Gets {@link #settings}.
	 * 
	 * @return {@link #settings}
	 */
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
