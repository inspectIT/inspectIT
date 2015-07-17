package info.novatec.inspectit.agent.sending.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.MockInit;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.ListListener;
import info.novatec.inspectit.agent.sending.ISendingStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ListSizeStrategyTest extends MockInit {

	@Mock
	private ICoreService coreService;

	private ISendingStrategy sendingStrategy;

	@BeforeMethod
	public void initTestClass() {
		sendingStrategy = new ListSizeStrategy();
	}

	@Test
	public void startStop() {
		sendingStrategy.start(coreService);
		verify(coreService).addListListener((ListListener<?>) sendingStrategy);

		sendingStrategy.stop();
		verify(coreService).removeListListener((ListListener<?>) sendingStrategy);

		verifyNoMoreInteractions(coreService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void contentChanged() {
		sendingStrategy.start(coreService);
		List<Object> list = mock(List.class);

		((ListListener<Object>) sendingStrategy).contentChanged(list);

		verify(list).size();

		verifyNoMoreInteractions(list);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fireSending() {
		sendingStrategy.start(coreService);
		List<Object> list = mock(List.class);
		when(list.size()).thenReturn(11);

		((ListListener<Object>) sendingStrategy).contentChanged(list);

		verify(coreService).sendData();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fireSendingModifiedListSize() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("size", "3");
		sendingStrategy.init(settings);
		sendingStrategy.start(coreService);
		List<Object> list = mock(List.class);
		when(list.size()).thenReturn(5);

		((ListListener<Object>) sendingStrategy).contentChanged(list);

		verify(coreService).sendData();
	}

}
