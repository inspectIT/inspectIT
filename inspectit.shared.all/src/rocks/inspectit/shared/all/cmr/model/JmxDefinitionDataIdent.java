package info.novatec.inspectit.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
 * The Jmx Definition Data Ident class is used to store the different definition data which are
 * collecting data of the target attributes of the specified MBeans.
 * 
 * @author Alfred Krauss
 * @author Marius Oehler
 * 
 */
@Entity
@NamedQueries({ @NamedQuery(name = JmxDefinitionDataIdent.FIND_BY_PLATFORM_AND_EXAMPLE, query = "SELECT j FROM JmxDefinitionDataIdent j JOIN j.platformIdent p WHERE p.id=:platformIdentId AND j.mBeanObjectName=:mBeanObjectName AND j.mBeanAttributeName=:mBeanAttributeName") })
public class JmxDefinitionDataIdent implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5190671450845879357L;

	/**
	 * Constant for findByPlatformAndExample query.
	 * <p>
	 * Parameters in the query:
	 * <ul>
	 * <li>platformIdentId
	 * <li>mBeanObjectName
	 * <li>mBeanAttributeName
	 * </ul>
	 * </p>
	 */
	public static final String FIND_BY_PLATFORM_AND_EXAMPLE = "JmxDefinitionDataIdent.findByPlatformAndExample";

	/**
	 * The ID on the CMR.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JMX_DATA_IDENT_SEQUENCE")
	@SequenceGenerator(name = "JMX_DATA_IDENT_SEQUENCE", sequenceName = "JMX_DATA_IDENT_SEQUENCE")
	private long id;

	/**
	 * The timestamp of initialization.
	 */
	@NotNull
	private Timestamp timeStamp;

	/**
	 * The many-to-one association to the {@link PlatformIdent} object.
	 */
	@ManyToOne
	private PlatformIdent platformIdent;

	/**
	 * The ObjectName of the to the attribute corresponding MBean.
	 */
	@NotNull
	private String mBeanObjectName;

	/**
	 * The name of the attribute.
	 */
	@NotNull
	private String mBeanAttributeName;

	/**
	 * The description of the attribute.
	 */
	private String mBeanAttributeDescription;

	/**
	 * The type of the attribute.
	 */
	@NotNull
	private String mBeanAttributeType;

	/**
	 * True if the attribute has an is-getter.
	 */
	@NotNull
	private Boolean mBeanAttributeIsIs;

	/**
	 * True if the attribute is readable.
	 */
	@NotNull
	private Boolean mBeanAttributeIsReadable;

	/**
	 * True if the attribute is writable.
	 */
	@NotNull
	private Boolean mBeanAttributeIsWritable;

	/**
	 * Returns the domain name which is derived from {@link #mBeanObjectName}.
	 * 
	 * @return the package name
	 */
	public String getDerivedDomainName() {
		// Possible object name: java.lang:type=GarbageCollector,name=PS MarkSweep
		Pattern pattern = Pattern.compile("([^:]+):[\\w\\s]+=([\\w\\s]+),[\\w\\s]+=([\\w\\s]+)");
		Matcher matcher = pattern.matcher(mBeanObjectName);

		if (matcher.find()) {
			String g1 = matcher.group(1);
			String g2 = matcher.group(2);

			return g1 + "." + g2;
		} else {
			return mBeanObjectName.split(":")[0].trim();
		}
	}

	/**
	 * Returns the type name which is derived from {@link #mBeanObjectName}.
	 * 
	 * @return the type name
	 */
	public String getDerivedTypeName() {
		String[] splitted = mBeanObjectName.split("=");
		return splitted[splitted.length - 1].trim();
	}

	/**
	 * Returns a combination of domain, type and attribute-name.
	 * 
	 * @return a derived full name
	 */
	public String getDerivedFullName() {
		return getDerivedDomainName() + "." + getDerivedTypeName() + ":" + getmBeanAttributeName();
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
	 * Gets {@link #platformIdent}.
	 * 
	 * @return {@link #platformIdent}
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * Sets {@link #platformIdent}.
	 * 
	 * @param platformIdent
	 *            New value for {@link #platformIdent}
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #mBeanAttributeName}.
	 * 
	 * @return {@link #mBeanAttributeName}
	 */
	public String getmBeanAttributeName() {
		return mBeanAttributeName;
	}

	/**
	 * Sets {@link #mBeanAttributeName}.
	 * 
	 * @param mBeanAttributeName
	 *            New value for {@link #mBeanAttributeName}
	 */
	public void setmBeanAttributeName(String mBeanAttributeName) {
		this.mBeanAttributeName = mBeanAttributeName;
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((mBeanAttributeDescription == null) ? 0 : mBeanAttributeDescription.hashCode());
		result = prime * result + ((mBeanAttributeIsIs == null) ? 0 : mBeanAttributeIsIs.hashCode());
		result = prime * result + ((mBeanAttributeIsReadable == null) ? 0 : mBeanAttributeIsReadable.hashCode());
		result = prime * result + ((mBeanAttributeIsWritable == null) ? 0 : mBeanAttributeIsWritable.hashCode());
		result = prime * result + ((mBeanAttributeName == null) ? 0 : mBeanAttributeName.hashCode());
		result = prime * result + ((mBeanAttributeType == null) ? 0 : mBeanAttributeType.hashCode());
		result = prime * result + ((mBeanObjectName == null) ? 0 : mBeanObjectName.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
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
		JmxDefinitionDataIdent other = (JmxDefinitionDataIdent) obj;
		if (id != other.id) {
			return false;
		}
		if (mBeanAttributeDescription == null) {
			if (other.mBeanAttributeDescription != null) {
				return false;
			}
		} else if (!mBeanAttributeDescription.equals(other.mBeanAttributeDescription)) {
			return false;
		}
		if (mBeanAttributeIsIs == null) {
			if (other.mBeanAttributeIsIs != null) {
				return false;
			}
		} else if (!mBeanAttributeIsIs.equals(other.mBeanAttributeIsIs)) {
			return false;
		}
		if (mBeanAttributeIsReadable == null) {
			if (other.mBeanAttributeIsReadable != null) {
				return false;
			}
		} else if (!mBeanAttributeIsReadable.equals(other.mBeanAttributeIsReadable)) {
			return false;
		}
		if (mBeanAttributeIsWritable == null) {
			if (other.mBeanAttributeIsWritable != null) {
				return false;
			}
		} else if (!mBeanAttributeIsWritable.equals(other.mBeanAttributeIsWritable)) {
			return false;
		}
		if (mBeanAttributeName == null) {
			if (other.mBeanAttributeName != null) {
				return false;
			}
		} else if (!mBeanAttributeName.equals(other.mBeanAttributeName)) {
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
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		return true;
	}

}
