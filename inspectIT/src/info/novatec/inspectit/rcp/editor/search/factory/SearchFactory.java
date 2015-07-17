package info.novatec.inspectit.rcp.editor.search.factory;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.AggregatedSqlStatementData;
import info.novatec.inspectit.communication.data.AggregatedTimerData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchCriteria;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

/**
 * Class for supporting the search functionality.
 * 
 * @author Ivan Senic
 * 
 */
public final class SearchFactory {

	/**
	 * Search finder for {@link TimerData}.
	 */
	private static TimerSearchFinder timerSearchFinder = new TimerSearchFinder();

	/**
	 * Search finder for {@link SqlStatementData}.
	 */
	private static SqlSearchFinder sqlSearchFinder = new SqlSearchFinder();

	/**
	 * Search finder for {@link HttpTimerData}.
	 */
	private static HttpSearchFinder httpSearchFinder = new HttpSearchFinder();

	/**
	 * Search finder for {@link ExceptionSearchFinder}.
	 */
	private static ExceptionSearchFinder exceptionSearchFinder = new ExceptionSearchFinder();

	/**
	 * Search finder for {@link InvocationSearchFinder}.
	 */
	private static InvocationSearchFinder invocationSearchFinder = new InvocationSearchFinder();

	/**
	 * Private constructor.
	 */
	private SearchFactory() {
	}

	/**
	 * Returns if the element is search compatible.
	 * 
	 * @param element
	 *            Element to check.
	 * @param searchCriteria
	 *            Criteria.
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition} where the element can be found.
	 * @return True if element is compatible with search. False otherwise. False also if the element
	 *         to check is null.
	 * 
	 */
	public static boolean isSearchCompatible(Object element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition) {
		if (null == element) {
			return false;
		}

		if (TimerData.class.equals(element.getClass()) || AggregatedTimerData.class.equals(element.getClass())) {
			return timerSearchFinder.isSearchCompatible((TimerData) element, searchCriteria, repositoryDefinition);
		} else if (SqlStatementData.class.equals(element.getClass()) || AggregatedSqlStatementData.class.equals(element.getClass())) {
			return sqlSearchFinder.isSearchCompatible((SqlStatementData) element, searchCriteria, repositoryDefinition);
		} else if (HttpTimerData.class.equals(element.getClass()) || AggregatedHttpTimerData.class.equals(element.getClass())) {
			return httpSearchFinder.isSearchCompatible((HttpTimerData) element, searchCriteria, repositoryDefinition);
		} else if (ExceptionSensorData.class.equals(element.getClass()) || AggregatedExceptionSensorData.class.equals(element.getClass())) {
			return exceptionSearchFinder.isSearchCompatible((ExceptionSensorData) element, searchCriteria, repositoryDefinition);
		} else if (InvocationSequenceData.class.equals(element.getClass())) {
			return invocationSearchFinder.isSearchCompatible((InvocationSequenceData) element, searchCriteria, repositoryDefinition);
		}
		return false;
	}

	/**
	 * Abstract class for all Search finders.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <E>
	 *            Type of element finder can search
	 */
	private abstract static class AbstractSearchFinder<E> {

		/**
		 * Returns if the element is search compatible.
		 * 
		 * @param element
		 *            Element to check.
		 * @param searchCriteria
		 *            Criteria.
		 * @param repositoryDefinition
		 *            {@link RepositoryDefinition} where the element can be found.
		 * @return True if element is compatible with search.
		 * 
		 */
		public abstract boolean isSearchCompatible(E element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition);

		/**
		 * Null safe checks if the string is matching the {@link SearchCriteria}.
		 * 
		 * @param str
		 *            String to check.
		 * @param searchCriteria
		 *            {@link SearchCriteria}.
		 * @return True only if the provided string to check is not null and it matches the
		 *         {@link SearchCriteria}.
		 */
		protected boolean stringMatches(String str, SearchCriteria searchCriteria) {
			if (str != null) {
				if (!searchCriteria.isCaseSensitive()) {
					return str.toUpperCase().indexOf(searchCriteria.getSearchStringUpperCase()) != -1;
				} else {
					return str.indexOf(searchCriteria.getSearchString()) != -1;
				}
			}
			return false;
		}

		/**
		 * Returns if the {@link MethodIdent} object is search compatible. Sub-classes can use this
		 * method.
		 * 
		 * @param methodIdent
		 *            {@link MethodIdent} to check.
		 * @param searchCriteria
		 *            {@link SearchCriteria}.
		 * @return True if {@link MethodIdent} matches the {@link SearchCriteria}.
		 */
		protected boolean isSearchCompatible(MethodIdent methodIdent, SearchCriteria searchCriteria) {
			if (null == methodIdent) {
				return false;
			} else if (stringMatches(methodIdent.getFQN(), searchCriteria)) {
				return true;
			} else if (stringMatches(methodIdent.getMethodName(), searchCriteria)) {
				return true;
			} else if (methodIdent.getParameters() != null && !methodIdent.getParameters().isEmpty()) {
				for (String parameter : methodIdent.getParameters()) {
					if (stringMatches(parameter, searchCriteria)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Search finder for {@link TimerData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class TimerSearchFinder extends AbstractSearchFinder<TimerData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSearchCompatible(TimerData element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition) {
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(element.getMethodIdent());
			return super.isSearchCompatible(methodIdent, searchCriteria);
		}
	}

	/**
	 * Search finder for {@link SqlStatementData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class SqlSearchFinder extends AbstractSearchFinder<SqlStatementData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSearchCompatible(SqlStatementData element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition) {
			if (stringMatches(element.getSql(), searchCriteria)) {
				return true;
			} else if (CollectionUtils.isNotEmpty(element.getParameterValues())) {
				for (String param : element.getParameterValues()) {
					if (stringMatches(param, searchCriteria)) {
						return true;
					}
				}
			} else if (stringMatches(element.getDatabaseProductName(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getDatabaseProductVersion(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getDatabaseUrl(), searchCriteria)) {
				return true;
			}
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(element.getMethodIdent());
			return super.isSearchCompatible(methodIdent, searchCriteria);
		}
	}

	/**
	 * Search finder for {@link HttpTimerData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class HttpSearchFinder extends AbstractSearchFinder<HttpTimerData> {

		@Override
		public boolean isSearchCompatible(HttpTimerData element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition) {
			if (stringMatches(element.getInspectItTaggingHeaderValue(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getRequestMethod(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getUri(), searchCriteria)) {
				return true;
			} else {
				if (MapUtils.isNotEmpty(element.getAttributes())) {

					for (Map.Entry<String, String> entry : element.getAttributes().entrySet()) {
						if (stringMatches(entry.getKey(), searchCriteria) || stringMatches(entry.getValue(), searchCriteria)) {
							return true;
						}
					}
				}
				if (MapUtils.isNotEmpty(element.getHeaders())) {
					for (Map.Entry<String, String> entry : element.getHeaders().entrySet()) {
						if (stringMatches(entry.getKey(), searchCriteria) || stringMatches(entry.getValue(), searchCriteria)) {
							return true;
						}
					}
				}
				if (MapUtils.isNotEmpty(element.getParameters())) {
					for (Map.Entry<String, String[]> entry : element.getParameters().entrySet()) {
						if (stringMatches(entry.getKey(), searchCriteria)) {
							return true;
						} else {
							for (String string : entry.getValue()) {
								if (stringMatches(string, searchCriteria)) {
									return true;
								}
							}
						}
					}
				}
				if (MapUtils.isNotEmpty(element.getSessionAttributes())) {
					for (Map.Entry<String, String> entry : element.getSessionAttributes().entrySet()) {
						if (stringMatches(entry.getKey(), searchCriteria) || stringMatches(entry.getValue().toString(), searchCriteria)) {
							return true;
						}
					}
				}
			}
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(element.getMethodIdent());
			return super.isSearchCompatible(methodIdent, searchCriteria);
		}

	}

	/**
	 * Search finder for {@link ExceptionSensorData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class ExceptionSearchFinder extends AbstractSearchFinder<ExceptionSensorData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSearchCompatible(ExceptionSensorData element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition) {
			if (stringMatches(element.getCause(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getErrorMessage(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getStackTrace(), searchCriteria)) {
				return true;
			} else if (stringMatches(element.getThrowableType(), searchCriteria)) {
				return true;
			}
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(element.getMethodIdent());
			return super.isSearchCompatible(methodIdent, searchCriteria);
		}

	}

	/**
	 * Search finder for {@link InvocationSequenceData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class InvocationSearchFinder extends AbstractSearchFinder<InvocationSequenceData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSearchCompatible(InvocationSequenceData element, SearchCriteria searchCriteria, RepositoryDefinition repositoryDefinition) {
			if (null != element.getTimerData()) {
				if (SearchFactory.isSearchCompatible(element.getTimerData(), searchCriteria, repositoryDefinition)) {
					return true;
				}
			}
			if (null != element.getSqlStatementData()) {
				if (SearchFactory.isSearchCompatible(element.getSqlStatementData(), searchCriteria, repositoryDefinition)) {
					return true;
				}
			}
			if (null != element.getExceptionSensorDataObjects() && !element.getExceptionSensorDataObjects().isEmpty()) {
				for (ExceptionSensorData exData : element.getExceptionSensorDataObjects()) {
					if (SearchFactory.isSearchCompatible(exData, searchCriteria, repositoryDefinition)) {
						return true;
					}
				}
			}
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(element.getMethodIdent());
			return super.isSearchCompatible(methodIdent, searchCriteria);
		}

	}

}
