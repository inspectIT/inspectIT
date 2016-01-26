package rocks.inspectit.agent.java.config.impl;

import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;

/**
 * Container for the values which describe the monitored JMX MBean. It stores all the values defined in a config
 * file for later access.
 *
 * @author Alfred Krauss
 */
public class JmxSensorConfig {

	/**
	 * SensorType of this config.
	 */
	private JmxSensorTypeConfig jmxSensorTypeConfig;

	/**
	 * The hash value of this sensor type.
	 */
	private long id = -1;

	/**
	 * Name of the monitored attribute of the MBean.
	 */
	private String attributeName;

	/**
	 * The ObjectName of the to the attribute corresponding MBean.
	 */
	private String mBeanObjectName;

	/**
	 * The ID of the attribute.
	 */
	private long mBeanAttributeId;

	/**
	 * The description of the attribute.
	 */
	private String mBeanAttributeDescription;

	/**
	 * The type of the attribute.
	 */
	private String mBeanAttributeType;

	/**
	 * True if the attribute has a is-getter.
	 */
	private boolean mBeanAttributeIsIs;

	/**
	 * True if the attribute is readable.
	 */
	private boolean mBeanAttributeIsReadable;

	/**
	 * True if the attribute is writable.
	 */
	private boolean mBeanAttributeIsWritable;

	/**
	 * Returns the config of the Sensor if assigned else a new one is created and stored.
	 *
	 * @return JmxSensorType config of this sensor.
	 */
	public JmxSensorTypeConfig getJmxSensorTypeConfig() {
		return jmxSensorTypeConfig;
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
	 * Gets {@link #attributeName}.
	 *
	 * @return {@link #attributeName}
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Sets {@link #attributeName}.
	 *
	 * @param attributeName
	 *            New value for {@link #attributeName}
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * Gets {@link #mBeanObjectName}.
	 *
	 * @return {@link #mBeanObjectName}
	 */
	public String getmBeanObjectName() {
		return mBeanObjectName;
	}

	/**
	 * Sets {@link #mBeanObjectName}.
	 *
	 * @param mBeanObjectName
	 *            New value for {@link #mBeanObjectName}
	 */
	public void setmBeanObjectName(String mBeanObjectName) {
		this.mBeanObjectName = mBeanObjectName;
	}

	/**
	 * Gets {@link #mBeanAttributeId}.
	 *
	 * @return {@link #mBeanAttributeId}
	 */
	public long getmBeanAttributeId() {
		return mBeanAttributeId;
	}

	/**
	 * Sets {@link #mBeanAttributeId}.
	 *
	 * @param mBeanAttributeId
	 *            New value for {@link #mBeanAttributeId}
	 */
	public void setmBeanAttributeId(long mBeanAttributeId) {
		this.mBeanAttributeId = mBeanAttributeId;
	}

	/**
	 * Gets {@link #mBeanAttributeDescription}.
	 *
	 * @return {@link #mBeanAttributeDescription}
	 */
	public String getmBeanAttributeDescription() {
		return mBeanAttributeDescription;
	}

	/**
	 * Sets {@link #mBeanAttributeDescription}.
	 *
	 * @param mBeanAttributeDescription
	 *            New value for {@link #mBeanAttributeDescription}
	 */
	public void setmBeanAttributeDescription(String mBeanAttributeDescription) {
		this.mBeanAttributeDescription = mBeanAttributeDescription;
	}

	/**
	 * Gets {@link #mBeanAttributeType}.
	 *
	 * @return {@link #mBeanAttributeType}
	 */
	public String getmBeanAttributeType() {
		return mBeanAttributeType;
	}

	/**
	 * Sets {@link #mBeanAttributeType}.
	 *
	 * @param mBeanAttributeType
	 *            New value for {@link #mBeanAttributeType}
	 */
	public void setmBeanAttributeType(String mBeanAttributeType) {
		this.mBeanAttributeType = mBeanAttributeType;
	}

	/**
	 * Gets {@link #mBeanAttributeIsIs}.
	 *
	 * @return {@link #mBeanAttributeIsIs}
	 */
	public Boolean getmBeanAttributeIsIs() {
		return mBeanAttributeIsIs;
	}

	/**
	 * Sets {@link #mBeanAttributeIsIs}.
	 *
	 * @param mBeanAttributeIsIs
	 *            New value for {@link #mBeanAttributeIsIs}
	 */
	public void setmBeanAttributeIsIs(Boolean mBeanAttributeIsIs) {
		this.mBeanAttributeIsIs = mBeanAttributeIsIs;
	}

	/**
	 * Gets {@link #mBeanAttributeIsReadable}.
	 *
	 * @return {@link #mBeanAttributeIsReadable}
	 */
	public Boolean getmBeanAttributeIsReadable() {
		return mBeanAttributeIsReadable;
	}

	/**
	 * Sets {@link #mBeanAttributeIsReadable}.
	 *
	 * @param mBeanAttributeIsReadable
	 *            New value for {@link #mBeanAttributeIsReadable}
	 */
	public void setmBeanAttributeIsReadable(Boolean mBeanAttributeIsReadable) {
		this.mBeanAttributeIsReadable = mBeanAttributeIsReadable;
	}

	/**
	 * Gets {@link #mBeanAttributeIsWritable}.
	 *
	 * @return {@link #mBeanAttributeIsWritable}
	 */
	public Boolean getmBeanAttributeIsWritable() {
		return mBeanAttributeIsWritable;
	}

	/**
	 * Sets {@link #mBeanAttributeIsWritable}.
	 *
	 * @param mBeanAttributeIsWritable
	 *            New value for {@link #mBeanAttributeIsWritable}
	 */
	public void setmBeanAttributeIsWritable(Boolean mBeanAttributeIsWritable) {
		this.mBeanAttributeIsWritable = mBeanAttributeIsWritable;
	}

	/**
	 * Sets {@link #jmxSensorTypeConfig}.
	 *
	 * @param jmxSensorTypeConfig
	 *            New value for {@link #jmxSensorTypeConfig}
	 */
	public void setJmxSensorTypeConfig(JmxSensorTypeConfig jmxSensorTypeConfig) {
		this.jmxSensorTypeConfig = jmxSensorTypeConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JmxSensorConfig [jmxSensorTypeConfig=" + jmxSensorTypeConfig + ", id=" + id + ", attributeName=" + attributeName + ", mBeanObjectName=" + mBeanObjectName + ", mBeanAttributeId="
				+ mBeanAttributeId + ", mBeanAttributeDescription=" + mBeanAttributeDescription + ", mBeanAttributeType=" + mBeanAttributeType + ", mBeanAttributeIsIs=" + mBeanAttributeIsIs
				+ ", mBeanAttributeIsReadable=" + mBeanAttributeIsReadable + ", mBeanAttributeIsWritable=" + mBeanAttributeIsWritable + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((jmxSensorTypeConfig == null) ? 0 : jmxSensorTypeConfig.hashCode());
		result = prime * result + ((mBeanAttributeDescription == null) ? 0 : mBeanAttributeDescription.hashCode());
		result = prime * result + (int) (mBeanAttributeId ^ (mBeanAttributeId >>> 32));
		result = prime * result + (mBeanAttributeIsIs ? 1231 : 1237);
		result = prime * result + (mBeanAttributeIsReadable ? 1231 : 1237);
		result = prime * result + (mBeanAttributeIsWritable ? 1231 : 1237);
		result = prime * result + ((mBeanAttributeType == null) ? 0 : mBeanAttributeType.hashCode());
		result = prime * result + ((mBeanObjectName == null) ? 0 : mBeanObjectName.hashCode());
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
		JmxSensorConfig other = (JmxSensorConfig) obj;
		if (attributeName == null) {
			if (other.attributeName != null) {
				return false;
			}
		} else if (!attributeName.equals(other.attributeName)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (jmxSensorTypeConfig == null) {
			if (other.jmxSensorTypeConfig != null) {
				return false;
			}
		} else if (!jmxSensorTypeConfig.equals(other.jmxSensorTypeConfig)) {
			return false;
		}
		if (mBeanAttributeDescription == null) {
			if (other.mBeanAttributeDescription != null) {
				return false;
			}
		} else if (!mBeanAttributeDescription.equals(other.mBeanAttributeDescription)) {
			return false;
		}
		if (mBeanAttributeId != other.mBeanAttributeId) {
			return false;
		}
		if (mBeanAttributeIsIs != other.mBeanAttributeIsIs) {
			return false;
		}
		if (mBeanAttributeIsReadable != other.mBeanAttributeIsReadable) {
			return false;
		}
		if (mBeanAttributeIsWritable != other.mBeanAttributeIsWritable) {
			return false;
		}
		if (mBeanAttributeType == null) {
			if (other.mBeanAttributeType != null) {
				return false;
			}
		} else if (!mBeanAttributeType.equals(other.mBeanAttributeType)) {
			return false;
		}
		if (mBeanObjectName == null) {
			if (other.mBeanObjectName != null) {
				return false;
			}
		} else if (!mBeanObjectName.equals(other.mBeanObjectName)) {
			return false;
		}
		return true;
	}

}
