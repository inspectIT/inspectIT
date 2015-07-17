package info.novatec.inspectit.indexing.restriction.impl;

import info.novatec.inspectit.indexing.restriction.AbstractIndexQueryRestriction;
import info.novatec.inspectit.indexing.restriction.IIndexQueryRestriction;

import java.util.Collection;

/**
 * 
 * Factory that provide different types of index query restrictions.
 * 
 * @author Ivan Senic
 * 
 */
public final class IndexQueryRestrictionFactory {

	/**
	 * Default private constructor because of the factory.
	 */
	private IndexQueryRestrictionFactory() {
	}

	/**
	 * Returns equals restriction. This restriction will check if the object supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is equal to the restriction value.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value.
	 * @return index query restriction
	 * @see EqualsIndexingRestriction
	 */
	public static IIndexQueryRestriction equal(String fieldName, Object restrictionValue) {
		return new EqualsIndexingRestriction(fieldName, restrictionValue);
	}

	/**
	 * Returns not equals restriction. This restriction will check if the object supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is not equal to the restriction value.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value.
	 * @return index query restriction
	 * @see NotEqualsIndexingRestriction
	 */
	public static IIndexQueryRestriction notEqual(String fieldName, Object restrictionValue) {
		return new NotEqualsIndexingRestriction(fieldName, restrictionValue);
	}

	/**
	 * Returns greater than restriction. This restriction will check if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is greater than the restriction value.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value. Must be comparable.
	 * @return index query restriction
	 * @see GreaterThanIndexingRestriction
	 */
	public static IIndexQueryRestriction greaterThan(String fieldName, Comparable<? extends Object> restrictionValue) {
		return new GreaterThanIndexingRestriction(fieldName, restrictionValue);
	}

	/**
	 * Returns greater equals restriction. This restriction will check if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is greater or equal than the restriction
	 * value.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value. Must be comparable.
	 * @return index query restriction
	 * @see GreaterEqualsIndexingRestriction
	 */
	public static IIndexQueryRestriction greaterEqual(String fieldName, Comparable<? extends Object> restrictionValue) {
		return new GreaterEqualsIndexingRestriction(fieldName, restrictionValue);
	}

	/**
	 * Returns less than restriction. This restriction will check if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is less than the restriction value.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value. Must be comparable.
	 * @return index query restriction
	 * @see LessThanIndexingRestriction
	 */
	public static IIndexQueryRestriction lessThan(String fieldName, Comparable<? extends Object> restrictionValue) {
		return new LessThanIndexingRestriction(fieldName, restrictionValue);
	}

	/**
	 * Returns less equals restriction. This restriction will check if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is less or equal than the restriction
	 * value.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param restrictionValue
	 *            Restriction value. Must be comparable.
	 * @return index query restriction
	 * @see LessEqualsIndexingRestriction
	 */
	public static IIndexQueryRestriction lessEqual(String fieldName, Comparable<? extends Object> restrictionValue) {
		return new LessEqualsIndexingRestriction(fieldName, restrictionValue);
	}

	/**
	 * Returns is null restriction. This restriction will check if the object supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is null.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @return index query restriction
	 * @see IsNullIndexingRestriction
	 */
	public static IIndexQueryRestriction isNull(String fieldName) {
		return new IsNullIndexingRestriction(fieldName);
	}

	/**
	 * Returns is not null restriction. This restriction will check if the object supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is not null.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @return index query restriction
	 * @see IsNotNullIndexingRestriction
	 */
	public static IIndexQueryRestriction isNotNull(String fieldName) {
		return new IsNotNullIndexingRestriction(fieldName);
	}

	/**
	 * Returns is in collection restriction. This restriction will check if the object supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is in the supplied collection.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param collection
	 *            Collection to check in.
	 * @return index query restriction
	 * @see Collection#contains(Object);
	 */
	public static IIndexQueryRestriction isInCollection(String fieldName, Collection<?> collection) {
		return new IsInCollection(fieldName, collection);
	}

	/**
	 * Returns are all in collection restriction. This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is a collection, and all it members are
	 * also contained in the collection provided with object construction. Note that this
	 * restriction has to be bounded to a {@link Collection} field.
	 * 
	 * @param fieldName
	 *            Name of the field that is restriction bounded to.
	 * @param collection
	 *            Collection to check in.
	 * @return index query restriction
	 * @see Collection#containsAll(Object);
	 */
	public static IIndexQueryRestriction areAllInCollection(String fieldName, Collection<?> collection) {
		return new AreAllInCollection(fieldName, collection);
	}

	/**
	 * This restriction checks if the restriction value and object supplied via
	 * {@link #isFulfilled(Object)} are equal, by terms of {@link Object#equals(Object)} method.
	 * Note that special care is needed if this restriction is used with primitive types, because
	 * this method will not returned true if the restriction value is 1 for example, and it is
	 * checked against field that is of a long primitive type, because Java will create an Integer
	 * object from literal 1.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class EqualsIndexingRestriction extends ObjectIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param restrictionValue
		 *            Restriction value.
		 */
		public EqualsIndexingRestriction(String fieldName, Object restrictionValue) {
			super(fieldName, restrictionValue);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the restriction value and supplied object are equal, by terms of
		 * {@link Object#equals(Object)} method. If restriction value is null, method will then
		 * return true if given object is also null.
		 * 
		 * @param object
		 * @returns
		 * @see Object#equals(Object)
		 */
		public boolean isFulfilled(Object object) {
			if (null == getRestrictionValue()) {
				return null == object;
			}
			return getRestrictionValue().equals(object);
		}

	}

	/**
	 * This restriction checks if the restriction value and object supplied via
	 * {@link #isFulfilled(Object)} are not equal, by terms of {@link Object#equals(Object)} method.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class NotEqualsIndexingRestriction extends ObjectIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param restrictionValue
		 *            Restriction value.
		 */
		public NotEqualsIndexingRestriction(String fieldName, Object restrictionValue) {
			super(fieldName, restrictionValue);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the restriction value and supplied object are not equal, by terms of
		 * {@link Object#equals(Object)} method. If restriction value is null, method will then
		 * return true if given object is not null.
		 * 
		 * @param object
		 * @returns
		 * @see Object#equals(Object)
		 */
		public boolean isFulfilled(Object object) {
			if (null == getRestrictionValue()) {
				return null != object;
			}
			return !getRestrictionValue().equals(object);
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is greater than the restriction value, by
	 * terms of {@link Comparable#compareTo(Object)} method.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class GreaterThanIndexingRestriction extends ComparableIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param restrictionValue
		 *            Restriction value.
		 */
		public GreaterThanIndexingRestriction(String fieldName, Comparable<? extends Object> restrictionValue) {
			super(fieldName, restrictionValue);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the supplied object is greater than the restriction value, by terms of
		 * {@link Comparable#compareTo(Object)} method. If restriction value is null, method will
		 * return false.
		 * 
		 * @param object
		 * @returns
		 * @see Comparable#compareTo(Object)
		 */
		@SuppressWarnings("unchecked")
		public boolean isFulfilled(Object object) {
			if (null == getRestrictionValue()) {
				return false;
			}
			return 0 > getRestrictionValue().compareTo(object);
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is greater or equal than the restriction
	 * value, by terms of {@link Comparable#compareTo(Object)} method.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class GreaterEqualsIndexingRestriction extends ComparableIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param restrictionValue
		 *            Restriction value.
		 */
		public GreaterEqualsIndexingRestriction(String fieldName, Comparable<? extends Object> restrictionValue) {
			super(fieldName, restrictionValue);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the supplied object is greater or equal than the restriction value, by terms of
		 * {@link Comparable#compareTo(Object)} method. If restriction value is null, method will
		 * return false.
		 * 
		 * @param object
		 * @returns
		 * @see Comparable#compareTo(Object)
		 */
		@SuppressWarnings("unchecked")
		public boolean isFulfilled(Object object) {
			if (null == getRestrictionValue()) {
				return false;
			}
			return 0 >= getRestrictionValue().compareTo(object);
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is less than the restriction value, by
	 * terms of {@link Comparable#compareTo(Object)} method.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class LessThanIndexingRestriction extends ComparableIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param restrictionValue
		 *            Restriction value.
		 */
		public LessThanIndexingRestriction(String fieldName, Comparable<? extends Object> restrictionValue) {
			super(fieldName, restrictionValue);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the supplied object is less than the restriction value, by terms of
		 * {@link Comparable#compareTo(Object)} method. If restriction value is null, method will
		 * return false.
		 * 
		 * @param object
		 * @returns
		 * @see Comparable#compareTo(Object)
		 */
		@SuppressWarnings("unchecked")
		public boolean isFulfilled(Object object) {
			if (null == getRestrictionValue()) {
				return false;
			}
			return 0 < getRestrictionValue().compareTo(object);
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is less or equal than the restriction
	 * value, by terms of {@link Comparable#compareTo(Object)} method.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class LessEqualsIndexingRestriction extends ComparableIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param restrictionValue
		 *            Restriction value.
		 */
		public LessEqualsIndexingRestriction(String fieldName, Comparable<? extends Object> restrictionValue) {
			super(fieldName, restrictionValue);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the supplied object is less or equal than the restriction value, by terms of
		 * {@link Comparable#compareTo(Object)} method. If restriction value is null, method will
		 * return false.
		 * 
		 * @param object
		 * @returns
		 * @see Comparable#compareTo(Object)
		 */
		@SuppressWarnings("unchecked")
		public boolean isFulfilled(Object object) {
			if (null == getRestrictionValue()) {
				return false;
			}
			return 0 <= getRestrictionValue().compareTo(object);
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is null.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class IsNullIndexingRestriction extends AbstractIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 */
		public IsNullIndexingRestriction(String fieldName) {
			super(fieldName);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the supplied object is null.
		 * 
		 * @param object
		 * @returns True if object is null, otherwise false.
		 */
		public boolean isFulfilled(Object object) {
			return object == null;
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is not null.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class IsNotNullIndexingRestriction extends AbstractIndexQueryRestriction {

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 */
		public IsNotNullIndexingRestriction(String fieldName) {
			super(fieldName);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Checks if the supplied object is not null.
		 * 
		 * @param object
		 * @returns True if object is not null, otherwise false.
		 */
		public boolean isFulfilled(Object object) {
			return object != null;
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is in a collection.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class IsInCollection extends AbstractIndexQueryRestriction {

		/**
		 * Collection to look in.
		 */
		private Collection<?> collection;

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param collection
		 *            Collection to look in.
		 */
		public IsInCollection(String fieldName, Collection<?> collection) {
			super(fieldName);
			this.collection = collection;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isFulfilled(Object fieldValue) {
			return collection != null && collection.contains(fieldValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((collection == null) ? 0 : collection.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			IsInCollection other = (IsInCollection) obj;
			if (collection == null) {
				if (other.collection != null) {
					return false;
				}
			} else if (!collection.equals(other.collection)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * This restriction checks if the value supplied via
	 * {@link IIndexQueryRestriction#isFulfilled(Object)} is a collection, and all it members are
	 * also contained in the collection provided with object construction. Note that this
	 * restriction has to be bounded to a {@link Collection} field.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class AreAllInCollection extends AbstractIndexQueryRestriction {

		/**
		 * Collection to look in.
		 */
		private Collection<?> collection;

		/**
		 * Default constructor.
		 * 
		 * @param fieldName
		 *            Name of the field that is restriction bounded to.
		 * @param collection
		 *            Collection to look in.
		 */
		public AreAllInCollection(String fieldName, Collection<?> collection) {
			super(fieldName);
			this.collection = collection;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isFulfilled(Object fieldValue) {
			if (fieldValue instanceof Collection<?>) {
				return collection.containsAll((Collection<?>) fieldValue);
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((collection == null) ? 0 : collection.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AreAllInCollection other = (AreAllInCollection) obj;
			if (collection == null) {
				if (other.collection != null) {
					return false;
				}
			} else if (!collection.equals(other.collection)) {
				return false;
			}
			return true;
		}

	}

}
