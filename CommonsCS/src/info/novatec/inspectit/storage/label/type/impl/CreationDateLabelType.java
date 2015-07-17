package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.Date;

/**
 * Type of the label to denote the date Storage was created.
 * 
 * @author Ivan Senic
 * 
 */
public class CreationDateLabelType extends AbstractStorageLabelType<Date> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8661627410668828044L;

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
	public Class<Date> getValueClass() {
		return Date.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEditable() {
		return false;
	}

}
