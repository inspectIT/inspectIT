package info.novatec.inspectit.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Class that connects the {@link MethodIdent} and {@link MethodSensorTypeIdent} and provides
 * additional information on the relationship.
 * 
 * @author Ivan Senic
 * 
 */
@Table(indexes = { @Index(name = "method_ident_to_sensor_type_idx", columnList = "methodIdent"), @Index(name = "method_ident_to_sensor_type_idx", columnList = "methodSensorTypeIdent") })
@NamedQuery(name = MethodIdentToSensorType.FIND_FOR_METHOD_ID_AND_METOHD_SENSOR_TYPE_ID, query = "SELECT m from MethodIdentToSensorType m JOIN m.methodIdent mi JOIN m.methodSensorTypeIdent ms WHERE mi.id=:methodIdentId AND ms.id=:methodSensorTypeIdentId")
@Entity
public class MethodIdentToSensorType implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3767712432753232084L;

	/**
	 * Constant for findForMethodAndMethodSensorType query.
	 * <p>
	 * Parameters in the query:
	 * <ul>
	 * <li>methodIdent
	 * <li>methodSensorTypeIdent
	 * </ul>
	 */
	public static final String FIND_FOR_METHOD_ID_AND_METOHD_SENSOR_TYPE_ID = "MethodIdentToSensorType.findForMethodIdAndMethodSensorTypeId";

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "METHOD_SENSOR_SEQUENCE")
	@SequenceGenerator(name = "METHOD_SENSOR_SEQUENCE", sequenceName = "METHOD_SENSOR_SEQUENCE")
	private Long id;

	/**
	 * {@link MethodIdent}.
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "methodIdent")
	private MethodIdent methodIdent;

	/**
	 * {@link MethodSensorTypeIdent}.
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "methodSensorTypeIdent")
	private MethodSensorTypeIdent methodSensorTypeIdent;

	/**
	 * Time-stamp represents last time the sensor on the method was registered.
	 */
	@NotNull
	private Timestamp timestamp;

	/**
	 * No-arg constructor.
	 */
	public MethodIdentToSensorType() {
	}

	/**
	 * Constructor that allows setting all values.
	 * 
	 * @param methodIdent
	 *            {@link MethodIdent}.
	 * @param methodSensorTypeIdent
	 *            {@link MethodSensorTypeIdent}.
	 * @param timestamp
	 *            Time-stamp represents last time the sensor on the method was registered.
	 */
	public MethodIdentToSensorType(MethodIdent methodIdent, MethodSensorTypeIdent methodSensorTypeIdent, Timestamp timestamp) {
		this.methodIdent = methodIdent;
		this.methodSensorTypeIdent = methodSensorTypeIdent;
		this.timestamp = timestamp;
	}

	/**
	 * Returns if the {@link MethodIdentToSensorType} is active, meaning if the latest agent
	 * registration included this instrumentation.
	 * 
	 * @return True if the latest agent registration included the {@link MethodSensorTypeIdent}
	 *         instrumentation on {@link MethodIdent}.
	 */
	public boolean isActive() {
		return timestamp.after(methodIdent.getPlatformIdent().getTimeStamp());
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #methodIdent}.
	 * 
	 * @return {@link #methodIdent}
	 */
	public MethodIdent getMethodIdent() {
		return methodIdent;
	}

	/**
	 * Sets {@link #methodIdent}.
	 * 
	 * @param methodIdent
	 *            New value for {@link #methodIdent}
	 */
	public void setMethodIdent(MethodIdent methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * Gets {@link #methodSensorTypeIdent}.
	 * 
	 * @return {@link #methodSensorTypeIdent}
	 */
	public MethodSensorTypeIdent getMethodSensorTypeIdent() {
		return methodSensorTypeIdent;
	}

	/**
	 * Sets {@link #methodSensorTypeIdent}.
	 * 
	 * @param methodSensorTypeIdent
	 *            New value for {@link #methodSensorTypeIdent}
	 */
	public void setMethodSensorTypeIdent(MethodSensorTypeIdent methodSensorTypeIdent) {
		this.methodSensorTypeIdent = methodSensorTypeIdent;
	}

	/**
	 * Gets {@link #timestamp}.
	 * 
	 * @return {@link #timestamp}
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets {@link #timestamp}.
	 * 
	 * @param timestamp
	 *            New value for {@link #timestamp}
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((methodIdent == null) ? 0 : methodIdent.hashCode());
		result = prime * result + ((methodSensorTypeIdent == null) ? 0 : methodSensorTypeIdent.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MethodIdentToSensorType other = (MethodIdentToSensorType) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (methodIdent == null) {
			if (other.methodIdent != null) {
				return false;
			}
		} else if (!methodIdent.equals(other.methodIdent)) {
			return false;
		}
		if (methodSensorTypeIdent == null) {
			if (other.methodSensorTypeIdent != null) {
				return false;
			}
		} else if (!methodSensorTypeIdent.equals(other.methodSensorTypeIdent)) {
			return false;
		}
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		} else if (!timestamp.equals(other.timestamp)) {
			return false;
		}
		return true;
	}

}
