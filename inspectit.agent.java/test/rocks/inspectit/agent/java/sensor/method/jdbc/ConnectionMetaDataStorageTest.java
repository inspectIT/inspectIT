package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.ConnectionMetaData;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.ConnectionMetaDataExtractor;
import info.novatec.inspectit.communication.data.SqlStatementData;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConnectionMetaDataStorageTest {

	private ConnectionMetaDataStorage storage;

	@Mock
	private SqlStatementData sqlStatementData;

	@Mock
	private ConnectionMetaDataExtractor extractor;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		storage = new ConnectionMetaDataStorage();
		storage.dataExtractor = extractor;
	}

	@Test
	public void nullConnectionInstance() {
		storage.populate(sqlStatementData, null);

		assertThat((int) storage.storage.size(), is(0));
		verifyZeroInteractions(sqlStatementData);
	}

	@Test
	public void extractorReturnsNull() {
		Object connectionObject = "";
		when(extractor.parse(connectionObject)).thenReturn(null);

		storage.populate(sqlStatementData, connectionObject);

		// we have one now as adding empty
		assertThat(storage.storage.size(), is(1L));

		// re-trying should not touch the storage
		storage.populate(sqlStatementData, connectionObject);
		assertThat(storage.storage.size(), is(1L));

		// in any case no sql touching
		verifyZeroInteractions(sqlStatementData);
	}

	@Test
	public void sqlPopulated() {
		ConnectionMetaData data = new ConnectionMetaData();
		data.product = "product";
		data.version = "version";
		data.url = "url";
		when(extractor.parse(anyObject())).thenReturn(data);
		// note that we can pass this as we mocked the data extraction.
		Object connectionObject = "";

		storage.populate(sqlStatementData, connectionObject);
		assertThat(storage.storage.size(), is(1L));
		assertThat(storage.storage.getIfPresent(connectionObject), is(data));

		verify(sqlStatementData, times(1)).setDatabaseProductName(data.product);
		verify(sqlStatementData, times(1)).setDatabaseProductVersion(data.version);
		verify(sqlStatementData, times(1)).setDatabaseUrl(data.url);

		// second try, storage stays same
		storage.populate(sqlStatementData, connectionObject);
		assertThat(storage.storage.size(), is(1L));
		assertThat(storage.storage.getIfPresent(connectionObject), is(data));

		verify(sqlStatementData, times(2)).setDatabaseProductName(data.product);
		verify(sqlStatementData, times(2)).setDatabaseProductVersion(data.version);
		verify(sqlStatementData, times(2)).setDatabaseUrl(data.url);
	}
}
