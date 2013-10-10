package info.novatec.inspectit.storage.label.type.impl;

import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Custom label type that holds {@link Number} values.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@DiscriminatorValue("CNLT")
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
