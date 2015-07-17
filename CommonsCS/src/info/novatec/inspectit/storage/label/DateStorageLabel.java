package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.text.DateFormat;
import java.util.Date;

/**
 * Label that has a {@link Date} as a value.
 * 
 * @author Ivan Senic
 * 
 */
public class DateStorageLabel extends AbstractStorageLabel<Date> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -170345155369176042L;

	/**
	 * Label value.
	 */
	private Date dateValue;

	/**
	 * Storage label type.
	 */
	private AbstractStorageLabelType<Date> storageLabelType;

	/**
	 * Default constructor.
	 */
	public DateStorageLabel() {
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param dateValue
	 *            Label value.
	 * @param storageLabelType
	 *            Label type.
	 */
	public DateStorageLabel(Date dateValue, AbstractStorageLabelType<Date> storageLabelType) {
		this.dateValue = dateValue;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getValue() {
		return dateValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Date value) {
		dateValue = (Date) value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormatedValue() {
		return DateFormat.getDateInstance().format(dateValue);
	}

	/**
	 * Gets {@link #dateValue}.
	 * 
	 * @return {@link #dateValue}
	 */
	public Date getDateValue() {
		return dateValue;
	}

	/**
	 * Sets {@link #dateValue}.
	 * 
	 * @param dateValue
	 *            New value for {@link #dateValue}
	 */
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	/**
	 * Gets {@link #storageLabelType}.
	 * 
	 * @return {@link #storageLabelType}
	 */
	@Override
	public AbstractStorageLabelType<Date> getStorageLabelType() {
		return storageLabelType;
	}

	/**
	 * Sets {@link #storageLabelType}.
	 * 
	 * @param storageLabelType
	 *            New value for {@link #storageLabelType}
	 */
	public void setStorageLabelType(AbstractStorageLabelType<Date> storageLabelType) {
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
		result = prime * result + ((storageLabelType == null) ? 0 : storageLabelType.hashCode());
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
		DateStorageLabel other = (DateStorageLabel) obj;
		// ISE: Tweaked here a bit of generated equals
		if (dateValue == null) {
			if (other.dateValue != null) {
				return false;
			}
		} else if (other.dateValue == null) {
			return false;
		} else if (dateValue.getTime() != other.dateValue.getTime()) {
			return false;
		}
		if (storageLabelType == null) {
			if (other.storageLabelType != null) {
				return false;
			}
		} else if (!storageLabelType.equals(other.storageLabelType)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AbstractStorageLabel<?> other) {
		int typeCompare = storageLabelType.compareTo(other.getStorageLabelType());
		if (0 != typeCompare) {
			return typeCompare;
		} else {
			if (!DateStorageLabel.class.isAssignableFrom(other.getClass())) {
				return super.compareTo(other);
			} else {
				DateStorageLabel otherLabel = (DateStorageLabel) other;
				return dateValue.compareTo(otherLabel.dateValue);
			}
		}
	}

}
