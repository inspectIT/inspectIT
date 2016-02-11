package info.novatec.inspectit.cmr.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The Platform Sensor Type Ident class is used to store the sensor types which are collecting data
 * of the target VM/System/etc. They are not based on method instrumentation.
 * 
 * @author Patrice Bouillet
 * 
 */
@Entity
@DiscriminatorValue("PSTI")
@NamedQueries({
		@NamedQuery(name = PlatformSensorTypeIdent.FIND_ALL, query = "SELECT ps FROM PlatformSensorTypeIdent ps"),
		@NamedQuery(name = PlatformSensorTypeIdent.FIND_BY_CLASS_AND_PLATFORM_ID, query = "SELECT ps FROM PlatformSensorTypeIdent ps JOIN ps.platformIdent p WHERE p.id=:platformIdent AND ps.fullyQualifiedClassName=:fullyQualifiedClassName") })
public class PlatformSensorTypeIdent extends SensorTypeIdent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -9186810846909304542L;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "PlatformSensorTypeIdent.findAll";

	/**
	 * Constant for findByClassAndPlatformId query.
	 */
	public static final String FIND_BY_CLASS_AND_PLATFORM_ID = "PlatformSensorTypeIdent.findByClassAndPlatformId";

}
