package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.Sizeable;

import java.io.Serializable;

/**
 * This class provide informations about system properties of the virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class VmArgumentData implements Serializable, Sizeable {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -8210901536567725333L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private long id;

	/**
	 * The system information id.
	 */
	private long systemInformationId;

	/**
	 * The name of the virtual machine system property.
	 */
	private String vmName;

	/**
	 * The value of the virtual machine system property.
	 */
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setSystemInformationId(long systemInformationId) {
		this.systemInformationId = systemInformationId;
	}

	public long getSystemInformationId() {
		return systemInformationId;
	}

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getVmValue() {
		return vmValue;
	}

	public void setVmValue(String vmValue) {
		this.vmValue = vmValue;
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
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(2, 0, 0, 0, 2, 0);
		size += objectSizes.getSizeOf(vmName);
		size += objectSizes.getSizeOf(vmValue);
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		return getObjectSize(objectSizes);
	}

}
