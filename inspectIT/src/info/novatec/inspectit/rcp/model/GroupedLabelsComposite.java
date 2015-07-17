package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.storage.label.AbstractStorageLabel;

/**
 * Composite for storage leafs when grouping by labels is used.
 * 
 * @author Ivan Senic
 * 
 */
public class GroupedLabelsComposite extends Composite implements Comparable<GroupedLabelsComposite> {

	/**
	 * Example label to represent the group.
	 */
	private AbstractStorageLabel<?> exampleLabel;

	/**
	 * No-arg constructor.
	 */
	public GroupedLabelsComposite() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param label
	 *            Example label to represent the group or <code>null</code> if this group can not be
	 *            represent by label.
	 */
	public GroupedLabelsComposite(AbstractStorageLabel<?> label) {
		super();
		this.exampleLabel = label;
	}

	/**
	 * Gets {@link #exampleLabel}.
	 * 
	 * @return {@link #exampleLabel}
	 */
	public AbstractStorageLabel<?> getLabel() {
		return exampleLabel;
	}

	/**
	 * Sets {@link #exampleLabel}.
	 * 
	 * @param label
	 *            New value for {@link #exampleLabel}
	 */
	public void setLabel(AbstractStorageLabel<?> label) {
		this.exampleLabel = label;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(GroupedLabelsComposite other) {
		if (null != exampleLabel && null != other.exampleLabel) {
			return exampleLabel.compareTo(other.exampleLabel);
		} else if (null == exampleLabel) {
			return 1;
		} else if (null == other.exampleLabel) {
			return -1;
		} else {
			return 0;
		}
	}

}
