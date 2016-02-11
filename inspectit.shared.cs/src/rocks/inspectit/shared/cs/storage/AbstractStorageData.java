package info.novatec.inspectit.storage;

import java.io.Serializable;

/**
 * Abstract storage data.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractStorageData implements IStorageData, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8161482616652852623L;

	/**
	 * Storage ID.
	 */
	private String id;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Size on disk in bytes.
	 */
	private long diskSize;

	/**
	 * Description.
	 */
	private String description;

	/**
	 * Version of the CMR on which the Storage is originally created.
	 */
	private String cmrVersion;

	/**
	 * {@inheritDoc}
	 */
	public String getStorageFolder() {
		return id;
	}

	/**
	 * @return the diskSize
	 */
	public long getDiskSize() {
		return diskSize;
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets {@link #name}.
	 * 
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 * 
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #description}.
	 * 
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets {@link #description}.
	 * 
	 * @param description
	 *            New value for {@link #description}
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets {@link #diskSize}.
	 * 
	 * @param diskSize
	 *            New value for {@link #diskSize}
	 */
	public void setDiskSize(long diskSize) {
		this.diskSize = diskSize;
	}

	/**
	 * Gets {@link #cmrVersion}.
	 * 
	 * @return {@link #cmrVersion}
	 */
	public String getCmrVersion() {
		return cmrVersion;
	}

	/**
	 * Sets {@link #cmrVersion}.
	 * 
	 * @param cmrVersion
	 *            New value for {@link #cmrVersion}
	 */
	public void setCmrVersion(String cmrVersion) {
		this.cmrVersion = cmrVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AbstractStorageData other = (AbstractStorageData) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "'" + name + "' (id=" + id + ")";
	}

}
