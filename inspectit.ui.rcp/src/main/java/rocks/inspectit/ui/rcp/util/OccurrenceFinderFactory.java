package rocks.inspectit.ui.rcp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.eclipse.jface.viewers.ViewerFilter;

import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.InspectIT;

/**
 * Factory for finding the occurrence of elements in the {@link InvocationSequenceData} based on the
 * element type.
 * 
 * @author Ivan Senic
 * 
 */
public final class OccurrenceFinderFactory {

	/**
	 * String used buy templates to denote a String that will not be checked when matching.
	 */
	private static final String TEMPLATE_STRING = "TEMPLATE_STRING";

	/**
	 * Map used by templates to denote a Map that will not be checked when matching. Currently not
	 * in use, but in future it might be needed.
	 */
	@SuppressWarnings("unused")
	private static final Map<Object, Object> TEMPLATE_MAP = new HashMap<Object, Object>(0);

	/**
	 * List used buy templates to denote a List that will not be checked when matching.
	 */
	private static final List<String> TEMPLATE_LIST = new ArrayList<String>(0);

	/**
	 * Set used buy templates to denote a Set that will not be checked when matching. Currently not
	 * in use, but in future it might be needed.
	 */
	@SuppressWarnings("unused")
	private static final Set<Object> TEMPLATE_SET = new HashSet<Object>(0);

	/**
	 * Private constructor because of the factory.
	 */
	private OccurrenceFinderFactory() {
	}

	/**
	 * Occurrence finder for {@link TimerData}.
	 */
	private static TimerOccurrenceFinder timerOccurrenceFinder = new TimerOccurrenceFinder();

	/**
	 * Occurrence finder for {@link SqlStatementData}.
	 */
	private static SqlOccurrenceFinder sqlOccurrenceFinder = new SqlOccurrenceFinder();

	/**
	 * Occurrence finder for {@link ExceptionSensorData}.
	 */
	private static ExceptionOccurrenceFinder exceptionOccurrenceFinder = new ExceptionOccurrenceFinder();

	/**
	 * Occurrence finder for {@link InvocationSequenceData}.
	 */
	private static InvocationOccurenceFinder invocationOccurenceFinder = new InvocationOccurenceFinder();

	/**
	 * Counts number of occurrences of the element in the given invocation.
	 * 
	 * @param invocation
	 *            Invocation to search in.
	 * @param element
	 *            Wanted element.
	 * @param filters
	 *            Array of filters that each found occurrence has to pass.
	 * @return Number of occurrences found and filtered.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ElementOccurrenceCount getOccurrenceCount(InvocationSequenceData invocation, Object element, ViewerFilter[] filters) {
		OccurrenceFinder finder = getOccurrenceFinder(element);
		return finder.getOccurrenceCount(invocation, element, filters, null);
	}

	/**
	 * Returns the {@link InvocationSequenceData} that holds the proper occurrence of the wanted
	 * element if it exists.
	 * 
	 * @param invocation
	 *            Invocation to search in.
	 * @param element
	 *            Wanted element.
	 * @param occurrence
	 *            Wanted occurrence.
	 * @param filters
	 *            Array of filters that each found occurrence has to pass.
	 * @return Returns the {@link InvocationSequenceData} that holds the proper occurrence of the
	 *         wanted element if it exists.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static InvocationSequenceData getOccurrence(InvocationSequenceData invocation, Object element, int occurrence, ViewerFilter[] filters) {
		OccurrenceFinder finder = getOccurrenceFinder(element);
		return finder.getOccurrence(invocation, element, new MutableInt(occurrence), filters);
	}

	/**
	 * Returns empty template.
	 * 
	 * @param <E>
	 *            Type of template.
	 * @param element
	 *            For element.
	 * @return Template to be used.
	 */
	@SuppressWarnings({ "unchecked" })
	public static <E> E getEmptyTemplate(E element) {
		OccurrenceFinder<E> finder = getOccurrenceFinder(element);
		return finder.getEmptyTemplate();
	}

	/**
	 * Returns the proper {@link OccurrenceFinder} for the given element, based on elements class.
	 * 
	 * @param element
	 *            Element.
	 * @return {@link OccurrenceFinder} or null if the finder does not exists for the given object
	 *         type.
	 */
	@SuppressWarnings("rawtypes")
	private static OccurrenceFinder getOccurrenceFinder(Object element) {
		if (SqlStatementData.class.isAssignableFrom(element.getClass())) {
			return sqlOccurrenceFinder;
		} else if (element.getClass().equals(TimerData.class) || element.getClass().equals(AggregatedTimerData.class)) {
			return timerOccurrenceFinder;
		} else if (ExceptionSensorData.class.isAssignableFrom(element.getClass())) {
			return exceptionOccurrenceFinder;
		} else if (InvocationSequenceData.class.isAssignableFrom(element.getClass())) {
			return invocationOccurenceFinder;
		}
		RuntimeException exception = new RuntimeException("Occurrence finder factory was not able to supply the correct occurrence finder for the object of class " + element.getClass().getName()
				+ ".");
		InspectIT.getDefault().createErrorDialog("Exception thrown during locating of stepping object.", exception, -1);
		throw exception;
	}

	/**
	 * Abstract class that holds the shared functionality of all occurrence finders.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <E>
	 *            Type of the element finder can locate.
	 */
	private abstract static class OccurrenceFinder<E> {

		/**
		 * Returns the number of children objects in invocation sequence that have the wanted
		 * template object defined. This method is recursive, and traverse the whole invocation
		 * tree.
		 * 
		 * @param invocationData
		 *            Top parent invocation sequence.
		 * @param template
		 *            Template data to search for.
		 * @param filters
		 *            Active filters of the tree viewer.
		 * @param elementOccurrence
		 *            Element occurrence count.
		 * @return Number of children in invocation that have template data set.
		 */
		public ElementOccurrenceCount getOccurrenceCount(InvocationSequenceData invocationData, E template, ViewerFilter[] filters, ElementOccurrenceCount elementOccurrence) {
			if (!getConcreteClass().isAssignableFrom(template.getClass())) {
				return null;
			}
			ElementOccurrenceCount occurrenceCount;
			if (null == elementOccurrence) {
				occurrenceCount = new ElementOccurrenceCount();
			} else {
				occurrenceCount = elementOccurrence;
			}

			boolean found = occurrenceFound(invocationData, template);
			if (found && filtersPassed(invocationData, filters)) {
				occurrenceCount.increaseVisibleOccurrences();
			} else if (found) {
				occurrenceCount.increaseFilteredOccurrences();
			}

			if (null != invocationData.getNestedSequences()) {
				for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
					getOccurrenceCount(child, template, filters, occurrenceCount);
				}
			}
			return occurrenceCount;
		}

		/**
		 * Get empty template.
		 * 
		 * @return Empty template.
		 */
		public abstract E getEmptyTemplate();

		/**
		 * Returns the {@link InvocationSequenceData} object that has the wanted template data
		 * object defined. The wanted occurrence of E object is defined via {@link #occurrencesLeft}
		 * , before this method is called. This method is recursive, and stops traversing the
		 * invocation sequence tree as soon the wanted element is found.
		 * 
		 * @param invocationData
		 *            Top parent invocation sequence.
		 * @param template
		 *            Template data.
		 * @param occurrencesLeft
		 *            Occurrence to search for.
		 * @param filters
		 *            Active filters of the tree viewer.
		 * @return Invocation sequence that has the Exception data set in Exceptions list and is
		 *         same as template data.
		 */
		public InvocationSequenceData getOccurrence(InvocationSequenceData invocationData, E template, MutableInt occurrencesLeft, ViewerFilter[] filters) {
			if (!getConcreteClass().isAssignableFrom(template.getClass())) {
				return null;
			}
			if (occurrenceFound(invocationData, template) && filtersPassed(invocationData, filters)) {
				occurrencesLeft.decrement();
				if (occurrencesLeft.intValue() == 0) {
					return invocationData;
				}
			}
			if (null != invocationData.getNestedSequences()) {
				for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
					InvocationSequenceData foundData = getOccurrence(child, template, occurrencesLeft, filters);
					if (null != foundData) {
						return foundData;
					}
				}
			}
			return null;
		}

		/**
		 * Returns if the template objects is found in the invocation data.
		 * 
		 * @param invocationData
		 *            Invocation data to look in.
		 * @param template
		 *            Template object.
		 * @return Return depends on the implementing classes.
		 */
		public abstract boolean occurrenceFound(InvocationSequenceData invocationData, E template);

		/**
		 * Compares if the template equals the element from the view of the finder.
		 * 
		 * @param template
		 *            Template.
		 * @param element
		 *            Element.
		 * @return True if templates are equal.
		 */
		public abstract boolean doesTemplateEqualsElement(E template, E element);

		/**
		 * Returns the concrete class that finder is working with.
		 * 
		 * @return Returns the concrete class that finder is working with.
		 */
		public abstract Class<? extends E> getConcreteClass();

		/**
		 * Returns if the invocation data object is passing all given filters.
		 * 
		 * @param invocationData
		 *            Invocation data.
		 * @param filters
		 *            Array of filters.
		 * @return True if all filters are passed, or filters array is null or empty.
		 */
		private boolean filtersPassed(InvocationSequenceData invocationData, ViewerFilter[] filters) {
			boolean passed = true;
			if (null != filters) {
				for (ViewerFilter filter : filters) {
					if (!filter.select(null, invocationData.getParentSequence(), invocationData)) {
						passed = false;
						break;
					}
				}
			}
			return passed;
		}

	}

	/**
	 * Occurrence finder for {@link ExceptionSensorData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class ExceptionOccurrenceFinder extends OccurrenceFinder<ExceptionSensorData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, ExceptionSensorData template) {
			if (invocationData.getExceptionSensorDataObjects() != null) {
				for (ExceptionSensorData exData : (List<ExceptionSensorData>) invocationData.getExceptionSensorDataObjects()) {
					return doesTemplateEqualsElement(template, exData);
				}
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<ExceptionSensorData> getConcreteClass() {
			return ExceptionSensorData.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean doesTemplateEqualsElement(ExceptionSensorData template, ExceptionSensorData element) {
			if (0 != template.getId()) {
				if (template.getId() != element.getId()) {
					return false;
				}
			}
			if (TEMPLATE_STRING != template.getCause()) {
				if (!ObjectUtils.equals(template.getCause(), element.getCause())) {
					return false;
				}
			}
			if (TEMPLATE_STRING != template.getErrorMessage()) {
				if (!ObjectUtils.equals(template.getErrorMessage(), element.getErrorMessage())) {
					return false;
				}
			}
			if (TEMPLATE_STRING != template.getStackTrace()) {
				// allow also parts of stack traces
				if (!StringUtils.contains(element.getStackTrace(), template.getStackTrace())) {
					return false;
				}
			}
			if (TEMPLATE_STRING != template.getThrowableType()) {
				if (!ObjectUtils.equals(template.getThrowableType(), element.getThrowableType())) {
					return false;
				}
			}
			if (0 != template.getThrowableIdentityHashCode()) {
				if (template.getThrowableIdentityHashCode() != element.getThrowableIdentityHashCode()) {
					return false;
				}
			}
			if (null != template.getExceptionEvent()) {
				if (!ObjectUtils.equals(template.getExceptionEvent(), element.getExceptionEvent())) {
					return false;
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ExceptionSensorData getEmptyTemplate() {
			ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
			exceptionSensorData.setCause(TEMPLATE_STRING);
			exceptionSensorData.setErrorMessage(TEMPLATE_STRING);
			exceptionSensorData.setStackTrace(TEMPLATE_STRING);
			exceptionSensorData.setThrowableType(TEMPLATE_STRING);
			exceptionSensorData.setThrowableIdentityHashCode(0);
			exceptionSensorData.setExceptionEvent(null);
			return exceptionSensorData;
		}

	}

	/**
	 * Occurrence finder for {@link SqlStatementData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class SqlOccurrenceFinder extends OccurrenceFinder<SqlStatementData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, SqlStatementData template) {
			if (invocationData.getSqlStatementData() != null) {
				return doesTemplateEqualsElement(template, invocationData.getSqlStatementData());
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<SqlStatementData> getConcreteClass() {
			return SqlStatementData.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean doesTemplateEqualsElement(SqlStatementData template, SqlStatementData element) {
			if (0 != template.getId()) {
				if (template.getId() != element.getId()) {
					return false;
				}
			}
			if (TEMPLATE_STRING != template.getSql()) {
				if (!ObjectUtils.equals(template.getSql(), element.getSql())) {
					return false;
				}
			}
			if (TEMPLATE_LIST != template.getParameterValues()) {
				if (!ObjectUtils.equals(template.getParameterValues(), element.getParameterValues())) {
					return false;
				}
			}
			if (0 != template.getMethodIdent()) {
				if (template.getMethodIdent() != element.getMethodIdent()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public SqlStatementData getEmptyTemplate() {
			SqlStatementData sqlStatementData = new SqlStatementData();
			sqlStatementData.setSql(TEMPLATE_STRING);
			sqlStatementData.setParameterValues(TEMPLATE_LIST);
			sqlStatementData.setMethodIdent(0);
			return sqlStatementData;
		}

	}

	/**
	 * Occurrence finder for {@link TimerData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class TimerOccurrenceFinder extends OccurrenceFinder<MethodSensorData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, MethodSensorData template) {
			if (invocationData.getTimerData() != null) {
				return doesTemplateEqualsElement(template, invocationData.getTimerData());
			} else {
				return doesTemplateEqualsElement(template, invocationData);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<TimerData> getConcreteClass() {
			return TimerData.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean doesTemplateEqualsElement(MethodSensorData template, MethodSensorData element) {
			if (0 != template.getId()) {
				if (template.getId() != element.getId()) {
					return false;
				}
			}
			if (0 != template.getMethodIdent()) {
				if (template.getMethodIdent() != element.getMethodIdent()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MethodSensorData getEmptyTemplate() {
			TimerData timerData = new TimerData();
			timerData.setMethodIdent(0);
			return timerData;
		}

	}

	/**
	 * Occurrence finder for {@link InvocationSequenceData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class InvocationOccurenceFinder extends OccurrenceFinder<InvocationSequenceData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public InvocationSequenceData getEmptyTemplate() {
			InvocationSequenceData invocation = new InvocationSequenceData();
			invocation.setMethodIdent(0);
			return invocation;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, InvocationSequenceData template) {
			return doesTemplateEqualsElement(template, invocationData);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean doesTemplateEqualsElement(InvocationSequenceData template, InvocationSequenceData element) {
			if (0 != template.getId()) {
				if (template.getId() != element.getId()) {
					return false;
				}
			}
			if (0 != template.getMethodIdent()) {
				if (template.getMethodIdent() != element.getMethodIdent()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends InvocationSequenceData> getConcreteClass() {
			return InvocationSequenceData.class;
		}

	}
}
