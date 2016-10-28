package rocks.inspectit.server.instrumentation.listener;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
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

import com.google.common.collect.Sets;

import rocks.inspectit.server.ci.event.AgentMappingsUpdateEvent;
import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.job.ProfileUpdateJob;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Tests the {@link ProfileEventListener} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings({ "PMD" })
public class ProfileEventListenerTest extends TestBase {

	@InjectMocks
	ProfileEventListener eventListener;

	@Mock
	Logger log;

	@Mock
	protected NextGenInstrumentationManager nextGenInstrumentationManager;

	@Mock
	protected ExecutorService executor;

	@Mock
	protected BeanFactory beanFactory;

	/**
	 * Tests the {@link ProfileEventListener#onApplicationEvent(AgentMappingsUpdateEvent)} method.
	 */
	public static class OnApplicationEvent extends ProfileEventListenerTest {

		@Mock
		ProfileUpdateEvent event;

		@Mock
		AgentCacheEntry cacheEntry;

		@Mock
		ConfigurationHolder configurationHolder;

		@Mock
		Environment environment;

		@Mock
		ProfileUpdateJob updateJob;

		@Mock
		Future<?> future;

		Map<Long, AgentCacheEntry> cacheMap;

		@BeforeMethod
		public void beforeMethod() {
			cacheMap = new HashMap<>();
			when(nextGenInstrumentationManager.getAgentCacheMap()).thenReturn(cacheMap);
			when(cacheEntry.getConfigurationHolder()).thenReturn(configurationHolder);
			when(configurationHolder.getEnvironment()).thenReturn(environment);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void successful() throws InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("id_1"));
			when(beanFactory.getBean(ProfileUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(event).getProfileId();
			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verify(cacheEntry).getConfigurationHolder();
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).isInitialized();
			verify(environment, times(2)).getProfileIds();
			verify(beanFactory).getBean(ProfileUpdateJob.class);
			verify(executor).submit(updateJob);
			verify(updateJob).setAgentCacheEntry(cacheEntry);
			verify(updateJob).setProfileUpdateEvent(event);
			verify(future).get();
			verifyNoMoreInteractions(event, cacheEntry, configurationHolder, environment, updateJob, future, nextGenInstrumentationManager, beanFactory, executor);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void profileWasDeactivated() throws InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileDeactivated()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("id_1"));
			when(beanFactory.getBean(ProfileUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(event).isProfileDeactivated();
			verify(event).getProfileId();
			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verify(cacheEntry).getConfigurationHolder();
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).isInitialized();
			verify(environment, times(2)).getProfileIds();
			verify(beanFactory).getBean(ProfileUpdateJob.class);
			verify(executor).submit(updateJob);
			verify(updateJob).setAgentCacheEntry(cacheEntry);
			verify(updateJob).setProfileUpdateEvent(event);
			verify(future).get();
			verifyNoMoreInteractions(event, cacheEntry, configurationHolder, environment, updateJob, future, nextGenInstrumentationManager, beanFactory, executor);
		}

		@Test
		public void profileNotActive() {
			cacheMap.put(1L, cacheEntry);

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(event).isProfileDeactivated();
			verifyNoMoreInteractions(event);
			verifyZeroInteractions(cacheEntry, configurationHolder, environment, updateJob, future, nextGenInstrumentationManager, beanFactory, executor);
		}

		@Test
		public void emptyCacheMap() {
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verifyNoMoreInteractions(event, nextGenInstrumentationManager);
			verifyZeroInteractions(cacheEntry, configurationHolder, environment, updateJob, future, beanFactory, executor);
		}

		@Test
		public void configurationNotInitialized() {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");
			when(configurationHolder.isInitialized()).thenReturn(false);

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verify(cacheEntry).getConfigurationHolder();
			verify(configurationHolder).isInitialized();
			verifyNoMoreInteractions(event, cacheEntry, configurationHolder, nextGenInstrumentationManager);
			verifyZeroInteractions(environment, updateJob, future, beanFactory, executor);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void emptyEnvironment() {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Collections.EMPTY_SET);

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verify(cacheEntry).getConfigurationHolder();
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).isInitialized();
			verify(environment).getProfileIds();
			verifyNoMoreInteractions(event, cacheEntry, configurationHolder, environment, nextGenInstrumentationManager);
			verifyZeroInteractions(updateJob, future, beanFactory, executor);
		}

		@Test
		public void unknownProfile() {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_unknown");
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("id_1"));

			eventListener.onApplicationEvent(event);

			verify(event).isProfileActive();
			verify(event).getProfileId();
			verify(nextGenInstrumentationManager).getAgentCacheMap();
			verify(cacheEntry).getConfigurationHolder();
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).isInitialized();
			verify(environment, times(2)).getProfileIds();
			verifyNoMoreInteractions(event, cacheEntry, configurationHolder, environment, nextGenInstrumentationManager);
			verifyZeroInteractions(updateJob, future, beanFactory, executor);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void futureThrowsExecutionException() throws InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("id_1"));
			when(beanFactory.getBean(ProfileUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);
			when(future.get()).thenThrow(ExecutionException.class);

			eventListener.onApplicationEvent(event);

			verify(future).get();
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void futureThrowsInterruptedException() throws InterruptedException, ExecutionException {
			cacheMap.put(1L, cacheEntry);
			when(event.isProfileActive()).thenReturn(true);
			when(event.getProfileId()).thenReturn("id_1");
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("id_1"));
			when(beanFactory.getBean(ProfileUpdateJob.class)).thenReturn(updateJob);
			when(executor.submit(updateJob)).thenReturn((Future) future);
			when(future.get()).thenThrow(InterruptedException.class);

			eventListener.onApplicationEvent(event);

			verify(future).get();
		}
	}

}
