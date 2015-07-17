package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;

/**
 * Custom label type that holds {@link String} values.
 * 
 * @author Ivan Senic
 * 
 */
public class CustomStringLabelType extends AbstractCustomStorageLabelType<String> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3504635667393667274L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

}
