package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.indexing.IIndexQuery;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
 * The {@link DefaultData} class is the base class for all data and value objects. Data objects are
 * persisted on the CMR and can be requested from the interfaces. Value Objects on the other hand
 * are only used as a transmission container from the Agent(s) to the CMR.
 * <p>
 * Every value object implementation needs to override the {@link #finalizeData()} method to return
 * a data object which can be persisted.
 * <p>
 * Data objects are free to use the {@link #finalizeData()} method to generate some additional
 * values (like the average).
 * 
 * @author Patrice Bouillet
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQuery(name = DefaultData.DELETE_FOR_PLATFORM_ID, query = "DELETE FROM DefaultData d WHERE d.platformIdent=:platformIdent")
public abstract class DefaultData implements Serializable, Sizeable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5195625080367033147L;

	/**
	 * Constant for deleteForPlatformId query.
	 */
	public static final String DELETE_FOR_PLATFORM_ID = "DefaultData.deleteForPlatformId";

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEFAULT_DATA_SEQUENCE")
	@SequenceGenerator(name = "DEFAULT_DATA_SEQUENCE", sequenceName = "DEFAULT_DATA_SEQUENCE")
	private long id;

	/**
	 * The unique identifier of the platform.
	 */
	private long platformIdent;

	/**
	 * The unique identifier of the sensor type.
	 */
	private long sensorTypeIdent;

	/**
	 * The timestamp which shows when this information was created on the Agent.
	 */
	@NotNull
	private Timestamp timeStamp;

	/**
	 * Default no-args constructor.
	 */
	public DefaultData() {
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
	public DefaultData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		this.timeStamp = timeStamp;
		this.platformIdent = platformIdent;
		this.sensorTypeIdent = sensorTypeIdent;
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #platformIdent}.
	 * 
	 * @return {@link #platformIdent}
	 */
	public long getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * Sets {@link #platformIdent}.
	 * 
	 * @param platformIdent
	 *            New value for {@link #platformIdent}
	 */
	public void setPlatformIdent(long platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #sensorTypeIdent}.
	 * 
	 * @return {@link #sensorTypeIdent}
	 */
	public long getSensorTypeIdent() {
		return sensorTypeIdent;
	}

	/**
	 * Sets {@link #sensorTypeIdent}.
	 * 
	 * @param sensorTypeIdent
	 *            New value for {@link #sensorTypeIdent}
	 */
	public void setSensorTypeIdent(long sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

	/**
	 * Gets {@link #timeStamp}.
	 * 
	 * @return {@link #timeStamp}
	 */
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets {@link #timeStamp}.
	 * 
	 * @param timeStamp
	 *            New value for {@link #timeStamp}
	 */
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * This method has to be overridden by every implementation of a value object to return a
	 * {@link DefaultData} object which can be persisted.
	 * 
	 * @return Returns a {@link DefaultData} object which can be persisted.
	 */
	public DefaultData finalizeData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (platformIdent ^ (platformIdent >>> 32));
		result = prime * result + (int) (sensorTypeIdent ^ (sensorTypeIdent >>> 32));
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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
		DefaultData other = (DefaultData) obj;
		if (id != other.id) {
			return false;
		}
		if (platformIdent != other.platformIdent) {
			return false;
		}
		if (sensorTypeIdent != other.sensorTypeIdent) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @return Approximate object size in bytes.
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		return this.getObjectSize(objectSizes, true);
	}

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * <p>
	 * This method needs to be overridden by all subclasses.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @param doAlign
	 *            Should the align of the bytes occur. Note that super classes objects should never
	 *            align the result because the align occurs only one time per whole object.
	 * @return Approximate object size in bytes.
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 3, 0);
		size += objectSizes.getSizeOf(timeStamp);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * Returns if the object is complied with passed {@link IIndexQuery}. This method will only
	 * return true if the object data correspond to the searching parameters set in
	 * {@link IIndexQuery}.
	 * 
	 * @param query
	 *            Query to be check against.
	 * @return True if the object is complied with query, otherwise false.
	 */
	public boolean isQueryComplied(IIndexQuery query) {
		if (query.getObjectClasses() != null && !query.getObjectClasses().contains(this.getClass())) {
			return false;
		}
		if (query.getMinId() > id) {
			return false;
		}
		if (query.getPlatformIdent() != 0 && query.getPlatformIdent() != platformIdent) {
			return false;
		}
		if (query.getSensorTypeIdent() != 0 && query.getSensorTypeIdent() != sensorTypeIdent) {
			return false;
		}
		if (!query.isInInterval(timeStamp)) {
			return false;
		}
		if (!query.areAllRestrictionsFulfilled(this)) {
			return false;
		}

		return true;
	}
}
