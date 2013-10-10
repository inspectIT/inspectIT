package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.indexing.IIndexQuery;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * The {@link MethodSensorData} abstract class is extended by all data & value objects which are
 * used for gathered measurements from instrumented methods. Thus an additional identifier is
 * necessary to store the unique method identifier.
 * 
 * @author Patrice Bouillet
 * 
 */
@Entity
public abstract class MethodSensorData extends DefaultData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7655082885002510364L;

	/**
	 * The unique identifier of the method.
	 */
	private long methodIdent;

	/**
	 * Contains optional information about the contents of some fields / parameters etc.
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<ParameterContentData> parameterContentData;

	/**
	 * Default no-args constructor.
	 */
	public MethodSensorData() {
	}

	/**
	 * Constructor which accepts four parameters to initialize itself.
	 * 
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 */
	public MethodSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);

		this.methodIdent = methodIdent;
	}

	/**
	 * Constructor which accepts four parameters to initialize itself.
	 * 
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 * @param parameterContentData
	 *            the parameter contents.
	 */
	public MethodSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List<ParameterContentData> parameterContentData) {
		this(timeStamp, platformIdent, sensorTypeIdent, methodIdent);

		if (null != parameterContentData) {
			this.parameterContentData = new HashSet<ParameterContentData>(parameterContentData);
		}
	}

	/**
	 * Gets {@link #methodIdent}.
	 * 
	 * @return {@link #methodIdent}
	 */
	public long getMethodIdent() {
		return methodIdent;
	}

	/**
	 * Sets {@link #methodIdent}.
	 * 
	 * @param methodIdent
	 *            New value for {@link #methodIdent}
	 */
	public void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * Adds parameter content data.
	 * 
	 * @param parameterContent
	 *            the data to add.
	 */
	public void addParameterContentData(ParameterContentData parameterContent) {
		if (null == parameterContentData) {
			parameterContentData = new HashSet<ParameterContentData>();
		}

		parameterContentData.add(parameterContent);
	}

	/**
	 * Gets {@link #parameterContentData}.
	 * 
	 * @return {@link #parameterContentData}
	 */
	public Set<ParameterContentData> getParameterContentData() {
		return parameterContentData;
	}

	/**
	 * Sets {@link #parameterContentData}.
	 * 
	 * @param parameterContentData
	 *            New value for {@link #parameterContentData}
	 */
	public void setParameterContentData(Set<ParameterContentData> parameterContentData) {
		this.parameterContentData = parameterContentData;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
		result = prime * result + ((parameterContentData == null) ? 0 : parameterContentData.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MethodSensorData other = (MethodSensorData) obj;
		if (methodIdent != other.methodIdent) {
			return false;
		}
		if (parameterContentData == null) {
			if (other.parameterContentData != null) {
				return false;
			}
		} else if (!parameterContentData.equals(other.parameterContentData)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 1, 0);
		if (parameterContentData instanceof HashSet) {
			size += objectSizes.getSizeOfHashSet(parameterContentData.size(), 0);
			for (ParameterContentData paramContentData : parameterContentData) {
				size += objectSizes.getSizeOf(paramContentData);
			}
		}
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isQueryComplied(IIndexQuery query) {
		if (query.getMethodIdent() != 0 && query.getMethodIdent() != methodIdent) {
			return false;
		}
		return super.isQueryComplied(query);
	}

}
