package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;

import java.util.Date;

/**
 * Custom label type that holds {@link Date} values.
 * 
 * @author Ivan Senic
 * 
 */
public class CustomDateLabelType extends AbstractCustomStorageLabelType<Date> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -7177067097630951476L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Date> getValueClass() {
		return Date.class;
	}

}
