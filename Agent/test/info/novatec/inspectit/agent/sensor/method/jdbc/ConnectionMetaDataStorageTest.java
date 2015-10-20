package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.ConnectionMetaData;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.ConnectionMetaDataExtractor;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConnectionMetaDataStorageTest {

	private ConnectionMetaDataStorage storage;

	@BeforeMethod
	public void init() {
		storage = new ConnectionMetaDataStorage();

	}

	@Test
	public void addNullConnectionInstance() {
		storage.add(null);
		assertThat((int) storage.storage.size(), is(0));
	}

	@Test
	public void addExtractorReturnsNull() {
		Object connectionObject = "";

		ConnectionMetaDataExtractor extractor = Mockito.mock(ConnectionMetaDataExtractor.class);
		when(extractor.parse(connectionObject)).thenReturn(null);
		storage.dataExtractor = extractor;

		storage.add(connectionObject);
		assertThat(storage.storage.size(), is(0L));
	}

	@Test
	public void getNotExisting() {
		ConnectionMetaData data = storage.get("String");
		assertThat(data, is(nullValue()));
	}

	@Test
	public void getNull() {
		ConnectionMetaData data = storage.get(null);
		assertThat(data, is(nullValue()));
	}

	@Test
	public void addAndGetConnection() {
		ConnectionMetaDataExtractor extractor = mock(ConnectionMetaDataExtractor.class);
		ConnectionMetaData data = mock(ConnectionMetaData.class);
		when(extractor.parse(anyObject())).thenReturn(data);
		storage.dataExtractor = extractor;
		Object connectionObject = ""; // note that we can pass this as we mocked the data
										// extraction.

		storage.add(connectionObject);
		assertThat(storage.storage.size(), is(1L));

		storage.add(connectionObject);
		assertThat(storage.storage.size(), is(1L));

		assertThat(storage.get(connectionObject), is(data));
	}
}
