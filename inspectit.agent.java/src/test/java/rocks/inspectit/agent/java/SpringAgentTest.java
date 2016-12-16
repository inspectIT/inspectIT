package rocks.inspectit.agent.java;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.analyzer.IByteCodeAnalyzer;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpringAgentTest extends TestBase {

	SpringAgent agent;

	@Mock
	IHookDispatcher hookDispatcher;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	IByteCodeAnalyzer byteCodeAnalyzer;

	@Mock
	IThreadTransformHelper threadTransformHelper;

	@BeforeMethod
	public void init() {
		// we need to init old way, because @InjectMocks on the SpringAgent won't work as it always
		// tries to invoke the constructor with File as argument and thus initialize the spring
		// context as well.
		agent = new SpringAgent();
		agent.byteCodeAnalyzer = byteCodeAnalyzer;
		agent.configurationStorage = configurationStorage;
		agent.hookDispatcher = hookDispatcher;
		agent.threadTransformHelper = threadTransformHelper;
	}

	public static class InspectByteCode extends SpringAgentTest {

		@Mock
		ClassLoader classLoader;

		@Test
		public void happyPath() {
			byte[] byteCode = "test".getBytes();
			byte[] instrumentedByteCode = "inst_test".getBytes();
			String className = "cls";
			IMatchPattern pattern = mock(IMatchPattern.class);
			when(pattern.match(className)).thenReturn(false);
			agent.ignoreClassesPatterns = Collections.singleton(pattern);
			when(byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader)).thenReturn(instrumentedByteCode);

			byte[] result = agent.inspectByteCode(byteCode, className, classLoader);

			assertThat(result, is(instrumentedByteCode));
			verify(threadTransformHelper).isThreadTransformDisabled();
			InOrder inOrder = inOrder(threadTransformHelper);
			inOrder.verify(threadTransformHelper, times(1)).setThreadTransformDisabled(true);
			inOrder.verify(threadTransformHelper, times(1)).setThreadTransformDisabled(false);
			verify(byteCodeAnalyzer).analyzeAndInstrument(byteCode, className, classLoader);
			verifyNoMoreInteractions(threadTransformHelper, byteCodeAnalyzer);
			verifyZeroInteractions(classLoader, hookDispatcher, configurationStorage);
		}

		@SuppressWarnings("unchecked")
		@Test
		public void analyzerThrowable() {
			byte[] byteCode = "test".getBytes();
			String className = "cls";
			IMatchPattern pattern = mock(IMatchPattern.class);
			when(pattern.match(className)).thenReturn(false);
			agent.ignoreClassesPatterns = Collections.singleton(pattern);
			when(byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader)).thenThrow(RuntimeException.class);

			byte[] result = agent.inspectByteCode(byteCode, className, classLoader);

			assertThat(result, is(byteCode));
			verify(threadTransformHelper).isThreadTransformDisabled();
			InOrder inOrder = inOrder(threadTransformHelper);
			inOrder.verify(threadTransformHelper, times(1)).setThreadTransformDisabled(true);
			inOrder.verify(threadTransformHelper, times(1)).setThreadTransformDisabled(false);
			verify(byteCodeAnalyzer).analyzeAndInstrument(byteCode, className, classLoader);
			verifyNoMoreInteractions(threadTransformHelper, byteCodeAnalyzer);
			verifyZeroInteractions(classLoader, hookDispatcher, configurationStorage);
		}

		@Test
		public void classIgnored() {
			byte[] byteCode = "test".getBytes();
			String className = "cls";
			IMatchPattern pattern = mock(IMatchPattern.class);
			when(pattern.match(className)).thenReturn(true);
			agent.ignoreClassesPatterns = Collections.singleton(pattern);

			byte[] result = agent.inspectByteCode(byteCode, className, classLoader);

			assertThat(result, is(byteCode));
			verify(threadTransformHelper).isThreadTransformDisabled();
			verifyNoMoreInteractions(threadTransformHelper);
			verifyZeroInteractions(classLoader, hookDispatcher, configurationStorage, byteCodeAnalyzer);
		}

		@Test
		public void threadTransformDisabled() {
			byte[] byteCode = "test".getBytes();
			String className = "cls";
			when(threadTransformHelper.isThreadTransformDisabled()).thenReturn(true);

			byte[] result = agent.inspectByteCode(byteCode, className, classLoader);

			assertThat(result, is(byteCode));

			verify(threadTransformHelper).isThreadTransformDisabled();
			verifyNoMoreInteractions(threadTransformHelper);
			verifyZeroInteractions(classLoader, hookDispatcher, configurationStorage, byteCodeAnalyzer);
		}

		@Test
		public void instrumenationDisabled() {
			byte[] byteCode = "test".getBytes();
			String className = "cls";
			agent.disableInstrumentation = true;

			byte[] result = agent.inspectByteCode(byteCode, className, classLoader);

			assertThat(result, is(byteCode));
			verifyZeroInteractions(classLoader, hookDispatcher, configurationStorage, byteCodeAnalyzer, threadTransformHelper);
		}
	}

	public static class GetHookDispatcher extends SpringAgentTest {

		@Test
		public void happyPath() {
			IHookDispatcher displatcher = agent.getHookDispatcher();

			assertThat(displatcher, is(hookDispatcher));
			verifyZeroInteractions(hookDispatcher, configurationStorage, byteCodeAnalyzer, threadTransformHelper);
		}
	}

	public static class ShouldClassBeIgnored extends SpringAgentTest {

		@Mock
		IMatchPattern pattern;

		@BeforeMethod
		public void initIgnoreClasses() {
			agent.ignoreClassesPatterns = Collections.singleton(pattern);
		}

		@Test
		public void ignored() {
			String className = "cls";
			when(pattern.match(className)).thenReturn(true);

			boolean ignored = agent.shouldClassBeIgnored(className);

			assertThat(ignored, is(true));
			verifyZeroInteractions(hookDispatcher, configurationStorage, byteCodeAnalyzer, threadTransformHelper);
		}

		@Test
		public void notIgnored() {
			String className = "cls";
			when(pattern.match(className)).thenReturn(false);

			boolean ignored = agent.shouldClassBeIgnored(className);

			assertThat(ignored, is(false));
			verifyZeroInteractions(hookDispatcher, configurationStorage, byteCodeAnalyzer, threadTransformHelper);
		}

		@Test
		public void instrumenationDisabled() {
			String className = "cls";
			agent.disableInstrumentation = true;

			boolean ignored = agent.shouldClassBeIgnored(className);

			assertThat(ignored, is(true));
			verifyZeroInteractions(pattern, hookDispatcher, configurationStorage, byteCodeAnalyzer, threadTransformHelper);
		}
	}

}
