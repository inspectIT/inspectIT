package rocks.inspectit.agent.java.analyzer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.Input;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.io.FileResolver;
import rocks.inspectit.agent.java.spring.PrototypesProvider;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class ClassHashHelperTest extends TestBase {

	protected static final String TEST_CACHE_FILE = "test.cache";

	@InjectMocks
	ClassHashHelper helper;

	@Mock
	Logger log;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	ICoreService coreService;

	@Mock
	PrototypesProvider prototypesProvider;

	@Mock
	SerializationManager serializationManager;

	@Mock
	ScheduledExecutorService executorService;

	@Mock
	FileResolver fileResolver;

	@BeforeMethod
	public void setup() {
		doReturn(new File(TEST_CACHE_FILE)).when(fileResolver).getClassHashCacheFile();

		when(prototypesProvider.createSerializer()).thenReturn(serializationManager);
		when(coreService.getExecutorService()).thenReturn(executorService);
	}

	@AfterMethod
	public void delete() {
		File file = new File(TEST_CACHE_FILE);
		if (file.exists()) {
			assertThat(file.delete(), is(true));
		}
	}

	public class AfterPropertiesSet extends ClassHashHelperTest {

		@Test
		public void noCacheFileExists() throws Exception {
			when(configurationStorage.isClassCacheExistsOnCmr()).thenReturn(true);

			helper.afterPropertiesSet();

			verify(prototypesProvider, times(1)).createSerializer();
			verify(executorService, times(1)).scheduleAtFixedRate(Mockito.<Runnable> any(), anyLong(), anyLong(), Mockito.<TimeUnit> any());
			verifyZeroInteractions(serializationManager);
			assertThat(helper.isEmpty(), is(true));
		}

		@Test
		public void cacheFileExists() throws Exception {
			when(configurationStorage.isClassCacheExistsOnCmr()).thenReturn(true);
			new File(TEST_CACHE_FILE).createNewFile();
			Object hashes = Collections.singleton("hash");
			when(serializationManager.deserialize(Mockito.<Input> any())).thenReturn(hashes);

			helper.afterPropertiesSet();

			verify(prototypesProvider, times(1)).createSerializer();
			verify(serializationManager, times(1)).deserialize(Mockito.<Input> any());
			verify(executorService, times(1)).scheduleAtFixedRate(Mockito.<Runnable> any(), anyLong(), anyLong(), Mockito.<TimeUnit> any());
			assertThat(helper.isEmpty(), is(false));
		}

		@Test
		public void cacheFileExistsException() throws Exception {
			when(configurationStorage.isClassCacheExistsOnCmr()).thenReturn(true);
			new File(TEST_CACHE_FILE).createNewFile();
			when(serializationManager.deserialize(Mockito.<Input> any())).thenThrow(new SerializationException());

			helper.afterPropertiesSet();

			verify(prototypesProvider, times(1)).createSerializer();
			verify(executorService, times(1)).scheduleAtFixedRate(Mockito.<Runnable> any(), anyLong(), anyLong(), Mockito.<TimeUnit> any());
			assertThat(helper.isEmpty(), is(true));
		}

		@Test
		public void cacheFileExistsCacheOnCmrNot() throws Exception {
			when(configurationStorage.isClassCacheExistsOnCmr()).thenReturn(false);
			new File(TEST_CACHE_FILE).createNewFile();
			Object hashes = Collections.singleton("hash");
			when(serializationManager.deserialize(Mockito.<Input> any())).thenReturn(hashes);

			helper.afterPropertiesSet();

			verify(executorService, times(1)).scheduleAtFixedRate(Mockito.<Runnable> any(), anyLong(), anyLong(), Mockito.<TimeUnit> any());
			assertThat(helper.isEmpty(), is(true));
			assertThat(new File(TEST_CACHE_FILE).exists(), is(false));
			verifyZeroInteractions(serializationManager);
		}

		@Test
		public void initialInstrumentationPoints() throws Exception {
			String hash = "hash";
			InstrumentationDefinition instrumentationResult = mock(InstrumentationDefinition.class);
			Map<Collection<String>, InstrumentationDefinition> initInstrumentations = Collections.<Collection<String>, InstrumentationDefinition> singletonMap(Collections.singleton(hash),
					instrumentationResult);
			when(configurationStorage.getInitialInstrumentationResults()).thenReturn(initInstrumentations);

			helper.afterPropertiesSet();

			assertThat(helper.isEmpty(), is(false));
			assertThat(helper.isSent(hash), is(true));
			assertThat(helper.getInstrumentationResult(hash), is(instrumentationResult));
		}

		@Test
		public void noInitialInstrumentationPoints() throws Exception {
			when(configurationStorage.getInitialInstrumentationResults()).thenReturn(Collections.<Collection<String>, InstrumentationDefinition> emptyMap());

			helper.afterPropertiesSet();

			assertThat(helper.isEmpty(), is(true));
		}
	}

	public class Register extends ClassHashHelperTest {

		@Test
		public void register() throws Exception {
			helper.afterPropertiesSet();
			InstrumentationDefinition ir1 = mock(InstrumentationDefinition.class);
			String hash = "hash";

			helper.register(hash, ir1);

			boolean sent = helper.isSent(hash);
			InstrumentationDefinition instrumentationResult = helper.getInstrumentationResult(hash);
			assertThat(sent, is(true));
			assertThat(instrumentationResult, is(ir1));
		}

	}

	public class IsSent extends ClassHashHelperTest {

		@Test
		public void notSent() throws Exception {
			helper.afterPropertiesSet();
			String hash = "hash";

			boolean sent = helper.isSent(hash);

			assertThat(sent, is(false));
		}

		@Test
		public void saveAndLoad() throws Exception {
			String hash = "hash";
			helper.register(hash, null);
			helper.destroy();
			helper.afterPropertiesSet();

			boolean sent = helper.isSent(hash);

			assertThat(sent, is(true));
		}
	}

}
