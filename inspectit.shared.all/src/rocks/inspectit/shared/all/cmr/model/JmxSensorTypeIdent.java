package info.novatec.inspectit.cmr.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The Jmx Sensor Type Ident class is used to store the sensortypes which are collecting data of the
 * target VM/System/etc.
 * 
 * @author Alfred Krauss
 * 
 */
@Entity
@DiscriminatorValue("JSTI")
@NamedQueries({ @NamedQuery(name = JmxSensorTypeIdent.FIND_BY_PLATFORM, query = "SELECT j FROM JmxSensorTypeIdent j JOIN j.platformIdent p WHERE p.id=:platformIdentId") })
public class JmxSensorTypeIdent extends SensorTypeIdent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6608523838770959163L;

	/**
	 * Constant for findByPlatform query.
	 * <p>
	 * Parameters in the query:
	 * <ul>
	 * <li>platformIdentId
	 * </ul>
	 * </p>
	 */
	public static final String FIND_BY_PLATFORM = "JmxSensorTypeIdent.findByPlatform";
}
