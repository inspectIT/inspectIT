package rocks.inspectit.server.instrumentation.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.config.ConfigurationCreator;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ConfigurationHolderTest extends TestBase {

	@InjectMocks
	ConfigurationHolder holder;

	@Mock
	ConfigurationCreator configurationCreator;

	@Mock
	ConfigurationResolver configurationResolver;

	public class IsInitialized extends ConfigurationHolderTest {

		@Test
		public void notInitialziedWithoutEnvironment() {
			boolean initialized = holder.isInitialized();

			assertThat(initialized, is(false));
		}
	}

	public class Update extends ConfigurationHolderTest {

		@Test
		public void update() {
			long platformId = 11;
			Environment environment = mock(Environment.class);
			AgentConfig configuration = mock(AgentConfig.class);
			IInstrumentationApplier applier = mock(IInstrumentationApplier.class);
			when(configurationCreator.environmentToConfiguration(environment, platformId)).thenReturn(configuration);
			when(configurationResolver.getInstrumentationAppliers(environment)).thenReturn(Collections.singleton(applier));

			holder.update(environment, platformId);

			assertThat(holder.isInitialized(), is(true));
			assertThat(holder.getEnvironment(), is(environment));
			assertThat(holder.getAgentConfiguration(), is(configuration));
			assertThat(holder.getInstrumentationAppliers(), hasSize(1));
			assertThat(holder.getInstrumentationAppliers(), hasItem(applier));

			verify(configurationCreator).environmentToConfiguration(environment, platformId);
			verify(configurationResolver).getInstrumentationAppliers(environment);
			verifyNoMoreInteractions(configurationCreator, configurationResolver);
		}

		@Test
		public void updateReset() {
			long platformId = 11;
			Environment environment = mock(Environment.class);
			AgentConfig configuration = mock(AgentConfig.class);
			IInstrumentationApplier applier = mock(IInstrumentationApplier.class);
			when(configurationCreator.environmentToConfiguration(environment, platformId)).thenReturn(configuration);
			when(configurationResolver.getInstrumentationAppliers(environment)).thenReturn(Collections.singleton(applier));

			holder.update(environment, platformId);
			holder.update(null, platformId);

			assertThat(holder.isInitialized(), is(false));

			// only one time verifications
			verify(configurationCreator).environmentToConfiguration(environment, platformId);
			verify(configurationResolver).getInstrumentationAppliers(environment);
			verifyNoMoreInteractions(configurationCreator, configurationResolver);
		}
	}
}
