package rocks.inspectit.shared.cs.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Abstract class for all XML adapters default value implementations.
 * <p>
 * These adapters will not marshal a value if it's equal to the default value.
 *
 * @param <T>
 *            Type of value.
 * @author Ivan Senic
 *
 */
public abstract class DefaultValue<T> extends XmlAdapter<T, T> {

	/**
	 * Returns default value.
	 *
	 * @return Returns default value.
	 */
	protected abstract T getDefaultValue();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T marshal(T value) throws Exception {
		if ((null == value) || value.equals(getDefaultValue())) {
			return null;
		} else {
			return value;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T unmarshal(T value) throws Exception {
		return value;
	}

	/**
	 * Default value for {@link Boolean#TRUE}.
	 *
	 * @author Ivan Senic
	 *
	 */
	public static class BooleanTrue extends DefaultValue<Boolean> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean getDefaultValue() {
			return Boolean.TRUE;
		}
	}

	/**
	 * Default value for {@link Boolean#FALSE}.
	 *
	 * @author Ivan Senic
	 *
	 */
	public static class BooleanFalse extends DefaultValue<Boolean> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean getDefaultValue() {
			return Boolean.FALSE;
		}
	}

	/**
	 * Default value for {@link Long}.
	 *
	 * @author Ivan Senic
	 *
	 */
	public static class LongZero extends DefaultValue<Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Long getDefaultValue() {
			return Long.valueOf(0);
		}
	}

}
