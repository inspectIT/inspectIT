package rocks.inspectit.server.diagnosis.engine.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.Session.State;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleA;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleB;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleC;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link SessionFactoryTest} class.
 *
 * @author Alexander Wert
 *
 */
public class SessionFactoryTest extends TestBase {

	SessionFactory<String, DefaultSessionResult<String>> sessionFactory;

	@Mock
	DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> config;

	@Mock
	DefaultSessionResultCollector<String> resultCollector;

	/**
	 * Tests the {@link SessionFactory#makeObject()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class MakeObject extends SessionFactoryTest {
		@Test
		public void makeValid() throws Exception {
			Set<Class<?>> ruleClasses = new HashSet<>(Arrays.asList(RuleA.class, RuleB.class, RuleC.class));
			when(config.getRuleClasses()).thenReturn(ruleClasses);
			when(config.getResultCollector()).thenReturn(resultCollector);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return DefaultRuleOutputStorage.class;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);

			Session<String, DefaultSessionResult<String>> session = sessionFactory.makeObject();

			assertThat(session.getState(), equalTo(State.NEW));
			assertThat(session.getSessionContext().getRuleSet(), empty());

			session.activate("input", Collections.<String, Object> emptyMap());

			assertThat(session.getState(), equalTo(State.ACTIVATED));
			assertThat(session.getSessionContext().getRuleSet(), containsInAnyOrder(Rules.define(RuleA.class, RuleB.class, RuleC.class).toArray()));
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void missingRules() throws Exception {
			when(config.getRuleClasses()).thenReturn(Collections.<Class<?>> emptySet());
			when(config.getResultCollector()).thenReturn(resultCollector);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return DefaultRuleOutputStorage.class;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);

			sessionFactory.makeObject();
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void nullRules() throws Exception {
			when(config.getRuleClasses()).thenReturn(null);
			when(config.getResultCollector()).thenReturn(resultCollector);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return DefaultRuleOutputStorage.class;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);

			sessionFactory.makeObject();
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void nullResultCollector() throws Exception {
			Set<Class<?>> ruleClasses = new HashSet<>(Arrays.asList(RuleA.class, RuleB.class, RuleC.class));
			when(config.getRuleClasses()).thenReturn(ruleClasses);
			when(config.getResultCollector()).thenReturn(null);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return DefaultRuleOutputStorage.class;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);

			sessionFactory.makeObject();
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void nullStorage() throws Exception {
			Set<Class<?>> ruleClasses = new HashSet<>(Arrays.asList(RuleA.class, RuleB.class, RuleC.class));
			when(config.getRuleClasses()).thenReturn(ruleClasses);
			when(config.getResultCollector()).thenReturn(null);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return null;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);

			sessionFactory.makeObject();
		}
	}

	/**
	 * Tests the {@link SessionFactory#passivateObject(Object)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class PassivateObject extends SessionFactoryTest {

		@Test
		public void test() throws Exception {
			Set<Class<?>> ruleClasses = new HashSet<>(Arrays.asList(RuleA.class, RuleB.class, RuleC.class));
			when(config.getRuleClasses()).thenReturn(ruleClasses);
			when(config.getResultCollector()).thenReturn(resultCollector);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return DefaultRuleOutputStorage.class;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);
			Session<String, DefaultSessionResult<String>> session = sessionFactory.makeObject();
			session.activate("", Collections.<String, Object> emptyMap());

			sessionFactory.passivateObject(session);

			assertThat(session.getState(), equalTo(State.PASSIVATED));
		}
	}

	/**
	 * Tests the {@link SessionFactory#destroyObject(Session)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class DestroyObject extends SessionFactoryTest {

		@Test
		public void test() throws Exception {
			Set<Class<?>> ruleClasses = new HashSet<>(Arrays.asList(RuleA.class, RuleB.class, RuleC.class));
			when(config.getRuleClasses()).thenReturn(ruleClasses);
			when(config.getResultCollector()).thenReturn(resultCollector);
			Answer<Class<? extends IRuleOutputStorage>> callableAnswer = new Answer<Class<? extends IRuleOutputStorage>>() {
				@Override
				public Class<? extends IRuleOutputStorage> answer(InvocationOnMock invocation) throws Throwable {
					return DefaultRuleOutputStorage.class;
				}
			};
			doAnswer(callableAnswer).when(config).getStorageClass();
			sessionFactory = new SessionFactory<>(config);
			Session<String, DefaultSessionResult<String>> session = sessionFactory.makeObject();
			session.activate("", Collections.<String, Object> emptyMap());

			sessionFactory.destroyObject(session);

			assertThat(session.getState(), equalTo(State.DESTROYED));
		}
	}

}
