package rocks.inspectit.server.instrumentation.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.AgentMappingsUpdateEvent;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.job.EnvironmentMappingUpdateJob;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Test the {@link AgentMappingsEventListener} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings({ "PMD" })
public class AgentMappingsEventListenerTest extends TestBase {

	@InjectMocks
	AgentMappingsEventListener eventListener;

	@Mock
	Logger log;

	@Mock
	protected NextGenInstrumentationManager nextGenInstrumentationManager;

	@Mock
	protected ExecutorService executor;

	@Mock
	protected BeanFactory beanFactory;

	@Mock
	protected PlatformIdentDao platformIdentDao;

	@Mock
	protected ConfigurationResolver configurationResolver;

	/**
	 * Tests the {@link AgentMappingsEventListener#onApplicationEvent(AgentMappingsUpdateEvent)}
	 * method.
	 */
	public static class OnApplicationEvent extends AgentMappingsEventListenerTest {

		@Mock
		AgentMappingsUpdateEvent event;

		@Mock
		AgentCacheEntry cacheEntry;

		@Mock
		ConfigurationHolder configurationHolder;

		@Mock
		Environment cachedEnvironment;

		@Mock
		Environment newEnvironment;

		@Mock
		EnvironmentMappingUpdateJob updateJob;

		@Mock
		Future<?> future;

		@Mock
		PlatformIdent platformIdent;

		Map<Long, AgentCacheEntry> cacheMap;

		@BeforeMethod
		public void beforeMethod() {
			cacheMap = new HashMap<>();
			when(nextGenInstrumentationManager.getAgentCacheMap()).thenReturn(cacheMap);
			when(cacheEntry.getConfigurationHolder()).thenReturn(configurationHolder);
			when(configurationHolder.getEnvironment()).thenReturn(cachedEnvironment);
			when(platformIdentDao.load(10L)).thenReturn(platformIdent);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void successful() throws BusinessException, InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(cacheEntry.getId()).thenReturn(10L);
			when(configurationResolver.getEnvironmentForAgent(any(List.class), any(String.class))).thenReturn(newEnvironment);
			when(cachedEnvironment.getId()).thenReturn("id");
			when(newEnvironment.getId()).thenReturn("newId");
			when(beanFactory.getBean(EnvironmentMappingUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);

			eventListener.onApplicationEvent(event);

			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verify(cacheEntry).getConfigurationHolder();
			verify(cacheEntry).getId();
			verify(configurationHolder).getEnvironment();
			verify(platformIdentDao).load(10L);
			verify(platformIdent).getAgentName();
			verify(platformIdent).getDefinedIPs();
			verify(configurationResolver).getEnvironmentForAgent(any(List.class), any(String.class));
			verify(cachedEnvironment).getId();
			verify(newEnvironment).getId();
			verify(beanFactory).getBean(EnvironmentMappingUpdateJob.class);
			verify(updateJob).setAgentCacheEntry(cacheEntry);
			verify(updateJob).setEnvironment(newEnvironment);
			verify(executor).submit(updateJob);
			verify(future).get();
			verifyNoMoreInteractions(nextGenInstrumentationManager, cacheEntry, configurationHolder, platformIdentDao, platformIdent, configurationResolver, cachedEnvironment, newEnvironment, event,
					beanFactory, updateJob, executor, future);
		}

		@Test
		public void emptyCacheMap() {
			eventListener.onApplicationEvent(event);

			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verifyNoMoreInteractions(nextGenInstrumentationManager);
			verifyZeroInteractions(cacheEntry, configurationHolder, platformIdentDao, platformIdent, configurationResolver, cachedEnvironment, newEnvironment, event, beanFactory, updateJob, executor,
					future);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void noEnvironmentAssigned() throws InterruptedException, ExecutionException, BusinessException {
			cacheMap.put(1L, cacheEntry);
			when(cacheEntry.getId()).thenReturn(10L);
			when(configurationHolder.getEnvironment()).thenReturn(null);
			when(configurationResolver.getEnvironmentForAgent(any(List.class), any(String.class))).thenReturn(newEnvironment);
			when(cachedEnvironment.getId()).thenReturn("id");
			when(newEnvironment.getId()).thenReturn("newId");
			when(beanFactory.getBean(EnvironmentMappingUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);

			eventListener.onApplicationEvent(event);

			verify(configurationHolder).getEnvironment();
			verify(future).get();
			verifyNoMoreInteractions(configurationHolder, future);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void EnvironmentsHaveSameID() throws BusinessException, InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(cacheEntry.getId()).thenReturn(10L);
			when(configurationResolver.getEnvironmentForAgent(any(List.class), any(String.class))).thenReturn(newEnvironment);
			when(cachedEnvironment.getId()).thenReturn("id");
			when(newEnvironment.getId()).thenReturn("id");
			when(beanFactory.getBean(EnvironmentMappingUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);

			eventListener.onApplicationEvent(event);

			verify(cachedEnvironment).getId();
			verify(newEnvironment).getId();
			verifyNoMoreInteractions(cachedEnvironment, newEnvironment);
			verifyZeroInteractions(beanFactory, executor, future);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void resolverThrowsBusinessException() throws BusinessException, InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(cacheEntry.getId()).thenReturn(10L);
			when(configurationResolver.getEnvironmentForAgent(any(List.class), any(String.class))).thenThrow(BusinessException.class);
			when(cachedEnvironment.getId()).thenReturn("id");
			when(newEnvironment.getId()).thenReturn("newId");
			when(beanFactory.getBean(EnvironmentMappingUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);

			eventListener.onApplicationEvent(event);

			verify(configurationResolver).getEnvironmentForAgent(any(List.class), any(String.class));
			verify(beanFactory).getBean(EnvironmentMappingUpdateJob.class);
			verify(updateJob).setAgentCacheEntry(cacheEntry);
			verify(executor).submit(updateJob);
			verify(future).get();
			verifyNoMoreInteractions(configurationResolver, beanFactory, updateJob, executor, future);
			verifyZeroInteractions(cachedEnvironment, newEnvironment);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void futureThrowsExecutionException() throws InterruptedException, ExecutionException, BusinessException {
			cacheMap.put(1L, cacheEntry);
			when(cacheEntry.getId()).thenReturn(10L);
			when(configurationResolver.getEnvironmentForAgent(any(List.class), any(String.class))).thenReturn(newEnvironment);
			when(cachedEnvironment.getId()).thenReturn("id");
			when(newEnvironment.getId()).thenReturn("newId");
			when(beanFactory.getBean(EnvironmentMappingUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);
			when(future.get()).thenThrow(ExecutionException.class);

			eventListener.onApplicationEvent(event);

			verify(future).get();
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void futureThrowsInterruptedException() throws InterruptedException, ExecutionException, BusinessException {
			cacheMap.put(1L, cacheEntry);
			when(cacheEntry.getId()).thenReturn(10L);
			when(configurationResolver.getEnvironmentForAgent(any(List.class), any(String.class))).thenReturn(newEnvironment);
			when(cachedEnvironment.getId()).thenReturn("id");
			when(newEnvironment.getId()).thenReturn("newId");
			when(beanFactory.getBean(EnvironmentMappingUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);
			when(future.get()).thenThrow(InterruptedException.class);

			eventListener.onApplicationEvent(event);

			verify(future).get();
		}
	}
}
