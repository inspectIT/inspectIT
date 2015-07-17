package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;

/**
 * Custom label type that holds {@link Boolean} values.
 * 
 * @author Ivan Senic
 * 
 */
public class CustomBooleanLabelType extends AbstractCustomStorageLabelType<Boolean> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2555296264644436351L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Boolean> getValueClass() {
		return Boolean.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValueReusable() {
		return false;
	}

}
