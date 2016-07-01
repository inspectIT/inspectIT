package rocks.inspectit.shared.all.communication;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * The {@link PlatformSensorData} class is extended by all value & data objects which are used to
 * gather system/platform information.
 *
 * @author Patrice Bouillet
 *
 */
@Entity
public abstract class PlatformSensorData extends DefaultData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6673967325185223939L;

	/**
	 * Default no-args constructor.
	 */
	public PlatformSensorData() {
	}

	/**
	 * Constructor which accepts three parameters to initialize itself.
	 *
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorTypeIdent
	 *            The unique identifier of the sensor type.
	 */
	public PlatformSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);
	}

}
