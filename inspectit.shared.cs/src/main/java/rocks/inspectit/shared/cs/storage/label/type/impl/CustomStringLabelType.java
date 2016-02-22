package rocks.inspectit.shared.cs.storage.label.type.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import rocks.inspectit.shared.cs.storage.label.type.AbstractCustomStorageLabelType;

/**
 * Custom label type that holds {@link String} values.
 * 
 * @author Ivan Senic
 * 
 */
@Entity
@DiscriminatorValue("CSLT")
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
