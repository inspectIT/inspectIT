package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.Sizeable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang.StringUtils;

/**
 * This class provide informations about system properties of the virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Entity
public class VmArgumentData implements Serializable, Sizeable {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -8210901536567725333L;

	/**
	 * Max length of parameter name and value.
	 */
	private static final int MAX_LENGTH = 10000;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_DATA_SEQUENCE")
	@SequenceGenerator(name = "VM_DATA_SEQUENCE", sequenceName = "VM_DATA_SEQUENCE")
	private long id;

	/**
	 * The name of the virtual machine system property.
	 */
	@Column(length = MAX_LENGTH)
	private String vmName;

	/**
	 * The value of the virtual machine system property.
	 */
	@Column(length = MAX_LENGTH)
	private String vmValue;

	/**
	 * Default no-args constructor.
	 */
	public VmArgumentData() {
	}

	/**
	 * Creates a new instance with the given parameters.
	 * 
	 * @param vmName
	 *            the name of the VM.
	 * @param vmValue
	 *            the value.
	 */
	public VmArgumentData(String vmName, String vmValue) {
		this.vmName = vmName;
		this.vmValue = vmValue;
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
	 * Gets {@link #vmName}.
	 * 
	 * @return {@link #vmName}
	 */
	public String getVmName() {
		return vmName;
	}

	/**
	 * Sets {@link #vmName}.
	 * 
	 * @param vmName
	 *            New value for {@link #vmName}
	 */
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	/**
	 * Gets {@link #vmValue}.
	 * 
	 * @return {@link #vmValue}
	 */
	public String getVmValue() {
		return vmValue;
	}

	/**
	 * Sets {@link #vmValue}.
	 * 
	 * @param vmValue
	 *            New value for {@link #vmValue}
	 */
	public void setVmValue(String vmValue) {
		this.vmValue = vmValue;
	}

	/**
	 * Checks for the {@link #vmName} and {@link #vmValue} lengths prior to persisting.
	 */
	@PrePersist
	protected void checkLengths() {
		if (StringUtils.isNotEmpty(vmName) && vmName.length() > MAX_LENGTH) {
			vmName = vmName.substring(0, MAX_LENGTH);
		}
		if (StringUtils.isNotEmpty(vmValue) && vmValue.length() > MAX_LENGTH) {
			vmValue = vmValue.substring(0, MAX_LENGTH);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vmName == null) ? 0 : vmName.hashCode());
		result = prime * result + ((vmValue == null) ? 0 : vmValue.hashCode());
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
		VmArgumentData other = (VmArgumentData) obj;
		if (vmName == null) {
			if (other.vmName != null) {
				return false;
			}
		} else if (!vmName.equals(other.vmName)) {
			return false;
		}
		if (vmValue == null) {
			if (other.vmValue != null) {
				return false;
			}
		} else if (!vmValue.equals(other.vmValue)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		return getObjectSize(objectSizes, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(2, 0, 0, 0, 1, 0);
		size += objectSizes.getSizeOf(vmName, vmValue);

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
