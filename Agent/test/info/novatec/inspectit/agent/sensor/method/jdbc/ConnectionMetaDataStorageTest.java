package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
		ConnectionMetaDataExtractor extractor = Mockito.mock(ConnectionMetaDataExtractor.class);
		ConnectionMetaData data = Mockito.mock(ConnectionMetaData.class);
		Mockito.when(extractor.parse(Mockito.anyObject())).thenReturn(data);
		storage.dataExtractor = extractor;
		Object connectionObject = ""; // note that we can pass this as we mocked the data
										// extraction.

		storage.add(connectionObject);
		assertThat((int) storage.storage.size(), is(1));

		storage.add(connectionObject);
		assertThat((int) storage.storage.size(), is(1));

		assertThat(storage.get(connectionObject), is(data));
	}

}
