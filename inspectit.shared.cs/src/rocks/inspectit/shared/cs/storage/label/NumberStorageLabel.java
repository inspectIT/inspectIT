package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.text.NumberFormat;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Label that has a {@link Number} as a value.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
public class NumberStorageLabel extends AbstractStorageLabel<Number> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 7550228008433561954L;

	/**
	 * Label value.
	 */
	@NotNull
	private Number numberValue;

	/**
	 * Default constructor.
	 */
	public NumberStorageLabel() {
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param numberValue
	 *            Label value.
	 * @param storageLabelType
	 *            Label type.
	 */
	public NumberStorageLabel(Number numberValue, AbstractStorageLabelType<Number> storageLabelType) {
		super(storageLabelType);
		this.numberValue = numberValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Number getValue() {
		return numberValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Number value) {
		numberValue = (Number) value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormatedValue() {
		return NumberFormat.getInstance().format(numberValue);
	}

	/**
	 * Gets {@link #numberValue}.
	 * 
	 * @return {@link #numberValue}
	 */
	public Number getNumberValue() {
		return numberValue;
	}

	/**
	 * Sets {@link #numberValue}.
	 * 
	 * @param numberValue
	 *            New value for {@link #numberValue}
	 */
	public void setNumberValue(Number numberValue) {
		this.numberValue = numberValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((numberValue == null) ? 0 : numberValue.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NumberStorageLabel other = (NumberStorageLabel) obj;
		if (numberValue == null) {
			if (other.numberValue != null) {
				return false;
			}
		} else if (!numberValue.equals(other.numberValue)) {
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
			if (!NumberStorageLabel.class.isAssignableFrom(other.getClass())) {
				return super.compareTo(other);
			} else {
				NumberStorageLabel otherLabel = (NumberStorageLabel) other;
				double res = numberValue.doubleValue() - otherLabel.numberValue.doubleValue();
				if (res > 0) {
					return 1;
				} else if (res < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
}
