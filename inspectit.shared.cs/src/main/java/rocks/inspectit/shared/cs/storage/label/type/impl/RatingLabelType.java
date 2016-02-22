package rocks.inspectit.shared.cs.storage.label.type.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;

/**
 * Type of the label to denote the Rating.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@DiscriminatorValue("RTLT")
public class RatingLabelType extends AbstractStorageLabelType<String> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8658019301290640860L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOnePerStorage() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValueReusable() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultiType() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

}
