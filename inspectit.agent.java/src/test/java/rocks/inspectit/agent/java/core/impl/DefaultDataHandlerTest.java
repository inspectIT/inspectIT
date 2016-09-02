package rocks.inspectit.agent.java.core.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings({ "unchecked", "PMD" })
public class DefaultDataHandlerTest extends TestBase {

	@InjectMocks
	DefaultDataHandler handler;

	@Mock
	IConnection connection;

	@Mock
	Logger log;

	public static class OnEvent extends DefaultDataHandlerTest {

		@Mock
		DefaultDataWrapper wrapper;

		@Mock
		DefaultData defaultData;

		List<DefaultData> sent;

		@BeforeMethod
		public void collectSent() throws ServerUnavailableException {
			sent = new ArrayList<DefaultData>();
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					sent.addAll((Collection<? extends DefaultData>) invocation.getArguments()[0]);
					return null;
				}
			}).when(connection).sendDataObjects(Mockito.<List<DefaultData>> any());
		}

		@Test
		public void happyPath() throws ServerUnavailableException {
			when(wrapper.getDefaultData()).thenReturn(defaultData);
			when(connection.isConnected()).thenReturn(true);

			handler.onEvent(wrapper, 0L, true);

			verify(connection).isConnected();
			verify(connection).sendDataObjects(Mockito.<List<DefaultData>> any());
			verifyNoMoreInteractions(connection);
			assertThat(sent, hasSize(1));
			assertThat(sent, hasItem(defaultData));
		}

		@Test
		public void notEndOfBatch() throws ServerUnavailableException {
			when(wrapper.getDefaultData()).thenReturn(defaultData);
			when(connection.isConnected()).thenReturn(true);

			handler.onEvent(wrapper, 0L, false);

			verifyZeroInteractions(connection);
		}

		@Test
		public void notConnected() throws ServerUnavailableException {
			when(wrapper.getDefaultData()).thenReturn(defaultData);
			when(connection.isConnected()).thenReturn(false);

			handler.onEvent(wrapper, 0L, true);

			verify(connection).isConnected();
			verifyNoMoreInteractions(connection);
		}

		@Test
		public void correctOrder() throws ServerUnavailableException {
			DefaultData defaultData2 = mock(DefaultData.class);
			when(wrapper.getDefaultData()).thenReturn(defaultData).thenReturn(defaultData2);
			when(connection.isConnected()).thenReturn(true);

			handler.onEvent(wrapper, 0L, false);
			handler.onEvent(wrapper, 0L, true);

			verify(connection).isConnected();
			verify(connection).sendDataObjects(Mockito.<List<DefaultData>> any());
			verifyNoMoreInteractions(connection);
			assertThat(sent, hasSize(2));
			assertThat(sent, hasItems(defaultData, defaultData2));
		}

		@Test
		public void noRepeat() throws ServerUnavailableException {
			DefaultData defaultData2 = mock(DefaultData.class);
			when(wrapper.getDefaultData()).thenReturn(defaultData).thenReturn(defaultData2);
			when(connection.isConnected()).thenReturn(true);

			handler.onEvent(wrapper, 0L, true);
			handler.onEvent(wrapper, 0L, true);

			verify(connection, times(2)).isConnected();
			verify(connection, times(2)).sendDataObjects(Mockito.<List<DefaultData>> any());
			verifyNoMoreInteractions(connection);
			assertThat(sent, hasSize(2));
			assertThat(sent, hasItems(defaultData, defaultData2));
		}

	}

}
