package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;

/**
 * Custom label type that holds {@link Number} values.
 * 
 * @author Ivan Senic
 * 
 */
public class CustomNumberLabelType extends AbstractCustomStorageLabelType<Number> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6487215082111553500L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Number> getValueClass() {
		return Number.class;
	}

}
