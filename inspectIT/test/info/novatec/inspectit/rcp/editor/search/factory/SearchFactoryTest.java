package info.novatec.inspectit.rcp.editor.search.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchCriteria;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link SearchFactory} class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class SearchFactoryTest {

	/**
	 * {@link RepositoryDefinition}.
	 */
	@Mock
	private RepositoryDefinition repositoryDefinition;

	/**
	 * Init method.
	 */
	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);

		MethodIdent methodIdent = mock(MethodIdent.class);
		when(methodIdent.getId()).thenReturn(1L);
		when(methodIdent.getFQN()).thenReturn("");
		when(methodIdent.getMethodName()).thenReturn("");
		when(methodIdent.getParameters()).thenReturn(Collections.<String> emptyList());

		CachedDataService cachedDataService = mock(CachedDataService.class);
		when(repositoryDefinition.getCachedDataService()).thenReturn(cachedDataService);
		when(cachedDataService.getMethodIdentForId(1L)).thenReturn(methodIdent);
	}

	/**
	 * Tests if passed element is <code>null</code>, return is always false.
	 */
	@Test
	public void nullElementSearch() {
		SearchCriteria searchCriteria = new SearchCriteria("");
		assertThat(SearchFactory.isSearchCompatible(null, searchCriteria, repositoryDefinition), is(equalTo(false)));
	}

	/**
	 * Tests that method ident is properly searched.
	 */
	@Test
	public void methodIdentSearch() {
		TimerData timerData = new TimerData();
		timerData.setMethodIdent(1L);

		MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(1L);

		SearchCriteria searchCriteria = new SearchCriteria("Blah");
		SearchCriteria wrong = new SearchCriteria("halB");

		when(methodIdent.getFQN()).thenReturn("blah.blah.blah");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(timerData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(timerData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(timerData, wrong, repositoryDefinition), is(equalTo(false)));

		when(methodIdent.getFQN()).thenReturn("");
		when(methodIdent.getMethodName()).thenReturn("balhblah");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(timerData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(timerData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(timerData, wrong, repositoryDefinition), is(equalTo(false)));

		when(methodIdent.getMethodName()).thenReturn("");
		List<String> params = new ArrayList<String>();
		params.add("blaha");
		when(methodIdent.getParameters()).thenReturn(params);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(timerData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(timerData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(timerData, wrong, repositoryDefinition), is(equalTo(false)));
	}

	/**
	 * Tests that the {@link SqlStatementData} is searched correctly.
	 */
	@Test
	public void sqlStatementDataSearch() {
		SqlStatementData sqlData = new SqlStatementData();
		sqlData.setMethodIdent(1L);
		sqlData.setSql("Select blah from table where condition");

		SearchCriteria searchCriteria = new SearchCriteria("Blah");
		SearchCriteria wrong = new SearchCriteria("halB");

		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(sqlData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(sqlData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(sqlData, wrong, repositoryDefinition), is(equalTo(false)));

		List<String> parameters = new ArrayList<String>();
		parameters.add("blah");
		sqlData.setSql("Select somthing from table where condition=?");
		sqlData.setParameterValues(parameters);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(sqlData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(sqlData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(sqlData, wrong, repositoryDefinition), is(equalTo(false)));

	}

	/**
	 * Tests that the {@link HttpTimerData} is searched correctly.
	 */
	@Test
	public void httpTimerDataSearch() {
		HttpTimerData httpData = new HttpTimerData();
		httpData.setMethodIdent(1L);

		SearchCriteria searchCriteria = new SearchCriteria("Blah");
		SearchCriteria wrong = new SearchCriteria("halB");

		httpData.setInspectItTaggingHeaderValue("blaha");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		httpData.setInspectItTaggingHeaderValue("");
		httpData.setUri("ablah");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		httpData.setUri("");
		httpData.setRequestMethod("ablaha");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		Map<String, String> map = new HashMap<String, String>();
		map.put("ablaha", "value");
		httpData.setRequestMethod("");
		httpData.setAttributes(map);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		httpData.setAttributes(Collections.<String, String> emptyMap());
		httpData.setSessionAttributes(map);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		httpData.setSessionAttributes(Collections.<String, String> emptyMap());
		map.clear();
		map.put("key", "ablaha");
		httpData.setHeaders(map);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		httpData.setHeaders(Collections.<String, String> emptyMap());
		Map<String, String[]> map1 = new HashMap<String, String[]>();
		map1.put("key", new String[] { "blah", "anotherValue" });
		httpData.setParameters(map1);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));
	}

	/**
	 * Tests that the {@link ExceptionSensorData} is searched correctly.
	 */
	@Test
	public void exceptionDataSearch() {
		ExceptionSensorData exceptionData = new ExceptionSensorData();
		exceptionData.setMethodIdent(1L);

		SearchCriteria searchCriteria = new SearchCriteria("Blah");
		SearchCriteria wrong = new SearchCriteria("halB");

		exceptionData.setCause("blah");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(exceptionData, wrong, repositoryDefinition), is(equalTo(false)));

		exceptionData.setCause("");
		exceptionData.setThrowableType("blah.bla");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(exceptionData, wrong, repositoryDefinition), is(equalTo(false)));

		exceptionData.setThrowableType("");
		exceptionData.setErrorMessage("My very blah error message");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(exceptionData, wrong, repositoryDefinition), is(equalTo(false)));

		exceptionData.setErrorMessage("");
		exceptionData.setStackTrace("10: java blah");
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(exceptionData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(exceptionData, wrong, repositoryDefinition), is(equalTo(false)));
	}

	/**
	 * Tests that the {@link InvocationSequenceData} is searched correctly.
	 */
	@Test
	public void invocationDataSearch() {
		InvocationSequenceData invocationData = new InvocationSequenceData();
		invocationData.setMethodIdent(1L);

		SearchCriteria searchCriteria = new SearchCriteria("Blah");
		SearchCriteria wrong = new SearchCriteria("halB");

		HttpTimerData httpData = new HttpTimerData();
		httpData.setMethodIdent(1L);
		httpData.setInspectItTaggingHeaderValue("blaha");
		invocationData.setTimerData(httpData);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(httpData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(httpData, wrong, repositoryDefinition), is(equalTo(false)));

		invocationData.setTimerData(null);
		SqlStatementData sqlData = new SqlStatementData();
		sqlData.setMethodIdent(1L);
		sqlData.setSql("Select blah from table where condition");
		invocationData.setSqlStatementData(sqlData);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(invocationData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(invocationData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(invocationData, wrong, repositoryDefinition), is(equalTo(false)));

		invocationData.setSqlStatementData(null);
		ExceptionSensorData exceptionData = new ExceptionSensorData();
		exceptionData.setMethodIdent(1L);
		exceptionData.setCause("blah");
		List<ExceptionSensorData> exceptionList = new ArrayList<ExceptionSensorData>();
		exceptionList.add(exceptionData);
		invocationData.setExceptionSensorDataObjects(exceptionList);
		searchCriteria.setCaseSensitive(false);
		assertThat(SearchFactory.isSearchCompatible(invocationData, searchCriteria, repositoryDefinition), is(equalTo(true)));
		searchCriteria.setCaseSensitive(true);
		assertThat(SearchFactory.isSearchCompatible(invocationData, searchCriteria, repositoryDefinition), is(equalTo(false)));
		assertThat(SearchFactory.isSearchCompatible(invocationData, wrong, repositoryDefinition), is(equalTo(false)));
	}

}
