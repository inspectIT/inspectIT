package rocks.inspectit.agent.java;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.instrument.Instrumentation;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.shared.all.instrumentation.config.impl.RetransformationStrategy;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class SpringAgentTest extends TestBase {

	SpringAgent agent;

	@Mock
	File inspectitJarFile;

	@Mock
	Instrumentation instrumentation;

	@BeforeMethod
	public void beforeMethod() {
		agent = new SpringAgent(inspectitJarFile, instrumentation);
	}

	public static class IsUsingRetransformation extends SpringAgentTest {

		@Mock
		IConfigurationStorage configurationStorage;

		@Test
		public void strategyAlways() throws StorageException {
			when(configurationStorage.getRetransformStrategy()).thenReturn(RetransformationStrategy.ALWAYS);
			agent.configurationStorage = configurationStorage;

			boolean result = agent.isUsingRetransformation();

			assertThat(result, is(true));
			verify(configurationStorage).getRetransformStrategy();
			verifyNoMoreInteractions(configurationStorage);
		}

		@Test
		public void strategyNever() throws StorageException {
			when(configurationStorage.getRetransformStrategy()).thenReturn(RetransformationStrategy.NEVER);
			agent.configurationStorage = configurationStorage;

			boolean result = agent.isUsingRetransformation();

			assertThat(result, is(false));
		}

		@Test
		public void noConfigurationStorage() {
			boolean result = agent.isUsingRetransformation();

			assertThat(result, is(true));
		}

		@Test
		public void bufferBooleanResult() throws StorageException {
			when(configurationStorage.getRetransformStrategy()).thenReturn(RetransformationStrategy.ALWAYS);
			agent.configurationStorage = configurationStorage;

			boolean result = agent.isUsingRetransformation();
			assertThat(result, is(true));
			result = agent.isUsingRetransformation();
			assertThat(result, is(true));
			result = agent.isUsingRetransformation();
			assertThat(result, is(true));

			verify(configurationStorage).getRetransformStrategy();
			verifyNoMoreInteractions(configurationStorage);
		}
	}
}
