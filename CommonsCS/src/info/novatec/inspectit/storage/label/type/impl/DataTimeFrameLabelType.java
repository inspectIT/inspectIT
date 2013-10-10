package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.util.TimeFrame;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Label type to define the data time-frame in storage.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@DiscriminatorValue("TFLT")
public class DataTimeFrameLabelType extends AbstractStorageLabelType<TimeFrame> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -6293072492276850761L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGroupingEnabled() {
		return false;
	}

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
		return false;
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
	public boolean isEditable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<TimeFrame> getValueClass() {
		return TimeFrame.class;
	}

}
